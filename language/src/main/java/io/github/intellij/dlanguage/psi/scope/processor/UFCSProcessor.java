package io.github.intellij.dlanguage.psi.scope.processor;

import consulo.util.dataholder.Key;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import io.github.intellij.dlanguage.psi.DLanguageImportBind;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageNamedImportBind;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import org.jspecify.annotations.Nullable;

public class UFCSProcessor implements PsiScopeProcessor {
    private final PsiScopeProcessor myDelegate;
    private boolean myIsCurrentModuleEval = false;

    public UFCSProcessor(PsiScopeProcessor delegate) {
        myDelegate = delegate;
    }

    @Override
    public boolean execute(PsiElement element, ResolveState state) {
        if (element instanceof DLanguageImportDeclaration
            || element instanceof DLanguageImportBind
            || element instanceof DLanguageNamedImportBind) {
            return myDelegate.execute(element, state);
        }
        if (myIsCurrentModuleEval
            && (element instanceof DLanguageFunctionDeclaration
            || element instanceof DLanguageTemplateDeclaration
            || element instanceof DLanguageAliasInitializer)) {
            return myDelegate.execute(element, state);
        }
        return true;
    }

    @Override
    public void handleEvent(PsiScopeProcessor.Event event, @Nullable Object associated) {
        if (event == PsiScopeProcessor.Event.SET_DECLARATION_HOLDER) {
            myIsCurrentModuleEval = associated instanceof DlangPsiFile;
        }
    }

    @Override
    public <T> @Nullable T getHint(Key<T> hintKey) {
        // Consulo: the delegate's name hint, see consulo.d.language.resolve.DNameHint
        return myDelegate.getHint(hintKey);
    }
}
