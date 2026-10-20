package consulo.d.language.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.impl.psi.stub.StubBasedPsiElementBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.stub.NamedStub;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.DVisibility;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageDeclaratorIdentifier;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageVersionSpecification;
import io.github.intellij.dlanguage.psi.scope.ElementDeclarationHint;
import io.github.intellij.dlanguage.resolve.DResolveUtilKt;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageImportDeclarationStub;
import org.jspecify.annotations.Nullable;


/**
 * Offers the module level declarations of a D module the walk does not start in (an imported module) from the module's
 * stub tree. Upstream walked the AST of every imported module, which parsed it (and every module it imports publicly);
 * the stub tree has the declarations of the module level attribute, {@code version}, {@code static if},
 * {@code static foreach} and declaration blocks as direct children of the file stub, and is read from the stub index
 * without parsing.
 * <p>
 * What is offered is what the AST walk offered to a walk coming from another module: the declarations themselves, not
 * their members or template parameters; the {@code public} imports are followed.
 */
public final class DStubDeclarationWalker {
    private DStubDeclarationWalker() {
    }

    @RequiredReadAction
    public static boolean processModule(
        DlangPsiFile file,
        StubElement<?> fileStub,
        PsiScopeProcessor processor,
        ResolveState state,
        ResolveState childState,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        String name = DNameHint.getName(processor);
        boolean toContinue = true;
        processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, file);
        for (StubElement<?> child : fileStub.getChildrenStubs()) {
            if (name != null && child instanceof NamedStub<?> namedStub && !name.equals(namedStub.getName())
                && !(child.getPsi() instanceof DLanguageModuleDeclaration)) {
                continue;
            }
            PsiElement element = child.getPsi();
            if (element == null) {
                continue;
            }
            if (element instanceof DLanguageModuleDeclaration) {
                if (!processor.execute(element, state)) {
                    toContinue = false;
                }
            }
            else if (element instanceof DLanguageImportDeclaration importDeclaration) {
                DLanguageImportDeclarationStub importStub = (DLanguageImportDeclarationStub) child;
                if (!processImport(importDeclaration, importStub, processor, childState, lastParent, place)) {
                    toContinue = false;
                }
                // processing an import may overwrite the declaration holder, so reset it
                processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, file);
            }
            else if (isModuleLevelDeclaration(element)) {
                if (!shouldProcess(element, processor)) {
                    continue;
                }
                if (!processor.execute(element, childState)) {
                    toContinue = false;
                }
            }
        }
        processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, null);
        return toContinue;
    }

    private static boolean isModuleLevelDeclaration(PsiElement element) {
        return element instanceof DLanguageClassDeclaration
            || element instanceof DLanguageInterfaceDeclaration
            || element instanceof DLanguageStructDeclaration
            || element instanceof DLanguageUnionDeclaration
            || element instanceof DLanguageEnumDeclaration
            // the members of an anonymous enum
            || element instanceof DLanguageEnumMember
            || element instanceof DLanguageTemplateDeclaration
            || element instanceof DLanguageTemplateMixinDeclaration
            || element instanceof DLanguageFunctionDeclaration
            || element instanceof DLanguageAliasInitializer
            || element instanceof DLanguageDeclaratorIdentifier
            || element instanceof DLanguageAutoAssignment
            || element instanceof DLanguageIdentifierInitializer
            || element instanceof DLanguageVersionSpecification;
    }

    private static boolean shouldProcess(PsiElement element, PsiScopeProcessor processor) {
        ElementDeclarationHint hint = processor.getHint(ElementDeclarationHint.KEY);
        if (hint == null) {
            return true;
        }
        if (element instanceof DLanguageClassDeclaration) {
            return hint.shouldProcess(ElementDeclarationHint.DeclarationKind.CLASS);
        }
        if (element instanceof DLanguageInterfaceDeclaration) {
            return hint.shouldProcess(ElementDeclarationHint.DeclarationKind.INTERFACE);
        }
        return true;
    }

    /**
     * The import part of {@code ScopeProcessorImpl.processDeclarations(DLanguageImportDeclaration, ...)} for a walk from
     * another module: only {@code public} imports are seen.
     */
    @RequiredReadAction
    private static boolean processImport(
        DLanguageImportDeclaration element,
        DLanguageImportDeclarationStub stub,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (!processor.execute(element, state)) {
            return false;
        }
        ElementDeclarationHint hint = processor.getHint(ElementDeclarationHint.KEY);
        if (hint != null && !hint.shouldProcess(ElementDeclarationHint.DeclarationKind.IMPORT)) {
            return true;
        }

        Boolean isQualifiedSymbol = state.get(DResolveUtilKt.getIS_QUALIFIED_SYMBOL());
        boolean canProcessStatic = isQualifiedSymbol != null ? isQualifiedSymbol : false;
        if (stub.isStatic() && !canProcessStatic) {
            return true;
        }
        if (!DVisibility.Public.INSTANCE.equals(element.visibility())) {
            return true;
        }

        for (DLanguageSingleImport anImport : element.getSingleImports()) {
            if (anImport.hasImportBinds()) {
                // a selective public import: rare, the AST walk handles the bindings
                return ScopeProcessorImpl.INSTANCE.processDeclarations(element, processor, state, lastParent, place);
            }
        }
        for (DLanguageSingleImport anImport : element.getSingleImports()) {
            // import a = foobar;
            if (anImport.hasAName()) {
                if (!processor.execute(anImport, state)) {
                    return false;
                }
                continue;
            }
            if (!anImport.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Offers the members of a type declared in another module than {@code place} from its stub: the members of the
     * attribute, {@code version}, {@code static if} and declaration blocks of its body are stub children of the type.
     *
     * @return whether to continue, or {@code null} when the members have to be read from the AST (the type is in the
     * module of {@code place}, or has no stub)
     */
    @RequiredReadAction
    public static @Nullable Boolean processMembers(
        PsiElement type,
        PsiScopeProcessor processor,
        ResolveState state,
        PsiElement place
    ) {
        if (type.getContainingFile() == place.getContainingFile()
            || !(type instanceof StubBasedPsiElementBase<?> stubBased)) {
            return null;
        }
        StubElement<?> stub = stubBased.getGreenStub();
        if (stub == null) {
            return null;
        }
        String name = DNameHint.getName(processor);
        boolean toContinue = true;
        for (StubElement<?> child : stub.getChildrenStubs()) {
            if (name != null && child instanceof NamedStub<?> namedStub && !name.equals(namedStub.getName())) {
                continue;
            }
            PsiElement element = child.getPsi();
            if (element == null || !isModuleLevelDeclaration(element) || !shouldProcess(element, processor)) {
                continue;
            }
            if (!processor.execute(element, state)) {
                toContinue = false;
            }
        }
        return toContinue;
    }
}
