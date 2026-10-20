package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.logging.Logger;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageSingleImportStub;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static io.github.intellij.dlanguage.utils.DPsiUtilKt.getImportText;

/**
 * Created by francis on 7/14/2017.
 */
public class DlangSingleImportImpl extends DNamedStubbedPsiElementBase<DLanguageSingleImportStub> implements DLanguageSingleImport {

    private static final Logger log = Logger.getInstance(DlangSingleImportImpl.class);

    public DlangSingleImportImpl(final DLanguageSingleImportStub stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DlangSingleImportImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {visitor.visitDNamedElement(this);
        visitor.visitSingleImport(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(DlangTypes.ID);
    }

    @Override
    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(DlangTypes.OP_EQ);
    }

    @Override
    public @Nullable DLanguageIdentifierChain getIdentifierChain() {
        return PsiTreeUtil.getChildOfType(this, DLanguageIdentifierChain.class);
    }

    @Override
    public boolean hasImportBinds() {
        return !getApplicableImportBinds().isEmpty() || !getApplicableNamedImportBinds().isEmpty();
    }

    @Override
    public Set<String> getApplicableImportBinds() {
        DLanguageSingleImportStub greenStub = getGreenStub();
        if (greenStub != null) {
            try {
                return greenStub.getApplicableImportBinds();
            } catch (final NullPointerException e) {
                Logger.getInstance(DlangSingleImportImpl.class).error(e);
            }
        }
        final DLanguageImportBindings importBindings = ((DLanguageImportDeclaration) getParent())
            .getImportBindings();
        if (importBindings != null) {
            final Set<String> set = new HashSet<>();
            for (final DLanguageImportBind importBind : importBindings.getImportBinds()) {
                if (importBind.getIdentifier() != null && importBind.getNamedImportBind() == null) {
                    final String name = importBind.getIdentifier().getText();
                    set.add(name);
                }
            }
            return set;
        }
        return Collections.emptySet();
    }

    @Override
    public Map<String, String> getApplicableNamedImportBinds() {
        DLanguageSingleImportStub greenStub = getGreenStub();
        if (greenStub != null) {
            try {
                return greenStub.getApplicableNamedImportBinds();
            } catch (final NullPointerException e) {
                Logger.getInstance(DlangSingleImportImpl.class).error(e);
            }
        }
        final DLanguageImportBindings importBindings = ((DLanguageImportDeclaration) getParent())
            .getImportBindings();
        if (importBindings != null) {
            final Map<String, String> map = new HashMap<>();
            for (final DLanguageImportBind importBind : importBindings.getImportBinds()) {
                if (importBind.getIdentifier() != null && importBind.getNamedImportBind() != null && importBind.getNamedImportBind().getIdentifier() != null) {
                    final String name = importBind.getNamedImportBind().getIdentifier().getText();
                    final String bind = importBind.getIdentifier().getText();
                    map.put(name, bind);
                }
            }
            return map;
        }
        return Collections.emptyMap();
    }

    @Override
    public String getImportedModuleName() {
        if (getGreenStub() != null) {
            return getGreenStub().getImportedModule();
        }
        if (getIdentifierChain() == null) {
            return "";
        }
        if (getIdentifierChain().getIdentifier() == null) {
            Logger.getInstance(getClass())
                .warn("getIdentifier chain was: \"\". Complete text of symbol: " + getText());
            return "";
        }

        return getImportText(getIdentifierChain());
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    public boolean hasAName() {
        // Consulo: from the stub when there is one
        DLanguageSingleImportStub greenStub = getGreenStub();
        if (greenStub != null) {
            return greenStub.hasName();
        }
        try {
            return getIdentifier() != null;
        } catch (final NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean processDeclarations(PsiScopeProcessor processor,
                                       ResolveState state,
                                       @Nullable PsiElement lastParent,
                                       PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }
}
