package consulo.d.dub.newProject;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.WriteAction;
import consulo.application.progress.Task;
import consulo.d.dub.DubConstants;
import consulo.d.dub.DubUtil;
import consulo.d.dub.setting.DubProjectSettings;
import consulo.d.dub.setting.DubSettings;
import consulo.d.localize.DLocalize;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ExternalSystemDataKeys;
import consulo.externalSystem.model.task.ProgressExecutionMode;
import consulo.externalSystem.service.project.ExternalProjectRefreshCallback;
import consulo.externalSystem.service.project.ExternalSystemProjectRefresher;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.logging.Logger;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.ModuleWithNameAlreadyExistsException;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.creation.scratch.NewModuleBuilderProcessor;
import consulo.project.Project;
import consulo.project.startup.StartupManager;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.wizard.WizardStep;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dub.module.DlangDubModuleBuilder;
import io.github.intellij.dub.project.wizard.DubNewProjectWizardStep;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A DUB package: the module is set up as upstream's {@code DubNewProjectWizardStep.linkDubProject} did. Once the project
 * is open, {@code dub init} runs in the background, the module takes the name of the package it created (the name the
 * DUB import gives its module) and the package is linked, so the DUB import fills in sources and dependencies.
 */
public class DubNewModuleBuilderProcessor implements NewModuleBuilderProcessor<DubNewModuleContext> {
    private static final Logger LOG = Logger.getInstance(DubNewModuleBuilderProcessor.class);

    private static final Pattern SDL_NAME = Pattern.compile("^\\s*name\\s+\"([^\"]+)\"", Pattern.MULTILINE);

    private static final String APP_SOURCE = "app.d";

    private final DubPackageTemplate myTemplate;

    public DubNewModuleBuilderProcessor(DubPackageTemplate template) {
        myTemplate = template;
    }

    @Override
    public DubNewModuleContext createContext(boolean isNewProject) {
        DubNewModuleContext context = new DubNewModuleContext(isNewProject);
        context.setDubProjectType(myTemplate.getProjectType());
        return context;
    }

    @Override
    public void buildSteps(Consumer<WizardStep<DubNewModuleContext>> consumer, DubNewModuleContext context) {
        consumer.accept(new DubNewProjectWizardStep(context));
    }

    @RequiredReadAction
    @Override
    public void process(DubNewModuleContext context, ContentEntry contentEntry, ModifiableRootModel modifiableRootModel) {
        VirtualFile contentRoot = contentEntry.getFile();
        if (contentRoot == null) {
            return;
        }

        DlangDubModuleBuilder builder = new DlangDubModuleBuilder();
        builder.setModuleJdk(context.getSdk());
        builder.setName(context.getName());
        builder.setContentEntryPath(contentRoot.getPath());
        Map<String, String> options = new HashMap<>(3);
        options.put("dubFormat", switch (context.getDubDsl()) {
            case SDLANG -> "sdl";
            case JSON -> "json";
        });
        options.put("dubType", context.getDubProjectType().getText());
        builder.setDubOptions(options);
        builder.setupRootModel(modifiableRootModel);

        Path directory = Path.of(contentRoot.getPath());
        String moduleName = modifiableRootModel.getModule().getName();
        DubPackageTemplate template = myTemplate;
        StartupManager.getInstance(modifiableRootModel.getProject()).runAfterOpened((project, uiAccess) ->
            Task.Backgroundable.queue(project, DLocalize.newProjectDubProgress(), indicator -> {
                builder.runDubInit(project);

                String packageName = readPackageName(directory);
                if (template == DubPackageTemplate.LIBRARY && packageName != null) {
                    makeLibrary(directory, packageName);
                }

                VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
                if (root != null) {
                    root.refresh(false, true);
                }

                uiAccess.give(() -> {
                    if (project.isDisposed()) {
                        return;
                    }
                    Module module = renameModule(project, moduleName, packageName);
                    if (module != null && !template.isLibrary()) {
                        builder.setupRunConfiguration(project, module);
                    }
                    if (packageName != null) {
                        link(project, directory);
                    }
                });
            }));
    }

    /**
     * @return the {@code name} of the recipe {@code dub init} wrote
     */
    private static @Nullable String readPackageName(Path directory) {
        Path manifest = DubUtil.findManifest(directory);
        if (manifest == null) {
            return null;
        }
        try {
            String text = Files.readString(manifest);
            if (DubConstants.DUB_JSON.equals(String.valueOf(manifest.getFileName()))) {
                JsonElement recipe = JsonParser.parseString(text);
                JsonElement name = recipe.isJsonObject() ? recipe.getAsJsonObject().get("name") : null;
                return name != null && name.isJsonPrimitive() ? name.getAsString() : null;
            }
            Matcher matcher = SDL_NAME.matcher(text);
            return matcher.find() ? matcher.group(1) : null;
        }
        catch (IOException | JsonParseException e) {
            LOG.warn("Cannot read the name of the DUB package in " + directory, e);
            return null;
        }
    }

