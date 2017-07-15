package net.masterthought.dlanguage.psi;

import com.intellij.psi.PsiElement;
import net.masterthought.dlanguage.psi.interfaces.DCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public interface DLanguageDestructor extends PsiElement , DCompositeElement{
    @Nullable
    public DLanguageFunctionBody getFunctionBody();

    @Nullable
    public PsiElement getOP_SCOLON();

    @Nullable
    public PsiElement getOP_PAR_RIGHT();

    @Nullable
    public PsiElement getOP_PAR_LEFT();

    @Nullable
    public PsiElement getKW_THIS();

    @Nullable
    public PsiElement getOP_TILDA();

    @NotNull
    public List<DLanguageMemberFunctionAttribute> getMemberFunctionAttributes();
}
