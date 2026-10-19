package io.github.intellij.dub.tools;

import io.github.intellij.dub.project.DubPackage;
import io.github.intellij.dub.project.DubProject;
import io.github.intellij.dub.project.DubTarget;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samael Bate (singingbush)
 * created on 06/02/18
 */
public class DescribeParserTest {

    private final DescribeParser parser = new DescribeParserImpl();

    @Test
    public void testParsingTheJsonOutputOfDubDescribeWithUnixPaths() throws Throwable {
        String json = load("dub/dubDescribeOutput-Unix.json");

        DubProject dubProject = parser.parse(json);

        assertEquals("SuperAwesomeApp", dubProject.rootPackageName());
        assertEquals("default", dubProject.configuration());
        assertEquals("debug", dubProject.buildType());
        assertEquals("dmd", dubProject.compiler());
        assertEquals(1, dubProject.architecture().size());
        assertEquals(2, dubProject.platform().size());
        assertEquals("linux", dubProject.platform().get(0));
        assertEquals("posix", dubProject.platform().get(1));

        assertDubPackageValid("root package", dubProject.rootPackage());

        assertFalse(
            dubProject.packages().contains(dubProject.rootPackage()),
            "The Root package should not be included in the dependencies"
        );

        assertEquals(29, dubProject.packages().size());

        for (DubPackage it : dubProject.packages()) {
            assertDubPackageValid(it.name(), it);
            assertFalse(it.sourcesDirs().isEmpty());
        }
    }

    @Test
    public void testParsingTheJsonOutputOfDubDescribeWithWindowsPaths() throws Throwable {
        String json = load("dub/dubDescribeOutput-Windows.json");

        DubProject dubProject = parser.parse(new BufferedReader(new java.io.StringReader(json)));

        assertEquals("testapp", dubProject.rootPackageName());
        assertEquals("default", dubProject.configuration());
        assertEquals("debug", dubProject.buildType());
        assertEquals("dmd", dubProject.compiler());
        assertEquals(1, dubProject.architecture().size());
        assertEquals(1, dubProject.platform().size());
        assertEquals("windows", dubProject.platform().get(0));

        assertDubPackageValid("root package", dubProject.rootPackage());

        assertFalse(
            dubProject.packages().contains(dubProject.rootPackage()),
            "The Root package should not be included in the dependencies"
        );

        assertEquals(38, dubProject.packages().size());

        for (DubPackage it : dubProject.packages()) {
            assertDubPackageValid(it.name(), it);

            // vibe-d:core doesn't specify importPaths
            if ("vibe-d:core".equals(it.name())) {
                // vibe-d:core is basically a package that pulls in "vibe-d:data", "vibe-d:utils", "vibe-core"
                //assertEquals("none", it.targetType)
                assertTrue(it.sourcesDirs().isEmpty());
                assertTrue(it.dependencies().containsAll(java.util.List.of("vibe-d:data", "vibe-d:utils", "vibe-core")));
                assertEquals("Compatibility forwarding package for vibe-core", it.description());
                assertTrue(it.resources().isEmpty());
                assertTrue(it.stringImportFiles().isEmpty());
                assertTrue(it.files().isEmpty());
            }
            else if ("vibe-d:data".equals(it.name())) {
                assertEquals("Data format and serialization support", it.description());
                assertFalse(it.sourcesDirs().isEmpty());
                assertTrue(it.dependencies().contains("vibe-d:utils"));
                assertTrue(it.sourcesDirs().contains("."));
                assertTrue(it.resources().isEmpty());
                assertTrue(it.stringImportFiles().isEmpty());
                assertEquals(3, it.files().size());
            }
            else {
                assertFalse(it.files().isEmpty());
            }
        }

        assertFalse(dubProject.targets().isEmpty());
        for (DubTarget it : dubProject.targets()) {
            assertNotBlank("rootPackage should not be blank", it.rootPackage());
            assertNotBlank("rootConfiguration should not be blank", it.rootConfiguration());
            assertFalse(it.packages().isEmpty());
        }
    }

    private void assertDubPackageValid(String name, DubPackage pkg) {
        assertNotBlank(name + " Should have name", pkg.name());
        assertNotBlank(name + " Should have path", pkg.path());
        assertNotBlank(name + " Should have version", pkg.version());

        assertNotNull(pkg.description(), name + " Should have description");
        assertNotNull(pkg.copyright(), name + " Should have copyright");
        assertNotNull(pkg.license(), name + " Should have license");

        assertNotNull(pkg.dependencies(), name + " Should have dependencies");
        assertNotNull(pkg.sourcesDirs(), name + " Should have sourcesDirs");
        assertNotNull(pkg.resources(), name + " Should have resources");
        assertNotNull(pkg.sourceFiles(), name + " Should have sourceFiles");
        assertNotNull(pkg.stringImportFiles(), name + " Should have stringImportFiles");

        //assertTrue(pkg.active)
        assertNotNull(pkg.workingDirectory());
        assertNotNull(pkg.mainSourceFile());

        assertNotNull(pkg.libs());
        assertNotNull(pkg.copyFiles());
        assertNotNull(pkg.extraDependencyFiles());
        assertNotNull(pkg.versions());
        assertNotNull(pkg.debugVersions());
        assertNotNull(pkg.preGenerateCommands());
        assertNotNull(pkg.postGenerateCommands());
        assertNotNull(pkg.preBuildCommands());
        assertNotNull(pkg.postBuildCommands());
        assertNotNull(pkg.preRunCommands());
        assertNotNull(pkg.postRunCommands());

        ////val dflags
        ////val lflags
        //assertTrue(pkg.files.isNotEmpty())
    }

    private void assertNotBlank(String message, String value) {
        assertNotNull(value, message);
        assertFalse(value.isEmpty(), message);
    }

    private String load(String resource) throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream, resource);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
