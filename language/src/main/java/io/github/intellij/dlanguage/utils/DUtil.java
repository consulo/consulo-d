package io.github.intellij.dlanguage.utils;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixin;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

/**
 * General util class. Provides methods for finding named nodes in the Psi tree.
 */
public final class DUtil {
    public static final DUtil INSTANCE = new DUtil();

    private DUtil() {
    }

    /**
     * Tells whether a named node is a definition node based on its context.
     * <p>
     * <p>
     * Precondition: Element is in a DLanguage file.
     */
    public boolean definitionNode(PsiNamedElement e) {
        return e instanceof Declaration;
    }

    /**
     * Tells whether a node is a definition node based on its context.
     */
    public boolean definitionNode(ASTNode node) {
        PsiElement element = node.getPsi();
        return element instanceof PsiNamedElement namedElement && definitionNode(
            namedElement
        );
    }

    public static boolean isNotNullOrEmpty(@Nullable String str) {
        return (str != null && !str.isEmpty());
    }


    public static boolean isDunitTestFile(@Nullable PsiFile psiFile) {
        Collection<DLanguageClassDeclaration> cds = PsiTreeUtil.findChildrenOfType(psiFile, DLanguageClassDeclaration.class);
        for (DLanguageClassDeclaration cd : cds) {
            // if a class contains the UnitTest mixin assume its a valid d-unit test class
            Collection<DLanguageTemplateMixin> tmis = PsiTreeUtil.findChildrenOfType(cd, DLanguageTemplateMixin.class);
            for (DLanguageTemplateMixin tmi : tmis) {
                if (tmi.getText().contains("UnitTest")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param namedElement constructor, or method contained within a class or struct
     * @return the class or struct containing this constructor/method. returns null if not found
     */
    public static @Nullable DNamedElement getParentClassOrStructOrTemplateOrInterfaceOrUnion(@Nullable PsiElement namedElement) {
        return PsiTreeUtil.<DNamedElement>getParentOfType(
            namedElement,
            DLanguageClassDeclaration.class,
            DLanguageStructDeclaration.class,
            DLanguageTemplateDeclaration.class,
            DLanguageUnionDeclaration.class
        );
    }

    public @Nullable ASTNode getPrevSiblingOfType(
        @Nullable ASTNode child,
        @Nullable IElementType type
    ) {
        if (child == null) {
            return null;
        }
        if (child.getElementType() == type) {
            return child.getTreePrev();
        }
        return getPrevSiblingOfType(child.getTreePrev(), type);
    }

    public @Nullable ASTNode getPrevSiblingOfType(
        @Nullable ASTNode child,
        IElementType type,
        @Nullable HashSet<IElementType> excluded
    ) {
        if (child == null) {
            return null;
        }
        if (child.getElementType() == type) {
            return child;
        }
        if (excluded != null && excluded.contains(child.getElementType())) {
            return null;
        }
        return getPrevSiblingOfType(child.getTreePrev(), type, excluded);
    }

    public @Nullable ASTNode getPrevSiblingOfTypes(
        @Nullable ASTNode child,
        HashSet<IElementType> newHashSet,
        @Nullable HashSet<IElementType> excluded
    ) {
        if (child == null) {
            return null;
        }
        if (newHashSet.contains(child.getElementType())) {
            return child;
        }
        if (excluded != null && excluded.contains(child.getElementType())) {
            return null;
        }
        return getPrevSiblingOfTypes(child.getTreePrev(), newHashSet, excluded);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PsiElement> @Nullable T findParentOfType(PsiElement element, Class<T> className) {
        if (className.isInstance(element)) {
            return (T) element;
        }

        // upstream: Optional.ofNullable(element.parent).map { findParentOfType(it, className) }.orElse(null)
        PsiElement parent = element.getParent();
        return parent != null ? findParentOfType(parent, className) : null;
    }

    /**
     * D file names should be composed of the ASCII characters lower case letters, digits or _ and should also not be a Keyword.
     *
     * @param name a filename to check
     * @return true if filename
     */
    public static boolean isValidDlangFileName(String name) {
        return name.matches("[a-zA-Z_0-9]+(\\.di|\\.d)?");
    }
}
