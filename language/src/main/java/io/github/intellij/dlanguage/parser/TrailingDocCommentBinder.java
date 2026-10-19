package io.github.intellij.dlanguage.parser;

import consulo.language.ast.IElementType;
import consulo.language.parser.WhitespacesAndCommentsBinder;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.psi.DTokenSets;

import java.util.List;

public class TrailingDocCommentBinder implements WhitespacesAndCommentsBinder {
    public static final TrailingDocCommentBinder INSTANCE = new TrailingDocCommentBinder();

    @Override
    public int getEdgePosition(
        List<IElementType> tokens,
        boolean atStreamEdge,
        WhitespacesAndCommentsBinder.TokenTextGetter getter
    ) {
        // bind trailing doc comments to their declaration only if they are on the same line
        int candidate = 0;
        while (candidate < tokens.size()) {
            if (DTokenSets.WHITESPACES.contains(tokens.get(candidate)) && StringUtil.containsLineBreak(
                getter.get(candidate)
            )) {
                break;
            }
            candidate++;
        }
        return candidate;
    }
}
