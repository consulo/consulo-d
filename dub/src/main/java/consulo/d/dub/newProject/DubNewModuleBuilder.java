package consulo.d.dub.newProject;

import consulo.annotation.component.ExtensionImpl;
import consulo.module.creation.scratch.NewModuleBuilder;
import consulo.module.creation.scratch.NewModuleContext;
import consulo.module.creation.scratch.NewModuleContextGroup;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dub.module.DlangDubModuleBuilder;

/**
 * The D group of the new project wizard (upstream's {@code DlangNewProjectWizard}) with an entry per DUB package
 * template (upstream's {@code DubDlangNewProjectWizard}). Upstream's plain D module ({@code IntellijDlangNewProjectWizard})
 * is not offered: D projects are built and run through DUB.
 */
@ExtensionImpl(id = "dub")
public class DubNewModuleBuilder implements NewModuleBuilder {
    public static final String GROUP_ID = "d";

    @Override
    public void setupContext(NewModuleContext context) {
        DlangDubModuleBuilder builder = new DlangDubModuleBuilder();
        NewModuleContextGroup group = context.addGroup(GROUP_ID, builder.getGroupName(), DLanguage.Icons.LANGUAGE);
        for (DubPackageTemplate template : DubPackageTemplate.values()) {
            group.add(template.getTitle(), template.getIcon(), builder.getWeight() + template.ordinal(),
                new DubNewModuleBuilderProcessor(template));
        }
    }
}
