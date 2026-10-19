package io.github.intellij.dlanguage.actions;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.application.dumb.DumbAware;
import consulo.d.localize.DLocalize;
import consulo.dataContext.DataContext;
import consulo.fileEditor.FileEditorManager;
import consulo.fileTemplate.FileTemplate;
import consulo.fileTemplate.FileTemplateManager;
import consulo.fileTemplate.FileTemplateUtil;
import consulo.ide.action.CreateFileAction;
import consulo.ide.action.CreateFileFromTemplateAction;
import consulo.ide.action.CreateFileFromTemplateDialog;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.project.ProjectPropertiesComponent;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DLanguage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by pirocks on 9/21/16.
 * mostly copy-pasted from "CreateDlangFileAction.java"
 */
@ActionImpl(
    id = "DLanguage.NewDLanguageClass",
    parents = @ActionParentRef(value = @ActionRef(id = "NewGroup"), anchor = ActionRefAnchor.BEFORE, relatedToAction = @ActionRef(id = "NewFile"))
)
public class CreateDlangClassAction extends CreateFileFromTemplateAction implements DumbAware {

    public CreateDlangClassAction() {
        super(DLocalize.actionNewDClassText(), DLocalize.actionNewDClassDescription(), DLanguage.Icons.CLASS);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(DLocalize.actionNewDFileTitle());

        for (Template template : Template.values()) {
            builder.addKind(template.getReadableName(), template.getIcon(), template.getId());
        }

        builder.setValidator(ClassNameValidator.INSTANCE);
    }

    @Override
    protected PsiFile createFileFromTemplate(String name, FileTemplate template, PsiDirectory inRequestedDirectory) {
        VirtualFile sourceRoot = findSourceRootOrDefault(inRequestedDirectory, inRequestedDirectory.getVirtualFile());

        String inputName;
        if (name.endsWith(".d")) {
            inputName = name.substring(0, name.length() - 2);
        }
        else if (name.endsWith(".di")) {
            inputName = name.substring(0, name.length() - 3);
        }
        else {
            inputName = name;
        }

        String directoryPath = inRequestedDirectory.getVirtualFile().getPath();
        if (directoryPath.startsWith(sourceRoot.getPath())) {
            directoryPath = directoryPath.substring(sourceRoot.getPath().length());
        }
        List<String> segments = new ArrayList<>();
        for (String segment : directoryPath.split("/", -1)) {
            if (!segment.isBlank()) {
                segments.add(segment);
            }
        }
        List<String> nameSegments = List.of(inputName.split("\\.", -1));
        segments.addAll(nameSegments);

        assert !segments.isEmpty();

        String module = segments.get(segments.size() - 1).toLowerCase(Locale.getDefault());
        List<String> pack = segments.subList(0, segments.size() - 1);
        String aggregate = segments.get(segments.size() - 1);

        PsiDirectory storeDirectory = new CreateFileAction.MkDirs(String.join(File.separator, nameSegments), inRequestedDirectory)
            .directory;
        String dlangModuleName = String.join(".", pack) + (!pack.isEmpty() ? ("package".equals(module) ? "" : "." + module) : module);

        Map<String, Object> properties = new HashMap<>();
        for (Map.Entry<Object, Object> entry : FileTemplateManager.getInstance(inRequestedDirectory.getProject())
            .getDefaultProperties().entrySet()) {
            properties.put(entry.getKey().toString(), entry.getValue());
        }
        properties.put("DLANGUAGE_MODULE_NAME", dlangModuleName);
        properties.put("DLANGUAGE_CLASS_NAME", aggregate);

        PsiElement element;
        try {
            element = FileTemplateUtil.createFromTemplate(template, module, properties, storeDirectory);
        }
        catch (IncorrectOperationException e) {
            throw e;
        }
        catch (Exception e) {
            // Kotlin let the exception of the template engine through
            throw new IncorrectOperationException(String.valueOf(e.getMessage()), e);
        }

        PsiFile containingFile = Objects.requireNonNull(element.getContainingFile());
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (virtualFile != null) {
            FileEditorManager.getInstance(inRequestedDirectory.getProject())
                .openFile(virtualFile, true);
            String defaultTemplateProperty = getDefaultTemplateProperty();
            if (defaultTemplateProperty != null) {
                ProjectPropertiesComponent.getInstance(inRequestedDirectory.getProject())
                    .setValue(defaultTemplateProperty, template.getName());
            }
        }

        return containingFile;
    }

    @Override
    protected LocalizeValue getActionName(PsiDirectory directory, String newName, String templateName) {
        return DLocalize.actionNewDFileTitle();
    }

    @RequiredReadAction
    @Override
    protected boolean isAvailable(DataContext dataContext) {
        if (super.isAvailable(dataContext)) {
            Project project = dataContext.getData(Project.KEY);
            VirtualFile dirContext = dataContext.getData(VirtualFile.KEY);

            if (project == null) {
                return false;
            }
            if (dirContext == null) {
                return false;
            }

            for (VirtualFile sourceRoot : ProjectRootManager.getInstance(project).getContentSourceRoots()) {
                if (dirContext.getPath().startsWith(sourceRoot.getPath())) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private VirtualFile findSourceRootOrDefault(PsiDirectory inRequestedDirectory, VirtualFile defaultSourceRoot) {
        for (VirtualFile sourceRoot : ProjectRootManager.getInstance(inRequestedDirectory.getProject()).getContentSourceRoots()) {
            if (inRequestedDirectory.getVirtualFile().getPath().startsWith(sourceRoot.getPath())) {
                return sourceRoot;
            }
        }
        return defaultSourceRoot;
    }

    private enum Template {
        MODULE("d_language_module", DLocalize::actionNewDClassKindModule, DLanguage.Icons.MODULE),
        CLASS("d_language_class", DLocalize::actionNewDClassKindClass, DLanguage.Icons.NODE_CLASS),
        INTERFACE("d_language_interface", DLocalize::actionNewDClassKindInterface, DLanguage.Icons.NODE_INTERFACE),
        STRUCT("d_language_struct", DLocalize::actionNewDClassKindStruct, DLanguage.Icons.NODE_STRUCT),
        UNION("d_language_union", DLocalize::actionNewDClassKindUnion, DLanguage.Icons.NODE_UNION),
        ENUM("d_language_enum", DLocalize::actionNewDClassKindEnum, DLanguage.Icons.NODE_ENUM);

        private final String myId;
        private final Supplier<LocalizeValue> myReadableName;
        private final Image myIcon;

        Template(String id, Supplier<LocalizeValue> readableName, Image icon) {
            myId = id;
            myReadableName = readableName;
            myIcon = icon;
        }

        String getId() {
            return myId;
        }

        LocalizeValue getReadableName() {
            return myReadableName.get();
        }

        Image getIcon() {
            return myIcon;
        }
    }
}
