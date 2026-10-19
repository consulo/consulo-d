package io.github.intellij.dlanguage;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.DocumentationOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.PlatformAwareSdkType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkType;
import consulo.d.localize.DLocalize;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Responsible for the mechanics when pressing "+" in the SDK configuration, as well as the project
 * SDK configuration.
 */
@ExtensionImpl
public class DlangSdkType extends PlatformAwareSdkType {

    private static final Logger LOG = Logger.getInstance(DlangSdkType.class);

    public static final String SDK_TYPE_ID = "D Compiler";

    private record DefaultPaths(File[] dmd, File[] documentation, File[] phobos, File[] druntime) {
    }

    private static DefaultPaths defaultPaths(Platform platform) {
        if (platform.os().isWindows()) {
            // This covers the default paths used by the Windows installer and therefore
            // https://chocolatey.org/packages/dmd is also covered (as it uses same process).
            return new DefaultPaths(
                new File[] {
                    new File("C:/D/dmd2/windows/bin/dmd.exe"),
                    new File("C:/D/dmd2/windows/bin64/dmd.exe")
                },
                new File[] {
                    new File("C:/D/dmd2/html/d")
                },
                new File[] {
                    new File("C:/D/dmd2/src/phobos")
                },
                new File[] {
                    new File("C:/D/dmd2/src/druntime/import")
                }
            );
        }
        else if (platform.os().isMac()) {
            // I use Homebrew on Mac. These paths may differ from what the .dmg does.
            return new DefaultPaths(
                new File[] {
                    new File("/usr/local/opt/dmd"), // installed via Homebrew
                    new File("/usr/local/bin"), // dmg install
                    new File("/opt/local/bin")
                },
                new File[] {},
                new File[] {
                    new File("/Library/D/dmd/src/phobos") // installed via Homebrew
                },
                new File[] {
                    new File("/Library/D/dmd/src/druntime/import") // installed via Homebrew
                }
            );
        }
        else if (platform.os().isUnix()) {
            // The official .rpm and .deb installers are the priority
            return new DefaultPaths(
                new File[] {
                    new File("/usr/bin"), // Debian, Ubuntu & Fedora (official .rpm), note that Arch also uses this path
                    new File("/usr/local/bin"),
                    new File("/snap/bin/dmd") // snapcraft.io (symlink to /snap/dmd/current/bin/dmd)
                },
                // the path to D documentation should contain "index.html"
                new File[] {
                    new File("/usr/share/dmd/html/d"), // Fedora (official .rpm)
                    new File("/usr/local/share/dmd/html/d"), // Ubuntu
                    new File("/usr/share/d/html/d") // Arch Linux (dmd-docs package is not consistent with dlang & dlang-dmd package paths)
                },
                // the path to phobos should contain "etc.c.*" and "std.*"
                new File[] {
                    new File("/usr/include/dmd/phobos"), // Fedora (official .rpm)
                    new File("/usr/local/include/dmd/phobos"), // Ubuntu (should it be src/phobos?)
                    new File("/usr/include/dlang/dmd"), // Arch Linux uses non-standard directory structure (see: #457 and #743)
                    new File("/snap/dmd/current/import/phobos") // snapcraft.io
                },
                // the path to phobos should contain "core.*", "importc.h", and "object.d"
                new File[] {
                    new File("/usr/include/dmd/druntime/import"), // Fedora (official .rpm)
                    new File("/usr/local/include/dmd/druntime/import"), // Ubuntu (should it be src/druntime/import?)
                    new File("/usr/include/dlang/dmd"), // Arch Linux uses non-standard directory structure (see: #457 and #743)
                    new File("/snap/dmd/current/import/druntime") // snapcraft.io
                }
            );
        }
        else {
            LOG.warn(String.format("We didn't cater for %s", platform.os().name()));
            return new DefaultPaths(new File[] {}, new File[] {}, new File[] {}, new File[] {});
        }
    }

    public static DlangSdkType getInstance() {
        return Application.get().getExtensionPoint(SdkType.class).findExtensionOrFail(DlangSdkType.class);
    }

    public DlangSdkType() {
        super(SDK_TYPE_ID, DLocalize.compilersDlangGeneric(), DLanguage.Icons.FILE);
    }

