package io.github.intellij.dlanguage.resolve;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.GlobalSearchScope;
import io.github.intellij.dlanguage.index.DModuleIndex;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageAnonymousEnumDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageAttributeSpecifier;
import io.github.intellij.dlanguage.psi.DLanguageAutoDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageBaseClassList;
import io.github.intellij.dlanguage.psi.DLanguageBlockStatement;
import io.github.intellij.dlanguage.psi.DLanguageCaseRangeStatement;
import io.github.intellij.dlanguage.psi.DLanguageCaseStatement;
import io.github.intellij.dlanguage.psi.DLanguageConditionalDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageConditionalStatement;
import io.github.intellij.dlanguage.psi.DLanguageDeclarationBlock;
import io.github.intellij.dlanguage.psi.DLanguageDeclarationStatement;
import io.github.intellij.dlanguage.psi.DLanguageDefaultStatement;
import io.github.intellij.dlanguage.psi.DLanguageDoStatement;
import io.github.intellij.dlanguage.psi.DLanguageEnumBody;
import io.github.intellij.dlanguage.psi.DLanguageForStatement;
import io.github.intellij.dlanguage.psi.DLanguageForeachStatement;
import io.github.intellij.dlanguage.psi.DLanguageForeachTypeList;
import io.github.intellij.dlanguage.psi.DLanguageFunctionLiteralExpression;
import io.github.intellij.dlanguage.psi.DLanguageIfCondition;
import io.github.intellij.dlanguage.psi.DLanguageIfStatement;
import io.github.intellij.dlanguage.psi.DLanguageImportBind;
import io.github.intellij.dlanguage.psi.DLanguageImportBindings;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageLabeledStatement;
import io.github.intellij.dlanguage.psi.DLanguageLambdaExpression;
import io.github.intellij.dlanguage.psi.DLanguageNamedImportBind;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageSpecifiedVariableDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageStaticForeachDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageStructBody;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameterList;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.DLanguageTemplateThisParameter;
import io.github.intellij.dlanguage.psi.DLanguageTemplateTypeParameter;
import io.github.intellij.dlanguage.psi.DLanguageWhileStatement;
import io.github.intellij.dlanguage.psi.DLanguageWithStatement;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.interfaces.DVisibility;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.interfaces.Statement;
import io.github.intellij.dlanguage.psi.interfaces.TemplateParameter;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.psi.named.DLanguageCatch;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.psi.named.DLanguageDeclaratorIdentifier;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.psi.named.DLanguageForeachType;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageOutStatement;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageVersionSpecification;
import io.github.intellij.dlanguage.psi.scope.ElementDeclarationHint;
import io.github.intellij.dlanguage.psi.scope.PsiScopesUtil;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.psi.types.UserDefinedDType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * implements processDeclarations for various statements
 */
public final class ScopeProcessorImpl {
    public static final ScopeProcessorImpl INSTANCE = new ScopeProcessorImpl();

    private ScopeProcessorImpl() {
    }

