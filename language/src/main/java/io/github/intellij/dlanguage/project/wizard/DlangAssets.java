package io.github.intellij.dlanguage.project.wizard;

import java.util.StringJoiner;

/**
 * Port of upstream {@code DlangAssets.kt}. Upstream's {@code withDlangSampleCodeAsset} extensions of IntelliJ's
 * {@code AssetsNewProjectWizardStep} are done by {@code consulo.d.newProject.DNewModuleBuilderProcessor}.
 */
public final class DlangAssets {
    public static final DlangAssets INSTANCE = new DlangAssets();

    public static final String DEFAULT_FILE_NAME = "main.d";

    private static final String DEFAULT_TEMPLATE_WITH_ONBOARDING_TIPS_NAME = "SampleCodeWithOnboardingTips.d";

    private DlangAssets() {
    }

    public String getDlangSampleTemplateName() {
        return DEFAULT_TEMPLATE_WITH_ONBOARDING_TIPS_NAME;
    }

    public String getDSampleSourcePath(String sourceRootPath, String filename) {
        StringJoiner pathJoiner = new StringJoiner("/");
        // Consulo: upstream added sourceRootPath twice, which put the sample into source/source/
        pathJoiner.add(sourceRootPath);
        pathJoiner.add(filename);
        return pathJoiner.toString();
    }
}
