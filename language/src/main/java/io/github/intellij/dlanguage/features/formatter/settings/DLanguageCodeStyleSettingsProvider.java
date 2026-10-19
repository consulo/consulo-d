package io.github.intellij.dlanguage.features.formatter.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.Configurable;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.CommonCodeStyleSettings;
import consulo.language.codeStyle.CustomCodeStyleSettings;
import consulo.language.codeStyle.setting.CodeStyleSettingsCustomizable;
import consulo.language.codeStyle.setting.IndentOptionsEditor;
import consulo.language.codeStyle.setting.LanguageCodeStyleSettingsProvider;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractConfigurable;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractPanel;
import consulo.language.codeStyle.ui.setting.SmartIndentOptionsEditor;
import consulo.ui.annotation.RequiredUIAccess;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.features.formatter.DCodeStyleSettings;

/**
 * Registered in upstream's {@code plugin.xml} as {@code langCodeStyleSettingsProvider}. Consulo builds code style pages
 * from {@code CodeStyleSettingsProvider} extensions only: {@code consulo.d.language.DCodeStyleSettingsProvider} asks this
 * provider for its page.
 */
@ExtensionImpl
public class DLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    private static final String DEFAULT_CODE_SAMPLE =
        """

        module foo.bar.example;

        import std.stdio : writeln;

        class A {
        private:
            int a;
        public:
            void foo() {
                import std.stdio : writefln;
                string bar = "test";
                writefln("variable bar = `%s`", bar);
            }
            protected float[] n;
        }

        alias AClass = A;

        version (Test) {
            static assert (10 == 5 * 2, "The computation arithmetic is buggy");
        }

        string bar(int i) {
            string ret = "got ";
            switch (i) {
                case 1:
                    return ret ~ "one";
                case 2:
                    throw Exception("two is forbidden");
                case 3: .. case 12:
                    ret ~= "three to twelve";
                case 13:
                case 14:
                    ret ~= "thirteen or fourteen";
                default:
                    assert(false);
            }
            return ret;
        }

        void foo(int x, string...) {
            try {
                writeln(bar(3));
            } catch (Exception e) {
                writeln(e);
            }
        }

        long square_root(long x) pure @system @nogc
        in {
            assert(x >= 0);
        } out (result) {
            assert((result * result) <= x
                && (result+1) * (result+1) > x);
        } do {
            return cast(long)std.math.sqrt(cast(real)x);
        }

        @safe unittest
        {
            import std.exception : assertThrown, assertNotThrown;
            static import std.file;

            auto deleteme = testFilename();
            std.file.write(deleteme, "foo");
            scope(exit) std.file.remove(deleteme);
            auto f = File(deleteme);
            assert(f.readln() == "foo");
        }

        void doSomething(int function(int, int) doer) {
            // call passed function
            doer(5,5);
        }

        auto add(T)(T lhs, T rhs) {
            return lhs + rhs;
        }

        void main() {
            string a = "this
            is
            a
            multiline string";

            mixin(`string b = r"raw \\n string";`);

            "Hello".writeln;
            add(3f, 12.67f);
            doSomething(&add);

            bool compiles = __traits(compiles, obvious error - $%42);

            int[string] assoc_array;
            assoc_array["shoe"] = 12;
            assoc_array["tie"] = 1;
        }
        """;

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    public String getCodeSample(SettingsType settingsType) {
        return DEFAULT_CODE_SAMPLE;
    }

    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new SmartIndentOptionsEditor();
    }

    /**
     * Upstream's {@code createConfigurable}.
     */
    @Override
    public Configurable createSettingsPage(CodeStyleSettings baseSettings, CodeStyleSettings modelSettings) {
        return new CodeStyleAbstractConfigurable(baseSettings, modelSettings, getConfigurableDisplayName()) {
            @Override
            @RequiredUIAccess
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
                return new DCodeStyleMainPanel(getCurrentSettings(), settings);
            }
        };
    }

    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new DCodeStyleSettings(settings);
    }

    /**
     * Consulo has no {@code customizeDefaults}: the defaults are built here and customized as upstream does.
     */
    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings commonSettings = new CommonCodeStyleSettings(getLanguage());
        CommonCodeStyleSettings.IndentOptions indentOptions = commonSettings.initIndentOptions();
        customizeDefaults(commonSettings, indentOptions);
        return commonSettings;
    }

    private void customizeDefaults(CommonCodeStyleSettings commonSettings,
                                   CommonCodeStyleSettings.IndentOptions indentOptions) {
        indentOptions.INDENT_SIZE = 4;
        indentOptions.CONTINUATION_INDENT_SIZE = 4;
        indentOptions.TAB_SIZE = 4;
        indentOptions.USE_TAB_CHARACTER = false;

        commonSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false;
        commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false;
    }

    @Override
    public void customizeSettings(CodeStyleSettingsCustomizable consumer, SettingsType settingsType) {
        switch (settingsType) {
            case SPACING_SETTINGS -> consumer.showCustomOption(DCodeStyleSettings.class,
                "SPACE_BEFORE_IMPORT_BINDS_COLON",
                DLocalize.codeStyleSpaceBeforeImportColon().get(),
                CodeStyleSettingsCustomizable.SPACES_OTHER);
            case WRAPPING_AND_BRACES_SETTINGS -> {
            }
            case BLANK_LINES_SETTINGS -> {
            }
            case COMMENTER_SETTINGS -> {
            }
            default -> consumer.showStandardOptions();
        }
    }

}
