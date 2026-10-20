import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Script to generate the psi implementations.
 * <p>
 * Java port of upstream's {@code scripts/types_regen_script.d} (kept in {@code legacy/scripts} for reference), so the
 * build does not need a D compiler. The tables and templates are upstream's; the only changes are the Consulo imports
 * and the nullability annotations ({@code @NotNull} is the default under {@code @NullMarked}).
 * <p>
 * Usage: {@code java language/src/psi-generator/TypesRegenScript.java <Interface|Implementation> <output source root>}
 */
public class TypesRegenScript {
    static final String defaultParentClassName = "ASTWrapperPsiElement";

    static final Map<String, List<String>> TYPES_CHILDREN = new HashMap<>();

    static final Map<String, List<String>> STUB_CHILDREN = new HashMap<>();

    static final Map<String, List<String>> TYPES_EXTRA_INTERFACES = new HashMap<>();

    /// If an entry appear here, then the element will inherit from it, otherwise it will inherit from ASTWrapperPsiElement
    static final Map<String, String> TYPES_MIXINS = new HashMap<>();

    /**
     * Contain the name of Psi elements that are named elements
     */
    static final List<String> NAMED_CHILDREN = List.of(
        "AliasInitializer",
        "AutoAssignment",
        "Catch",
        "ClassDeclaration",
        "Constructor",
        "DeclaratorIdentifier",
        "Destructor",
        "EnumDeclaration",
        "EnumMember",
        "ForeachType",
        "FunctionDeclaration",
        "IdentifierInitializer",
        "IfCondition",
        "InterfaceDeclaration",
        "LabeledStatement",
        "ModuleDeclaration",
        "NamedImportBind",
        "OutStatement",
        "Parameter",
        "SingleImport",
        "StructDeclaration",
        "TemplateDeclaration",
        "TemplateAliasParameter",
        "TemplateTupleParameter",
        "TemplateTypeParameter",
        "TemplateValueParameter",
        "UnionDeclaration",
        "VersionSpecification"
    );

    // TODO try to remove this array and generate all stub implementation
    static final List<String> GENERATE_STUB_IMPL = List.of(
        "SharedStaticConstructor",
        "SharedStaticDestructor",
        "StaticConstructor",
        "StaticDestructor",
        "TemplateMixinDeclaration"
    );

    static final List<String> IGNORE_IMPLEMENTATION_GENERATION = List.of(
        "BlockStatement"
    );

    /*
    Associative array which contains Name of psi element as key and if it has a processDeclaration as value
    */
    static final Map<String, Boolean> HAS_PROCESS_DECLARATION = new HashMap<>();

