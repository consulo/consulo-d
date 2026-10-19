package io.github.intellij.dlanguage.parser;

import consulo.language.ast.IElementType;
import consulo.language.parser.WhitespacesAndCommentsBinder;
import io.github.intellij.dlanguage.psi.DTokenSets;

import java.util.List;

public class TrailingSpaceBinder implements WhitespacesAndCommentsBinder {
    public static final TrailingSpaceBinder INSTANCE = new TrailingSpaceBinder();

    @Override
    public int getEdgePosition(
        List<IElementType> tokens,
        boolean atStreamEdge,
        WhitespacesAndCommentsBinder.TokenTextGetter getter
    ) {
        // bind trailing doc comments to their declaration only if they are on the same line
        int candidate = 0;
        while (candidate < tokens.size()) {
            if (!DTokenSets.WHITESPACES.contains(tokens.get(candidate))) {
                break;
            }
            candidate++;
        }
        return candidate;
    }
}
