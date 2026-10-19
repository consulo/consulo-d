package consulo.d.dub.setting;

import consulo.externalSystem.setting.ExternalProjectSettings;

public class DubProjectSettings extends ExternalProjectSettings {
    private String myDubExecutable = "";

    /**
     * @return the {@code dub} executable to run, empty to take {@code dub} from {@code PATH}
     */
    public String getDubExecutable() {
        return myDubExecutable;
    }

    public void setDubExecutable(String dubExecutable) {
        myDubExecutable = dubExecutable;
    }

    @Override
    public DubProjectSettings clone() {
        DubProjectSettings clone = new DubProjectSettings();
        copyTo(clone);
        clone.myDubExecutable = myDubExecutable;
        return clone;
    }
}
