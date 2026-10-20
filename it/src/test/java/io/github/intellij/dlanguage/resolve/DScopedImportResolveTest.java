package io.github.intellij.dlanguage.resolve;

import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test import cases for scoped import.
 *
 * Test cases taken from specs: https://dlang.org/spec/module.html#scoped_imports
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DScopedImportResolveTest extends DResolveTestCase {
    @Test
    public void test1(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            void main()
            {
                void /*<resolved>*/writeln(string) {}
                void foo()
                {
                    /*<ref>*/writeln("bar");
                    import std.stdio;
                    void writeln(string) {}
                }
            }
            """,
            """
            module std.stdio;

                void write(string a){}
                void writeln(string a){}
            """);
    }

    @Test
    public void test2(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            void main()
            {
                void writeln(string) {}
                void foo()
                {
                    import std.stdio;
                    /*<ref>*/writeln("bar");
                    void writeln(string) {}
                }
            }
            """,
            """
            module std.stdio;

                void write(string a){}
                void /*<resolved>*/writeln(string a){}
            """);
    }

    @Test
    public void test3(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            void main()
            {
                void writeln(string) {}
                void foo()
                {
                    import std.stdio;
                    void /*<resolved>*/writeln(string) {}
                    /*<ref>*/writeln("bar");
                }
            }
            """,
            """
            module std.stdio;

                void write(string a){}
                void writeln(string a){}
            """);
    }

    @Test
    public void test4(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            void main()
            {
                void /*<resolved>*/writeln(string) {}
                void foo()
                {
                    import std.stdio;
                    void writeln(string) {}
                }
                /*<ref>*/writeln("bar");
            }
            """,
            """
            module std.stdio;

                void write(string a){}
                void writeln(string a){}
            """);
    }

    @Test
    public void test5(CodeInsightTestFixture fixture) throws Exception {
        doCheckByText2(fixture,
            """
            void main()
            {
                void writeln(string) {}
                void foo()
                {
                    import std.stdio;
                    void writeln(string) {}
                }
                std.stdio./*<ref>*/writeln("bar");
            }
            """,
            """
            module std.stdio;

                void write(string a){}
                void writeln(string a){}
            """,
            false);
    }
}
