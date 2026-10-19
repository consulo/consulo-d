package io.github.intellij.dlanguage.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.ApplicationPropertiesComponent;
import consulo.configurable.ProjectConfigurable;
import consulo.configurable.SearchableConfigurable;
import consulo.configurable.StandardConfigurableIds;
import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.cmd.ParametersListUtil;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.process.util.CapturingProcessUtil;
import consulo.project.Project;
import consulo.ui.Alerts;
import consulo.ui.Button;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.Tab;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.UIAccess;
import consulo.ui.ValueComponent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.TabbedLayout;
import consulo.ui.style.ComponentColors;
import consulo.ui.util.FormBuilder;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.messagebus.ToolChangeListener;
import io.github.intellij.dlanguage.messagebus.Topics;
import io.github.intellij.dlanguage.tools.DtoolUtils;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.intellij.dlanguage.tools.DtoolUtils.*;

/**
 * The "D Tools" option in: Settings -> Languages & Frameworks -> D Tools.
 * <p>
 * Consulo: the page is built with Consulo UI instead of upstream's Swing form and sits under "Build, Execution,
 * Deployment" (Consulo has no "Languages &amp; Frameworks" group). It has the DScanner and DFormat tabs only: DCD is
 * not supported, the GDB path is not read by anything (the nativeDev debugger finds GDB itself), DUB settings are on
 * the DUB page, and Consulo has no API for upstream's {@code SearchableOptionContributor}.
 */
@ExtensionImpl
public class DLanguageToolsConfigurable implements ProjectConfigurable, SearchableConfigurable {

    public static final String D_TOOLS_ID = "D Tools";

    private static final Logger LOG = Logger.getInstance(DLanguageToolsConfigurable.class);
    private final Project project;
    private final ApplicationPropertiesComponent propertiesComponent;
    private final List<Tool> properties = new ArrayList<>();
    private final List<Tab> tabs = new ArrayList<>();
    private final List<LocalizeValue> tabTitles = new ArrayList<>();

    @Inject
    public DLanguageToolsConfigurable(final Project project, final ApplicationPropertiesComponent propertiesComponent) {
        this.project = project;
        this.propertiesComponent = propertiesComponent;
    }

    @Override
    public String getId() {
        return D_TOOLS_ID;
    }

    @Override
    public @Nullable String getParentId() {
        return StandardConfigurableIds.EXECUTION_GROUP;
    }

    @Override
    public @Nullable Runnable enableSearch(final String option) {
        return () -> {
            if(StringUtil.isNotEmpty(option)) {
                final int tabCount = tabs.size();

                for (int i = 0; i < tabCount-1; i++) {
                    final String title = tabTitles.get(i).get();
                    if(title.toLowerCase().contains(option.toLowerCase())) {
                        tabs.get(i).select();
                    }
                }
            }

        };
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.dToolsDisplayName();
    }

    @RequiredUIAccess
    @Override
    public Component createUIComponent(final Disposable parentDisposable) {
        properties.clear();
        tabs.clear();
        tabTitles.clear();

        final TabbedLayout tabbedPane = TabbedLayout.create();
        addTool(tabbedPane, parentDisposable, DLocalize.dToolsTabDscanner(), "dscanner", ToolKey.DSCANNER_KEY, DSCANNER_LATEST,
            Topics.DSCANNER_TOOL_CHANGE);
        addTool(tabbedPane, parentDisposable, DLocalize.dToolsTabDfmt(), "dfmt", ToolKey.DFORMAT_KEY, DFORMAT_LATEST,
            Topics.DFMT_TOOL_CHANGE);
        return tabbedPane;
    }

    @RequiredUIAccess
    private void addTool(final TabbedLayout tabbedPane,
                         final Disposable parentDisposable,
                         final LocalizeValue title,
                         final String command,
                         final ToolKey key,
                         final @Nullable String latestVersion,
                         final Class<? extends ToolChangeListener> topic) {
        final FileChooserDescriptor dToolchooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false)
            .withTitle(DLocalize.dToolsSelectExecutable(command))
            .withShowHiddenFiles(false)
            .withFileFilter(vf -> vf.getNameWithoutExtension().equalsIgnoreCase(command));

