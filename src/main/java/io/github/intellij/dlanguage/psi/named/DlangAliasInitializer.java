package io.github.intellij.dlanguage.psi.named;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import consulo.d.psi.impl.stubs.DlangAliasInitializerStub;
import io.github.intellij.dlanguage.psi.DLanguageStorageClass;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;

public interface DlangAliasInitializer extends PsiElement, DNamedElement, StubBasedPsiElement<DlangAliasInitializerStub>
{
	@Nullable
	DlangIdentifier getIdentifier();

	@Nullable
	PsiElement getOP_EQ();

	@NotNull
	List<DLanguageStorageClass> getStorageClasss();

	@Nullable
	DLanguageTemplateParameters getTemplateParameters();

	@Nullable
	DLanguageType getType();
}
