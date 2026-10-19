package io.github.intellij.dlanguage.unittest;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.ConfigurationTypeUtil;
import io.github.intellij.dlanguage.DLanguage;

@ExtensionImpl
public class DUnitTestRunConfigurationType extends ConfigurationTypeBase {

    public static DUnitTestRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(DUnitTestRunConfigurationType.class);
    }

    public DUnitTestRunConfigurationType() {
        super("DlangTestConfigType",
                DLocalize.dunitRunConfigurationTypeName(),
                DLocalize.dunitRunConfigurationTypeDescription(),
            DLanguage.Icons.RUN);
        addFactory(new DUnitTestRunConfigurationFactory(this));
    }
}
