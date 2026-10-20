package consulo.d.language.completion;

import consulo.application.ReadAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.completion.CompletionUtilCore;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The variants {@link DReferenceCompletionContributor} offers, computed on a file holding the dummy identifier at the
 * caret as the completion copy does.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DReferenceCompletionTest {
    private static final String CARET = "<caret>";

    @Test
    public void testMembersOfVariableType(CodeInsightTestFixture fixture) throws Exception {
        Set<String> variants = variants(fixture, """
            struct Point { int x; int y; void move() {} }
            void main() {
                Point p;
                p.<caret>
            }
            """);
        assertEquals(Set.of("x", "y", "move"), variants);
    }

    @Test
    public void testMembersOfVariableTypeBeforeStatement(CodeInsightTestFixture fixture) throws Exception {
        Set<String> variants = variants(fixture, """
            struct Point { int x; int y; }
            void main() {
                Point p;
                p.<caret>
                int z = 1;
            }
            """);
        assertEquals(Set.of("x", "y"), variants);
    }

    @Test
    public void testMembersOfAutoVariable(CodeInsightTestFixture fixture) throws Exception {
        Set<String> variants = variants(fixture, """
            struct Point { int x; int y; }
            void main() {
                auto p = Point(1, 2);
                p.<caret>
            }
            """);
        assertEquals(Set.of("x", "y"), variants);
    }

    @Test
    public void testMembersOfParameterType(CodeInsightTestFixture fixture) throws Exception {
        Set<String> variants = variants(fixture, """
            class Shape { int area; void draw() {} }
            void render(Shape s) {
                s.<caret>
            }
            """);
        assertEquals(Set.of("area", "draw"), variants);
    }

    @Test
    public void testUfcsCandidates(CodeInsightTestFixture fixture) throws Exception {
        Set<String> variants = variants(fixture, """
            struct Point { int x; }
            int twice(Point p) { return p.x * 2; }
            void main() {
                Point p;
                p.<caret>
            }
            """);
        assertTrue(variants.contains("x"), variants.toString());
        assertTrue(variants.contains("twice"), variants.toString());
    }

    @Test
    public void testUnqualified(CodeInsightTestFixture fixture) throws Exception {
        Set<String> variants = variants(fixture, """
            struct Point { int x; }
            void main() {
                Point p;
                <caret>
            }
            """);
        assertTrue(variants.containsAll(Set.of("p", "Point", "main")), variants.toString());
        assertTrue(!variants.contains("x"), variants.toString());
    }

    private static Set<String> variants(CodeInsightTestFixture fixture, String text) throws Exception {
        int offset = text.indexOf(CARET);
        String fileText = text.replace(CARET, CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED);
        PsiFile file = fixture.configureByText("main.d", fileText);
        return ReadAction.compute(() -> {
            PsiElement position = file.findElementAt(offset);
            assertNotNull(position);
            Collection<DNamedElement> elements = DReferenceCompletionContributor.collectVariants(position);
            Set<String> names = new TreeSet<>();
            for (DNamedElement element : elements) {
                names.add(element.getName());
            }
            return names;
        });
    }
}
