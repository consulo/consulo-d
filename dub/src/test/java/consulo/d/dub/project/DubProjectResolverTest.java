package consulo.d.dub.project;

import io.github.intellij.dub.project.DubProject;
import io.github.intellij.dub.tools.DescribeParserImpl;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the import makes of the {@code dub describe} output of a real vibe.d application.
 */
public class DubProjectResolverTest {
    private static final Path PACKAGE_DIRECTORY = Path.of("/path/to/project/SuperAwesomeApp");

    @Test
    public void testSourceRoots() throws Throwable {
        DubProject project = unixProject();
        assertEquals(List.of(PACKAGE_DIRECTORY.resolve("source")), DubProjectResolver.sourceRoots(PACKAGE_DIRECTORY, project.rootPackage()));
    }

    @Test
    public void testModuleCompilesWithPredefinedAndPackageVersions() throws Throwable {
        assertEquals(List.of("linux", "Posix", "X86_64", "DigitalMars", "VibeDefaultMain", "Have_vibe_d_core", "VibeUseOpenSSL11",
            "PRODUCTION", "USE_MYSQL", "USE_SQLITE", "USE_PGSQL"), DubProjectResolver.versionIdentifiers(unixProject()));
    }

    @Test
    public void testTargetExecutable() throws Throwable {
        DubProject project = unixProject();
        assertEquals(Path.of("/path/to/project/SuperAwesomeApp/" + project.rootPackage().targetPath() + "/" + project.rootPackage().targetFileName()).normalize().toString(),
            DubProjectResolver.targetExecutable(PACKAGE_DIRECTORY, project.rootPackage()));
    }

    @Test
    public void testEveryOtherPackageIsADependency() throws Throwable {
        List<DubProjectResolver.DubDependency> dependencies = DubProjectResolver.dependencies(PACKAGE_DIRECTORY, unixProject());
        assertEquals(29, dependencies.size());

        DubProjectResolver.DubDependency consoled = dependencies.stream()
            .filter(dependency -> dependency.name().equals("consoled"))
            .findFirst()
            .orElseThrow();
        assertEquals("1.0.4", consoled.version());
        assertEquals(List.of(Path.of("/home/username/.dub/packages/consoled-1.0.4/consoled/source").toString()), consoled.importPaths());
        assertTrue(Path.of(consoled.directory()).endsWith(Path.of("consoled-1.0.4", "consoled")));
        assertEquals(List.of("linux", "Posix", "X86_64", "DigitalMars"), consoled.versionIdentifiers());
    }

    private DubProject unixProject() throws Throwable {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("dub/dubDescribeOutput-Unix.json")) {
            return new DescribeParserImpl().parse(new String(Objects.requireNonNull(stream).readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
