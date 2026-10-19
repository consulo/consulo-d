package io.github.intellij.dub.actions;

import consulo.annotation.component.ActionImpl;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.progress.Task;
import consulo.d.dub.DubNotificationGroupContributor;
import consulo.d.localize.DLocalize;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationService;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A users local dun repo (~/dub) can get large over time. If left for a number of years it can waste gigabytes
 * of disk space. This action should be triggered (either manually or automatically) to scan the users local dub
 * repository directory and suggest directories that can be removed (older dependencies that are no longer being accessed).
 *
 * This should be based on the build cache
 *
 * @author Samael Bate (singingbush)
 * created on 10/08/2025
 */
@ActionImpl(id = "dub.repo-usage.action")
public class DubRepoUsageAction extends DumbAwareAction implements AnActionWithSyncUpdate {

    private static final long ONE_GiB = 1_073_741_824L; // (2^30 bytes)

    private static final Logger LOG = Logger.getInstance(DubRepoUsageAction.class);

    private final Path myLocalDubRepoCachePath = getUsersDubRepoDirectory("cache");
    private final Path myLocalDubRepoPackagesPath = getUsersDubRepoDirectory("packages");

    public DubRepoUsageAction() {
        super(DLocalize.dubActionsRepoUsageText(), DLocalize.dubActionsRepoUsageDescription(), null);
    }

    public Path getLocalDubRepoCachePath() {
        return myLocalDubRepoCachePath;
    }

    public Path getLocalDubRepoPackagesPath() {
        return myLocalDubRepoPackagesPath;
    }

    /*
     * On unix is should be '~/.dub' and on Windows it's '%LOCALAPPDATA%/dub'
     */
    private static Path getUsersDubRepoDirectory(String subDir) {
        Platform platform = Platform.current();
        return platform.os().isUnix()
            ? platform.user().homePath().resolve(".dub").resolve(subDir)
            : Paths.get(Objects.requireNonNull(platform.os().getEnvironmentVariable("LOCALAPPDATA")), "dub", subDir);
    }

    /**
     * Only enable the action in the UI of the IDE if the ~/.dub/packages and ~/.dub/cache exist
     */
    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(Files.exists(myLocalDubRepoCachePath) && Files.exists(myLocalDubRepoPackagesPath));
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        if (!Files.exists(myLocalDubRepoCachePath) || !Files.exists(myLocalDubRepoPackagesPath)) {
            // should not happen as the update function should ensure the UI is only enabled if the path exists
            return;
        }
        Project project = e.getData(Project.KEY);

