package io.github.intellij.dlanguage.sdlang.parser;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.impl.parser.GeneratedParserUtilBase;
import consulo.language.parser.PsiBuilder;
import consulo.util.lang.StringUtil;

public class SDLangParserUtil extends GeneratedParserUtilBase {

    public static boolean atNewLine(PsiBuilder b, int level, Parser parser) {
        PsiBuilder.Marker marker = enter_section_(b);
        b.eof(); // skip whitespace
        boolean result = isNextAfterNewLine(b) && parser.parse(b, level);
        exit_section_(b, marker, null, result);
        return result;
    }

    private static boolean isNextAfterNewLine(PsiBuilder b) {
        IElementType prevToken = b.rawLookup(-1);
        return prevToken == null || prevToken == TokenType.WHITE_SPACE && StringUtil.contains(rawLookupText(b, -1), "\n");
    }

    /**
     * Similar to {@code PsiBuilderUtil.rawTokenText}
     */
    private static CharSequence rawLookupText(PsiBuilder b, int steps) {
        int start = b.rawTokenTypeStart(steps);
        int end = b.rawTokenTypeStart(steps + 1);
        return start == -1 || end == -1 ? "" : b.getOriginalText().subSequence(start, end);
    }
}
