package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.stubs.DLanguageSingleImportStub;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;


public interface DLanguageSingleImport extends PsiElement, DNamedElement, StubBasedPsiElement<DLanguageSingleImportStub> {
    @Nullable PsiElement getIdentifier();

    @Nullable PsiElement getOP_EQ();

    @Nullable DLanguageIdentifierChain getIdentifierChain();

    boolean hasImportBinds();

    Set<String> getApplicableImportBinds();

    Map<String, String> getApplicableNamedImportBinds();

    String getImportedModuleName();
}
