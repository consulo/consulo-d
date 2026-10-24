package consulo.d.execution.fragment;

import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentType;
import consulo.localize.LocalizeValue;

/**
 * A section of a D run configuration page; its fragments name it as their parent.
 */
public class DGroupRunConfigurationFragment<T> implements RunConfigurationFragment<T> {
    private final String myId;
    private final LocalizeValue myName;

    public DGroupRunConfigurationFragment(String id, LocalizeValue name) {
        myId = id;
        myName = name;
    }

    @Override
    public String getId() {
        return myId;
    }

    @Override
    public LocalizeValue getName() {
        return myName;
    }

    @Override
    public RunConfigurationFragmentType getType() {
        return RunConfigurationFragmentType.GROUP;
    }
}
