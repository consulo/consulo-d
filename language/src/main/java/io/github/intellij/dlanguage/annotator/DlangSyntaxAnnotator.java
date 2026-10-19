package io.github.intellij.dlanguage.annotator;

import consulo.application.dumb.DumbAware;
import consulo.d.localize.DLocalize;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DLanguageAnonymousEnumDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageEnumBody;
import io.github.intellij.dlanguage.psi.DLanguageLiteralExpression;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;

import static io.github.intellij.dlanguage.annotator.DlangLiteralCheckerKt.checkLiteral;

/**
 * Registered by {@link consulo.d.language.DlangSyntaxAnnotatorFactory}.
 */
public class DlangSyntaxAnnotator implements Annotator, DumbAware {
    @Override
    public void annotate(PsiElement element, AnnotationHolder holder) {
        if (element instanceof DLanguageLiteralExpression literalExpression) {
            checkLiteral(literalExpression, holder);
        }
        else if (element instanceof DLanguageEnumDeclaration enumDeclaration) {
            DLanguageEnumBody enumBody = enumDeclaration.getEnumBody();
            if (enumBody != null && enumBody.getEnumMembers().isEmpty()) {
                holder.newAnnotation(HighlightSeverity.ERROR, DLocalize.enumNoMembers()).range(element).create();
            }
        }
        else if (element instanceof DLanguageAnonymousEnumDeclaration anonymousEnumDeclaration) {
            if (anonymousEnumDeclaration.getEnumMembers().isEmpty()) {
                holder.newAnnotation(HighlightSeverity.ERROR, DLocalize.enumNoMembers()).range(element).create();
            }
        }
    }
}
