package io.github.intellij.dlanguage.psi.impl.named;

import static io.github.intellij.dlanguage.psi.DlangTypes.OP_COMMA;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.d.psi.impl.stubs.DlangAliasInitializerStub;
import io.github.intellij.dlanguage.psi.DLanguageStorageClass;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.named.DlangAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DlangIdentifier;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;

public class DLanguageAliasInitializerImpl extends DNamedStubbedPsiElementBase<DlangAliasInitializerStub> implements DlangAliasInitializer
{
	//todo doesn't cover all alias declarations possible
	public DLanguageAliasInitializerImpl(final DlangAliasInitializerStub stub, final IStubElementType type)
	{
		super(stub, type);
	}

	public DLanguageAliasInitializerImpl(final ASTNode node)
	{
		super(node);
	}

	public void accept(@NotNull final DlangVisitor visitor)
	{
		visitor.visitDNamedElement(this);
		visitor.visitAliasInitializer(this);
	}

	public void accept(@NotNull final PsiElementVisitor visitor)
	{
		if(visitor instanceof DlangVisitor)
		{
			accept((DlangVisitor) visitor);
		}
		else
		{
			super.accept(visitor);
		}
	}

	@Override
	@Nullable
	public DlangIdentifier getIdentifier()
	{
		return PsiTreeUtil.getStubChildOfType(this, DlangIdentifier.class);
	}

	@Nullable
	@Override
	public PsiElement getOP_EQ()
	{
		return findChildByType(OP_COMMA);
	}

	@NotNull
	@Override
	public List<DLanguageStorageClass> getStorageClasss()
	{
		return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageStorageClass.class);
	}

	@Override
	@Nullable
	public DLanguageTemplateParameters getTemplateParameters()
	{
		return PsiTreeUtil.getChildOfType(this, DLanguageTemplateParameters.class);
	}

	@Override
	@Nullable
	public DLanguageType getType()
	{
		return PsiTreeUtil.getChildOfType(this, DLanguageType.class);
	}

	@Nullable
	public DlangIdentifier getNameIdentifier()
	{
		return getIdentifier();
	}

	public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final PsiElement lastParent, final PsiElement place)
	{
		return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
	}

}
