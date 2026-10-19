package io.github.intellij.dub.project.settings;

import consulo.d.dub.setting.DubSettings;
import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.externalSystem.service.setting.ExternalSystemSettingsConfigurable;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import io.github.intellij.dlanguage.settings.ToolKey;
import io.github.intellij.dub.service.DubBinaryPathProvider;
import org.jspecify.annotations.Nullable;

/**
 * The DUB executable used when a linked DUB package does not name its own: upstream's {@code DubConfigurablePanel},
 * shown as the system settings of the DUB page ({@link DubConfigurable}).
 */
public class DubConfigurablePanel extends ExternalSystemSettingsConfigurable<DubSettings> implements ImprovedDisposable {

    private final UiDebouncer myUiDebouncer = new UiDebouncer(this, 400);

    private @Nullable Label myVersionLabel;

    private @Nullable TextBox myExecutablePath;

    private boolean myIsDisposed = false;

    public DubConfigurablePanel(DubSettings settings) {
        super(settings);
    }

    @Override
    public void dispose() {
        myIsDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return myIsDisposed;
    }

    @RequiredUIAccess
    @Override
    public Component createUIComponent(Disposable parentDisposable) {
        myIsDisposed = false;
        Disposer.register(parentDisposable, this);

        // Consulo: no animated icon in the unified UI
        Label versionLabel = Label.create(DLocalize.settingsDubExecutableDetecting());
        myVersionLabel = versionLabel;

        FileChooserTextBoxBuilder.Controller executablePathSelector = FileChooserTextBoxBuilder.create(getSettings().getProject())
            .uiDisposable(parentDisposable)
            .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
            .dialogTitle(DLocalize.settingsDubExecutableChooserTitle())
            .build();
        TextBox executablePath = executablePathSelector.getComponent();
        myExecutablePath = executablePath;
        String path = ToolKey.DUB_KEY.getPath();
        executablePath.setValue(path != null ? path : "");
        executablePath.setPlaceholder(DLocalize.settingsDubExecutableDetecting());
        executablePath.addValueListener(event -> {
            executablePath.setPlaceholder(DLocalize.settingsDubExecutableDetecting());
            update();
        });

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(DLocalize.settingsDubExecutableLocationLabel(), executablePath);
        builder.addBottom(versionLabel);

        update();
        return builder.build();
    }

    @RequiredUIAccess
    @Override
    public boolean isModified() {
        String text = getExecutablePath();
        String path = ToolKey.DUB_KEY.getPath();
        if (path == null) {
            return !text.isEmpty();
        }
        else {
            return !text.equals(path);
        }
    }

    @RequiredUIAccess
    @Override
    public void apply() {
        ToolKey.DUB_KEY.setPath(getExecutablePath());
    }

    @RequiredUIAccess
    private String getExecutablePath() {
        TextBox executablePath = myExecutablePath;
        String text = executablePath != null ? executablePath.getValue() : null;
        return text != null ? text : "";
    }

    /**
     * Consulo: the text is read on the UI thread, and the DUB binary is checked in the pooled thread with the lookup.
     */
    @RequiredUIAccess
    public void update() {
        String text = getExecutablePath();
        myUiDebouncer.run(
            () -> {
                String dubPath = text.isEmpty() ? DubBinaryPathProvider.INSTANCE.getAutodetectPath() : text;
                if (dubPath == null || dubPath.isEmpty()) {
                    return new DubCheck(dubPath, false, "");
                }
                boolean valid = DubBinaryPathProvider.INSTANCE.verifyDubBinary(dubPath);
                return new DubCheck(dubPath, valid, valid ? DubBinaryPathProvider.INSTANCE.getVersionOutput(dubPath) : "");
            },
            check -> {
                Label versionLabel = myVersionLabel;
                TextBox executablePath = myExecutablePath;
                if (versionLabel == null || executablePath == null) {
                    return;
                }
                String dubPath = check.dubPath();
                if (dubPath == null || dubPath.isEmpty()) {
                    // not provided and not found
                    versionLabel.setImage(PlatformIconGroup.generalError());
                    versionLabel.setText(DLocalize.settingsDubExecutableNotFound());
                }
                else if (check.valid()) {
                    versionLabel.setImage(PlatformIconGroup.generalInspectionsok());
                    versionLabel.setText(DLocalize.settingsDubExecutableVersion(check.version()));
                    executablePath.setPlaceholder(DLocalize.settingsDubExecutableDetected(dubPath));
                }
                else {
                    versionLabel.setImage(PlatformIconGroup.generalError());
                    versionLabel.setText(DLocalize.settingsDubExecutableInvalid());
                }
            }
        );
    }

    private record DubCheck(@Nullable String dubPath, boolean valid, String version) {
    }
}
