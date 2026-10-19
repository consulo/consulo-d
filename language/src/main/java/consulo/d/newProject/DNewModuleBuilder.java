package consulo.d.newProject;

import consulo.annotation.component.ExtensionImpl;
import consulo.module.creation.scratch.NewModuleBuilder;
import consulo.module.creation.scratch.NewModuleContext;
import consulo.module.creation.scratch.NewModuleContextGroup;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.module.DlangModuleBuilder;

/**
 * The D group of the new project wizard (upstream's {@code DlangNewProjectWizard}); its entries are the build systems
 * (upstream's {@code BuildSystemDlangNewProjectWizard}): a plain D module here, DUB in the DUB module.
 */
@ExtensionImpl(id = "d")
public class DNewModuleBuilder implements NewModuleBuilder {
    public static final String GROUP_ID = "d";

    @Override
    public void setupContext(NewModuleContext context) {
        DlangModuleBuilder builder = new DlangModuleBuilder();
        NewModuleContextGroup group = addGroup(context, builder);
        group.add(builder.getPresentableName(), builder.getNodeIcon(), builder.getWeight(), new DNewModuleBuilderProcessor());
    }

    /**
     * The D group, created by whichever of the D and DUB builders comes first.
     */
    public static NewModuleContextGroup addGroup(NewModuleContext context, DlangModuleBuilder builder) {
        return context.addGroup(GROUP_ID, builder.getGroupName(), DLanguage.Icons.LANGUAGE);
    }
}
