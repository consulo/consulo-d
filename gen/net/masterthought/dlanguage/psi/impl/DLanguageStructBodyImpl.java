

package net.masterthought.dlanguage.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.masterthought.dlanguage.psi.*;
import java.util.List;
import static net.masterthought.dlanguage.psi.DLanguageTypes.*;


public class DLanguageStructBodyImpl extends ASTWrapperPsiElement implements DLanguageStructBody{
       public DLanguageStructBodyImpl (ASTNode node){
               super(node);
       }
       public void accept(@NotNull DLanguageVisitor visitor){
           visitor.visitStructBody(this);
       }
       public void accept(@NotNull PsiElementVisitor visitor){
           if(visitor instanceof DLanguageVisitor) accept((DLanguageVisitor)visitor);
           else super.accept(visitor);
       }

            @Nullable
            public PsiElement getOP_BRACES_RIGHT() {
                return findChildByType(OP_BRACES_RIGHT);
            }
        
            @Nullable
            public PsiElement getOP_BRACES_LEFT() {
                return findChildByType(OP_BRACES_LEFT);
            }
        
                @NotNull
                public List<DLanguageDeclaration> getDeclarations() {
                    return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageDeclaration.class);
                }
}