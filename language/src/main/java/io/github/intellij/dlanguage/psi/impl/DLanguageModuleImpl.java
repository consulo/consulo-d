package io.github.intellij.dlanguage.psi.impl;

import consulo.application.util.Queryable;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.PsiElementBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguageModule;
import io.github.intellij.dlanguage.psi.named.DLanguagePackage;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class DLanguageModuleImpl extends PsiElementBase implements DLanguageModule, Queryable {
    private final PsiManager myManager;
    private final DlangPsiFile myFile;

    public DLanguageModuleImpl(PsiManager manager, DlangPsiFile file) {
        myManager = manager;
        myFile = file;
    }

    private @Nullable DLanguagePackage findPackage(String qName) {
        return null;
    }

    private GlobalSearchScope allScope() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getQualifiedName() {
        return myFile.getFullyQualifiedModuleName();
    }

    @Override
    public @Nullable DLanguagePackage getParentPackage() {
        return findPackage(StringUtil.getPackageName(getQualifiedName()));
    }

    @Override
    public PsiFile getFile(GlobalSearchScope searchScope) {
        return myFile;
    }

    @Override
    public String getName() {
        return myFile.getModuleName();
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
    public PsiElement setName(String name) {
        return new DLanguageModuleImpl(myManager, (DlangPsiFile) myFile.setName(name));
    }

    @Override
    public void checkSetName(@Nullable String name) {
    }

    @Override
    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor dlangVisitor) {
            dlangVisitor.visitModule(this);
        }
        else {
            visitor.visitElement(this);
        }
    }

    // upstream overrode getElementIcon(flags) with IconManager.createLayeredIcon(this, AllIcons.Nodes.Package, flags):
    // Consulo's PsiElementBase has no getElementIcon, PSI icons come from IconDescriptorUpdater extensions

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return ItemPresentationProvider.getItemPresentation(this);
    }

    @Override
    public void navigate(boolean requestFocus) {
        myFile.navigate(requestFocus);
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
        myFile.delete();
    }

    @Override
    public PsiElement replace(PsiElement newElement) {
        throw new IncorrectOperationException();
    }

    @Override
    public boolean isWritable() {
        return myFile.isWritable();
    }

    @Override
    public String toString() {
        return "DLanguagePackage:" + getQualifiedName();
    }

    @Override
    public boolean isValid() {
        return myFile.isValid();
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
        info.put("packageQualifiedName", getQualifiedName());
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
        return other instanceof DLanguageModuleImpl otherModule
            && myManager.equals(otherModule.myManager)
            && getQualifiedName().equals(otherModule.getQualifiedName());
    }

    @Override
    public boolean isEquivalentTo(@Nullable PsiElement another) {
        if (another instanceof DlangPsiFile) {
            return myFile.equals(another);
        }
        return super.isEquivalentTo(another);
    }

    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return null;
    }

    @Override
    public boolean processDeclarations(
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return myFile.processDeclarations(processor, state, lastParent, place);
    }
}
