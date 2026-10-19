package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.types.DType;

import java.util.Objects;

import static io.github.intellij.dlanguage.psi.types.DTypesUtilKt.getDtypeWithSuffixes;

public abstract class DLanguageTypeImplMixin extends ASTWrapperPsiElement implements DLanguageType {
    public DLanguageTypeImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public DType getDType() {
        // TODO type constructor
        DType dtype = Objects.requireNonNull(getBasicType()).getDType();
        return getDtypeWithSuffixes(dtype, getTypeSuffixs());
    }
}
