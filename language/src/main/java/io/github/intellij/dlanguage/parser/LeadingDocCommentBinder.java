package io.github.intellij.dlanguage.parser;

import consulo.language.ast.IElementType;
import consulo.language.parser.WhitespacesAndCommentsBinder;
import io.github.intellij.dlanguage.psi.DTokenSets;

import java.util.List;

public class LeadingDocCommentBinder implements WhitespacesAndCommentsBinder {
    public static final LeadingDocCommentBinder INSTANCE = new LeadingDocCommentBinder();

    @Override
    public int getEdgePosition(
        List<IElementType> tokens,
        boolean atStreamEdge,
        WhitespacesAndCommentsBinder.TokenTextGetter getter
    ) {
        // bind all doc comments before a declaration
        for (int idx = 0; idx < tokens.size(); idx++) {
            if (DTokenSets.DOCS.contains(tokens.get(idx))) {
                return idx;
            }
        }
        return tokens.size();
    }
}