    @Override
    public void collectHomePaths(Platform platform, Consumer<Path> pathConsumer) {
        File found = null;
        for (File f : defaultPaths(platform).dmd()) {
            if (f.exists()) {
                if (isValidSdkHome(platform, f.toPath())) {
                    found = f;
                    break;
                }
                else if (found == null) {
                    found = f;
                }
            }
        }
        if (found != null) {
            pathConsumer.accept(found.getAbsoluteFile().toPath());
        }
    }

    /**
     * When user sets up a D compiler path this method checks if specified path contains a D compiler executable.
     * <p>
     * We allow selecting dmd, ldc2, gdc, or opend
     * <p>
     * This method determines if it can run the dmd executable based on a home path that's passed in. So in the case
     * that the sdk home is "C:\D\dmd2\" then this method would return true if "C:\D\dmd2\windows\bin\dmd.exe" exists
     * and is executable.
     * <p>
     * On Linux it's likely that the compiler will be installed in a locations such as /usr/bin but a user could still
     * have extracted a tarball and be using that, so we may still need to look for a bin directory that contains a
     * supported compiler.
     *
     * @param path path to the root directory of a D compiler installation
     * @return true if the sdk home contains a executable dmd compiler
     */
    @Override
    public boolean isValidSdkHome(Platform platform, Path path) {
        File compilerBinary = findRelativeBinary(platform, path.toString());
        return compilerBinary != null && compilerBinary.exists() && compilerBinary.canExecute();
    }

    private @Nullable File findRelativeBinary(Platform platform, String sdkHome) {
        File sdkHomeFile = new File(sdkHome);

        if (sdkHomeFile.exists() && sdkHomeFile.isDirectory()) {
            // first look for any of the acceptable binaries in the current directory
            String binaryPath = FileUtil.findFileInProvidedPath(sdkHome, "dmd", "ldc2", "gdc", "opend");

            if (binaryPath == null || binaryPath.isEmpty()) {
                // does the dir have a bin directory in it?
                File relativeBin = Paths.get(sdkHome, "bin").toFile();
                if (relativeBin.exists() && relativeBin.isDirectory()) {
                    // look for any of the acceptable binaries in the bin directory
                    binaryPath = FileUtil.findFileInProvidedPath(relativeBin.getPath(), "dmd", "ldc2", "gdc", "opend");
                }
            }

            if ((binaryPath == null || binaryPath.isEmpty()) && platform.os().isWindows()) {
                // if the binary path is still null and we're on Windows, we need to handle the Windows install
                // location: 'C:\D\dmd2\windows\bin\dmd.exe'
                File windowsBin = Paths.get(sdkHome, "windows", "bin").toFile();
                if (windowsBin.exists() && windowsBin.isDirectory()) {
                    // look for dmd.exe or dmd64.exe bin directory
                    binaryPath = FileUtil.findFileInProvidedPath(windowsBin.getPath(), "dmd.exe", "dmd64.exe");
                }
            }

            return binaryPath != null ? new File(binaryPath) : null;
        }
        return null;
    }

    @Override
    public String suggestSdkName(Platform platform, @Nullable String currentSdkName, Path path) {
        String version = getCompilerVersion(platform, path.toString());

        return StringUtil.isNotEmpty(version) ? version : SDK_TYPE_ID;
    }

    @Override
    public @Nullable String getDefaultDocumentationUrl(Sdk sdk) {
        return "https://dlang.org/";
    }

    static class SetupStatus {
        private boolean runtime;
        private boolean phobos;
        private boolean documentation;

        SetupStatus(boolean runtime, boolean phobos, boolean documentation) {
            this.runtime = runtime;
            this.phobos = phobos;
            this.documentation = documentation;
        }

        boolean getRuntimeStatus() {
            return runtime;
        }

        boolean getPhobosStatus() {
            return phobos;
        }

        boolean getDocumentationStatus() {
            return documentation;
        }

        public void setRuntime(boolean runtime) {
            this.runtime = runtime;
        }

        public void setPhobos(boolean phobos) {
            this.phobos = phobos;
        }

        public void setDocumentation(boolean documentation) {
            this.documentation = documentation;
        }
    }

