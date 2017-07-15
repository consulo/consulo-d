

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


public class DLanguageAsmExpImpl extends ASTWrapperPsiElement implements DLanguageAsmExp{
       public DLanguageAsmExpImpl (ASTNode node){
               super(node);
       }
       public void accept(@NotNull DLanguageVisitor visitor){
           visitor.visitAsmExp(this);
       }
       public void accept(@NotNull PsiElementVisitor visitor){
           if(visitor instanceof DLanguageVisitor) accept((DLanguageVisitor)visitor);
           else super.accept(visitor);
       }

            @Nullable
            public DLanguageAsmLogOrExp getAsmLogOrExp() {
                return PsiTreeUtil.getChildOfType(this, DLanguageAsmLogOrExp.class);
            }
            @Nullable
            public DLanguageAsmExp getAsmExp() {
                return PsiTreeUtil.getChildOfType(this, DLanguageAsmExp.class);
            }
            @Nullable
            public PsiElement getOP_QUEST() {
                return findChildByType(OP_QUEST);
            }
        
            @Nullable
            public PsiElement getOP_COLON() {
                return findChildByType(OP_COLON);
            }
        
}