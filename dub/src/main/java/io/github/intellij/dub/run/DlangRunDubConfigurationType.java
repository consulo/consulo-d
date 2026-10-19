package io.github.intellij.dub.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.execution.configuration.RunConfiguration;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import io.github.intellij.dlanguage.DLanguage;

@ExtensionImpl
public class DlangRunDubConfigurationType extends ConfigurationTypeBase {
    public static DlangRunDubConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(DlangRunDubConfigurationType.class);
    }

    public DlangRunDubConfigurationType() {
        super(
            "DlangRunDubConfigType",
            DLocalize.runDubText(),
            DLocalize.runDubDescr(),
            DLanguage.Icons.RUN
        );
        addFactory(new DLanguageFactory(this));
    }

    private static class DLanguageFactory extends ConfigurationFactory {
        DLanguageFactory(DlangRunDubConfigurationType type) {
            super(type);
        }

        @Override
        public RunConfiguration createTemplateConfiguration(Project project) {
            return new DlangRunDubConfiguration("DlangRunDubConfig", project, this);
        }

        @Override
        public boolean isApplicable(Project project) {
            String basePath = project.getBasePath();
            if (basePath == null) {
                return false;
            }
            String file = FileUtil.findFileInProvidedPath(basePath, "dub.json", "dub.sdl");
            return file != null && !file.isEmpty();
        }
    }
}
