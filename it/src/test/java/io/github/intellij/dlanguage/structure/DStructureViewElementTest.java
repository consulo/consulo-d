package io.github.intellij.dlanguage.structure;

import consulo.application.ReadAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.PsiFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Port of upstream {@code DStructureViewElementTest.kt}. The presentation carries icons, which the light test
 * application cannot load, so the test runs on the headless platform; each case is its own file.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DStructureViewElementTest {
    private int myFileCount;

    @Test
    public void testPresentationForConstructor(CodeInsightTestFixture fixture) throws Exception {
        assertStructureViewText(
            fixture,
            """
                this() {
                    writeln("constructor");
                }""",
            "this()"
        );

        assertStructureViewText(
            fixture,
            """
                this(int arg1, int arg2) {
                    writeln("constructor");
                }""",
            "this(int, int)"
        );
    }

    /*
     * https://github.com/intellij-dlanguage/intellij-dlanguage/issues/1024
     */
    @Test
    public void testPresentationForDelegate(CodeInsightTestFixture fixture) throws Exception {
        assertStructureViewText(
            fixture,
            "void delegate() @safe @nogc nothrow refAdd_;",
            "refAdd_ : void delegate() @safe @nogc nothrow"
        );

        assertStructureViewText(
            fixture,
            "void delegate() @safe @nogc nothrow refSub_;",
            "refSub_ : void delegate() @safe @nogc nothrow"
        );

        assertStructureViewText(
            fixture,
            "void[] delegate(size_t, TypeInfo ti = null) @safe @nogc nothrow allocate_;",
            "allocate_ : void [] delegate(size_t, TypeInfo ti = null) @safe @nogc nothrow"
        );

        assertStructureViewText(
            fixture,
            "bool delegate(scope void[]) @safe @nogc nothrow deallocate_;",
            "deallocate_ : bool delegate(scope void[]) @safe @nogc nothrow"
        );
    }

    @Test
    public void testPresentationForFunctionDeclaration(CodeInsightTestFixture fixture) throws Exception {
        assertStructureViewText(
            fixture,
            """
                void hello() {
                    writeln("Hello World");
                }""",
            "hello() : void"
        );
    }

    @Test
    public void testPresentationForLambdaDeclaration(CodeInsightTestFixture fixture) throws Exception {
        assertStructureViewText(
            fixture,
            "auto hello = () => writeln(\"Hello World\");",
            "hello = () => writeln(\"Hello World\")"
        );
    }

    @Test
    public void testPresentationForEnumDeclaration(CodeInsightTestFixture fixture) throws Exception {
        assertStructureViewText(
            fixture,
            """
                enum IntOps {
                    add = 0,
                    sub = 1,
                    mul = 2,
                    div = 3
                }""",
            "IntOps"
        );
    }

    private void assertStructureViewText(CodeInsightTestFixture fixture, String sourceCode, String expectedPresentableText) throws Exception {
        PsiFile psiFile = fixture.configureByText("test" + (myFileCount++) + ".d", sourceCode);
        String presentableText = ReadAction.compute(() -> {
            DStructureViewElement structureView = new DStructureViewElement(psiFile.getFirstChild());
            return structureView.getPresentation().getPresentableText().get();
        });
        assertEquals(expectedPresentableText, presentableText);
    }
}
