package io.github.intellij.dlanguage.resolve;

import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test class for resolving references.  To add a new test, add a method to this class following the conventions below.
 * - If your test is named `testThing` then you should create a `Thing` directory under `gold/resolve`.
 * - Add D files to the new directory.
 * - In your files, insert "<ref>" right before the element you want to resolve.
 * - Insert "<resolved>" right before the element you wish for the reference to resolve to.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DResolveTest extends DResolveTestCase {
    @Test
    public void testArrayLengthVariableToEnumDeclaration(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testArrayLengthVariableToParameter(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testClassUsageToClassDefinition(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testClassInheritedFieldUsageToDefinition(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testClassInheritedFieldWithThisUsageToDefinition(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testClassInheritedFieldWithSuperUsageToDefinition(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testClassParentInheritorShouldNotResolveItself(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testInterfaceParentInheritorShouldNotResolveItself(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testInterfaceInheritanceResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testPrimaryExpressionToFunctionDefinition(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

//    @Test fun testOverloadedParameterCount() = doTest()

    @Test
    public void testClassUsageToOpaqueClassDefinitionShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    // Note: the constructor call must resolve to the class to properly handle the rename
    // Another mechanism will handle the goto behavior to allow a redirection to the constructor call
    @Test
    public void testClassConstructorToClassDefinition(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testConstructorCallShouldResolveClassDeclarationInTheSameFile(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testDeclarationInsideDeclarationBlock(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testDeclarationInsideDeclarationBlockUsageOutside(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testPublicImports(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testPublicImportLoopImportShouldNotLoopIndefinitely(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testPublicImportsWithAttributeSpecifier(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testPublicImportsInClassWithAttributeSpecifierShouldNotResolveOutsideClass(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testPublicImportSelectiveImportInfiniteRecursion(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTemplateUsageToTemplateDeclaration(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testGlobalVariableUsageToVariableDeclaration(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testAliasUsageToAliasDeclaration(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testAliasUsageToAliasDeclarationSecondAliasForm(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testAliasUsageToAliasDeclarationSecondAliasFormWithImport(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testAliasValueBeingFunction(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testAliasDeclarationWithAliasValueContainingTemplateInstanceDotIdentifier(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testAliasValueWithSameNameAsAliasDeclarationShouldNotResolveToAliasDeclaration(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testAliasValueWithSameNameAsAliasDeclarationShouldNotResolveToAliasDeclaration2(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testAliasValueWithSameNameAsAliasDeclarationShouldNotResolveToAliasDeclaration3(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testAliasValueElementCanResolveToAliasParameter(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testScopedImportsFail(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testScopedImportsPass(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testImportBindResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testImportWithSpacesAndComments(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testImportFromPackage(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testImportNamedBindResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testImportClassWithConstructor(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testImportClassWithoutConstructor(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testScopeOperatorDotXResolveGlobal(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testScopeOperatorDotXResolveGlobalWithImport(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testScopeOperatorXResolveLocal(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testScopeOperatorTypeResolveGlobal(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testScopeOperatorTypeInnerTypeResolveGlobalInner(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testGotoToLabeledStatementShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testContinueToLabeledStatementShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testForeachVariableUsageResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testForeachVariableMemberUsageShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testForeachVariableIteratedShouldNotResolveToVariable(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testForeachVariableOutOfScopeUsageShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testForLoopVariableUsageResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testForLoopVariableUsageInConditionResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testForLoopVariableOutOfScopeUsageShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testIfVariableUsageResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testIfValueOfConditionShouldNotResolveToIfVariable(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testIfVariableOutOfScopeUsageShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testStaticForeachVariableUsageResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testStaticForeachVariableIteratedShouldNotResolveToVariable(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testStaticForeachLoopVariableOutOfScopeUsageShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testStaticForeachDeclarationShouldSeeEachOther(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testConstructorCallResolveParameters(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testAnonymousEnumUsage(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testEnumUsageWithEnumTypePrefix(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testEnumUsageWithoutEnumTypePrefixShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testEnumUsageWithoutEnumTypePrefixShouldNotResolve2(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testEnumValueQualifiedAsTypeShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testNestedStructDefinitionReferenceFullyQualifiedShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testNestedStructDefinitionReferenceWithoutParentReferenceShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testNestedStructDefinitionReferenceFullyQualifiedShouldNotResolveATopLevelStruct(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testNestedStructMemberUsageShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testStructMemberAccessShouldNotMatchFunctionParameterOfTheStruct(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testLocalTypeDefinitionWithUsageAfterDefinitionShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testLocalTypeDefinitionWithUsageBeforeDefinitionShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testLocalVariableUsageDefinedInLocalConditionalStatement(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableInitializationAssignedValuePreviousInitializationSameVariableDeclarationShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableInitializationAssignedValueItselfShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testVariableInitializationAssignedValueItself2ShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testVariableInitializationAssignedValueItself3ShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testVariableInitializationAssignedValueNextInitializationSameVariableDeclarationShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testVariableUsageWithDeclarationInSameDeclarationBlockShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableUsageWithDeclarationInSameDeclarationBlockShouldResolve2(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableUsageWithDeclarationInSameDeclarationBlockShouldResolve3(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableUsageWithDeclarationInSameDeclarationBlockShouldResolve4(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableUsageWithNameShadowingTheTemplateName(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableUsageWithNameShadowingTheTemplateName2(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableInCaseStatement(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableInCaseStatementUsageOutsideCaseShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testVariableInCaseRangeStatement(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableInCaseDefaultStatement(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testVariableValueCanResolveToVariableTemplateParameter(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testWithStatementShouldResolveInnerElements(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testReferenceInVariableDeclarationMustNotResolveToVariableDeclarationItself(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testUsageOfShadowVariableShouldPointToShadowVariable(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTemplateTypeUsageInTemplatedClassShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTemplateTypeShouldNotLeakOutsideTemplate(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void testTemplateInstanceParameterUsageToOuterStructDefinitionShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTemplateThisTypeUsageInTemplatedClassShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTemplateTupleUsageInTemplatedClassShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTemplateTupleUsage2InTemplatedClassShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTemplateRecursiveCallShouldResolveToTemplateDeclaration(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTemplateRecursiveCallShouldResolveToTemplateDeclaration2(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testLambdaParameterUsageShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testLambdaMultiParametersUsageShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testStructMemberFunctionCall(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testStructMemberFunctionCall2(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFieldReferenceFromAutoVariableStruct(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testTypeOfFieldOfStructWithNestedAnonymousUnionWithNestedStruct(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFieldOfStructUsageWithTypeOfVarAliased(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFieldUsageOfAnonymousStructInAnotherStruct(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFieldUsageOfAnonymousUnionInAnotherStruct(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFunctionCallUFCS(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFunctionCallUFCSWithLocalReferenceSameName(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFunctionCallUFCSWithLocalImport(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFunctionCallUFCSWithLocalImportResolveLocalFunction(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFunctionCallUFCSWithLocalImportToAliasSymbol(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFunctionCallUFCSWithLocalSelectiveImportResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFunctionCallUFCSWithLocalSelectiveImportResolve2(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }

    @Test
    public void testFunctionCallUFCSWithoutImportShouldNotResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, false);
    }

    @Test
    public void functionDeclarationInSameConditionalDeclarationBlockAsUsageShouldResolve(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture);
    }
}