        if (project != null) {
            if (project.isDisposed()) {
                return;
            }
            File usersDubCacheDir = myLocalDubRepoCachePath.toFile();
            if (!usersDubCacheDir.canRead()) {
                LOG.warn("Cannot read path: " + myLocalDubRepoCachePath);
                return;
            }

            File usersDubPackagesDir = myLocalDubRepoPackagesPath.toFile();
            if (!usersDubPackagesDir.canRead()) {
                LOG.warn("Cannot read path: " + myLocalDubRepoPackagesPath);
                return;
            }

            ScanLocalDubRepoTask scanDubRepoTask = new ScanLocalDubRepoTask(project, usersDubCacheDir, usersDubPackagesDir);

            ProgressManager.getInstance().run(scanDubRepoTask);
        }
    }

    public static class ScanLocalDubRepoTask extends Task.Backgroundable {
        private static final Logger LOG = Logger.getInstance(ScanLocalDubRepoTask.class);

        private final Project myP;
        private final File myUsersDubCacheDir;
        private final File myUsersDubPackagesDir;

        public ScanLocalDubRepoTask(Project p, File usersDubCacheDir, File usersDubPackagesDir) {
            super(p, DLocalize.dubActionsRepoUsageTaskTitle());
            myP = p;
            myUsersDubCacheDir = usersDubCacheDir;
            myUsersDubPackagesDir = usersDubPackagesDir;
        }

        public Project getP() {
            return myP;
        }

        public File getUsersDubCacheDir() {
            return myUsersDubCacheDir;
        }

        public File getUsersDubPackagesDir() {
            return myUsersDubPackagesDir;
        }

        @Override
        public void run(ProgressIndicator indicator) {
            // todo: let the user configure the threshold (default to 1GB) somewhere in settings
            long cacheDirSize = sizeOfFiles(myUsersDubCacheDir);

            long packagesDirSize = sizeOfFiles(myUsersDubPackagesDir);

            long totalSizeBytes = cacheDirSize + packagesDirSize;

            if (totalSizeBytes > ONE_GiB) {
                LOG.info("local dub repo (packages & cache) is using " + StringUtil.formatFileSize(cacheDirSize) + " of space");
            }

            // todo: allow the user to define a number of months or years that is considered old enough to no longer need
            Instant packageAgeThreshold = ZonedDateTime.now(ZoneId.of("UTC")).minusYears(1).toInstant();

            // go through all dub packages looking for old packages
            File[] dubCacheDirectories = Objects.requireNonNull(myUsersDubCacheDir.listFiles());
            File[] dubPackageDirectories = Objects.requireNonNull(myUsersDubPackagesDir.listFiles());
            double progressIncrement = 1.0 / (dubCacheDirectories.length + dubPackageDirectories.length);

            // status indicator needs to allow progress update
            indicator.setIndeterminate(false);

            Map<String, File> oldCaches = new HashMap<>();
            for (File packageDir : dubCacheDirectories) {
                if (packageDir.isDirectory()) {
                    for (File versionDir : Objects.requireNonNull(packageDir.listFiles())) {
                        if (versionDir.isDirectory()) {
                            indicator.setText(LocalizeValue.of(packageDir.getName() + " " + versionDir.getName()));

                            Instant lastUpdated = Instant.ofEpochMilli(versionDir.lastModified());

                            if (lastUpdated.isBefore(packageAgeThreshold)) {
                                oldCaches.put(packageDir.getName() + " " + versionDir.getName(), versionDir);
                            }
                        }
                    }
                }
                indicator.setFraction(indicator.getFraction() + progressIncrement);
            }

            Map<String, File> oldPackages = new HashMap<>();
            for (File packageDir : dubPackageDirectories) {
                if (packageDir.isDirectory()) {
                    for (File versionDir : Objects.requireNonNull(packageDir.listFiles())) {
                        if (versionDir.isDirectory()) {
                            indicator.setText(LocalizeValue.of(packageDir.getName() + " " + versionDir.getName()));

                            Instant lastUpdated = Instant.ofEpochMilli(versionDir.lastModified());

                            if (lastUpdated.isBefore(packageAgeThreshold)) {
                                oldPackages.put(packageDir.getName() + " " + versionDir.getName(), versionDir);
                            }
                        }
                    }
                }
                indicator.setFraction(indicator.getFraction() + progressIncrement);
            }

            // note that neither Jetbrains's StringUtil.formatFileSize or Apache's FileUtils.byteCountToDisplaySize
            // format large byte sizes properly. See:
            // https://youtrack.jetbrains.com/issue/IJSDK-2627
            // https://issues.apache.org/jira/browse/IO-876
            // For now we're using Jetbrains code but it may be worth writing our own
            String totalSizeTxt = StringUtil.formatFileSize(totalSizeBytes);

            List<File> allOldDirectories = new ArrayList<>(oldCaches.values());
            allOldDirectories.addAll(oldPackages.values());

            if (totalSizeBytes > ONE_GiB && (!oldCaches.isEmpty() || !oldPackages.isEmpty())) {

                NotificationService.getInstance()
                    .newInfo(DubNotificationGroupContributor.GROUP)
                    .title(DLocalize.dubActionsRepoUsageNotificationTitle("1 GiB"))
                    .content(DLocalize.dubActionsRepoUsageNotificationContent(totalSizeTxt, oldPackages.size(), oldCaches.size()))
                    .suggestion()
                    .important()
                    .addClosingAction(DLocalize.dubActionsRepoUsageRemoveDirectories(allOldDirectories.size()), () -> {
                        for (File directory : allOldDirectories) {
                            LOG.debug("deleting: " + directory.getAbsolutePath());
                            FileUtil.delete(directory.toPath());
                        }
                        NotificationService.getInstance()
                            .newInfo(DubNotificationGroupContributor.GROUP)
                            .title(DLocalize.dubActionsRepoUsageUpdateNotificationTitle())
                            .content(DLocalize.dubActionsRepoUsageRemovedNotificationContent(allOldDirectories.size()))
                            .notify(myP);
                    })
                    .notify(myP);
            }
            else {
                NotificationService.getInstance()
                    .newInfo(DubNotificationGroupContributor.GROUP)
                    .title(DLocalize.dubActionsRepoUsageUpdateNotificationTitle())
                    .content(DLocalize.dubActionsRepoUsageUpdateNotificationContent(totalSizeTxt))
                    .notify(myP);
            }
        }

        /**
         * The total length of the files under {@code directory}, as Kotlin's {@code walkTopDown()} visits them.
         */
        private static long sizeOfFiles(File directory) {
            if (directory.isFile()) {
                return directory.length();
            }
            long size = 0;
            File[] children = directory.listFiles();
            if (children != null) {
                for (File child : children) {
                    size += sizeOfFiles(child);
                }
            }
            return size;
        }
    }
}
