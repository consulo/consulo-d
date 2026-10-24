package consulo.d.execution.fragment;

import consulo.execution.configuration.fragment.TagRunConfigurationFragment;
import consulo.localize.LocalizeValue;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * An on/off option of a D run configuration, shown as a tag while it is set.
 */
public class DTagRunConfigurationFragment<T> implements TagRunConfigurationFragment<T> {
    private final String myId;
    private final LocalizeValue myName;
    private final Predicate<T> myGetter;
    private final BiConsumer<T, Boolean> mySetter;
    private LocalizeValue myGroup = LocalizeValue.empty();
    private @Nullable String myParentId;

    public DTagRunConfigurationFragment(String id, LocalizeValue name, Predicate<T> getter, BiConsumer<T, Boolean> setter) {
        myId = id;
        myName = name;
        myGetter = getter;
        mySetter = setter;
    }

    public DTagRunConfigurationFragment<T> withGroup(LocalizeValue group) {
        myGroup = group;
        return this;
    }

    public DTagRunConfigurationFragment<T> withParentId(String parentId) {
        myParentId = parentId;
        return this;
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
    public LocalizeValue getGroup() {
        return myGroup;
    }

    @Override
    public @Nullable String getParentId() {
        return myParentId;
    }

    @Override
    public boolean isSet(T target) {
        return myGetter.test(target);
    }

    @Override
    public void set(T target, boolean value) {
        mySetter.accept(target, value);
    }
}
