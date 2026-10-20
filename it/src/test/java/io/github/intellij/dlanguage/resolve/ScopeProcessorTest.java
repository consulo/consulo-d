package io.github.intellij.dlanguage.resolve;

import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageStructBody;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Port of upstream {@code ScopeProcessorTest.kt}.
 */
public class ScopeProcessorTest {

    @Test
    public void testProcessDeclarationsWithEmptyList() {
        DLanguageStructBody element = mock(DLanguageStructBody.class);
        when(element.getDeclarations()).thenReturn(List.of());
        PsiScopeProcessor processor = mock(PsiScopeProcessor.class);
        ResolveState state = mock(ResolveState.class);
        PsiElement lastParent = mock(PsiElement.class);
        PsiElement place = mock(PsiElement.class);

        boolean shouldContinue = ScopeProcessorImpl.INSTANCE.processDeclarations(element, processor, state, lastParent, place);
        assertTrue(shouldContinue, "Should continue");
    }

    @Test
    public void testProcessDeclarationsShouldntContinue() {
        DLanguageAliasDeclaration alias = mock(DLanguageAliasDeclaration.class);
        DLanguageAliasInitializer aliasInitializer = mock(DLanguageAliasInitializer.class);
        when(alias.getAliasInitializers()).thenReturn(List.of(aliasInitializer));

        DLanguageStructBody element = mock(DLanguageStructBody.class);
        when(element.getDeclarations()).thenReturn(List.of(aliasInitializer));
        PsiScopeProcessor processor = mock(PsiScopeProcessor.class);
        ResolveState state = mock(ResolveState.class);
        when(processor.execute(aliasInitializer, state)).thenReturn(false);
        PsiElement lastParent = mock(PsiElement.class);
        PsiElement place = mock(PsiElement.class);

        boolean shouldContinue = ScopeProcessorImpl.INSTANCE.processDeclarations(element, processor, state, lastParent, place);
        assertFalse(shouldContinue, "Shouldn't continue");
    }
}
