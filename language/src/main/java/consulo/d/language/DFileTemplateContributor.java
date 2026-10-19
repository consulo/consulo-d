package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileTemplate.FileTemplateContributor;
import consulo.fileTemplate.FileTemplateRegistrator;

/**
 * The internal templates in {@code fileTemplates/internal}: the kinds of {@code CreateDlangClassAction} and the sample
 * code of a new D module.
 */
@ExtensionImpl
public class DFileTemplateContributor implements FileTemplateContributor {
    @Override
    public void register(FileTemplateRegistrator registrator) {
        registrator.registerInternalTemplate("d_language_module");
        registrator.registerInternalTemplate("d_language_class");
        registrator.registerInternalTemplate("d_language_interface");
        registrator.registerInternalTemplate("d_language_struct");
        registrator.registerInternalTemplate("d_language_union");
        registrator.registerInternalTemplate("d_language_enum");
        registrator.registerInternalTemplate("SampleCodeWithOnboardingTips");
    }
}
