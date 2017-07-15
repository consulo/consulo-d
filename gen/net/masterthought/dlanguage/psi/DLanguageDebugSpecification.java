
    package net.masterthought.dlanguage.psi;

    import com.intellij.psi.PsiElement;
    import org.jetbrains.annotations.NotNull;
    import static net.masterthought.dlanguage.psi.DLanguageTypes.*;
    import org.jetbrains.annotations.Nullable;
    import com.intellij.psi.util.PsiTreeUtil;
    import java.util.List;
    import net.masterthought.dlanguage.psi.interfaces.DNamedElement;
    import net.masterthought.dlanguage.psi.interfaces.DCompositeElement;




    public interface DLanguageDebugSpecification extends PsiElement {
            @Nullable
            public PsiElement getKW_DEBUG();
        
            @Nullable
            public PsiElement getOP_EQ();
        
            @Nullable
            public DLanguageIdentifier getIdentifier();
            @Nullable
            public PsiElement getINTEGER_LITERAL();
        
            @Nullable
            public PsiElement getOP_SCOLON();
        
}