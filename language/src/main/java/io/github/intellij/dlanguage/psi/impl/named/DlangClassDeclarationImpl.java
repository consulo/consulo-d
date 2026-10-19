package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.application.progress.ProgressIndicatorProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.types.UserDefinedDType;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageClassDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.intellij.dlanguage.psi.DlangTypes.KW_CLASS;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_COLON;

public class DlangClassDeclarationImpl extends
    DNamedStubbedPsiElementBase<DLanguageClassDeclarationStub> implements
    DLanguageClassDeclaration {

    public DlangClassDeclarationImpl(final DLanguageClassDeclarationStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DlangClassDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);
        visitor.visitClassDeclaration(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable PsiElement getKW_CLASS() {
        return findChildByType(KW_CLASS);
    }

    @Override
    public @Nullable DLanguageBaseClassList getBaseClassList() {
        return PsiTreeUtil.getChildOfType(this, DLanguageBaseClassList.class);
    }

    @Override
    public @Nullable DLanguageClassDeclaration getParentClass() {
        // Object does not have a parent, it is the root of all the classes
        if ("Object".equals(getQualifiedName())) return null;

        var baseClassElement = getBaseClassList();
        if (baseClassElement == null || baseClassElement.getBasicTypes().isEmpty()) return null;

        var referenceElements = baseClassElement.getBasicTypes();
        for (var element : referenceElements) {
            ProgressIndicatorProvider.checkCanceled();
            var dtype = element.getDType();
            if (dtype instanceof UserDefinedDType elementType) {
                var psiResolved = elementType.resolve();
                if (psiResolved instanceof DLanguageClassDeclaration resolved) {
                    return resolved;
                }
            }
        }
        // TODO should actually return object class
        return null;
    }

    @Override
    public List<DLanguageInterfaceDeclaration> getInterfaces() {
        var baseClassElement = getBaseClassList();
        if (baseClassElement == null || baseClassElement.getBasicTypes().isEmpty()) return List.of();

        var referenceElements = baseClassElement.getBasicTypes();
        var result = new ArrayList<DLanguageInterfaceDeclaration>();
        for (var element : referenceElements) {
            var dtype = element.getDType();
            if (dtype instanceof UserDefinedDType elementType) {
                var psiResolved = elementType.resolve();
                if (psiResolved instanceof DLanguageInterfaceDeclaration resolved) {
                    result.add(resolved);
                }
            }
        }
        return result;
    }

    @Override
    public @Nullable DLanguageConstraint getConstraint() {
        return PsiTreeUtil.getChildOfType(this, DLanguageConstraint.class);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(DlangTypes.ID);
    }

    @Override
    public @Nullable DLanguageTemplateParameters getTemplateParameters() {
        return PsiTreeUtil.getChildOfType(this, DLanguageTemplateParameters.class);
    }

    @Override
    public @Nullable PsiElement getOP_COLON() {
        return findChildByType(OP_COLON);
    }

    @Override
    public @Nullable DLanguageStructBody getStructBody() {
        return PsiTreeUtil.getChildOfType(this, DLanguageStructBody.class);
    }

    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final @Nullable PsiElement lastParent, final PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }

    @Override
    public @Nullable String getQualifiedName() {
        var file = getContainingFile();

        if (file instanceof DlangPsiFile dlangPsiFile)
            return dlangPsiFile.getFullyQualifiedModuleName() + "." + getName();

        return getName();
    }
}
