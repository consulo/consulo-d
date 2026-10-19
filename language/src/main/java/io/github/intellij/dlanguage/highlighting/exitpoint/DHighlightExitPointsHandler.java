package io.github.intellij.dlanguage.highlighting.exitpoint;

import consulo.codeEditor.Editor;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.editor.util.ProductivityFeatureNames;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import io.github.intellij.dlanguage.psi.DLanguageFunctionLiteralExpression;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;

import java.util.List;
import java.util.function.Consumer;

import static io.github.intellij.dlanguage.psi.ext.PsiElementKt.getAncestors;

public class DHighlightExitPointsHandler extends HighlightUsagesHandlerBase<PsiElement> {
    private PsiElement myTarget;

    public DHighlightExitPointsHandler(Editor editor, PsiFile file, PsiElement target) {
        super(editor, file);
        myTarget = target;
    }

    public PsiElement getTarget() {
        return myTarget;
    }

    public void setTarget(PsiElement target) {
        myTarget = target;
    }

    @Override
    public List<PsiElement> getTargets() {
        return List.of(myTarget);
    }

    @Override
    public String getFeatureId() {
        return ProductivityFeatureNames.CODEASSISTS_HIGHLIGHT_RETURN;
    }

    @Override
    protected void selectTargets(List<PsiElement> targets, Consumer<List<PsiElement>> selectionConsumer) {
        selectionConsumer.accept(targets);
    }

    @Override
    public void computeUsages(List<PsiElement> targets) {
        Consumer<ExitPoint> sink = exitPoint -> {
            if (exitPoint instanceof ExitPoint.Return returnPoint) {
                addOccurrence(returnPoint.getE());
            }
            else if (exitPoint instanceof ExitPoint.Throw throwPoint) {
                addOccurrence(throwPoint.getE());
            }
        };

        PsiElement functionOrFunctionLiteral = getAncestors(myTarget)
            .filter(it -> it instanceof DLanguageFunctionDeclaration || it instanceof DLanguageFunctionLiteralExpression)
            .findFirst()
            .orElse(null);

        if (functionOrFunctionLiteral instanceof DLanguageFunctionDeclaration functionDeclaration) {
            ExitPoint.process(functionDeclaration, sink);
        }
        else if (functionOrFunctionLiteral instanceof DLanguageFunctionLiteralExpression functionLiteralExpression) {
            ExitPoint.process(functionLiteralExpression, sink);
        }
    }
}
