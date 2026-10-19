package io.github.intellij.dlanguage.features.formatter.impl;

import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.SpacingBuilder;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.features.formatter.DCodeStyleSettings;

import static io.github.intellij.dlanguage.psi.DlangTypes.ARGUMENT_LIST;
import static io.github.intellij.dlanguage.psi.DlangTypes.CASE_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.IMPORT_BINDINGS;
import static io.github.intellij.dlanguage.psi.DlangTypes.INTERFACE_DECLARATION;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_BREAK;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_CASE;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_CONTINUE;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_ELSE;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_FOR;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_FOREACH;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_IF;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_RETURN;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_SWITCH;
import static io.github.intellij.dlanguage.psi.DlangTypes.LINE_COMMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_COLON;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_COMMA;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_DOT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_LEFT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_RIGHT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_SCOLON;
import static io.github.intellij.dlanguage.psi.DlangTypes.PARAMETERS;
import static io.github.intellij.dlanguage.psi.DlangTypes.SINGLE_IMPORT;

/**
 * Top-level declarations of upstream's {@code spacing.kt}.
 */
public final class SpacingKt {
    private SpacingKt() {
    }

    public static SpacingBuilder createSpacingBuilder(CodeStyleSettings settings, DCodeStyleSettings dSettings) {
        return new SpacingBuilder(settings, DLanguage.INSTANCE)
            .between(SINGLE_IMPORT, IMPORT_BINDINGS).spaceIf(dSettings.SPACE_BEFORE_IMPORT_BINDS_COLON) // import std.stdio : stderr, writeln;
            .afterInside(OP_COLON, IMPORT_BINDINGS).spaces(1) // import std.stdio : stderr, writeln;
            .between(KW_CASE, ARGUMENT_LIST).spaces(1)
            .between(ARGUMENT_LIST, CASE_STATEMENT).spacing(0, 0, 0, false, 0)
            .afterInside(OP_COLON, CASE_STATEMENT).lineBreakInCode()
            .before(OP_COMMA).spaceIf(false)
            .after(OP_COMMA).spaceIf(true)
            .before(OP_SCOLON).spaceIf(false)
            .after(OP_SCOLON).spaceIf(true)
            .around(OP_DOT).none()
            .before(ARGUMENT_LIST).none()
            .afterInside(OP_PAR_LEFT, ARGUMENT_LIST).none()
            .beforeInside(OP_PAR_RIGHT, ARGUMENT_LIST).none()
            .afterInside(OP_PAR_LEFT, PARAMETERS).none()
            .beforeInside(OP_PAR_RIGHT, PARAMETERS).none()
            .after(INTERFACE_DECLARATION).spaces(1)
            .after(KW_RETURN).spaces(1)
            .after(KW_CONTINUE).spaces(1)
            .after(KW_BREAK).spaces(1)
            .after(KW_FOREACH).spaces(1)
            .after(KW_FOR).spaces(1)
            .after(KW_IF).spaces(1)
            .after(KW_ELSE).spaces(1)
            .after(KW_SWITCH).spaces(1)
            .after(LINE_COMMENT).lineBreakInCode();
    }
}
