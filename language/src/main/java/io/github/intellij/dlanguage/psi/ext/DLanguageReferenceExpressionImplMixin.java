package io.github.intellij.dlanguage.psi.ext;

import consulo.d.language.resolve.DResolveCaching;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.psi.DLanguageReferenceExpression;
import io.github.intellij.dlanguage.psi.DLanguageTemplateInstance;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.references.GenericExpressionElementReference;
import io.github.intellij.dlanguage.psi.types.DAliasType;
import io.github.intellij.dlanguage.psi.types.DArrayType;
import io.github.intellij.dlanguage.psi.types.DAssociativeArrayType;
import io.github.intellij.dlanguage.psi.types.DPointerType;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.psi.types.UserDefinedDType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageReferenceExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageReferenceExpression {
    public DLanguageReferenceExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        PsiReference reference = getReference();
        PsiElement result = reference != null ? reference.resolve() : null;
        if (result instanceof DTypedElement typedElement) {
            return typedElement.getDType();
        }
        if (result instanceof DLanguageFunctionDeclaration functionDeclaration) {
            return functionDeclaration.getReturnDType();
        }
        Expression expression = getExpression();
        DType qualifierType = expression != null ? expression.getDType() : null;
        PsiElement identifier = getIdentifier();
        String identifierText = identifier != null ? identifier.getText() : null;
        // Compiler defined property? : https://dlang.org/spec/property.html
        DType dtype = switch (identifierText) {
            case "init" -> qualifierType;
            case "alignof",
                 "sizeof" -> DPrimitiveType.fromText("uint"); // TODO actually it is a size_t
            case "mangleof",
                 "stringof" -> new DArrayType(DPrimitiveType.fromText("char"), null); // TODO it is immutable(char) array
            case null, default -> null;
        };
        if (dtype != null) {
            return dtype;
        }
        DArrayType arrayType = getArrayType(qualifierType);
        DAssociativeArrayType assocArrayType = getAssociativeArrayType(qualifierType);
        if (arrayType != null) {
            return switch (identifierText) {
                case "dup" -> arrayType;
                case "idup" -> arrayType; // TODO idup return an immutable array
                case "length" -> DPrimitiveType.fromText("int"); // TODO size_t
                case "ptr" -> new DPointerType(arrayType.getBase());
                case "tupleof" -> arrayType.isFixedSize() ? new DUnknownType() : null; // TODO replace DUnknownType by tuple
                case "capacity" -> arrayType.isFixedSize() ? null : DPrimitiveType.fromText("int"); // TODO size_t
                case null, default -> null;
            };
        }
        else if (assocArrayType != null) {
            return switch (identifierText) {
                case "length" -> DPrimitiveType.fromText("int"); // TODO size_t
                case "dup" -> assocArrayType;
                case "keys" -> new DArrayType(assocArrayType.getKeyType(), null);
                case "values" -> new DArrayType(assocArrayType.getValueType(), null);
                case "rehash" -> assocArrayType;
                case "clear" -> DPrimitiveType.fromText("void");
                case "byKey" -> new DUnknownType(); // TODO
                case "byValue" -> new DUnknownType(); // TODO
                case "byKeyValue" -> new DUnknownType(); // TODO
                // TODO get, require and update which are functions
                case null, default -> null;
            };
        }
        else if (isEnumType(qualifierType)) {
            return switch (identifierText) {
                case "init" -> qualifierType;
                case "min",
                     "max" -> DPrimitiveType.fromText("int"); // TODO actually need to fetch the real type from the enum
                case null, default -> null;
            };
        }
        // TODO handle all compiler defined properties (class, struct, vector)

        return null;
    }

    private @Nullable DArrayType getArrayType(@Nullable DType type) {
        if (type instanceof DPointerType pointerType) {
            return getArrayType(pointerType.getBase());
        }
        if (type instanceof DAliasType aliasType) {
            return getArrayType(aliasType.getAliased());
        }
        return type instanceof DArrayType arrayType ? arrayType : null;
    }

    private @Nullable DAssociativeArrayType getAssociativeArrayType(@Nullable DType type) {
        if (type instanceof DPointerType pointerType) {
            return getAssociativeArrayType(pointerType.getBase());
        }
        if (type instanceof DAliasType aliasType) {
            return getAssociativeArrayType(aliasType.getAliased());
        }
        return type instanceof DAssociativeArrayType associativeArrayType ? associativeArrayType : null;
    }

    private boolean isEnumType(@Nullable DType type) {
        if (type instanceof DPointerType pointerType) {
            return isEnumType(pointerType.getBase());
        }
        if (type instanceof DAliasType aliasType) {
            return isEnumType(aliasType.getAliased());
        }
        return type instanceof UserDefinedDType userDefinedDType && userDefinedDType.resolve() instanceof DLanguageEnumDeclaration;
    }

    /**
     * Consulo: one reference object per element until the file changes, see {@link DResolveCaching}.
     */
    @Override
    public @Nullable PsiReference getReference() {
        return DResolveCaching.reference(this, this::createReference);
    }

    private @Nullable PsiReference createReference() {
        PsiElement referenceElement;
        TextRange range;
        PsiElement identifier = getIdentifier();
        DLanguageTemplateInstance templateInstance = getTemplateInstance();
        PsiElement templateInstanceIdentifier = templateInstance != null ? templateInstance.getIdentifier() : null;
        if (identifier != null) {
            referenceElement = identifier;
            range = referenceElement.getTextRangeInParent();
        }
        else if (templateInstance != null && templateInstanceIdentifier != null) {
            referenceElement = templateInstanceIdentifier;
            range = referenceElement.getTextRangeInParent().shiftRight(templateInstance.getStartOffsetInParent());
        }
        else {
            return null;
        }
        return new GenericExpressionElementReference(this, range, getExpression(), referenceElement.getText());
    }
}
