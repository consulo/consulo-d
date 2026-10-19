package io.github.intellij.dlanguage.features;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiFile;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DTokenSets;
import io.github.intellij.dlanguage.psi.DlangTypes;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class DBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = {
        new BracePair(DlangTypes.OP_PAR_LEFT, DlangTypes.OP_PAR_RIGHT, false),
        new BracePair(DlangTypes.OP_BRACES_LEFT, DlangTypes.OP_BRACES_RIGHT, true),
        new BracePair(DlangTypes.OP_BRACKET_LEFT, DlangTypes.OP_BRACKET_RIGHT, false)
    };
    private static final TokenSet alwaysMatch = TokenSet.create(DlangTypes.OP_BRACES_LEFT);

    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(IElementType lbraceType, @Nullable IElementType contextType) {
        return (alwaysMatch.contains(lbraceType)
                || DTokenSets.WHITESPACES.contains(contextType)
                || DTokenSets.LINE_COMMENTS.contains(contextType)
                || DTokenSets.BLOCK_COMMENTS.contains(contextType)) || DlangTypes.OP_PAR_RIGHT == contextType || null == contextType;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}
