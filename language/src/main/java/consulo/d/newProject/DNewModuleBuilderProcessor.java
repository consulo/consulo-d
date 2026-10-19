package consulo.d.newProject;

import consulo.annotation.access.RequiredReadAction;
import consulo.fileEditor.FileEditorManager;
import consulo.fileTemplate.FileTemplate;
import consulo.fileTemplate.FileTemplateManager;
import consulo.logging.Logger;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.creation.scratch.NewModuleBuilderProcessor;
import consulo.project.Project;
import consulo.project.startup.StartupManager;
import consulo.ui.ex.wizard.WizardStep;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.module.DlangModuleBuilder;
import io.github.intellij.dlanguage.project.wizard.DlangAssets;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * A plain D module: what upstream's {@code IntellijDlangNewProjectWizard} did, the module set up by
 * {@link DlangModuleBuilder} and the sample code of its {@code AssetStep}.
 */
public class DNewModuleBuilderProcessor implements NewModuleBuilderProcessor<DNewModuleContext> {
    private static final Logger LOG = Logger.getInstance(DNewModuleBuilderProcessor.class);

    private static final String SOURCE_ROOT = "source";
    private static final String SAMPLE_FILE_NAME = "app.d";

    @Override
    public DNewModuleContext createContext(boolean isNewProject) {
        return new DNewModuleContext(isNewProject);
    }

    @Override
    public void buildSteps(Consumer<WizardStep<DNewModuleContext>> consumer, DNewModuleContext context) {
        consumer.accept(new DNewModuleSetupStep<>(context, new DlangModuleBuilder()));
    }

    @RequiredReadAction
    @Override
    public void process(DNewModuleContext context, ContentEntry contentEntry, ModifiableRootModel modifiableRootModel) {
        VirtualFile contentRoot = contentEntry.getFile();
        if (contentRoot == null) {
            return;
        }

        DlangModuleBuilder builder = new DlangModuleBuilder();
        builder.setName(context.getName());
        builder.setContentEntryPath(contentRoot.getPath());
        builder.setModuleJdk(context.getSdk());
        builder.setupRootModel(modifiableRootModel);

        if (context.isAddSampleCode()) {
            Path sampleFile = writeSampleCode(Path.of(contentRoot.getPath()));
            if (sampleFile != null) {
                openWhenProjectIsOpened(modifiableRootModel.getProject(), sampleFile);
            }
        }
    }

    private static @Nullable Path writeSampleCode(Path contentRoot) {
        Path sampleFile = contentRoot.resolve(DlangAssets.INSTANCE.getDSampleSourcePath(SOURCE_ROOT, SAMPLE_FILE_NAME));
        if (Files.exists(sampleFile)) {
            return sampleFile;
        }
        try {
            FileTemplateManager templateManager = FileTemplateManager.getDefaultInstance();
            FileTemplate template = templateManager.getInternalTemplate(DlangAssets.INSTANCE.getDlangSampleTemplateName());
            String text = template.getText(templateManager.getDefaultProperties());
            Path directory = sampleFile.getParent();
            if (directory != null) {
                Files.createDirectories(directory);
            }
            Files.writeString(sampleFile, text, StandardCharsets.UTF_8);
            return sampleFile;
        }
        catch (IOException e) {
            LOG.warn("Cannot write the D sample code to " + sampleFile, e);
            return null;
        }
    }

    private static void openWhenProjectIsOpened(Project project, Path file) {
        StartupManager.getInstance(project).runAfterOpened((openedProject, uiAccess) -> uiAccess.give(() -> {
            if (openedProject.isDisposed()) {
                return;
            }
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file);
            if (virtualFile != null && virtualFile.isValid()) {
                FileEditorManager.getInstance(openedProject).openFile(virtualFile, true);
            }
        }));
    }
}
