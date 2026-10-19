package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.interfaces.Foreach;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageForeachType;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.types.*;
import io.github.intellij.dlanguage.stubs.DLanguageForeachTypeStub;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static io.github.intellij.dlanguage.psi.DlangTypes.*;
import static io.github.intellij.dlanguage.utils.DTypeConversionUtilKt.getCommonType;

public class DLanguageForeachTypeImpl extends
    DNamedStubbedPsiElementBase<DLanguageForeachTypeStub> implements DLanguageForeachType {

    public DLanguageForeachTypeImpl(final DLanguageForeachTypeStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DLanguageForeachTypeImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);
        visitor.visitForeachType(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(ID);
    }

    @Override
    public @Nullable PsiElement getKW_REF() {
        return findChildByType(KW_REF);
    }

    @Override
    public @Nullable PsiElement getKW_ALIAS() {
        return findChildByType(KW_ALIAS);
    }

    @Override
    public @Nullable PsiElement getKW_ENUM() {
        return findChildByType(KW_ENUM);
    }

    @Override
    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(OP_EQ);
    }

    @Override
    public @Nullable Expression getExpression() {
        return PsiTreeUtil.getChildOfType(this, Expression.class);
    }

    @Override
    public @Nullable DLanguageBasicType getBasicType() {
        return PsiTreeUtil.getChildOfType(this, DLanguageBasicType.class);
    }

    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public DType getDType() {
        if (getBasicType() != null) {
            return getBasicType().getDType();
        }

        // Search in foreach statement parent
        var foreachParent = this.getParent().getParent(); // 1st parent is type list

        if (foreachParent instanceof Foreach foreach) {
            if (foreach.getExpressions().isEmpty()) {
                return new DUnknownType();
            }

            // get the type of the expression
            DType expressionType;
            if (foreach.getExpressions().size() > 1) {
                // Specs for foreach https://dlang.org/spec/statement.html#foreach-statement
                // foreach range statement so 2 expressions
                var rangeCommonType = getCommonType(foreach.getExpressions().getFirst().getDType(), foreach.getExpressions().get(1).getDType());
                if (rangeCommonType == null)
                    expressionType = new DUnknownType();
                else
                    expressionType = new DArrayType(rangeCommonType, null);
            } else {
                expressionType = foreach.getExpressions().getFirst().getDType();
            }
            if (expressionType == null || expressionType instanceof DUnknownType) {
                return new DUnknownType();
            }

            // Now get the index of the index of the element in the list (is it the 1st, 2nd, nth variable?)
            var list = Objects.requireNonNull(foreach.getForeachTypeList()).getForeachTypes();
            var elementIdx = -1;
            for (int idx = 0; idx < list.size(); idx++) {
                if (list.get(idx) == this) {
                    elementIdx = idx;
                    break;
                }
            }
            if (elementIdx == -1) {
                // Should be impossible by construction
                return new DUnknownType();
            }
            return getTypeFromExpressionType(expressionType, elementIdx, list);
        }

        // Should be impossible
        return new DUnknownType();
    }

    private DType getTypeFromExpressionType(DType expressionType, int elementIdx, List<DLanguageForeachType> foreachTypes) {
        assert (elementIdx >= 0 && elementIdx < foreachTypes.size()) : "Element should be part of the typeList";
        switch (expressionType) {
            case DArrayType arrayExpressionType -> {
                if (foreachTypes.size() == 1) {
                    return arrayExpressionType.getBase();
                }
                if (foreachTypes.size() == 2) {
                    if (elementIdx == 0) {
                        if (arrayExpressionType.isFixedSize()) {
                            // take the shortest size that fits
                            var size = Objects.requireNonNull(arrayExpressionType.getSize());
                            if (size <= 256)
                                return DPrimitiveType.USHORT;
                            return DPrimitiveType.UINT;
                        }
                        return DPrimitiveType.fromText("int"); // TODO size_t (if size not known, otherwise smallest type that can contain it)
                    } else
                        return arrayExpressionType.getBase();
                }
                return new DUnknownType(); // semantic error, this variable should not exist
            }
            case DAssociativeArrayType associativeArrayType -> {
                if (foreachTypes.size() == 1) {
                    // we only unroll values
                    return associativeArrayType.getValueType();
                }
                if (foreachTypes.size() == 2) {
                    // we unroll keys and values
                    if (elementIdx == 0)
                        return associativeArrayType.getKeyType();
                    else
                        return associativeArrayType.getValueType();
                }
                return new DUnknownType(); // semantic error, this variable should not exist
            }
            case UserDefinedDType userDefinedDType -> {
                var resolved = userDefinedDType.resolve();
                if (resolved instanceof DLanguageStructDeclaration || resolved instanceof DLanguageClassDeclaration || resolved instanceof DLanguageInterfaceDeclaration) {
                    // TODO opApply or opApplyReverse, case need to find the proper method and return its return type
                    // TODO can also be range (popFront or popBack method)
                }
                return new DUnknownType(); // semantic error, we can't foreach over union and enum
            }
            // TODO delegate
            // TODO sequence
            default -> {
                return new DUnknownType();
            }
        }
    }

}
