package io.github.intellij.dlanguage.features;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.documentation.LanguageDocumentationProvider;
import consulo.language.psi.PsiDocCommentBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SyntaxTraverser;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.documentation.psi.DlangDocComment;
import io.github.intellij.dlanguage.features.documentation.DDocGenerator;
import io.github.intellij.dlanguage.features.documentation.DSignatureDocGenerator;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.impl.named.DlangSingleImportImpl;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by francis on 7/18/2017.
 * <p>
 * Registered in upstream's {@code plugin.xml} as {@code lang.documentationProvider} for D.
 */
@ExtensionImpl
public class DDocumentationProvider implements LanguageDocumentationProvider {

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    /**
     * Returns the text to show in the Ctrl-hover popup for the specified element.
     *
     * @param element         the element for which the documentation is requested (for example, if the mouse is over
     *                        a method reference, this will be the method to which the reference is resolved).
     * @param originalElement the element under the mouse cursor
     * @return the documentation to show, or null if the provider can't provide any documentation for this element.
     */
    @Override
    public @Nullable String getQuickNavigateInfo(@Nullable PsiElement element, @Nullable PsiElement originalElement) {
        return null;
    }

    /*
     * Returns the list of possible URLs to show as external documentation for the specified element.
     * The URL's are shown in UI by clicking on 'View' -> 'ExternalDocumentation', or by using Ctrl+F1
     *
     * This is different to the documentation that is displayed on hover.
     *
     * todo: Add further logic to actually return other useful results from dlang.org (phobos or language spec)
     *  https://dlang.org/spec/spec.html
     *  https://dlang.org/phobos/index.html
     */
    @Override
    @RequiredReadAction
    public List<String> getUrlFor(@Nullable PsiElement element, @Nullable PsiElement originalElement) {
        if (originalElement != null) {
            PsiElement singleImport = PsiTreeUtil.findFirstParent(originalElement, true, t -> t instanceof DLanguageSingleImport);

            String moduleName = singleImport != null ? ((DlangSingleImportImpl) singleImport).getImportedModuleName() : null;

            if (moduleName != null && moduleName.startsWith("std.")) {
                return List.of("https://dlang.org/phobos/" + moduleName.replace('.', '_') + ".html");
            }
            else {
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void collectDocComments(PsiFile file, Consumer<? super PsiDocCommentBase> sink) {
        if (!(file instanceof DlangPsiFile)) {
            return;
        }
        for (PsiElement element : SyntaxTraverser.psiTraverser(file)) {
            if (element instanceof DlangDocComment docComment) {
                sink.accept(docComment);
            }
        }
    }

    @Override
    @RequiredReadAction
    public @Nullable String generateRenderedDoc(PsiDocCommentBase comment) {
        if (!(comment instanceof DlangDocComment docComment)) {
            return LanguageDocumentationProvider.super.generateRenderedDoc(comment);
        }
        return new DDocGenerator().generateDocRendered(docComment);
    }

    /**
     * Callback for asking the doc provider for the complete documentation.
     * Underlying implementation may be time-consuming, that's why this method is expected not to be called from EDT.
     * <p>
     * One can use {@link consulo.language.editor.documentation.DocumentationMarkup} to get proper content layout. Typical sample will look like this:
     * <pre>
     * DEFINITION_START + definition + DEFINITION_END +
     * CONTENT_START + main description + CONTENT_END +
     * SECTIONS_START +
     * SECTION_HEADER_START + section name +
     * SECTION_SEPARATOR + "&lt;p&gt;" + section content + SECTION_END +
     * ... +
     * SECTIONS_END
     * </pre>
     * <p>
     * To show different content on mouse hover in editor, {@link #generateHoverDoc} should be implemented.
     *
     * @param element         the PSI element for which the documentation is requested (for example, if the mouse is over
     *                        a method reference, this will be the method to which the reference is resolved).
     * @param originalElement the element under the mouse cursor
     * @return target element's documentation, or {@code null} if provider is unable to generate documentation
     * for the given element
     */
    @Override
    @RequiredReadAction
    public @Nullable String generateDoc(@Nullable PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof DNamedElement) {
            StringBuilder builder = new StringBuilder();
            PsiElement declarationElement = element;
            new DSignatureDocGenerator().appendDeclarationHeader(builder, declarationElement, element);
            String doc = new DDocGenerator().generateDoc(element);
            builder.append(doc);
            String result = builder.toString();
            return result.isBlank() ? null : result;
        }
        return element == null ? null : LanguageDocumentationProvider.super.generateDoc(element, originalElement);
    }
}