    /**
     * Windows has docs in 'C:\D\dmd2\html\d' and sources in [
     *   'C:\D\dmd2\src\dmd\dmd',
     *   'C:\D\dmd2\src\phobos',
     *   'C:\D\dmd2\src\druntime\import'
     * ]
     * OSX has docs in ??? and sources in
     * [
     *   '/Library/D/dmd/src/phobos',
     *   '/Library/D/dmd/src/druntime/import'
     * ]
     * Linux has docs in
     * '/usr/share/dmd/html/d' and sources in [
     *   '/usr/include/dmd/phobos',
     *   '/usr/include/dmd/druntime/import'
     * ]
     *
     * @param sdk The DMD installation
     */
    @Override
    public void setupSdkPaths(Sdk sdk) {
        SdkModificator sdkModificator = sdk.getSdkModificator();
        Platform platform = Platform.current();
        DefaultPaths defaultPaths = defaultPaths(platform);

        SetupStatus status = new SetupStatus(false, false, false);

        if (platform.os().isWindows()) {
            status = setupSDKPathsFromWindowsConfigFile(sdk, sdkModificator);
        }

        // documentation paths (todo: find out why using 'OrderRootType.DOCUMENTATION' didn't work)
        if (!status.getDocumentationStatus()) {
            setupDocumentationPath(sdkModificator, status);
        }

        // add phobos to classes & sources root
        if (!status.getPhobosStatus()) {
            setupPhobosPaths(sdkModificator, status, defaultPaths);
        }

        // add druntime to classes & sources root
        if (!status.getRuntimeStatus()) {
            setupRuntimePaths(sdkModificator, status, defaultPaths);
        }

        sdkModificator.commitChanges();
    }

    private Optional<VirtualFile> firstVirtualFileFrom(File[] files) {
        for (File f : files) {
            if (f.isDirectory()) {
                return Optional.ofNullable(
                    LocalFileSystem.getInstance().findFileByPath(f.getAbsolutePath()));
            }
        }
        return Optional.empty();
    }

    private void setupRuntimePaths(SdkModificator sdkModificator, SetupStatus status, DefaultPaths defaultPaths) {
        Optional<VirtualFile> druntimeSource = firstVirtualFileFrom(defaultPaths.druntime());
        if (druntimeSource.isPresent()) {
            sdkModificator.addRoot(druntimeSource.get(), BinariesOrderRootType.ID);
            sdkModificator.addRoot(druntimeSource.get(), SourcesOrderRootType.ID);
            status.setRuntime(true);
        }
        else {
            status.setRuntime(false);
        }
    }

    private void setupPhobosPaths(SdkModificator sdkModificator, SetupStatus status, DefaultPaths defaultPaths) {
        Optional<VirtualFile> phobosSource = firstVirtualFileFrom(defaultPaths.phobos());
        if (phobosSource.isPresent()) {
            sdkModificator.addRoot(phobosSource.get(), BinariesOrderRootType.ID);
            sdkModificator.addRoot(phobosSource.get(), SourcesOrderRootType.ID);
            status.setPhobos(true);
        }
        else {
            status.setPhobos(false);
        }
    }

    private void setupDocumentationPath(SdkModificator sdkModificator, SetupStatus status) {
        // We can add URLs as documentation links to the SDK
        sdkModificator.addRoot("https://dlang.org/spec/spec.html", DocumentationOrderRootType.ID);
        sdkModificator.addRoot("https://dlang.org/phobos/", DocumentationOrderRootType.ID);

        status.setDocumentation(true); // todo: adding local file-based documentation doesn't work but its not clear why
    }

