package consulo.d.dub.setting;

import consulo.externalSystem.model.setting.ExternalSystemExecutionSettings;
import org.jspecify.annotations.Nullable;

public class DubExecutionSettings extends ExternalSystemExecutionSettings {
    private final String myDubExecutable;

    public DubExecutionSettings(String dubExecutable) {
        myDubExecutable = dubExecutable;
    }

    public String getDubExecutable() {
        return myDubExecutable;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + myDubExecutable.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof DubExecutionSettings that && super.equals(o) && myDubExecutable.equals(that.myDubExecutable);
    }
}