    /**
     * Turns the {@code minimal} package {@code dub init} wrote into a library: the recipe gets {@code targetType "library"}
     * and {@code source/app.d} is replaced by a module named after the package.
     */
    private static void makeLibrary(Path directory, String packageName) {
        Path manifest = DubUtil.findManifest(directory);
        if (manifest == null) {
            return;
        }
        try {
            String text = Files.readString(manifest).replace("A minimal D application.", "A minimal D library.");
            if (DubConstants.DUB_JSON.equals(String.valueOf(manifest.getFileName()))) {
                int end = text.lastIndexOf('}');
                if (end < 0) {
                    return;
                }
                text = text.substring(0, end).stripTrailing() + ",\n\t\"targetType\": \"library\"\n}\n";
            }
            else {
                text = text.stripTrailing() + "\ntargetType \"library\"\n";
            }
            Files.writeString(manifest, text);

            Path sourceDirectory = directory.resolve("source");
            Files.deleteIfExists(sourceDirectory.resolve(APP_SOURCE));
            String moduleName = packageName.replace('-', '_');
            Path librarySource = sourceDirectory.resolve(moduleName + ".d");
            if (!Files.exists(librarySource)) {
                Files.createDirectories(sourceDirectory);
                Files.writeString(librarySource, """
                    module %s;

                    int add(int a, int b)
                    {
                    	return a + b;
                    }

                    unittest
                    {
                    	assert(add(1, 2) == 3);
                    }
                    """.formatted(moduleName));
            }
        }
        catch (IOException e) {
            LOG.warn("Cannot turn the DUB package in " + directory + " into a library", e);
        }
    }

    /**
     * The DUB import finds its module by the package name: the new module takes that name, so it is not imported twice.
     */
    @RequiredUIAccess
    private static @Nullable Module renameModule(Project project, String moduleName, @Nullable String packageName) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module module = moduleManager.findModuleByName(moduleName);
        if (module == null || packageName == null || packageName.equals(moduleName) || moduleManager.findModuleByName(packageName) != null) {
            return module;
        }
        ModifiableModuleModel model = moduleManager.getModifiableModel();
        try {
            model.renameModule(module, packageName);
        }
        catch (ModuleWithNameAlreadyExistsException e) {
            model.dispose();
            return module;
        }
        WriteAction.run(model::commit);
        return module;
    }

    @RequiredUIAccess
    private static void link(Project project, Path directory) {
        String path = ExternalSystemApiUtil.toCanonicalPath(directory.toString());
        DubSettings settings = DubSettings.getInstance(project);
        if (settings.getLinkedProjectSettings(path) == null) {
            DubProjectSettings projectSettings = new DubProjectSettings();
            projectSettings.setExternalProjectPath(path);
            settings.linkProject(projectSettings);
        }
        project.putUserData(ExternalSystemDataKeys.NEWLY_IMPORTED_PROJECT, Boolean.TRUE);

        ExternalSystemProjectRefresher.getInstance().refreshProject(project,
            DubConstants.SYSTEM_ID,
            path,
            new ExternalProjectRefreshCallback() {
                @Override
                public void onSuccess(@Nullable DataNode<ProjectData> externalProject) {
                    if (externalProject != null) {
                        importData(project, externalProject);
                    }
                }

                @Override
                public void onFailure(String errorMessage, @Nullable String errorDetails) {
                }
            },
            false,
            ProgressExecutionMode.IN_BACKGROUND_ASYNC);
    }

    private static void importData(Project project, DataNode<ProjectData> externalProject) {
        // the import reads the local settings inside the write action, where a storable service cannot be created
        ExternalSystemApiUtil.getLocalSettings(project, DubConstants.SYSTEM_ID);
        ExternalSystemApiUtil.executeProjectChangeAction(true, new DisposeAwareProjectChange(project) {
            @RequiredUIAccess
            @Override
            public void execute() {
                ProjectDataManager projectDataManager = project.getApplication().getInstance(ProjectDataManager.class);
                ProjectRootManager.getInstance(project).mergeRootsChangesDuring(
                    () -> projectDataManager.importData(externalProject.getKey(), Collections.singleton(externalProject), project, true)
                );
            }
        });
    }
}
