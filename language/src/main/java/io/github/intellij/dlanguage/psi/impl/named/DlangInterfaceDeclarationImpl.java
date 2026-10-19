package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.types.UserDefinedDType;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageInterfaceDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.intellij.dlanguage.psi.DlangTypes.*;

public class DlangInterfaceDeclarationImpl extends
    DNamedStubbedPsiElementBase<DLanguageInterfaceDeclarationStub> implements
    DLanguageInterfaceDeclaration {

    public DlangInterfaceDeclarationImpl(final DLanguageInterfaceDeclarationStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DlangInterfaceDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);
        visitor.visitInterfaceDeclaration(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable PsiElement getKW_INTERFACE() {
        return findChildByType(KW_INTERFACE);
    }

    @Override
    public @Nullable DLanguageBaseClassList getBaseClassList() {
        return PsiTreeUtil.getChildOfType(this, DLanguageBaseClassList.class);
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
        return findChildByType(ID);
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

}
