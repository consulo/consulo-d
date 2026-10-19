package io.github.intellij.dlanguage.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageAttribute;
import io.github.intellij.dlanguage.psi.DLanguageBlockStatement;
import io.github.intellij.dlanguage.psi.DLanguageImportBindings;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageSharedStaticConstructor;
import io.github.intellij.dlanguage.psi.DLanguageSharedStaticDestructor;
import io.github.intellij.dlanguage.psi.DLanguageStaticConstructor;
import io.github.intellij.dlanguage.psi.DLanguageStaticDestructor;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.interfaces.DVisibility;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.psi.named.DLanguageDestructor;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.scope.PsiScopesUtil;
import io.github.intellij.dlanguage.psi.scope.processor.DVisibilityProcessor;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageImportDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DLanguageImportDeclarationImpl extends DStubBasedPsiElementBase<DLanguageImportDeclarationStub>
    implements DLanguageImportDeclaration {
    public DLanguageImportDeclarationImpl(DLanguageImportDeclarationStub stub, IStubElementType<?, ?> elementType) {
        super(stub, elementType);
    }

    public DLanguageImportDeclarationImpl(ASTNode node) {
        super(node);
    }

    public void accept(DlangVisitor visitor) {
        visitor.visitImportDeclaration(this);
    }

    @Override
    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor dlangVisitor) {
            accept(dlangVisitor);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public @Nullable PsiElement getKW_STATIC() {
        return findChildByType(DlangTypes.KW_STATIC);
    }

    @Override
    public @Nullable PsiElement getKW_IMPORT() {
        return findChildByType(DlangTypes.KW_IMPORT);
    }

    @Override
    public List<DLanguageSingleImport> getSingleImports() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageSingleImport.class);
    }

    @Override
    public @Nullable DLanguageImportBindings getImportBindings() {
        return PsiTreeUtil.getChildOfType(this, DLanguageImportBindings.class);
    }

    @Override
    public List<PsiElement> getOP_COMMAs() {
        return findChildrenByType(DlangTypes.OP_COMMA);
    }

    @Override
    public @Nullable PsiElement getOP_SCOLON() {
        return findChildByType(DlangTypes.OP_SCOLON);
    }

    @Override
    public DVisibility visibility() {
        DLanguageImportDeclarationStub greenStub = getGreenStub();
        if (greenStub != null) {
            return switch (greenStub.getVisibility()) {
                case EXPORT -> DVisibility.Export.INSTANCE;
                case PUBLIC -> DVisibility.Public.INSTANCE;
                case PRIVATE -> DVisibility.Private.INSTANCE;
                case PROTECTED -> DVisibility.Protected.INSTANCE;
                case PACKAGE -> new DVisibility.Package(null);
                case PACKAGE_SPECIFIC -> new DVisibility.Package(greenStub.getPackageVisibilityName());
            };
        }

        List<DLanguageAttribute> attributes = findChildrenByType(DlangTypes.ATTRIBUTE);
        for (DLanguageAttribute attribute : attributes) {
            if (attribute.getKW_PUBLIC() != null) {
                return DVisibility.Public.INSTANCE;
            }
        }
        DVisibilityProcessor processor = new DVisibilityProcessor();
        if (PsiScopesUtil.INSTANCE.searchAttributeSpecifierInPreviousSiblings(this, processor, ResolveState.initial())
            && processor.getVisibility() != null) {
            return processor.getVisibility();
        }

        PsiElement scope = PsiTreeUtil.getParentOfType(this, DLanguageFunctionDeclaration.class, UserDefinedType.class,
            DLanguageConstructor.class, DLanguageDestructor.class,
            DLanguageStaticConstructor.class, DLanguageStaticDestructor.class,
            DLanguageSharedStaticConstructor.class, DLanguageSharedStaticDestructor.class,
            DLanguageTemplateDeclaration.class, DLanguageTemplateMixinDeclaration.class,
            DLanguageBlockStatement.class);
        PsiElement context = getContext();
        if (context != null && !(context instanceof PsiFile)) {
            PsiScopesUtil.INSTANCE.treeWalkUp(processor, context, scope);
        }
        DVisibility visibility = processor.getVisibility();
        return visibility != null ? visibility : DVisibility.Private.INSTANCE;
    }

    @Override
    public boolean processDeclarations(
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }
}
