package consulo.d.newProject;

import consulo.content.bundle.SdkTable;
import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.module.creation.ui.UnifiedProjectOrModuleNameStep;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import io.github.intellij.dlanguage.module.DlangModuleBuilder;
import org.jspecify.annotations.Nullable;

/**
 * Name and location, then the D compiler (upstream's {@code IntellijDlangNewProjectWizard.DlangStep}).
 */
public class DNewModuleSetupStep<C extends DNewModuleContext> extends UnifiedProjectOrModuleNameStep<C> {
    private final DlangModuleBuilder myBuilder;

    private @Nullable BundleBox myBundleBox;

    public DNewModuleSetupStep(C context, DlangModuleBuilder builder) {
        super(context);
        myBuilder = builder;
    }

    @RequiredUIAccess
    @Override
    protected void extend(FormBuilder builder, Disposable uiDisposable) {
        super.extend(builder, uiDisposable);

        BundleBox bundleBox = BundleBoxBuilder.create(uiDisposable).withSdkTypeFilter(myBuilder::isSuitableSdkType).build();
        ComboBox<BundleBox.BundleBoxItem> component = bundleBox.getComponent();
        if (component.getDataModel().getSize() > 0) {
            component.setValueByIndex(0);
        }
        builder.addLabeled(getSdkLabel(), component);
        myBundleBox = bundleBox;

        extendSettings(builder, uiDisposable);
    }

    protected LocalizeValue getSdkLabel() {
        return DLocalize.newProjectDCompilerLabel();
    }

    /**
     * The settings after the D compiler.
     */
    @RequiredUIAccess
    protected void extendSettings(FormBuilder builder, Disposable uiDisposable) {
    }

    @Override
    public void onStepLeave(C context) {
        super.onStepLeave(context);

        BundleBox bundleBox = myBundleBox;
        String selectedBundleName = bundleBox != null ? bundleBox.getSelectedBundleName() : null;
        context.setSdk(selectedBundleName != null ? SdkTable.getInstance().findSdk(selectedBundleName) : null);
    }
}
