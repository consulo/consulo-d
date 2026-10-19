package io.github.intellij.dub.project.wizard;

import consulo.content.bundle.Sdk;
import io.github.intellij.dub.packageConfig.DubDsl;
import org.jspecify.annotations.Nullable;

/**
 * Port of upstream {@code DubNewProjectWizardData.kt}, without the observable properties of IntelliJ's wizard.
 */
public interface DubNewProjectWizardData {

    @Nullable Sdk getSdk();

    void setSdk(@Nullable Sdk sdk);

    DubDsl getDubDsl();

    void setDubDsl(DubDsl dubDsl);

    DubNewProjectWizardStep.DubProjectType getDubProjectType();

    void setDubProjectType(DubNewProjectWizardStep.DubProjectType dubProjectType);
}
