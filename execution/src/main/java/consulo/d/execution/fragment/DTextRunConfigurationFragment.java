package consulo.d.execution.fragment;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.TextBox;
import consulo.ui.ValueComponent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A text option of a D run configuration, visible while it is set; hiding it clears the value.
 */
public class DTextRunConfigurationFragment<T> implements RunConfigurationFragment<T> {
    private final String myId;
    private final LocalizeValue myName;
    private final LocalizeValue myLabel;
    private final Function<T, @Nullable String> myGetter;
    private final BiConsumer<T, @Nullable String> mySetter;
    private LocalizeValue myGroup = LocalizeValue.empty();
    private @Nullable String myParentId;
    private Function<Project, ValueComponent<String>> myFieldFactory = project -> TextBox.create();

    public DTextRunConfigurationFragment(String id,
                                         LocalizeValue name,
                                         LocalizeValue label,
                                         Function<T, @Nullable String> getter,
                                         BiConsumer<T, @Nullable String> setter) {
        myId = id;
        myName = name;
        myLabel = label;
        myGetter = getter;
        mySetter = setter;
    }

    public DTextRunConfigurationFragment<T> withGroup(LocalizeValue group) {
        myGroup = group;
        return this;
    }

    public DTextRunConfigurationFragment<T> withParentId(String parentId) {
        myParentId = parentId;
        return this;
    }

    /**
     * The input of the value, a text box by default.
     */
    public DTextRunConfigurationFragment<T> withField(Function<Project, ValueComponent<String>> fieldFactory) {
        myFieldFactory = fieldFactory;
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
    public boolean isVisibleByDefault(T target) {
        return !StringUtil.isEmptyOrSpaces(myGetter.apply(target));
    }

    @Override
    public void applyHidden(T target) {
        mySetter.accept(target, null);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<T> createEditor(Project project, Disposable parent) {
        ValueComponent<String> field = myFieldFactory.apply(project);

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(myLabel, field);
            }

            @RequiredUIAccess
            @Override
            public void reset(T target) {
                field.setValue(StringUtil.notNullize(myGetter.apply(target)));
            }

            @RequiredUIAccess
            @Override
            public void apply(T target) {
                mySetter.accept(target, StringUtil.nullize(field.getValue(), true));
            }
        };
    }
}
