package consulo.d.language.moduleAware;

import consulo.language.ast.ASTNode;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersionUtil;
import consulo.test.junit.impl.language.SimpleParsingTest;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.DlangFileType;
import io.github.intellij.dlanguage.psi.DlangTypes;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Which declarations survive {@link DConditionalCompilation} under given options: each case lists the names declared in
 * the branches that are compiled (or may be).
 */
public class DConditionalCompilationTest extends SimpleParsingTest<Object> {
    private static final DCompileOptions LINUX_DMD = new DCompileOptions(List.of("linux", "Posix", "X86_64", "DigitalMars"), List.of());

    public DConditionalCompilationTest() {
        super("moduleAware", "d");
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return DlangFileType.INSTANCE;
    }

    @Test
    public void testNothingKnownKeepsEverything(Context context) {
        assertEquals("a b c d", compiled(context, null, """
            version(linux) { int a; } else { int b; }
            version(FeatureX) { int c; }
            debug(Trace) { int d; }
            """));
    }

    @Test
    public void testPlatformIdentifiers(Context context) {
        assertEquals("a d", compiled(context, LINUX_DMD, """
            version(linux) { int a; } else { int b; }
            version(Windows) { int c; } else { int d; }
            """));
    }

    @Test
    public void testOtherPredefinedIdentifiersAreUndecided(Context context) {
        assertEquals("a b c d", compiled(context, LINUX_DMD, """
            version(D_LP64) { int a; } else { int b; }
            version(unittest) { int c; }
            version(LittleEndian) { int d; }
            """));
    }

    @Test
    public void testOwnIdentifiers(Context context) {
        assertEquals("b c", compiled(context, new DCompileOptions(List.of("linux", "FeatureY"), List.of()), """
            version(FeatureX) { int a; } else { int b; }
            version(FeatureY) { int c; }
            """));
    }

    @Test
    public void testAllAndNone(Context context) {
        assertEquals("a d", compiled(context, LINUX_DMD, """
            version(all) { int a; } else { int b; }
            version(none) { int c; } else { int d; }
            """));
    }

    @Test
    public void testStaticIfIsNeverEvaluated(Context context) {
        assertEquals("a b", compiled(context, LINUX_DMD, """
            static if (false) { int a; } else { int b; }
            """));
    }

    @Test
    public void testVersionSpecificationOfTheFile(Context context) {
        assertEquals("b", compiled(context, LINUX_DMD, """
            version = FeatureX;
            version(FeatureX) { int b; }
            """));
    }

    @Test
    public void testVersionSpecificationInInactiveBranchDoesNotCount(Context context) {
        assertEquals("", compiled(context, LINUX_DMD, """
            version(Windows) { version = OnWindows; }
            version(OnWindows) { int a; }
            """));
    }

    @Test
    public void testDebugIdentifiers(Context context) {
        assertEquals("a c", compiled(context, new DCompileOptions(List.of("linux"), List.of("Trace")), """
            debug(Trace) { int a; }
            debug(Other) { int b; }
            debug { int c; }
            """));
    }

    @Test
    public void testColonForm(Context context) {
        assertEquals("a", compiled(context, LINUX_DMD, """
            int a;
            version(Windows):
            int b;
            int c;
            """));
    }

    @Test
    public void testBranchWithoutBraces(Context context) {
        assertEquals("b", compiled(context, LINUX_DMD, """
            version(Windows) int a; else int b;
            """));
    }

    @Test
    public void testNestedConditions(Context context) {
        assertEquals("b", compiled(context, LINUX_DMD, """
            version(linux) {
                version(X86) { int a; } else { int b; }
            }
            version(Windows) {
                version(X86_64) { int c; }
            }
            """));
    }

    private static String compiled(Context context, @Nullable DCompileOptions options, String text) {
        PsiFile file = context.psiFileFactory().createFileFromText("test.d", DLanguage.INSTANCE,
            LanguageVersionUtil.findDefaultVersion(DLanguage.INSTANCE), text);
        ASTNode fileNode = file.getNode();
        DConditionalCompilation compilation = DConditionalCompilation.of(fileNode, options);
        List<String> names = new ArrayList<>();
        collectNames(fileNode, compilation, names);
        return String.join(" ", names);
    }

    private static void collectNames(ASTNode node, DConditionalCompilation compilation, List<String> names) {
        if (node.getElementType() == DlangTypes.COMPILE_CONDITION || node.getElementType() == DlangTypes.VERSION_SPECIFICATION) {
            return;
        }
        if (node.getElementType() == DlangTypes.ID) {
            names.add(node.getText());
            return;
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (node.getElementType() == DlangTypes.CONDITIONAL_DECLARATION && compilation.isInactive(node, child)) {
                continue;
            }
            collectNames(child, compilation, names);
        }
    }
}
