package io.github.intellij.dlanguage.run;

import consulo.application.ReadAction;
import consulo.compiler.ModuleCompilerPathsManager;
import consulo.it.HeadlessModules;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.module.Module;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.run.exception.NoSourcesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Port of upstream {@code DlangDmdConfigToArgsConverterTest.kt}. Upstream ran on a light project with a mocked
 * configuration; here a module with a {@code src} source root is laid out on disk, and a new configuration (whose
 * options are all off, like the mock's) stands in for the mock. A Consulo module always has a compiler output (the
 * project's by default), so the output paths are in it, where upstream's light module had none and used the project
 * directory.
 *
 * @author Samael Bate (singingbush)
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DlangDmdConfigToArgsConverterTest {
    private static final String MODULE_NAME = "app";

    private Project project;
    private Module module;
    private Path src;

    private String outputDirArg;
    private String outputFile;
    private String outputFileArg;

    private void setUp(HeadlessProjects projects, String... sources) throws Exception {
        Path directory = projects.newDirectory();
        src = Files.createDirectories(directory.resolve("src"));
        for (String source : sources) {
            Files.writeString(src.resolve(source), "");
        }

        project = projects.open(directory);
        module = HeadlessModules.createModule(project, MODULE_NAME, findFile(directory));
        HeadlessModules.addSourceRoot(module, findFile(src));

        // these need to be set within setUp so that the module output path is known
        final Path outputDir = ModuleCompilerPathsManager.getInstance(module)
            .getCompilerOutputPath(ProductionContentFolderTypeProvider.getInstance());
        assertNotNull(outputDir);
        this.outputDirArg = "-od" + new File(outputDir.toFile(), "obj").getAbsolutePath();
        this.outputFile = MODULE_NAME + (Platform.current().os().isWindows() ? ".exe" : "");
        this.outputFileArg = "-of" + new File(outputDir.toFile(), outputFile).getAbsolutePath();
    }

    @Test
    public void testGetDMDParametersShouldOutputValidArgsByDefault(HeadlessProjects projects) throws Exception {
        setUp(projects, "myapp.d", "othersource.di");

        DlangRunDmdConfiguration dmdConfig = newConfiguration();

        List<String> dmdParameters = getDmdParameters(dmdConfig);

        assertEquals(List.of(outputDirArg, outputFileArg, sourcePath("myapp.d"), sourcePath("othersource.di")), dmdParameters);
    }

    @Test
    public void testGetDMDParametersBuildingALibrary(HeadlessProjects projects) throws Exception {
        setUp(projects, "myapp.d");

        DlangRunDmdConfiguration dmdConfig = newConfiguration();
        dmdConfig.setLibrary(true);

        List<String> dmdParameters = getDmdParameters(dmdConfig);

        String libraryArg = (outputFileArg.endsWith(".exe") ? outputFileArg.substring(0, outputFileArg.length() - 4) : outputFileArg) + ".lib";
        assertEquals(List.of("-lib", outputDirArg, libraryArg, sourcePath("myapp.d")), dmdParameters);
    }

    @Test
    public void testGetDMDParametersShouldSetAdditionalArgsCorrectly(HeadlessProjects projects) throws Exception {
        setUp(projects, "myapp.d");

        DlangRunDmdConfiguration dmdConfig = newConfiguration();
        dmdConfig.setDebug(true);
        dmdConfig.setUnitTest(true);
        dmdConfig.setCoverageAnalysis(true);
        dmdConfig.setVerbose(true);

        List<String> dmdParameters = getDmdParameters(dmdConfig);

        assertEquals(List.of("-debug", "-unittest", "-cov", "-v", outputDirArg, outputFileArg, sourcePath("myapp.d")), dmdParameters);
    }

    @Test
    public void testGetDMDParametersShouldThrowNoSourcesExceptionWhenNoDSrcFilesFound(HeadlessProjects projects) throws Exception {
        setUp(projects);

        DlangRunDmdConfiguration config = newConfiguration();

        assertThrows(NoSourcesException.class, () -> getDmdParameters(config));
    }

    private DlangRunDmdConfiguration newConfiguration() {
        return new DlangRunDmdConfiguration("test", project, new DlangRunDmdConfigurationType().getConfigurationFactories()[0]);
    }

    private List<String> getDmdParameters(DlangRunDmdConfiguration config) throws Exception {
        return ReadAction.compute(() -> DlangDmdConfigToArgsConverter.getDmdParameters(config, module));
    }

    private String sourcePath(String name) {
        return findFile(src.resolve(name)).getPath();
    }

    private static VirtualFile findFile(Path path) {
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path);
        assertNotNull(file, path.toString());
        return file;
    }
}
