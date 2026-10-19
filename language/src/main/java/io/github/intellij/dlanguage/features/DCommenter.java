package io.github.intellij.dlanguage.features;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.CodeDocumentationAwareCommenter;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiComment;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DlangTypes;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class DCommenter implements CodeDocumentationAwareCommenter {
    @Override
    public String getLineCommentPrefix() {
        return "//";
    }

    @Override
    public String getBlockCommentPrefix() {
        return "/*";
    }

    @Override
    public String getBlockCommentSuffix() {
        return "*/";
    }

    @Override
    public @Nullable String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Override
    public @Nullable IElementType getLineCommentTokenType() {
        return DlangTypes.LINE_COMMENT;
    }

    @Override
    public @Nullable IElementType getBlockCommentTokenType() {
        return DlangTypes.BLOCK_COMMENT;
    }

    @Override
    public String getDocumentationCommentPrefix() {
        return "/**";
    }

    @Override
    public String getDocumentationCommentLinePrefix() {
        return "*";
    }

    @Override
    public String getDocumentationCommentSuffix() {
        return "*/";
    }

    @Override
    public boolean isDocumentationComment(PsiComment element) {
        return element.getTokenType() == DlangTypes.LINE_DOC ||
                element.getTokenType() == DlangTypes.LINE_DOC ||
                element.getTokenType() == DlangTypes.NESTING_BLOCK_DOC;
    }

    @Override
    public @Nullable IElementType getDocumentationCommentTokenType() {
        return DlangTypes.LINE_COMMENT;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}
