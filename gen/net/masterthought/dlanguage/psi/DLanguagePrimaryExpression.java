
    package net.masterthought.dlanguage.psi;

    import com.intellij.psi.PsiElement;
    import org.jetbrains.annotations.NotNull;
    import static net.masterthought.dlanguage.psi.DLanguageTypes.*;
    import org.jetbrains.annotations.Nullable;
    import com.intellij.psi.util.PsiTreeUtil;
    import java.util.List;
    import net.masterthought.dlanguage.psi.interfaces.DNamedElement;
    import net.masterthought.dlanguage.psi.interfaces.DCompositeElement;




    public interface DLanguagePrimaryExpression extends PsiElement {
            @Nullable
            public DLanguageType getType();
            @Nullable
            public DLanguageArguments getArguments();
            @Nullable
            public DLanguageFunctionLiteralExpression getFunctionLiteralExpression();
            @Nullable
            public DLanguageTypeofExpression getTypeofExpression();
            @Nullable
            public DLanguageTypeidExpression getTypeidExpression();
            @Nullable
            public DLanguageVector getVector();
            @Nullable
            public DLanguageAssocArrayLiteral getAssocArrayLiteral();
            @Nullable
            public DLanguageArrayLiteral getArrayLiteral();
            @Nullable
            public DLanguageExpression getExpression();
            @Nullable
            public PsiElement getOP_PAR_LEFT();
        
            @Nullable
            public PsiElement getOP_PAR_RIGHT();
        
            @Nullable
            public DLanguageIsExpression getIsExpression();
            @Nullable
            public DLanguageLambdaExpression getLambdaExpression();
            @Nullable
            public DLanguageTraitsExpression getTraitsExpression();
            @Nullable
            public DLanguageMixinExpression getMixinExpression();
            @Nullable
            public DLanguageImportExpression getImportExpression();
            @Nullable
            public PsiElement getKW_SUPER();
        
            @Nullable
            public PsiElement getKW_THIS();
        
            @Nullable
            public PsiElement getOP_DOLLAR();
        
            @Nullable
            public PsiElement getKW_TRUE();
        
            @Nullable
            public PsiElement getKW_FALSE();
        
            @Nullable
            public PsiElement getKW___DATE__();
        
            @Nullable
            public PsiElement getKW___EOF__();
        
            @Nullable
            public PsiElement getKW___FILE__();
        
            @Nullable
            public PsiElement getKW___FILE_FULL_PATH__();
        
            @Nullable
            public PsiElement getKW___FUNCTION__();
        
            @Nullable
            public PsiElement getKW___GSHARED();
        
            @Nullable
            public PsiElement getKW___LINE__();
        
            @Nullable
            public PsiElement getKW___MODULE__();
        
            @Nullable
            public PsiElement getKW___PARAMETERS();
        
            @Nullable
            public PsiElement getKW___PRETTY_FUNCTION__();
        
            @Nullable
            public PsiElement getKW___TIME__();
        
            @Nullable
            public PsiElement getKW___TIMESTAMP__();
        
            @Nullable
            public PsiElement getKW___TRAITS();
        
            @Nullable
            public PsiElement getKW___VECTOR();
        
            @Nullable
            public PsiElement getKW___VENDOR__();
        
            @Nullable
            public PsiElement getKW___VERSION__ ();
        
            @Nullable
            public DLanguageIntegerLiteral getIntegerLiteral();
            @Nullable
            public DLanguageFloatLiteral getFloatLiteral();
                @NotNull
                public List<DLanguageStringLiteral> getStringLiterals();
            @Nullable
            public DLanguageCharacterLiteral getCharacterLiteral();
            @Nullable
            public DLanguageIdentifierOrTemplateInstance getIdentifierOrTemplateInstance();
            @Nullable
            public PsiElement getOP_DOT();
        
            @Nullable
            public DLanguageTypeConstructor getTypeConstructor();
}