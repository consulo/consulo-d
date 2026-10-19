package consulo.d.language.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.CompletionUtilCore;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiQualifiedReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.ProcessingContext;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.highlighting.DHighlightingTokenSets;
import io.github.intellij.dlanguage.psi.DlangTokenType;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.scope.PsiScopesUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Completes identifiers from the declarations their position can see, found by the same scope walk resolve uses
 * (upstream's completion is DCD's, which Consulo does not ship), and keywords where the identifier is unqualified.
 */
@ExtensionImpl
public class DReferenceCompletionContributor extends CompletionContributor {
    public DReferenceCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(DLanguage.INSTANCE), new CompletionProvider() {
            @Override
            @RequiredReadAction
            public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
                PsiElement position = parameters.getPosition();
                if (PsiUtilCore.getElementType(position) != DlangTypes.ID) {
                    return;
                }

                VariantsProcessor processor = new VariantsProcessor();
                PsiElement parent = position.getParent();
                PsiReference reference = parent == null ? null : parent.getReference();
                boolean qualified = false;
                if (reference instanceof PsiQualifiedReference qualifiedReference) {
                    qualified = qualifiedReference.getQualifier() != null;
                    PsiScopesUtil.INSTANCE.resolveAndWalk(processor, qualifiedReference, null);
                }
                else {
                    PsiScopesUtil.INSTANCE.treeWalkUp(processor, position, null);
                }

                PsiFile originalFile = parameters.getOriginalFile();
                for (DNamedElement element : processor.getElements()) {
                    result.addElement(createLookupElement(element, originalFile));
                }

                if (!qualified) {
                    for (IElementType keyword : DHighlightingTokenSets.KEYWORD.getTypes()) {
                        if (keyword instanceof DlangTokenType tokenType) {
                            result.addElement(LookupElementBuilder.create(tokenType.getName()).bold());
                        }
                    }
                }
            }
        });
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @RequiredReadAction
    private static LookupElementBuilder createLookupElement(DNamedElement element, PsiFile originalFile) {
        String name = element.getName();
        LookupElementBuilder builder = LookupElementBuilder.create(element, name == null ? "" : name)
            .withIcon(IconDescriptorUpdaters.getIcon(element, 0));
        PsiFile file = element.getContainingFile();
        if (file != null && !file.getName().equals(originalFile.getName())) {
            builder = builder.withTypeText(file.getName(), true);
        }
        return builder;
    }

    /**
     * Collects every named declaration the walk sees, once per name: inner scopes are walked first and shadow the outer
     * ones.
     */
    private static class VariantsProcessor implements PsiScopeProcessor {
        private final Map<String, DNamedElement> myElements = new LinkedHashMap<>();

        @Override
        @RequiredReadAction
        public boolean execute(PsiElement element, ResolveState state) {
            if (element instanceof DNamedElement namedElement) {
                String name = namedElement.getName();
                if (name != null && !name.isEmpty() && !name.contains(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)) {
                    myElements.putIfAbsent(name, namedElement);
                }
            }
            return true;
        }

        Collection<DNamedElement> getElements() {
            return myElements.values();
        }
    }
}
