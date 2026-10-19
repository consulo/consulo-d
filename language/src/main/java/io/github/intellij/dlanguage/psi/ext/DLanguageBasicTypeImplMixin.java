package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageBuiltinType;
import io.github.intellij.dlanguage.psi.DLanguageNamedImportBind;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageModule;
import io.github.intellij.dlanguage.psi.named.DLanguagePackage;
import io.github.intellij.dlanguage.psi.types.DArrayType;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;

public abstract class DLanguageBasicTypeImplMixin extends ASTWrapperPsiElement implements DLanguageBasicType {
    public DLanguageBasicTypeImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public DType getDType() {
        DLanguageBuiltinType builtinType = getBuiltinType();
        if (builtinType != null) {
            return DPrimitiveType.fromText(builtinType.getText());
        }
        DLanguageQualifiedIdentifier qualifiedIdentifier = getQualifiedIdentifier();
        if (qualifiedIdentifier != null) {
            PsiReference reference = qualifiedIdentifier.getReference();
            PsiElement resolved = reference != null ? reference.resolve() : null;
            DType dType = new DUnknownType();
            if (resolved != null) {
                if (resolved instanceof DTypedElement typedElement) {
                    dType = typedElement.getDType();
                }
                else if (resolved instanceof DLanguageFunctionDeclaration functionDeclaration) {
                    dType = functionDeclaration.getReturnDType();
                }
                else if (resolved instanceof DLanguageNamedImportBind) {
                    dType = new DUnknownType(); // TODO implement (needs to put the implementation int the NamedImportBind)
                }
                else if (resolved instanceof DLanguagePackage) {
                    dType = new DUnknownType();
                }
                else if (resolved instanceof DLanguageModule) {
                    dType = new DUnknownType();
                }
                else {
                    assert false : "Unexpected/Unimplemented case of Qualified identifier DType for a Basic Type";
                    dType = new DUnknownType();
                }
            }
            if (qualifiedIdentifier.getOP_BRACKET_LEFT() != null) {
                return new DArrayType(dType, null); // TODO set size
            }
            return dType;
        }
        DLanguageType type = getType();
        if (type != null) {
            DType dtype = type.getDType();
            // TODO theses are type constraints, they enforce different properties on the values of this types
            if (getKW_CONST() != null) {

            }
            if (getKW_IMMUTABLE() != null) {

            }
            if (getKW_INOUT() != null) {

            }
            if (getKW_SHARED() != null) {

            }
            return dtype;
        }
        // Fallback case
        return new DUnknownType();
    }
}
