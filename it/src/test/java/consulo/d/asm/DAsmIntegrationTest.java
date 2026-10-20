package consulo.d.asm;

import consulo.application.ReadAction;
import consulo.asm.gas.GasAarch64LanguageVersion;
import consulo.asm.gas.GasAttLanguageVersion;
import consulo.asm.language.AsmLanguage;
import consulo.document.util.TextRange;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.version.LanguageVersion;
import io.github.intellij.dlanguage.psi.DLanguageGccAsmInstruction;
import io.github.intellij.dlanguage.psi.DLanguageGccAsmOperand;
import io.github.intellij.dlanguage.psi.DLanguageGccAsmTemplate;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GCC style asm statements, assembly injected into their templates, and navigation between {@code extern} D functions
 * and assembly labels.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DAsmIntegrationTest {

    /**
     * consulo-it's document manager cannot freeze an injected document window, so the platform's injection registrar
     * fails there; what the injector registers is recorded instead.
     */
    @Test
    public void testTemplateIsGasAssembly(CodeInsightTestFixture fixture) throws Exception {
        Injection injection = inject(fixture, """
            void f(int x) {
                int r;
                asm { "movl %1, %0\\n\\taddl $1, %0" : "=r" (r) : "r" (x); }
            }
            """);
        assertInstanceOf(GasAttLanguageVersion.class, injection.version());
        // the escapes of the D string are decoded
        assertEquals("movl %1, %0\n\taddl $1, %0", injection.decoded());
    }

    @Test
    public void testWysiwygTemplateIsInjectedVerbatim(CodeInsightTestFixture fixture) throws Exception {
        Injection injection = inject(fixture, """
            void f() {
                asm { `pause\\n`; }
            }
            """);
        assertEquals("pause\\n", injection.decoded());
    }

    private record Injection(LanguageVersion version, String decoded) {
    }

    private static Injection inject(CodeInsightTestFixture fixture, String text) throws Exception {
        PsiFile file = fixture.configureByText("main.d", text);
        return ReadAction.compute(() -> {
            DLanguageGccAsmTemplate template = PsiTreeUtil.findChildOfType(file, DLanguageGccAsmTemplate.class);
            assertNotNull(template);
            List<LanguageVersion> versions = new ArrayList<>();
            List<String> decoded = new ArrayList<>();
            new DGccAsmInjector().injectLanguages(new MultiHostRegistrar() {
                @Override
                public MultiHostRegistrar startInjecting(LanguageVersion languageVersion) {
                    versions.add(languageVersion);
                    return this;
                }

                @Override
                public MultiHostRegistrar addPlace(@Nullable String prefix,
                                                   @Nullable String suffix,
                                                   PsiLanguageInjectionHost host,
                                                   TextRange rangeInsideHost) {
                    StringBuilder builder = new StringBuilder();
                    assertTrue(host.createLiteralTextEscaper().decode(rangeInsideHost, builder));
                    decoded.add(builder.toString());
                    return this;
                }

                @Override
                public void doneInjecting() {
                }
            }, template);
            assertEquals(1, versions.size());
            assertEquals(1, decoded.size());
            return new Injection(versions.getFirst(), decoded.getFirst());
        });
    }

    @Test
    public void testOperandExpressionsResolve(CodeInsightTestFixture fixture) throws Exception {
        String text = """
            int f(int x) {
                int r;
                asm { "movl %1, %0" : "=r" (r) : "r" (x) : "memory"; }
                return r;
            }
            """;
        PsiFile file = fixture.configureByText("main.d", text);
        int offset = text.indexOf("(r)") + 1;
        ReadAction.run(() -> {
            DLanguageGccAsmInstruction instruction = PsiTreeUtil.findChildOfType(file, DLanguageGccAsmInstruction.class);
            assertNotNull(instruction);
            assertNotNull(PsiTreeUtil.findChildOfType(instruction, DLanguageGccAsmTemplate.class));
            assertEquals(2, PsiTreeUtil.findChildrenOfType(instruction, DLanguageGccAsmOperand.class).size());
            PsiReference reference = file.findReferenceAt(offset);
            assertNotNull(reference, "the operand expression has no reference");
            assertInstanceOf(DLanguageIdentifierInitializer.class, reference.resolve());
        });
    }

    @Test
    public void testExternFunctionToAssemblyLabel(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/add.s", """
                .globl add
            add:
                leal (%rdi,%rsi), %eax
                ret
            """);
        fixture.configureByText("main.d", """
            extern (C) int add(int a, int b);
            int notExtern(int a);
            extern (C) int withBody(int a) { return a; }
            """);
        PsiFile asm = ReadAction.compute(() -> PsiManager.getInstance(fixture.getProject())
            .findFile(fixture.getProjectRoot().findFileByRelativePath("src/add.s")));
        assertNotNull(asm);
        assertEquals(AsmLanguage.INSTANCE, asm.getLanguage(), "add.s is " + asm.getFileType());
        List<LineMarkerInfo<?>> markers = fixture.getLineMarkers();
        assertEquals(1, markers.size(), markers.toString());
        assertEquals("add", ReadAction.compute(() -> markers.getFirst().getElement().getText()));
    }

    @Test
    public void testUnderscorePrefixedLabel(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/add.s", """
                .globl _add
            _add:
                ret
            """);
        fixture.configureByText("main.d", "extern (C) int add(int a, int b);\n");
        assertEquals(1, fixture.getLineMarkers().size());
    }

    @Test
    public void testAssemblyLabelToExternFunction(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/decl.d", "extern (C) int add(int a, int b);\n");
        fixture.configureByText("add.s", """
                .globl add
            add:
                ret
            .Llocal:
                ret
            other:
                ret
            """);
        List<LineMarkerInfo<?>> markers = fixture.getLineMarkers();
        assertEquals(1, markers.size(), markers.toString());
        assertEquals("add", ReadAction.compute(() -> markers.getFirst().getElement().getText()));
    }

    @Test
    public void testDialectOfTarget() {
        LanguageVersion x86 = DAsmDialects.forVersionIdentifiers(Set.of("linux", "Posix", "X86_64"));
        assertInstanceOf(GasAttLanguageVersion.class, x86);
        LanguageVersion arm = DAsmDialects.forVersionIdentifiers(Set.of("linux", "AArch64"));
        assertInstanceOf(GasAarch64LanguageVersion.class, arm);
        assertTrue(DAsmDialects.forVersionIdentifiers(Set.of()) instanceof GasAttLanguageVersion);
    }
}
