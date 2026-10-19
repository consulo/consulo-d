package io.github.intellij.dlanguage.psi.impl;

import consulo.application.util.Queryable;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.PsiElementBase;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiNavigationSupport;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.module.content.DirectoryIndex;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguagePackage;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DLanguagePackageImpl extends PsiElementBase implements DLanguagePackage, Queryable {
    private final PsiManager myManager;
    private final String myQualifiedName;

    public DLanguagePackageImpl(PsiManager manager, String qualifiedName) {
        myManager = manager;
        myQualifiedName = qualifiedName;
    }

    private @Nullable DLanguagePackage findPackage(String qName) {
        return null;
    }

    private GlobalSearchScope allScope() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getQualifiedName() {
        return myQualifiedName;
    }

    @Override
    public @Nullable DLanguagePackage getParentPackage() {
        return findPackage(StringUtil.getPackageName(myQualifiedName));
    }

    @Override
    public DLanguagePackage[] getSubPackages() {
        return getSubPackages(allScope());
    }

    @Override
    public DLanguagePackage[] getSubPackages(GlobalSearchScope scope) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public PsiFile[] getFiles(GlobalSearchScope searchScope) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void handleQualifiedNameChange(String newQualifiedName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public PsiDirectory[] getDirectories() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public PsiDirectory[] getDirectories(GlobalSearchScope p0) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getName() {
        int index = myQualifiedName.lastIndexOf(".");
        if (index <= 0) {
            return myQualifiedName;
        }
        else {
            return myQualifiedName.substring(index + 1);
        }
    }

    @Override
    public @Nullable DAttributes getAttributes() {
        return null;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public DAttributesFinder.Visibility visibility() {
        return DAttributesFinder.Visibility.PUBLIC;
    }

    @Override
    public boolean isProperty() {
        return false;
    }

    @Override
    public boolean isNoGC() {
        return false;
    }

    @Override
    public boolean isExtern() {
        return false;
    }

    @Override
    public boolean isPure() {
        return false;
    }

    @Override
    public boolean isNothrow() {
        return false;
    }

    @Override
    public boolean isConst() {
        return false;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    @SuppressWarnings("NullAway") // upstream returns the renamed package, which may not be found
    public @Nullable PsiElement setName(String name) {
        checkSetName(name);
        for (PsiDirectory dir : getDirectories()) {
            dir.setName(name);
        }
        String nameAfterRename = PsiUtilCore.getQualifiedNameAfterRename(myQualifiedName, name);
        return findPackage(nameAfterRename);
    }

    @Override
    public void checkSetName(String name) {
        for (PsiDirectory dir : getDirectories()) {
            dir.checkSetName(name);
        }
    }

    @Override
    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor dlangVisitor) {
            dlangVisitor.visitPackage(this);
        }
        else {
            visitor.visitElement(this);
        }
    }

    // upstream's getElementIcon (the package icon) is given by io.github.intellij.dlanguage.DlangIconProvider

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return ItemPresentationProvider.getItemPresentation(this);
    }

    @Override
    public void navigate(boolean requestFocus) {
        // todo: DirectoryIndex is now obsolete so this code needs replacing.
        // also, should only run if canNavigate() returns true
        // (IntelliJ's PsiDirectoryFactory has no Consulo counterpart, PsiManager#findDirectory is used instead)
        Set<PsiDirectory> directories = new LinkedHashSet<>();
        for (VirtualFile file : DirectoryIndex.getInstance(getProject()).getDirectoriesByPackageName(myQualifiedName, true)) {
            PsiDirectory directory = PsiManager.getInstance(getProject()).findDirectory(file);
            if (directory != null) {
                directories.add(directory);
            }
        }
        if (directories.size() != 1) {
            return;
        }
        PsiNavigationSupport.getInstance().navigateToDirectory(directories.iterator().next(), requestFocus);
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    public PsiElement[] getChildren() {
        return PsiElement.EMPTY_ARRAY;
    }

    @Override
    public @Nullable PsiElement getParent() {
        return getParentPackage();
    }

    @Override
    public @Nullable PsiFile getContainingFile() {
        return null;
    }

    @Override
    public TextRange getTextRange() {
        // upstream returns null; Consulo's contract is non-null, and its PsiDirectoryImpl answers the same
        return TextRange.EMPTY_RANGE;
    }

    @Override
    public int getStartOffsetInParent() {
        return -1;
    }

    @Override
    public int getTextLength() {
        return -1;
    }

    @Override
    public @Nullable PsiElement findElementAt(int offset) {
        return null;
    }

    @Override
    public int getTextOffset() {
        return -1;
    }

    @Override
    public String getText() {
        // upstream returns null; Consulo's contract is non-null, and its PsiDirectoryImpl answers the same
        return "";
    }

    @Override
    public char[] textToCharArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("NullAway") // there is no AST behind it, like Consulo's PsiDirectoryImpl
    public @Nullable ASTNode getNode() {
        return null;
    }

    @Override
    public boolean textMatches(CharSequence text) {
        throw new IncorrectOperationException();
    }

    @Override
    public boolean textMatches(PsiElement element) {
        throw new IncorrectOperationException();
    }

    @Override
    public @Nullable PsiElement copy() {
        return null;
    }

    @Override
    public PsiElement add(PsiElement element) {
        throw new IncorrectOperationException();
    }

    @Override
    public PsiElement addBefore(PsiElement element, @Nullable PsiElement anchor) {
        throw new IncorrectOperationException();
    }

    @Override
    public PsiElement addAfter(PsiElement element, @Nullable PsiElement anchor) {
        throw new IncorrectOperationException();
    }

    @Override
    public void checkAdd(PsiElement element) {
        throw new IncorrectOperationException();
    }

    @Override
    public void delete() {
        for (PsiDirectory dir : getDirectories()) {
            dir.delete();
        }
    }

    @Override
    public PsiElement replace(PsiElement newElement) {
        throw new IncorrectOperationException();
    }

    @Override
    public boolean isWritable() {
        PsiDirectory[] dirs = getDirectories();
        for (PsiDirectory dir : dirs) {
            if (!dir.isWritable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "DLanguagePackage:" + myQualifiedName;
    }

    @Override
    public boolean isValid() {
        return !getProject().isDisposed();
    }

    @Override
    public boolean canNavigate() {
        return isValid();
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public boolean isPhysical() {
        return true;
    }

    @Override
    public void putInfo(Map<String, String> info) {
        info.put("packageName", getName());
        info.put("packageQualifiedName", myQualifiedName);
    }

    @Override
    public PsiManager getManager() {
        return myManager;
    }

    @Override
    public Object clone() {
        return super.clone();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof DLanguagePackageImpl otherPackage
            && myManager.equals(otherPackage.myManager)
            && myQualifiedName.equals(otherPackage.myQualifiedName);
    }

    @Override
    public boolean isEquivalentTo(@Nullable PsiElement another) {
        if (another instanceof PsiDirectory directory) {
            return getName().equals(directory.getName());
        }
        return super.isEquivalentTo(another);
    }

    @Override
    public int hashCode() {
        return myQualifiedName.hashCode();
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return null;
    }
}
