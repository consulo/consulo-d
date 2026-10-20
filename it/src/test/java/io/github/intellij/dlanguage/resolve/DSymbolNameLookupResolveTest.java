package io.github.intellij.dlanguage.resolve;

import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 * Test resolution algorithm fom symbol when there is import "conflict".
 *
 * Test case are taken from specs: https://dlang.org/spec/module.html#name_lookup
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DSymbolNameLookupResolveTest extends DResolveTestCase {

    private void doCheckByText3(CodeInsightTestFixture fixture, String main, String second, String third) throws Exception {
        doCheckByText3(fixture, main, second, third, true);
    }

    private void doCheckByText3(CodeInsightTestFixture fixture,
                                String main,
                                String second,
                                String third,
                                boolean succeed) throws Exception {
        String referenceIndicator = "/*<ref>*/";
        String resolvedIndicator = "/*<resolved>*/";
        prepareFileFindReferences(fixture, referenceIndicator, resolvedIndicator, main, "main.d");
        prepareFileFindReferences(fixture, referenceIndicator, resolvedIndicator, second, "file2.d");
        prepareFileFindReferences(fixture, referenceIndicator, resolvedIndicator, third, "file3.d");
        doCheck(succeed);
    }

    @Test
    public void testCModuleFooCase(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            module A;
            void foo();
            void bar();
            """,
            """
            module C;
            import A;
            void /*<resolved>*/foo();
            void test()
            {
                /*<ref>*/foo(); // C.foo() is called
            }
            """);
    }

    @Test
    public void testCModuleBarCase(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            module A;
            void foo();
            void /*<resolved>*/bar();
            """,
            """
            module C;
            import A;
            void foo();
            void test()
            {
                /*<ref>*/bar(); // A.bar() is called
            }
            """);
    }

    /* TODO enable once fixed.
        Needs to properly implement the lookup algorithm for that : https://dlang.org/spec/module.html#name_lookup
    @Test
    public void testDModuleFooCase(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText3(fixture,
            """
            module A;
            void foo();
            void bar();
            """,
            """
            module B;
            void foo();
            void bar();
            """,
            """
            module D;
            import A;
            import B;
            void test()
            {
                /*<ref>foo(); // error, A.foo() or B.foo() ?
            }
            """,
            false);
    }
    */

    @Test
    public void testDModuleAFooCase(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText3(fixture,
            """
            module A;
            void /*<resolved>*/foo();
            void bar();
            """,
            """
            module B;
            void foo();
            void bar();
            """,
            """
            module D;
            import A;
            import B;
            void test()
            {
                A./*<ref>*/foo(); // A.foo() is called
            }
            """);
    }

    @Test
    public void testDModuleBFooCase(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText3(fixture,
            """
            module A;
            void foo();
            void bar();
            """,
            """
            module B;
            void /*<resolved>*/foo();
            void bar();
            """,
            """
            module D;
            import A;
            import B;
            void test()
            {
                B./*<ref>*/foo(); // B.foo() is called
            }
            """);
    }

    @Test
    public void testEModuleFooCase(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText3(fixture,
            """
            module A;
            void foo();
            void bar();
            """,
            """
            module B;
            void foo();
            void bar();
            """,
            """
            module E;
            import A;
            import B;
            alias /*<resolved>*/foo = B.foo;
            void test()
            {
                /*<ref>*/foo(); // call B.foo()
            }
            """);
    }

    @Test
    public void testEModuleAFooCase(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText3(fixture,
            """
            module A;
            void /*<resolved>*/foo();
            void bar();
            """,
            """
            module B;
            void foo();
            void bar();
            """,
            """
            module E;
            import A;
            import B;
            alias foo = B.foo;
            void test()
            {
                A./*<ref>*/foo(); // A.foo() is called
            }
            """);
    }

    @Test
    public void testEModuleBFooCase(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText3(fixture,
            """
            module A;
            void foo();
            void bar();
            """,
            """
            module B;
            void foo();
            void bar();
            """,
            """
            module E;
            import A;
            import B;
            alias /*<resolved>*/foo = B.foo;
            void test()
            {
                /*<ref>*/foo(); // B.foo() is called
            }
            """);
    }

    // Note: this test is not technically in the spec files, but it is here to validate the resolve algorithm
    // AKA: if a package (of imported module) as the same as a symbol, then it resolve to the package and not to the symbol
    // The test ensure that we don’t resolve the symbol (it is not as simple to check that it resolve correctly to the package,
    // but this case is already covered by other tests)
    @Test
    public void testPackageResolvingHasPriorityOverSymbol(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText3(fixture,
            """
            module principal;

            void main() {
                import std.stdio;
                import a;

                /*<ref>*/std;
            }
            """,
            """
            module std.stdio;

            void writeln();
            """,
            """
            module a;
            void /*<resolved>*/std();
            """,
            false);
    }
}
