package consulo.d.language.resolve;

import consulo.application.ReadAction;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.impl.internal.psi.PsiManagerEx;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.PsiReference;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Names resolved through imports are found in the stubs of the imported modules ({@link DStubDeclarationWalker}): the
 * AST of every file but {@code main.d} must stay unloaded while resolving.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DStubCrossModuleResolveTest {
    private static final String REF = "/*<ref>*/";

    @Test
    public void testFunctionOfImportedModule(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            void foo() {}
            void bar() {}
            """);
        assertResolved(fixture, """
            import lib;
            void main() { /*<ref>*/foo(); }
            """, "foo", "lib.d");
    }

    @Test
    public void testTypeThroughPublicImportChain(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/a.d", """
            module a;
            public import b;
            """);
        fixture.addFileToProject("src/b.d", """
            module b;
            public import c;
            """);
        fixture.addFileToProject("src/c.d", """
            module c;
            struct Point { int x; }
            """);
        assertResolved(fixture, """
            import a;
            void main() { /*<ref>*/Point p; }
            """, "Point", "c.d");
    }

    @Test
    public void testPrivateImportOfImportedModuleIsNotFollowed(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/a.d", """
            module a;
            import b;
            """);
        fixture.addFileToProject("src/b.d", """
            module b;
            void hidden() {}
            """);
        assertUnresolved(fixture, """
            import a;
            void main() { /*<ref>*/hidden(); }
            """);
    }

    @Test
    public void testStaticPublicImportOfImportedModuleNeedsQualifier(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/a.d", """
            module a;
            public static import b;
            """);
        fixture.addFileToProject("src/b.d", """
            module b;
            void qualifiedOnly() {}
            """);
        assertUnresolved(fixture, """
            import a;
            void main() { /*<ref>*/qualifiedOnly(); }
            """);
    }

    @Test
    public void testDeclarationsInVersionAndAttributeBlocks(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            version (linux) {
                void onLinux() {}
            }
            public:
            extern (C) {
                int cFunction(int);
            }
            """);
        assertResolved(fixture, """
            import lib;
            void main() { /*<ref>*/onLinux(); }
            """, "onLinux", "lib.d");
        assertResolved(fixture, """
            import lib;
            void main() { /*<ref>*/cFunction(1); }
            """, "cFunction", "lib.d");
    }

    @Test
    public void testMixinTemplateMembersAreNotModuleLevel(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            mixin template Fields() {
                int mixedIn;
            }
            """);
        assertUnresolved(fixture, """
            import lib;
            void main() { int i = /*<ref>*/mixedIn; }
            """);
    }

    @Test
    public void testClassMembersAreNotModuleLevel(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            class Shape {
                int area;
            }
            """);
        assertUnresolved(fixture, """
            import lib;
            void main() { int i = /*<ref>*/area; }
            """);
    }

    @Test
    public void testAnonymousEnumMember(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            enum {
                Red,
                Green
            }
            """);
        assertResolved(fixture, """
            import lib;
            void main() { int i = /*<ref>*/Green; }
            """, "Green", "lib.d");
    }

    @Test
    public void testModuleQualifiedSymbol(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            void foo() {}
            """);
        assertResolved(fixture, """
            import lib;
            void main() { lib./*<ref>*/foo(); }
            """, "foo", "lib.d");
    }

    @Test
    public void testUfcsFunctionOfImportedModule(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            struct Point { int x; }
            int twice(Point p) { return p.x * 2; }
            """);
        assertResolved(fixture, """
            import lib;
            void main() {
                Point p;
                p./*<ref>*/twice();
            }
            """, "twice", "lib.d");
    }

    @Test
    public void testMemberOfImportedStruct(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            struct Point {
                int x;
                version (linux) {
                    int y;
                }
            }
            """);
        assertResolved(fixture, """
            import lib;
            void main() {
                Point p;
                int i = p./*<ref>*/y;
            }
            """, "y", "lib.d");
    }

    @Test
    public void testMemberOfImportedEnum(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            enum Color { Red, Green }
            """);
        assertResolved(fixture, """
            import lib;
            void main() { auto c = Color./*<ref>*/Green; }
            """, "Green", "lib.d");
    }

    /**
     * The base class list is not stubbed: finding an inherited member still reads the AST of the class's module.
     */
    @Test
    public void testInheritedMemberOfImportedClass(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject("src/lib.d", """
            module lib;
            class Base { int baseField; }
            class Derived : Base { int ownField; }
            """);
        PsiElement element = resolve(fixture, """
            import lib;
            void main() {
                Derived d = new Derived();
                int i = d./*<ref>*/baseField;
            }
            """, false);
        assertNotNull(element, "not resolved");
        assertEquals("baseField", ReadAction.compute(() -> ((PsiNamedElement) element).getName()));
    }

    private static void assertResolved(CodeInsightTestFixture fixture, String mainText, String name, String fileName)
        throws Exception {
        PsiElement element = resolve(fixture, mainText);
        assertNotNull(element, "not resolved");
        assertTrue(element instanceof PsiNamedElement);
        assertEquals(name, ReadAction.compute(() -> ((PsiNamedElement) element).getName()));
        assertEquals(fileName, ReadAction.compute(() -> element.getContainingFile().getName()));
    }

    private static void assertUnresolved(CodeInsightTestFixture fixture, String mainText) throws Exception {
        PsiElement element = resolve(fixture, mainText);
        assertNull(element, () -> "resolved to " + ReadAction.compute(() -> element instanceof PsiNamedElement named ? named.getName() : "?"));
    }

    private static int ourMainCount;

    private static @Nullable PsiElement resolve(CodeInsightTestFixture fixture, String mainText) throws Exception {
        return resolve(fixture, mainText, true);
    }

    private static @Nullable PsiElement resolve(CodeInsightTestFixture fixture, String mainText, boolean stubsOnly) throws Exception {
        int offset = mainText.indexOf(REF);
        assertTrue(offset >= 0);
        PsiFile file = fixture.configureByText("check" + ourMainCount++ + "/main.d", mainText.replace(REF, ""));
        PsiReference reference = ReadAction.compute(() -> file.findReferenceAt(offset));
        assertNotNull(reference, "no reference at the marker");
        Disposable disposable = Disposable.newDisposable();
        try {
            if (stubsOnly) {
                PsiManagerEx.getInstanceEx(fixture.getProject())
                    .setAssertOnFileLoadingFilter(virtualFile -> !virtualFile.getPath().endsWith("main.d"), disposable);
            }
            return ReadAction.compute(reference::resolve);
        }
        finally {
            Disposer.dispose(disposable);
        }
    }
}
