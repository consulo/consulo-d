package consulo.d.dub.newProject;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.newProject.DNewModuleBuilder;
import consulo.module.creation.scratch.NewModuleBuilder;
import consulo.module.creation.scratch.NewModuleContext;
import io.github.intellij.dub.module.DlangDubModuleBuilder;

/**
 * The DUB entry of the D group of the new project wizard (upstream's {@code DubDlangNewProjectWizard}).
 */
@ExtensionImpl(id = "dub")
public class DubNewModuleBuilder implements NewModuleBuilder {
    @Override
    public void setupContext(NewModuleContext context) {
        DlangDubModuleBuilder builder = new DlangDubModuleBuilder();
        DNewModuleBuilder.addGroup(context, builder)
            .add(builder.getPresentableName(), builder.getNodeIcon(), builder.getWeight(), new DubNewModuleBuilderProcessor());
    }
}
