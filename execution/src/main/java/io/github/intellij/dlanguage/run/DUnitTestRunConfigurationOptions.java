package io.github.intellij.dlanguage.run;

import com.dslplatform.json.CompiledJson;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class DUnitTestRunConfigurationOptions extends RunConfigurationOptions {
    private @Nullable String myDFilePath;

    @Attribute("dFilePath")
    public @Nullable String getDFilePath() {
        return myDFilePath;
    }

    public void setDFilePath(@Nullable String dFilePath) {
        myDFilePath = StringUtil.nullize(dFilePath, true);
    }
}
