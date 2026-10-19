package io.github.intellij.dlanguage.utils;

import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageTemplateTupleParameter;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static consulo.language.psi.util.PsiTreeUtil.findChildrenOfType;


public final class DPsiUtil {
    public static final DPsiUtil INSTANCE = new DPsiUtil();

    private DPsiUtil() {
    }

    public <T extends PsiElement> @Nullable String[] getTexts(List<T> psiElements) {
        int size = psiElements.size();
        @Nullable String[] result = new String[size];
        for (int i = 0; i <= size - 1; i++) {
            result[i] = psiElements.get(i).getText();
        }
        return result;
    }

    private List<Declaration> getDeclDefs(PsiElement defs, List<Declaration> declDefsList) {
        Declaration declDefs = PsiTreeUtil.getChildOfType(defs, Declaration.class);
        if (declDefs != null) {
            declDefsList.add(declDefs);
            getDeclDefs(declDefs, declDefsList);

        }
        return declDefsList;
    }

    /**
     * Returns a map of module -> alias for each imported module.  If a module is imported but not qualified, alias
     * will be null.
     */

    public Set<String> parseImports(PsiFile file) {
        Set<String> imports = new HashSet<>();
        List<Declaration> declDefList = new ArrayList<>();
        declDefList.addAll(findChildrenOfType(file, Declaration.class));
        for (Declaration declDef : declDefList) {
            Collection<DLanguageSingleImport> importDecls = findChildrenOfType(declDef, DLanguageSingleImport.class);
            for (DLanguageSingleImport importDecl : importDecls) {
                imports.add(Objects.requireNonNull(importDecl.getIdentifierChain()).getText());
            }
        }
        return imports;
    }

    public @Nullable PsiElement getParent(PsiElement element, Set<IElementType> targetType, Set<IElementType> excludedType) {
        PsiElement parent = element.getParent();
        if (parent == null || element instanceof DlangPsiFile) {
            return null;
        }

        if (targetType.contains(element.getNode().getElementType())) {
            return element;
        }

        if (excludedType.contains(element.getNode().getElementType())) {
            return null;
        }

        return getParent(parent, targetType, excludedType);

    }

    public ParameterCountRange getNumParameters(DLanguageParameters parameters, PsiElement parameterContainer) {
        int min = 0;
        int max = 0;
        DAttributesFinder finder = new DAttributesFinder(parameterContainer);
        finder.recurseUp();
        if (!finder.isStatic()) {
            min++;
            max++;
        }
        for (DLanguageParameter parameter : parameters.getParameters()) {
            if (parameter.getOP_EQ() != null) {
                max++;
                continue;
            }
            PsiElement param = getQualifiedIdentifierIdentifier(parameter);
            if (param != null) {
                PsiReference reference = param.getReference();
                PsiElement resolve = reference != null ? reference.resolve() : null;
                if (resolve instanceof DLanguageTemplateTupleParameter) {
                    //then the paramater in question is actually a var arg
                    max = Integer.MAX_VALUE;
                    break;
                }
            }
            min++;
            max++;
        }
        if (parameters.getOP_TRIPLEDOT() != null) {
            max = Integer.MAX_VALUE;
        }
        return new ParameterCountRange(min, max);

    }

    /**
     * {@code parameter.type?.basicType?.qualifiedIdentifier?.identifier}
     */
    private static @Nullable PsiElement getQualifiedIdentifierIdentifier(DLanguageParameter parameter) {
        DLanguageType type = parameter.getType();
        if (type == null) {
            return null;
        }
        DLanguageBasicType basicType = type.getBasicType();
        if (basicType == null) {
            return null;
        }
        DLanguageQualifiedIdentifier qualifiedIdentifier = basicType.getQualifiedIdentifier();
        return qualifiedIdentifier != null ? qualifiedIdentifier.getIdentifier() : null;
    }
}
