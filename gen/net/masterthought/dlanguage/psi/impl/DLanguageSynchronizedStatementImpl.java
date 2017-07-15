

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


public class DLanguageSynchronizedStatementImpl extends ASTWrapperPsiElement implements DLanguageSynchronizedStatement{
       public DLanguageSynchronizedStatementImpl (ASTNode node){
               super(node);
       }
       public void accept(@NotNull DLanguageVisitor visitor){
           visitor.visitSynchronizedStatement(this);
       }
       public void accept(@NotNull PsiElementVisitor visitor){
           if(visitor instanceof DLanguageVisitor) accept((DLanguageVisitor)visitor);
           else super.accept(visitor);
       }

            @Nullable
            public PsiElement getOP_PAR_RIGHT() {
                return findChildByType(OP_PAR_RIGHT);
            }
        
            @Nullable
            public PsiElement getOP_PAR_LEFT() {
                return findChildByType(OP_PAR_LEFT);
            }
        
            @Nullable
            public DLanguageExpression getExpression() {
                return PsiTreeUtil.getChildOfType(this, DLanguageExpression.class);
            }
            @Nullable
            public DLanguageStatementNoCaseNoDefault getStatementNoCaseNoDefault() {
                return PsiTreeUtil.getChildOfType(this, DLanguageStatementNoCaseNoDefault.class);
            }
            @Nullable
            public PsiElement getKW_SYNCHRONIZED() {
                return findChildByType(KW_SYNCHRONIZED);
            }
        
}