package io.github.intellij.dlanguage.run;

import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code DlangVirtualFileVisitorTest.java}. Consulo has no {@code MockVirtualFile}, so the files are
 * real ones in a temporary directory.
 *
 * @author Samael Bate (singingbush)
 * created on 19/03/2021
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DlangVirtualFileVisitorTest {

    @Test
    public void testVisitFile_Individual_Files(HeadlessProjects projects) throws Exception {
        final Path directory = projects.newDirectory();
        final DlangVirtualFileVisitor visitor = new DlangVirtualFileVisitor(null);

        assertEquals(VirtualFileVisitor.SKIP_CHILDREN, visitor.visitFileEx(createFile(directory, "source.d")));
        assertEquals(VirtualFileVisitor.SKIP_CHILDREN, visitor.visitFileEx(createFile(directory, "source.di")));
        assertEquals(VirtualFileVisitor.SKIP_CHILDREN, visitor.visitFileEx(createFile(directory, "source.java")));

        assertEquals(2, visitor.getDlangSources().size());
        assertFalse(visitor.getDlangSources().stream().anyMatch(f -> f.contains("source.java")));
    }

    @Test
    public void testVisitFile_Recursively(HeadlessProjects projects) throws Exception {
        final DlangVirtualFileVisitor visitor = new DlangVirtualFileVisitor(null);

        final Path sourceDir = Files.createDirectories(projects.newDirectory().resolve("myfolder"));
        createFile(sourceDir, "app.d");
        createFile(sourceDir, "my-impl.d");
        createFile(sourceDir, "my-interface.di");
        createFile(sourceDir, "README.md");

        // check mock directory with sources
        assertEquals(VirtualFileVisitor.CONTINUE, VirtualFileUtil.visitChildrenRecursively(
            findFile(sourceDir),
            visitor
        ));
        assertEquals(3, visitor.getDlangSources().size());
    }

    @Test
    public void testVisitFile_Recursively_With_exclusions(HeadlessProjects projects) throws Exception {
        final Path projectRoot = Files.createDirectories(projects.newDirectory().resolve("projectRoot"));

        // setup sources that we want to scan
        final Path sourceDir = Files.createDirectories(projectRoot.resolve("source"));
        createFile(sourceDir, "app.d");
        createFile(sourceDir, "my-impl.d");
        createFile(sourceDir, "my-interface.di");
        createFile(sourceDir, "README.md");

        // setup sources that we want to exclude
        final Path examplesDir = Files.createDirectories(projectRoot.resolve("examples"));
        createFile(examplesDir, "example.d");

        final DlangVirtualFileVisitor visitor = new DlangVirtualFileVisitor(new VirtualFile[] { findFile(examplesDir) });

        assertEquals(VirtualFileVisitor.CONTINUE, VirtualFileUtil.visitChildrenRecursively(
            findFile(projectRoot),
            visitor
        ));
        assertEquals(3, visitor.getDlangSources().size());
    }

    private static VirtualFile createFile(Path directory, String name) throws Exception {
        return findFile(Files.writeString(directory.resolve(name), ""));
    }

    private static VirtualFile findFile(Path path) {
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path);
        assertNotNull(file, path.toString());
        return file;
    }
}
