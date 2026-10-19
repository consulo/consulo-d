package io.github.intellij.dub.project.wizard;

import consulo.application.ApplicationPropertiesComponent;
import consulo.d.dub.newProject.DubNewModuleContext;
import consulo.d.localize.DLocalize;
import consulo.d.newProject.DNewModuleSetupStep;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.ui.RadioGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import io.github.intellij.dub.module.DlangDubModuleBuilder;
import io.github.intellij.dub.packageConfig.DubDsl;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Port of upstream {@code DubNewProjectWizardStep.kt} and the UI of {@code DubDlangNewProjectWizard.DubStep}: the D
 * compiler and the recipe format; the {@code dub init} type comes from the wizard entry. Building the module ({@code linkDubProject}
 * upstream) is done by {@code consulo.d.dub.newProject.DubNewModuleBuilderProcessor}.
 */
public class DubNewProjectWizardStep extends DNewModuleSetupStep<DubNewModuleContext> {
    private static final String DUB_DSL_STATE = "NewProjectWizard.dubDslState";

    private @Nullable RadioGroup<DubDsl> myDubDslGroup;

    public DubNewProjectWizardStep(DubNewModuleContext context) {
        super(context, new DlangDubModuleBuilder());
    }

    @Override
    protected LocalizeValue getSdkLabel() {
        return DLocalize.newProjectDubSdkLabel();
    }

    @RequiredUIAccess
    @Override
    protected void extendSettings(FormBuilder builder, Disposable uiDisposable) {
        setupDubFileLang(builder);
    }

    @RequiredUIAccess
    private void setupDubFileLang(FormBuilder builder) {
        RadioGroup<DubDsl> group = RadioGroup.create();
        builder.addLabeled(DLocalize.newProjectDubDslLabel(), group.fillHorizontal(List.of(DubDsl.values()), dubDsl -> switch (dubDsl) {
            case SDLANG -> DLocalize.newProjectDubDslSdlang();
            case JSON -> DLocalize.newProjectDubDslJson();
        }));
        group.setValue(readState(DUB_DSL_STATE, DubDsl.class, DubDsl.JSON));
        myDubDslGroup = group;
    }

    @Override
    public void onStepLeave(DubNewModuleContext context) {
        super.onStepLeave(context);

        RadioGroup<DubDsl> dubDslGroup = myDubDslGroup;
        DubDsl dubDsl = dubDslGroup != null ? dubDslGroup.getValue() : null;
        if (dubDsl != null) {
            context.setDubDsl(dubDsl);
            ApplicationPropertiesComponent.getInstance().setValue(DUB_DSL_STATE, dubDsl.name());
        }
    }

    private static <E extends Enum<E>> E readState(String key, Class<E> enumClass, E defaultValue) {
        String value = ApplicationPropertiesComponent.getInstance().getValue(key);
        if (value != null) {
            try {
                return Enum.valueOf(enumClass, value);
            }
            catch (IllegalArgumentException ignored) {
            }
        }
        return defaultValue;
    }

    public enum DubProjectType {
        MINIMAL("minimal"),
        VIBE("vibe.d"),
        DEIMOS("deimos");

        private final String myText;

        DubProjectType(String text) {
            myText = text;
        }

        public String getText() {
            return myText;
        }
    }
}