    static {
        TYPES_CHILDREN.put("AddExpression", List.of("Expression*", "OP_TILDA", "OP_PLUS" , "OP_MINUS"));
        TYPES_MIXINS.put("AddExpression", "DLanguageAddExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("AddExpression", List.of("Expression"));
        TYPES_CHILDREN.put("AliasAssign", List.of("Type","OP_EQ"));
        TYPES_EXTRA_INTERFACES.put("AliasAssign", List.of("Declaration"));
        TYPES_CHILDREN.put("AliasDeclaration", List.of("DeclaratorIdentifier*","OP_COMMA","StorageClass*","KW_ALIAS", "Type", "OP_SCOLON", "AliasInitializer*"));
        TYPES_EXTRA_INTERFACES.put("AliasDeclaration", List.of("Declaration"));
        TYPES_CHILDREN.put("AliasThisDeclaration", List.of("KW_ALIAS", "Identifier", "KW_THIS", "OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("AliasThisDeclaration", List.of("Declaration"));
        TYPES_CHILDREN.put("AlignAttribute", List.of("KW_ALIGN", "AssignExpression", "OP_PAR_RIGHT", "OP_PAR_LEFT"));
        TYPES_CHILDREN.put("AndAndExpression", List.of("Expression*", "OP_BOOL_AND"));
        TYPES_MIXINS.put("AndAndExpression", "DLanguageAndAndExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("AndAndExpression", List.of("Expression"));
        TYPES_CHILDREN.put("AndExpression", List.of("Expression*", "OP_AND"));
        TYPES_EXTRA_INTERFACES.put("AndExpression", List.of("BitwiseExpression"));
        TYPES_CHILDREN.put("AnonymousEnumDeclaration", List.of("Expression","OP_COLON","KW_ENUM","OP_PAR_LEFT","OP_PAR_RIGHT","Type","EnumMember*"));
        TYPES_EXTRA_INTERFACES.put("AnonymousEnumDeclaration", List.of("Declaration", "DTypedElement"));
        TYPES_MIXINS.put("AnonymousEnumDeclaration", "DLanguageAnonymousEnumDeclarationImplMixin");
        TYPES_CHILDREN.put("ArgumentList", List.of("Expression*", "OP_COMMA*"));
        TYPES_CHILDREN.put("ArrayAccessExpression", List.of("Expression", "IndexExpression"));
        TYPES_EXTRA_INTERFACES.put("ArrayAccessExpression", List.of("Expression"));
        TYPES_MIXINS.put("ArrayAccessExpression", "DLanguageArrayAccessExpressionImplMixin");
        TYPES_CHILDREN.put("ArrayInitializer", List.of("OP_BRACKET_LEFT", "OP_COMMA*", "ArrayMemberInitializer*", "OP_BRACKET_RIGHT"));
        TYPES_CHILDREN.put("ArrayLiteral", List.of("OP_BRACKET_LEFT", "OP_COMMA*", "Expression*", "OP_BRACKET_RIGHT"));
        TYPES_CHILDREN.put("ArrayLiteralExpression", List.of("ArrayLiteral", "AssocArrayLiteral"));
        TYPES_MIXINS.put("ArrayLiteralExpression", "DLanguageArrayLiteralExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("ArrayLiteralExpression", List.of("Expression"));
        TYPES_CHILDREN.put("ArrayMemberInitializer", List.of("Expression", "OP_COLON", "ArrayLiteral", "StructInitializer", "OP_BRACES_LEFT"));
        TYPES_CHILDREN.put("AsmAddExp", List.of("AsmAddExp", "AsmMulExp",  "OP_MINUS", "OP_PLUS"));
        TYPES_CHILDREN.put("AsmAndExp", List.of("AsmAndExp", "AsmEqualExp", "OP_AND"));
        TYPES_CHILDREN.put("AsmBrExp", List.of("AsmExp", "AsmUnaExp", "AsmBrExp", "OP_BRACKET_RIGHT", "OP_BRACKET_LEFT"));
        TYPES_CHILDREN.put("AsmEqualExp", List.of("AsmEqualExp", "AsmRelExp",  "OP_NOT_EQ", "OP_EQ_EQ"));
        TYPES_CHILDREN.put("AsmExp", List.of("AsmLogOrExp",  "AsmExp", "OP_QUEST", "OP_COLON"));
        TYPES_CHILDREN.put("AsmInstruction", List.of( "INTEGER_LITERAL","Identifier","AsmInstruction","Operands","KW_ALIGN", "OP_COLON", "OP_SCOLON"));
        TYPES_CHILDREN.put("AsmLogAndExp", List.of("AsmLogAndExp", "AsmOrExp", "OP_BOOL_AND"));
        TYPES_CHILDREN.put("AsmLogOrExp", List.of("AsmLogOrExp", "AsmLogAndExp", "OP_BOOL_OR"));
        TYPES_CHILDREN.put("AsmMulExp", List.of("AsmMulExp", "AsmBrExp","OP_MOD","OP_DIV","OP_ASTERISK"));
        TYPES_CHILDREN.put("AsmOrExp", List.of("AsmOrExp", "AsmXorExp", "OP_OR"));
        TYPES_CHILDREN.put("AsmPrimaryExp", List.of("FLOAT_LITERAL", "INTEGER_LITERAL", "Register",  "AsmExp", "IdentifierChain","OP_DOLLAR","KW_THIS"));
        TYPES_CHILDREN.put("AsmRelExp", List.of("AsmRelExp", "AsmShiftExp","OP_GT_EQ","OP_GT","OP_LESS","OP_LESS_EQ"));
        TYPES_CHILDREN.put("AsmShiftExp", List.of("AsmShiftExp", "AsmAddExp","OP_SH_LEFT","OP_SH_RIGHT","OP_USH_RIGHT"));
        TYPES_CHILDREN.put("AsmStatement", List.of("KW_ASM", "OP_BRACES_LEFT", "OP_BRACES_RIGHT", "AsmInstruction*", "GccAsmInstruction*"));
        TYPES_EXTRA_INTERFACES.put("AsmStatement", List.of("Statement"));
        TYPES_CHILDREN.put("AsmTypePrefix", List.of("Identifier*"));
        TYPES_CHILDREN.put("GccAsmClobberList", List.of("DOUBLE_QUOTED_STRING*", "OP_COMMA*"));
        TYPES_CHILDREN.put("GccAsmGotoLabels", List.of("Identifier*", "OP_COMMA*"));
        TYPES_CHILDREN.put("GccAsmInstruction", List.of("Expression", "GccAsmTemplate", "GccAsmOperandList*", "GccAsmClobberList", "GccAsmGotoLabels", "OP_COLON*", "OP_SCOLON"));
        TYPES_CHILDREN.put("GccAsmOperand", List.of("OP_BRACKET_LEFT", "Identifier", "OP_BRACKET_RIGHT", "DOUBLE_QUOTED_STRING", "OP_PAR_LEFT", "Expression", "OP_PAR_RIGHT"));
        TYPES_CHILDREN.put("GccAsmOperandList", List.of("GccAsmOperand*", "OP_COMMA*"));
        TYPES_CHILDREN.put("GccAsmTemplate", List.of("DOUBLE_QUOTED_STRING", "WYSIWYG_STRING", "ALTERNATE_WYSIWYG_STRING"));
        TYPES_MIXINS.put("GccAsmTemplate", "DLanguageGccAsmTemplateImplMixin");
        TYPES_CHILDREN.put("AsmUnaExp", List.of("AsmUnaExp","Identifier","AsmExp","OP_PLUS","OP_MINUS","OP_NOT","OP_TILDA","AsmPrimaryExp"));
        TYPES_CHILDREN.put("AsmXorExp", List.of(/*"AsmPrimaryExp", "AsmTypePrefix", "AsmExp",*/ "AsmXorExp", "AsmAndExp", "OP_XOR"));
        TYPES_CHILDREN.put("AssertArguments", List.of("AssignExpression*", "OP_COMMA"));
        TYPES_CHILDREN.put("AssertExpression", List.of("KW_ASSERT","OP_BRACES_LEFT","OP_BRACES_RIGHT" ,"Expression*", "OP_COMMA*"));
        TYPES_MIXINS.put("AssertExpression", "DLanguageAssertExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("AssertExpression", List.of("Expression"));
        TYPES_CHILDREN.put("AssignExpression", List.of("Expression*","OP_AND_EQ","OP_DIV_EQ","OP_EQ","OP_EQ_EQ","OP_GT_EQ","OP_LESS_EQ","OP_MINUS_EQ","OP_MOD_EQ","OP_MUL_EQ","OP_NOT_EQ","OP_NOT_GR_EQ","OP_NOT_LESS_EQ","OP_OR_EQ","OP_PLUS_EQ","OP_POW_EQ","OP_SH_LEFT_EQ","OP_SH_RIGHT_EQ","OP_TILDA_EQ","OP_USH_RIGHT_EQ","OP_XOR_EQ"));
        TYPES_MIXINS.put("AssignExpression", "DLanguageAssignExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("AssignExpression", List.of("Expression"));
        TYPES_CHILDREN.put("AssocArrayLiteral", List.of("KeyValuePairs","OP_BRACKET_RIGHT","OP_BRACKET_LEFT"));
        TYPES_CHILDREN.put("AtAttribute", List.of("OP_AT","OP_PAR_LEFT","OP_PAR_RIGHT","Identifier", "ArgumentList","FunctionCallExpression"));
        TYPES_CHILDREN.put("Attribute", List.of("PragmaExpression", "KW_SYNCHRONIZED","KW_ABSTRACT","KW_AUTO","KW_ENUM","KW_EXTERN","KW_FINAL","KW_INOUT","KW_NOTHROW","KW_THROW","KW_OVERRIDE","KW_PURE","KW_REF","KW___GSHARED","KW_SCOPE","KW_STATIC","KW_EXPORT", "KW_PRIVATE","KW_PROTECTED","KW_PUBLIC","AlignAttribute","Deprecated","AtAttribute","KW_PACKAGE","IdentifierChain","LinkageAttribute","KW_CONST"));
        TYPES_CHILDREN.put("AttributeSpecifier", List.of("OP_COLON","Attribute","DeclarationBlock"));
        TYPES_EXTRA_INTERFACES.put("AttributeSpecifier", List.of("Declaration"));
        TYPES_CHILDREN.put("AutoDeclaration", List.of("StorageClass*","OP_COMMA*","OP_SCOLON","AutoAssignment*"));
        TYPES_EXTRA_INTERFACES.put("AutoDeclaration", List.of("VariableDeclaration"));
        TYPES_CHILDREN.put("BlockStatement", List.of("Statement*","OP_BRACES_RIGHT","OP_BRACES_LEFT"));
        TYPES_EXTRA_INTERFACES.put("BlockStatement", List.of("Statement"));
        TYPES_CHILDREN.put("BreakStatement", List.of("KW_BREAK","Identifier","OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("BreakStatement", List.of("Statement", "LabelQualifier"));
        TYPES_MIXINS.put("BreakStatement", "DLanguageBreakStatementImplMixin");
        TYPES_CHILDREN.put("BaseClassList", List.of("BasicType*","OP_COMMA*"));
        TYPES_CHILDREN.put("BasicType", List.of("Type","TypeofExpression","MixinType","Expression*","Vector","BuiltinType","QualifiedIdentifier","OP_DOT","OP_PAR_RIGHT","OP_PAR_LEFT","KW_SUPER","KW_THIS","KW_CONST","KW_IMMUTABLE","KW_INOUT","KW_SHARED"));
        TYPES_EXTRA_INTERFACES.put("BasicType", List.of("DTypedElement"));
        TYPES_MIXINS.put("BasicType", "DLanguageBasicTypeImplMixin");
        TYPES_CHILDREN.put("BuiltinType", List.of(/*todo add the types*/));  // UNUSED currently
        TYPES_CHILDREN.put("CaseRangeStatement", List.of("KW_CASE*","OP_DDOT","OP_COLON*","AssignExpression", "Statement*"));
        TYPES_EXTRA_INTERFACES.put("CaseRangeStatement", List.of("Statement"));
        TYPES_CHILDREN.put("CaseStatement", List.of("KW_CASE","OP_COLON","ArgumentList","Statement*"));
        TYPES_EXTRA_INTERFACES.put("CaseRangeStatement", List.of("Statement"));
        TYPES_CHILDREN.put("CastExpression", List.of("CastQualifier", "Type","Expression","KW_CAST","OP_PAR_LEFT","OP_PAR_RIGHT"));
        TYPES_EXTRA_INTERFACES.put("CastExpression", List.of("Expression"));
        TYPES_MIXINS.put("CastExpression", "DLanguageCastExpressionImplMixin");
        TYPES_CHILDREN.put("CastQualifier", List.of("KW_IMMUTABLE","KW_CONST","KW_SHARED","KW_INOUT"));
        TYPES_CHILDREN.put("Catches", List.of("LastCatch","Catch"));
        TYPES_CHILDREN.put("CommaExpression", List.of("Expression*", "OP_COMMA"));
        TYPES_MIXINS.put("CommaExpression", "DLanguageCommaExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("CommaExpression", List.of("Expression"));
        TYPES_CHILDREN.put("CompileCondition", List.of( "VersionCondition",  "DebugCondition", "StaticIfCondition"));
        TYPES_CHILDREN.put("ConditionalDeclaration", List.of("CompileCondition", "Declaration*","OP_COLON","KW_ELSE","DeclarationBlock*"));
        TYPES_EXTRA_INTERFACES.put("ConditionalDeclaration", List.of("Declaration"));
        TYPES_CHILDREN.put("ConditionalStatement", List.of("CompileCondition", "Statement*", "KW_ELSE","OP_BRACES_RIGHT","OP_BRACES_LEFT"));
        TYPES_EXTRA_INTERFACES.put("ConditionalStatement", List.of("Statement"));
        TYPES_CHILDREN.put("Constraint", List.of("KW_IF","Expression*","OP_COMMA*", "OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_CHILDREN.put("ContinueStatement", List.of("KW_CONTINUE", "Identifier", "OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("ContinueStatement", List.of("Statement", "LabelQualifier"));
        TYPES_MIXINS.put("ContinueStatement", "DLanguageContinueStatementImplMixin");
        TYPES_CHILDREN.put("DebugCondition", List.of("KW_DEBUG","INTEGER_LITERAL", "Identifier", "OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_CHILDREN.put("DebugSpecification", List.of("KW_DEBUG","OP_EQ","Identifier", "INTEGER_LITERAL", "OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("DebugSpecification", List.of("Declaration"));
        TYPES_CHILDREN.put("DeclarationBlock", List.of("OP_BRACES_LEFT", "Declaration*", "OP_BRACES_RIGHT"));
        TYPES_CHILDREN.put("DeclarationStatement", List.of("Declaration"));
        TYPES_EXTRA_INTERFACES.put("DeclarationStatement", List.of("Statement"));
        TYPES_CHILDREN.put("DefaultStatement", List.of("KW_DEFAULT","OP_COLON","Statement*"));
        TYPES_EXTRA_INTERFACES.put("DefaultStatement", List.of("Statement"));
        TYPES_CHILDREN.put("DeleteExpression", List.of("KW_DELETE","Expression"));
        TYPES_MIXINS.put("DeleteExpression", "DLanguageDeleteExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("DeleteExpression", List.of("Expression"));
        TYPES_CHILDREN.put("DeleteStatement", List.of("KW_DELETE"));
        TYPES_CHILDREN.put("Deprecated", List.of("AssignExpression","Deprecated","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_CHILDREN.put("DollarExpression", List.of("OP_DOLLAR"));
        TYPES_MIXINS.put("DollarExpression", "DLanguageDollarExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("DollarExpression", List.of("Expression"));
        TYPES_CHILDREN.put("DoStatement", List.of("KW_DO","KW_WHILE","Statement","Expression*","OP_COMMA*","OP_SCOLON","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_EXTRA_INTERFACES.put("DoStatement", List.of("Statement"));
        TYPES_CHILDREN.put("EmptyDeclaration", List.of("OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("EmptyDeclaration", List.of("Declaration"));
        TYPES_CHILDREN.put("EmptyStatement", List.of("OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("EmptyStatement", List.of("Statement"));
        TYPES_CHILDREN.put("EnumBody", List.of( "Identifier","OP_BRACES_RIGHT", "OP_BRACES_LEFT", "EnumMember*","OP_COMMA*","OP_SCOLON"));
        TYPES_CHILDREN.put("EnumMemberAttribute", List.of("Attribute","Deprecated"));
        TYPES_CHILDREN.put("EqualExpression", List.of("Expression*","OP_EQ_EQ","OP_NOT_EQ"));
        TYPES_MIXINS.put("EqualExpression", "DLanguageEqualExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("EqualExpression", List.of("Expression"));
        TYPES_CHILDREN.put("ExpressionStatement", List.of("Expression*","OP_COMMA*","OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("ExpressionStatement", List.of("Statement"));
        TYPES_CHILDREN.put("Finally", List.of("KW_FINALLY", "Statement"));
        TYPES_CHILDREN.put("FinalSwitchStatement", List.of("KW_FINAL", "SwitchStatement"));
        TYPES_EXTRA_INTERFACES.put("FinalSwitchStatement", List.of("Statement"));
        TYPES_CHILDREN.put("FundamentalTypeConstructExpression", List.of("BasicType", "OP_PAR_LEFT", "NamedArgumentList", "OP_PAR_RIGHT"));
        TYPES_MIXINS.put("FundamentalTypeConstructExpression", "DLanguageFundamentalTypeConstructExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("FundamentalTypeConstructExpression", List.of("Expression"));
        TYPES_CHILDREN.put("FundamentalTypePropertyExpression", List.of("BasicType", "OP_DOT", "Identifier"));
        TYPES_MIXINS.put("FundamentalTypePropertyExpression", "DLanguageFundamentalTypePropertyExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("FundamentalTypePropertyExpression", List.of("Expression"));
        TYPES_CHILDREN.put("ForStatement", List.of("Statement*", "AssignExpression*","OP_BRACES_RIGHT","OP_BRACES_LEFT","KW_FOR","OP_SCOLON*","OP_PAR_LEFT", "OP_PAR_RIGHT"));
        TYPES_EXTRA_INTERFACES.put("ForStatement", List.of("Statement"));
        TYPES_CHILDREN.put("ForeachStatement", List.of("KW_STATIC","KW_FOREACH","KW_FOREACH_REVERSE","Statement","Expression*","OP_BRACES_RIGHT","OP_BRACES_LEFT","OP_DDOT","ForeachTypeList","OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("ForeachStatement", List.of("Statement", "Foreach"));
        TYPES_CHILDREN.put("ForeachTypeList", List.of("ForeachType*","OP_COMMA*"));
        TYPES_CHILDREN.put("FunctionAttribute", List.of("AtAttribute","KW_PURE","KW_NOTHROW"));
        TYPES_CHILDREN.put("FunctionCallExpression", List.of("Expression", "OP_PAR_LEFT", "NamedArgumentList", "OP_PAR_RIGHT"));
        TYPES_MIXINS.put("FunctionCallExpression", "DLanguageFunctionCallExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("FunctionCallExpression", List.of("Statement", "Expression"));
        TYPES_CHILDREN.put("FunctionContract", List.of("OP_BRACES_LEFT", "InOutStatement", "OP_PAR_LEFT", "InOutContractExpression"));
        TYPES_CHILDREN.put("FunctionLiteralExpression", List.of("Type","KW_FUNCTION","KW_DELEGATE","KW_REF","Parameters","FunctionAttribute*","SpecifiedFunctionBody"));
        TYPES_MIXINS.put("FunctionLiteralExpression", "DLanguageFunctionLiteralExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("FunctionLiteralExpression", List.of("Expression"));
        TYPES_CHILDREN.put("GotoStatement", List.of("Identifier","AssignExpression*","OP_COMMA*","KW_DEFAULT","KW_CASE","KW_GOTO","OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("GotoStatement", List.of("Statement", "LabelQualifier"));
        TYPES_MIXINS.put("GotoStatement", "DLanguageGotoStatementImplMixin");
        TYPES_CHILDREN.put("IdentifierChain", List.of("Identifier", "OP_DOT", "IdentifierChain"));
        TYPES_MIXINS.put("IdentifierChain", "DLanguageIdentifierChainImplMixin");
        TYPES_CHILDREN.put("IdentityExpression", List.of("Expression*","KW_IS","OP_NOT"));
        TYPES_MIXINS.put("IdentityExpression", "DLanguageIdentityExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("IdentityExpression", List.of("Expression"));
        TYPES_CHILDREN.put("IESString", List.of());
        STUB_CHILDREN.put("IfCondition", List.of("Identifier",  "AssignExpression*","OP_COMMA*", "KW_AUTO", "KW_SCOPE","Type", "OP_EQ"));
        TYPES_CHILDREN.put("IfStatement", List.of("Statement","KW_ELSE","KW_IF","OP_PAR_LEFT","OP_PAR_RIGHT","IfCondition"));
        TYPES_EXTRA_INTERFACES.put("IfStatement", List.of("Statement"));
        TYPES_CHILDREN.put("ImportBind", List.of("Identifier","NamedImportBind"));
        TYPES_MIXINS.put("ImportBind", "DLanguageImportBindImplMixin");
        TYPES_CHILDREN.put("ImportBindings", List.of("OP_COMMA*","ImportBind*","SingleImport","OP_COLON"));
        STUB_CHILDREN.put("ImportDeclaration", List.of("KW_STATIC","KW_IMPORT","SingleImport*","ImportBindings","OP_COMMA*","OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("ImportDeclaration", List.of("Statement", "Declaration", "DVisibilityModifier"));
        TYPES_CHILDREN.put("ImportExpression", List.of("ImportExpression","AssignExpression","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_MIXINS.put("ImportExpression", "DLanguageImportExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("ImportExpression", List.of("Expression"));
        TYPES_CHILDREN.put("IndexExpression", List.of("OP_BRACKET_LEFT","OP_BRACKET_RIGHT","Expression*", "OP_COMMA*", "OP_COLON*", "OP_DDOT*")); // contrary to what his name suggest, it is not an expression
        TYPES_CHILDREN.put("InExpression", List.of("ShiftExpression*","KW_IN","OP_NOT"));
        TYPES_CHILDREN.put("Initializer", List.of("KW_VOID","Expression", "ArrayInitializer", "StructInitializer"));
        TYPES_CHILDREN.put("InContractExpression", List.of("KW_IN","OP_PAR_LEFT", "AssertArguments", "OP_PAR_RIGHT"));
        TYPES_CHILDREN.put("InOutContractExpression", List.of("InContractExpression", "OutContractExpression"));
        TYPES_CHILDREN.put("InOutStatement", List.of("InStatement", "OutStatement"));
        TYPES_CHILDREN.put("InStatement", List.of("KW_IN","BlockStatement"));
        TYPES_CHILDREN.put("Invariant", List.of("BlockStatement","KW_INVARIANT","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_EXTRA_INTERFACES.put("Invariant", List.of("Declaration"));
        TYPES_CHILDREN.put("IsExpression", List.of("OP_PAR_RIGHT","OP_PAR_LEFT","Type","Identifier","TypeSpecialization","TemplateParameterList","OP_COMMA","OP_COLON","OP_EQ","KW_IS"));
        TYPES_MIXINS.put("IsExpression", "DLanguageIsExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("IsExpression", List.of("Expression"));
        TYPES_CHILDREN.put("KeyValuePair", List.of("Expression*","OP_COLON"));
        TYPES_CHILDREN.put("KeyValuePairs", List.of("OP_COMMA*","KeyValuePair*"));
        STUB_CHILDREN.put("LabeledStatement", List.of("Identifier", "OP_COLON","Statement"));
        TYPES_EXTRA_INTERFACES.put("LabeledStatement", List.of("Statement"));
        TYPES_CHILDREN.put("LambdaExpression", List.of("Identifier","KW_FUNCTION","KW_DELEGATE","OP_LAMBDA_ARROW","Expression","Parameters","FunctionAttribute*"));
        TYPES_MIXINS.put("LambdaExpression", "DLanguageLambdaExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("LambdaExpression", List.of("Expression"));
        TYPES_CHILDREN.put("LastCatch", List.of("KW_CATCH","Statement","DebugSpecification"));
        TYPES_CHILDREN.put("LinkageAttribute", List.of("IdentifierChain","Identifier","OP_PAR_RIGHT","OP_PAR_LEFT","OP_PLUS_PLUS","KW_EXTERN","OP_COMMA"));
        TYPES_CHILDREN.put("LiteralExpression", List.of("KW_THIS", "KW_SUPER", "KW_NULL", "KW_TRUE", "KW_FALSE", "INTEGER_LITERAL", "FLOAT_LITERAL", "CHARACTER_LITERAL", "ALTERNATE_WYSIWYG_STRING*", "DOUBLE_QUOTED_STRING*", "WYSIWYG_STRING*", "DELIMITED_STRING*", "TOKEN_STRING*", "HEX_STRING*"));
        TYPES_EXTRA_INTERFACES.put("LiteralExpression", List.of("Expression"));
        TYPES_MIXINS.put("LiteralExpression", "DLanguageLiteralExpressionImplMixin");
        TYPES_CHILDREN.put("MagicLiteralExpression", List.of("KW___FILE__", "KW___FILE_FULL_PATH__", "KW___MODULE__", "KW___LINE__", "KW___FUNCTION__", "KW___PRETTY_FUNCTION__"));
        TYPES_MIXINS.put("MagicLiteralExpression", "DLanguageMagicLiteralExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("MagicLiteralExpression", List.of("Expression"));
        TYPES_CHILDREN.put("MemberFunctionAttribute", List.of( "FunctionAttribute","KW_IMMUTABLE","KW_INOUT","KW_SHARED","KW_CONST"));
        TYPES_CHILDREN.put("MissingFunctionBody", List.of("FunctionContract*","OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("MissingFunctionBody", List.of("FunctionBody"));
        TYPES_CHILDREN.put("MixinDeclaration", List.of("KW_MIXIN", "OP_PAR_LEFT", "ArgumentList", "OP_PAR_RIGHT", "OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("MixinDeclaration", List.of("Declaration"));
        TYPES_CHILDREN.put("MixinQualifiedIdentifier", List.of("Identifier", "TemplateInstance", "OP_DOT", "MixinQualifiedIdentifier"));
        TYPES_CHILDREN.put("MixinTemplateName", List.of("TypeofExpression", "MixinQualifiedIdentifier", "OP_DOT"));
        TYPES_CHILDREN.put("MixinExpression", List.of("ArgumentList","OP_PAR_RIGHT","OP_PAR_LEFT","KW_MIXIN"));
        TYPES_MIXINS.put("MixinExpression", "DLanguageMixinExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("MixinExpression", List.of("Expression"));
        TYPES_CHILDREN.put("MixinType", List.of("ArgumentList","OP_PAR_RIGHT","OP_PAR_LEFT","KW_MIXIN"));
        TYPES_CHILDREN.put("MulExpression", List.of("Expression*","OP_MOD","OP_DIV","OP_ASTERISK"));
        TYPES_MIXINS.put("MulExpression", "DLanguageMulExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("MulExpression", List.of("Expression"));
        TYPES_CHILDREN.put("NamedArgument", List.of("Identifier", "OP_COLON", "Expression", "OP_COMMA"));
        TYPES_CHILDREN.put("NamedArgumentList", List.of("NamedArgument*", "OP_COMMA*"));
        STUB_CHILDREN.put("NamedImportBind", List.of("Identifier",  "OP_EQ"));
        TYPES_CHILDREN.put("NamespaceList", List.of("OP_COMMA",  "Expression*"));
        TYPES_CHILDREN.put("NewAnonClassExpression", List.of("KW_NEW", "KW_CLASS", "OP_PAR_LEFT", "NamedArgumentList", "OP_PAR_RIGHT", "BaseClassList", "StructBody"));
        TYPES_CHILDREN.put("NewExpression", List.of("KW_NEW", "PlacementNew", "NewAnonClassExpression", "Type", "OP_PAR_LEFT", "NamedArgumentList", "OP_PAR_RIGHT", "OP_BRACKET_LEFT", "Expression","OP_BRACKET_RIGHT"));
        TYPES_MIXINS.put("NewExpression", "DLanguageNewExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("NewExpression", List.of("Expression"));
        TYPES_CHILDREN.put("Operands", List.of("OP_COMMA","Operands","AsmExp"));
        TYPES_CHILDREN.put("OrExpression", List.of("Expression*", "OP_OR"));
        TYPES_EXTRA_INTERFACES.put("OrExpression", List.of("BitwiseExpression"));
        TYPES_CHILDREN.put("OrOrExpression", List.of("Expression*", "OP_BOOL_OR"));
        TYPES_MIXINS.put("OrOrExpression", "DLanguageOrOrExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("OrOrExpression", List.of("Expression"));
        TYPES_CHILDREN.put("OutContractExpression", List.of("KW_OUT", "OP_PAR_LEFT", "OP_SCOLON", "AssertArguments", "OP_PAR_RIGHT"));
        TYPES_CHILDREN.put("Parameters", List.of("OP_COMMA*","OP_TRIPLEDOT","Parameter*","OP_PAR_LEFT","OP_PAR_RIGHT"));
        TYPES_CHILDREN.put("ParameterAttribute", List.of("KW_FINAL","KW_IN","KW_LAZY","KW_OUT","KW_REF","KW_SCOPE","KW_AUTO","KW_CONST","KW_IMMUTABLE","KW_INOUT","KW_SHARED","AtAttribute"));
        TYPES_CHILDREN.put("ParenthesisedExpression", List.of("OP_PAR_LEFT", "Expression", "OP_PAR_RIGHT"));
        TYPES_MIXINS.put("ParenthesisedExpression", "DLanguageParenthesisedExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("ParenthesisedExpression", List.of("Expression"));
        TYPES_CHILDREN.put("PlacementNew", List.of("OP_PAR_LEFT*","Expression","OP_PAR_RIGHT"));
        TYPES_CHILDREN.put("Postblit", List.of("FunctionBody","OP_SCOLON","KW_THIS*","OP_PAR_LEFT","OP_PAR_RIGHT","MemberFunctionAttribute"));
        TYPES_EXTRA_INTERFACES.put("Postblit", List.of("Declaration"));
        TYPES_CHILDREN.put("PostfixExpression", List.of("Expression", "OP_PLUS_PLUS", "OP_MINUS_MINUS", "OP_PAR_LEFT", "NamedArgumentList", "OP_PAR_RIGHT"));
        TYPES_MIXINS.put("PostfixExpression", "DLanguagePostfixExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("PostfixExpression", List.of("Expression"));
        TYPES_CHILDREN.put("PowExpression", List.of("Expression*","OP_POW"));
        TYPES_MIXINS.put("PowExpression", "DLanguagePowExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("PowExpression", List.of("Expression"));
        TYPES_CHILDREN.put("PragmaExpression", List.of("Identifier","ArgumentList","OP_PAR_LEFT","OP_PAR_RIGHT","OP_COMMA","KW_PRAGMA")); // Note contrary to his name suggest, it is not an exression
        TYPES_CHILDREN.put("PragmaStatement", List.of("PragmaExpression", "OP_SCOLON","Statement"));
        TYPES_CHILDREN.put("QualifiedIdentifier", List.of("Identifier", "TemplateInstance", "OP_BRACKET_LEFT", "OP_BRACKET_RIGHT", "Expression", "OP_DOT", "QualifiedIdentifier"));
        TYPES_MIXINS.put("QualifiedIdentifier", "DLanguageQualifiedIdentifierImplMixin");
        TYPES_CHILDREN.put("ReferenceExpression", List.of("OP_DOT", "Identifier", "TemplateInstance", "Expression"));
        TYPES_EXTRA_INTERFACES.put("ReferenceExpression", List.of("Expression"));
        TYPES_MIXINS.put("ReferenceExpression", "DLanguageReferenceExpressionImplMixin");
        TYPES_CHILDREN.put("Register", List.of("Identifier","INTEGER_LITERAL", "OP_PAR_RIGHT", "OP_PAR_LEFT"));
        TYPES_CHILDREN.put("RelExpression", List.of("Expression*", "OP_GT","OP_GT_EQ","OP_LESS","OP_LESS_EQ","OP_NOT_GR","OP_NOT_GR_EQ","OP_NOT_LESS","OP_NOT_LESS_EQ"));
        TYPES_MIXINS.put("RelExpression", "DLanguageRelExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("RelExpression", List.of("Expression"));
        TYPES_CHILDREN.put("ReturnStatement", List.of("KW_RETURN","Expression*","OP_COMMA*","OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("ReturnStatement", List.of("Statement"));
        TYPES_CHILDREN.put("ScopeGuardStatement", List.of("KW_SCOPE","Identifier","Statement","DebugSpecification", "OP_PAR_LEFT","OP_PAR_RIGHT"));
        TYPES_EXTRA_INTERFACES.put("ScopeGuardStatement", List.of("Statement"));
        STUB_CHILDREN.put("SharedStaticConstructor", List.of("KW_STATIC","KW_SHARED","KW_THIS","OP_PAR_LEFT","OP_PAR_RIGHT","FunctionBody"));
        TYPES_EXTRA_INTERFACES.put("SharedStaticConstructor", List.of("Declaration"));
        STUB_CHILDREN.put("SharedStaticDestructor", List.of("OP_TILDA","KW_STATIC","KW_SHARED","KW_THIS","OP_PAR_LEFT","OP_PAR_RIGHT","FunctionBody"));
        TYPES_EXTRA_INTERFACES.put("SharedStaticDestructor", List.of("Declaration"));
        TYPES_CHILDREN.put("ShiftExpression", List.of("Expression*","OP_SH_RIGHT","OP_SH_LEFT","OP_USH_RIGHT"));
        TYPES_MIXINS.put("ShiftExpression", "DLanguageShiftExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("ShiftExpression", List.of("Expression"));
        TYPES_CHILDREN.put("ShortenedFunctionBody", List.of("FunctionContract*", "OP_LAMBDA_ARROW", "AssignExpression", "OP_SCOLON"));
        TYPES_EXTRA_INTERFACES.put("ShortenedFunctionBody", List.of("FunctionBody"));
        TYPES_CHILDREN.put("SpecifiedFunctionBody", List.of("FunctionContract*", "KW_DO", "BlockStatement"));
        TYPES_EXTRA_INTERFACES.put("SpecifiedFunctionBody", List.of("FunctionBody"));
        TYPES_CHILDREN.put("SpecifiedVariableDeclaration", List.of("OP_SCOLON", "BasicType", "TypeSuffix*", "IdentifierInitializer*","OP_COMMA*","OP_EQ","FunctionBody","StorageClass*"));
        TYPES_EXTRA_INTERFACES.put("SpecifiedVariableDeclaration", List.of("VariableDeclaration"));
        TYPES_CHILDREN.put("StaticAssertDeclaration", List.of("StaticAssertStatement"));
        TYPES_EXTRA_INTERFACES.put("StaticAssertDeclaration", List.of("Declaration"));
        TYPES_CHILDREN.put("StaticAssertStatement", List.of("KW_STATIC","AssertExpression","OP_SCOLON"));
        TYPES_CHILDREN.put("StaticCtorDtorCommon", List.of("FunctionBody"));
        STUB_CHILDREN.put("StaticConstructor", List.of("KW_STATIC","KW_THIS","OP_PAR_LEFT","OP_PAR_RIGHT","FunctionBody"));
        TYPES_EXTRA_INTERFACES.put("StaticConstructor", List.of("Declaration"));
        STUB_CHILDREN.put("StaticDestructor", List.of("OP_TILDA","KW_STATIC","KW_THIS","OP_PAR_LEFT","OP_PAR_RIGHT","FunctionBody"));
        TYPES_EXTRA_INTERFACES.put("StaticDestructor", List.of("Declaration"));
        TYPES_CHILDREN.put("StaticForeachDeclaration", List.of("KW_FOREACH", "KW_FOREACH_REVERSE", "Declaration*", "Expression*", "OP_BRACES_RIGHT", "OP_BRACES_LEFT", "OP_DDOT", "ForeachType", "ForeachTypeList", "OP_SCOLON", "KW_STATIC"));
        TYPES_EXTRA_INTERFACES.put("StaticForeachDeclaration", List.of("Declaration", "Foreach"));
        TYPES_CHILDREN.put("StaticIfCondition", List.of("StaticIfCondition","Expression","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_CHILDREN.put("StorageClass", List.of("AtAttribute","Deprecated","AlignAttribute","LinkageAttribute","KW_SYNCHRONIZED","KW_ABSTRACT","KW_CONST","KW_IMMUTABLE","KW_AUTO","KW_ENUM","KW_EXTERN","KW_FINAL","KW_INOUT","KW_NOTHROW","KW_OVERRIDE","KW_PURE","KW_REF","KW___GSHARED","KW_SCOPE","KW_STATIC","KW_THROW"));
        TYPES_CHILDREN.put("StructBody", List.of("OP_BRACES_RIGHT","OP_BRACES_LEFT","Declaration*"));
        TYPES_CHILDREN.put("StructInitializer", List.of("OP_BRACES_LEFT", "StructMemberInitializer*", "OP_COMMA*", "OP_BRACES_RIGHT"));
        TYPES_CHILDREN.put("StructMemberInitializer", List.of("Identifier","OP_COLON","Expression", "ArrayLiteral", "StructInitializer"));
        TYPES_CHILDREN.put("SwitchStatement", List.of("KW_SWITCH","OP_PAR_RIGHT","OP_PAR_LEFT","AssignExpression*","OP_COMMA*","Statement","DebugSpecification"));
        TYPES_EXTRA_INTERFACES.put("SwitchStatement", List.of("Statement"));
        TYPES_CHILDREN.put("SynchronizedStatement", List.of("OP_PAR_RIGHT","OP_PAR_LEFT","AssignExpression*","OP_COMMA*","Statement","DebugSpecification","KW_SYNCHRONIZED"));
        TYPES_EXTRA_INTERFACES.put("SynchronizedStatement", List.of("Statement"));
        STUB_CHILDREN.put("TemplateAliasParameter", List.of("KW_ALIAS","Identifier","Type*","AssignExpression*","OP_COLON","OP_EQ"));
        TYPES_EXTRA_INTERFACES.put("TemplateAliasParameter", List.of("TemplateParameter"));
        TYPES_CHILDREN.put("TemplateArgument", List.of("Type","AssignExpression"));
        TYPES_CHILDREN.put("TemplateArgumentList", List.of("OP_COMMA*","TemplateArgument*"));
        TYPES_CHILDREN.put("TemplateArguments", List.of("TemplateArgumentList","TemplateSingleArgument","OP_PAR_RIGHT","OP_PAR_LEFT","OP_NOT"));
        TYPES_CHILDREN.put("TemplateInstance", List.of("Identifier", "TemplateArguments"));
        TYPES_CHILDREN.put("TemplateMixin", List.of("KW_MIXIN","MixinTemplateName","TemplateArguments","Identifier"));
        TYPES_EXTRA_INTERFACES.put("TemplateMixin", List.of("Declaration"));
        STUB_CHILDREN.put("TemplateMixinDeclaration", List.of("KW_MIXIN","KW_TEMPLATE", "Identifier", "TemplateParameters", "KW_IF", "OP_BRACES_LEFT", "Declaration*", "OP_BRACES_RIGHT"));
        TYPES_EXTRA_INTERFACES.put("TemplateMixinDeclaration", List.of("Declaration"));
        TYPES_CHILDREN.put("TemplateParameterList", List.of("TemplateParameter*","OP_COMMA*"));
        TYPES_CHILDREN.put("TemplateParameters", List.of("TemplateParameterList","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_CHILDREN.put("TemplateSingleArgument", List.of("Identifier","BuiltinType","KW_SUPER","KW_THIS","OP_DOLLAR","KW_TRUE","KW_FALSE","KW_NULL",
                                                    "KW___DATE__","KW___EOF__","KW___FILE__","KW___FILE_FULL_PATH__","KW___FUNCTION__","KW___GSHARED","KW___LINE__","KW___MODULE__","KW___PARAMETERS","KW___PRETTY_FUNCTION__","KW___TIME__","KW___TIMESTAMP__","KW___TRAITS","KW___VECTOR","KW___VENDOR__","KW___VERSION__",
                                                    "INTEGER_LITERAL","FLOAT_LITERAL","DOUBLE_QUOTED_STRING","CHARACTER_LITERAL"));
        TYPES_MIXINS.put("TemplateSingleArgument", "DLanguageTemplateSingleArgumentImplMixin");
        TYPES_CHILDREN.put("TemplateThisParameter", List.of("KW_THIS","TemplateTypeParameter"));
        TYPES_MIXINS.put("TemplateThisParameter", "DLanguageTemplateThisParameterImplMixin");
        TYPES_EXTRA_INTERFACES.put("TemplateThisParameter", List.of("TemplateParameter"));
        STUB_CHILDREN.put("TemplateTupleParameter", List.of("Identifier","OP_TRIPLEDOT"));
        TYPES_EXTRA_INTERFACES.put("TemplateTupleParameter", List.of("TemplateParameter"));
        STUB_CHILDREN.put("TemplateTypeParameter", List.of("Identifier","Type*","OP_COLON","OP_EQ"));
        TYPES_EXTRA_INTERFACES.put("TemplateTypeParameter", List.of("TemplateParameter"));
        STUB_CHILDREN.put("TemplateValueParameter", List.of("Type","Identifier","OP_COLON","AssignExpression",  "TemplateValueParameterDefault"));
        TYPES_EXTRA_INTERFACES.put("TemplateValueParameter", List.of("TemplateParameter"));
        TYPES_CHILDREN.put("TemplateValueParameterDefault", List.of("OP_EQ","Expression"));
        TYPES_CHILDREN.put("TernaryExpression", List.of("OP_QUEST","OP_COLON","Expression*"));
        TYPES_MIXINS.put("TernaryExpression", "DLanguageTernaryExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("TernaryExpression", List.of("Expression"));
        TYPES_CHILDREN.put("ThrowExpression", List.of("KW_THROW","Expression","OP_SCOLON"));
        TYPES_MIXINS.put("ThrowExpression", "DLanguageThrowExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("ThrowExpression", List.of("Expression"));
        TYPES_CHILDREN.put("TraitsExpression", List.of("KW___TRAITS","TemplateArgumentList","Identifier","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_MIXINS.put("TraitsExpression", "DLanguageTraitsExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("TraitsExpression", List.of("Expression"));
        TYPES_CHILDREN.put("TryStatement", List.of("KW_TRY","Statement","Catches","Finally"));
        TYPES_EXTRA_INTERFACES.put("TryStatement", List.of("Statement"));
        TYPES_CHILDREN.put("Type", List.of("KW_CONST","KW_IMMUTABLE","KW_INOUT","KW_SHARED","BasicType","TypeSuffix*"));
        TYPES_EXTRA_INTERFACES.put("Type", List.of("DTypedElement"));
        TYPES_MIXINS.put("Type", "DLanguageTypeImplMixin");
        TYPES_CHILDREN.put("TypeConstructExpression", List.of("KW_IMMUTABLE", "KW_CONST", "KW_INOUT", "KW_SHARED", "Type", "OP_PAR_LEFT", "ArgumentList", "OP_PAR_RIGHT"));
        TYPES_MIXINS.put("TypeConstructExpression", "DLanguageTypeConstructExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("TypeConstructExpression", List.of("Expression"));
        TYPES_CHILDREN.put("TypePropertyExpression", List.of("KW_IMMUTABLE", "KW_CONST", "KW_INOUT", "KW_SHARED", "Type", "OP_DOT", "Identifier"));
        TYPES_CHILDREN.put("TypeidExpression", List.of("Expression","KW_TYPEID","Type","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_MIXINS.put("TypeidExpression", "DLanguageTypeidExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("TypeidExpression", List.of("Expression"));
        TYPES_CHILDREN.put("TypeofExpression", List.of("Expression","KW_TYPEOF","OP_PAR_RIGHT","OP_PAR_LEFT","KW_RETURN"));
        TYPES_MIXINS.put("TypeofExpression", "DLanguageTypeofExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("TypeofExpression", List.of("Expression"));
        TYPES_CHILDREN.put("TypeSpecialization", List.of("Type","KW___PARAMETERS","KW_STRUCT","KW_UNION","KW_CLASS","KW_INTERFACE","KW_ENUM","KW___VECTOR","KW_FUNCTION","KW_DELEGATE","KW_SUPER","KW_CONST","KW_IMMUTABLE","KW_INOUT","KW_SHARED","KW_RETURN","KW_MODULE","KW_PACKAGE"));
        TYPES_CHILDREN.put("TypeSuffix", List.of("Expression*","OP_TRIPLEDOT","KW_FUNCTION","KW_DELEGATE","OP_ASTERISK","MemberFunctionAttribute*","Parameters","Type","OP_BRACKET_LEFT","OP_BRACKET_RIGHT"));
        TYPES_CHILDREN.put("UnaryExpression", List.of("Expression","OP_AND","OP_ASTERISK","OP_MINUS","OP_MINUS_MINUS","OP_NOT","OP_PLUS","OP_PLUS_PLUS","OP_TILDA"));
        TYPES_MIXINS.put("UnaryExpression", "DLanguageUnaryExpressionImplMixin");
        TYPES_EXTRA_INTERFACES.put("UnaryExpression", List.of("Expression"));
        STUB_CHILDREN.put("Unittest", List.of("Unittest",  "BlockStatement"));
        TYPES_EXTRA_INTERFACES.put("Unittest", List.of("Declaration"));
        TYPES_CHILDREN.put("VariadicArgumentsAttribute", List.of("KW_CONST", "KW_IMMUTABLE", "KW_SHARED", "KW_SCOPE", "KW_RETURN"));
        TYPES_CHILDREN.put("VariadicArgumentsAttributes", List.of("OP_TRIPLEDOT*", "VariadicArgumentsAttribute*"));
        TYPES_CHILDREN.put("Vector", List.of("KW___VECTOR", "Type", "OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_CHILDREN.put("VersionCondition", List.of("KW_VERSION","KW_UNITTEST","KW_ASSERT","INTEGER_LITERAL","Identifier","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_EXTRA_INTERFACES.put("VersionCondition", List.of("Declaration"));
        TYPES_CHILDREN.put("WhileStatement", List.of("KW_WHILE","IfCondition","Statement","OP_PAR_RIGHT","OP_PAR_LEFT"));
        TYPES_EXTRA_INTERFACES.put("WhileStatement", List.of("Statement"));
        TYPES_CHILDREN.put("WithStatement", List.of("KW_WITH","OP_PAR_LEFT","Expression","OP_PAR_RIGHT","Statement"));
        TYPES_EXTRA_INTERFACES.put("WithStatement", List.of("Statement"));
        TYPES_CHILDREN.put("XorExpression", List.of("Expression*", "OP_XOR"));
        TYPES_EXTRA_INTERFACES.put("XorExpression", List.of("BitwiseExpression"));



        HAS_PROCESS_DECLARATION.put("AddExpression", false);
        HAS_PROCESS_DECLARATION.put("AliasAssign", false);
        HAS_PROCESS_DECLARATION.put("AliasDeclaration", true);
        HAS_PROCESS_DECLARATION.put("AliasInitializer", false);
        HAS_PROCESS_DECLARATION.put("AliasThisDeclaration", false);
        HAS_PROCESS_DECLARATION.put("AlignAttribute", false);
        HAS_PROCESS_DECLARATION.put("AndAndExpression", false);
        HAS_PROCESS_DECLARATION.put("AndExpression", false);
        HAS_PROCESS_DECLARATION.put("AnonymousEnumDeclaration", true);
        HAS_PROCESS_DECLARATION.put("ArgumentList", false);
        HAS_PROCESS_DECLARATION.put("ArrayAccessExpression", false);
        HAS_PROCESS_DECLARATION.put("ArrayInitializer", false);
        HAS_PROCESS_DECLARATION.put("ArrayLiteral", false);
        HAS_PROCESS_DECLARATION.put("ArrayMemberInitializer", false);
        HAS_PROCESS_DECLARATION.put("AsmAddExp", false);
        HAS_PROCESS_DECLARATION.put("AsmAndExp", false);
        HAS_PROCESS_DECLARATION.put("AsmBrExp", false);
        HAS_PROCESS_DECLARATION.put("AsmEqualExp", false);
        HAS_PROCESS_DECLARATION.put("AsmExp", false);
        HAS_PROCESS_DECLARATION.put("AsmInstruction", false);
        HAS_PROCESS_DECLARATION.put("AsmLogAndExp", false);
        HAS_PROCESS_DECLARATION.put("AsmLogOrExp", false);
        HAS_PROCESS_DECLARATION.put("AsmMulExp", false);
        HAS_PROCESS_DECLARATION.put("AsmOrExp", false);
        HAS_PROCESS_DECLARATION.put("AsmPrimaryExp", false);
        HAS_PROCESS_DECLARATION.put("AsmRelExp", false);
        HAS_PROCESS_DECLARATION.put("AsmShiftExp", false);
        HAS_PROCESS_DECLARATION.put("AsmStatement", false);
        HAS_PROCESS_DECLARATION.put("AsmTypePrefix", false);
        HAS_PROCESS_DECLARATION.put("AsmUnaExp", false);
        HAS_PROCESS_DECLARATION.put("AsmXorExp", false);
        HAS_PROCESS_DECLARATION.put("AssertArguments", false);
        HAS_PROCESS_DECLARATION.put("AssertExpression", false);
        HAS_PROCESS_DECLARATION.put("AssignExpression", false);
        HAS_PROCESS_DECLARATION.put("AssocArrayLiteral", false);
        HAS_PROCESS_DECLARATION.put("AtAttribute", false);
        HAS_PROCESS_DECLARATION.put("Attribute", false);
        HAS_PROCESS_DECLARATION.put("AttributeDeclaration", false);
        HAS_PROCESS_DECLARATION.put("AttributeSpecifier", true);
        HAS_PROCESS_DECLARATION.put("AutoDeclaration", true);
        HAS_PROCESS_DECLARATION.put("AutoAssignment", false);
        HAS_PROCESS_DECLARATION.put("BlockStatement", true);
        HAS_PROCESS_DECLARATION.put("BreakStatement", false);
        HAS_PROCESS_DECLARATION.put("BaseClassList", false);
        HAS_PROCESS_DECLARATION.put("BasicType", false);
        HAS_PROCESS_DECLARATION.put("BuiltinType", false);
        HAS_PROCESS_DECLARATION.put("CaseRangeStatement", true);
        HAS_PROCESS_DECLARATION.put("CaseStatement", true);
        HAS_PROCESS_DECLARATION.put("CastExpression", false);
        HAS_PROCESS_DECLARATION.put("CastQualifier", false);
        HAS_PROCESS_DECLARATION.put("Catch", true);
        HAS_PROCESS_DECLARATION.put("Catches", false);
        //has_processDeclaration["ClassDeclaration"] = true;
        HAS_PROCESS_DECLARATION.put("CompileCondition", false);
        HAS_PROCESS_DECLARATION.put("ConditionalDeclaration", true);
        HAS_PROCESS_DECLARATION.put("ConditionalStatement", true);
        HAS_PROCESS_DECLARATION.put("Constraint", false);
        HAS_PROCESS_DECLARATION.put("Constructor", true);
        HAS_PROCESS_DECLARATION.put("ContinueStatement", false);
        HAS_PROCESS_DECLARATION.put("DebugCondition", false);
        HAS_PROCESS_DECLARATION.put("DebugSpecification", false);
        HAS_PROCESS_DECLARATION.put("DeclarationBlock", true);
        HAS_PROCESS_DECLARATION.put("DeclarationStatement", true);
        HAS_PROCESS_DECLARATION.put("DefaultStatement", true);
        HAS_PROCESS_DECLARATION.put("DeleteExpression", false);
        HAS_PROCESS_DECLARATION.put("DeleteStatement", false);
        HAS_PROCESS_DECLARATION.put("Deprecated", false);
        HAS_PROCESS_DECLARATION.put("Destructor", false);
        HAS_PROCESS_DECLARATION.put("DoStatement", false);
        HAS_PROCESS_DECLARATION.put("EmptyStatement", false);
        HAS_PROCESS_DECLARATION.put("EnumBody", false);
//    has_processDeclaration["EnumDeclaration"] = true;
        HAS_PROCESS_DECLARATION.put("EnumMember", false);
        HAS_PROCESS_DECLARATION.put("EnumMemberAttribute", false);
        HAS_PROCESS_DECLARATION.put("EqualExpression", false);
        HAS_PROCESS_DECLARATION.put("ExpressionStatement", false);
        HAS_PROCESS_DECLARATION.put("Finally", false);
        HAS_PROCESS_DECLARATION.put("FinalSwitchStatement", false);
        HAS_PROCESS_DECLARATION.put("ForStatement", true);
        HAS_PROCESS_DECLARATION.put("ForeachStatement", true);
        HAS_PROCESS_DECLARATION.put("ForeachTypeList", false);
        HAS_PROCESS_DECLARATION.put("ForeachType", false);
        HAS_PROCESS_DECLARATION.put("FunctionAttribute", false);
        HAS_PROCESS_DECLARATION.put("FunctionCallExpression", false);
        HAS_PROCESS_DECLARATION.put("FunctionContract", false);
        HAS_PROCESS_DECLARATION.put("FunctionDeclaration", true);
        HAS_PROCESS_DECLARATION.put("FunctionLiteralExpression", true);
        HAS_PROCESS_DECLARATION.put("GotoStatement", false);
        HAS_PROCESS_DECLARATION.put("IdentifierChain", false);
        HAS_PROCESS_DECLARATION.put("IdentifierInitializer", false);
        HAS_PROCESS_DECLARATION.put("IdentifierList", false);
        HAS_PROCESS_DECLARATION.put("IdentityExpression", false);
        HAS_PROCESS_DECLARATION.put("IfStatement", true);
        HAS_PROCESS_DECLARATION.put("IfCondition", false);
        HAS_PROCESS_DECLARATION.put("ImportBind", false);
        HAS_PROCESS_DECLARATION.put("ImportBindings", false);
        HAS_PROCESS_DECLARATION.put("ImportDeclaration", false);
        HAS_PROCESS_DECLARATION.put("ImportExpression", false);
        HAS_PROCESS_DECLARATION.put("Index", false);
        HAS_PROCESS_DECLARATION.put("IndexExpression", false);
        HAS_PROCESS_DECLARATION.put("InExpression", false);
        HAS_PROCESS_DECLARATION.put("Initializer", false);
        HAS_PROCESS_DECLARATION.put("InContractExpression", false);
        HAS_PROCESS_DECLARATION.put("InStatement", false);
        HAS_PROCESS_DECLARATION.put("Initializer", false);
        HAS_PROCESS_DECLARATION.put("InterfaceDeclaration", true);
        HAS_PROCESS_DECLARATION.put("Invariant", false);
        HAS_PROCESS_DECLARATION.put("IsExpression", false);
        HAS_PROCESS_DECLARATION.put("KeyValuePair", false);
        HAS_PROCESS_DECLARATION.put("KeyValuePairs", false);
        HAS_PROCESS_DECLARATION.put("LambdaExpression", true);
        HAS_PROCESS_DECLARATION.put("LabeledStatement", false);
        HAS_PROCESS_DECLARATION.put("LastCatch", false);
        HAS_PROCESS_DECLARATION.put("LinkageAttribute", false);
        HAS_PROCESS_DECLARATION.put("MemberFunctionAttribute", false);
        HAS_PROCESS_DECLARATION.put("MixinDeclaration", false);
        HAS_PROCESS_DECLARATION.put("MixinQualifiedIdentifier", false);
        HAS_PROCESS_DECLARATION.put("MixinTemplateName", false);
        HAS_PROCESS_DECLARATION.put("ModuleDeclaration", false);
        HAS_PROCESS_DECLARATION.put("MulExpression", false);
        HAS_PROCESS_DECLARATION.put("NamedImportBind", false);
        HAS_PROCESS_DECLARATION.put("NewAnonClassExpression", false);
        HAS_PROCESS_DECLARATION.put("NewExpression", false);
        HAS_PROCESS_DECLARATION.put("Operands", false);
        HAS_PROCESS_DECLARATION.put("OrExpression", false);
        HAS_PROCESS_DECLARATION.put("OrOrExpression", false);
        HAS_PROCESS_DECLARATION.put("OutStatement", false);
        HAS_PROCESS_DECLARATION.put("Parameter", true);
        HAS_PROCESS_DECLARATION.put("ParameterAttribute", false);
        HAS_PROCESS_DECLARATION.put("Parameters", false);
        HAS_PROCESS_DECLARATION.put("Postblit", false);
        HAS_PROCESS_DECLARATION.put("PowExpression", false);
        HAS_PROCESS_DECLARATION.put("PragmaExpression", false);
        HAS_PROCESS_DECLARATION.put("QualifiedIdentifier", false);
        HAS_PROCESS_DECLARATION.put("Register", false);
        HAS_PROCESS_DECLARATION.put("RelExpression", false);
        HAS_PROCESS_DECLARATION.put("ReturnStatement", false);
        HAS_PROCESS_DECLARATION.put("ScopeGuardStatement", false);
        HAS_PROCESS_DECLARATION.put("SharedStaticConstructor", false);
        HAS_PROCESS_DECLARATION.put("SharedStaticDestructor", false);
        HAS_PROCESS_DECLARATION.put("ShiftExpression", false);
//    has_processDeclaration["SingleImport"] = false;
        HAS_PROCESS_DECLARATION.put("UnaryExpression", false);
        HAS_PROCESS_DECLARATION.put("SpecifiedVariableDeclaration", true);
        HAS_PROCESS_DECLARATION.put("StaticAssertDeclaration", false);
        HAS_PROCESS_DECLARATION.put("StaticAssertStatement", false);
        HAS_PROCESS_DECLARATION.put("StaticConstructor", false);
        HAS_PROCESS_DECLARATION.put("StaticDestructor", false);
        HAS_PROCESS_DECLARATION.put("StaticCtorDtorCommon", false);
        HAS_PROCESS_DECLARATION.put("StaticForeachDeclaration", true);
        HAS_PROCESS_DECLARATION.put("StaticIfCondition", false);
        HAS_PROCESS_DECLARATION.put("StorageClass", false);
        HAS_PROCESS_DECLARATION.put("StructBody", true);
//    has_processDeclaration["StructDeclaration"] = true;
        HAS_PROCESS_DECLARATION.put("StructInitializer", false);
        HAS_PROCESS_DECLARATION.put("StructMemberInitializer", false);
        HAS_PROCESS_DECLARATION.put("String", false);
        HAS_PROCESS_DECLARATION.put("SwitchStatement", false);
        HAS_PROCESS_DECLARATION.put("SynchronizedStatement", false);
        HAS_PROCESS_DECLARATION.put("TemplateAliasParameter", false);
        HAS_PROCESS_DECLARATION.put("TemplateArgument", false);
        HAS_PROCESS_DECLARATION.put("TemplateArgumentList", false);
        HAS_PROCESS_DECLARATION.put("TemplateArguments", false);
//    has_processDeclaration["TemplateDeclaration"] = true;
        HAS_PROCESS_DECLARATION.put("TemplateInstance", false);
        HAS_PROCESS_DECLARATION.put("TemplateMixinDeclaration", true);
        HAS_PROCESS_DECLARATION.put("TemplateParameterList", false);
        HAS_PROCESS_DECLARATION.put("TemplateParameters", true);
        HAS_PROCESS_DECLARATION.put("TemplateSingleArgument", false);
        HAS_PROCESS_DECLARATION.put("TemplateThisParameter", false);
        HAS_PROCESS_DECLARATION.put("TemplateTupleParameter", false);
        HAS_PROCESS_DECLARATION.put("TemplateTypeParameter", false);
        HAS_PROCESS_DECLARATION.put("TemplateValueParameter", false);
        HAS_PROCESS_DECLARATION.put("TemplateValueParameterDefault", false);
        HAS_PROCESS_DECLARATION.put("TernaryExpression", false);
        HAS_PROCESS_DECLARATION.put("ThrowStatement", false);
        HAS_PROCESS_DECLARATION.put("TraitsExpression", false);
        HAS_PROCESS_DECLARATION.put("TryStatement", false);
        HAS_PROCESS_DECLARATION.put("Type", false);
        HAS_PROCESS_DECLARATION.put("TypeidExpression", false);
        HAS_PROCESS_DECLARATION.put("TypeofExpression", false);
        HAS_PROCESS_DECLARATION.put("TypeSpecialization", false);
        HAS_PROCESS_DECLARATION.put("TypeSuffix", false);
        HAS_PROCESS_DECLARATION.put("UnaryExpression", false);
//    has_processDeclaration["UnionDeclaration"] = true;
        HAS_PROCESS_DECLARATION.put("Unittest", false);
        HAS_PROCESS_DECLARATION.put("VariableDeclaration", false);
        HAS_PROCESS_DECLARATION.put("Vector", false);
        HAS_PROCESS_DECLARATION.put("VersionCondition", false);
        HAS_PROCESS_DECLARATION.put("VersionSpecification", true);
        HAS_PROCESS_DECLARATION.put("WhileStatement", true);
        HAS_PROCESS_DECLARATION.put("WithStatement", true);
        HAS_PROCESS_DECLARATION.put("XorExpression", false);    }

    static final String psiDlangImportTemplate = "import io.github.intellij.dlanguage.psi.DLanguage%s;";
    static final String psiDlangExtraInterfaceImportTemplate = "import io.github.intellij.dlanguage.psi.interfaces.%s;";
    static final String psiDlangNamedImportTemplate = "import io.github.intellij.dlanguage.psi.named.DLanguage%s;";

    static final List<String> defaultInterfaceImports = List.of(
        "import consulo.language.psi.PsiElement;",
        "import org.jspecify.annotations.Nullable;"
    );

    static final List<String> defaultImplImports = List.of(
        "import consulo.language.ast.ASTNode;",
        "import consulo.language.psi.PsiElement;",
        "import consulo.language.psi.PsiElementVisitor;",
        "import consulo.language.psi.util.PsiTreeUtil;",
        "import io.github.intellij.dlanguage.psi.DlangVisitor;",
        "import org.jspecify.annotations.Nullable;"
    );

    static final List<String> defaultStubImplImports = List.of(
        "import consulo.language.ast.ASTNode;",
        "import consulo.language.psi.PsiElement;",
        "import consulo.language.psi.PsiElementVisitor;",
        "import consulo.language.psi.stub.IStubElementType;",
        "import consulo.language.psi.util.PsiTreeUtil;",
        "import io.github.intellij.dlanguage.psi.DlangTypes;",
        "import io.github.intellij.dlanguage.psi.DlangVisitor;",
        "import org.jspecify.annotations.Nullable;"
    );

    static final String implFileTemplate = """
        package io.github.intellij.dlanguage.psi.impl;

        %s


        public class %s extends %s implements
            %s {

            public %s(ASTNode node) {
                super(node);
            }

            public void accept(DlangVisitor visitor) {
                visitor.visit%s(this);
            }

            public void accept(PsiElementVisitor visitor) {
                if (visitor instanceof DlangVisitor) {
                    accept((DlangVisitor) visitor);
                }
                else {
                    super.accept(visitor);
                }
            }
        """;

    static final String implStubFileTemplate = """
        package io.github.intellij.dlanguage.psi.impl;

        %s


        public class %s extends
            DStubbedPsiElementBase<%s> implements %s {

            public %s(ASTNode node) {
                super(node);
            }

            public %s(%s stub,
                IStubElementType nodeType) {
                super(stub, nodeType);
            }

            public void accept(DlangVisitor visitor) {
                visitor.visit%s(this);
            }

            public void accept(PsiElementVisitor visitor) {
                if (visitor instanceof DlangVisitor) {
                    accept((DlangVisitor) visitor);
                }
                else {
                    super.accept(visitor);
                }
            }
        """;

    static String interfaceFileTemplate(List<String> interfaces) {
        String base = """
            package io.github.intellij.dlanguage.psi;

            %s


            public interface %s extends PsiElement""";
        for (String interface_ : interfaces) {
            base += (", " + interface_);
        }
        base += " {\n";
        return base;
    }

    static boolean isToken(String toget) {
        String name = toget.replace("*", "");
        return name.contains("OP") || name.contains("KW") || name.contains("LITERAL") || name.contains("STRING") || name.equals("Identifier");
    }

    static boolean isExtraInterface(String toget) {
        return !TYPES_CHILDREN.containsKey(toget) && !STUB_CHILDREN.containsKey(toget) && !NAMED_CHILDREN.contains(toget);
    }

    static List<String> getExtraImport(List<String> elements, String key) {
        List<String> normalImports = new ArrayList<>();
        if (elements.stream().anyMatch(a -> a.contains("*"))) {
            normalImports.add("import java.util.List;");
        }
        if (HAS_PROCESS_DECLARATION.getOrDefault(key, false)) {
            normalImports.addAll(List.of(
                "import consulo.language.psi.resolve.ResolveState;",
                "import consulo.language.psi.resolve.PsiScopeProcessor;",
                "import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;"
            ));
        }
        return normalImports;
    }

    static String getNonTokenImport(String toget, String key) {
        String import_;
        if (toget.contains("*")) {
            String name = toget.replace("*", "");
            if ((NAMED_CHILDREN.contains(name) && !STUB_CHILDREN.containsKey(name))) {
                import_ = "import io.github.intellij.dlanguage.psi.named.DLanguage%s;".formatted(name);
            }
            else if (isExtraInterface(name)) {
                import_ = psiDlangExtraInterfaceImportTemplate.formatted(name);
            }
            else {
                import_ = psiDlangImportTemplate.formatted(name);
            }
        }
        else {
            if (NAMED_CHILDREN.contains(toget) && !STUB_CHILDREN.containsKey(toget)) {
                import_ = psiDlangNamedImportTemplate.formatted(toget);
            }
            else if (isExtraInterface(toget)) {
                import_ = psiDlangExtraInterfaceImportTemplate.formatted(toget);
            }
            else {
                import_ = psiDlangImportTemplate.formatted(toget);
            }
        }
        return import_;
    }

    static void getImplImportsElements(List<String> elements, String key, List<String> staticImports, List<String> normalImports) {
        // By default import the base type
        normalImports.add(psiDlangImportTemplate.formatted(key));
        for (String toget : elements) {
            if (isToken(toget)) {
                String token = toget.replace("*", "");
                // tokens are imported statically
                if (token.equals("Identifier")) {
                    token = "ID";
                }
                staticImports.add("import static io.github.intellij.dlanguage.psi.DlangTypes.%s;".formatted(token));
            }
            else {
                normalImports.add(getNonTokenImport(toget, key));
            }
        }
        normalImports.addAll(getExtraImport(elements, key));
    }

    static String formatImplImports(List<String> staticImports, List<String> normalImports) {
        String importString = String.join("\n", new TreeSet<>(staticImports));
        if (staticImports.size() > 0) {
            importString += "\n\n";
        }
        importString += String.join("\n", new TreeSet<>(normalImports));
        return importString;
    }

    static String getImplImports(List<String> elements, String key, String parentClassName) {
        List<String> normalImports = new ArrayList<>(defaultImplImports);
        List<String> staticImports = new ArrayList<>();
        if (parentClassName != null && !parentClassName.equals(defaultParentClassName)) {
            normalImports.add("import io.github.intellij.dlanguage.psi.ext." + parentClassName + ";");
        }
        else {
            normalImports.add("import consulo.language.impl.psi.ASTWrapperPsiElement;");
        }
        getImplImportsElements(elements, key, staticImports, normalImports);
        return formatImplImports(staticImports, normalImports);
    }

    static String getStubImplImports(Map<String, List<String>> elements, String key) {
        List<String> normalImports = new ArrayList<>(defaultStubImplImports);
        List<String> staticImports = new ArrayList<>();
        String stubImport = "import io.github.intellij.dlanguage.stubs.DLanguage%sStub;".formatted(key);
        normalImports.add(stubImport);
        getImplImportsElements(elements.get(key), key, staticImports, normalImports);
        return formatImplImports(staticImports, normalImports);
    }

    static String getterMethod(String toget) {
        boolean hasMultiple = toget.contains("*");
        String name = toget.replace("*", "");

        String type;
        if (isToken(name)) {
            type = "PsiElement";
        }
        else if (isExtraInterface(name)) {
            type = name;
        }
        else {
            type = "DLanguage" + name;
        }

        if (hasMultiple) {
            if (isToken(name)) {
                String typeToGet = name;
                if (name.equals("Identifier")) {
                    typeToGet = "ID";
                }
                return """

                        public List<PsiElement> get%ss() {
                            return findChildrenByType(%s);
                        }
                    """.formatted(name, typeToGet);
            }
            else {
                return """

                        public List<%s> get%ss() {
                            return PsiTreeUtil.getChildrenOfTypeAsList(this, %s.class);
                        }
                    """.formatted(type, name, type);
            }
        }

        if (isToken(name)) {
            String typeToGet = name;
            if (name.equals("Identifier")) {
                typeToGet = "ID";
            }
            return """

                    public @Nullable PsiElement get%s() {
                        return findChildByType(%s);
                    }
                """.formatted(name, typeToGet);
        }
        else {
            return """

                    public @Nullable %s get%s() {
                        return PsiTreeUtil.getChildOfType(this, %s.class);
                    }
                """.formatted(type, name, type);
        }
    }

    static List<String> getInterfaceImportElements(List<String> elements, String key) {
        List<String> normalImports = new ArrayList<>(defaultInterfaceImports);
        for (String toget : elements) {
            if (isToken(toget)) {
                continue;
            }
            normalImports.add(getNonTokenImport(toget, key));
        }
        if (elements.stream().anyMatch(a -> a.contains("*"))) {
            normalImports.add("import java.util.List;");
        }
        return normalImports;
    }

    static String getInterfaceImports(List<String> elements, String key) {
        List<String> imports = getInterfaceImportElements(elements, key);
        return String.join("\n", new TreeSet<>(imports));
    }

    static String getStubInterfaceImports(List<String> elements, String key) {
        List<String> imports = getInterfaceImportElements(elements, key);
        // unittest is a special case where it’s stored under another path
        String stubImport = "import io.github.intellij.dlanguage.stubs.DLanguage%sStub;".formatted(key);
        imports.addAll(List.of(
            "import consulo.language.psi.StubBasedPsiElement;",
            stubImport
        ));
        //if (named_children.canFind(key))
        imports.add("import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;");
        if (HAS_PROCESS_DECLARATION.getOrDefault(key, false)) {
            // upstream emitted processDeclarations into stub interfaces without importing its types; no stub type has it today
            imports.addAll(List.of(
                "import consulo.language.psi.resolve.ResolveState;",
                "import consulo.language.psi.resolve.PsiScopeProcessor;",
                "import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;"
            ));
        }
        return String.join("\n", new TreeSet<>(imports));
    }

    static String getterMethodInterface(String toget) {
        String type;
        boolean hasMultiple = toget.contains("*");
        String name = toget.replace("*", "");

        if (isToken(name)) {
            type = "PsiElement";
        }
        else if (isExtraInterface(name)) {
            type = name;
        }
        else {
            type = "DLanguage" + name;
        }

        if (hasMultiple) {
            return """

                    List<%s> get%ss();
                """.formatted(type, name);
        }
        return """

                @Nullable
                %s get%s();
            """.formatted(type, name);
    }

    static List<String> uniq(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (result.isEmpty() || !result.get(result.size() - 1).equals(s)) {
                result.add(s);
            }
        }
        return result;
    }

    static List<String> concat(List<String> a, List<String> b) {
        List<String> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
    }

    static void write(Path file, String text) throws IOException {
        Files.writeString(file, text, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2 || (!args[0].equals("Interface") && !args[0].equals("Implementation"))) {
            System.out.println("Error: need to provide the target (Interface or Implementation) and the output directory");
            System.exit(1);
        }
        boolean genInterface = args[0].equals("Interface");
        if (genInterface) {
            System.out.println("Generating Java interfaces");
        }
        else {
            System.out.println("Generating Java implementation classes");
        }

        Path packagePath = Path.of(args[1], genInterface ? "io/github/intellij/dlanguage/psi" : "io/github/intellij/dlanguage/psi/impl");

        // preparatory work
        Files.createDirectories(packagePath);

        for (String key : TYPES_CHILDREN.keySet()) {
            String parentClassName = TYPES_MIXINS.getOrDefault(key, defaultParentClassName);
            String interfaceClassName = "DLanguage" + key;
            if (genInterface) {
                // interface
                List<String> extraInterfaces = TYPES_EXTRA_INTERFACES.getOrDefault(key, List.of());
                String interfaceFile = interfaceFileTemplate(extraInterfaces)
                    .formatted(getInterfaceImports(uniq(concat(TYPES_CHILDREN.get(key), extraInterfaces)), key), interfaceClassName);
                for (String toget : TYPES_CHILDREN.get(key)) {
                    interfaceFile += getterMethodInterface(toget);
                }
                interfaceFile += "\n}\n";

                write(packagePath.resolve(interfaceClassName + ".java"), interfaceFile);
            }
            else {
                if (IGNORE_IMPLEMENTATION_GENERATION.contains(key)) {
                    continue;
                }
                // implementation
                String implClassName = "DLanguage" + key + "Impl";
                String implFile = implFileTemplate.formatted(getImplImports(TYPES_CHILDREN.get(key), key, parentClassName), implClassName,
                    parentClassName, interfaceClassName, implClassName, key);
                for (String toget : TYPES_CHILDREN.get(key)) {
                    implFile += getterMethod(toget);
                }
                if (HAS_PROCESS_DECLARATION.getOrDefault(key, false)) {
                    implFile += """

                            @Override
                            public boolean processDeclarations(PsiScopeProcessor processor,
                                                               ResolveState state,
                                                               @Nullable PsiElement lastParent,
                                                               PsiElement place) {
                                return ScopeProcessorImpl.INSTANCE.processDeclarations(this,processor,state,lastParent,place);
                            }
                        """;
                }
                implFile += "\n}\n";

                write(packagePath.resolve(implClassName + ".java"), implFile);
            }
        }

        for (String key : STUB_CHILDREN.keySet()) {
            String interfaceClassName = "DLanguage" + key;
            if (genInterface) {
                // interface
                List<String> interfaces = new ArrayList<>(List.of("StubBasedPsiElement<DLanguage%sStub>".formatted(key)));
                if (NAMED_CHILDREN.contains(key)) {
                    interfaces.add(1, "DNamedElement");
                }
                List<String> extraInterfaces = TYPES_EXTRA_INTERFACES.getOrDefault(key, List.of());
                String interfaceFile = interfaceFileTemplate(concat(interfaces, extraInterfaces))
                    .formatted(getStubInterfaceImports(uniq(concat(STUB_CHILDREN.get(key), extraInterfaces)), key), interfaceClassName);
                for (String toget : STUB_CHILDREN.get(key)) {
                    interfaceFile += getterMethodInterface(toget);
                }
                if (HAS_PROCESS_DECLARATION.getOrDefault(key, false)) {
                    interfaceFile += """

                            @Override
                            default public boolean processDeclarations(PsiScopeProcessor processor,
                                                                       ResolveState state,
                                                                       @Nullable PsiElement lastParent,
                                                                       PsiElement place) {
                                return ScopeProcessorImpl.INSTANCE.processDeclarations(this,processor,state,lastParent,place);
                            }
                        """;
                }
                interfaceFile += "\n}\n";
                String javaFileName = interfaceClassName + ".java";

                write(packagePath.resolve(javaFileName), interfaceFile);

                System.out.println(" - " + javaFileName);
            }
            else if (GENERATE_STUB_IMPL.contains(key)) {
                // implementation
                String implClassName = "DLanguage" + key + "Impl";
                String stubPsiElementClassName = "DLanguage" + key + "Stub";
                String implFile = implStubFileTemplate.formatted(getStubImplImports(STUB_CHILDREN, key), implClassName,
                    stubPsiElementClassName, interfaceClassName, implClassName, implClassName, stubPsiElementClassName, key);
                for (String toget : STUB_CHILDREN.get(key)) {
                    implFile += getterMethod(toget);
                }
                implFile += "\n}\n";
                String javaFileName = implClassName + ".java";

                write(packagePath.resolve(javaFileName), implFile);

                System.out.println(" - impl/" + javaFileName);
            }
        }
    }
}