    private SetupStatus setupSDKPathsFromWindowsConfigFile(Sdk sdk, SdkModificator sdkModificator) {
        String dmd_path = getDlangCompilerPath(sdk);
        GeneralCommandLine cmd = new GeneralCommandLine(dmd_path);

        try {
            ProcessOutput output = CapturingProcessUtil.execAndGetOutput(cmd, 6_000);

            Optional<String> configFile = output
                .getStdoutLines().stream()
                .filter(line -> line.contains("Config file: "))
                .map(line -> line.replace("Config file:", "").trim())
                .findFirst();

            if (configFile.isEmpty()) {
                return new SetupStatus(false, false, false);
            }
            File file = new File(configFile.get());
            if (!file.exists()) {
                return new SetupStatus(false, false, false);
            }
            //DFLAGS="-I%@P%\..\..\src\phobos" "-I%@P%\..\..\src\druntime\import"
            String[] phobos = new String[1];
            Pattern phobosPattern = Pattern
                .compile("\"-I%@P%([\\.\\\\A-Za-z]+phobos[\\.\\\\A-Za-z]*)\"");

            String[] druntime = new String[1];
            Pattern druntimePattern = Pattern
                .compile("\"-I%@P%([\\.\\\\A-Za-z]+druntime\\\\import[\\.\\\\A-Za-z]*)\"");

            try (Stream<String> lines = Files.lines(file.toPath())) {
                lines.forEach(line -> {
                    if (line.contains("DFLAGS=")) {
                        Matcher phobosMatcher = phobosPattern.matcher(line);
                        Matcher druntimeMatcher = druntimePattern.matcher(line);
                        if (phobosMatcher.find()) {
                            phobos[0] = phobosMatcher.group(1);
                        }
                        if (druntimeMatcher.find()) {
                            druntime[0] = druntimeMatcher.group(1);
                        }
                    }
                });
            }
            String phobosPath = (new File(dmd_path)).getParent() + phobos[0];
            String druntimePath = (new File(dmd_path)).getParent() + druntime[0];
            File phobosFile = new File(phobosPath);
            File druntimeFile = new File(druntimePath);
            if (phobosFile.exists() && druntimeFile.exists()) {
                VirtualFile phobosVirtualFile = LocalFileSystem.getInstance().findFileByPath(phobosFile.getAbsolutePath());
                VirtualFile druntimeVirtualFile = LocalFileSystem.getInstance().findFileByPath(druntimeFile.getAbsolutePath());
                if (phobosVirtualFile == null || druntimeVirtualFile == null) {
                    return new SetupStatus(false, false, false);
                }
                sdkModificator.addRoot(phobosVirtualFile, BinariesOrderRootType.ID);
                sdkModificator.addRoot(druntimeVirtualFile, BinariesOrderRootType.ID);

                sdkModificator.addRoot(phobosVirtualFile, SourcesOrderRootType.ID);
                sdkModificator.addRoot(druntimeVirtualFile, SourcesOrderRootType.ID);
                return new SetupStatus(true, true, false);
            }
            else {
                return new SetupStatus(false, false, false);
            }

        }
        catch (IOException | ExecutionException e) {
            LOG.error(e);
            return new SetupStatus(false, false, false);
        }
    }

    @Override
    public @Nullable String getVersionString(Platform platform, Path path) {
        String version = getCompilerVersion(platform, path.toString());

        if (StringUtil.isNotEmpty(version)) {
            Matcher m = Pattern.compile("(?:.*v)(.+)").matcher(version);
            return m.matches() ? m.group(1) : null;
        }

        return null;
    }

    /**
     * Try to execute the compiler binary with '--version' and return first line of the output.
     *
     * @param sdkHome path to D compiler or the compiler's home directory (if on Windows or non-system install on unix)
     * @return String containing DMD version or null
     */
    private @Nullable String getCompilerVersion(Platform platform, String sdkHome) {
        File dCompilerBinary = findRelativeBinary(platform, sdkHome);
        if (dCompilerBinary != null && isValidSdkHome(platform, Path.of(sdkHome))) {
            GeneralCommandLine cmd = new GeneralCommandLine();
            //cmd.withWorkDirectory(sdkHome.getAbsolutePath());
            cmd.setExePath(dCompilerBinary.getAbsolutePath());
            cmd.addParameter("--version");

            try {
                ProcessOutput output = CapturingProcessUtil.execAndGetOutput(cmd, 2_000);

                //Parse output of a DMD compiler
                List<String> outputLines = output.getStdoutLines();
                if (!outputLines.isEmpty()) {
                    String version = outputLines.getFirst().trim();
                    LOG.debug(String.format("Found version: %s", version));
                    return version;
                }
            }
            catch (ExecutionException e) {
                LOG.error(String.format("There was a problem running '%s --version'", dCompilerBinary.getAbsolutePath()), e);
            }
        }
        return null;
    }

    /**
     * Returns full path to a D compiler executable for the given Sdk (based on the home path).
     * If the Sdk doesn't have a valid value then this method will simply return "dmd" in the hope that it's on the PATH
     *
     * @param sdk an Sdk of {@link DlangSdkType}
     * @return Either the absolute path to a supported D compiler or simply "dmd"
     */
    public String getDlangCompilerPath(Sdk sdk) {
        String homePath = sdk.getHomePath();

        if (homePath != null) {
            File dCompilerBinary = findRelativeBinary(Platform.current(), homePath);
            if (dCompilerBinary != null && dCompilerBinary.exists() && dCompilerBinary.canExecute()) {
                return dCompilerBinary.getAbsolutePath();
            }
        }

        LOG.warn(String.format("Home path '%s' for dlang sdk was not valid. Falling back to 'dmd'", homePath));

        return "dmd"; // it may just be on the PATH
    }
}
