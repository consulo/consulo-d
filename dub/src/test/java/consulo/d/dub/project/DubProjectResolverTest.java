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
 * What the import makes of the {@code dub describe} output of a real vibe.d application, and of a package with a path
 * dependency described by DUB 1.42.
 */
public class DubProjectResolverTest {
    private static final Path PACKAGE_DIRECTORY = Path.of("/path/to/project/SuperAwesomeApp");

    @Test
    public void testSourceRoots() throws Throwable {
        DubProject project = unixProject();
        assertEquals(List.of(PACKAGE_DIRECTORY.resolve("source")), DubProjectResolver.sourceRoots(PACKAGE_DIRECTORY, project.rootPackage()));
    }

    @Test
    public void testModuleCompilesWithPredefinedAndTargetVersions() throws Throwable {
        assertEquals(List.of("linux", "Posix", "X86_64", "DigitalMars", "VibeDefaultMain", "Have_vibe_d_core", "VibeUseOpenSSL11",
            "PRODUCTION", "USE_MYSQL", "USE_SQLITE", "USE_PGSQL", "EventcoreEpollDriver", "Have_SuperAwesomeApp", "Have_colourfulmoon",
            "Have_consoled", "Have_hibernated", "Have_ddbc", "Have_derelict_pq", "Have_derelict_util", "Have_mysql_native",
            "Have_vibe_core", "Have_eventcore", "Have_taggedalgebraic", "Have_vibe_d_data", "Have_vibe_d_utils", "Have_poodinis",
            "Have_proper_d", "Have_vibe_d_web", "Have_vibe_d_http", "Have_diet_ng", "Have_vibe_d_crypto", "Have_vibe_d_inet",
            "Have_vibe_d_stream", "Have_vibe_d_textfilter", "Have_vibe_d_tls", "Have_openssl"), DubProjectResolver.versionIdentifiers(unixProject()));
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
        assertEquals(List.of("linux", "Posix", "X86_64", "DigitalMars", "VibeDefaultMain", "Have_vibe_d_core", "VibeUseOpenSSL11",
            "PRODUCTION", "USE_MYSQL", "USE_SQLITE", "USE_PGSQL", "EventcoreEpollDriver", "Have_consoled"), consoled.versionIdentifiers());
    }

    @Test
    public void testDependencyVersionsReachTheModule() throws Throwable {
        assertEquals(List.of("linux", "Posix", "X86_64", "DigitalMars", "MyFeature", "Have_hello", "Have_lib", "LibFeature"),
            DubProjectResolver.versionIdentifiers(pathDependencyProject()));
    }

    @Test
    public void testDependencyCompilesWithTheVersionsOfItsTarget() throws Throwable {
        Path hello = Path.of("/home/dev/projects/hello");
        List<DubProjectResolver.DubDependency> dependencies = DubProjectResolver.dependencies(hello, pathDependencyProject());
        assertEquals(1, dependencies.size());

        DubProjectResolver.DubDependency lib = dependencies.get(0);
        assertEquals("lib", lib.name());
        assertEquals(List.of(Path.of("/home/dev/projects/lib/source").toString()), lib.importPaths());
        assertEquals(List.of("linux", "Posix", "X86_64", "DigitalMars", "LibFeature", "MyFeature", "Have_lib"), lib.versionIdentifiers());
        assertEquals(Path.of("/home/dev/projects/hello/hello").toString(),
            DubProjectResolver.targetExecutable(hello, pathDependencyProject().rootPackage()));
    }

    private DubProject unixProject() throws Throwable {
        return load("dub/dubDescribeOutput-Unix.json");
    }

    private DubProject pathDependencyProject() throws Throwable {
        return load("dub/dubDescribeOutput-dub-1.42-path-dependency.json");
    }

    private DubProject load(String path) throws Throwable {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            return new DescribeParserImpl().parse(new String(Objects.requireNonNull(stream).readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