        final FileChooserTextBoxBuilder.Controller pathField = FileChooserTextBoxBuilder.create(project)
            .uiDisposable(parentDisposable)
            .dialogTitle(DLocalize.dToolsSelectExecutable(command))
            .fileChooserDescriptor(dToolchooserDescriptor)
            .build();
        final TextBoxWithExpandAction flagsField = TextBoxWithExpandAction.create(
            PlatformIconGroup.actionsShow(),
            "",
            ParametersListUtil.DEFAULT_LINE_PARSER,
            ParametersListUtil.DEFAULT_LINE_JOINER
        );
        final Label versionField = Label.create();
        final Button autoFindButton = Button.create(DLocalize.dToolsAutoFind());

        final FormBuilder builder = FormBuilder.create();
        builder.addLabeled(DLocalize.dToolsPathLabel(), DockLayout.create().center(pathField.getComponent()).right(autoFindButton));
        builder.addLabeled(DLocalize.dToolsInfoLabel(), versionField);
        builder.addLabeled(DLocalize.dToolsFlagsLabel(), flagsField);

        tabs.add(tabbedPane.addTab(title, builder.build()));
        tabTitles.add(title);
        properties.add(new Tool(project, command, key, pathField, flagsField, autoFindButton, versionField, latestVersion, topic));
    }

    /**
     * Enables the apply button if anything changed.
     */
    @RequiredUIAccess
    @Override
    public boolean isModified() {
        for (final Tool t : properties) {
            if (t.isModified()) {
                t.setDirty(true);

                final String exePath = StringUtil.trim(t.pathField.getValue());

                if(StringUtil.isNotEmpty(exePath)) {
                    final Path path = Paths.get(exePath);

                    if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                        t.versionField.setText(DLocalize.dToolsPathDoesNotExist());
                        t.versionField.setForegroundColor(ComponentColors.ERROR_FOREGROUND);
                    } else if(!Files.isExecutable(path)) {
                        t.versionField.setText(DLocalize.dToolsPathIsNotExecutable());
                        t.versionField.setForegroundColor(ComponentColors.ERROR_FOREGROUND);
                    } else {
                        // then it's a valid path to the tool
                        t.updateVersion();
                        return true;
                    }
                } else {
                    // allow it to be set to a blank value
                    t.updateVersion();
                    return true;
                }
            } else if (t.isDirty()) {
                t.updateVersion();
                t.setDirty(false);
            }
        }
        return false;
    }

    /**
     * Triggered when the user pushes the apply button.
     */
    @RequiredUIAccess
    @Override
    public void apply() {
        updateVersionInfoFields();
        saveState();
    }

    /**
     * Triggered when the user pushes the cancel button.
     */
    @RequiredUIAccess
    @Override
    public void reset() {
        restoreState();
    }

    @RequiredUIAccess
    @Override
    public void disposeUIResources() {

        for (Tool tool: properties) {
            tool.cancelUpdate();
        }
    }

    /**
     * Updates the version info fields for all files configured.
     */
    @RequiredUIAccess
    private void updateVersionInfoFields() {
        properties.forEach(Tool::updateVersion);
    }

    /**
     * Persistent save of the current state.
     */
    @RequiredUIAccess
    private void saveState() {
        LOG.info("Saving D Tools Config");
        for (final Property property : properties) {
            property.saveState();
        }
    }

    /**
     * Restore components to the initial state.
     */
    @RequiredUIAccess
    private void restoreState() {
        LOG.info("Restore D Tools Config");
        for (final Property property : properties) {
            property.restoreState();
        }
    }

    interface Property {

        @RequiredUIAccess
        boolean isModified();

        @RequiredUIAccess
        void saveState();

        @RequiredUIAccess
        void restoreState();
    }

    interface Versioned {

        @RequiredUIAccess
        void updateVersion();
    }

    /**
     * Manages the state of a PropertyComponent and its respective field.
     */
    class PropertyField implements Property {

        public final ValueComponent<String> field;
        private final String propertyKey;
        private String oldValue;

        @RequiredUIAccess
        PropertyField(final String propertyKey, final ValueComponent<String> field) {
            this(propertyKey, field, "");
        }

        @RequiredUIAccess
        PropertyField(final String propertyKey, final ValueComponent<String> field,
            final String defaultValue) {
            this.propertyKey = propertyKey;
            this.field = field;
            this.oldValue = propertiesComponent.getValue(propertyKey, defaultValue);
            field.setValue(oldValue);
        }

        @RequiredUIAccess
        @Override
        public boolean isModified() {
            return !getText().equals(oldValue);
        }

        @RequiredUIAccess
        @Override
        public void saveState() {
            propertiesComponent.setValue(propertyKey, oldValue = getText());
        }

        @RequiredUIAccess
        @Override
        public void restoreState() {
            field.setValue(oldValue);
        }

        @RequiredUIAccess
        private String getText() {
            return StringUtil.notNullize(field.getValue());
        }
    }

    /**
     * Manages the group of fields which reside to a particular tool.
     */
    class Tool implements Property, Versioned {

        public final Project project;
        public final String command;
        public final ToolKey key;
        final FileChooserTextBoxBuilder.Controller pathField;
        final TextBoxWithExpandAction flagsField;
        final Label versionField;
        final @Nullable String latestVersion;
        final Button autoFindButton;
        final List<PropertyField> propertyFields;
        final Class<? extends ToolChangeListener> topic;
        private final ToolChangeListener publisher;
        private boolean dirty = false;

        private @Nullable Future<?> updateInProgress;

        @RequiredUIAccess
        Tool(final Project project, final String command, final ToolKey key,
             final FileChooserTextBoxBuilder.Controller pathField,
             final TextBoxWithExpandAction flagsField, final Button autoFindButton,
             final Label versionField,
             final @Nullable String latestVersion,
             final Class<? extends ToolChangeListener> topic) {
            this.project = project;
            this.command = command;
            this.key = key;
            this.pathField = pathField;
            this.flagsField = flagsField;
            this.versionField = versionField;
            this.latestVersion = latestVersion;
            this.autoFindButton = autoFindButton;
            this.topic = topic;
            this.publisher = project.getMessageBus().syncPublisher(topic);

            this.propertyFields = Arrays.asList(
                new PropertyField(key.getPathKey(), pathField.getComponent()),
                new PropertyField(key.getFlagsKey(), flagsField));

            autoFindButton.addClickListener(event -> new LocateToolListener(pathField, command).actionPerformed());

            updateVersion();
        }

        public boolean isDirty() {
            return dirty;
        }

        void setDirty(final boolean dirty) {
            this.dirty = dirty;
        }

        @RequiredUIAccess
        @Override
        public void updateVersion() {
            updateVersionField(this);
        }

        @RequiredUIAccess
        @Override
        public boolean isModified() {
            return propertyFields.stream().anyMatch(PropertyField::isModified);
        }

        @RequiredUIAccess
        @Override
        public void saveState() {
            if (isModified()) {
                final String pathText = StringUtil.notNullize(StringUtil.trim(pathField.getValue()));
                publisher.onToolSettingsChanged(new ToolSettings(pathText, StringUtil.notNullize(flagsField.getValue())));
            }
            for (final PropertyField propertyField : propertyFields) {
                propertyField.saveState();
            }
        }

        @RequiredUIAccess
        @Override
        public void restoreState() {
            for (final PropertyField propertyField : propertyFields) {
                propertyField.restoreState();
            }
        }

        public void setUpdateInProgress(Future<?> future) {
            cancelUpdate();
            updateInProgress = future;
        }

        public void cancelUpdate() {
            if (updateInProgress != null) {
                updateInProgress.cancel(true);
            }
            updateInProgress = null;
        }
    }

    @RequiredUIAccess
    private void updateVersionField(final Tool tool) {
        final @Nullable String pathText = StringUtil.trim(tool.pathField.getValue());

        if(StringUtil.isEmptyOrSpaces(pathText)) {
            LOG.trace(String.format("unable to get %s version info as path is empty", tool.command));
            tool.versionField.setText(LocalizeValue.empty());
            tool.versionField.setForegroundColor(ComponentColors.DISABLED_TEXT);
            tool.versionField.setToolTipText(LocalizeValue.empty()); // turns the tool tip off
        } else if(!Paths.get(pathText).toFile().canExecute()) {
            LOG.warn(String.format("unable to get %s version info for path: '%s'", tool.command, pathText));
            tool.versionField.setText(DLocalize.dToolsPathNotExecutable(pathText));
            tool.versionField.setForegroundColor(ComponentColors.ERROR_FOREGROUND);
            tool.versionField.setToolTipText(LocalizeValue.empty()); // turns the tool tip off
        } else {
            final GeneralCommandLine cmd = new GeneralCommandLine(pathText)
                .withCharset(StandardCharsets.UTF_8)
                .withParameters("--version");
            final UIAccess uiAccess = UIAccess.current();

            tool.setUpdateInProgress(Application.get()
                .executeOnPooledThread(() -> {
                    try {
                        final ProcessHandler handler = Application.get().getInstance(ProcessHandlerBuilderFactory.class)
                            .newBuilder(cmd)
                            .build();
                        handler.addProcessListener(new DlangToolVersionProcessAdapter(tool, uiAccess));
                        handler.startNotify();
                    } catch (final ExecutionException e) {
                        LOG.error("Could not run: " + cmd.getCommandLineString(), e);
                    }
                }));
        }
    }

    private static class LocateToolListener {
        private final FileChooserTextBoxBuilder.Controller pathField;
        private final String command;

        public LocateToolListener(final FileChooserTextBoxBuilder.Controller pathField, final String command) {
            this.pathField = pathField;
            this.command = command;
        }

        @RequiredUIAccess
        public void actionPerformed() {
            final String path = Optional.ofNullable(DtoolUtils.INSTANCE.lookInStandardDirectories(command)).orElseGet(this::locateViaCommandline);

            if (StringUtil.isNotEmpty(path)) {
                pathField.setValue(StringUtil.trim(path));
            } else {
                Alerts.okWarning(DLocalize.dToolsCouldNotFind(command))
                    .title(DLocalize.dToolsDialogTitle())
                    .showAsync(pathField.getComponent());
            }
        }

        /**
         * Attempt to find the D Tool by looking on the PATH
         *
         * @return either the found tool path or null
         */
        private @Nullable String locateViaCommandline() {
            final boolean isWindows = Platform.current().os().isWindows();
            final GeneralCommandLine cmd = new GeneralCommandLine()
                .withExePath(isWindows ? "cmd" : "/bin/sh")
                .withParameters(
                    isWindows ? "/c" : "-c",
                    isWindows ? "where" : "which",
                    command)
                ;

            try {
                final String path = Application.get().executeOnPooledThread(() -> {
                        try {
                            return CapturingProcessUtil.execAndGetOutput(cmd).getStdout();
                        } catch (final ExecutionException e) {
                            LOG.warn(String.format("Failed to run '%s'.", command), e);
                        }
                        return null;
                    })
                    .get(800, TimeUnit.MILLISECONDS);

                if (path != null && isWindows && path.contains("C:\\")) {
                    // not sure if this is actually needed. Was moved over from ExecUtil
                    final String[] split = path.split("(?=C:\\\\)");
                    LOG.info("Multiple paths found for " + command);
                    return split[0]; // if there are multiple results default to first one.
                }
                return path;
            } catch (final InterruptedException | java.util.concurrent.ExecutionException | TimeoutException e) {
                LOG.warn(String.format("Failed to run '%s'.", command), e);
            }
            return null;
        }
    }

    // A process handler for assign version output from D Tools to the appropriate fields
    // Consulo: a listener on a process handler from ProcessHandlerBuilderFactory, which updates the fields on the UI thread.
    private static class DlangToolVersionProcessAdapter implements ProcessListener {

        private final Tool tool;
        private final UIAccess uiAccess;
        private boolean complete;

        public DlangToolVersionProcessAdapter(final Tool tool, final UIAccess uiAccess) {
            this.tool = tool;
            this.uiAccess = uiAccess;
            this.complete = false;
        }

        @Override
        public void onTextAvailable(ProcessEvent event, Key outputType) {
            if(!complete && ProcessOutputTypes.STDOUT.equals(outputType)) {
                final String version = StringUtil.replace(event.getText(), "\n", "");
                final boolean predates = DtoolUtils.versionPredates(version, tool.latestVersion);
                uiAccess.give(() -> {
                    tool.versionField.setText(LocalizeValue.of(version));

                    if(predates) {
                        tool.versionField.setToolTipText(DLocalize.dToolsNewerVersionAvailable(tool.command));
                        tool.versionField.setForegroundColor(ComponentColors.ERROR_FOREGROUND);
                    } else {
                        tool.versionField.setForegroundColor(ComponentColors.DISABLED_TEXT);
                        tool.versionField.setToolTipText(LocalizeValue.empty()); // turns the tool tip off
                    }
                });
                complete = true;
            }
        }
    }
}