    public boolean processDeclarations(
        DLanguageStructBody element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        boolean toContinue = true;
        for (Declaration declaration : element.getDeclarations()) {
            // don’t re-process the declaration we just processed
            if (lastParent == declaration) {
                continue;
            }
            if (!declaration.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageAliasDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // the element is in the alias declaration, so it can’t match to this declaration
        if (lastParent != null && lastParent.getParent() == element) {
            return true;
        }

        if (!element.getAliasInitializers().isEmpty()) {
            for (DLanguageAliasInitializer varDeclaration : element.getAliasInitializers()) {
                if (!varDeclaration.processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }

        if (!element.getDeclaratorIdentifiers().isEmpty()) {
            for (DLanguageDeclaratorIdentifier varDeclaration : element.getDeclaratorIdentifiers()) {
                if (!processor.execute(varDeclaration, state)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageAliasInitializer element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // our value can see our parameters
        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }

        // Element in initializer cannot see our elements
        if (lastParent != null && lastParent.getParent() == element) {
            return true;
        }

        if (!processor.execute(element, state)) {
            return false;
        }

        return true;
    }

    public boolean processDeclarations(
        DLanguageAutoAssignment element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // Our value can see our parameters
        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }

        // Our children cannot see our elements
        if (lastParent != null && lastParent.getParent() == element) {
            return true;
        }

        return processor.execute(element, state);
    }

    public boolean processDeclarations(
        DLanguageAutoDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // Our children can only access to elements declared before them
        if (lastParent != null && lastParent.getParent() == element) {
            return PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place);
        }

        for (DLanguageAutoAssignment declarator : element.getAutoAssignments()) {
            if (!declarator.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageSpecifiedVariableDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // Our children can only access to elements declared before them
        if (lastParent != null && lastParent.getParent() == element) {
            return PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place);
        }

        for (DLanguageIdentifierInitializer declarator : element.getIdentifierInitializers()) {
            if (declarator.equals(lastParent)) {
                return true;
            }
            if (!processor.execute(declarator, state)) {
                return false;
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageIdentifierInitializer element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }

        // Our children cannot see our elements
        if (lastParent != null && lastParent.getParent() == element) {
            return true;
        }

        return processor.execute(element, state);
    }

    public boolean processDeclarations(
        DLanguageFunctionLiteralExpression element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent != null && lastParent.getParent() != element) {
            // Outside elements cannot see our inner elements
            return true;
        }
        boolean toContinue = true;
        DLanguageParameters parameters = element.getParameters();
        if (parameters != null) {
            for (DLanguageParameter parameter : parameters.getParameters()) {
                if (!parameter.processDeclarations(processor, state, lastParent, place)) {
                    toContinue = false;
                }
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageLambdaExpression element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent != null && lastParent.getParent() != element) {
            // Outside elements cannot see our inner elements
            return true;
        }
        DLanguageParameters parameters = element.getParameters();
        if (parameters != null) {
            for (DLanguageParameter parameter : parameters.getParameters()) {
                if (!parameter.processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageClassDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        ElementDeclarationHint hint = processor.getHint(ElementDeclarationHint.KEY);
        if (hint != null && !hint.shouldProcess(ElementDeclarationHint.DeclarationKind.CLASS)) {
            return true;
        }

        // Base class list cannot match itself (prevent infinite recursion)
        if (lastParent != null && lastParent.equals(element.getBaseClassList())) {
            return true;
        }

        if (!processor.execute(element, state)) {
            return false;
        }

        // We are outside elements so check our members
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        DLanguageStructBody structBody = element.getStructBody();
        if (lastParent == null && structBody != null) {
            if (!structBody.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }

        // We are inside elements so check our parameters, no need to check body, it was already processed
        boolean toContinue = true;
        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }

        // To prevent recursivity, if we checked the parent element, we are sure we don’t want to check the element itself (a child)
        if (lastParent instanceof DLanguageBaseClassList) {
            return true;
        }

        if (toContinue) {
            DLanguageClassDeclaration parentClass = element.getParentClass();
            toContinue = parentClass == null || parentClass.processDeclarations(processor, state, null, place);
        }

        if (hint != null && hint.shouldProcess(ElementDeclarationHint.DeclarationKind.INTERFACE)) {
            Set<DLanguageInterfaceDeclaration> processedInterfaces = new HashSet<>();
            for (DLanguageInterfaceDeclaration parentInterface : element.getInterfaces()) {
                if (!processDeclarationInInterface(parentInterface, processor, state, processedInterfaces, null, place)) {
                    toContinue = false;
                }
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageInterfaceDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return processDeclarationInInterface(element, processor, state, new HashSet<>(), lastParent, place);
    }

    private boolean processDeclarationInInterface(
        DLanguageInterfaceDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        Set<DLanguageInterfaceDeclaration> visited,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (!visited.add(element)) {
            return true;
        }

        ElementDeclarationHint hint = processor.getHint(ElementDeclarationHint.KEY);
        if (hint != null && !hint.shouldProcess(ElementDeclarationHint.DeclarationKind.INTERFACE)) {
            return true;
        }

        // Base class list cannot match itself (prevent infinite recursion)
        if (lastParent != null && lastParent.equals(element.getBaseClassList())) {
            return true;
        }

        if (!processor.execute(element, state)) {
            return false;
        }

        // We are outside elements so check our members
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        DLanguageStructBody structBody = element.getStructBody();
        if (lastParent == null && structBody != null) {
            if (!structBody.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }

        // We are inside elements so check our parameters, no need to check body, it was already processed
        boolean toContinue = true;
        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }

        if (!toContinue) {
            return false;
        }

        // To prevent recursivity, if we checked the parent element, we are sure we don’t want to check the element itself (a child)
        if (lastParent instanceof DLanguageBaseClassList) {
            return true;
        }

        for (DLanguageInterfaceDeclaration parentInterface : element.getInterfaces()) {
            if (!processDeclarationInInterface(parentInterface, processor, state, visited, null, place)) {
                toContinue = false;
            }
        }

        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageStructDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (element.getName() != null) {
            if (!processor.execute(element, state)) {
                return false;
            }
        }

        // We are outside elements so check our members
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        DLanguageStructBody structBody = element.getStructBody();
        if (lastParent == null && structBody != null) {
            if (!structBody.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }

        // We are inside elements so check our parameters, no need to check body, it was already processed
        boolean toContinue = true;
        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageTemplateMixinDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (!processor.execute(element, state)) {
            return false;
        }

        boolean toContinue = true;
        // Outside elements cannot see our parameter nor our members
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }

        // Only check declarations if we want to see inside
        if (lastParent == null) {
            for (Declaration declaration : element.getDeclarations()) {
                if (!declaration.processDeclarations(processor, state, lastParent, place)) {
                    toContinue = false;
                }
            }
        }

        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageTemplateDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent == null || element.equals(lastParent.getParent())) {
            // Only our member can see our elements

            if (!PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place)) {
                return false;
            }

            DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
            if (templateParameters != null) {
                if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }

        if (!processor.execute(element, state)) {
            return false;
        }

        return true;
    }

    public boolean processDeclarations(
        DLanguageUnionDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (element.getName() != null) {
            if (!processor.execute(element, state)) {
                return false;
            }
        }

        // We are outside elements so check our members
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        DLanguageStructBody structBody = element.getStructBody();
        if (lastParent == null && structBody != null) {
            if (!structBody.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }

        boolean toContinue = true;
        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageBlockStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place);
    }

    public boolean processDeclarations(
        DLanguageDeclarationStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // don’t process twice, we just processed this element
        if (lastParent != null && lastParent.getParent() == element) {
            return true;
        }
        Declaration declaration = element.getDeclaration();
        return declaration == null || declaration.processDeclarations(processor, state, lastParent, place);
    }

    public boolean processDeclarations(
        DLanguageFunctionDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent != null && element.equals(lastParent.getParent())) {
            // Only our member can see our elements

            if (!PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place)) {
                return false;
            }

            DLanguageParameters parameters = element.getParameters();
            if (parameters != null) {
                for (DLanguageParameter parameter : parameters.getParameters()) {
                    if (!parameter.processDeclarations(processor, state, lastParent, place)) {
                        return false;
                    }
                }
            }
            DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
            if (templateParameters != null) {
                if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }

        if (!processor.execute(element, state)) {
            return false;
        }

        return true;
    }

    public boolean processDeclarations(
        DLanguageStaticForeachDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent != null && (lastParent.getParent() != element || element.getExpressions().contains(lastParent))) {
            // declaration and our expressions neither cannot see our vars
            return true;
        }

        boolean shouldContinue = true;
        DLanguageForeachType foreachType = element.getForeachType();
        if (foreachType != null) {
            if (!processor.execute(foreachType, state)) {
                shouldContinue = false;
            }
        }
        DLanguageForeachTypeList foreachTypeList = element.getForeachTypeList();
        if (foreachTypeList != null) {
            for (DLanguageForeachType type : foreachTypeList.getForeachTypes()) {
                if (!processor.execute(type, state)) {
                    shouldContinue = false;
                }
            }
        }

        for (Declaration declaration : element.getDeclarations()) {
            // Don’t processe the declaration twice, we just processed it
            if (lastParent != null && lastParent == element) {
                continue;
            }
            if (!declaration.processDeclarations(processor, state, lastParent, place)) {
                shouldContinue = false;
            }
        }

        return shouldContinue;
    }

    public boolean processDeclarations(
        DLanguageForeachStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent == null || lastParent.getParent() != element || element.getExpressions().contains(lastParent)) {
            // Parent element should not see our vars, and the iterated variable cannot see our vars
            return true;
        }

        boolean shouldContinue = true;
        DLanguageForeachTypeList foreachTypeList = element.getForeachTypeList();
        if (foreachTypeList != null) {
            for (DLanguageForeachType foreachType : foreachTypeList.getForeachTypes()) {
                if (!processor.execute(foreachType, state)) {
                    shouldContinue = false;
                }
            }
        }
        return shouldContinue;
    }

    public boolean processDeclarations(
        DLanguageWhileStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        boolean toContinue = true;
        DLanguageIfCondition ifCondition = element.getIfCondition();
        if (ifCondition != null && ifCondition.getIdentifier() != null) {
            if (!processor.execute(ifCondition, state)) {
                toContinue = false;
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageLabeledStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return processor.execute(element, state);
    }


    public boolean processDeclarations(
        DLanguageForStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent == null || !element.equals(lastParent.getParent())) {
            // Parent element should not see our vars
            return true;
        }

        if (element.getStatements().isEmpty()) {
            return true;
        }

        // TODO Extract this logic to be re-usable
        boolean found = false;
        Statement declarationStatement = element.getStatements().getFirst();
        if (!(declarationStatement instanceof DLanguageDeclarationStatement)) {
            return true;
        }
        PsiElement start = element.getOP_PAR_LEFT();
        while (start != null
            && start.getNode().getElementType() != DlangTypes.OP_SCOLON
            && start.getNode().getElementType() != DlangTypes.OP_BRACES_RIGHT) {
            if (declarationStatement.equals(start)) {
                found = true;
                break;
            }
            start = start.getNextSibling();
        }

        if (found && !lastParent.equals(declarationStatement)) {
            if (!declarationStatement.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageDoStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return true;//todo check that while statement's can/can't contain truthy/falsy variable declarations or casts
    }

    public boolean processDeclarations(
        DLanguageIfStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        DLanguageIfCondition ifCondition = element.getIfCondition();
        if (lastParent == null || !element.equals(lastParent.getParent()) || lastParent == ifCondition) {
            // Parent element should not see our vars and value of the declared variable should not see the variable itself
            return true;
        }
        boolean toContinue = true;
        if (ifCondition != null && ifCondition.getIdentifier() != null) {
            if (!processor.execute(ifCondition, state)) {
                toContinue = false;
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageCaseStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent == null || !element.equals(lastParent.getParent())) {
            // Parent element should not see our vars
            return true;
        }

        return PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place);
    }

    public boolean processDeclarations(
        DLanguageCaseRangeStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent == null || !element.equals(lastParent.getParent())) {
            // Parent element should not see our vars
            return true;
        }
        return PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place);
    }

    public boolean processDeclarations(
        DLanguageDefaultStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (lastParent == null || !element.equals(lastParent.getParent())) {
            // Parent element should not see our vars
            return true;
        }

        return PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place);
    }

    public boolean processDeclarations(
        DLanguageWithStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {

        // Outside elements cannot see our members
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }
        // Our expression cannot see our members
        if (lastParent != null && lastParent == element.getExpression()) {
            return true;
        }

        ElementDeclarationHint hint = processor.getHint(ElementDeclarationHint.KEY);

        // Eager return if the processor does not accept elements
        // FIXME: introduce proper type, this check is mainly to prevent PackageOrModule to check it (optimization)
        if (hint != null && !hint.shouldProcess(ElementDeclarationHint.DeclarationKind.CLASS)) {
            return true;
        }

        Expression expression = element.getExpression();
        DType elementType = expression != null ? expression.getDType() : null;
        if (elementType != null && !(elementType instanceof DUnknownType)) {
            if (elementType instanceof UserDefinedDType userDefinedDType) {
                UserDefinedType resolvedTypeElement = userDefinedDType.resolve();
                if (!resolvedTypeElement.processDeclarations(processor, state, null, place)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageEnumDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (!processor.execute(element, state)) {
            return false;
        }

        // Don’t look into the enum if we are known to be outside
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        boolean toContinue = true;
        DLanguageEnumBody enumBody = element.getEnumBody();
        if (enumBody != null) {
            for (DLanguageEnumMember member : enumBody.getEnumMembers()) {
                if (!processor.execute(member, state)) {
                    toContinue = false;
                }
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageAnonymousEnumDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        for (DLanguageEnumMember enumMember : element.getEnumMembers()) {
            // enum member cannot be seen from his assigned value (and values after the declaration neither)
            if (lastParent != null && lastParent == enumMember) {
                continue;
            }

            if (!processor.execute(enumMember, state)) {
                return false;
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageCatch element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // Outside elements cannot see our variable
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        PsiElement identifier = element.getIdentifier();
        if (identifier != null) {
            return processor.execute(identifier, state);
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageSingleImport element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {

        if (element.getImportedModuleName().isBlank()) {
            return true;
        }

        // prevent infinite recursing check
        List<String> processedFiles = state.get(DResolveUtilKt.getPROCESSED_FILES_KEY());
        if (processedFiles != null && processedFiles.contains(element.getImportedModuleName())) {
            return true;
        }

        Set<DlangPsiFile> files = new LinkedHashSet<>(DModuleIndex.getFilesByModuleName(
            element.getProject(),
            element.getImportedModuleName(),
            GlobalSearchScope.allScope(element.getProject())
        ));
        for (DlangPsiFile file : files) {
            if (file.equals(element.getContainingFile())) {
                continue;
            }
            if (!file.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageImportDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (!processor.execute(element, state)) {
            return false;
        }
        ElementDeclarationHint hint = processor.getHint(ElementDeclarationHint.KEY);
        if (hint != null && !hint.shouldProcess(ElementDeclarationHint.DeclarationKind.IMPORT)) {
            return true;
        }

        Boolean isQualifiedSymbol = state.get(DResolveUtilKt.getIS_QUALIFIED_SYMBOL());
        boolean canProcessStatic = isQualifiedSymbol != null ? isQualifiedSymbol : false;
        if (element.getKW_STATIC() != null && !canProcessStatic) {
            return true;
        }

        if (lastParent != null && element.equals(lastParent.getParent())) {
            if (!(place instanceof DLanguageImportBind)) {
                return true;
            }
            // it is a specific symbol in the import bind, look for the original symbol
            DLanguageSingleImport anImport = element.getSingleImports().getLast();
            return anImport.processDeclarations(processor, state, lastParent, place);
        }
        // safeguard if call processDeclarations of others files
        if (!Objects.equals(place.getContainingFile(), element.getContainingFile())
            && !DVisibility.Public.INSTANCE.equals(element.visibility())) {
            return true;
        }
        List<DLanguageSingleImport> singleImports = new ArrayList<>(element.getSingleImports());
        // we have an import binding, search for it first
        DLanguageImportBindings importBindings = element.getImportBindings();
        if (importBindings != null) {
            // with import binding restricts the scope of the corresponding single import
            if (singleImports.isEmpty()) {
                return true; // remove the last, as, due to binding, we can only access to his bindings
            }
            DLanguageSingleImport base = singleImports.removeLast();

            // prevent infinite recursing check
            // We know this symbol is not the one we want, as it is in a file we already processed
            String lastParentModuleName = lastParent != null && lastParent.getContainingFile() instanceof DlangPsiFile lastParentFile
                ? lastParentFile.getFullyQualifiedModuleName()
                : null;
            List<String> processedFiles = state.get(DResolveUtilKt.getPROCESSED_FILES_KEY());
            if (base.getImportedModuleName().equals(lastParentModuleName)
                || (processedFiles != null && processedFiles.contains(base.getImportedModuleName()))) {
                return true;
            }

            for (DLanguageImportBind elt : importBindings.getImportBinds()) {
                DLanguageNamedImportBind namedImportBind = elt.getNamedImportBind();
                if (namedImportBind != null) {
                    if (!processor.execute(namedImportBind, state)) {
                        return false;
                    }
                }
                else {
                    PsiReference reference = elt.getReference();
                    PsiElement found = reference != null ? reference.resolve() : null;
                    if (found != null) {
                        processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, found.getContainingFile());
                        if (!processor.execute(found, state)) {
                            return false;
                        }
                    }
                }
            }
            // import a = foobar;
            if (base.getIdentifier() != null) {
                if (!processor.execute(base, state)) {
                    return false;
                }
            }
        }
        for (DLanguageSingleImport anImport : singleImports) {
            // import a = foobar;
            if (anImport.getIdentifier() != null) {
                if (!processor.execute(anImport, state)) {
                    return false;
                }
                continue;
            }

            // check the symbol itself
            if (!anImport.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageConstructor element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {

        // External elements cannot see our parameters
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        DLanguageParameters parameters = element.getParameters();
        if (parameters != null) {
            for (DLanguageParameter parameter : parameters.getParameters()) {
                if (!parameter.processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }
        DLanguageTemplateParameters templateParameters = element.getTemplateParameters();
        if (templateParameters != null) {
            if (!templateParameters.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;

    }

    public boolean processDeclarations(
        DLanguageConditionalStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        boolean toContinue = true;
        for (Statement statement : element.getStatements()) {
            // Don’t processe the declaration twice, we just processed it
            if (lastParent != null && lastParent == statement) {
                continue;
            }
            // do not break as there can be multiple definition of the same element based on a certain condition
            if (!statement.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageConditionalDeclaration element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // Maybe change Psi structure to separate staticIF and version(Foo) forms into 2 different nodes
        boolean toContinue = true;

        // `version(FOO):` form
        if (element.getKW_ELSE() == null && element.getOP_COLON() != null) {
            // Our member cannot see other declarations
            for (Declaration decl : element.getDeclarations()) {
                // don’t process declaration twice, we just processed it
                if (lastParent != null && lastParent == decl) {
                    continue;
                }
                if (!decl.processDeclarations(processor, state, lastParent, place)) {
                    toContinue = false;
                }
            }
            for (DLanguageDeclarationBlock block : element.getDeclarationBlocks()) {
                // don’t process declaration twice, we just processed it
                if (lastParent != null && lastParent == block) {
                    continue;
                }
                if (!block.processDeclarations(processor, state, lastParent, place)) {
                    toContinue = false;
                }
            }
            return toContinue;
        }

        // Static If
        // Our member cannot see other declarations
        if (lastParent != null && lastParent.getParent() == element) {
            return true;
        }
        for (Declaration decl : element.getDeclarations()) {
            // Note: only 2 declarations are possible the one for the if and the one for the else
            if (!decl.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }
        for (DLanguageDeclarationBlock block : element.getDeclarationBlocks()) {
            // Note: only 2 declarations blocks are possible the one for the if and the one for the else
            if (!block.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageVersionSpecification element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return processor.execute(element, state);
    }

    public boolean processDeclarations(
        DLanguageOutStatement element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        // External elements cannot see our variables
        if (lastParent != null && lastParent.getParent() != element) {
            return true;
        }

        return processor.execute(element, state);
    }

    public boolean processDeclarations(
        DLanguageDeclarationBlock element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        boolean toContinue = true;

        // Our children can only access to elements declared before them
        if (lastParent != null && lastParent.getParent() == element) {
            return PsiScopesUtil.INSTANCE.walkChildrenScopes(element, processor, state, lastParent, place);
        }

        for (Declaration decl : element.getDeclarations()) {
            if (!decl.processDeclarations(processor, state, lastParent, place)) {
                toContinue = false;
            }
        }
        return toContinue;
    }

    public boolean processDeclarations(
        DLanguageAttributeSpecifier element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        if (!processor.execute(element, state)) {
            return false;
        }
        DLanguageDeclarationBlock declarationBlock = element.getDeclarationBlock();
        if (declarationBlock == null) {
            return true;
        }
        return declarationBlock.processDeclarations(processor, state, lastParent, place);
    }

    public boolean processDeclarations(
        DLanguageTemplateParameters element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        DLanguageTemplateParameterList templateParameterList = element.getTemplateParameterList();
        List<TemplateParameter> templateParameters =
            templateParameterList != null ? templateParameterList.getTemplateParameters() : List.of();
        for (TemplateParameter p : templateParameters) {
            if (p instanceof DLanguageTemplateThisParameter templateThisParameter) {
                DLanguageTemplateTypeParameter templateTypeParameter = templateThisParameter.getTemplateTypeParameter();
                if (templateTypeParameter == null) {
                    continue;
                }
                if (!processor.execute(templateTypeParameter, state)) {
                    return false;
                }
            }
            else {
                if (!processor.execute(p, state)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean processDeclarations(
        DLanguageParameter element,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return processor.execute(element, state);
    }
}
