package io.github.intellij.dlanguage.resolve;

import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HeadlessProjectExtension.class)
public class DImportsUsageResolveTest extends DResolveTestCase {
    private static final String content = """
            module resolve.to.include;

            void write(string a){}
            void /*<resolved>*/writeln(string a){}
            """;

    private void doCheckByText(CodeInsightTestFixture fixture, String main) throws Exception {
        doCheckByText2(fixture, main, content);
    }

    @Test
    public void testLocalImport(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            void main() {
                import resolve.to.include;
                /*<ref>*/writeln("local");
            }
            """);
    }

    @Test
    public void testSelectiveImportUseNotImportedMethod(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            import resolve.to.include : writeln;

            void main() {
                /*<ref>*/writeln("selective");
            }
            """);
    }

    @Test
    public void testStaticImport(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            static import resolve.to.include;

            void main() {
                resolve.to.include./*<ref>*/writeln("static");
            }
            """);
    }

    @Test
    public void testNonStaticImportFullyQualifiedSymbol(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            import resolve.to.include;

            void main() {
                resolve.to.include./*<ref>*/writeln("static");
            }
            """);
    }

    @Test
    public void testStaticImportShouldNotResolveDirectUsage(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            static import resolve.to.include;

            void main() {
                /*<ref>*/writeln("static");
            }
            """,
            """
            module resolve.to.include;

            void write(string a){}
            void writeln(string a){}
            """,
            false);
    }

    @Test
    public void testRenamedImport(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            import io = resolve.to.include;

            void main() {
                io./*<ref>*/writeln("renamed");
            }
            """);
    }

    @Test
    public void testRenamedImportResolveRename(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText1(fixture,
            """
            import /*<resolved>*/io = resolve.to.include;

            void main() {
                /*<ref>*/io.writeln("renamed");
            }
            """);
    }

    @Test
    public void testImportNamedBinding(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText1(fixture,
            """
            import resolve.to.include : /*<resolved>*/foo = writeln;

            void main() {
                /*<ref>*/foo("named selective");
            }
            """);
    }

    @Test
    public void testSelectiveAndRenamedImport(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText1(fixture,
            """
            import io = resolve.to.include: /*<resolved>*/foo = writeln;

            void main() {
                /*<ref>*/foo("renamed import named selective");
            }
            """);
    }

    @Test
    public void testSelectiveAndRenamedImport2(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            import io = resolve.to.include: foo = write;

            void main() {
                io./*<ref>*/writeln("renamed");
            }
            """);
    }

    @Test
    public void testSelectiveImportShouldFindNestedObjectProperImport(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            import resolve.to.include: A;

            void main() {
                A./*<ref>*/B variable;
            }
            """,
            """
            module resolve.to.include;
            struct A {
                struct /*<resolved>*/B {}
                void foo{}
            }

            """);
    }

    @Test
    public void testSelectiveImportShouldNotFindNestedObject(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            import resolve.to.include: B;

            void main() {
                A./*<ref>*/B variable;
            }
            """,
            """
            module resolve.to.include;
            struct A {
                struct B {}
                void foo{}
            }

            """,
            false);
    }

    @Test
    public void testSelectiveImportShouldResolveSelectiveImportSection(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            import resolve.to.include : /*<ref>*/writeln;
            """);
    }

    @Test
    public void testSelectiveRenamedImportShouldResolveSelectiveImportSection(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            import resolve.to.include : foo = /*<ref>*/writeln;
            """);
    }

    @Test
    public void testSelectiveRenamedImportShouldResolveSelectiveImportSectionSameName(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText(fixture,
            """
            import resolve.to.include : writeln = /*<ref>*/writeln;
            """);
    }

    @Test
    public void testResolveInAlias(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            import resolve.to.include;

            final class A {
                alias B = /*<ref>*/Foo.Bar;
            }
            """,
            """
            module resolve.to.include;
            enum /*<resolved>*/Foo {
                Bar
            }

            """);
    }

    @Test
    public void testResolveAnonymousEnumMember(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            import resolve.to.include;

            void fun() {
                 int i = /*<ref>*/Bar;
            }
            """,
            """
            module resolve.to.include;
            enum {
                /*<resolved>*/Bar
            }
            """);
    }

    @Test
    public void testFullyQualifiedSymbolType(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            import resolve.to.include;

            class A : resolve.to.include./*<ref>*/A {}
            """,
            """
            module resolve.to.include;

            class /*<resolved>*/A {}
            """);
    }

    @Test
    public void testRenamedImportForType(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            import resolve.to.include : /*<resolved>*/renamed = A;

            const /*<ref>*/renamed element;
            """,
            """
            module resolve.to.include;

            class A {}
            """);
    }
}
