package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageFundamentalTypePropertyExpression;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.psi.types.DArrayType;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public abstract class DLanguageFundamentalTypePropertyExpressionImplMixin extends ASTWrapperPsiElement
    implements DLanguageFundamentalTypePropertyExpression {

    public DLanguageFundamentalTypePropertyExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        PsiElement identifier = getIdentifier();
        if (identifier == null) {
            return null;
        }
        // TODO need to add a reference to be able to resolve static fields
        PsiReference reference = getReference();
        PsiElement result = reference != null ? reference.resolve() : null;
        // matched something, means it is a static field
        if (result instanceof DTypedElement typedElement) {
            return typedElement.getDType();
        }
        DLanguageBasicType basicType = getBasicType();
        DType qualifierType = basicType != null ? basicType.getDType() : null;
        return switch (identifier.getText()) {
            case "init" -> qualifierType;
            case "alignof",
                 "sizeof" -> DPrimitiveType.fromText("uint"); // TODO actually it is a size_t
            case "mangleof",
                 "stringof" -> new DArrayType(DPrimitiveType.fromText("char"), null); // TODO it is immutable(char) array
            case "min",
                 "max" -> isIntOrFloatType(qualifierType) ? qualifierType : null;
            case "infinity",
                 "nan",
                 "epsilon",
                 "min_normal" -> isFloatType(qualifierType) ? qualifierType : null;
            case "dig",
                 "mant_dig",
                 "max_10_exp",
                 "max_exp",
                 "min_10_exp",
                 "min_exp" -> DPrimitiveType.fromText("int");
            // TODO others (specs are not clear enough)
            default -> null;
        };
    }

    private boolean isIntOrFloatType(@Nullable DType type) {
        return type instanceof DPrimitiveType primitiveType
            && Set.of("byte", "ubyte", "short", "ushort", "int", "uint", "long", "ulong", "float", "double")
            .contains(primitiveType.getName());
    }

    private boolean isFloatType(@Nullable DType type) {
        return type instanceof DPrimitiveType primitiveType && Set.of("float", "double").contains(primitiveType.getName());
    }
}
