package io.github.intellij.dlanguage.parser;

import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.WhitespacesBinders;
import consulo.localize.LocalizeValue;
import io.github.intellij.dlanguage.psi.DlangTokenType;
import io.github.intellij.dlanguage.psi.DlangTypes;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DLangParser {
    // Please keep this list in order
    private static final Set<String> REGISTER_NAMES = new HashSet<>(
        Arrays.asList(
            "AH", "AL", "AX", "BH", "BL", "BP", "BPL", "BX", "CH", "CL", "CR0", "CR2",
            "CR3", "CR4", "CS", "CX", "DH", "DI", "DIL", "DL", "DR0", "DR1", "DR2",
            "DR3", "DR6", "DR7", "DS", "DX", "EAX", "EBP", "EBX", "ECX", "EDI", "EDX",
            "ES", "ESI", "ESP", "FS", "GS", "MM0", "MM1", "MM2", "MM3", "MM4", "MM5",
            "MM6", "MM7", "R10", "R10B", "R10D", "R10W", "R11", "R11B", "R11D", "R11W",
            "R12", "R12B", "R12D", "R12W", "R13", "R13B", "R13D", "R13W", "R14", "R14B",
            "R14D", "R14W", "R15", "R15B", "R15D", "R15W", "R8", "R8B", "R8D", "R8W",
            "R9", "R9B", "R9D", "R9W", "RAX", "RBP", "RBX", "RCX", "RDI", "RDX", "RSI",
            "RSP", "SI", "SIL", "SP", "SPL", "SS", "ST", "TR3", "TR4", "TR5", "TR6",
            "TR7", "XMM0", "XMM1", "XMM10", "XMM11", "XMM12", "XMM13", "XMM14", "XMM15",
            "XMM2", "XMM3", "XMM4", "XMM5", "XMM6", "XMM7", "XMM8", "XMM9", "YMM0",
            "YMM1", "YMM10", "YMM11", "YMM12", "YMM13", "YMM14", "YMM15", "YMM2",
            "YMM3", "YMM4", "YMM5", "YMM6", "YMM7", "YMM8", "YMM9"
        )
    );

    private static final IElementType[] STRING_LITERALS_ARRAY = new IElementType[]{
        DlangTypes.ALTERNATE_WYSIWYG_STRING,
        DlangTypes.DOUBLE_QUOTED_STRING,
        DlangTypes.WYSIWYG_STRING,
        DlangTypes.DELIMITED_STRING,
        DlangTypes.TOKEN_STRING,
        DlangTypes.HEX_STRING,
        DlangTypes.IES_START
    };

    private static final Set<IElementType> LITERALS = new HashSet<>(Arrays.asList(
        DlangTypes.KW_THIS,
        DlangTypes.KW_SUPER,
        DlangTypes.KW_NULL,
        DlangTypes.KW_TRUE,
        DlangTypes.KW_FALSE,
        DlangTypes.INTEGER_LITERAL,  // represent int, long, uint and ulong values
        DlangTypes.FLOAT_LITERAL,  // represent float, double, ifloat, idouble, real and ireal values
        DlangTypes.CHARACTER_LITERAL,
        DlangTypes.ALTERNATE_WYSIWYG_STRING,
        DlangTypes.DOUBLE_QUOTED_STRING,
        DlangTypes.WYSIWYG_STRING,
        DlangTypes.DELIMITED_STRING,
        DlangTypes.TOKEN_STRING,
        DlangTypes.HEX_STRING,
        DlangTypes.IES_START
    ));

    private static final Set<IElementType> BUILTIN_TYPES = new HashSet<>(Arrays.asList(
        DlangTypes.KW_INT,
        DlangTypes.KW_BOOL,
        DlangTypes.KW_BYTE,
        DlangTypes.KW_CDOUBLE,
        DlangTypes.KW_CENT,
        DlangTypes.KW_CFLOAT,
        DlangTypes.KW_CHAR,
        DlangTypes.KW_CREAL,
        DlangTypes.KW_DCHAR,
        DlangTypes.KW_DOUBLE,
        DlangTypes.KW_FLOAT,
        DlangTypes.KW_IDOUBLE,
        DlangTypes.KW_IFLOAT,
        DlangTypes.KW_IREAL,
        DlangTypes.KW_LONG,
        DlangTypes.KW_REAL,
        DlangTypes.KW_SHORT,
        DlangTypes.KW_UBYTE,
        DlangTypes.KW_UCENT,
        DlangTypes.KW_UINT,
        DlangTypes.KW_ULONG,
        DlangTypes.KW_USHORT,
        DlangTypes.KW_VOID,
        DlangTypes.KW_WCHAR
    ));

    private final PsiBuilder myBuilder;
    private final Map<Integer, Boolean> myCachedAAChecks = new HashMap<>();
    private final Map<Integer, Boolean> myCachedTypedChecks = new HashMap<>();

    public DLangParser(PsiBuilder builder) {
        myBuilder = builder;
    }

    private void cleanup(PsiBuilder.Marker marker, IElementType element) {
        exit_section_modified(myBuilder, marker, element, true);
    }

    /**
     * Parses an AddExpression.
     * converted
     * $(GRAMMAR $(RULEDEF addExpression):
     * $(RULE mulExpression)
     * | $(RULE addExpression) $(LPAREN)$(LITERAL '+') | $(LITERAL'-') | $(LITERAL'~')$(RPAREN) $(RULE mulExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseAddExpression() {
        PsiBuilder.Marker m = parseMulExpression();
        if (m == null) {
            return null;
        }
        while (currentIsOneOf(DlangTypes.OP_PLUS, DlangTypes.OP_MINUS, DlangTypes.OP_TILDA)) {
            m = m.precede();
            myBuilder.advanceLexer();
            if (parseMulExpression() == null) {
                m.done(DlangTypes.ADD_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.ADD_EXPRESSION);
        }
        return m;
    }

    /**
     * Parses an AliasDeclaration.
     * converted
     * $(GRAMMAR $(RULEDEF aliasDeclaration):
     * $(LITERAL 'alias') $(RULE aliasInitializer) $(LPAREN)$(LITERAL ',') $(RULE aliasInitializer)$(RPAREN)* $(LITERAL ';')
     * | $(LITERAL 'alias') $(RULE storageClass)* $(RULE type) $(RULE declaratorIdentifierList) $(LITERAL ';')
     * | $(LITERAL 'alias') $(RULE storageClass)* $(RULE type) $(RULE identifier) $(LITERAL '(') $(RULE parameters) $(LITERAL ')') $(memberFunctionAttribute)* $(LITERAL ';')
     * ;)
     */
    public boolean parseAliasDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_ALIAS) {
            return false;
        }
        myBuilder.advanceLexer();
        if (startsWith(DlangTypes.ID, DlangTypes.OP_EQ) || startsWith(DlangTypes.ID, DlangTypes.OP_PAR_LEFT)) {
            do {
                if (!parseAliasInitializer()) {
                    m.done(DlangTypes.ALIAS_DECLARATION);
                    return true;
                }
                if (currentIs(DlangTypes.OP_COMMA)) {
                    advance();
                }
                else {
                    break;
                }
            }
            while (moreTokens());
        }
        else {
            while (moreTokens() && isStorageClass()) {
                if (!parseStorageClass()) {
                    m.done(DlangTypes.ALIAS_DECLARATION);
                    return true;
                }
            }
            if (!parseType()) {
                m.done(DlangTypes.ALIAS_DECLARATION);
                return true;
            }
            if (!parseDeclaratorIdentifierList()) {
                m.done(DlangTypes.ALIAS_DECLARATION);
                return true;
            }
            if (currentIs(DlangTypes.OP_PAR_LEFT)) {
                if (!parseParameters()) {
                    m.done(DlangTypes.ALIAS_DECLARATION);
                    return true;
                }
                while (moreTokens() && currentIsMemberFunctionAttribute()) {
                    if (!parseMemberFunctionAttribute()) {
                        m.done(DlangTypes.ALIAS_DECLARATION);
                        return true;
                    }
                }
            }
        }
        expect(DlangTypes.OP_SCOLON);
        m.done(DlangTypes.ALIAS_DECLARATION);
        return true;
    }

    private boolean isFunction() {
        if (currentIsOneOf(
            DlangTypes.KW_FUNCTION,
            DlangTypes.KW_DELEGATE,
            DlangTypes.OP_BRACES_LEFT
        )) {
            return true;
        }
        if (startsWith(
            DlangTypes.ID,
            DlangTypes.OP_LAMBDA_ARROW
        )) {
            return true;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        if (currentIs(DlangTypes.OP_PAR_LEFT) || currentIs(DlangTypes.KW_REF) && peekIs(
            DlangTypes.OP_PAR_LEFT
        )) {
            if (currentIs(DlangTypes.KW_REF)) {
                advance();
            }
            IElementType t = peekPastParens();
            if (t != null) {
                if (t == DlangTypes.OP_LAMBDA_ARROW || t == DlangTypes.OP_BRACES_LEFT || isMemberFunctionAttribute(
                    t
                )) {
                    bookmark.rollbackTo();
                    return true;
                }
            }
        }
        bookmark.rollbackTo();
        return false;
    }

    /**
     * Parses an AliasAssign.
     *
     *
     * $(GRAMMAR $(RULEDEF aliasAssign):
     * $(LITERAL Identifier) $(LITERAL '=') $(RULE type)
     */
    public boolean parseAliasAssign(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.ID) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        advance();
        if (!tokenCheck(DlangTypes.OP_EQ)) {
            bookmark.rollbackTo();
            return false;
        }
        if (!parseType()) {
            bookmark.rollbackTo();
            return false;
        }
        if (myBuilder.getTokenType() != DlangTypes.OP_SCOLON) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        myBuilder.advanceLexer();
        m.done(DlangTypes.ALIAS_ASSIGN);
        return true;
    }

    /**
     * Parses an AliasInitializer.
     *
     *
     * $(GRAMMAR $(RULEDEF aliasInitializer):
     * $(LITERAL Identifier) $(RULE templateParameters)? $(LITERAL '=') $(RULE storageClass)* $(RULE type)
     * | $(LITERAL Identifier) $(RULE templateParameters)? $(LITERAL '=') $(RULE storageClass)* $(RULE type) $(RULE parameters) $(RULE memberFunctionAttribute)*
     * | $(LITERAL Identifier) $(RULE templateParameters)? $(LITERAL '=') $(RULE functionLiteralExpression)
     * ;)
     */
    public boolean parseAliasInitializer() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.ID)) {
            cleanup(m, DlangTypes.ALIAS_INITIALIZER);
            return false;
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            if (!parseTemplateParameters()) {
                cleanup(m, DlangTypes.ALIAS_INITIALIZER);
                return false;
            }
        }
        if (!tokenCheck(DlangTypes.OP_EQ)) {
            cleanup(m, DlangTypes.ALIAS_INITIALIZER);
            return false;
        }
        if (isFunction()) {
            if (parseFunctionLiteralExpression() == null) {
                cleanup(m, DlangTypes.ALIAS_INITIALIZER);
                return false;
            }
        }
        else {
            while (moreTokens() && isStorageClass()) {
                if (!parseStorageClass()) {
                    cleanup(m, DlangTypes.ALIAS_INITIALIZER);
                    return false;
                }
            }
            if (!parseType()) {
                cleanup(m, DlangTypes.ALIAS_INITIALIZER);
                return false;
            }
            if (currentIs(DlangTypes.OP_PAR_LEFT)) {
                if (!parseParameters()) {
                    cleanup(m, DlangTypes.ALIAS_INITIALIZER);
                    return false;
                }
                while (moreTokens() && currentIsMemberFunctionAttribute()) {
                    if (!parseMemberFunctionAttribute()) {
                        cleanup(m, DlangTypes.ALIAS_INITIALIZER);
                        return false;
                    }
                }
            }
        }
        m.done(DlangTypes.ALIAS_INITIALIZER);
        return true;
    }

    /**
     * Parses an AliasThisDeclaration.
     *
     *
     * $(GRAMMAR $(RULEDEF aliasThisDeclaration):
     * $(LITERAL 'alias') $(LITERAL Identifier) $(LITERAL 'this') $(LITERAL ';')
     * | $(LITERAL 'alias') $(LITERAL 'this') $(LITERAL Identifier) $(LITERAL ';')
     * ;)
     */
    public boolean parseAliasThisDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_ALIAS) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() == DlangTypes.ID) {
            if (!tokenCheck(DlangTypes.ID)) {
                bookmark.rollbackTo();
                return false;
            }
            if (!tokenCheck(DlangTypes.KW_THIS)) {
                bookmark.rollbackTo();
                return false;
            }
            expect(DlangTypes.OP_SCOLON);
        }
        else if (myBuilder.getTokenType() == DlangTypes.KW_THIS) {
            myBuilder.advanceLexer();
            expect(DlangTypes.OP_EQ);
            expect(DlangTypes.ID);
            expect(DlangTypes.OP_SCOLON);
        }
        else {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        m.done(DlangTypes.ALIAS_THIS_DECLARATION);
        return true;
    }

    /**
     * Parses an AlignAttribute.
     *
     *
     * $(GRAMMAR $(RULEDEF alignAttribute):
     * $(LITERAL 'align') ($(LITERAL '$(LPAREN)') $(RULE AssignExpression) $(LITERAL '$(RPAREN)'))?
     * ;)
     */
    public boolean parseAlignAttribute() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_ALIGN);
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
                cleanup(m, DlangTypes.ALIGN_ATTRIBUTE);
                return false;
            }
            if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.ALIGN_ATTRIBUTE);
                return false;
            }
            if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
                cleanup(m, DlangTypes.ALIGN_ATTRIBUTE);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ALIGN_ATTRIBUTE, true);
        return true;
    }

    /**
     * Parses an AndAndExpression.
     *
     *
     * $(GRAMMAR $(RULEDEF andAndExpression):
     * $(RULE orExpression)
     * | $(RULE andAndExpression) $(LITERAL '&&') $(RULE orExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseAndAndExpression() {
        PsiBuilder.Marker m = parseOrExpression();
        if (m == null) {
            return null;
        }
        while (currentIs(DlangTypes.OP_BOOL_AND)) {
            m = m.precede();
            myBuilder.advanceLexer();
            if (parseOrExpression() == null) {
                m.done(DlangTypes.AND_AND_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.AND_AND_EXPRESSION);
        }
        return m;
    }

    /**
     * Parses an AndExpression.
     *
     *
     * $(GRAMMAR $(RULEDEF andExpression):
     * $(RULE cmpExpression)
     * | $(RULE andExpression) $(LITERAL '&') $(RULE cmpExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseAndExpression() {
        PsiBuilder.Marker m = parseCmpExpression();
        if (m == null) {
            return null;
        }
        while (currentIs(DlangTypes.OP_AND)) {
            m = m.precede();
            myBuilder.advanceLexer();
            if (parseCmpExpression() == null) {
                m.done(DlangTypes.AND_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.AND_EXPRESSION);
        }
        return m;
    }

    /**
     * Parses an ArgumentList.
     *
     * $(GRAMMAR $(RULEDEF argumentList):
     * $(RULE assignExpression) ($(LITERAL ',') $(RULE assignExpression)?)*
     * ;)
     */
    private boolean parseArgumentList() {
        if (myBuilder.getTokenType() == DlangTypes.OP_PAR_RIGHT) {
            return true;
        }
        PsiBuilder.Marker m = myBuilder.mark();
        while (!myBuilder.eof() && myBuilder.getTokenType() != DlangTypes.OP_PAR_RIGHT) {
            if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.ARGUMENT_LIST);
                return false;
            }
            if (currentIs(DlangTypes.OP_PAR_RIGHT)) {
                break;
            }
            if (!currentIs(DlangTypes.OP_COMMA)) {
                break;
            }
            advance();
        }
        m.done(DlangTypes.ARGUMENT_LIST);
        return true;
    }

    /**
     * Parses a named argument list
     *
     * $(GRAMMAR $(RULEDEF namedArgumentList)):
     * $(RULE namedArgument) ($(LITERAL '$(OP_COMMA)') $(Rule namedArgument)?)?
     */
    private void parseNamedArgumentList() {
        if (currentIs(DlangTypes.OP_PAR_RIGHT)) {
            return;
        }
        PsiBuilder.Marker m = myBuilder.mark();
        while (!myBuilder.eof() && myBuilder.getTokenType() != DlangTypes.OP_PAR_RIGHT) {
            parseNamedArgument();
            if (currentIs(DlangTypes.OP_PAR_RIGHT)) {
                break;
            }
            if (!currentIs(DlangTypes.OP_COMMA)) {
                break;
            }
            advance();
        }
        m.done(DlangTypes.NAMED_ARGUMENT_LIST);
    }

    /**
     * Parses a named argument
     *
     * $(GRAMMAR $(RULEDEF namedArgument)):
     * $(LITERAL Identifier) $(LITERAL '$(OP_COLON)') $(Rule assignExpression)
     */
    private void parseNamedArgument() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (myBuilder.getTokenType() == DlangTypes.ID && myBuilder.lookAhead(1) == DlangTypes.OP_COLON) {
            myBuilder.advanceLexer();
            myBuilder.advanceLexer();
        }
        if (parseAssignExpression() == null) {
            argumentRecover();
        }
        m.done(DlangTypes.NAMED_ARGUMENT);
    }

    private void argumentRecover() {
        PsiBuilder.Marker recovery = myBuilder.mark();
        while (!myBuilder.eof()) {
            if (myBuilder.getTokenType() == DlangTypes.OP_COMMA || myBuilder.getTokenType() == DlangTypes.OP_PAR_LEFT) {
                break;
            }
            myBuilder.advanceLexer();
        }
        recovery.error(LocalizeValue.localizeTODO("Assign expression expected"));
    }

    /**
     * Parses an ArrayInitializer.
     *
     *
     * $(GRAMMAR $(RULEDEF arrayInitializer):
     * $(LITERAL '[') ($(RULE arrayMemberInitializer) ($(LITERAL ',') $(RULE arrayMemberInitializer)?)*)? $(LITERAL ']')
     * ;)
     */
    public boolean parseArrayInitializer() {
        PsiBuilder.Marker m = myBuilder.mark();
        assert myBuilder.getTokenType() == DlangTypes.OP_BRACKET_LEFT;
        myBuilder.advanceLexer();
        boolean r = true;
        while (!myBuilder.eof() && myBuilder.getTokenType() != DlangTypes.OP_BRACKET_RIGHT) {
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (!parseArrayMemberInitialization()) {
                bookmark.rollbackTo();
                initializerRecover("Invalid array initializer", DlangTypes.OP_BRACKET_LEFT, DlangTypes.OP_BRACKET_RIGHT);
                r = false;
                break;
            }
            bookmark.drop();
            if (myBuilder.getTokenType() == DlangTypes.OP_COMMA) {
                myBuilder.advanceLexer();
            }
            else {
                break;
            }
        }
        expect(DlangTypes.OP_BRACKET_RIGHT);
        m.done(DlangTypes.ARRAY_INITIALIZER);
        return r;
    }

    /**
     * Parses an ArrayLiteral.
     *
     *
     * $(GRAMMAR $(RULEDEF arrayLiteral):
     * $(LITERAL '[') $(RULE ArrayMemberInitiolizations)? $(LITERAL ']')
     * ;)
     */
    public void parseArrayLiteral() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType open = expect(DlangTypes.OP_BRACKET_LEFT);
        if (open == null) {
            m.done(DlangTypes.ARRAY_LITERAL);
            return;
        }
        while (!myBuilder.eof() && myBuilder.getTokenType() != DlangTypes.OP_BRACKET_RIGHT) {
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (parseAssignExpression() == null) {
                bookmark.rollbackTo();
                initializerRecover("Invalid array initializer", DlangTypes.OP_BRACKET_LEFT, DlangTypes.OP_BRACKET_RIGHT);
                m.done(DlangTypes.ARRAY_LITERAL);
                return;
            }
            bookmark.drop();
            if (myBuilder.getTokenType() == DlangTypes.OP_COMMA) {
                myBuilder.advanceLexer();
            }
            else {
                break;
            }
        }
        if (myBuilder.getTokenType() != DlangTypes.OP_BRACKET_RIGHT) {
            initializerRecover("] expected", DlangTypes.OP_BRACKET_LEFT, DlangTypes.OP_BRACKET_RIGHT);
        }
        if (!myBuilder.eof()) {
            myBuilder.advanceLexer();
        }
        m.done(DlangTypes.ARRAY_LITERAL);
    }

    /**
     * Parses an ArrayMemberInitialization.
     *
     *
     * $(GRAMMAR $(RULEDEF arrayMemberInitialization):
     * ($RULE nonVoidInitializer)
     * | ($(RULE assignExpression) $(LITERAL ':'))? $(RULE nonVoidInitializer)
     * ;)
     */
    public boolean parseArrayMemberInitialization() {
        PsiBuilder.Marker m = myBuilder.mark();
        PsiBuilder.Marker bookmark = myBuilder.mark();
        if (!parseNonVoidInitializer()) {
            bookmark.rollbackTo();
            boolean r = parseExpression();
            m.done(DlangTypes.ARRAY_MEMBER_INITIALIZER);
            return r;
        }
        bookmark.drop();

        if (myBuilder.getTokenType() == DlangTypes.OP_COLON) {
            myBuilder.advanceLexer();
            boolean r = parseNonVoidInitializer();
            m.done(DlangTypes.ARRAY_MEMBER_INITIALIZER);
            return r;
        }
        m.done(DlangTypes.ARRAY_MEMBER_INITIALIZER);
        return true;
    }

    /**
     * Parses an AsmAddExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmAddExp):
     * $(RULE asmMulExp)
     * | $(RULE asmAddExp) ($(LITERAL '+') | $(LITERAL '-')) $(RULE asmMulExp)
     * ;)
     */
    public boolean parseAsmAddExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmMulExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIsOneOf(DlangTypes.OP_PLUS, DlangTypes.OP_MINUS)) {
            advance();
            if (!parseAsmMulExp()) {
                cleanup(m, DlangTypes.ASM_ADD_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_ADD_EXP, true);
        return true;
    }

    /**
     * Parses an AsmAndExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmAndExp):
     * $(RULE asmEqualExp)
     * | $(RULE asmAndExp) $(LITERAL '&') $(RULE asmEqualExp)
     * ;)
     */
    public boolean parseAsmAndExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmEqualExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIs(DlangTypes.OP_AND)) {
            advance();
            if (!parseAsmEqualExp()) {
                cleanup(m, DlangTypes.ASM_AND_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_AND_EXP, true);
        return true;
    }

    /**
     * Parses an AsmBrExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmBrExp):
     * $(RULE asmUnaExp)
     * | $(RULE asmBrExp)? $(LITERAL '[') $(RULE asmExp) $(LITERAL ']')
     * ;)
     */
    public boolean parseAsmBrExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!moreTokens()) {
            error("Found end-of-file when expecting an AsmBrExp" /*, false*/);
            cleanup(m, DlangTypes.ASM_BR_EXP);
            return false;
        }
        if (currentIs(DlangTypes.OP_BRACKET_LEFT)) {
            advance(); // [
            if (!parseAsmExp()) {
                cleanup(m, DlangTypes.ASM_BR_EXP);
                return false;
            }
            if (!tokenCheck(DlangTypes.OP_BRACKET_RIGHT)) {
                cleanup(m, DlangTypes.ASM_BR_EXP);
                return false;
            }
            if (currentIs(DlangTypes.OP_BRACKET_LEFT)) {
                while (currentIs(DlangTypes.OP_BRACKET_LEFT)) {
                    advance(); // [
                    if (!parseAsmExp()) {
                        cleanup(m, DlangTypes.ASM_BR_EXP);
                        return false;
                    }
                    if (!tokenCheck(DlangTypes.OP_BRACKET_RIGHT)) {
                        cleanup(m, DlangTypes.ASM_BR_EXP);
                        return false;
                    }
                }
            }
        }
        else {
            if (!parseAsmUnaExp()) {
                cleanup(m, DlangTypes.ASM_BR_EXP);
                return false;
            }
            while (currentIs(DlangTypes.OP_BRACKET_LEFT)) {
                advance(); // [
                if (!parseAsmExp()) {
                    cleanup(m, DlangTypes.ASM_BR_EXP);
                    return false;
                }
                if (!tokenCheck(DlangTypes.OP_BRACKET_RIGHT)) {
                    cleanup(m, DlangTypes.ASM_BR_EXP);
                    return false;
                }
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_BR_EXP, true);
        return true;
    }

    /**
     * Parses an AsmEqualExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmEqualExp):
     * $(RULE asmRelExp)
     * | $(RULE asmEqualExp) ('==' | '!=') $(RULE asmRelExp)
     * ;)
     */
    public boolean parseAsmEqualExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmRelExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIsOneOf(DlangTypes.OP_EQ_EQ, DlangTypes.OP_NOT_EQ)) {
            advance();
            if (!parseAsmRelExp()) {
                cleanup(m, DlangTypes.ASM_EQUAL_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_EQUAL_EXP, true);
        return true;
    }

    /**
     * Parses an AsmExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmExp):
     * $(RULE asmLogOrExp) ($(LITERAL '?') $(RULE asmExp) $(LITERAL ':') $(RULE asmExp))?
     * ;)
     */
    public boolean parseAsmExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!parseAsmLogOrExp()) {
            cleanup(m, DlangTypes.ASM_EXP);
            return false;
        }
        if (currentIs(DlangTypes.OP_QUEST)) {
            advance();
            if (!parseAsmExp()) {
                cleanup(m, DlangTypes.ASM_EXP);
                return false;
            }
            if (!tokenCheck(DlangTypes.OP_COLON)) {
                cleanup(m, DlangTypes.ASM_EXP);
                return false;
            }
            if (!parseAsmExp()) {
                cleanup(m, DlangTypes.ASM_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_EXP, true);
        return true;
    }

    /**
     * Parses an AsmInstruction
     *
     *
     * $(GRAMMAR $(RULEDEF asmInstruction):
     * $(LITERAL Identifier)
     * | $(LITERAL 'align') $(LITERAL IntegerLiteral)
     * | $(LITERAL 'align') $(LITERAL Identifier)
     * | $(LITERAL Identifier) $(LITERAL ':') $(RULE asmInstruction)
     * | $(LITERAL Identifier) $(RULE operands)
     * | $(LITERAL 'in') $(RULE operands)
     * | $(LITERAL 'out') $(RULE operands)
     * | $(LITERAL 'int') $(RULE operands)
     * | $(LITERAL ';')
     * ;)
     */
    public boolean parseAsmInstruction() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (currentIs(DlangTypes.OP_SCOLON)) {
            myBuilder.advanceLexer();
            m.done(DlangTypes.ASM_INSTRUCTION);
            return true;
        }
        if (currentIs(DlangTypes.KW_ALIGN)) {
            advance(); // align
            if (currentIsOneOf(DlangTypes.INTEGER_LITERAL, DlangTypes.ID)) {
                if (!currentIs(DlangTypes.OP_SCOLON)) {
                    myBuilder.error(LocalizeValue.localizeTODO("`;` expected."));
                    m.done(DlangTypes.ASM_INSTRUCTION);
                    return false;
                }
                myBuilder.advanceLexer();
            }
            else {
                error("Identifier or integer literal expected.");
                m.done(DlangTypes.ASM_INSTRUCTION);
                return false;
            }
        }
        else if (currentIsOneOf(DlangTypes.ID, DlangTypes.KW_IN, DlangTypes.KW_OUT, DlangTypes.KW_INT)) {
            IElementType t = advance();
            if (t == DlangTypes.ID && currentIs(DlangTypes.OP_COLON)) {
                advance(); // :
                if (currentIs(DlangTypes.OP_SCOLON)) {
                    myBuilder.advanceLexer();
                    m.done(DlangTypes.ASM_INSTRUCTION);
                    return true;
                }
                if (!parseAsmInstruction()) {
                    m.done(DlangTypes.ASM_INSTRUCTION);
                    return false;
                }
            }
            else if (!currentIs(DlangTypes.OP_SCOLON)) {
                if (!parseOperands()) {
                    m.done(DlangTypes.ASM_INSTRUCTION);
                    return false;
                }
            }
            myBuilder.advanceLexer();
        }
        else {
            m.rollbackTo();
            return false;
        }
        m.done(DlangTypes.ASM_INSTRUCTION);
        return true;
    }

    /**
     * Parses an AsmLogAndExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmLogAndExp):
     * $(RULE asmOrExp)
     * | $(RULE asmLogAndExp) $(LITERAL '&&') $(RULE asmOrExp)
     * ;)
     */
    public boolean parseAsmLogAndExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmOrExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIs(DlangTypes.OP_BOOL_AND)) {
            advance();
            if (!parseAsmOrExp()) {
                cleanup(m, DlangTypes.ASM_LOG_AND_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_LOG_AND_EXP, true);
        return true;
    }

    /**
     * Parses an AsmLogOrExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmLogOrExp):
     * $(RULE asmLogAndExp)
     * | $(RULE asmLogOrExp) '||' $(RULE asmLogAndExp)
     * ;)
     */
    public boolean parseAsmLogOrExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmLogAndExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIs(DlangTypes.OP_BOOL_OR)) {
            advance();
            if (!parseAsmLogAndExp()) {
                cleanup(m, DlangTypes.ASM_LOG_OR_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_LOG_OR_EXP, true);
        return true;
    }

    /**
     * Parses an AsmMulExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmMulExp):
     * $(RULE asmBrExp)
     * | $(RULE asmMulExp) ($(LITERAL '*') | $(LITERAL '/') | $(LITERAL '%')) $(RULE asmBrExp)
     * ;)
     */
    public boolean parseAsmMulExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmBrExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIsOneOf(DlangTypes.OP_ASTERISK, DlangTypes.OP_DIV, DlangTypes.OP_MOD)) {
            advance();
            if (!parseAsmBrExp()) {
                cleanup(m, DlangTypes.ASM_MUL_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_MUL_EXP, true);
        return true;
    }

    /**
     * Parses an AsmOrExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmOrExp):
     * $(RULE asmXorExp)
     * | $(RULE asmOrExp) $(LITERAL '|') $(RULE asmXorExp)
     * ;)
     */
    public boolean parseAsmOrExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmXorExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIs(DlangTypes.OP_OR)) {
            advance();
            if (!parseAsmXorExp()) {
                cleanup(m, DlangTypes.ASM_OR_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_OR_EXP, true);
        return true;
    }

    /**
     * Parses an AsmPrimaryExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmPrimaryExp):
     * $(LITERAL IntegerLiteral)
     * | $(LITERAL FloatLiteral)
     * | $(LITERAL StringLiteral)
     * | $(RULE register)
     * | $(RULE register : AsmExp)
     * | $(RULE identifierChain)
     * | $(LITERAL '$')
     * | $(LITERAL 'this')
     * | $(LITERAL '__LOCAL_SIZE')
     * ;)
     */
    public boolean parseAsmPrimaryExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.FLOAT_LITERAL || i == DlangTypes.INTEGER_LITERAL || i == DlangTypes.DOUBLE_QUOTED_STRING || i == DlangTypes.OP_DOLLAR || i == DlangTypes.KW_THIS) {
            advance();
        }
        else if (i == DlangTypes.ID) {
            if (REGISTER_NAMES.contains(myBuilder.getTokenText())) {
                if (!parseRegister()) {
                    cleanup(m, DlangTypes.ASM_PRIMARY_EXP);
                    return false;
                }
                if (current() == DlangTypes.OP_COLON) {
                    advance();
                    if (!parseAsmExp()) {
                        cleanup(m, DlangTypes.ASM_PRIMARY_EXP);
                        return false;
                    }
                }
            }
            else {
                if (parseIdentifierChain() == null) {
                    cleanup(m, DlangTypes.ASM_PRIMARY_EXP);
                    return false;
                }
            }
        }
        else {
            error("Float literal, integer literal, `$` `this` or identifier expected.");
            cleanup(m, DlangTypes.ASM_PRIMARY_EXP);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_PRIMARY_EXP, true);
        return true;
    }

    /**
     * Parses an AsmRelExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmRelExp):
     * $(RULE asmShiftExp)
     * | $(RULE asmRelExp) (($(LITERAL '<') | $(LITERAL '<=') | $(LITERAL '>') | $(LITERAL '>=')) $(RULE asmShiftExp))?
     * ;)
     */
    public boolean parseAsmRelExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmShiftExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIsOneOf(DlangTypes.OP_LESS, DlangTypes.OP_LESS_EQ, DlangTypes.OP_GT, DlangTypes.OP_GT_EQ)) {
            advance();
            if (!parseAsmShiftExp()) {
                cleanup(m, DlangTypes.ASM_REL_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_REL_EXP, true);
        return true;
    }

    /**
     * Parses an AsmShiftExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmShiftExp):
     * $(RULE asmAddExp)
     * $(RULE asmShiftExp) ($(LITERAL '<<') | $(LITERAL '>>') | $(LITERAL '>>>')) $(RULE asmAddExp)
     * ;)
     */
    public boolean parseAsmShiftExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmAddExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIsOneOf(DlangTypes.OP_SH_LEFT, DlangTypes.OP_SH_RIGHT, DlangTypes.OP_USH_RIGHT)) {
            advance();
            if (!parseAsmAddExp()) {
                cleanup(m, DlangTypes.ASM_SHIFT_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_SHIFT_EXP, true);
        return true;
    }

    /**
     * Parses an AsmStatement
     *
     *
     * $(GRAMMAR $(RULEDEF asmStatement):
     * $(LITERAL 'asm') $(RULE functionAttributes)? $(LITERAL '{') $(RULE asmInstruction)+ $(LITERAL '}')
     * ;)
     */
    public boolean parseAsmStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        advance(); // asm
        while (isAttribute()) {
            if (!parseFunctionAttribute()) {
                error("Function attribute or '{' expected");
                cleanup(m, DlangTypes.ASM_STATEMENT);
                return false;
            }
        }
        expect(DlangTypes.OP_BRACES_LEFT);
        while (moreTokens() && !currentIs(DlangTypes.OP_BRACES_RIGHT)) {
            if (!parseAsmInstruction()) {
                // TODO: here libdparse handle gcc asm instructions
                while (myBuilder.getTokenType() != DlangTypes.OP_BRACES_RIGHT) {
                    myBuilder.advanceLexer();
                }
                break;
            }
        }
        expect(DlangTypes.OP_BRACES_RIGHT);
        exit_section_modified(myBuilder, m, DlangTypes.ASM_STATEMENT, true);
        return true;
    }

    /**
     * Parses an AsmTypePrefix
     *
     *
     * Note that in the following grammar definition the first identifier must
     * be "near", "far", "word", "dword", or "qword". The second identifier must
     * be "ptr".
     *
     *
     * $(GRAMMAR $(RULEDEF asmTypePrefix):
     * $(LITERAL Identifier) $(LITERAL Identifier)?
     * | $(LITERAL 'byte') $(LITERAL Identifier)?
     * | $(LITERAL 'short') $(LITERAL Identifier)?
     * | $(LITERAL 'int') $(LITERAL Identifier)?
     * | $(LITERAL 'float') $(LITERAL Identifier)?
     * | $(LITERAL 'double') $(LITERAL Identifier)?
     * | $(LITERAL 'real') $(LITERAL Identifier)?
     * ;)
     */
    public boolean parseAsmTypePrefix() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.ID || i == DlangTypes.KW_BYTE || i == DlangTypes.KW_SHORT || i == DlangTypes.KW_INT || i == DlangTypes.KW_FLOAT || i == DlangTypes.KW_DOUBLE || i == DlangTypes.KW_REAL) {
            String tokenText = myBuilder.getTokenText();
            IElementType t = advance();
            if (t == DlangTypes.ID) {
                if ("near".equals(tokenText) || "far".equals(tokenText) || "word".equals(tokenText)
                    || "dword".equals(tokenText) || "qword".equals(tokenText)) {
                }
                else {
                    error("ASM type node expected");
                    exit_section_modified(myBuilder, m, DlangTypes.ASM_TYPE_PREFIX, true); //todo
                    return false;
                }
            }
            if (currentIs(DlangTypes.ID) && "ptr".equals(myBuilder.getTokenText())) {
                advance();
            }
            exit_section_modified(myBuilder, m, DlangTypes.ASM_TYPE_PREFIX, true);
            return true;
        }
        else {
            error("Expected an identifier, 'byte', 'short', 'int', 'float', 'double', or 'real'");
            cleanup(m, DlangTypes.ASM_TYPE_PREFIX);
            return false;
        }
    }

    /**
     * Parses an AsmUnaExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmUnaExp):
     * $(RULE asmTypePrefix) $(RULE asmExp)
     * | $(LITERAL Identifier) $(RULE asmExp)
     * | $(LITERAL '+') $(RULE asmUnaExp)
     * | $(LITERAL '-') $(RULE asmUnaExp)
     * | $(LITERAL '!') $(RULE asmUnaExp)
     * | $(LITERAL '~') $(RULE asmUnaExp)
     * | $(RULE asmPrimaryExp)
     * ;)
     */
    public boolean parseAsmUnaExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.OP_PLUS || i == DlangTypes.OP_MINUS || i == DlangTypes.OP_NOT || i == DlangTypes.OP_TILDA) {
            advance();
            if (!parseAsmUnaExp()) {
                cleanup(m, DlangTypes.ASM_UNA_EXP);
                return false;
            }
        }
        else if (i == DlangTypes.KW_BYTE || i == DlangTypes.KW_SHORT || i == DlangTypes.KW_INT || i == DlangTypes.KW_FLOAT || i == DlangTypes.KW_DOUBLE || i == DlangTypes.KW_REAL) {
            if (!typePrefix(m)) {
                return false;
            }
        }
        else if (i == DlangTypes.ID) {
            String tokenText = myBuilder.getTokenText();
            if ("offsetof".equals(tokenText) || "seg".equals(tokenText)) {
                advance();
                if (!parseAsmExp()) {
                    cleanup(m, DlangTypes.ASM_UNA_EXP);
                    return false;
                }
            }
            else if ("near".equals(tokenText) || "far".equals(tokenText) || "word".equals(tokenText)
                || "dword".equals(tokenText) || "qword".equals(tokenText)) {
                if (!typePrefix(m)) {
                    return false;
                }
            }
            else {
                if (!outerDefault(m)) {
                    return false;
                }
            }
        }
        else {
            if (!outerDefault(m)) {
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_UNA_EXP, true);
        return true;
    }

    private boolean outerDefault(PsiBuilder.Marker m) {
        if (!parseAsmPrimaryExp()) {
            cleanup(m, DlangTypes.ASM_UNA_EXP);
            return false;
        }
        return true;
    }

    private boolean typePrefix(PsiBuilder.Marker m) {
        if (!parseAsmTypePrefix()) {
            cleanup(m, DlangTypes.ASM_UNA_EXP);
            return false;
        }
        if (!parseAsmExp()) {
            cleanup(m, DlangTypes.ASM_UNA_EXP);
            return false;
        }
        return true;
    }

    /**
     * Parses an AsmXorExp
     *
     *
     * $(GRAMMAR $(RULEDEF asmXorExp):
     * $(RULE asmAndExp)
     * | $(RULE asmXorExp) $(LITERAL '^') $(RULE asmAndExp)
     * ;)
     */
    public boolean parseAsmXorExp() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean node = parseAsmAndExp();
        if (!node) {
            m.drop();
            return false;
        }
        while (currentIs(DlangTypes.OP_XOR)) {
            advance();
            if (!parseAsmAndExp()) {
                cleanup(m, DlangTypes.ASM_XOR_EXP);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.ASM_XOR_EXP, true);
        return true;
    }

    /**
     * Parses an AssertArguments
     *
     * $(GRAMMAR $(RULEDEF assertArguments):
     * $(RULE assignExpression) ($(LITERAL ',') ($(RULE assignExpression))* $(LITERAL ',')?
     * ;)
     */
    public boolean parseAssertArguments() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (parseAssignExpression() == null) {
            m.done(DlangTypes.ASSERT_ARGUMENTS);
            return false;
        }
        if (currentIs(DlangTypes.OP_COMMA)) {
            advance();
        }
        if (currentIs(DlangTypes.OP_PAR_RIGHT)) {
            m.done(DlangTypes.ASSERT_ARGUMENTS);
            return true;
        }
        while (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
            if (parseAssignExpression() == null) {
                m.done(DlangTypes.ASSERT_ARGUMENTS);
                return false;
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
            }
        }
        m.done(DlangTypes.ASSERT_ARGUMENTS);
        return true;
    }

    /**
     * Parses an AssertExpression
     *
     *
     * $(GRAMMAR $(RULEDEF assertExpression):
     * $(LITERAL 'assert') $(LITERAL '$(LPAREN)') $(RULE assertArguments) ($(LITERAL ',') $(RULE assignExpression))? $(LITERAL ',')? $(LITERAL '$(RPAREN)')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseAssertExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        advance(); // "assert"
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            m.done(DlangTypes.ASSERT_EXPRESSION);
            return null;
        }
        if (!parseAssertArguments()) {
            m.done(DlangTypes.ASSERT_EXPRESSION);
            return null;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            m.done(DlangTypes.ASSERT_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.ASSERT_EXPRESSION);
        return m;
    }

    /**
     * Parses an AssignExpression
     *
     *
     * $(GRAMMAR $(RULEDEF assignExpression):
     * $(RULE ternaryExpression) ($(RULE assignOperator) $(RULE assignExpression))?
     * ;
     * $(RULEDEF assignOperator):
     * $(LITERAL '=')
     * | $(LITERAL '>>>=')
     * | $(LITERAL '>>=')
     * | $(LITERAL '<<=')
     * | $(LITERAL '+=')
     * | $(LITERAL '-=')
     * | $(LITERAL '*=')
     * | $(LITERAL '%=')
     * | $(LITERAL '&=')
     * | $(LITERAL '/=')
     * | $(LITERAL '|=')
     * | $(LITERAL '^^=')
     * | $(LITERAL '^=')
     * | $(LITERAL '~=')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseAssignExpression() {
        PsiBuilder.Marker ternary = parseTernaryExpression();
        if (ternary == null) {
            return null;
        }
        if (currentIsOneOf(
            DlangTypes.OP_EQ,
            DlangTypes.OP_USH_RIGHT_EQ,
            DlangTypes.OP_SH_RIGHT_EQ,
            DlangTypes.OP_SH_LEFT_EQ,
            DlangTypes.OP_PLUS_EQ,
            DlangTypes.OP_MINUS_EQ,
            DlangTypes.OP_MUL_EQ,
            DlangTypes.OP_MOD_EQ,
            DlangTypes.OP_AND_EQ,
            DlangTypes.OP_DIV_EQ,
            DlangTypes.OP_OR_EQ,
            DlangTypes.OP_POW_EQ,
            DlangTypes.OP_XOR_EQ,
            DlangTypes.OP_TILDA_EQ
        )) {
            PsiBuilder.Marker m = ternary.precede();
            advance();
            if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.ASSIGN_EXPRESSION);
                return m;
            }
            exit_section_modified(myBuilder, m, DlangTypes.ASSIGN_EXPRESSION, true);
            return m;
        }
        return ternary;
    }

    /**
     * Parses an AssocArrayLiteral
     *
     *
     * $(GRAMMAR $(RULEDEF assocArrayLiteral):
     * $(LITERAL '[') $(RULE keyValuePairs) $(LITERAL ']')
     * ;)
     */
    public void parseAssocArrayLiteral() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (expect(DlangTypes.OP_BRACKET_LEFT) == null) {
            m.done(DlangTypes.ASSOC_ARRAY_LITERAL);
            return;
        }
        if (!parseKeyValuePairs()) {
            m.done(DlangTypes.ASSOC_ARRAY_LITERAL);
            return;
        }
        if (expect(DlangTypes.OP_BRACKET_RIGHT) == null) {
            m.done(DlangTypes.ASSOC_ARRAY_LITERAL);
            return;
        }
        m.done(DlangTypes.ASSOC_ARRAY_LITERAL);
    }

    /**
     * Parses an AtAttribute
     *
     *
     * $(GRAMMAR $(RULEDEF atAttribute):
     * $(LITERAL '@') $(LITERAL Identifier)
     * | $(LITERAL '@') $(LITERAL Identifier) $(LITERAL '$(LPAREN)') $(RULE argumentList)? $(LITERAL '$(RPAREN)')
     * | $(LITERAL '@') $(LITERAL '$(LPAREN)') $(RULE argumentList) $(LITERAL '$(RPAREN)')
     * | $(LITERAL '@') $(RULE TemplateInstance)
     * ;)
     */
    public boolean parseAtAttribute() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType start = expect(DlangTypes.OP_AT);
        if (start == null) {
            m.drop();
            return false;
        }
        IElementType i = current();
        if (i == DlangTypes.ID) {
            if (peekIs(DlangTypes.OP_NOT)) {
                if (!parseTemplateInstance()) {
                    m.done(DlangTypes.AT_ATTRIBUTE);
                    return false;
                }
            }
            else {
                advance();
            }

            if (currentIs(DlangTypes.OP_PAR_LEFT)) {
                advance(); // (
                if (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
                    if (!parseArgumentList()) {
                        m.done(DlangTypes.AT_ATTRIBUTE);
                        return false;
                    }
                }
                expect(DlangTypes.OP_PAR_RIGHT);
            }
        }
        else if (i == DlangTypes.OP_PAR_LEFT) {
            advance();
            if (!parseArgumentList()) {
                cleanup(m, DlangTypes.AT_ATTRIBUTE);
                return false;
            }
            expect(DlangTypes.OP_PAR_RIGHT);
        }
        else {
            myBuilder.error(LocalizeValue.localizeTODO("`(`, or identifier expected"));
        }
        m.done(DlangTypes.AT_ATTRIBUTE);
        return true;
    }

    /**
     * Parses an Attribute
     *
     *
     * $(GRAMMAR $(RULEDEF attribute):
     * $(RULE pragmaExpression)
     * | $(RULE alignAttribute)
     * | $(RULE deprecated)
     * | $(RULE atAttribute)
     * | $(RULE linkageAttribute)
     * | $(LITERAL 'export')
     * | $(LITERAL 'package') ($(LITERAL "(") $(RULE identifierChain) $(LITERAL ")"))?
     * | $(LITERAL 'private')
     * | $(LITERAL 'protected')
     * | $(LITERAL 'public')
     * | $(LITERAL 'static')
     * | $(LITERAL 'extern')
     * | $(LITERAL 'abstract')
     * | $(LITERAL 'final')
     * | $(LITERAL 'override')
     * | $(LITERAL 'synchronized')
     * | $(LITERAL 'auto')
     * | $(LITERAL 'scope')
     * | $(LITERAL 'const')
     * | $(LITERAL 'immutable')
     * | $(LITERAL 'inout')
     * | $(LITERAL 'shared')
     * | $(LITERAL '__gshared')
     * | $(LITERAL 'nothrow')
     * | $(LITERAL 'pure')
     * | $(LITERAL 'ref')
     * | $(LITERAL 'throw')
     * ;)
     */
    public boolean parseAttribute() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.KW_PRAGMA) {
            if (!parsePragmaExpression()) {
                cleanup(m, DlangTypes.ATTRIBUTE);
                return false;
            }
        }
        else if (i == DlangTypes.KW_DEPRECATED) {
            if (!parseDeprecated()) {
                cleanup(m, DlangTypes.ATTRIBUTE);
                return false;
            }
        }
        else if (i == DlangTypes.KW_ALIGN) {
            if (!parseAlignAttribute()) {
                cleanup(m, DlangTypes.ATTRIBUTE);
                return false;
            }
        }
        else if (i == DlangTypes.OP_AT) {
            if (!parseAtAttribute()) {
                cleanup(m, DlangTypes.ATTRIBUTE);
                return false;
            }
        }
        else if (i == DlangTypes.KW_PACKAGE) {
            advance();
            if (currentIs(DlangTypes.OP_PAR_LEFT)) {
                expect(DlangTypes.OP_PAR_LEFT);
                if (parseIdentifierChain() == null) {
                    cleanup(m, DlangTypes.ATTRIBUTE);
                    return false;
                }
                expect(DlangTypes.OP_PAR_RIGHT);
            }
        }
        else if (i == DlangTypes.KW_EXTERN) {
            if (peekIs(DlangTypes.OP_PAR_LEFT)) {
                if (!parseLinkageAttribute()) {
                    cleanup(m, DlangTypes.ATTRIBUTE);
                    return false;
                }
            }
            else {
                advance();
            }
        }
        else if (i == DlangTypes.KW_CONST || i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_INOUT || i == DlangTypes.KW_SHARED) {
            if (peekIs(DlangTypes.OP_PAR_LEFT)) {
                m.rollbackTo();
                return false;
            }
            myBuilder.advanceLexer();
            if (myBuilder.getTokenType() == DlangTypes.ID) {
                m.rollbackTo();
                return false;
            }
        }
        else if (i == DlangTypes.KW_PRIVATE || i == DlangTypes.KW_PROTECTED || i == DlangTypes.KW_PUBLIC || i == DlangTypes.KW_EXPORT || i == DlangTypes.KW_STATIC || i == DlangTypes.KW_ABSTRACT || i == DlangTypes.KW_FINAL || i == DlangTypes.KW_OVERRIDE || i == DlangTypes.KW_SYNCHRONIZED || i == DlangTypes.KW_AUTO || i == DlangTypes.KW_SCOPE || i == DlangTypes.KW___GSHARED || i == DlangTypes.KW_NOTHROW || i == DlangTypes.KW_PURE || i == DlangTypes.KW_REF || i == DlangTypes.KW_THROW) {
            boolean isStorageClass = isStorageClass();
            advance();
            if (isStorageClass && myBuilder.getTokenType() == DlangTypes.ID) {
                m.rollbackTo();
                return false;
            }
        }
        else {
            m.drop();
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.ATTRIBUTE, true);
        return true;
    }

    public void parseAutoAssignments() {
        do {
            if (!parseAutoDeclarationPart()) {
                return;
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
            }
            else {
                break;
            }
        }
        while (moreTokens());
    }

    /**
     * Parses an AutoDeclarationPart
     *
     *
     * $(GRAMMAR $(RULEDEF autoDeclarationPart):
     * $(LITERAL Identifier) $(RULE templateParameters)? $(LITERAL '=') $(RULE initializer)
     * ;)
     */
    public boolean parseAutoDeclarationPart() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = expect(DlangTypes.ID);
        if (i == null) {
            cleanup(m, DlangTypes.AUTO_ASSIGNMENT);
            return false;
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            if (!parseTemplateParameters()) {
                cleanup(m, DlangTypes.AUTO_ASSIGNMENT);
                return false;
            }
        }
        if (!tokenCheck(DlangTypes.OP_EQ)) {
            cleanup(m, DlangTypes.AUTO_ASSIGNMENT);
            return false;
        }
        if (!parseInitializer()) {
            cleanup(m, DlangTypes.AUTO_ASSIGNMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.AUTO_ASSIGNMENT, true);
        return true;
    }

    public boolean parseBlockStatement() {
        if (myBuilder.getTokenType() != DlangTypes.OP_BRACES_LEFT) {
            return false;
        }
        PsiBuilder.Marker marker = parseBlockLazy(myBuilder, DlangTypes.OP_BRACES_LEFT, DlangTypes.OP_BRACES_RIGHT, DlangTypes.BLOCK_STATEMENT);
        return marker != null;
    }

    /**
     * Parses a BlockStatement
     *
     *
     * $(GRAMMAR $(RULEDEF blockStatement):
     * $(LITERAL '{') $(RULE statement)* $(LITERAL '}')
     * ;)
     */
    public boolean parseBlockStatementDeep() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType openBrace = expect(DlangTypes.OP_BRACES_LEFT);
        if (openBrace == null) {
            m.done(DlangTypes.BLOCK_STATEMENT);
            return false;
        }
        while (!myBuilder.eof() && myBuilder.getTokenType() != DlangTypes.OP_BRACES_RIGHT) {
            if (!parseStatement()) {
                recover("Unable to parse this statement");
            }
        }
        expect(DlangTypes.OP_BRACES_RIGHT);
        m.done(DlangTypes.BLOCK_STATEMENT);
        return true;
    }

    /**
     * Parses a BreakStatement
     *
     *
     * $(GRAMMAR $(RULEDEF breakStatement):
     * $(LITERAL 'break') $(LITERAL Identifier)? $(LITERAL ';')
     * ;)
     */
    public boolean parseBreakStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_BREAK);
        if (!moreTokens()) {
            cleanup(m, DlangTypes.BREAK_STATEMENT);
            return false;
        }
        IElementType i = current();
        if (i == DlangTypes.ID) {
            advance();
            if (!tokenCheck(DlangTypes.OP_SCOLON)) {
                cleanup(m, DlangTypes.BREAK_STATEMENT);
                return false;
            }
        }
        else if (i == DlangTypes.OP_SCOLON) {
            advance();
        }
        else {
            error("Identifier or semicolon expected following `break`");
            exit_section_modified(myBuilder, m, DlangTypes.BREAK_STATEMENT, true);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.BREAK_STATEMENT, true);
        return true;
    }

    /**
     * Parses a BaseClass
     *
     *
     * $(GRAMMAR $(RULEDEF baseClass):
     * $(RULE basicType)
     * ;)
     */
    public boolean parseBaseClass() {
        return parseBasicType();
    }

    /**
     * Parses a BaseClassList
     *
     *
     * $(GRAMMAR $(RULEDEF baseClassList):
     * $(RULE baseClass) ($(LITERAL ',') $(RULE baseClass))*
     * ;)
     */
    public void parseBaseClassList() {
        PsiBuilder.Marker m = myBuilder.mark();
        while (!myBuilder.eof()) {
            if (!parseBaseClass()) {
                break;
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                myBuilder.advanceLexer();
            }
            else {
                break;
            }
        }
        m.done(DlangTypes.BASE_CLASS_LIST);
    }

    /**
     * Parses an BuiltinType
     *
     *
     * $(GRAMMAR $(RULEDEF builtinType):
     * $(LITERAL 'bool')
     * | $(LITERAL 'byte')
     * | $(LITERAL 'ubyte')
     * | $(LITERAL 'short')
     * | $(LITERAL 'ushort')
     * | $(LITERAL 'int')
     * | $(LITERAL 'int')
     * | $(LITERAL 'long')
     * | $(LITERAL 'ulong')
     * | $(LITERAL 'char')
     * | $(LITERAL 'wchar')
     * | $(LITERAL 'dchar')
     * | $(LITERAL 'float')
     * | $(LITERAL 'double')
     * | $(LITERAL 'real')
     * | $(LITERAL 'ifloat')
     * | $(LITERAL 'idouble')
     * | $(LITERAL 'ireal')
     * | $(LITERAL 'cfloat')
     * | $(LITERAL 'cdouble')
     * | $(LITERAL 'creal')
     * | $(LITERAL 'void')
     * ;)
     */
    private IElementType parseBuiltinType() {
        assert isBuiltinType(myBuilder.getTokenType());
        PsiBuilder.Marker marker = myBuilder.mark();
        IElementType type = Objects.requireNonNull(myBuilder.getTokenType());
        myBuilder.advanceLexer();
        marker.done(DlangTypes.BUILTIN_TYPE);
        return type;
    }

    /**
     * Parses a CaseRangeStatement
     *
     *
     * $(GRAMMAR $(RULEDEF caseRangeStatement):
     * $(LITERAL 'case') $(RULE assignExpression) $(LITERAL ':') $(LITERAL '...') $(LITERAL 'case') $(RULE assignExpression) $(LITERAL ':') $(RULE scopeStatementList)?
     * ;)
     */
    public boolean parseCaseRangeStatement(PsiBuilder.Marker m) {
        if (!tokenCheck(DlangTypes.OP_COLON)) {
            cleanup(m, DlangTypes.CASE_RANGE_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_DDOT)) {
            cleanup(m, DlangTypes.CASE_RANGE_STATEMENT);
            return false;
        }
        expect(DlangTypes.KW_CASE);
        if (parseAssignExpression() == null) {
            cleanup(m, DlangTypes.CASE_RANGE_STATEMENT);
            return false;
        }
        IElementType colon = expect(DlangTypes.OP_COLON);
        if (colon == null) {
            cleanup(m, DlangTypes.CASE_RANGE_STATEMENT);
            return false;
        }
        if (!parseScopeStatementList()) {
            cleanup(m, DlangTypes.CASE_RANGE_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.CASE_RANGE_STATEMENT, true);
        return true;
    }

    /**
     * Parses an CaseStatement
     *
     *
     * $(GRAMMAR $(RULEDEF caseStatement):
     * $(LITERAL 'case') $(RULE argumentList) $(LITERAL ':') $(RULE scopeArgumentList)?
     * ;)
     */
    public boolean parseCaseStatement(PsiBuilder.Marker m) {
        IElementType colon = expect(DlangTypes.OP_COLON);
        if (colon == null) {
            cleanup(m, DlangTypes.CASE_STATEMENT);
            return false;
        }
        if (!parseScopeStatementList()) {
            cleanup(m, DlangTypes.CASE_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.CASE_STATEMENT, true);
        return true;
    }

    /**
     * Parses a CastExpression
     *
     *
     * $(GRAMMAR $(RULEDEF castExpression):
     * $(LITERAL 'cast') $(LITERAL '$(LPAREN)') ($(RULE type) | $(RULE castQualifier))? $(LITERAL '$(RPAREN)') $(RULE unaryExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseCastExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_CAST);
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            m.done(DlangTypes.CAST_EXPRESSION);
            return null;
        }
        if (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
            if (isCastQualifier()) {
                if (!parseCastQualifier()) {
                    m.done(DlangTypes.CAST_EXPRESSION);
                    return null;
                }
            }
            else {
                if (!parseType()) {
                    m.done(DlangTypes.CAST_EXPRESSION);
                    return null;
                }
            }
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            m.done(DlangTypes.CAST_EXPRESSION);
            return null;
        }
        if (parseUnaryExpression() == null) {
            m.done(DlangTypes.CAST_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.CAST_EXPRESSION);
        return m;
    }

    /**
     * Parses a CastQualifier
     *
     *
     * $(GRAMMAR $(RULEDEF castQualifier):
     * $(LITERAL 'const')
     * | $(LITERAL 'const') $(LITERAL 'shared')
     * | $(LITERAL 'immutable')
     * | $(LITERAL 'inout')
     * | $(LITERAL 'inout') $(LITERAL 'shared')
     * | $(LITERAL 'shared')
     * | $(LITERAL 'shared') $(LITERAL 'const')
     * | $(LITERAL 'shared') $(LITERAL 'inout')
     * ;)
     */
    public boolean parseCastQualifier() {
        if (!moreTokens()) {
            return false;
        }
        PsiBuilder.Marker marker = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.KW_INOUT || i == DlangTypes.KW_CONST) {
            advance();
            if (currentIs(DlangTypes.KW_SHARED)) {
                advance();
            }
        }
        else if (i == DlangTypes.KW_SHARED) {
            advance();
            if (currentIsOneOf(DlangTypes.KW_CONST, DlangTypes.KW_INOUT)) {
                advance();
            }
        }
        else if (i == DlangTypes.KW_IMMUTABLE) {
            advance();
        }
        else {
            error("`const`, `immutable`, `inout`, or `shared` expected");
            return false;
        }
        exit_section_modified(myBuilder, marker, DlangTypes.CAST_QUALIFIER, true);
        return true;
    }

    /**
     * Parses a Catch
     *
     *
     * $(GRAMMAR $(RULEDEF catch):
     * $(LITERAL 'catch') $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL Identifier)? $(LITERAL '$(RPAREN)') $(RULE declarationOrStatement)
     * ;)
     */
    public boolean parseCatch() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_CATCH);
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.CATCH);
            return false;
        }
        if (!parseType()) {
            cleanup(m, DlangTypes.CATCH);
            return false;
        }
        if (currentIs(DlangTypes.ID)) {
            advance();
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.CATCH);
            return false;
        }
        if (!parseNoScopeNonEmptyStatement()) {
            cleanup(m, DlangTypes.CATCH);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.CATCH, true);
        return true;
    }

    /**
     * Parses a Catches
     *
     *
     * $(GRAMMAR $(RULEDEF catches):
     * $(RULE catch)+
     * | $(RULE catch)* $(RULE lastCatch)
     * ;)
     */
    public boolean parseCatches() {
        PsiBuilder.Marker m = myBuilder.mark();
        while (moreTokens()) {
            if (!currentIs(DlangTypes.KW_CATCH)) {
                break;
            }
            if (peekIs(DlangTypes.OP_PAR_LEFT)) {
                if (!parseCatch()) {
                    cleanup(m, DlangTypes.CATCHES);
                    return false;
                }
            }
            else {
                if (!parseLastCatch()) {
                    cleanup(m, DlangTypes.CATCHES);
                    return false;
                }
                break;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.CATCHES, true);
        return true;
    }

    /**
     * Parses a ClassDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF classDeclaration):
     * $(LITERAL 'class') $(LITERAL Identifier) $(LITERAL ';')
     * | $(LITERAL 'class') $(LITERAL Identifier) ($(LITERAL ':') $(RULE baseClassList))? $(RULE structBody)
     * | $(LITERAL 'class') $(LITERAL Identifier) $(RULE templateParameters) $(RULE consraint)? ($(RULE structBody) | $(LITERAL ';'))
     * | $(LITERAL 'class') $(LITERAL Identifier) $(RULE templateParameters) $(RULE consraint)? ($(LITERAL ':') $(RULE baseClassList))? $(RULE structBody)
     * | $(LITERAL 'class') $(LITERAL Identifier) $(RULE templateParameters) ($(LITERAL ':') $(RULE baseClassList))? $(RULE raint)? $(RULE structBody)
     * ;)
     */
    public boolean parseClassDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_CLASS) {
            return false;
        }
        myBuilder.advanceLexer();
        parseInterfaceOrClass();
        m.done(DlangTypes.CLASS_DECLARATION);
        return true;
    }

    /**
     * Parses a CmpExpression
     *
     *
     * $(GRAMMAR $(RULEDEF cmpExpression):
     * $(RULE shiftExpression)
     * | $(RULE equalExpression)
     * | $(RULE identityExpression)
     * | $(RULE relExpression)
     * | $(RULE inExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseCmpExpression() {
        PsiBuilder.Marker shift = parseShiftExpression();
        if (shift == null) {
            return null;
        }
        IElementType i = current();
        if (i == DlangTypes.KW_IS) {
            return parseIdentityExpression(shift.precede());
        }
        else if (i == DlangTypes.KW_IN) {
            return parseInExpression(shift.precede());
        }
        else if (i == DlangTypes.OP_NOT) {
            if (peekIs(DlangTypes.KW_IS)) {
                return parseIdentityExpression(shift.precede());
            }
            else if (peekIs(DlangTypes.KW_IN)) {
                return parseInExpression(shift.precede());
            }
        }
        else if (i == DlangTypes.OP_LESS || i == DlangTypes.OP_LESS_EQ || i == DlangTypes.OP_GT || i == DlangTypes.OP_GT_EQ || i == DlangTypes.OP_NOT_GR || i == DlangTypes.OP_NOT_GR_EQ || i == DlangTypes.OP_NOT_LESS || i == DlangTypes.OP_NOT_LESS_EQ) {
            return parseRelExpression(shift.precede());
        }
        else if (i == DlangTypes.OP_EQ_EQ || i == DlangTypes.OP_NOT_EQ) {
            return parseEqualExpression(shift.precede());
        }
        return shift;
    }

    /**
     * Parses a CompileCondition
     *
     *
     * $(GRAMMAR $(RULEDEF compileCondition):
     * $(RULE versionCondition)
     * | $(RULE debugCondition)
     * | $(RULE staticIfCondition)
     * ;)
     */
    public boolean parseCompileCondition() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.KW_VERSION) {
            if (!parseVersionCondition()) {
                cleanup(m, DlangTypes.COMPILE_CONDITION);
                return false;
            }
        }
        else if (i == DlangTypes.KW_DEBUG) {
            if (!parseDebugCondition()) {
                cleanup(m, DlangTypes.COMPILE_CONDITION);
                return false;
            }
        }
        else if (i == DlangTypes.KW_STATIC) {
            if (!parseStaticIfCondition()) {
                cleanup(m, DlangTypes.COMPILE_CONDITION);
                return false;
            }
        }
        else {
            m.drop();
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.COMPILE_CONDITION, true);
        return true;
    }

    public void parseDeclDefsWithRecoveryUpToParentScope() {
        // Assume that the open brace is already passed
        while (!myBuilder.eof() && myBuilder.getTokenType() != DlangTypes.OP_BRACES_RIGHT) {
            parseDeclDefWithRecovery();
        }
    }

    public void parseDeclDefWithRecovery() {
        PsiBuilder.Marker bookmark = myBuilder.mark();
        if (!parseDeclDef()) {
            bookmark.rollbackTo();
            recover("Unable to parse this declaration");
        }
        else {
            bookmark.drop();
        }
    }

    /**
     * Parses a ConditionalDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF conditionalDeclaration):
     * $(RULE compileCondition) $(RULE declarationBlock)
     * | $(RULE compileCondition) $(RULE declarationBlock) $(LITERAL 'else') $(RULE declarationBlock)
     * | $(RULE compileCondition) $(RULE declarationBlock) $(LITERAL 'else') $(LITERAL ':') $(RULE declDefs)*
     * | $(RULE compileCondition) $(LITERAL ':') $(RULE declDefs)+
     * ;)
     */
    public boolean parseConditionalDeclaration(PsiBuilder.Marker m) {
        PsiBuilder.Marker bookmark = myBuilder.mark();
        if (!parseCompileCondition()) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        if (currentIs(DlangTypes.OP_COLON)) {
            myBuilder.advanceLexer();
            parseDeclDefsWithRecoveryUpToParentScope();
            m.done(DlangTypes.CONDITIONAL_DECLARATION);
            return true;
        }
        if (!parseDeclarationBlock()) {
            m.done(DlangTypes.CONDITIONAL_DECLARATION);
            return true;
        }

        if (myBuilder.getTokenType() != DlangTypes.KW_ELSE) {
            m.done(DlangTypes.CONDITIONAL_DECLARATION);
            return true;
        }
        myBuilder.advanceLexer();

        if (myBuilder.getTokenType() == DlangTypes.OP_COLON) {
            myBuilder.advanceLexer();
            parseDeclDefsWithRecoveryUpToParentScope();
        }
        else {
            parseDeclarationBlock();
        }
        m.done(DlangTypes.CONDITIONAL_DECLARATION);
        return true;
    }

    /**
     * Parses a ConditionalStatement
     *
     *
     * $(GRAMMAR $(RULEDEF conditionalStatement):
     * $(RULE compileCondition) $(RULE noScopeNonEmptyStatement) ($(LITERAL 'else') $(RULE noScopeNonEmptyStatement))?
     * ;)
     */
    public boolean parseConditionalStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!parseCompileCondition()) {
            cleanup(m, DlangTypes.CONDITIONAL_STATEMENT);
            return false;
        }
        if (!parseNoScopeNonEmptyStatement()) {
            cleanup(m, DlangTypes.CONDITIONAL_STATEMENT);
            return false;
        }
        if (currentIs(DlangTypes.KW_ELSE)) {
            advance();
            if (!parseNoScopeNonEmptyStatement()) {
                cleanup(m, DlangTypes.CONDITIONAL_STATEMENT);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.CONDITIONAL_STATEMENT, true);
        return true;
    }

    /**
     * Parses a Constraint
     *
     *
     * $(GRAMMAR $(RULEDEF raint):
     * $(LITERAL 'if') $(LITERAL '$(LPAREN)') $(RULE expression) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseConstraint() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType ifToken = expect(DlangTypes.KW_IF);
        if (ifToken == null) {
            cleanup(m, DlangTypes.CONSTRAINT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.CONSTRAINT);
            return false;
        }
        if (!parseExpression()) {
            cleanup(m, DlangTypes.CONSTRAINT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.CONSTRAINT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.CONSTRAINT, true);
        return true;
    }

    /**
     * Parses a Constructor
     *
     *
     * $(GRAMMAR $(RULEDEF ructor):
     * $(LITERAL 'this') $(RULE templateParameters)? $(RULE parameters) $(RULE memberFunctionAttribute)* $(RULE constraint)? ($(RULE functionBody) | $(LITERAL ';'))
     * ;)
     */
    public boolean parseConstructor(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_THIS) {
            return false;
        }
        myBuilder.advanceLexer();
        IElementType p = peekPastParens();
        boolean isTemplate = false;
        if (p == DlangTypes.OP_PAR_LEFT) {
            isTemplate = true;
            if (!parseTemplateParameters()) {
                m.done(DlangTypes.CONSTRUCTOR);
                return true;
            }
        }
        if (!parseParameters()) {
            m.done(DlangTypes.CONSTRUCTOR);
            return true;
        }
        while (moreTokens() && currentIsMemberFunctionAttribute()) {
            if (!parseMemberFunctionAttribute()) {
                m.done(DlangTypes.CONSTRUCTOR);
                return true;
            }
        }
        if (isTemplate && currentIs(DlangTypes.KW_IF)) {
            if (!parseConstraint()) {
                m.done(DlangTypes.CONSTRUCTOR);
                return true;
            }
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
        }
        else if (!parseFunctionBody()) {
            m.done(DlangTypes.CONSTRUCTOR);
            return true;
        }
        m.done(DlangTypes.CONSTRUCTOR);
        return true;
    }

    /**
     * Parses an ContinueStatement
     *
     *
     * $(GRAMMAR $(RULEDEF continueStatement):
     * $(LITERAL 'continue') $(LITERAL Identifier)? $(LITERAL ';')
     * ;)
     */
    public boolean parseContinueStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_CONTINUE)) {
            cleanup(m, DlangTypes.CONTINUE_STATEMENT);
            return false;
        }
        if (!moreTokens()) {
            cleanup(m, DlangTypes.CONTINUE_STATEMENT);
            return false;
        }
        IElementType i = current();
        if (i == DlangTypes.ID) {
            advance();
            if (!tokenCheck(DlangTypes.OP_SCOLON)) {
                cleanup(m, DlangTypes.CONTINUE_STATEMENT);
                return false;
            }
        }
        else if (i == DlangTypes.OP_SCOLON) {
            advance();
        }
        else {
            error("Identifier or semicolon expected following \"continue\"");
            exit_section_modified(myBuilder, m, DlangTypes.CONTINUE_STATEMENT, true);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.CONTINUE_STATEMENT, true);
        return true;
    }

    /**
     * Parses a DebugCondition
     *
     *
     * $(GRAMMAR $(RULEDEF debugCondition):
     * $(LITERAL 'debug') ($(LITERAL '$(LPAREN)') ($(LITERAL IntegerLiteral) | $(LITERAL Identifier)) $(LITERAL '$(RPAREN)'))?
     * ;)
     */
    public boolean parseDebugCondition() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType d = expect(DlangTypes.KW_DEBUG);
        if (d == null) {
            cleanup(m, DlangTypes.DEBUG_CONDITION);
            return false;
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            advance();
            if (currentIsOneOf(DlangTypes.INTEGER_LITERAL, DlangTypes.ID)) {
                advance();
            }
            else {
                error("Integer literal or identifier expected");
                exit_section_modified(myBuilder, m, DlangTypes.DEBUG_CONDITION, true);
                return false;
            }
            if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
                cleanup(m, DlangTypes.DEBUG_CONDITION);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.DEBUG_CONDITION, true);
        return true;
    }

    /**
     * Parses a DebugSpecification
     *
     *
     * $(GRAMMAR $(RULEDEF debugSpecification):
     * $(LITERAL 'debug') $(LITERAL '=') ($(LITERAL Identifier) | $(LITERAL IntegerLiteral)) $(LITERAL ';')
     * ;)
     */
    public boolean parseDebugSpecification(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_DEBUG) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (!tokenCheck(DlangTypes.OP_EQ)) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        if (currentIsOneOf(
            DlangTypes.ID,
            DlangTypes.INTEGER_LITERAL
        )) { // Note: that using integer for version is deprecated since 2.101.0
            advance();
        }
        else {
            error("Integer literal or identifier expected");
            m.done(DlangTypes.DEBUG_SPECIFICATION);
            return true;
        }
        tokenCheck(DlangTypes.OP_SCOLON);
        m.done(DlangTypes.DEBUG_SPECIFICATION);
        return true;
    }

    /**
     * Parses a DeclDef
     * $(GRAMMAR $(RULEDEF declDef):
     * | $(RULE attributeSpecifier)
     * | $(RULE declaration)
     * | $(RULE constructor)
     * | $(RULE destructor)
     * | $(RULE postblit)
     * | $(RULE invariant)
     * | $(RULE unittest)
     * | $(RULE aliasThisDeclaration)
     * | $(RULE staticConstructor)
     * | $(RULE staticDestructor)
     * | $(RULE sharedStaticConstructor)
     * | $(RULE sharedStaticDestructor)
     * | $(RULE conditionalDeclaration)
     * | $(RULE debugSpecification)
     * | $(RULE versionSpecification)
     * | $(RULE mixinDeclaration)
     * | $(RULE emptyDeclaration)
     * ;)
     */
    public boolean parseDeclDef() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean result = parseDeclDef(m);
        if (!result) {
            m.drop();
        }
        return result;
    }

    public boolean parseDeclDef(PsiBuilder.Marker m) {
        m.setCustomEdgeTokenBinders(LeadingDocCommentBinder.INSTANCE, TrailingDocCommentBinder.INSTANCE);
        boolean result = false;
        result = result || parseDestructor(m);
        result = result || parsePostblit(m);
        result = result || parseInvariant(m);
        result = result || parseUnittest(m);
        result = result || parseDebugSpecification(m);
        result = result || parseVersionSpecification(m);
        result = result || parseAliasThisDeclaration(m);
        result = result || parseConstructor(m);
        result = result || parseStaticConstructor(m);
        result = result || parseStaticDestructor(m);
        result = result || parseSharedStaticConstructor(m);
        result = result || parseSharedStaticDestructor(m);
        result = result || parseConditionalDeclaration(m);
        result = result || parseMixinDeclaration(m);
        result = result || parseDeclaration(m);
        result = result || parseAttributeSpecifier(m);
        result = result || parseEmptyDeclaration(m);
        return result;
    }

    public boolean parseAttributeSpecifier(PsiBuilder.Marker marker) {
        PsiBuilder.Marker bookmark = myBuilder.mark();
        boolean hasAttribute = parseAttribute();
        if (!hasAttribute) {
            bookmark.rollbackTo();
            return false;
        }
        if (currentIs(DlangTypes.OP_COLON)) {
            bookmark.drop();
            advance();
            marker.done(DlangTypes.ATTRIBUTE_SPECIFIER);
            return true;
        }
        if (!parseDeclarationBlockWithAttribute(marker)) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        // Marker done by parseDeclarationBlock
        return true;
    }

    public boolean parseDeclarationBlockWithAttribute(PsiBuilder.Marker marker) {
        // single statement
        if (!currentIs(DlangTypes.OP_BRACES_LEFT)) {
            return parseDeclDef(marker);
        }
        // Multiple statements
        parseDeclarationBlockWithBlock();
        marker.done(DlangTypes.ATTRIBUTE_SPECIFIER);
        return true;
    }

    public boolean parseDeclarationBlock() {
        // single statement
        if (!currentIs(DlangTypes.OP_BRACES_LEFT)) {
            return parseDeclDef();
        }
        // Multiple statements
        parseDeclarationBlockWithBlock();
        return true;
    }

    private void parseDeclarationBlockWithBlock() {
        assert myBuilder.getTokenType() == DlangTypes.OP_BRACES_LEFT;
        // Multiple statements
        PsiBuilder.Marker marker = myBuilder.mark();
        myBuilder.advanceLexer();
        parseDeclDefsWithRecoveryUpToParentScope();
        expect(DlangTypes.OP_BRACES_RIGHT);
        marker.done(DlangTypes.DECLARATION_BLOCK);
    }

    public boolean parseEmptyDeclaration(PsiBuilder.Marker marker) {
        if (myBuilder.getTokenType() != DlangTypes.OP_SCOLON) {
            return false;
        }
        myBuilder.advanceLexer();
        marker.done(DlangTypes.EMPTY_DECLARATION);
        return true;
    }

    /**
     * Parses a Declaration
     * $(GRAMMAR $(RULEDEF declaration):
     * $(RULE functionDeclaration)
     * | $(RULE variableDeclaration)
     * | $(RULE aliasDeclaration)
     * | $(RULE aliasAssign)
     * | $(RULE aggregateDeclaration)
     * | $(RULE enumDeclaration)
     * | $(RULE importDeclaration)
     * | $(RULE conditionalDeclaration)
     * | $(RULE staticForeachDeclaration)
     * | $(RULE staticAssertDeclaration)
     * | $(RULE templateDeclaration)
     * | $(RULE templateMixinDeclaration)
     * | $(RULE templateMixin)
     * ;)
     */
    public boolean parseDeclaration(PsiBuilder.Marker m) {
        m.setCustomEdgeTokenBinders(LeadingDocCommentBinder.INSTANCE, TrailingDocCommentBinder.INSTANCE);
        boolean result = parseAliasDeclaration(m);
        result = result || parseAliasAssign(m);
        result = result || parseAggregateDeclaration(m);
        result = result || parseEnumDeclaration(m);
        result = result || parseImportDeclaration(m);
        result = result || parseConditionalDeclaration(m);
        result = result || parseStaticForeachDeclaration(m);
        result = result || parseStaticAssertDeclaration(m);
        result = result || parseTemplateDeclaration(m);
        result = result || parseTemplateMixinDeclaration(m);
        result = result || parseTemplateMixin(m);
        result = result || parseFunctionDeclaration(m);
        result = result || parseVariableDeclaration(m);
        return result;
    }

    public boolean parseAggregateDeclaration(PsiBuilder.Marker m) {
        boolean result = parseClassDeclaration(m);
        result = result || parseInterfaceDeclaration(m);
        result = result || parseStructDeclaration(m);
        result = result || parseUnionDeclaration(m);
        return result;
    }

    private void exit_section_modified(PsiBuilder builder, PsiBuilder.Marker m, IElementType type, boolean b) {
        m.done(type);
    }

    /**
     * Parses an Identifier initializer
     *
     *
     * $(GRAMMAR $(RULEDEF declarator):
     * $(LITERAL Identifier)
     * | $(LITERAL Identifier) $(LITERAL '=') $(RULE initializer)
     * | $(LITERAL Identifier) $(RULE templateParameters) $(LITERAL '=') $(RULE initializer)
     * ;)
     */
    public boolean parseIdentifierInitializer() {
        if (myBuilder.getTokenType() != DlangTypes.ID) {
            return false;
        }
        PsiBuilder.Marker m = myBuilder.mark();
        advance();
        if (currentIs(DlangTypes.OP_BRACKET_LEFT))  // dmd doesn't accept pointer after identifier
        {
            while (moreTokens() && currentIs(DlangTypes.OP_BRACKET_LEFT)) {
                if (!parseTypeSuffix()) {
                    cleanup(m, DlangTypes.IDENTIFIER_INITIALIZER);
                    return false;
                }
            }
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            if (!parseTemplateParameters()) {
                cleanup(m, DlangTypes.IDENTIFIER_INITIALIZER);
                return false;
            }
            if (!tokenCheck(DlangTypes.OP_EQ)) {
                cleanup(m, DlangTypes.IDENTIFIER_INITIALIZER);
                return false;
            }
            if (!parseInitializer()) {
                cleanup(m, DlangTypes.IDENTIFIER_INITIALIZER);
                return true;
            }
        }
        else if (currentIs(DlangTypes.OP_EQ)) {
            advance();
            if (!parseInitializer()) {
                cleanup(m, DlangTypes.IDENTIFIER_INITIALIZER);
                return true;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.IDENTIFIER_INITIALIZER, true);
        return true;
    }

    /**
     * Parses a DeclaratorIdentifierList
     *
     *
     * $(GRAMMAR $(RULEDEF declaratorIdentifierList):
     * $(LITERAL Identifier) ($(LITERAL ',') $(LITERAL Identifier))*
     * ;)
     */
    public boolean parseDeclaratorIdentifierList() {
        while (moreTokens()) {
            PsiBuilder.Marker m = myBuilder.mark();
            IElementType ident = expect(DlangTypes.ID);
            if (ident == null) {
                cleanup(m, DlangTypes.DECLARATOR_IDENTIFIER);
                return false;
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
            }
            else {
                exit_section_modified(myBuilder, m, DlangTypes.DECLARATOR_IDENTIFIER, true);
                break;
            }
            exit_section_modified(myBuilder, m, DlangTypes.DECLARATOR_IDENTIFIER, true);
        }
        return true;
    }

    /**
     * Parses a DefaultStatement
     *
     *
     * $(GRAMMAR $(RULEDEF defaultStatement):
     * $(LITERAL 'default') $(LITERAL ':') $(RULE scopeStatementList)?
     * ;)
     */
    public boolean parseDefaultStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_DEFAULT)) {
            cleanup(m, DlangTypes.DEFAULT_STATEMENT);
            return false;
        }
        IElementType colon = expect(DlangTypes.OP_COLON);
        if (colon == null) {
            cleanup(m, DlangTypes.DEFAULT_STATEMENT);
            return false;
        }
        if (!parseScopeStatementList()) {
            cleanup(m, DlangTypes.DEFAULT_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.DEFAULT_STATEMENT, true);
        return true;
    }

    /**
     * Parses a DeleteExpression
     *
     *
     * $(GRAMMAR $(RULEDEF deleteExpression):
     * $(LITERAL 'delete') $(RULE unaryExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseDeleteExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_DELETE)) {
            m.done(DlangTypes.DELETE_EXPRESSION);
            return null;
        }
        if (parseUnaryExpression() == null) {
            m.done(DlangTypes.DELETE_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.DELETE_EXPRESSION);
        return m;
    }

    /**
     * Parses a Deprecated attribute
     *
     *
     * $(GRAMMAR $(RULEDEF deprecated):
     * $(LITERAL 'deprecated') ($(LITERAL '$(LPAREN)') $(LITERAL StringLiteral)+ $(LITERAL '$(RPAREN)'))?
     * ;)
     */
    public boolean parseDeprecated() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_DEPRECATED)) {
            cleanup(m, DlangTypes.DEPRECATED);
            return false;
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            advance();
            if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.DEPRECATED);
                return false;
            }
            if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
                cleanup(m, DlangTypes.DEPRECATED);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.DEPRECATED, true);
        return true;
    }

    /**
     * Parses a Destructor
     *
     *
     * $(GRAMMAR $(RULEDEF destructor):
     * $(LITERAL '~') $(LITERAL 'this') $(LITERAL '$(LPAREN)') $(LITERAL '$(RPAREN)') $(RULE memberFunctionAttribute)* ($(RULE functionBody) | $(LITERAL ';'))
     * ;)
     */
    public boolean parseDestructor(PsiBuilder.Marker m) {
        if (!currentIs(DlangTypes.OP_TILDA)) {
            return false;
        }
        myBuilder.advanceLexer();
        if (!moreTokens()) {
            error("`this` expected");
            m.done(DlangTypes.DESTRUCTOR);
            return true;
        }
        if (!tokenCheck(DlangTypes.KW_THIS)) {
            m.done(DlangTypes.DESTRUCTOR);
            return true;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            m.done(DlangTypes.DESTRUCTOR);
            return true;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            m.done(DlangTypes.DESTRUCTOR);
            return true;
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
        }
        else {
            while (moreTokens() && currentIsMemberFunctionAttribute()) {
                if (!parseMemberFunctionAttribute()) {
                    m.done(DlangTypes.DESTRUCTOR);
                    return true;
                }
            }
            if (!parseFunctionBody()) {
                m.done(DlangTypes.DESTRUCTOR);
                return true;
            }
        }
        m.done(DlangTypes.DESTRUCTOR);
        return true;
    }

    /**
     * Parses a DoStatement
     *
     *
     * $(GRAMMAR $(RULEDEF doStatement):
     * $(LITERAL 'do') $(RULE statementNoCaseNoDefault) $(LITERAL 'while') $(LITERAL '$(LPAREN)') $(RULE expression) $(LITERAL '$(RPAREN)') $(LITERAL ';')
     * ;)
     */
    public boolean parseDoStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_DO)) {
            cleanup(m, DlangTypes.DO_STATEMENT);
            return false;
        }
        if (!moreTokens()) {
            cleanup(m, DlangTypes.DO_STATEMENT);
            return false;
        }
        if (!parseNonEmptyStatementNoCaseNoDefault()) {
            cleanup(m, DlangTypes.DO_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.KW_WHILE)) {
            cleanup(m, DlangTypes.DO_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.DO_STATEMENT);
            return false;
        }
        if (!parseExpression()) {
            cleanup(m, DlangTypes.DO_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.DO_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_SCOLON)) {
            cleanup(m, DlangTypes.DO_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.DO_STATEMENT, true);
        return true;
    }

    /**
     * Parses an EnumBody
     *
     *
     * $(GRAMMAR $(RULEDEF enumBody):
     * $(LITERAL '{') $(RULE enumMember) ($(LITERAL ',') $(RULE enumMember)?)* $(LITERAL '}')
     * ;)
     */
    public boolean parseEnumBody() {
        if (myBuilder.getTokenType() != DlangTypes.OP_BRACES_LEFT) {
            return false;
        }
        PsiBuilder.Marker m = myBuilder.mark();
        myBuilder.advanceLexer();
        while (!myBuilder.eof() && myBuilder.getTokenType() != DlangTypes.OP_BRACES_RIGHT) {
            if (parseEnumMember()) {
                if (myBuilder.getTokenType() == DlangTypes.OP_COMMA) {
                    advance();
                    if (myBuilder.getTokenType() != DlangTypes.OP_BRACES_RIGHT) {
                        continue;
                    }
                }

                if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_RIGHT) {
                    break;
                }
                else {
                    error("`,` or `}` expected");
                    if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_RIGHT) {
                        break;
                    }
                }
            }
            else {
                if (myBuilder.getTokenType() == DlangTypes.OP_COMMA) {
                    PsiBuilder.Marker err = myBuilder.mark();
                    myBuilder.advanceLexer();
                    err.error(LocalizeValue.localizeTODO("Unexpected `,` while enum member or `}` expected"));
                    continue;
                }
                error("Enum member expected");
            }
        }
        expect(DlangTypes.OP_BRACES_RIGHT);
        m.done(DlangTypes.ENUM_BODY);
        return true;
    }

    /**
     * $(GRAMMAR $(RULEDEF enumMember):
     * $(RULE enumMemberAttributes) $(RULE type)? $(LITERAL identifier) ($(LITERAL '=') $(RULE assignExpression))?
     * ;)
     */
    public boolean parseEnumMember(boolean typeAllowed) {
        PsiBuilder.Marker m = myBuilder.mark();
        while (moreTokens()) {
            if (!parseEnumMemberAttribute()) {
                break;
            }
        }

        if (!(currentIs(DlangTypes.ID) && peekIsOneOf(
            DlangTypes.OP_COMMA,
            DlangTypes.OP_EQ,
            DlangTypes.OP_BRACES_RIGHT
        ))) {
            if (typeAllowed) {
                if (!parseType()) {
                    cleanup(m, DlangTypes.ENUM_MEMBER);
                    return false;
                }
            }
            else {
                PsiBuilder.Marker error = myBuilder.mark();
                if (!parseUnexpectedType()) {
                    error.rollbackTo();
                    return false;
                }
                error.error(LocalizeValue.localizeTODO("Cannot specify anonymous enum member type if anonymous enum has a base type."));
            }
        }
        if (!tokenCheck(DlangTypes.ID)) {
            cleanup(m, DlangTypes.ENUM_MEMBER);
            return false;
        }

        if (myBuilder.getTokenType() == DlangTypes.OP_EQ) {
            advance();
            if (!assignAnonEnumMember(m)) {
                return false;
            }
        }
        m.done(DlangTypes.ENUM_MEMBER);
        return true;
    }

    private boolean assignAnonEnumMember(PsiBuilder.Marker m) {
        if (parseAssignExpression() == null) {
            cleanup(m, DlangTypes.ENUM_MEMBER);
            return false;
        }
        return true;
    }

    public boolean parseAnonymousEnumDeclaration() {
        boolean hasBaseType = false;
        if (currentIs(DlangTypes.OP_COLON)) {
            advance();
            if (!parseType()) {
                return true;
            }
            hasBaseType = true;
        }
        if (myBuilder.getTokenType() != DlangTypes.OP_BRACES_LEFT) {
            return false;
        }
        myBuilder.advanceLexer();
        while (!myBuilder.eof()) {
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
            }
            else if (currentIs(DlangTypes.OP_BRACES_RIGHT)) {
                break;
            }
            else {
                parseEnumMember(!hasBaseType);
            }
        }
        if (!tokenCheck(DlangTypes.OP_BRACES_RIGHT)) {
            return true;
        }
        return true;
    }

    /**
     * Parses an EnumDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF enumDeclaration):
     * $(LITERAL 'enum') $(LITERAL Identifier) ($(LITERAL ':') $(RULE type)) $(LITERAL ';')
     * | $(LITERAL 'enum') $(LITERAL Identifier) ($(LITERAL ':') $(RULE type)) $(RULE enumBody)
     * $(GRAMMAR $(RULEDEF anonymousEnumDeclaration):
     * $(LITERAL 'enum') ($(LITERAL ':') $(RULE type))? $(LITERAL '{') $(RULE anonymousEnumMember)+ $(LITERAL '}')
     * ;)
     */
    public boolean parseEnumDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_ENUM) {
            return false;
        }

        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();

        if (myBuilder.getTokenType() != DlangTypes.ID) {
            if (!parseAnonymousEnumDeclaration()) {
                bookmark.rollbackTo();
                return false;
            }
            bookmark.drop();
            m.done(DlangTypes.ANONYMOUS_ENUM_DECLARATION);
            return true;
        }
        advance();

        if (myBuilder.getTokenType() == DlangTypes.OP_COLON) {
            advance(); // skip ':'
            if (!parseType()) {
                bookmark.drop();
                m.done(DlangTypes.ENUM_DECLARATION);
                return true;
            }
        }

        if (myBuilder.getTokenType() == DlangTypes.OP_SCOLON) {
            advance();
            bookmark.drop();
            m.done(DlangTypes.ENUM_DECLARATION);
            return true;
        }

        if (!parseEnumBody()) {
            bookmark.rollbackTo();
            return false;
        }

        bookmark.drop();
        m.done(DlangTypes.ENUM_DECLARATION);
        return true;
    }

    /**
     * Parses an EnumMemberAttribute
     *
     *
     * $(GRAMMAR $(RULEDEF enumMemberAttribute):
     * $(RULE atAttribute)
     * | $(RULE deprecated)
     * ;)
     */
    public boolean parseEnumMemberAttribute() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (myBuilder.getTokenType() == DlangTypes.OP_AT) {
            if (!parseAtAttribute()) {
                m.done(DlangTypes.ENUM_MEMBER_ATTRIBUTE);
                return false;
            }
        }
        else if (myBuilder.getTokenType() == DlangTypes.KW_DEPRECATED) {
            if (!parseDeprecated()) {
                cleanup(m, DlangTypes.ENUM_MEMBER_ATTRIBUTE);
                return false;
            }
        }
        else {
            m.drop(); // drop instead of cleanup otherwise an empty EnumMemberAttribute will be created
            return false;
        }
        m.done(DlangTypes.ENUM_MEMBER_ATTRIBUTE);
        return true;
    }

    /**
     * Parses an EnumMember
     *
     *
     * $(GRAMMAR $(RULEDEF enumMember):
     * ($(RULE enumMemberAttribute))* $(LITERAL Identifier) $(LITERAL '=') $(RULE assignExpression)
     * ;)
     */
    public boolean parseEnumMember() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean memberFound = false;
        while (!myBuilder.eof()) {
            if (!parseEnumMemberAttribute()) {
                break;
            }
            memberFound = true;
        }

        if (myBuilder.getTokenType() != DlangTypes.ID) {
            PsiBuilder.Marker error = myBuilder.mark();
            boolean type = parseUnexpectedType();
            if (!type) {
                error.rollbackTo();
                if (memberFound) {
                    m.done(DlangTypes.ENUM_MEMBER);
                }
                else {
                    m.drop();
                }
                return false;
            }
            error.error(LocalizeValue.localizeTODO("Type is not allowed for named enum"));
        }

        if (!tokenCheck(DlangTypes.ID)) {
            cleanup(m, DlangTypes.ENUM_MEMBER);
            return false;
        }

        if (myBuilder.getTokenType() == DlangTypes.OP_EQ) {
            advance();
            parseAssignExpression();
        }

        m.done(DlangTypes.ENUM_MEMBER);
        return true;
    }

    /**
     * Parses an EqualExpression
     *
     *
     * $(GRAMMAR $(RULEDEF equalExpression):
     * $(RULE shiftExpression) ($(LITERAL '==') | $(LITERAL '!=')) $(RULE shiftExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseEqualExpression(PsiBuilder.@Nullable Marker marker) {
        PsiBuilder.Marker m = marker;
        if (m == null) {
            m = myBuilder.mark();
            if (parseShiftExpression() == null) {
                m.done(DlangTypes.EQUAL_EXPRESSION);
                return null;
            }
        }
        if (currentIsOneOf(DlangTypes.OP_EQ_EQ, DlangTypes.OP_NOT_EQ)) {
            advance();
        }
        if (parseShiftExpression() == null) {
            m.done(DlangTypes.EQUAL_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.EQUAL_EXPRESSION);
        return m;
    }

    /**
     * Parses an Expression
     *
     *
     * $(GRAMMAR $(RULEDEF expression):
     * $(RULE commaExpression)
     * ;)
     */
    public boolean parseExpression() {
        return parseCommaExpression() != null;
    }

    /**
     * Parses an Expression
     *
     *
     * $(GRAMMAR $(RULEDEF expression):
     * $(RULE assignExpression)
     * | $(RULE commaExpression) ($(LITERAL ',') $(RULE assignExpression))
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseCommaExpression() {
        PsiBuilder.Marker assign = parseAssignExpression();
        if (assign == null) {
            return null;
        }
        while (currentIs(DlangTypes.OP_COMMA)) {
            assign = assign.precede();
            advance();
            if (!currentIsOneOf(DlangTypes.OP_PAR_RIGHT, DlangTypes.OP_BRACES_RIGHT, DlangTypes.OP_BRACKET_RIGHT)) {
                if (parseAssignExpression() == null) {
                    assign.done(DlangTypes.COMMA_EXPRESSION);
                    break;
                }
            }
            assign.done(DlangTypes.COMMA_EXPRESSION);
        }
        return assign;
    }

    /**
     * Parses an ExpressionStatement
     *
     *
     * $(GRAMMAR $(RULEDEF expressionStatement):
     * $(RULE expression) $(LITERAL ';')
     * ;)
     */
    public boolean parseExpressionStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean b = parseExpression();
        if (!b) {
            m.rollbackTo();
            return false;
        }
        if (expect(DlangTypes.OP_SCOLON) == null) {
            // To have enter key properly work in unfinished expression statement
            m.setCustomEdgeTokenBinders(null, TrailingSpaceBinder.INSTANCE);
        }
        m.done(DlangTypes.EXPRESSION_STATEMENT);
        return true;
    }

    public boolean parseDeclarationStatement() {
        PsiBuilder.Marker marker = myBuilder.mark();
        // First try without storage class to have storage class attached to the declaration that can have some
        // It produces a nicer tree
        if (parseDeclaration(myBuilder.mark())) {
            marker.done(DlangTypes.DECLARATION_STATEMENT);
            return true;
        }
        // Declaration does not contains storage class
        marker.rollbackTo();
        marker = myBuilder.mark();
        while (!myBuilder.eof()) {
            if (!parseStorageClass()) {
                break;
            }
        }
        if (!parseDeclaration(myBuilder.mark())) {
            marker.rollbackTo();
            return false;
        }
        marker.done(DlangTypes.DECLARATION_STATEMENT);
        return true;
    }

    /**
     * Parses a FinalSwitchStatement
     *
     *
     * $(GRAMMAR $(RULEDEF finalSwitchStatement):
     * $(LITERAL 'final') $(RULE switchStatement)
     * ;)
     */
    public boolean parseFinalSwitchStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (expect(DlangTypes.KW_FINAL) == null) {
            cleanup(m, DlangTypes.FINAL_SWITCH_STATEMENT);
            return false;
        }
        if (!parseSwitchStatement()) {
            cleanup(m, DlangTypes.FINAL_SWITCH_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.FINAL_SWITCH_STATEMENT, true);
        return true;
    }

    /**
     * Parses a Finally statement
     *
     *
     * $(GRAMMAR $(RULEDEF finallyStatement):
     * $(LITERAL 'finally') $(RULE noScopeNonEmptyStatement)
     * ;)
     */
    public boolean parseFinally() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_FINALLY)) {
            cleanup(m, DlangTypes.FINALLY);
            return false;
        }
        if (!parseNoScopeNonEmptyStatement()) {
            cleanup(m, DlangTypes.FINALLY);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.FINALLY, true);
        return true;
    }

    /**
     * Parses a ForStatement
     *
     *
     * $(GRAMMAR $(RULEDEF forStatement):
     * $(LITERAL 'for') $(LITERAL '$(LPAREN)') ($LITERAL ';') | $(RULE noScopeNonEmptyStatement)) $(RULE expression)? $(LITERAL ';') $(RULE expression)? $(LITERAL '$(RPAREN)') $(RULE scopeStatement)
     * ;)
     */
    public boolean parseForStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_FOR)) {
            cleanup(m, DlangTypes.FOR_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.FOR_STATEMENT);
            return false;
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
        }
        else if (!parseNoScopeNonEmptyStatement()) {
            cleanup(m, DlangTypes.FOR_STATEMENT);
            return false;
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
        }
        else {
            if (!parseExpression()) {
                cleanup(m, DlangTypes.FOR_STATEMENT);
                return false;
            }
            expect(DlangTypes.OP_SCOLON);
        }
        if (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
            if (!parseExpression()) {
                cleanup(m, DlangTypes.FOR_STATEMENT);
                return false;
            }
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.FOR_STATEMENT);
            return false;
        }
        if (!parseScopeStatement()) {
            cleanup(m, DlangTypes.FOR_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.FOR_STATEMENT, true);
        return true;
    }


    /**
     * Parses a StaticForeachDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF staticForeachDeclaration):
     * $(LITERAL 'static') ($(LITERAL 'foreach') | $(LITERAL 'foreach_reverse')) $(LITERAL '$(LPAREN)') $(RULE foreachTypeList) $(LITERAL ';') $(RULE expression) $(LITERAL '$(RPAREN)') ($(RULE declaration) | $(LITERAL '{') $(RULE declaration)* $(LITERAL '}'))
     * | $(LITERAL 'static') ($(LITERAL 'foreach') | $(LITERAL 'foreach_reverse')) $(LITERAL '$(LPAREN)') $(RULE foreachType) $(LITERAL ';') $(RULE expression) $(LITERAL '..') $(RULE expression) $(LITERAL '$(RPAREN)') ($(RULE declaration) | $(LITERAL '{') $(RULE declaration)* $(LITERAL '}'))
     * ;)
     */
    public boolean parseStaticForeachDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_STATIC) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (!currentIsOneOf(DlangTypes.KW_FOREACH, DlangTypes.KW_FOREACH_REVERSE)) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        parseForeach(true);
        m.done(DlangTypes.STATIC_FOREACH_DECLARATION);
        return true;
    }

    /**
     * Parses a ForeachStatement
     *
     * $(GRAMMAR $(RULEDEF foreachStatement):
     *   ($(LITERAL 'foreach') | $(LITERAL 'foreach_reverse')) $(LITERAL '$(LPAREN)') $(RULE foreachTypeList) $(LITERAL ';') $(RULE expression) $(LITERAL '$(RPAREN)') $(RULE NoScopeNonEmptyStatement)
     * | ($(LITERAL 'foreach') | $(LITERAL 'foreach_reverse')) $(LITERAL '$(LPAREN)') $(RULE foreachType) $(LITERAL ';') $(RULE expression) $(LITERAL '..') $(RULE expression) $(LITERAL '$(RPAREN)') $(RULE NoScopeNonEmptyStatement)
     * ;)
     */
    public boolean parseForeachStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean r = parseForeach(false);
        m.done(DlangTypes.FOREACH_STATEMENT);
        return r;
    }

    public boolean parseStaticForeachStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (expect(DlangTypes.KW_STATIC) == null) {
            return false;
        }
        boolean r = parseForeach(false);
        m.done(DlangTypes.FOREACH_STATEMENT);
        return r;
    }

    private boolean parseForeach(boolean declOnly) {
        if (currentIsOneOf(DlangTypes.KW_FOREACH, DlangTypes.KW_FOREACH_REVERSE)) {
            advance();
        }
        else {
            error("`foreach` or `foreach_reverse` expected");
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            return false;
        }
        int types = parseForeachTypeList();
        boolean canBeRange = types == 1;
        if (!tokenCheck(DlangTypes.OP_SCOLON)) {
            return false;
        }
        if (!parseExpression()) {
            return false;
        }
        if (currentIs(DlangTypes.OP_DDOT)) {
            if (!canBeRange) {
                error("Cannot have more than one foreach variable for a foreach range statement");
                return false;
            }
            advance();
            if (!parseExpression()) {
                return false;
            }
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            return false;
        }
        if (currentIs(DlangTypes.OP_BRACES_RIGHT)) {
            error("Statement expected");
            return true;
        }
        if (declOnly) {
            if (currentIs(DlangTypes.OP_BRACES_LEFT)) {
                advance();
                parseDeclDefsWithRecoveryUpToParentScope();
                if (!tokenCheck(DlangTypes.OP_BRACES_RIGHT)) {
                    return false;
                }
            }
            else if (!parseDeclDef()) {
                return false;
            }
        }
        else {
            if (!parseNoScopeNonEmptyStatement()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses a ForeachType
     *
     *
     * $(GRAMMAR $(RULEDEF foreachType):
     * $(LITERAL 'scope') | $(LITERAL 'ref') | $(LITERAL 'enum') | $(RULE typeConstructor)* $(RULE basicType) $(RULE typeSuffix)* $(LITERAL Identifier)
     * | $(LITERAL 'scope') | $(LITERAL 'ref') | $(LITERAL 'enum') | $(RULE typeConstructor)* $(LITERAL Identifier)
     * | $(LITERAL 'scope') | $(LITERAL 'ref') | $(LITERAL 'enum') | $(RULE typeConstructor)* $(LITERAL 'alias') $(LITERAL Identifier)
     * ;)
     */
    public void parseForeachType() {
        PsiBuilder.Marker m = myBuilder.mark();
        while (moreTokens()) {
            if (currentIs(DlangTypes.KW_SCOPE)) {
                myBuilder.advanceLexer();
            }
            else if (currentIs(DlangTypes.KW_REF)) {
                myBuilder.advanceLexer();
            }
            else if (currentIs(DlangTypes.KW_ENUM)) {
                myBuilder.advanceLexer();
            }
            else if (!parseTypeConstructor()) {
                break;
            }
        }

        boolean aliased = false;
        if (myBuilder.getTokenType() == DlangTypes.KW_ALIAS) {
            myBuilder.advanceLexer();
            aliased = true;
        }

        if (myBuilder.getTokenType() == DlangTypes.ID && peekIsOneOf(DlangTypes.OP_COMMA, DlangTypes.OP_SCOLON)) {
            myBuilder.advanceLexer();
            m.done(DlangTypes.FOREACH_TYPE);
            return;
        }
        if (aliased) {
            errorUpToExcluded("Identifier expected", DlangTypes.OP_SCOLON, DlangTypes.OP_COMMA, DlangTypes.OP_PAR_RIGHT);
            m.done(DlangTypes.FOREACH_TYPE);
            return;
        }
        if (!parseBasicType()) {
            errorUpToExcluded("Type expected", DlangTypes.OP_PAR_RIGHT);
            m.done(DlangTypes.FOREACH_TYPE);
            return;
        }
        parseTypeSuffixes();
        expect(DlangTypes.ID);
        m.done(DlangTypes.FOREACH_TYPE);
    }

    /**
     * Parses a ForeachTypeList
     *
     * $(GRAMMAR $(RULEDEF foreachTypeList):
     * $(RULE foreachType) ($(LITERAL ',') $(RULE foreachType))*
     * ;)
     */
    public int parseForeachTypeList() {
        PsiBuilder.Marker marker = myBuilder.mark();
        int count = 0;
        while (!myBuilder.eof()) {
            parseForeachType();
            count++;
            if (currentIs(DlangTypes.OP_COMMA)) {
                myBuilder.advanceLexer();
                if (myBuilder.getTokenType() == DlangTypes.OP_SCOLON) {
                    myBuilder.error(LocalizeValue.localizeTODO("Foreach variable expected"));
                    break;
                }
            }
            else {
                break;
            }
        }
        marker.done(DlangTypes.FOREACH_TYPE_LIST);
        return count;
    }

    /**
     * Parses a FunctionAttribute
     *
     *
     * $(GRAMMAR $(RULEDEF functionAttribute):
     * $(RULE atAttribute)
     * | $(LITERAL 'pure')
     * | $(LITERAL 'nothrow')
     * ;)
     */
    public boolean parseFunctionAttribute() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.OP_AT) {
            if (!parseAtAttribute()) {
                cleanup(m, DlangTypes.FUNCTION_ATTRIBUTE);
                return false;
            }
        }
        else if (i == DlangTypes.KW_PURE || i == DlangTypes.KW_NOTHROW) {
            advance();
        }
        else {
            error("@attribute, `pure`, or `nothrow` expected");
            exit_section_modified(myBuilder, m, DlangTypes.FUNCTION_ATTRIBUTE, true);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.FUNCTION_ATTRIBUTE, true);
        return true;
    }

    /**
     * Parses a FunctionBody
     *
     *
     * $(GRAMMAR $(RULEDEF functionBody):
     * $(RULE specifiedFunctionBody)
     * | $(RULE missingFunctionBody)
     * | $(RULE shortenedFunctionBody)
     * ;)
     */
    public boolean parseFunctionBody() {
        PsiBuilder.Marker bookmark = myBuilder.mark();
        if (parseMissingFunctionBody()) {
            bookmark.drop();
        }
        else {
            bookmark.rollbackTo();
            bookmark = myBuilder.mark();
            if (parseShortenedFunctionBody()) {
                bookmark.drop();
            }
            else {
                bookmark.rollbackTo();
                return parseSpecifiedFunctionBody();
            }
        }
        return true;
    }

    /**
     * Parses a FunctionContract
     *
     *
     * $(GRAMMAR $(RULEDEF functionContract):
     * $(RULE inOutContractExpression)
     * | $(RULE inOutStatement)
     * ;)
     */
    public boolean parseFunctionContract() {
        return parseFunctionContract(true);
    }

    /**
     * Parses a FunctionContract
     *
     *
     * $(GRAMMAR $(RULEDEF functionContract):
     * $(RULE inOutContractExpression)
     * | $(RULE inOutStatement)
     * ;)
     */
    public boolean parseFunctionContract(boolean allowStatement) {
        PsiBuilder.Marker m = myBuilder.mark();
        if (allowStatement && (peekIs(DlangTypes.OP_BRACES_LEFT) || (currentIs(DlangTypes.KW_OUT) && peekAre(
            DlangTypes.OP_PAR_LEFT,
            DlangTypes.ID,
            DlangTypes.OP_PAR_RIGHT
        )))) {
            if (!parseInOutStatement()) {
                cleanup(m, DlangTypes.FUNCTION_CONTRACT);
                return false;
            }
        }
        else if (peekIs(DlangTypes.OP_PAR_LEFT)) {
            if (!parseInOutContractExpression()) {
                cleanup(m, DlangTypes.FUNCTION_CONTRACT);
                return false;
            }
        }
        else {
            error(
                allowStatement
                    ? "`{` or `(` expected"
                    : "`(` expected"
            );
            cleanup(m, DlangTypes.FUNCTION_CONTRACT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.FUNCTION_CONTRACT, true);
        return true;
    }

    /**
     * Parses a FunctionDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF functionDeclaration):
     * ($(RULE storageClass)+ | $(RULE _type)) $(LITERAL Identifier) $(RULE parameters) $(RULE memberFunctionAttribute)* ($(RULE functionBody) | $(LITERAL ';'))
     * | ($(RULE storageClass)+ | $(RULE _type)) $(LITERAL Identifier) $(RULE templateParameters) $(RULE parameters) $(RULE memberFunctionAttribute)* $(RULE constraint)? ($(RULE functionBody) | $(LITERAL ';'))
     * ;)
     */
    public boolean parseFunctionDeclaration(PsiBuilder.Marker m) {
        PsiBuilder.Marker bookmark = myBuilder.mark();
        boolean hasStorageClass = false;
        while (!myBuilder.eof()) {
            if (!parseStorageClass()) {
                break;
            }
            hasStorageClass = true;
        }
        if (hasStorageClass && currentIs(DlangTypes.ID) && peekIs(DlangTypes.OP_PAR_LEFT)) {
            // it’s an auto function declaration
            advance();
            if (!parseFunctionDeclaratorSuffix()) {
                bookmark.rollbackTo();
                return false;
            }
            if (!parseFunctionBody()) {
                bookmark.rollbackTo();
                return false;
            }
            bookmark.drop();
            m.done(DlangTypes.FUNCTION_DECLARATION);
            return true;
        }
        if (!parseBasicType()) {
            bookmark.rollbackTo();
            return false;
        }
        if (!parseFunctionDeclarator()) {
            bookmark.rollbackTo();
            return false;
        }
        if (!parseFunctionBody()) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        m.done(DlangTypes.FUNCTION_DECLARATION);
        return true;
    }

    public boolean parseFunctionDeclarator() {
        parseTypeSuffixes();
        if (!tokenCheck(DlangTypes.ID)) {
            return false;
        }
        return parseFunctionDeclaratorSuffix();
    }

    public boolean parseFunctionDeclaratorSuffix() {
        if (!currentIs(DlangTypes.OP_PAR_LEFT)) {
            error("`(` expected");
            return false;
        }
        IElementType p = peekPastParens();
        boolean isTemplate = p == DlangTypes.OP_PAR_LEFT;
        if (isTemplate) {
            if (!parseTemplateParameters()) {
                return false;
            }
        }
        if (!parseParameters()) {
            return false;
        }
        while (moreTokens() && currentIsMemberFunctionAttribute()) {
            if (!parseMemberFunctionAttribute()) {
                return false;
            }
        }
        if (isTemplate && currentIs(DlangTypes.KW_IF)) {
            return parseConstraint();
        }
        return true;
    }

    /**
     * Parses a FunctionLiteralExpression
     *
     *
     * $(GRAMMAR $(RULEDEF functionLiteralExpression):
     * | $(LITERAL 'delegate') $($(LITERAL 'auto')? LITERAL 'ref')? $(RULE type)? ($(RULE parameters) $(RULE functionAttribute)*)? $(RULE specifiedFunctionBody)
     * | $(LITERAL 'function') $($(LITERAL 'auto')? LITERAL 'ref')? $(RULE type)? ($(RULE parameters) $(RULE functionAttribute)*)? $(RULE specifiedFunctionBody)
     * | $($(LITERAL 'auto')? LITERAL 'ref')? $(RULE parameters) $(RULE functionAttribute)* $(RULE specifiedFunctionBody)
     * | $(RULE specifiedFunctionBody)
     * | $(LITERAL Identifier) $(LITERAL '=>') $(RULE assignExpression)
     * | $(LITERAL 'function') $($(LITERAL 'auto')? LITERAL 'ref')? $(RULE type)? $(RULE parameters) $(RULE functionAttribute)* $(LITERAL '=>') $(RULE assignExpression)
     * | $(LITERAL 'delegate') $($(LITERAL 'auto')? LITERAL 'ref')? $(RULE type)? $(RULE parameters) $(RULE functionAttribute)* $(LITERAL '=>') $(RULE assignExpression)
     * | $($(LITERAL 'auto')? LITERAL 'ref')? $(RULE parameters) $(RULE functionAttribute)* $(LITERAL '=>') $(RULE assignExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseFunctionLiteralExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (currentIsOneOf(DlangTypes.KW_FUNCTION, DlangTypes.KW_DELEGATE)) {
            advance();
            if (currentIs(DlangTypes.KW_AUTO)) {
                advance();
                expect(DlangTypes.KW_REF);
            }
            else if (currentIs(DlangTypes.KW_REF)) {
                advance();
            }
            if (!currentIsOneOf(
                DlangTypes.OP_PAR_LEFT, DlangTypes.KW_IN, DlangTypes.KW_DO,
                DlangTypes.KW_OUT, DlangTypes.OP_BRACES_LEFT, DlangTypes.OP_LAMBDA_ARROW
            )) {
                if (!parseType()) {
                    m.done(DlangTypes.FUNCTION_LITERAL_EXPRESSION);
                    return null;
                }
            }
        }
        if (startsWith(DlangTypes.ID, DlangTypes.OP_LAMBDA_ARROW)) {
            parseSingleParameter();
            myBuilder.advanceLexer(); // =>
            PsiBuilder.Marker success = parseAssignExpression();
            m.done(DlangTypes.FUNCTION_LITERAL_EXPRESSION);
            return success != null ? m : null;
        }
        else if (currentIs(DlangTypes.OP_PAR_LEFT)
            || currentIs(DlangTypes.KW_REF) && peekIs(DlangTypes.OP_PAR_LEFT) || currentIs(DlangTypes.KW_AUTO) && peekAre(
            DlangTypes.KW_REF, DlangTypes.OP_PAR_LEFT
        )) {
            if (currentIs(DlangTypes.KW_AUTO)) {
                advance();
                expect(DlangTypes.KW_REF);
            }
            else if (currentIs(DlangTypes.KW_REF)) {
                advance();
            }
            if (!parseParameters()) {
                m.done(DlangTypes.FUNCTION_LITERAL_EXPRESSION);
                return null;
            }
            while (currentIsMemberFunctionAttribute()) {
                if (!parseMemberFunctionAttribute()) {
                    break;
                }
            }
        }
        if (currentIs(DlangTypes.OP_LAMBDA_ARROW)) {
            advance();
            if (parseAssignExpression() == null) {
                m.done(DlangTypes.FUNCTION_LITERAL_EXPRESSION);
                return null;
            }
        }
        else if (!parseSpecifiedFunctionBody()) {
            m.done(DlangTypes.FUNCTION_LITERAL_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.FUNCTION_LITERAL_EXPRESSION);
        return m;
    }

    /**
     * Parses a GotoStatement
     *
     *
     * $(GRAMMAR $(RULEDEF gotoStatement):
     * $(LITERAL 'goto') ($(LITERAL Identifier) | $(LITERAL 'default') | $(LITERAL 'case') $(RULE expression)?) $(LITERAL ';')
     * ;)
     */
    public boolean parseGotoStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_GOTO)) {
            cleanup(m, DlangTypes.GOTO_STATEMENT);
            return false;
        }
        IElementType i = current();
        if (i == DlangTypes.ID || i == DlangTypes.KW_DEFAULT) {
            advance();
        }
        else if (i == DlangTypes.KW_CASE) {
            advance();
            if (!currentIs(DlangTypes.OP_SCOLON)) {
                if (!parseExpression()) {
                    cleanup(m, DlangTypes.GOTO_STATEMENT);
                    return false;
                }
            }
        }
        else {
            error("Identifier, `default`, or `case` expected");
            exit_section_modified(myBuilder, m, DlangTypes.GOTO_STATEMENT, true);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_SCOLON)) {
            cleanup(m, DlangTypes.GOTO_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.GOTO_STATEMENT, true);
        return true;
    }

    /**
     * Parses an IdentifierChain
     *
     *
     * $(GRAMMAR $(RULEDEF identifierChain):
     * $(LITERAL Identifier) ($(LITERAL '.') $(LITERAL Identifier))*
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseIdentifierChain() {
        if (myBuilder.getTokenType() != DlangTypes.ID) {
            return null;
        }
        PsiBuilder.Marker m = myBuilder.mark();
        myBuilder.advanceLexer();
        m.done(DlangTypes.IDENTIFIER_CHAIN);
        while (myBuilder.getTokenType() == DlangTypes.OP_DOT) {
            m = m.precede();
            myBuilder.advanceLexer();
            expect(DlangTypes.ID);
            m.done(DlangTypes.IDENTIFIER_CHAIN);
        }
        return m;
    }

    /**
     * Parses a QualifiedIdentifier.
     *
     *
     * $(GRAMMAR $(RULEDEF qualifiedIdentifier):
     * $(RULE identifierOrTemplateInstance)
     * | $(RULE identifierOrTemplateInstance) $(LITERAL '.') $(RULE qualifiedIdentifier)
     * | $(RULE identifier) $(LITERAL '[') $(RULE assignExpression) $(LITERAL ']')
     * | $(RULE identifier) $(LITERAL '[') $(RULE assignExpression) $(LITERAL ']') $(LITERAL '.') $(RULE qualifiedIdentifier)
     * ;)
     */
    public boolean parseQualifiedIdentifier() {
        if (myBuilder.getTokenType() != DlangTypes.ID) {
            myBuilder.error(LocalizeValue.localizeTODO("Identifier expected"));
            return false;
        }
        PsiBuilder.Marker m = myBuilder.mark();
        while (!myBuilder.eof()) {
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (parseTemplateInstance()) {
                bookmark.drop();
                if (currentIs(DlangTypes.OP_DOT)) {
                    advance();
                    m.done(DlangTypes.QUALIFIED_IDENTIFIER);
                    m = m.precede();
                    continue;
                }
                m.done(DlangTypes.QUALIFIED_IDENTIFIER);
                return true;
            }
            else {
                bookmark.rollbackTo();
                advance(); // Identifier
                if (currentIs(DlangTypes.OP_BRACKET_LEFT)) {
                    // dyn arrays -> type suffixes
                    if (peekIs(DlangTypes.OP_BRACKET_RIGHT)) {
                        break;
                    }
                    bookmark = myBuilder.mark();
                    advance();
                    // here we can have a type (AA key)
                    if (parseAssignExpression() == null) {
                        bookmark.rollbackTo();
                        break;
                    }
                    else if (currentIs(DlangTypes.OP_DDOT)) {
                        bookmark.rollbackTo();
                        break;
                    }
                    // otherwise either the index of a type list or a dim
                    bookmark.drop();
                    expect(DlangTypes.OP_BRACKET_RIGHT);
                }
            }
            if (myBuilder.getTokenType() != DlangTypes.OP_DOT) {
                break;
            }
            advance();
            m.done(DlangTypes.QUALIFIED_IDENTIFIER);
            m = m.precede();
        }
        m.done(DlangTypes.QUALIFIED_IDENTIFIER);
        return true;
    }


    /**
     * Parses an MixinQualifiedIdentifier
     *
     *
     * $(GRAMMAR $(RULEDEF MixinQualifiedIdentifier):
     * $(RULE identifier)
     * | $(RULE identifier) ($(LITERAL '.') $(RULE MixinQualifiedIdentifier)
     * | $(RULE TemplateInstance) ($(LITERAL '.') $(RULE MixinQualifiedIdentifier)
     * ;)
     */
    public boolean parseMixinQualifiedIdentifier() {
        if (!currentIs(DlangTypes.ID)) {
            myBuilder.error(LocalizeValue.localizeTODO("Identifier expected"));
            return false;
        }
        PsiBuilder.Marker m = myBuilder.mark();
        while (moreTokens()) {
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (parseTemplateInstance()) {
                if (myBuilder.getTokenType() != DlangTypes.OP_DOT) {
                    // Template arguments not part of the name, just take the id
                    bookmark.rollbackTo();
                    assert myBuilder.getTokenType() == DlangTypes.ID;
                    advance();
                }
                else {
                    bookmark.drop();
                }
            }
            else {
                bookmark.rollbackTo();
                assert myBuilder.getTokenType() == DlangTypes.ID;
                advance();
            }
            if (myBuilder.getTokenType() != DlangTypes.OP_DOT) {
                break;
            }
            myBuilder.advanceLexer();
            if (myBuilder.getTokenType() != DlangTypes.ID) {
                break;
            }
            m.done(DlangTypes.MIXIN_QUALIFIED_IDENTIFIER);
            m = m.precede();
        }
        m.done(DlangTypes.MIXIN_QUALIFIED_IDENTIFIER);
        return true;
    }

    /**
     * Parses an IdentifierOrTemplateInstance
     *
     *
     * $(GRAMMAR $(RULEDEF identifierOrTemplateInstance):
     * $(LITERAL Identifier)
     * | $(RULE templateInstance)
     * ;)
     */
    public boolean parseIdentifierOrTemplateInstance() {
        if (peekIs(DlangTypes.OP_NOT) && !startsWith(DlangTypes.ID, DlangTypes.OP_NOT, DlangTypes.KW_IS) && !startsWith(
            DlangTypes.ID, DlangTypes.OP_NOT, DlangTypes.KW_IN
        )) {
            return parseTemplateInstance();
        }
        else {
            IElementType ident = expect(DlangTypes.ID);
            return ident != null;
        }
    }

    /**
     * Parses an IdentityExpression
     *
     *
     * $(GRAMMAR $(RULEDEF identityExpression):
     * $(RULE shiftExpression) ($(LITERAL 'is') | ($(LITERAL '!') $(LITERAL 'is'))) $(RULE shiftExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseIdentityExpression(PsiBuilder.@Nullable Marker marker) {
        PsiBuilder.Marker m = marker;
        if (m == null) {
            m = myBuilder.mark();
            if (parseShiftExpression() == null) {
                m.done(DlangTypes.IDENTITY_EXPRESSION);
                return null;
            }
        }
        if (currentIs(DlangTypes.OP_NOT)) {
            advance();
        }
        if (!tokenCheck(DlangTypes.KW_IS)) {
            m.done(DlangTypes.IDENTITY_EXPRESSION);
            return null;
        }
        if (parseShiftExpression() == null) {
            m.done(DlangTypes.IDENTITY_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.IDENTITY_EXPRESSION);
        return m;
    }

    /**
     * Parses an IfStatement
     *
     *
     * $(GRAMMAR $(RULEDEF ifStatement):
     * $(LITERAL 'if') $(LITERAL '$(LPAREN)') $(RULE ifCondition) $(LITERAL '$(RPAREN)') $(RULE ifStatement) ($(LITERAL 'else') $(RULE elseStatement))?
     * ;)
     */
    public boolean parseIfStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_IF)) {
            cleanup(m, DlangTypes.IF_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.IF_STATEMENT);
            return false;
        }
        if (!parseIfCondition()) {
            cleanup(m, DlangTypes.IF_STATEMENT);
            return false;
        }

        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.IF_STATEMENT);
            return false;
        }
        if (currentIs(DlangTypes.OP_BRACES_RIGHT)) {
            error("Statement expected");
            exit_section_modified(myBuilder, m, DlangTypes.IF_STATEMENT, true);
            return true;
        }
        if (!parseThenStatement()) {
            cleanup(m, DlangTypes.IF_STATEMENT);
            return false;
        }
        if (currentIs(DlangTypes.KW_ELSE)) {
            advance();
            if (!parseElseStatement()) {
                cleanup(m, DlangTypes.IF_STATEMENT);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.IF_STATEMENT, true);
        return true;
    }

    /**
     * Parse IfConditionStorageClass, returns if it parsed a valid storage class
     *
     * $(RULEDEF ifCondition):
     *   $(LITERAL 'auto')
     * | $(LITERAL 'scope')
     * | $(LITERAL 'ref')
     * | $(RULE typeConstructors)
     * ;)
     */
    private boolean parseIfConditionStorageClass() {
        if (myBuilder.getTokenType() == DlangTypes.KW_AUTO || myBuilder.getTokenType() == DlangTypes.KW_SCOPE || myBuilder.getTokenType() == DlangTypes.KW_REF) {
            myBuilder.advanceLexer();
            return true;
        }
        if (isTypeCtor(current())) {
            PsiBuilder.Marker beforeAdvance = myBuilder.mark();
            while (isTypeCtor(current())) {
                beforeAdvance.drop();
                beforeAdvance = myBuilder.mark();
                myBuilder.advanceLexer();
            }
            // goes back for TypeCtor(Type) = identifier (aka type)
            if (myBuilder.getTokenType() == DlangTypes.OP_PAR_LEFT) {
                beforeAdvance.rollbackTo();
                return false;
            }
            beforeAdvance.drop();
            return true;
        }
        return false;
    }

    /**
     * Parse IfCondition
     *
     * $(RULEDEF ifCondition):
     *  $(RULE ifConditionStorageClass) $(LITERAL Identifier) $(LITERAL '=') $(RULE expression)
     * | $(RULE ifConditionStorageClass)* $(RULE basicType) $(LITERAL Identifier) $(LITERAL '=') $(RULE expression)
     * | $(RULE expression)
     * ;)
     */
    public boolean parseIfCondition() {
        PsiBuilder.Marker m = myBuilder.mark();
        int storageClassCount = 0;
        while (parseIfConditionStorageClass()) {
            storageClassCount++;
        }

        if (storageClassCount == 1 && myBuilder.getTokenType() == DlangTypes.ID) {
            PsiBuilder.Marker breakpoint = myBuilder.mark();
            // check for `id = expression` form
            myBuilder.advanceLexer();
            if (myBuilder.getTokenType() == DlangTypes.OP_EQ) {
                myBuilder.advanceLexer();
                breakpoint.drop();
                parseExpression();
                m.done(DlangTypes.IF_CONDITION);
                return true;
            }
            // Else try the other form
            breakpoint.rollbackTo();
        }

        // Try form BasicType Declarator = Expression
        PsiBuilder.Marker breakpoint = myBuilder.mark();
        if (parseBasicType()) {
            parseTypeSuffixes();
            if (myBuilder.getTokenType() == DlangTypes.ID) {
                // form confirmed
                breakpoint.drop();
                myBuilder.advanceLexer();
                if (!tokenCheck(DlangTypes.OP_EQ)) {
                    m.done(DlangTypes.IF_CONDITION);
                    return false;
                }
                parseExpression();
                m.done(DlangTypes.IF_CONDITION);
                return true;
            }
        }

        // Last it’s a simple expression
        if (storageClassCount == 0) {
            breakpoint.rollbackTo();
            if (parseExpression()) {
                m.done(DlangTypes.IF_CONDITION);
                return true;
            }
        }
        else {
            breakpoint.drop();
        }
        m.done(DlangTypes.IF_CONDITION);
        return false;
    }

    public boolean parseThenStatement() {
        return parseScopeStatement();
    }

    public boolean parseElseStatement() {
        return parseScopeStatement();
    }


    /**
     * Parses an ImportBind
     *
     *
     * $(GRAMMAR $(RULEDEF importBind):
     * $(LITERAL Identifier) ($(LITERAL '=') $(LITERAL Identifier))?
     * ;)
     */
    public boolean parseImportBind() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean isNamedBind = false;
        PsiBuilder.Marker bookmark = myBuilder.mark();
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            bookmark.drop();
            cleanup(m, DlangTypes.IMPORT_BIND);
            return false;
        }
        if (currentIs(DlangTypes.OP_EQ)) {
            isNamedBind = true;
            advance();
            IElementType id = expect(DlangTypes.ID);
            if (id == null) {
                bookmark.drop();
                cleanup(m, DlangTypes.IMPORT_BIND);
                return false;
            }
        }
        bookmark.rollbackTo();
        if (isNamedBind) {
            PsiBuilder.Marker namedImportBind = myBuilder.mark();
            ident = expect(DlangTypes.ID);
            if (ident == null) {
                cleanup(m, DlangTypes.IMPORT_BIND);
                return false;
            }
            exit_section_modified(myBuilder, namedImportBind, DlangTypes.NAMED_IMPORT_BIND, true);
            if (currentIs(DlangTypes.OP_EQ)) {
                advance();
                IElementType id = expect(DlangTypes.ID);
                if (id == null) {
                    cleanup(m, DlangTypes.IMPORT_BIND);
                    return false;
                }
            }
        }
        else {
            ident = expect(DlangTypes.ID);
            if (ident == null) {
                cleanup(m, DlangTypes.IMPORT_BIND);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.IMPORT_BIND, true);
        return true;
    }

    /**
     * Parses ImportBindings
     *
     *
     * $(GRAMMAR $(RULEDEF importBindings):
     * $(RULE import) $(LITERAL ':') $(RULE importBind) ($(LITERAL ',') $(RULE importBind))*
     * ;)
     */
    public boolean parseImportBindings() {
        PsiBuilder.Marker m = myBuilder.mark();
        myBuilder.advanceLexer();
        while (moreTokens()) {
            if (parseImportBind()) {
                if (currentIs(DlangTypes.OP_COMMA)) {
                    advance();
                }
                else {
                    break;
                }
            }
            else {
                break;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.IMPORT_BINDINGS, true);
        return true;
    }

    /**
     * Parses an ImportDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF importDeclaration):
     * $(LITERAL 'static')? $(LITERAL 'import') $(RULE import) ($(LITERAL ',') $(RULE import))* ($(LITERAL ',') $(RULE importBindings))? $(LITERAL ';')
     * | $(LITERAL 'static')? $(LITERAL 'import') $(RULE importBindings) $(LITERAL ';')
     * ;)
     */
    public boolean parseImportDeclaration(PsiBuilder.Marker m) {
        PsiBuilder.Marker bookmark = myBuilder.mark();
        boolean isStatic = false;
        if (myBuilder.getTokenType() == DlangTypes.KW_STATIC) {
            myBuilder.advanceLexer();
            isStatic = true;
        }
        if (!tokenCheck(DlangTypes.KW_IMPORT)) {
            bookmark.rollbackTo();
            return false;
        }
        if (myBuilder.getTokenType() == DlangTypes.OP_PAR_LEFT) {
            if (isStatic) {
                myBuilder.error(LocalizeValue.localizeTODO("Expected identifier instead of '("));
                bookmark.drop();
                m.done(DlangTypes.IMPORT_DECLARATION);
                return true;
            }
            // It’s an import expression
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        boolean si = parseSingleImport();
        if (!si) {
            m.done(DlangTypes.IMPORT_DECLARATION);
            return true;
        }
        if (currentIs(DlangTypes.OP_COLON)) {
            parseImportBindings();
        }
        else {
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
                while (moreTokens()) {
                    if (!parseSingleImport()) {
                        m.done(DlangTypes.IMPORT_DECLARATION);
                        return true;
                    }
                    if (currentIs(DlangTypes.OP_COLON)) {
                        parseImportBindings();
                        break;
                    }
                    else {
                        if (currentIs(DlangTypes.OP_COMMA)) {
                            advance();
                        }
                        else {
                            break;
                        }
                    }
                }
            }
        }
        if (!tokenCheck(DlangTypes.OP_SCOLON)) {
            m.done(DlangTypes.IMPORT_DECLARATION);
            return true;
        }
        exit_section_modified(myBuilder, m, DlangTypes.IMPORT_DECLARATION, true);
        return true;
    }

    /**
     * Parses an ImportExpression
     *
     *
     * $(GRAMMAR $(RULEDEF importExpression):
     * $(LITERAL 'import') $(LITERAL '$(LPAREN)') $(RULE assignExpression) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseImportExpression() {
        PsiBuilder.Marker marker = myBuilder.mark();
        if (expect(DlangTypes.KW_IMPORT) == null) {
            cleanup(marker, DlangTypes.IMPORT_EXPRESSION);
            return null;
        }
        if (expect(DlangTypes.OP_PAR_LEFT) == null) {
            cleanup(marker, DlangTypes.IMPORT_EXPRESSION);
            return null;
        }
        if (parseAssignExpression() == null) {
            cleanup(marker, DlangTypes.IMPORT_EXPRESSION);
            return null;
        }
        if (expect(DlangTypes.OP_PAR_RIGHT) == null) {
            cleanup(marker, DlangTypes.IMPORT_EXPRESSION);
            return null;
        }
        exit_section_modified(myBuilder, marker, DlangTypes.IMPORT_EXPRESSION, true);
        return marker;
    }

    /**
     * Parses an InContractExpression
     *
     *
     * $(GRAMMAR $(RULEDEF inContractExpression):
     * $(LITERAL 'in') $(LITERAL '$(LPAREN)') $(RULE assertArguments) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseInContractExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (expect(DlangTypes.KW_IN) == null) {
            cleanup(m, DlangTypes.IN_CONTRACT_EXPRESSION);
            return false;
        }
        if (expect(DlangTypes.OP_PAR_LEFT) == null) {
            cleanup(m, DlangTypes.IN_CONTRACT_EXPRESSION);
            return false;
        }
        if (!parseAssertArguments()) {
            cleanup(m, DlangTypes.IN_CONTRACT_EXPRESSION);
            return false;
        }
        if (expect(DlangTypes.OP_PAR_RIGHT) == null) {
            cleanup(m, DlangTypes.IN_CONTRACT_EXPRESSION);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.IN_CONTRACT_EXPRESSION, true);
        return true;
    }

    /**
     * Parses an InExpression
     *
     *
     * $(GRAMMAR $(RULEDEF inExpression):
     * $(RULE shiftExpression) ($(LITERAL 'in') | ($(LITERAL '!') $(LITERAL 'in'))) $(RULE shiftExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseInExpression(PsiBuilder.@Nullable Marker marker) {
        PsiBuilder.Marker m = marker;
        if (m == null) {
            m = myBuilder.mark();
            if (parseShiftExpression() == null) {
                m.done(DlangTypes.IN_EXPRESSION);
                return null;
            }
        }
        if (currentIs(DlangTypes.OP_NOT)) {
            advance();
        }
        if (!tokenCheck(DlangTypes.KW_IN)) {
            m.done(DlangTypes.IN_EXPRESSION);
            return null;
        }
        if (parseShiftExpression() == null) {
            m.done(DlangTypes.IN_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.IN_EXPRESSION);
        return m;
    }

    /**
     * Parses an InOutContractExpression
     *
     *
     * $(GRAMMAR $(RULEDEF inOutContractExpression):
     * $(RULE inContractExpression)
     * | $(RULE outContractExpression)
     * ;)
     */
    public boolean parseInOutContractExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (currentIs(DlangTypes.KW_IN)) {
            if (!parseInContractExpression()) {
                cleanup(m, DlangTypes.IN_OUT_CONTRACT_EXPRESSION);
                return false;
            }
        }
        else if (!parseOutContractExpression()) {
            cleanup(m, DlangTypes.IN_OUT_CONTRACT_EXPRESSION);
            return false;
        }
        else {
            cleanup(m, DlangTypes.IN_OUT_CONTRACT_EXPRESSION);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.IN_OUT_CONTRACT_EXPRESSION, true);
        return true;
    }

    /**
     * Parses an InOutStatement
     *
     *
     * $(GRAMMAR $(RULEDEF inOutStatement):
     * $(RULE inStatement)
     * | $(RULE outStatement)
     * ;)
     */
    public boolean parseInOutStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (currentIs(DlangTypes.KW_IN)) {
            if (!parseInStatement()) {
                cleanup(m, DlangTypes.IN_OUT_STATEMENT);
                return false;
            }
        }
        else if (currentIs(DlangTypes.KW_OUT)) {
            if (!parseOutStatement()) {
                cleanup(m, DlangTypes.IN_OUT_STATEMENT);
                return false;
            }
        }
        else {
            cleanup(m, DlangTypes.IN_OUT_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.IN_OUT_STATEMENT, true);
        return true;
    }

    /**
     * Parses an InStatement
     *
     *
     * $(GRAMMAR $(RULEDEF inStatement):
     * $(LITERAL 'in') $(RULE blockStatement)
     * ;)
     */
    public boolean parseInStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = expect(DlangTypes.KW_IN);
        if (i == null) {
            cleanup(m, DlangTypes.IN_STATEMENT);
            return false;
        }
        if (!parseBlockStatement()) {
            cleanup(m, DlangTypes.IN_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.IN_STATEMENT, true);
        return true;
    }

    /**
     * Parses an Initializer
     *
     *
     * $(GRAMMAR $(RULEDEF initializer):
     * $(LITERAL 'void')
     * | $(RULE nonVoidInitializer)
     * ;)
     */
    public boolean parseInitializer() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (currentIs(DlangTypes.KW_VOID)) {
            advance();
        }
        else if (!parseNonVoidInitializer()) {
            cleanup(m, DlangTypes.INITIALIZER);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.INITIALIZER, true);
        return true;
    }

    /**
     * Parses an InterfaceDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF interfaceDeclaration):
     * $(LITERAL 'interface') $(LITERAL Identifier) $(LITERAL ';')
     * | $(LITERAL 'interface') $(LITERAL Identifier) ($(LITERAL ':') $(RULE baseClassList))? $(RULE structBody)
     * | $(LITERAL 'interface') $(LITERAL Identifier) $(RULE templateParameters) $(RULE raint)? ($(LITERAL ':') $(RULE baseClassList))? $(RULE structBody)
     * | $(LITERAL 'interface') $(LITERAL Identifier) $(RULE templateParameters) ($(LITERAL ':') $(RULE baseClassList))? $(RULE raint)? $(RULE structBody)
     * ;)
     */
    public boolean parseInterfaceDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_INTERFACE) {
            return false;
        }
        myBuilder.advanceLexer();
        parseInterfaceOrClass();
        m.done(DlangTypes.INTERFACE_DECLARATION);
        return true;
    }

    /**
     * Parses an Invariant
     *
     *
     * $(GRAMMAR $(RULEDEF invariant):
     * $(LITERAL 'invariant') ($(LITERAL '$(LPAREN)') $(LITERAL '$(RPAREN)'))? $(RULE blockStatement)
     * | $(LITERAL 'invariant') $(LITERAL '$(LPAREN)') $(RULE assertArguments) $(LITERAL '$(RPAREN)') $(LITERAL ';')
     * ;)
     */
    public boolean parseInvariant(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_INVARIANT) {
            return false;
        }
        myBuilder.advanceLexer();
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            myBuilder.advanceLexer();
            if (myBuilder.getTokenType() != DlangTypes.OP_PAR_RIGHT) {
                if (!parseAssertArguments()) {
                    m.done(DlangTypes.INVARIANT);
                    return true;
                }
                if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
                    m.done(DlangTypes.INVARIANT);
                    return true;
                }
                if (!tokenCheck(DlangTypes.OP_SCOLON)) {
                    m.done(DlangTypes.INVARIANT);
                    return true;
                }
                m.done(DlangTypes.INVARIANT);
                return true;
            }
            myBuilder.advanceLexer();
        }
        parseBlockStatement();
        m.done(DlangTypes.INVARIANT);
        return true;
    }

    /**
     * Parses an IsExpression
     *
     *
     * $(GRAMMAR $(RULEDEF isExpression):
     * $(LITERAL'is') $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL identifier)? $(LITERAL '$(RPAREN)')
     * | $(LITERAL'is') $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL identifier)? $(LITERAL ':') $(RULE typeSpecialization) $(LITERAL '$(RPAREN)')
     * | $(LITERAL'is') $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL identifier)? $(LITERAL '=') $(RULE typeSpecialization) $(LITERAL '$(RPAREN)')
     * | $(LITERAL'is') $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL identifier)? $(LITERAL ':') $(RULE typeSpecialization) $(LITERAL ',') $(RULE templateParameterList) $(LITERAL '$(RPAREN)')
     * | $(LITERAL'is') $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL identifier)? $(LITERAL '=') $(RULE typeSpecialization) $(LITERAL ',') $(RULE templateParameterList) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseIsExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_IS)) {
            cleanup(m, DlangTypes.IS_EXPRESSION);
            return null;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.IS_EXPRESSION);
            return null;
        }
        if (!parseType()) {
            cleanup(m, DlangTypes.IS_EXPRESSION);
            return null;
        }
        if (currentIs(DlangTypes.ID)) {
            advance();
        }

        if (currentIsOneOf(DlangTypes.OP_EQ_EQ, DlangTypes.OP_COLON)) {
            advance();
            if (!parseTypeSpecialization()) {
                cleanup(m, DlangTypes.IS_EXPRESSION);
                return null;
            }
        }
        if (currentIs(DlangTypes.OP_COMMA)) {
            advance();
            if (!parseTemplateParameterList()) {
                cleanup(m, DlangTypes.IS_EXPRESSION);
                return null;
            }
        }
        tokenCheck(DlangTypes.OP_PAR_RIGHT);
        m.done(DlangTypes.IS_EXPRESSION);
        return m;
    }

    /**
     * Parses a KeyValuePair
     *
     *
     * $(GRAMMAR $(RULEDEF keyValuePair):
     * $(RULE assignExpression) $(LITERAL ':') $(RULE assignExpression)
     * ;)
     */
    public boolean parseKeyValuePair() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (parseAssignExpression() == null) {
            cleanup(m, DlangTypes.KEY_VALUE_PAIR);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_COLON)) {
            cleanup(m, DlangTypes.KEY_VALUE_PAIR);
            return false;
        }
        if (parseAssignExpression() == null) {
            cleanup(m, DlangTypes.KEY_VALUE_PAIR);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.KEY_VALUE_PAIR, true);
        return true;
    }

    /**
     * Parses KeyValuePairs
     *
     *
     * $(GRAMMAR $(RULEDEF keyValuePairs):
     * $(RULE keyValuePair) ($(LITERAL ',') $(RULE keyValuePair))* $(LITERAL ',')?
     * ;)
     */
    public boolean parseKeyValuePairs() {
        PsiBuilder.Marker m = myBuilder.mark();
        while (moreTokens()) {
            if (!parseKeyValuePair()) {
                cleanup(m, DlangTypes.KEY_VALUE_PAIRS);
                return false;
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
                if (currentIs(DlangTypes.OP_BRACKET_RIGHT)) {
                    break;
                }
            }
            else {
                break;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.KEY_VALUE_PAIRS, true);
        return true;
    }

    /**
     * Parses a LabeledStatement
     *
     *
     * $(GRAMMAR $(RULEDEF labeledStatement):
     * $(LITERAL Identifier) $(LITERAL ':') $(RULE Statement)?
     * ;)
     */
    public boolean parseLabeledStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            cleanup(m, DlangTypes.LABELED_STATEMENT);
            return false;
        }
        expect(DlangTypes.OP_COLON);
        if (!currentIs(DlangTypes.OP_BRACES_RIGHT)) {
            if (!parseStatement()) {
                cleanup(m, DlangTypes.LABELED_STATEMENT);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.LABELED_STATEMENT, true);
        return true;
    }

    /**
     * Parses a LastCatch
     *
     *
     * $(GRAMMAR $(RULEDEF lastCatch):
     * $(LITERAL 'catch') $(RULE statementNoCaseNoDefault)
     * ;)
     */
    public boolean parseLastCatch() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType t = expect(DlangTypes.KW_CATCH);
        if (t == null) {
            cleanup(m, DlangTypes.LAST_CATCH);
            return false;
        }
        if (!parseNonEmptyStatementNoCaseNoDefault()) {
            cleanup(m, DlangTypes.LAST_CATCH);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.LAST_CATCH, true);
        return true;
    }

    /**
     * Parses a LinkageAttribute
     *
     *
     * $(GRAMMAR $(RULEDEF linkageAttribute):
     * $(LITERAL 'extern') $(LITERAL '$(LPAREN)') $(LITERAL Identifier) $(LITERAL '$(RPAREN)')
     * | $(LITERAL 'extern') $(LITERAL '$(LPAREN)') $(LITERAL Identifier) $(LITERAL '-') $(LITERAL Identifier) $(LITERAL '$(RPAREN)')
     * | $(LITERAL 'extern') $(LITERAL '$(LPAREN)') $(LITERAL Identifier) $(LITERAL '++') ($(LITERAL ',') $(RULE QualifiedIdentifier) | $(RULE namespaceList) | $(LITERAL 'struct') | $(LITERAL 'class'))? $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseLinkageAttribute() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_EXTERN)) {
            cleanup(m, DlangTypes.LINKAGE_ATTRIBUTE);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.LINKAGE_ATTRIBUTE);
            return false;
        }
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            cleanup(m, DlangTypes.LINKAGE_ATTRIBUTE);
            return false;
        }
        if (currentIs(DlangTypes.OP_PLUS_PLUS)) {
            advance();
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
                if (currentIsOneOf(DlangTypes.KW_STRUCT, DlangTypes.KW_CLASS)) {
                    advance();
                }
                else if (currentIs(DlangTypes.ID)) {
                    if (!parseQualifiedIdentifier()) {
                        cleanup(m, DlangTypes.LINKAGE_ATTRIBUTE);
                        return false;
                    }
                }
                else {
                    if (!parseNamespaceList()) {
                        cleanup(m, DlangTypes.LINKAGE_ATTRIBUTE);
                        return false;
                    }
                }
            }
        }
        else if (currentIs(DlangTypes.OP_MINUS)) {
            advance();
            if (!tokenCheck(DlangTypes.ID)) {
                cleanup(m, DlangTypes.LINKAGE_ATTRIBUTE);
                return false;
            }
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        exit_section_modified(myBuilder, m, DlangTypes.LINKAGE_ATTRIBUTE, true);
        return true;
    }

    /**
     * Parses a MemberFunctionAttribute
     *
     *
     * $(GRAMMAR $(RULEDEF memberFunctionAttribute):
     * $(RULE functionAttribute)
     * | $(LITERAL 'immutable')
     * | $(LITERAL 'inout')
     * | $(LITERAL 'shared')
     * | $(LITERAL 'const')
     * | $(LITERAL 'return')
     * | $(LITERAL 'scope')
     * | $(LITERAL 'throw')
     * ;)
     */
    public boolean parseMemberFunctionAttribute() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!moreTokens()) {
            cleanup(m, DlangTypes.MEMBER_FUNCTION_ATTRIBUTE);
            return false;
        }
        IElementType i = current();
        if (i == DlangTypes.OP_AT) {
            if (!parseAtAttribute()) {
                cleanup(m, DlangTypes.MEMBER_FUNCTION_ATTRIBUTE);
                return false;
            }
        }
        else if (i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_INOUT || i == DlangTypes.KW_SHARED || i == DlangTypes.KW_CONST || i == DlangTypes.KW_PURE || i == DlangTypes.KW_NOTHROW || i == DlangTypes.KW_RETURN || i == DlangTypes.KW_SCOPE || i == DlangTypes.KW_THROW) {
            advance();
        }
        else {
            error("Member function attribute expected");
        }
        exit_section_modified(myBuilder, m, DlangTypes.MEMBER_FUNCTION_ATTRIBUTE, true);
        return true;
    }

    /**
     * Parses a MissingFunctionBody
     *
     *
     * $(GRAMMAR $(RULEDEF missingFunctionBody):
     * $(LITERAL ';')
     * | $(RULE functionContract)* $(LITERAL ';')
     * ;)
     */
    public boolean parseMissingFunctionBody() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean haveContract = false;
        boolean lastIsOutContractExpression = false;
        while (currentIsOneOf(DlangTypes.KW_IN, DlangTypes.KW_OUT)) {
            boolean isOut = currentIs(DlangTypes.KW_OUT);
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (parseFunctionContract(false)) {
                lastIsOutContractExpression = isOut;
            }
            else {
                lastIsOutContractExpression = false;
                bookmark.rollbackTo();
                if (parseFunctionContract()) {
                    haveContract = true;
                }
            }
            bookmark.drop();
        }
        if (!haveContract || lastIsOutContractExpression) {
            if (expect(DlangTypes.OP_SCOLON) == null) {
                cleanup(m, DlangTypes.MISSING_FUNCTION_BODY);
                return false;
            }
        }
        else if (moreTokens() && currentIsOneOf(DlangTypes.KW_DO, DlangTypes.OP_LAMBDA_ARROW)) {
            cleanup(m, DlangTypes.MISSING_FUNCTION_BODY);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.MISSING_FUNCTION_BODY, true);
        return true;
    }

    /**
     * Parses a MixinDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF mixinDeclaration):
     * $(LITERAL 'mixin') $(LITERAL '$(LPAREN)') $(RULE argumentList) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseMixinDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_MIXIN) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() != DlangTypes.OP_PAR_LEFT) {
            bookmark.rollbackTo();
            return false;
        }
        myBuilder.advanceLexer();
        if (!parseArgumentList()) {
            bookmark.rollbackTo();
            m.done(DlangTypes.MIXIN_DECLARATION);
            return false;
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        if (myBuilder.getTokenType() != DlangTypes.OP_SCOLON) {
            // if no scolon, it’s probably a mixin type part of a function declaration
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        myBuilder.advanceLexer();
        m.done(DlangTypes.MIXIN_DECLARATION);
        return true;
    }

    /**
     * Parses a MixinType
     *
     *
     * $(GRAMMAR $(RULEDEF mixinType):
     * $(LITERAL 'mixin') $(LITERAL '$(LPAREN)') $(RULE argumentList) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseMixinType() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_MIXIN);
        if (myBuilder.getTokenType() != DlangTypes.OP_PAR_LEFT) {
            m.rollbackTo();
            return false;
        }
        myBuilder.advanceLexer();
        if (!parseArgumentList()) {
            cleanup(m, DlangTypes.MIXIN_TYPE);
            return false;
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        m.done(DlangTypes.MIXIN_TYPE);
        return true;
    }

    public PsiBuilder.@Nullable Marker parseMixinExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_MIXIN);
        if (myBuilder.getTokenType() != DlangTypes.OP_PAR_LEFT) {
            m.rollbackTo();
            return null;
        }
        myBuilder.advanceLexer();
        if (!parseArgumentList()) {
            cleanup(m, DlangTypes.MIXIN_EXPRESSION);
            return null;
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        m.done(DlangTypes.MIXIN_EXPRESSION);
        return m;
    }

    /**
     * Parses a MixinTemplateDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF mixinTemplateDeclaration):
     * $(LITERAL 'mixin') $(RULE templateDeclaration)
     * ;)
     */
    public boolean parseTemplateMixinDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_MIXIN) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() != DlangTypes.KW_TEMPLATE) {
            bookmark.rollbackTo();
            return false;
        }
        if (!parseTemplateDeclarationCommon()) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        m.done(DlangTypes.TEMPLATE_MIXIN_DECLARATION);
        return true;
    }

    /**
     * Parses a MixinTemplateName
     *
     *
     * $(GRAMMAR $(RULEDEF mixinTemplateName):
     * $(LITERAL '.')? $(RULE mixinQualifiedIdentifier)
     * | $(RULE typeofExpression) $(LITERAL '.') $(RULE mixinQualifiedIdentifier)
     * ;)
     */
    public boolean parseMixinTemplateName() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (currentIs(DlangTypes.KW_TYPEOF)) {
            if (parseTypeofExpression() != null) {
                cleanup(m, DlangTypes.MIXIN_TEMPLATE_NAME);
                return false;
            }
            expect(DlangTypes.OP_DOT);
            if (!parseMixinQualifiedIdentifier()) {
                cleanup(m, DlangTypes.MIXIN_TEMPLATE_NAME);
                return false;
            }
        }
        else {
            if (currentIs(DlangTypes.OP_DOT)) {
                myBuilder.advanceLexer();
            }
            if (!parseMixinQualifiedIdentifier()) {
                cleanup(m, DlangTypes.MIXIN_TEMPLATE_NAME);
                return false;
            }
        }
        m.done(DlangTypes.MIXIN_TEMPLATE_NAME);
        return true;
    }

    /**
     * Parses a Module
     *
     *
     * $(GRAMMAR $(RULEDEF module):
     * $(RULE moduleDeclaration)? $(RULE declaration)*
     * ;)
     */
    public void parseModule() {
        if (currentIs(DlangTypes.SHEBANG)) {
            advance();
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        while (currentIs(DlangTypes.OP_AT) || currentIs(DlangTypes.KW_DEPRECATED)) {
            parseAttribute();
        }
        boolean isModule = currentIs(DlangTypes.KW_MODULE);
        bookmark.rollbackTo();
        if (isModule) {
            parseModuleDeclaration();
        }
        while (!myBuilder.eof()) {
            // To prevent infinite loop if top level } are present
            if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_RIGHT) {
                PsiBuilder.Marker error = myBuilder.mark();
                myBuilder.advanceLexer();
                error.error(LocalizeValue.localizeTODO("Unexpected '}'"));
            }
            parseDeclDefWithRecovery();
        }
    }

    /**
     * Parses a ModuleDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF moduleDeclaration):
     * $(RULE atAttribute)* $(RULE deprecated)? $(RULE atAttribute)* $(LITERAL 'module') $(RULE identifierChain) $(LITERAL ';')
     * ;)
     */
    public void parseModuleDeclaration() {
        PsiBuilder.Marker m = myBuilder.mark();
        m.setCustomEdgeTokenBinders(LeadingDocCommentBinder.INSTANCE, TrailingDocCommentBinder.INSTANCE);
        while (currentIs(DlangTypes.OP_AT)) {
            parseAttribute();
        }
        if (currentIs(DlangTypes.KW_DEPRECATED)) {
            if (!parseDeprecated()) {
                m.done(DlangTypes.MODULE_DECLARATION);
                return;
            }
        }
        while (currentIs(DlangTypes.OP_AT)) {
            parseAttribute();
        }
        IElementType start = expect(DlangTypes.KW_MODULE);
        if (start == null) {
            m.done(DlangTypes.MODULE_DECLARATION);
            return;
        }
        if (parseIdentifierChain() == null) {
            m.done(DlangTypes.MODULE_DECLARATION);
            return;
        }
        expect(DlangTypes.OP_SCOLON);
        m.done(DlangTypes.MODULE_DECLARATION);
    }

    /**
     * Parses a MulExpression.
     *
     *
     * $(GRAMMAR $(RULEDEF mulExpression):
     * $(RULE powExpression)
     * | $(RULE mulExpression) ($(LITERAL '*') | $(LITERAL '/') | $(LITERAL '%')) $(RULE powExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseMulExpression() {
        PsiBuilder.Marker m = parseUnaryExpression();
        if (m == null) {
            return null;
        }
        while (currentIsOneOf(DlangTypes.OP_ASTERISK, DlangTypes.OP_DIV, DlangTypes.OP_MOD)) {
            m = m.precede();
            myBuilder.advanceLexer();
            if (parseUnaryExpression() == null) {
                m.done(DlangTypes.MUL_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.MUL_EXPRESSION);
        }
        return m;
    }


    /**
     * Parses a NamespaceList.
     *
     *
     * $(GRAMMAR $(RULEDEF namespaceList):
     * $(RULE ternaryExpression) ($(LITERAL ',') $(RULE ternaryExpression)?)* $(LITERAL ',')?
     * ;)
     */
    public boolean parseNamespaceList() {
        PsiBuilder.Marker marker = myBuilder.mark();
        while (moreTokens()) {
            if (parseTernaryExpression() == null) {
                marker.drop();
                return false;
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
                if (currentIsOneOf(DlangTypes.OP_PAR_RIGHT)) {
                    break;
                }
            }
            else {
                break;
            }
        }
        exit_section_modified(myBuilder, marker, DlangTypes.NAMESPACE_LIST, true);
        return true;
    }

    /**
     * Parses a NewAnonClassExpression
     *
     * $(GRAMMAR $(RULEDEF newAnonClassExpression):
     * $(LITERAL 'new') $(RULE placementExpression)? $(LITERAL 'class') $(RULE arguments)? $(RULE baseClassList)? $(RULE structBody)
     * ;)
     */
    public boolean parseNewAnonClassExpression(PsiBuilder.Marker marker) {
        assert myBuilder.getTokenType() == DlangTypes.KW_CLASS;
        myBuilder.advanceLexer();
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            myBuilder.advanceLexer();
            if (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
                parseNamedArgumentList();
            }
            if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
                marker.done(DlangTypes.NEW_ANON_CLASS_EXPRESSION);
                return false;
            }
        }
        if (!currentIs(DlangTypes.OP_BRACES_LEFT)) {
            parseBaseClassList();
        }
        parseStructBody();
        marker.done(DlangTypes.NEW_ANON_CLASS_EXPRESSION);
        return true;
    }

    /**
     * Parses a NewExpression
     *
     * $(LITERAL 'new') $(RULE placementExpression)? $(RULE type) (($(LITERAL '[') $(RULE assignExpression) $(LITERAL ']')) | $(RULE arguments))?
     * | $(RULE newAnonClassExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseNewExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        PsiBuilder.Marker newAnnonClassMarker = myBuilder.mark();
        expect(DlangTypes.KW_NEW);

        if (myBuilder.getTokenType() == DlangTypes.OP_PAR_LEFT) {
            if (!parsePlacementNew()) {
                newAnnonClassMarker.drop();
                m.done(DlangTypes.NEW_EXPRESSION);
                return null;
            }
        }

        if (myBuilder.getTokenType() == DlangTypes.KW_CLASS) {
            boolean res = parseNewAnonClassExpression(newAnnonClassMarker);
            m.done(DlangTypes.NEW_EXPRESSION);
            return res ? m : null;
        }

        newAnnonClassMarker.drop();
        if (!parseType(true)) {
            m.done(DlangTypes.NEW_EXPRESSION);
            return null;
        }
        if (currentIs(DlangTypes.OP_BRACKET_LEFT)) {
            myBuilder.advanceLexer();
            if (parseAssignExpression() == null) {
                m.done(DlangTypes.NEW_EXPRESSION);
                return null;
            }
            expect(DlangTypes.OP_BRACKET_RIGHT);
        }
        else if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            myBuilder.advanceLexer();
            if (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
                parseNamedArgumentList();
            }
            expect(DlangTypes.OP_PAR_RIGHT);
        }
        m.done(DlangTypes.NEW_EXPRESSION);
        return m;
    }

    /**
     * Parses a placement new
     *
     * $(LITERAL '(') $(RULE assignExpression) $(LITERAL ')'
     */
    public boolean parsePlacementNew() {
        assert myBuilder.getTokenType() == DlangTypes.OP_PAR_LEFT;
        PsiBuilder.Marker m = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() == DlangTypes.OP_PAR_RIGHT) {
            myBuilder.error(LocalizeValue.localizeTODO("Expression expected"));
        }
        else if (parseAssignExpression() == null) {
            m.done(DlangTypes.NEW_EXPRESSION);
            return false;
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        m.done(DlangTypes.PLACEMENT_NEW);
        return true;
    }

    /**
     * Parses a NonVoidInitializer
     *
     *
     * $(GRAMMAR $(RULEDEF nonVoidInitializer):
     *   $(RULE arrayLiteral)
     * | $(RULE structInitializer)
     * |  $(RULE assignExpression)
     * ;)
     */
    public boolean parseNonVoidInitializer() {
        if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_LEFT) {
            IElementType b = peekPastBraces();
            if (b == DlangTypes.OP_PAR_LEFT) {
                // Anonymous function call
                return parseExpression();
            }
            if (isExpInitializer()) {
                return parseExpression();
            }
            return parseStructInitializer();
        }
        else if (myBuilder.getTokenType() == DlangTypes.OP_BRACKET_LEFT) {
            IElementType b = peekPastBrackets();
            if (b == DlangTypes.OP_DOT || b == DlangTypes.OP_BRACKET_LEFT) {
                // Array literal . something expression or, define and access static array expression
                return parseExpression();
            }
            return parseArrayInitializer();
        }
        return parseAssignExpression() != null;
    }

    /**
     * Parses Operands
     *
     *
     * $(GRAMMAR $(RULEDEF operands):
     * $(RULE asmExp)
     * | $(RULE asmExp) $(LITERAL ',') $(RULE operands)
     * ;)
     */
    public boolean parseOperands() {
        PsiBuilder.Marker marker = myBuilder.mark();
        while (true) {
            if (!(parseAsmExp())) {
                cleanup(marker, DlangTypes.OPERANDS);
                return false;
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
            }
            else {
                break;
            }
        }
        exit_section_modified(myBuilder, marker, DlangTypes.OPERANDS, true);
        return true;
    }

    /**
     * Parses an OrExpression
     *
     *
     * $(GRAMMAR $(RULEDEF orExpression):
     * $(RULE xorExpression)
     * | $(RULE orExpression) $(LITERAL '|') $(RULE xorExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseOrExpression() {
        PsiBuilder.Marker m = parseXorExpression();
        if (m == null) {
            return null;
        }
        while (currentIs(DlangTypes.OP_OR)) {
            m = m.precede();
            myBuilder.advanceLexer();
            if (parseXorExpression() == null) {
                m.done(DlangTypes.OR_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.OR_EXPRESSION);
        }
        return m;
    }

    /**
     * Parses an OrOrExpression
     *
     *
     * $(GRAMMAR $(RULEDEF orOrExpression):
     * $(RULE andAndExpression)
     * | $(RULE orOrExpression) $(LITERAL '||') $(RULE andAndExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseOrOrExpression() {
        PsiBuilder.Marker m = parseAndAndExpression();
        if (m == null) {
            return null;
        }
        while (currentIs(DlangTypes.OP_BOOL_OR)) {
            m = m.precede();
            myBuilder.advanceLexer();
            if (parseAndAndExpression() == null) {
                cleanup(m, DlangTypes.OR_OR_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.OR_OR_EXPRESSION);
        }
        return m;
    }

    /**
     * Parses an OutContractExpression
     *
     *
     * $(GRAMMAR $(RULEDEF outContractExpression):
     * $(LITERAL 'out') $(LITERAL '$(LPAREN)') $(LITERAL Identifier)? $(LITERAL ';') $(RULE assertArguments) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseOutContractExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType o = expect(DlangTypes.KW_OUT);
        if (o == null) {
            cleanup(m, DlangTypes.OUT_CONTRACT_EXPRESSION);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.OUT_CONTRACT_EXPRESSION);
            return false;
        }
        if (currentIs(DlangTypes.ID)) {
            advance();
        }
        if (!tokenCheck(DlangTypes.OP_SCOLON)) {
            cleanup(m, DlangTypes.OUT_CONTRACT_EXPRESSION);
            return false;
        }
        if (!parseAssertArguments()) {
            cleanup(m, DlangTypes.OUT_CONTRACT_EXPRESSION);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.OUT_CONTRACT_EXPRESSION);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.OUT_CONTRACT_EXPRESSION, true);
        return true;
    }

    /**
     * Parses an OutStatement
     *
     *
     * $(GRAMMAR $(RULEDEF outStatement):
     * $(LITERAL 'out') ($(LITERAL '$(LPAREN)') $(LITERAL Identifier) $(LITERAL '$(RPAREN)'))? $(RULE blockStatement)
     * ;)
     */
    public boolean parseOutStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType o = expect(DlangTypes.KW_OUT);
        if (o == null) {
            cleanup(m, DlangTypes.OUT_STATEMENT);
            return false;
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            advance();
            IElementType ident = expect(DlangTypes.ID);
            if (ident == null) {
                cleanup(m, DlangTypes.OUT_STATEMENT);
                return false;
            }
            expect(DlangTypes.OP_PAR_RIGHT);
        }
        if (!parseBlockStatement()) {
            cleanup(m, DlangTypes.OUT_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.OUT_STATEMENT, true);
        return true;
    }

    /**
     * Parses a Parameter
     *
     *
     * $(GRAMMAR $(RULEDEF parameter):
     * $(RULE parameterAttribute)* $(RULE type)
     * | $(RULE parameterAttribute)* $(RULE type) $(LITERAL Identifier)? $(LITERAL '...')
     * | $(RULE parameterAttribute)* $(RULE type) $(LITERAL Identifier)? ($(LITERAL '=') $(RULE assignExpression))?
     * ;)
     */
    public boolean parseParameter() {
        PsiBuilder.Marker m = myBuilder.mark();
        while (!myBuilder.eof()) {
            if (!parseParameterAttribute()) {
                break;
            }
        }
        // Parsed the attributes of the variadic attributes.
        // Abort and defer to parseVariadicArgumentsAttributes
        if (currentIs(DlangTypes.OP_TRIPLEDOT)) {
            cleanup(m, DlangTypes.PARAMETER);
            return false;
        }

        if (!parseType()) {
            cleanup(m, DlangTypes.PARAMETER);
            return false;
        }
        if (currentIs(DlangTypes.ID)) {
            advance();
            if (currentIs(DlangTypes.OP_TRIPLEDOT)) {
                advance();
            }
            else if (currentIs(DlangTypes.OP_EQ)) {
                advance();
                if (parseAssignExpression() == null) {
                    cleanup(m, DlangTypes.PARAMETER);
                    return false;
                }
                if (currentIs(DlangTypes.OP_TRIPLEDOT)) {
                    advance();
                }
            }
            else if (currentIs(DlangTypes.OP_BRACKET_LEFT)) {
                while (moreTokens() && currentIs(DlangTypes.OP_BRACKET_LEFT)) {
                    if (!(parseTypeSuffix())) {
                        cleanup(m, DlangTypes.PARAMETER);
                        return false;
                    }
                }
            }
        }
        else if (currentIs(DlangTypes.OP_TRIPLEDOT)) {
            advance();
        }
        else if (currentIs(DlangTypes.OP_EQ)) {
            advance();
            if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.PARAMETER);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.PARAMETER, true);
        return true;
    }

    /**
     * Parses a ParameterAttribute
     *
     *
     * $(GRAMMAR $(RULEDEF parameterAttribute):
     * $(RULE atAttribute)
     * | $(RULE typeConstructor)
     * | $(LITERAL 'final')
     * | $(LITERAL 'in')
     * | $(LITERAL 'lazy')
     * | $(LITERAL 'out')
     * | $(LITERAL 'ref')
     * | $(LITERAL 'scope')
     * | $(LITERAL 'auto')
     * | $(LITERAL 'return')
     * ;)
     */
    public boolean parseParameterAttribute() {
        IElementType i = myBuilder.getTokenType();
        if (i == DlangTypes.OP_AT) {
            if (!parseAtAttribute()) {
                error("Parameter attribute expected");
                return false;
            }
            return true;
        }
        else if (i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_SHARED || i == DlangTypes.KW_CONST || i == DlangTypes.KW_INOUT) {
            if (peekIs(DlangTypes.OP_PAR_LEFT)) {
                return false;
            }
            else {
                myBuilder.advanceLexer();
            }
        }
        else if (i == DlangTypes.KW_FINAL || i == DlangTypes.KW_IN || i == DlangTypes.KW_LAZY || i == DlangTypes.KW_OUT || i == DlangTypes.KW_REF || i == DlangTypes.KW_SCOPE || i == DlangTypes.KW_AUTO || i == DlangTypes.KW_RETURN) {
            myBuilder.advanceLexer();
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * Parse a single parameter of a FunctionLiteral Expression
     */
    public void parseSingleParameter() {
        PsiBuilder.Marker parametersListMarker = myBuilder.mark();
        PsiBuilder.Marker parameterMarker = myBuilder.mark();
        assert myBuilder.getTokenType() == DlangTypes.ID;
        myBuilder.advanceLexer();
        parameterMarker.done(DlangTypes.PARAMETER);
        parametersListMarker.done(DlangTypes.PARAMETERS);
    }

    /**
     * Parses Parameters
     *
     *
     * $(GRAMMAR $(RULEDEF parameters):
     * $(LITERAL '$(LPAREN)') $(RULE parameter) ($(LITERAL ',') $(RULE parameter))* ($(LITERAL ',') $(LITERAL '...'))? $(LITERAL '$(RPAREN)')
     * | $(LITERAL '$(LPAREN)') $(LITERAL '...') $(LITERAL '$(RPAREN)')
     * | $(LITERAL '$(LPAREN)') $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseParameters() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.PARAMETERS);
            return false;
        }

        if (currentIs(DlangTypes.OP_PAR_RIGHT)) {
            advance(); // )
            exit_section_modified(myBuilder, m, DlangTypes.PARAMETERS, true);
            return true;
        }
        if (currentIs(DlangTypes.OP_TRIPLEDOT)) {
            advance();
            if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
                cleanup(m, DlangTypes.PARAMETERS);
                return false;
            }
            exit_section_modified(myBuilder, m, DlangTypes.PARAMETERS, true);
            return true;
        }
        while (moreTokens()) {
            if (currentIs(DlangTypes.OP_TRIPLEDOT)) {
                advance();
                break;
            }
            if (currentIs(DlangTypes.OP_PAR_RIGHT)) {
                break;
            }
            // Save starting point to deal with attributed variadics, e.g.
            // int printf(in char* format, scope const ...);
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (!(parseParameter())) {
                // parseParameter fails for C-style variadics, they are parsed below
                if (!currentIs(DlangTypes.OP_TRIPLEDOT)) {
                    bookmark.drop();
                    cleanup(m, DlangTypes.PARAMETERS);
                    return false;
                }
                // Reset to the beginning of the current parameters
                bookmark.rollbackTo();
                if (!parseVariadicArgumentsAttributes()) {
                    cleanup(m, DlangTypes.PARAMETERS);
                    return false;
                }
                if (!tokenCheck(DlangTypes.OP_TRIPLEDOT)) {
                    cleanup(m, DlangTypes.PARAMETERS);
                    return false;
                }
            }
            else {
                bookmark.drop();
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
            }
            else {
                break;
            }
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.PARAMETERS);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.PARAMETERS, true);
        return true;
    }

    /**
     * Parses attributes of C-style variadic parameters.
     *
     *
     * $(GRAMMAR $(RULEDEF variadicArgumentsAttributes):
     * $(RULE variadicArgumentsAttribute)+
     * ;)
     */
    public boolean parseVariadicArgumentsAttributes() {
        PsiBuilder.Marker m = myBuilder.mark();
        while (moreTokens() && !currentIs(DlangTypes.OP_TRIPLEDOT)) {
            if (!parseVariadicArgumentsAttribute()) {
                cleanup(m, DlangTypes.VARIADIC_ARGUMENTS_ATTRIBUTES);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.VARIADIC_ARGUMENTS_ATTRIBUTES, true);
        return true;
    }

    /**
     * Parses an attribute of C-style variadic parameters.
     *
     *
     * $(GRAMMAR $(RULEDEF variadicArgumentsAttribute):
     * $(LITERAL 'const')
     * | $(LITERAL 'immutable')
     * | $(LITERAL 'scope')
     * | $(LITERAL 'shared')
     * | $(LITERAL 'return')
     * ;)
     */
    public boolean parseVariadicArgumentsAttribute() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!currentIsOneOf(
            DlangTypes.KW_CONST,
            DlangTypes.KW_IMMUTABLE,
            DlangTypes.KW_SHARED,
            DlangTypes.KW_SCOPE,
            DlangTypes.KW_RETURN
        )) {
            error("`const`, `immutable`, `shared`, `scope` or `return` expected");
            cleanup(m, DlangTypes.VARIADIC_ARGUMENTS_ATTRIBUTE);
            return false;
        }
        advance();
        exit_section_modified(myBuilder, m, DlangTypes.VARIADIC_ARGUMENTS_ATTRIBUTE, true);
        return true;
    }

    /**
     * Parses a Postblit
     *
     *
     * $(GRAMMAR $(RULEDEF postblit):
     * $(LITERAL 'this') $(LITERAL '$(LPAREN)') $(LITERAL 'this') $(LITERAL '$(RPAREN)') $(RULE memberFunctionAttribute)* ($(RULE functionBody) | $(LITERAL ';'))
     * ;)
     */
    public boolean parsePostblit(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_THIS) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        advance();
        if (myBuilder.getTokenType() != DlangTypes.OP_PAR_LEFT) {
            bookmark.rollbackTo();
            return false;
        }
        advance();
        if (myBuilder.getTokenType() != DlangTypes.KW_THIS) {
            // probably a constructor
            bookmark.rollbackTo();
            return false;
        }
        advance();
        if (myBuilder.getTokenType() != DlangTypes.OP_PAR_RIGHT) {
            // probably a template taking this as template argument
            bookmark.rollbackTo();
            return false;
        }
        advance();
        bookmark.drop();
        while (currentIsMemberFunctionAttribute()) {
            if (!parseMemberFunctionAttribute()) {
                m.done(DlangTypes.POSTBLIT);
                return true;
            }
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
        }
        else if (!parseFunctionBody()) {
            m.done(DlangTypes.POSTBLIT);
            return true;
        }
        m.done(DlangTypes.POSTBLIT);
        return true;
    }

    /**
     * Parses a PowExpression
     *
     *
     * $(GRAMMAR $(RULEDEF powExpression):
     * $(RULE unaryExpression)
     * | $(RULE powExpression) $(LITERAL '^^') $(RULE unaryExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parsePowExpression() {
        PsiBuilder.Marker m = parsePostfixExpression();
        if (m == null) {
            return null;
        }
        if (myBuilder.getTokenType() == DlangTypes.OP_POW) {
            PsiBuilder.Marker marker = m.precede();
            myBuilder.advanceLexer();
            if (parseUnaryExpression() == null) {
                marker.done(DlangTypes.POW_EXPRESSION);
                return null;
            }
            marker.done(DlangTypes.POW_EXPRESSION);
            return marker;
        }
        return m;
    }

    /**
     * Parses a PragmaExpression
     *
     *
     * $(GRAMMAR $(RULEDEF pragmaExpression):
     * $(RULE 'pragma') $(LITERAL '$(LPAREN)') $(LITERAL Identifier) ($(LITERAL ',') $(RULE argumentList))? $(LITERAL '$(RPAREN)')
     * ;)
     */
    // Todo rename parsePragma (it is not an expression)
    public boolean parsePragmaExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_PRAGMA);
        expect(DlangTypes.OP_PAR_LEFT);
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            cleanup(m, DlangTypes.PRAGMA_EXPRESSION);
            return false;
        }
        if (currentIs(DlangTypes.OP_COMMA)) {
            advance();
            if (!parseArgumentList()) {
                cleanup(m, DlangTypes.PRAGMA_EXPRESSION);
                return false;
            }
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        exit_section_modified(myBuilder, m, DlangTypes.PRAGMA_EXPRESSION, true);
        return true;
    }

    /**
     * Parses a PragmaStatement
     *
     * $(GRAMMAR $(RULEDEF pragmaStatement):
     * $(RULE pragmaExpression) $(RULE statement)
     * | $(RULE pragmaExpression) $(RULE blockStatement)
     * | $(RULE pragmaExpression) $(LITERAL ';')
     * ;)
     */
    public boolean parsePragmaStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!parsePragmaExpression()) {
            cleanup(m, DlangTypes.PRAGMA_STATEMENT);
            return false;
        }
        if (current() == DlangTypes.OP_BRACES_LEFT) {
            if (!parseBlockStatement()) {
                cleanup(m, DlangTypes.PRAGMA_STATEMENT);
                return false;
            }
        }
        else if (current() == DlangTypes.OP_SCOLON) {
            advance();
        }
        else {
            if (!parseNonEmptyStatement()) {
                cleanup(m, DlangTypes.PRAGMA_STATEMENT);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.PRAGMA_STATEMENT, true);
        return true;
    }

    /**
     * Parses a PrimaryExpression
     *
     *
     * $(GRAMMAR $(RULEDEF primaryExpression):
     * $(RULE identifierOrTemplateInstance)
     * | $(LITERAL '.') $(RULE identifierOrTemplateInstance)
     * | $(RULE typeConstructor) $(LITERAL '$(LPAREN)') $(RULE basicType) $(LITERAL '$(RPAREN)') $(LITERAL '.') $(LITERAL Identifier)
     * | $(RULE basicType) $(LITERAL '.') $(LITERAL Identifier)
     * | $(RULE basicType) $(RULE arguments)
     * | $(RULE typeofExpression)
     * | $(RULE typeidExpression)
     * | $(RULE vector)
     * | $(RULE arrayLiteral)
     * | $(RULE assocArrayLiteral)
     * | $(LITERAL '$(LPAREN)') $(RULE expression) $(LITERAL '$(RPAREN)')
     * | $(RULE isExpression)
     * | $(RULE functionLiteralExpression)
     * | $(RULE traitsExpression)
     * | $(RULE mixinExpression)
     * | $(RULE importExpression)
     * | $(LITERAL '$')
     * | $(LITERAL 'this')
     * | $(LITERAL 'super')
     * | $(LITERAL '_null')
     * | $(LITERAL '_true')
     * | $(LITERAL '_false')
     * | $(LITERAL '___DATE__')
     * | $(LITERAL '___TIME__')
     * | $(LITERAL '___TIMESTAMP__')
     * | $(LITERAL '___VENDOR__')
     * | $(LITERAL '___VERSION__')
     * | $(LITERAL '___FILE__')
     * | $(LITERAL '___LINE__')
     * | $(LITERAL '___MODULE__')
     * | $(LITERAL '___FUNCTION__')
     * | $(LITERAL '___PRETTY_FUNCTION__')
     * | $(LITERAL IntegerLiteral)
     * | $(LITERAL FloatLiteral)
     * | $(LITERAL StringLiteral)+
     * | $(LITERAL CharacterLiteral)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parsePrimaryExpression() {
        IElementType i = current();

        // Special keyword
        if (i == DlangTypes.KW___FILE__ || i == DlangTypes.KW___FILE_FULL_PATH__ || i == DlangTypes.KW___MODULE__ || i == DlangTypes.KW___LINE__ || i == DlangTypes.KW___FUNCTION__ || i == DlangTypes.KW___PRETTY_FUNCTION__) {
            PsiBuilder.Marker m = myBuilder.mark();
            advance();
            exit_section_modified(myBuilder, m, DlangTypes.MAGIC_LITERAL_EXPRESSION, true);
            return m;
        }

        // Special token
        if (i == DlangTypes.KW___DATE__ || i == DlangTypes.KW___TIME__ || i == DlangTypes.KW___TIMESTAMP__ || i == DlangTypes.KW___VENDOR__ || i == DlangTypes.KW___VERSION__) {
            PsiBuilder.Marker m = myBuilder.mark();
            advance();
            exit_section_modified(myBuilder, m, DlangTypes.MAGIC_LITERAL_EXPRESSION, true);
            return m;
        }

        if (i == DlangTypes.ID && peekIs(DlangTypes.OP_LAMBDA_ARROW)) {
            return parseFunctionLiteralExpression();
        }

        // [.] (Identifier|TemplateInstance)
        if (i == DlangTypes.OP_DOT || i == DlangTypes.ID) {
            return primaryExpressionIdentifierCase();
        }

        if (i == DlangTypes.OP_DOLLAR) {
            PsiBuilder.Marker m = myBuilder.mark();
            myBuilder.advanceLexer();
            m.done(DlangTypes.DOLLAR_EXPRESSION);
            return m;
        }

        // LiteralExpression
        if (isLiteral(i)) {
            PsiBuilder.Marker m = myBuilder.mark();
            if (currentIsOneOf(STRING_LITERALS_ARRAY)) {
                do {
                    parseString();
                }
                while (currentIsOneOf(STRING_LITERALS_ARRAY));
            }
            else {
                advance();
            }
            m.done(DlangTypes.LITERAL_EXPRESSION);
            return m;
        }
        if (i == DlangTypes.OP_BRACKET_LEFT) {
            // ArrayLiteral | AssocArrayLiteral
            PsiBuilder.Marker m = myBuilder.mark();
            if (isAssociativeArrayLiteral()) {
                parseAssocArrayLiteral();
            }
            else {
                parseArrayLiteral();
            }
            m.done(DlangTypes.ARRAY_LITERAL_EXPRESSION);
            return m;
        }
        // Function Literal
        PsiBuilder.Marker bookmark = myBuilder.mark();
        PsiBuilder.Marker fnctlMarker = parseFunctionLiteralExpression();
        if (fnctlMarker != null) {
            bookmark.drop();
            return fnctlMarker;
        }
        bookmark.rollbackTo();

        // AssertExpression
        if (i == DlangTypes.KW_ASSERT) {
            return parseAssertExpression();
        }

        // MixinExpression
        if (i == DlangTypes.KW_MIXIN) {
            return parseMixinExpression();
        }

        // ImportExpression
        if (i == DlangTypes.KW_IMPORT) {
            return parseImportExpression();
        }

        // NewExpression
        if (i == DlangTypes.KW_NEW) {
            return parseNewExpression();
        }

        // FundamentalType . Identifier | FundamentalType ( NamedArgumentList )
        if (isBuiltinType(i)) {
            PsiBuilder.Marker m = myBuilder.mark();
            advance();
            if (currentIs(DlangTypes.OP_DOT)) {
                advance();
                expect(DlangTypes.ID);
            }
            else if (currentIs(DlangTypes.OP_PAR_LEFT)) {
                myBuilder.advanceLexer();
                if (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
                    parseNamedArgumentList();
                }
                expect(DlangTypes.OP_PAR_RIGHT);
                m.done(DlangTypes.FUNDAMENTAL_TYPE_CONSTRUCT_EXPRESSION);
                return m;
            }
            else {
                myBuilder.error(LocalizeValue.localizeTODO("`.` or `(` expected"));
                m.drop();
                return null;
            }
            exit_section_modified(myBuilder, m, DlangTypes.FUNDAMENTAL_TYPE_PROPERTY_EXPRESSION, true);
            return m;
        }

        // TypeCtor? ...
        if (i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_CONST || i == DlangTypes.KW_INOUT || i == DlangTypes.KW_SHARED) {
            PsiBuilder.Marker m = myBuilder.mark();
            bookmark = myBuilder.mark();
            advance();
            if (expect(DlangTypes.OP_PAR_LEFT) == null) {
                bookmark.rollbackTo();
                m.drop();
                return null;
            }
            if (!parseType()) {
                bookmark.rollbackTo();
                m.drop();
                return null;
            }
            bookmark.drop();
            expect(DlangTypes.OP_PAR_RIGHT);
            if (currentIs(DlangTypes.OP_DOT)) {
                // ( type ) . id
                advance();
                if (currentIs(DlangTypes.ID)) {
                    advance();
                    exit_section_modified(myBuilder, m, DlangTypes.TYPE_PROPERTY_EXPRESSION, true);
                    return m;
                }
            }
            else if (myBuilder.getTokenType() != DlangTypes.OP_PAR_LEFT) {
                myBuilder.error(LocalizeValue.localizeTODO("`.` or `(` expected"));
                m.done(DlangTypes.TYPE_CONSTRUCT_EXPRESSION);
                return null;
            }
            // (type) (args)
            myBuilder.advanceLexer();
            parseArgumentList();
            expect(DlangTypes.OP_PAR_RIGHT);
            m.done(DlangTypes.TYPE_CONSTRUCT_EXPRESSION);
            return m;
        }

        if (i == DlangTypes.OP_PAR_LEFT) {
            PsiBuilder.Marker m = myBuilder.mark();
            advance();
            bookmark = myBuilder.mark();
            if (parseType()) {
                if (expect(DlangTypes.OP_PAR_RIGHT) != null) {
                    if (myBuilder.getTokenType() == DlangTypes.OP_DOT) {
                        // ( type ) . id
                        myBuilder.advanceLexer();
                        expect(DlangTypes.ID);
                        if (myBuilder.getTokenType() != DlangTypes.OP_NOT && myBuilder.getTokenType() != DlangTypes.OP_PAR_LEFT && myBuilder.getTokenType() != DlangTypes.OP_DDOT) {
                            bookmark.drop();
                            exit_section_modified(myBuilder, m, DlangTypes.TYPE_PROPERTY_EXPRESSION, true);
                            return m;
                        }
                        // then it’s a parenthesised expression followed by reference expression
                    }
                    else if (myBuilder.getTokenType() == DlangTypes.OP_PAR_LEFT) {
                        // (type) (args)
                        bookmark.drop();
                        myBuilder.advanceLexer();
                        parseArgumentList();
                        expect(DlangTypes.OP_PAR_RIGHT);
                        m.done(DlangTypes.TYPE_CONSTRUCT_EXPRESSION);
                        return m;
                    }
                }
            }
            bookmark.rollbackTo();

            // ( expression )
            if (!parseExpression()) {
                cleanup(m, DlangTypes.PARENTHESISED_EXPRESSION);
                return null;
            }
            expect(DlangTypes.OP_PAR_RIGHT);
            exit_section_modified(myBuilder, m, DlangTypes.PARENTHESISED_EXPRESSION, true);
            return m;
        }

        if (i == DlangTypes.KW_TYPEOF) {
            return parseTypeofExpression();
        }

        if (i == DlangTypes.KW_TYPEID) {
            return parseTypeidExpression();
        }

        if (i == DlangTypes.KW_IS) {
            return parseIsExpression();
        }

        if (i == DlangTypes.KW___TRAITS) {
            return parseTraitsExpression();
        }

        return null;
    }

    private void parseString() {
        if (myBuilder.getTokenType() != DlangTypes.IES_START) {
            // standard string
            myBuilder.advanceLexer();
            return;
        }

        assert myBuilder.getTokenType() == DlangTypes.IES_START;
        PsiBuilder.Marker m = myBuilder.mark();
        myBuilder.advanceLexer();
        while (!myBuilder.eof()) {
            if (myBuilder.getTokenType() == DlangTypes.IES_TEXT || myBuilder.getTokenType() == DlangTypes.IES_TEXT_RAW) {
                myBuilder.advanceLexer();
            }
            else if (myBuilder.getTokenType() == DlangTypes.IES_FRAGMENT_START) {
                myBuilder.advanceLexer();
                parseAssignExpression();
                expect(DlangTypes.IES_FRAGMENT_END);
            }
            else if (myBuilder.getTokenType() == DlangTypes.IES_END) {
                myBuilder.advanceLexer();
                break;
            }
            else {
                myBuilder.mark().error(LocalizeValue.localizeTODO("Unexpected ies-string token"));
                break;
            }
        }
        m.done(DlangTypes.IES_STRING);
    }

    private boolean isLiteral(@Nullable IElementType i) {
        return LITERALS.contains(i);
    }

    private boolean isBuiltinType(@Nullable IElementType i) {
        return BUILTIN_TYPES.contains(i);
    }

    private PsiBuilder.@Nullable Marker primaryExpressionIdentifierCase() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (myBuilder.getTokenType() == DlangTypes.OP_DOT) {
            myBuilder.advanceLexer();
        }
        boolean result = parseIdentifierOrTemplateInstance();
        m.done(DlangTypes.REFERENCE_EXPRESSION);
        return result ? m : null;
    }

    /**
     * Parses a Register
     *
     *
     * $(GRAMMAR $(RULEDEF register):
     * $(LITERAL Identifier)
     * | $(LITERAL Identifier) $(LITERAL '$(LPAREN)') $(LITERAL IntegerLiteral) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseRegister() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            cleanup(m, DlangTypes.REGISTER);
            return false;
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            advance();
            IElementType intLit = expect(DlangTypes.INTEGER_LITERAL);
            if (intLit == null) {
                cleanup(m, DlangTypes.REGISTER);
                return false;
            }
            expect(DlangTypes.OP_PAR_RIGHT);
        }
        exit_section_modified(myBuilder, m, DlangTypes.REGISTER, true);
        return true;
    }

    /**
     * Parses a RelExpression
     *
     *
     * $(GRAMMAR $(RULEDEF relExpression):
     * $(RULE shiftExpression)
     * | $(RULE relExpression) $(RULE relOperator) $(RULE shiftExpression)
     * ;
     * $(RULEDEF relOperator):
     * $(LITERAL '<')
     * | $(LITERAL '<=')
     * | $(LITERAL '>')
     * | $(LITERAL '>=')
     * | $(LITERAL '!<>=')
     * | $(LITERAL '!<>')
     * | $(LITERAL '<>')
     * | $(LITERAL '<>=')
     * | $(LITERAL '!>')
     * | $(LITERAL '!>=')
     * | $(LITERAL '!<')
     * | $(LITERAL '!<=')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseRelExpression(PsiBuilder.@Nullable Marker marker) {
        PsiBuilder.Marker m = marker;
        if (m == null) {
            m = myBuilder.mark();
            if (parseShiftExpression() == null) {
                m.done(DlangTypes.IDENTITY_EXPRESSION);
                return null;
            }
        }
        if (!currentIsOneOf(DlangTypes.OP_LESS, DlangTypes.OP_LESS_EQ, DlangTypes.OP_GT, DlangTypes.OP_GT_EQ)) {
            m.done(DlangTypes.IDENTITY_EXPRESSION);
            return null;
        }
        advance();
        if (parseShiftExpression() == null) {
            m.done(DlangTypes.IDENTITY_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.REL_EXPRESSION);
        return m;
    }

    /**
     * Parses a ReturnStatement
     *
     *
     * $(GRAMMAR $(RULEDEF returnStatement):
     * $(LITERAL 'return') $(RULE expression)? $(LITERAL ';')
     * ;)
     */
    public boolean parseReturnStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType start = expect(DlangTypes.KW_RETURN);
        if (start == null) {
            cleanup(m, DlangTypes.RETURN_STATEMENT);
            return false;
        }
        if (!currentIs(DlangTypes.OP_SCOLON)) {
            if (!parseExpression()) {
                cleanup(m, DlangTypes.RETURN_STATEMENT);
                return false;
            }
        }
        IElementType semicolon = expect(DlangTypes.OP_SCOLON);
        if (semicolon == null) {
            cleanup(m, DlangTypes.RETURN_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.RETURN_STATEMENT, true);
        return true;
    }

    /**
     * Parses a ScopeGuardStatement
     *
     *
     * $(GRAMMAR $(RULEDEF scopeGuardStatement):
     * $(LITERAL 'scope') $(LITERAL '$(LPAREN)') $(LITERAL Identifier) $(LITERAL '$(RPAREN)') $(RULE statementNoCaseNoDefault)
     * ;)
     */
    public boolean parseScopeGuardStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_SCOPE);
        expect(DlangTypes.OP_PAR_LEFT);
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            cleanup(m, DlangTypes.SCOPE_GUARD_STATEMENT);
            return false;
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        if (!parseNonEmptyStatementNoCaseNoDefault()) {
            cleanup(m, DlangTypes.SCOPE_GUARD_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.SCOPE_GUARD_STATEMENT, true);
        return true;
    }

    /**
     * Parses a SharedStaticConstructor
     *
     *
     * $(GRAMMAR $(RULEDEF sharedStaticConstructor):
     * $(LITERAL 'shared') $(LITERAL 'static') $(LITERAL 'this') $(LITERAL '$(LPAREN)') $(LITERAL '$(RPAREN)') $(RULE MemberFunctionAttribute)* ($(RULE functionBody) | $(LITERAL ";"))
     * ;)
     */
    public boolean parseSharedStaticConstructor(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_SHARED) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() != DlangTypes.KW_STATIC) {
            bookmark.rollbackTo();
            return false;
        }
        myBuilder.advanceLexer();
        boolean b = parseStaticCtorDtorCommon();
        if (!b) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        m.done(DlangTypes.SHARED_STATIC_CONSTRUCTOR);
        return true;
    }

    /**
     * Parses a SharedStaticDestructor
     *
     *
     * $(GRAMMAR $(RULEDEF sharedStaticDestructor):
     * $(LITERAL 'shared') $(LITERAL 'static') $(LITERAL '~') $(LITERAL 'this') $(LITERAL '$(LPAREN)') $(LITERAL '$(RPAREN)') $(RULE MemberFunctionAttribute)* ($(RULE functionBody) | $(LITERAL ";"))
     * ;)
     */
    public boolean parseSharedStaticDestructor(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_SHARED) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() != DlangTypes.KW_STATIC) {
            bookmark.rollbackTo();
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_TILDA)) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        parseStaticCtorDtorCommon();
        m.done(DlangTypes.SHARED_STATIC_DESTRUCTOR);
        return true;
    }

    /**
     * Parses a ShiftExpression
     *
     *
     * $(GRAMMAR $(RULEDEF shiftExpression):
     * $(RULE addExpression)
     * | $(RULE shiftExpression) ($(LITERAL '<<') | $(LITERAL '>>') | $(LITERAL '>>>')) $(RULE addExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseShiftExpression() {
        PsiBuilder.Marker m = parseAddExpression();
        if (m == null) {
            return null;
        }
        while (currentIsOneOf(DlangTypes.OP_SH_LEFT, DlangTypes.OP_SH_RIGHT, DlangTypes.OP_USH_RIGHT)) {
            m = m.precede();
            myBuilder.advanceLexer();
            if (parseAddExpression() == null) {
                m.done(DlangTypes.SHIFT_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.SHIFT_EXPRESSION);
        }
        return m;
    }

    /**
     * Parses a SingleImport
     *
     *
     * $(GRAMMAR $(RULEDEF import):
     * ($(LITERAL Identifier) $(LITERAL '='))? $(RULE identifierChain)
     * ;)
     */
    public boolean parseSingleImport() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (startsWith(DlangTypes.ID, DlangTypes.OP_EQ)) {
            advance(); // identifier
            advance(); // =
        }
        if (parseIdentifierChain() == null) {
            cleanup(m, DlangTypes.SINGLE_IMPORT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.SINGLE_IMPORT, true);
        return true;
    }

    /**
     * Parses a ShortenedFunctionBody
     *
     *
     * $(GRAMMAR $(RULEDEF shortenedFunctionBody):
     * $(RULE inOutContractExpression)* $(LITERAL '=>') $(RULE expression) $(LITERAL ';')
     * ;)
     */
    public boolean parseShortenedFunctionBody() {
        PsiBuilder.Marker m = myBuilder.mark();
        while (currentIsOneOf(DlangTypes.KW_IN, DlangTypes.KW_OUT)) {
            parseFunctionContract(false);
        }
        if (!tokenCheck(DlangTypes.OP_LAMBDA_ARROW)) {
            cleanup(m, DlangTypes.SHORTENED_FUNCTION_BODY);
            return false;
        }
        if (!parseExpression()) {
            cleanup(m, DlangTypes.SHORTENED_FUNCTION_BODY);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_SCOLON)) {
            cleanup(m, DlangTypes.SHORTENED_FUNCTION_BODY);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.SHORTENED_FUNCTION_BODY, true);
        return true;
    }

    /**
     * Parses a SpecifiedFunctionBody
     *
     *
     * $(GRAMMAR $(RULEDEF specifiedFunctionBody):
     * $(LITERAL 'do')? $(RULE blockStatement)
     * | $(RULE functionContract)* $(RULE inOutContractExpression) $(LITERAL 'do')? $(RULE blockStatement)
     * | $(RULE functionContract)* $(RULE inOutStatement) $(LITERAL 'do') $(RULE blockStatement)
     * ;)
     */
    public boolean parseSpecifiedFunctionBody() {
        PsiBuilder.Marker m = myBuilder.mark();
        boolean requireDo = false;
        while (currentIsOneOf(DlangTypes.KW_IN, DlangTypes.KW_OUT)) {
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (parseFunctionContract(false)) {
                requireDo = false;
            }
            else {
                bookmark.rollbackTo();
                if (parseFunctionContract()) {
                    requireDo = true;
                }
            }
            bookmark.drop();
        }
        if (currentIs(DlangTypes.KW_DO) || currentIs(DlangTypes.ID)) {
            advance();
            requireDo = false;
        }
        if (requireDo) {
            error("`do` expected after InStatement or OutStatement");
            cleanup(m, DlangTypes.SPECIFIED_FUNCTION_BODY);
            return false;
        }
        if (!parseBlockStatement()) {
            cleanup(m, DlangTypes.SPECIFIED_FUNCTION_BODY);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.SPECIFIED_FUNCTION_BODY, true);
        return true;
    }

    /**
     * Parse a Statement
     * $(GRAMMAR Statement):
     * $(RULE emptyStatement)
     * | $(RULE scopeBlockStatement)
     * | $(RULE nonEmptyStatement)
     */
    public boolean parseStatement() {
        IElementType i = myBuilder.getTokenType();
        if (i == DlangTypes.OP_SCOLON) {
            return parseEmptyStatement();
        }
        else if (i == DlangTypes.OP_BRACES_LEFT) {
            return parseScopeBlockStatement();
        }
        else {
            return parseNonEmptyStatement();
        }
    }

    /**
     * Parses a Non Empty Statement
     *
     *
     * $(GRAMMAR $(RULEDEF nonEmptyStatement):
     * $(RULE statementNoCaseNoDefault)
     * | $(RULE caseStatement)
     * | $(RULE caseRangeStatement)
     * | $(RULE defaultStatement)
     * ;)
     */
    public boolean parseNonEmptyStatement() {
        if (!moreTokens()) {
            error("Expected statement instead of EOF");
            return false;
        }
        IElementType i = current();
        if (i == DlangTypes.KW_CASE) {
            PsiBuilder.Marker m_case = myBuilder.mark();
            advance();
            boolean argumentList = parseArgumentList();
            if (!argumentList) {
                m_case.drop();
                return false;
            }
            if (startsWith(
                DlangTypes.OP_COLON,
                DlangTypes.OP_DDOT
            )) {
                return parseCaseRangeStatement(m_case);
            }
            else {
                return parseCaseStatement(m_case);
            }
        }
        else if (i == DlangTypes.KW_DEFAULT) {
            return parseDefaultStatement();
        }
        else {
            return parseNonEmptyStatementNoCaseNoDefault();
        }
    }

    /**
     * Parses a StatementNoCaseNoDefault
     *
     *
     * $(GRAMMAR $(RULEDEF statementNoCaseNoDefault):
     * $(RULE labeledStatement)
     * | $(RULE ifStatement)
     * | $(RULE whileStatement)
     * | $(RULE doStatement)
     * | $(RULE forStatement)
     * | $(RULE foreachStatement)
     * | $(RULE switchStatement)
     * | $(RULE finalSwitchStatement)
     * | $(RULE continueStatement)
     * | $(RULE breakStatement)
     * | $(RULE returnStatement)
     * | $(RULE gotoStatement)
     * | $(RULE withStatement)
     * | $(RULE synchronizedStatement)
     * | $(RULE tryStatement)
     * | $(RULE scopeGuardStatement)
     * | $(RULE pragmaStatement)
     * | $(RULE asmStatement)
     * | $(RULE conditionalStatement)
     * | $(RULE staticAssertStatement)
     * | $(RULE versionSpecification)
     * | $(RULE debugSpecification)
     * | $(RULE expressionStatement)
     * | $(Rule importDeclation)
     * ;)
     */
    public boolean parseNonEmptyStatementNoCaseNoDefault() {
        if (!moreTokens()) {
            error("Expected statement instead of EOF");
            return false;
        }
        IElementType i = current();
        if (i == DlangTypes.OP_BRACES_LEFT) {
            return parseBlockStatement();
        }
        else if (i == DlangTypes.KW_IF) {
            return parseIfStatement();
        }
        else if (i == DlangTypes.KW_WHILE) {
            return parseWhileStatement();
        }
        else if (i == DlangTypes.KW_DO) {
            return parseDoStatement();
        }
        else if (i == DlangTypes.KW_FOR) {
            return parseForStatement();
        }
        else if (i == DlangTypes.KW_FOREACH || i == DlangTypes.KW_FOREACH_REVERSE) {
            return parseForeachStatement();
        }
        else if (i == DlangTypes.KW_SWITCH) {
            return parseSwitchStatement();
        }
        else if (i == DlangTypes.KW_CONTINUE) {
            return parseContinueStatement();
        }
        else if (i == DlangTypes.KW_BREAK) {
            return parseBreakStatement();
        }
        else if (i == DlangTypes.KW_RETURN) {
            return parseReturnStatement();
        }
        else if (i == DlangTypes.KW_GOTO) {
            return parseGotoStatement();
        }
        else if (i == DlangTypes.KW_WITH) {
            return parseWithStatement();
        }
        else if (i == DlangTypes.KW_SYNCHRONIZED) {
            return parseSynchronizedStatement();
        }
        else if (i == DlangTypes.KW_TRY) {
            return parseTryStatement();
        }
        else if (i == DlangTypes.KW_SCOPE) {
            if (peekIs(DlangTypes.OP_PAR_LEFT)) {
                return parseScopeGuardStatement();
            }
            else {
                return parseDeclarationStatement();
            }
        }
        else if (i == DlangTypes.KW_ASM) {
            return parseAsmStatement();
        }
        else if (i == DlangTypes.KW_PRAGMA) {
            return parsePragmaStatement();
        }
        else if (i == DlangTypes.KW_FINAL && peekIs(DlangTypes.KW_SWITCH)) {
            return parseFinalSwitchStatement();
        }
        else if (i == DlangTypes.KW_DEBUG) {
            if (peekIs(DlangTypes.OP_EQ)) {
                return parseDeclarationStatement();
            }
            return parseConditionalStatement();
        }
        else if (i == DlangTypes.KW_VERSION) {
            if (peekIs(DlangTypes.OP_EQ)) {
                // TODO for better error reporting `version = X;` in statement is an error
                //  the parsing should print a pretty error, specifying that they must be done at module level
                return false;
            }
            return parseConditionalStatement();
        }
        else if (i == DlangTypes.KW_STATIC) {
            IElementType next = myBuilder.lookAhead(1);
            if (next == DlangTypes.KW_IF) {
                return parseConditionalStatement();
            }
            else if (next == DlangTypes.KW_ASSERT) {
                return parseStaticAssertStatement();
            }
            else if (next == DlangTypes.KW_FOREACH || next == DlangTypes.KW_FOREACH_REVERSE) {
                return parseStaticForeachStatement();
            }
            else if (next == DlangTypes.KW_IMPORT) {
                return parseImportDeclaration(myBuilder.mark());
            }
            else {
                PsiBuilder.Marker m = myBuilder.mark();
                if (parseDeclarationStatement()) {
                    m.drop();
                    return true;
                }
                myBuilder.advanceLexer();
                error("'if', 'assert', 'foreach', 'foreach_reverse' or 'import' expected.");
                m.rollbackTo();
                return false;
            }
        }
        else if (i == DlangTypes.ID && peekIs(DlangTypes.OP_COLON)) {
            return parseLabeledStatement();
        }
        else if (i == DlangTypes.KW_IMPORT && !peekIs(DlangTypes.OP_PAR_LEFT)) {
            return parseImportDeclaration(myBuilder.mark());
        }
        else {
            PsiBuilder.Marker marker = myBuilder.mark();
            if (currentIs(DlangTypes.ID) && myBuilder.lookAhead(1) == DlangTypes.OP_EQ) {
                // there can’t be alias reassignment at this level
                marker.drop();
                return parseExpressionStatement();
            }
            if (parseDeclarationStatement()) {
                marker.drop();
                return true;
            }
            else {
                marker.rollbackTo();
                return parseExpressionStatement();
            }
        }
    }

    public boolean parseEmptyStatement() {
        PsiBuilder.Marker marker = myBuilder.mark();
        if (expect(DlangTypes.OP_SCOLON) == null) {
            marker.drop();
            return false;
        }
        marker.done(DlangTypes.EMPTY_STATEMENT);
        return true;
    }

    public boolean parseScopeBlockStatement() {
        return parseBlockStatement();
    }

    public boolean parseScopeStatementList() {
        while (!myBuilder.eof()) {
            if (!parseStatementNoCaseNoDefault()) {
                break;
            }
        }
        return true;
    }

    public boolean parseStatementNoCaseNoDefault() {
        IElementType i = myBuilder.getTokenType();
        if (i == DlangTypes.OP_SCOLON) {
            return parseEmptyStatement();
        }
        else if (i == DlangTypes.OP_BRACES_LEFT) {
            return parseScopeBlockStatement();
        }
        else {
            return parseNonEmptyStatementNoCaseNoDefault();
        }
    }

    public boolean parseNoScopeNonEmptyStatement() {
        if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_LEFT) {
            return parseBlockStatement();
        }
        else {
            return parseNonEmptyStatement();
        }
    }

    public boolean parseScopeStatement() {
        if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_LEFT) {
            return parseScopeBlockStatement();
        }
        else {
            return parseNonEmptyStatement();
        }
    }

    /**
     * Parses a StaticAssertDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF staticAssertDeclaration):
     * $(RULE staticAssertStatement)
     * ;)
     */
    public boolean parseStaticAssertDeclaration(PsiBuilder.Marker marker) {
        if (myBuilder.getTokenType() != DlangTypes.KW_STATIC) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() != DlangTypes.KW_ASSERT) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        if (parseAssertExpression() == null) {
            marker.done(DlangTypes.STATIC_ASSERT_DECLARATION);
            return true;
        }
        expect(DlangTypes.OP_SCOLON);
        marker.done(DlangTypes.STATIC_ASSERT_DECLARATION);
        return true;
    }

    /**
     * Parses a StaticAssertStatement
     *
     *
     * $(GRAMMAR $(RULEDEF staticAssertStatement):
     * $(LITERAL 'static') $(RULE assertExpression) $(LITERAL ';')
     * ;)
     */
    public boolean parseStaticAssertStatement() {
        PsiBuilder.Marker marker = myBuilder.mark();
        if (expect(DlangTypes.KW_STATIC) == null) {
            cleanup(marker, DlangTypes.STATIC_ASSERT_STATEMENT);
            return false;
        }
        if (parseAssertExpression() == null) {
            cleanup(marker, DlangTypes.STATIC_ASSERT_STATEMENT);
            return false;
        }
        if (expect(DlangTypes.OP_SCOLON) == null) {
            cleanup(marker, DlangTypes.STATIC_ASSERT_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, marker, DlangTypes.STATIC_ASSERT_STATEMENT, true);
        return true;
    }

    /**
     * Parses a StaticConstructor
     *
     *
     * $(GRAMMAR $(RULEDEF staticConstructor):
     * $(LITERAL 'static') $(LITERAL 'this') $(LITERAL '$(LPAREN)') $(LITERAL '$(RPAREN)') $(RULE memberFunctionAttribute)* ($(RULE functionBody) | $(LITERAL ";"))
     * ;)
     */
    public boolean parseStaticConstructor(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_STATIC) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (!parseStaticCtorDtorCommon()) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        m.done(DlangTypes.STATIC_CONSTRUCTOR);
        return true;
    }

    /**
     * Parses a StaticDestructor
     *
     *
     * $(GRAMMAR $(RULEDEF staticDestructor):
     * $(LITERAL 'static') $(LITERAL '~') $(LITERAL 'this') $(LITERAL '$(LPAREN)') $(LITERAL '$(RPAREN)') $(RULE memberFunctionAttribute)* ($(RULE functionBody) | $(LITERAL ";"))
     * ;)
     */
    public boolean parseStaticDestructor(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_STATIC) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (!tokenCheck(DlangTypes.OP_TILDA)) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        parseStaticCtorDtorCommon();
        m.done(DlangTypes.STATIC_DESTRUCTOR);
        return true;
    }

    /**
     * Parses an StaticIfCondition
     *
     *
     * $(GRAMMAR $(RULEDEF staticIfCondition):
     * $(LITERAL 'static') $(LITERAL 'if') $(LITERAL '$(LPAREN)') $(RULE assignExpression) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseStaticIfCondition() {
        PsiBuilder.Marker marker = myBuilder.mark();
        if (expect(DlangTypes.KW_STATIC) == null) {
            cleanup(marker, DlangTypes.STATIC_IF_CONDITION);
            return false;
        }
        if (expect(DlangTypes.KW_IF) == null) {
            cleanup(marker, DlangTypes.STATIC_IF_CONDITION);
            return false;
        }
        if (expect(DlangTypes.OP_PAR_LEFT) == null) {
            cleanup(marker, DlangTypes.STATIC_IF_CONDITION);
            return false;
        }
        if (parseAssignExpression() == null) {
            cleanup(marker, DlangTypes.STATIC_IF_CONDITION);
            return false;
        }
        if (expect(DlangTypes.OP_PAR_RIGHT) == null) {
            cleanup(marker, DlangTypes.STATIC_IF_CONDITION);
            return false;
        }
        exit_section_modified(myBuilder, marker, DlangTypes.STATIC_IF_CONDITION, true);
        return true;
    }

    /**
     * Parses a StorageClass
     *
     *
     * $(GRAMMAR $(RULEDEF storageClass):
     * $(RULE linkageAttribute)
     * | $(RULE alignAttribute)
     * | $(RULE atAttribute)
     * | $(RULE typeConstructor)
     * | $(LITERAL 'deprecated')
     * | $(LITERAL 'enum')
     * | $(LITERAL 'static')
     * | $(LITERAL 'extern')
     * | $(LITERAL 'abstract')
     * | $(LITERAL 'final')
     * | $(LITERAL 'override')
     * | $(LITERAL 'synchronized')
     * | $(LITERAL 'auto')
     * | $(LITERAL 'scope')
     * | $(LITERAL 'const')
     * | $(LITERAL 'immutable')
     * | $(LITERAL 'inout')
     * | $(LITERAL 'shared')
     * | $(LITERAL '__gshared')
     * | $(LITERAL 'nothrow')
     * | $(LITERAL 'pure')
     * | $(LITERAL 'ref')
     * ;)
     */
    public boolean parseStorageClass() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.OP_AT) {
            if (!parseAtAttribute()) {
                cleanup(m, DlangTypes.STORAGE_CLASS);
                return false;
            }
        }
        else if (i == DlangTypes.KW_DEPRECATED) {
            myBuilder.advanceLexer();
            if (myBuilder.getTokenType() == DlangTypes.OP_PAR_LEFT) {
                m.rollbackTo();
                return false;
            }
        }
        else if (i == DlangTypes.KW_ALIGN) {
            if (!parseAlignAttribute()) {
                cleanup(m, DlangTypes.STORAGE_CLASS);
                return false;
            }
        }
        else if (i == DlangTypes.KW_EXTERN) {
            if (peekIs(DlangTypes.OP_PAR_LEFT)) {
                if (!parseLinkageAttribute()) {
                    cleanup(m, DlangTypes.STORAGE_CLASS);
                    return false;
                }
                exit_section_modified(myBuilder, m, DlangTypes.STORAGE_CLASS, true);
                return true;
            }
            else {
                advance();
            }
        }
        else if (i == DlangTypes.KW_CONST || i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_INOUT || i == DlangTypes.KW_SHARED || i == DlangTypes.KW_ABSTRACT || i == DlangTypes.KW_AUTO || i == DlangTypes.KW_ENUM || i == DlangTypes.KW_FINAL || i == DlangTypes.KW_NOTHROW || i == DlangTypes.KW_OVERRIDE || i == DlangTypes.KW_PURE || i == DlangTypes.KW_REF || i == DlangTypes.KW___GSHARED || i == DlangTypes.KW_SCOPE || i == DlangTypes.KW_STATIC || i == DlangTypes.KW_SYNCHRONIZED) {
            advance();
            if (myBuilder.getTokenType() == DlangTypes.OP_PAR_LEFT) {
                m.rollbackTo();
                return false;
            }
        }
        else {
            m.rollbackTo();
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.STORAGE_CLASS, true);
        return true;
    }

    /**
     * Parses a StructBody
     *
     *
     * $(GRAMMAR $(RULEDEF structBody):
     * $(LITERAL '{') $(RULE declaration)* $(LITERAL '}')
     * ;)
     */
    public boolean parseStructBody() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.OP_BRACES_LEFT);
        parseDeclDefsWithRecoveryUpToParentScope();
        expect(DlangTypes.OP_BRACES_RIGHT);
        m.done(DlangTypes.STRUCT_BODY);
        return true;
    }

    /**
     * Parses a StructDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF structDeclaration):
     * $(LITERAL 'struct') $(LITERAL Identifier)? ($(RULE templateParameters) $(RULE raint)? $(RULE structBody) | ($(RULE structBody) | $(LITERAL ';')))
     * ;)
     */
    public boolean parseStructDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_STRUCT) {
            return false;
        }
        myBuilder.advanceLexer();
        if (currentIs(DlangTypes.ID)) {
            advance();
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            if (!parseTemplateParameters()) {
                m.done(DlangTypes.STRUCT_DECLARATION);
                return true;
            }
            if (currentIs(DlangTypes.KW_IF)) {
                if (!parseConstraint()) {
                    m.done(DlangTypes.STRUCT_DECLARATION);
                    return true;
                }
            }
        }
        if (currentIs(DlangTypes.OP_BRACES_LEFT)) {
            if (!parseStructBody()) {
                m.done(DlangTypes.STRUCT_DECLARATION);
                return true;
            }
        }
        else if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
        }
        else {
            error("Template Parameters, Struct Body, or Semicolon expected");
        }
        m.done(DlangTypes.STRUCT_DECLARATION);
        return true;
    }

    /**
     * Parses an StructInitializer
     *
     * $(GRAMMAR $(RULEDEF structInitializer):
     * $(LITERAL '{') $(RULE structMemberInitializer) ($(LITERAL ',') $(RULE structMemberInitializer)?)* $(LITERAL '}')
     * ;)
     */
    public boolean parseStructInitializer() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.OP_BRACES_LEFT);
        boolean r = true;
        boolean first = true;
        while (!myBuilder.eof() && myBuilder.getTokenType() != DlangTypes.OP_BRACES_RIGHT) {
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (!parseStructMemberInitializer()) {
                bookmark.rollbackTo();
                initializerRecover("Invalid Struct initializer", DlangTypes.OP_BRACES_LEFT, DlangTypes.OP_BRACES_RIGHT);
                r = false;
                break;
            }
            bookmark.drop();
            if (currentIs(DlangTypes.OP_COMMA)) {
                first = false;
                advance();
            }
            else if (currentIs(DlangTypes.OP_COLON)) {
                if (first) {
                    // the first one is associative array syntax, report error for the whole block
                    m.rollbackTo();
                    PsiBuilder.Marker errorMarker = myBuilder.mark();
                    expect(DlangTypes.OP_BRACES_LEFT);
                    int count = 1;
                    while (!myBuilder.eof()) {
                        if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_LEFT) {
                            count++;
                        }
                        else if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_RIGHT) {
                            if (--count == 0) {
                                myBuilder.advanceLexer();
                                break;
                            }
                        }
                        myBuilder.advanceLexer();
                    }
                    errorMarker.error(LocalizeValue.localizeTODO("Incorrect syntax for associative array, expected `[]`, found `{}`"));
                    return true;
                }
                else {
                    // this case appeared at the middle of the struct initializer, beginning is fine
                    // so mark only the extra as error
                    PsiBuilder.Marker errorMarker = myBuilder.mark();
                    int count = 0;
                    while (!myBuilder.eof()) {
                        if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_LEFT) {
                            count++;
                        }
                        else if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_RIGHT) {
                            if (--count < 0) {
                                myBuilder.advanceLexer();
                                break;
                            }
                        }
                        else if (myBuilder.getTokenType() == DlangTypes.OP_COMMA) {
                            break;
                        }
                        myBuilder.advanceLexer();
                    }
                    errorMarker.error(LocalizeValue.localizeTODO("Invalid syntax, `:` is not allowed here, `,` or `}` is expected"));
                    continue;
                }
            }
            else {
                break;
            }
        }
        if (myBuilder.getTokenType() != DlangTypes.OP_BRACES_RIGHT) {
            r = false;
        }
        else {
            myBuilder.advanceLexer();
        }
        m.done(DlangTypes.STRUCT_INITIALIZER);
        return r;
    }

    /**
     * Parses a StructMemberInitializer
     *
     *
     * $(GRAMMAR $(RULEDEF structMemberInitializer):
     * ($(LITERAL Identifier) $(LITERAL ':'))? $(RULE nonVoidInitializer)
     * ;)
     */
    public boolean parseStructMemberInitializer() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (startsWith(DlangTypes.ID, DlangTypes.OP_COLON)) {
            advance();
            advance();
        }
        boolean r = parseNonVoidInitializer();
        m.done(DlangTypes.STRUCT_MEMBER_INITIALIZER);
        return r;
    }

    /**
     * Parses a SwitchStatement
     *
     *
     * $(GRAMMAR $(RULEDEF switchStatement):
     * $(LITERAL 'switch') $(LITERAL '$(LPAREN)') $(RULE expression) $(LITERAL '$(RPAREN)') $(RULE statement)
     * ;)
     */
    public boolean parseSwitchStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_SWITCH);
        expect(DlangTypes.OP_PAR_LEFT);
        if (!parseExpression()) {
            cleanup(m, DlangTypes.SWITCH_STATEMENT);
            return false;
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        if (!parseNonEmptyStatement()) {
            cleanup(m, DlangTypes.SWITCH_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.SWITCH_STATEMENT, true);
        return true;
    }

    /**
     * Parses a SynchronizedStatement
     *
     *
     * $(GRAMMAR $(RULEDEF synchronizedStatement):
     * $(LITERAL 'synchronized') ($(LITERAL '$(LPAREN)') $(RULE expression) $(LITERAL '$(RPAREN)'))? $(RULE scopeStatement)
     * ;)
     */
    public boolean parseSynchronizedStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_SYNCHRONIZED);
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            expect(DlangTypes.OP_PAR_LEFT);
            if (!parseExpression()) {
                cleanup(m, DlangTypes.SYNCHRONIZED_STATEMENT);
                return false;
            }
            expect(DlangTypes.OP_PAR_RIGHT);
        }
        if (!parseScopeStatement()) {
            cleanup(m, DlangTypes.SYNCHRONIZED_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.SYNCHRONIZED_STATEMENT, true);
        return true;
    }

    /**
     * Parses a TemplateAliasParameter
     *
     *
     * $(GRAMMAR $(RULEDEF templateAliasParameter):
     * $(LITERAL 'alias') $(RULE type)? $(LITERAL Identifier) ($(LITERAL ':') ($(RULE type) | $(RULE assignExpression)))? ($(LITERAL '=') ($(RULE type) | $(RULE assignExpression)))?
     * ;)
     */
    public boolean parseTemplateAliasParameter() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_ALIAS);
        if (currentIs(DlangTypes.ID) && !peekIs(DlangTypes.OP_DOT)) {
            if (peekIsOneOf(
                DlangTypes.OP_COMMA,
                DlangTypes.OP_PAR_RIGHT,
                DlangTypes.OP_EQ,
                DlangTypes.OP_COLON
            )) {
                advance();
            }
            else {
                if (!parseType()) {
                    cleanup(m, DlangTypes.TEMPLATE_ALIAS_PARAMETER);
                    return false;
                }
                IElementType ident = expect(DlangTypes.ID);
                if (ident == null) {
                    cleanup(m, DlangTypes.TEMPLATE_ALIAS_PARAMETER);
                    return false;
                }
            }
        }
        else {
            if (!parseType()) {
                cleanup(m, DlangTypes.TEMPLATE_ALIAS_PARAMETER);
                return false;
            }
            IElementType ident = expect(DlangTypes.ID);
            if (ident == null) {
                cleanup(m, DlangTypes.TEMPLATE_ALIAS_PARAMETER);
                return false;
            }
        }

        if (currentIs(DlangTypes.OP_COLON)) {
            advance();
            if (isType()) {
                if (!parseType()) {
                    cleanup(m, DlangTypes.TEMPLATE_ALIAS_PARAMETER);
                    return false;
                }
            }
            else if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.TEMPLATE_ALIAS_PARAMETER);
                return false;
            }
        }
        if (currentIs(DlangTypes.OP_EQ)) {
            advance();
            if (isType()) {
                if (!parseType()) {
                    cleanup(m, DlangTypes.TEMPLATE_ALIAS_PARAMETER);
                    return false;
                }
            }
            else if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.TEMPLATE_ALIAS_PARAMETER);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_ALIAS_PARAMETER, true);
        return true;
    }

    /**
     * Parses a TemplateArgument
     *
     *
     * $(GRAMMAR $(RULEDEF templateArgument):
     * $(RULE type)
     * | $(RULE assignExpression)
     * ;)
     */
    public boolean parseTemplateArgument() {
        PsiBuilder.Marker m = myBuilder.mark();
        int startIndex = myBuilder.getCurrentOffset();
        boolean p = myCachedTypedChecks.containsKey(startIndex);
        if (p) {
            if (Objects.requireNonNull(myCachedTypedChecks.get(startIndex))) {
                parseType();
            }
            else {
                if (parseAssignExpression() == null) {
                    cleanup(m, DlangTypes.TEMPLATE_ARGUMENT);
                    return false;
                }
            }
        }
        else {
            PsiBuilder.Marker bookmark = myBuilder.mark();
            boolean t = parseType();
            if (t && currentIsOneOf(DlangTypes.OP_COMMA, DlangTypes.OP_PAR_RIGHT)) {
                myCachedTypedChecks.put(startIndex, true);
                bookmark.drop();
            }
            else {
                myCachedTypedChecks.put(startIndex, false);
                bookmark.rollbackTo();
                if (parseAssignExpression() == null) {
                    cleanup(m, DlangTypes.TEMPLATE_ARGUMENT);
                    return false;
                }
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_ARGUMENT, true);
        return true;
    }

    /**
     * Parses a TemplateArgumentList
     *
     *
     * $(GRAMMAR $(RULEDEF templateArgumentList):
     * $(RULE templateArgument) ($(LITERAL ',') $(RULE templateArgument)?)*
     * ;)
     */
    public boolean parseTemplateArgumentList() {
        PsiBuilder.Marker marker = myBuilder.mark();
        while (moreTokens()) {
            if (!parseTemplateArgument()) {
                cleanup(marker, DlangTypes.TEMPLATE_ARGUMENT_LIST);
                return false;
            }
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
                if (currentIsOneOf(DlangTypes.OP_PAR_RIGHT)) {
                    break;
                }
            }
            else {
                break;
            }
        }
        exit_section_modified(myBuilder, marker, DlangTypes.TEMPLATE_ARGUMENT_LIST, true);
        return true;
    }

    /**
     * Parses TemplateArguments
     *
     *
     * $(GRAMMAR $(RULEDEF templateArguments):
     * $(LITERAL '!') ($(LITERAL '$(LPAREN)') $(RULE templateArgumentList)? $(LITERAL '$(RPAREN)')) | $(RULE templateSingleArgument)
     * ;)
     */
    public boolean parseTemplateArguments() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.OP_NOT);
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            advance();
            if (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
                if (!parseTemplateArgumentList()) {
                    cleanup(m, DlangTypes.TEMPLATE_ARGUMENTS);
                    return false;
                }
            }
            if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
                cleanup(m, DlangTypes.TEMPLATE_ARGUMENTS);
                return false;
            }
        }
        else if (!parseTemplateSingleArgument()) {
            cleanup(m, DlangTypes.TEMPLATE_ARGUMENTS);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_ARGUMENTS, true);
        return true;
    }

    /**
     * Parses a TemplateDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF templateDeclaration):
     * $(LITERAL 'template') $(LITERAL Identifier) $(RULE templateParameters) $(RULE raint)? $(LITERAL '{') $(RULE declaration)* $(LITERAL '}')
     * ;)
     */
    public boolean parseTemplateDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_TEMPLATE) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        if (!parseTemplateDeclarationCommon()) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        m.done(DlangTypes.TEMPLATE_DECLARATION);
        return true;
    }

    private boolean parseTemplateDeclarationCommon() {
        assert myBuilder.getTokenType() == DlangTypes.KW_TEMPLATE;
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() != DlangTypes.ID) {
            return false;
        }
        advance();
        if (!parseTemplateParameters()) {
            return true;
        }
        if (currentIs(DlangTypes.KW_IF)) {
            if (!parseConstraint()) {
                return true;
            }
        }
        if (expect(DlangTypes.OP_BRACES_LEFT) == null) {
            return true;
        }
        parseDeclDefsWithRecoveryUpToParentScope();
        expect(DlangTypes.OP_BRACES_RIGHT);
        return true;
    }

    /**
     * Parses a TemplateInstance
     *
     *
     * $(GRAMMAR $(RULEDEF templateInstance):
     * $(LITERAL Identifier) $(RULE templateArguments)
     * ;)
     */
    public boolean parseTemplateInstance() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            cleanup(m, DlangTypes.TEMPLATE_INSTANCE);
            return false;
        }
        if (!currentIs(DlangTypes.OP_NOT)) {
            cleanup(m, DlangTypes.TEMPLATE_INSTANCE);
            return false;
        }
        if (!parseTemplateArguments()) {
            cleanup(m, DlangTypes.TEMPLATE_INSTANCE);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_INSTANCE, true);
        return true;
    }

    /**
     * Parses a TemplateMixin
     *
     *
     * $(GRAMMAR $(RULEDEF templateMixinExpression):
     * $(LITERAL 'mixin') $(RULE mixinTemplateName) $(RULE templateArguments)? $(LITERAL Identifier)?
     * ;)
     */
    public boolean parseTemplateMixin(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_MIXIN) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (!parseMixinTemplateName()) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        if (currentIs(DlangTypes.OP_NOT)) {
            if (!parseTemplateArguments()) {
                m.done(DlangTypes.TEMPLATE_MIXIN);
                return true;
            }
        }
        if (currentIs(DlangTypes.ID)) {
            advance();
        }
        expect(DlangTypes.OP_SCOLON);
        m.done(DlangTypes.TEMPLATE_MIXIN);
        return true;
    }

    /**
     * Parses a TemplateParameter
     *
     *
     * $(GRAMMAR $(RULEDEF templateParameter):
     * $(RULE templateTypeParameter)
     * | $(RULE templateValueParameter)
     * | $(RULE templateAliasParameter)
     * | $(RULE templateTupleParameter)
     * | $(RULE templateThisParameter)
     * ;)
     */
    public boolean parseTemplateParameter() {
        IElementType i = current();
        if (i == DlangTypes.KW_ALIAS) {
            return parseTemplateAliasParameter();
        }
        else if (i == DlangTypes.KW_THIS) {
            return parseTemplateThisParameter();
        }
        else if (i == DlangTypes.ID) {
            if (peekIs(DlangTypes.OP_TRIPLEDOT)) {
                return parseTemplateTupleParameter();
            }
            else if (peekIsOneOf(
                DlangTypes.OP_COLON,
                DlangTypes.OP_EQ,
                DlangTypes.OP_COMMA,
                DlangTypes.OP_PAR_RIGHT
            )) {
                return parseTemplateTypeParameter();
            }
        }
        return parseTemplateValueParameter();
    }

    /**
     * Parses a TemplateParameterList
     *
     *
     * $(GRAMMAR $(RULEDEF templateParameterList):
     * $(RULE templateParameter) ($(LITERAL ',') $(RULE templateParameter)?)* $(LITERAL ',')?
     * ;)
     */
    public boolean parseTemplateParameterList() {
        PsiBuilder.Marker marker = myBuilder.mark();
        while (moreTokens()) {
            if (!parseTemplateParameter()) {
                cleanup(marker, DlangTypes.TEMPLATE_PARAMETER_LIST);
                return false;
            }

            if (myBuilder.getTokenType() != DlangTypes.OP_COMMA) {
                break;
            }

            advance();

            if (currentIsOneOf(
                DlangTypes.OP_PAR_RIGHT,
                DlangTypes.OP_BRACES_RIGHT,
                DlangTypes.OP_BRACKET_RIGHT
            )) {
                break;
            }
        }
        marker.done(DlangTypes.TEMPLATE_PARAMETER_LIST);
        return true;
    }

    /**
     * Parses TemplateParameters
     *
     *
     * $(GRAMMAR $(RULEDEF templateParameters):
     * $(LITERAL '$(LPAREN)') $(RULE templateParameterList)? $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseTemplateParameters() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.TEMPLATE_PARAMETERS);
            return false;
        }
        if (!currentIs(DlangTypes.OP_PAR_RIGHT)) {
            if (!parseTemplateParameterList()) {
                cleanup(m, DlangTypes.TEMPLATE_PARAMETERS);
                return false;
            }
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.TEMPLATE_PARAMETERS);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_PARAMETERS, true);
        return true;
    }

    /**
     * Parses a TemplateSingleArgument
     *
     *
     * $(GRAMMAR $(RULEDEF templateSingleArgument):
     * $(RULE builtinType)
     * | $(LITERAL Identifier)
     * | $(LITERAL CharacterLiteral)
     * | $(LITERAL StringLiteral)
     * | $(LITERAL IntegerLiteral)
     * | $(LITERAL FloatLiteral)
     * | $(LITERAL '_true')
     * | $(LITERAL '_false')
     * | $(LITERAL '_null')
     * | $(LITERAL 'this')
     * | $(LITERAL '___DATE__')
     * | $(LITERAL '___FILE__')
     * | $(LITERAL '___FILE_FULL_PATH__')
     * | $(LITERAL '___FUNCTION__')
     * | $(LITERAL '___LINE__')
     * | $(LITERAL '___MODULE__')
     * | $(LITERAL '___PRETTY_FUNCTION__')
     * | $(LITERAL '___TIME__')
     * | $(LITERAL '___TIMESTAMP__')
     * | $(LITERAL '___VENDOR__')
     * | $(LITERAL '___VERSION__')
     * ;)
     */
    public boolean parseTemplateSingleArgument() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!moreTokens()) {
            error("template argument expected instead of EOF");
            exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_SINGLE_ARGUMENT, true);
            return false;
        }
        IElementType i = current();
        if (i == DlangTypes.KW_THIS || i == DlangTypes.ID || isLiteral(i)) {
            advance();
        }
        else if (isBuiltinType(i)) {
            parseBuiltinType();
        }
        else {
            error("Invalid template argument. (Try enclosing in parenthesis?)");
            exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_SINGLE_ARGUMENT, true);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_SINGLE_ARGUMENT, true);
        return true;
    }

    /**
     * Parses a TemplateThisParameter
     *
     *
     * $(GRAMMAR $(RULEDEF templateThisParameter):
     * $(LITERAL 'this') $(RULE templateTypeParameter)
     * ;)
     */
    public boolean parseTemplateThisParameter() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_THIS);
        if (!parseTemplateTypeParameter()) {
            cleanup(m, DlangTypes.TEMPLATE_THIS_PARAMETER);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_THIS_PARAMETER, true);
        return true;
    }

    /**
     * Parses an TemplateTupleParameter
     *
     *
     * $(GRAMMAR $(RULEDEF templateTupleParameter):
     * $(LITERAL Identifier) $(LITERAL '...')
     * ;)
     */
    public boolean parseTemplateTupleParameter() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = expect(DlangTypes.ID);
        if (i == null) {
            cleanup(m, DlangTypes.TEMPLATE_TUPLE_PARAMETER);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_TRIPLEDOT)) {
            cleanup(m, DlangTypes.TEMPLATE_TUPLE_PARAMETER);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_TUPLE_PARAMETER, true);
        return true;
    }

    /**
     * Parses a TemplateTypeParameter
     *
     *
     * $(GRAMMAR $(RULEDEF templateTypeParameter):
     * $(LITERAL Identifier) ($(LITERAL ':') $(RULE type))? ($(LITERAL '=') $(RULE type))?
     * ;)
     */
    public boolean parseTemplateTypeParameter() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            cleanup(m, DlangTypes.TEMPLATE_TYPE_PARAMETER);
            return false;
        }
        if (currentIs(DlangTypes.OP_COLON)) {
            advance();
            if (!parseType()) {
                cleanup(m, DlangTypes.TEMPLATE_TYPE_PARAMETER);
                return false;
            }
        }
        if (currentIs(DlangTypes.OP_EQ)) {
            advance();
            if (!parseType()) {
                cleanup(m, DlangTypes.TEMPLATE_TYPE_PARAMETER);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_TYPE_PARAMETER, true);
        return true;
    }

    /**
     * Parses a TemplateValueParameter
     *
     *
     * $(GRAMMAR $(RULEDEF templateValueParameter):
     * $(RULE type) $(LITERAL Identifier) ($(LITERAL ':') $(RULE assignExpression))? $(RULE templateValueParameterDefault)?
     * ;)
     */
    public boolean parseTemplateValueParameter() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!parseType()) {
            cleanup(m, DlangTypes.TEMPLATE_VALUE_PARAMETER);
            return false;
        }
        if (!tokenCheck(DlangTypes.ID)) {
            cleanup(m, DlangTypes.TEMPLATE_VALUE_PARAMETER);
            return false;
        }
        if (currentIs(DlangTypes.OP_COLON)) {
            advance();
            if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.TEMPLATE_VALUE_PARAMETER);
                return false;
            }
        }
        if (currentIs(DlangTypes.OP_EQ)) {
            if (!parseTemplateValueParameterDefault()) {
                cleanup(m, DlangTypes.TEMPLATE_VALUE_PARAMETER);
                return false;
            }
        }
        m.done(DlangTypes.TEMPLATE_VALUE_PARAMETER);
        return true;
    }

    /**
     * Parses a TemplateValueParameterDefault
     *
     *
     * $(GRAMMAR $(RULEDEF templateValueParameterDefault):
     * $(LITERAL '=') $(LITERAL '___DATE__')
     * | $(LITERAL '=') $(LITERAL '___FILE__')
     * | $(LITERAL '=') $(LITERAL '___FILE_FULL_PATH__')
     * | $(LITERAL '=') $(LITERAL '___FUNCTION__')
     * | $(LITERAL '=') $(LITERAL '___LINE__')
     * | $(LITERAL '=') $(LITERAL '___MODULE__')
     * | $(LITERAL '=') $(LITERAL '___PRETTY_FUNCTION__')
     * | $(LITERAL '=') $(LITERAL '___TIME__')
     * | $(LITERAL '=') $(LITERAL '___TIMESTAMP__')
     * | $(LITERAL '=') $(LITERAL '___VENDOR__')
     * | $(LITERAL '=') $(LITERAL '___VERSION__')
     * | $(LITERAL '=') $(RULE assignExpression))
     * ;)
     */
    public boolean parseTemplateValueParameterDefault() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.OP_EQ);
        IElementType i = current();
        if (isMagicKeywordLiteral(i)) {
            advance();
        }
        else {
            if (parseAssignExpression() == null) {
                cleanup(m, DlangTypes.TEMPLATE_VALUE_PARAMETER_DEFAULT);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.TEMPLATE_VALUE_PARAMETER_DEFAULT, true);
        return true;
    }

    public boolean isMagicKeywordLiteral(@Nullable IElementType i) {
        return i == DlangTypes.KW___FILE__ || i == DlangTypes.KW___FILE_FULL_PATH__ || i == DlangTypes.KW___MODULE__ || i == DlangTypes.KW___LINE__ || i == DlangTypes.KW___FUNCTION__ || i == DlangTypes.KW___PRETTY_FUNCTION__;
    }

    /**
     * Parses a TernaryExpression (called ConditionalExpression in specs)
     *
     *
     * $(GRAMMAR $(RULEDEF ternaryExpression):
     * $(RULE orOrExpression) ($(LITERAL '?') $(RULE expression) $(LITERAL ':') $(RULE ternaryExpression))?
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseTernaryExpression() {
        PsiBuilder.Marker orOrExpression = parseOrOrExpression();
        if (orOrExpression == null) {
            return null;
        }
        if (currentIs(DlangTypes.OP_QUEST)) {
            PsiBuilder.Marker m = orOrExpression.precede();
            advance();
            if (!parseExpression()) {
                m.done(DlangTypes.TERNARY_EXPRESSION);
                return null;
            }
            IElementType colon = expect(DlangTypes.OP_COLON);
            if (colon == null) {
                m.done(DlangTypes.TERNARY_EXPRESSION);
                return null;
            }
            if (parseTernaryExpression() == null) {
                m.done(DlangTypes.TERNARY_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.TERNARY_EXPRESSION);
            return m;
        }
        return orOrExpression;
    }

    /**
     * Parses a ThrowExpression
     *
     *
     * $(GRAMMAR $(RULEDEF throwExpression):
     * $(LITERAL 'throw') $(RULE assignExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseThrowExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_THROW);
        if (parseAssignExpression() == null) {
            m.done(DlangTypes.THROW_EXPRESSION);
            return null;
        }
        m.done(DlangTypes.THROW_EXPRESSION);
        return m;
    }

    /**
     * Parses an TraitsExpression
     *
     *
     * $(GRAMMAR $(RULEDEF traitsExpression):
     * $(LITERAL '___traits') $(LITERAL '$(LPAREN)') $(LITERAL Identifier) $(LITERAL ',') $(RULE TemplateArgumentList) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseTraitsExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW___TRAITS)) {
            cleanup(m, DlangTypes.TRAITS_EXPRESSION);
            return null;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.TRAITS_EXPRESSION);
            return null;
        }
        IElementType ident = expect(DlangTypes.ID);
        if (ident == null) {
            cleanup(m, DlangTypes.TRAITS_EXPRESSION);
            return null;
        }
        if (currentIs(DlangTypes.OP_COMMA)) {
            advance();
            if (!(parseTemplateArgumentList())) {
                cleanup(m, DlangTypes.TRAITS_EXPRESSION);
                return null;
            }
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.TRAITS_EXPRESSION);
            return null;
        }
        exit_section_modified(myBuilder, m, DlangTypes.TRAITS_EXPRESSION, true);
        return m;
    }

    /**
     * Parses a TryStatement
     *
     *
     * $(GRAMMAR $(RULEDEF tryStatement):
     * $(LITERAL 'try') $(RULE scopeStatement) ($(RULE catches) | $(RULE catches) $(RULE finally) | $(RULE finally))
     * ;)
     */
    public boolean parseTryStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_TRY);
        if (!parseScopeStatement()) {
            cleanup(m, DlangTypes.TRY_STATEMENT);
            return false;
        }
        if (currentIs(DlangTypes.KW_CATCH)) {
            if (!parseCatches()) {
                cleanup(m, DlangTypes.TRY_STATEMENT);
                return false;
            }
        }
        if (currentIs(DlangTypes.KW_FINALLY)) {
            if (!parseFinally()) {
                cleanup(m, DlangTypes.TRY_STATEMENT);
                return false;
            }
        }
        exit_section_modified(myBuilder, m, DlangTypes.TRY_STATEMENT, true);
        return true;
    }

    /**
     * Parses a Type
     *
     *
     * $(GRAMMAR $(RULEDEF type):
     * $(RULE typeConstructors)? $(RULE basicType) $(RULE typeSuffix)*
     * ;)
     */
    public boolean parseType() {
        return parseType(false);
    }

    /**
     * Parses a Type
     *
     *
     * $(GRAMMAR $(RULEDEF type):
     * $(RULE typeConstructors)? $(RULE basicType) $(RULE typeSuffix)*
     * ;)
     */
    public boolean parseType(boolean inNewExpression) {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (isTypeCtor(i)) {
            if (!peekIs(DlangTypes.OP_PAR_LEFT)) {
                if (!parseTypeConstructors()) {
                    m.done(DlangTypes.TYPE);
                    return false;
                }
            }
        }
        if (!parseBasicType()) {
            m.done(DlangTypes.TYPE);
            return false;
        }
        while (moreTokens()) {
            IElementType i1 = current();

            if (i1 == DlangTypes.OP_BRACKET_LEFT) {
                PsiBuilder.Marker bookmark = myBuilder.mark();
                if (inNewExpression) {
                    myBuilder.advanceLexer();
                    if (parseAssignExpression() != null && myBuilder.getTokenType() == DlangTypes.OP_BRACES_RIGHT) {
                        myBuilder.advanceLexer();
                        if (myBuilder.getTokenType() != DlangTypes.OP_BRACKET_LEFT && myBuilder.getTokenType() != DlangTypes.OP_ASTERISK && myBuilder.getTokenType() != DlangTypes.KW_DELEGATE && myBuilder.getTokenType() != DlangTypes.KW_FUNCTION && myBuilder.getTokenType() != DlangTypes.OP_PAR_LEFT) {
                            // This one is not a type prefix but part of the parent newExpression
                            bookmark.rollbackTo();
                            break;
                        }
                    }
                }
                bookmark.rollbackTo();
                if (!parseTypeSuffix()) {
                    m.done(DlangTypes.TYPE);
                    return false;
                }
            }
            else if (i1 == DlangTypes.OP_ASTERISK || i1 == DlangTypes.KW_DELEGATE || i1 == DlangTypes.KW_FUNCTION) {
                if (!parseTypeSuffix()) {
                    m.done(DlangTypes.TYPE);
                    return false;
                }
            }
            else {
                break;
            }
        }
        m.done(DlangTypes.TYPE);
        return true;
    }

    public boolean parseUnexpectedType() {
        if (!moreTokens()) {
            return false;
        }
        IElementType i = current();
        if (isTypeCtor(i)) {
            if (!peekIs(DlangTypes.OP_PAR_LEFT)) {
                if (!parseTypeConstructors()) {
                    return false;
                }
            }
        }
        if (!parseBasicType()) {
            return false;
        }
        while (moreTokens()) {
            IElementType i1 = current();

            if (i1 == DlangTypes.OP_BRACKET_LEFT) {
                if (!parseTypeSuffix()) {
                    return false;
                }
            }
            else if (i1 == DlangTypes.OP_ASTERISK || i1 == DlangTypes.KW_DELEGATE || i1 == DlangTypes.KW_FUNCTION) {
                if (!parseTypeSuffix()) {
                    return false;
                }
            }
            else {
                break;
            }
        }
        return true;
    }

    public void parseTypeSuffixes() {
        while (!myBuilder.eof()) {
            if (!parseTypeSuffix()) {
                break;
            }
        }
    }

    /**
     * Parses a BasicType
     *
     *
     * $(GRAMMAR $(RULEDEF basicType):
     * $(RULE builtinType)
     * | $(RULE qualifiedIdentifier)
     * | $(LITERAL 'super') $(LITERAL '.') $(RULE qualifiedIdentifier)
     * | $(LITERAL 'this') $(LITERAL '.') $(RULE qualifiedIdentifier)
     * | $(RULE typeofExpression) ($(LITERAL '.') $(RULE qualifiedIdentifier))?
     * | $(RULE typeConstructor) $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL '$(RPAREN)')
     * | $(RULE traitsExpression)
     * | $(RULE vector)
     * | $(RULE mixinExpression)
     * ;)
     */
    public boolean parseBasicType() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (myBuilder.getTokenType() == DlangTypes.OP_DOT) {
            myBuilder.advanceLexer();
        }
        IElementType i = myBuilder.getTokenType();
        if (i == DlangTypes.ID) {
            if (!parseQualifiedIdentifier()) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
        }
        else if (isBuiltinType(i)) {
            parseBuiltinType();
        }
        else if (i == DlangTypes.KW_SUPER || i == DlangTypes.KW_THIS) {
            // note: super can be removed but `this` can be an alias to an instance.
            advance();
            if (myBuilder.getTokenType() != DlangTypes.OP_DOT) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
            advance();
            if (!parseQualifiedIdentifier()) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
        }
        else if (i == DlangTypes.KW___TRAITS) {
            if (parseTraitsExpression() == null) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
        }
        else if (i == DlangTypes.KW_TYPEOF) {
            if (parseTypeofExpression() == null) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
            if (currentIs(DlangTypes.OP_DOT)) {
                advance();
                if (!parseQualifiedIdentifier()) {
                    m.done(DlangTypes.BASIC_TYPE);
                    return false;
                }
            }
        }
        else if (i == DlangTypes.KW_MIXIN) {
            if (!parseMixinType()) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
        }
        else if (i == DlangTypes.KW_CONST || i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_INOUT || i == DlangTypes.KW_SHARED) {
            advance();
            if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
            if (!(parseType())) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
            if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
        }
        else if (i == DlangTypes.KW___VECTOR) {
            if (!(parseVector())) {
                m.done(DlangTypes.BASIC_TYPE);
                return false;
            }
        }
        else {
            error("Basic type, type constructor, symbol, `typeof`, `__traits`, `__vector` or `mixin` expected");
            m.done(DlangTypes.BASIC_TYPE);
            return false;
        }
        m.done(DlangTypes.BASIC_TYPE);
        return true;
    }

    /**
     * Parses a TypeConstructor
     *
     *
     * $(GRAMMAR $(RULEDEF typeConstructor):
     * $(LITERAL 'const')
     * | $(LITERAL 'immutable')
     * | $(LITERAL 'inout')
     * | $(LITERAL 'shared')
     * ;)
     */
    public boolean parseTypeConstructor() {
        IElementType i = current();
        if (i == DlangTypes.KW_CONST || i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_INOUT || i == DlangTypes.KW_SHARED) {
            if (!peekIs(DlangTypes.OP_PAR_LEFT)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * Parses TypeConstructors
     *
     *
     * $(GRAMMAR $(RULEDEF typeConstructors):
     * $(RULE typeConstructor)+
     * ;)
     */
    public boolean parseTypeConstructors() {
        boolean containsConstructors = false;
        while (moreTokens()) {
            if (!parseTypeConstructor()) {
                break;
            }
            containsConstructors = true;
        }
        return containsConstructors;
    }

    /**
     * Parses a TypeSpecialization
     *
     *
     * $(GRAMMAR $(RULEDEF typeSpecialization):
     * $(RULE type)
     * | $(LITERAL 'struct')
     * | $(LITERAL 'union')
     * | $(LITERAL 'class')
     * | $(LITERAL 'interface')
     * | $(LITERAL 'enum')
     * | $(LITERAL '__vector')
     * | $(LITERAL 'function')
     * | $(LITERAL 'delegate')
     * | $(LITERAL 'super')
     * | $(LITERAL 'const')
     * | $(LITERAL 'immutable')
     * | $(LITERAL 'inout')
     * | $(LITERAL 'shared')
     * | $(LITERAL 'return')
     * | $(LITERAL 'typedef')
     * | $(LITERAL '__parameters')
     * | $(LITERAL 'module')
     * | $(LITERAL 'package')
     * ;)
     */
    public boolean parseTypeSpecialization() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if ((isTypeCtor(i) || i == DlangTypes.KW___VECTOR) && peekIsOneOf(DlangTypes.OP_PAR_RIGHT, DlangTypes.OP_COMMA)) {
            advance();
        }
        else if (i == DlangTypes.KW_STRUCT || i == DlangTypes.KW_UNION || i == DlangTypes.KW_CLASS || i == DlangTypes.KW_INTERFACE || i == DlangTypes.KW_ENUM ||
            i == DlangTypes.KW_FUNCTION || i == DlangTypes.KW_DELEGATE || i == DlangTypes.KW_SUPER || i == DlangTypes.KW_RETURN || i == DlangTypes.KW___PARAMETERS ||
            i == DlangTypes.KW_MODULE || i == DlangTypes.KW_PACKAGE) {
            advance();
        }
        else {
            if (!parseType()) {
                cleanup(m, DlangTypes.TYPE_SPECIALIZATION);
                return false;
            }
        }
        m.done(DlangTypes.TYPE_SPECIALIZATION);
        return true;
    }

    /**
     * Parses a TypeSuffix
     *
     *
     * $(GRAMMAR $(RULEDEF typeSuffix):
     * $(LITERAL '*')
     * | $(LITERAL '[') $(RULE type)? $(LITERAL ']')
     * | $(LITERAL '[') $(RULE assignExpression) $(LITERAL ']')
     * | $(LITERAL '[') $(RULE assignExpression) $(LITERAL '..')  $(RULE assignExpression) $(LITERAL ']')
     * | ($(LITERAL 'delegate') | $(LITERAL 'function')) $(RULE parameters) $(RULE memberFunctionAttribute)*
     * ;)
     */
    public boolean parseTypeSuffix() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType i = current();
        if (i == DlangTypes.OP_ASTERISK) {
            advance();
            exit_section_modified(myBuilder, m, DlangTypes.TYPE_SUFFIX, true);
            return true;
        }
        else if (i == DlangTypes.OP_BRACKET_LEFT) {
            advance();
            if (currentIs(DlangTypes.OP_BRACKET_RIGHT)) {
                advance();
                exit_section_modified(myBuilder, m, DlangTypes.TYPE_SUFFIX, true);
                return true;
            }
            PsiBuilder.Marker bookmark = myBuilder.mark();
            boolean type = parseType();
            if (type && currentIs(DlangTypes.OP_BRACKET_RIGHT)) {
                bookmark.drop();
            }
            else {
                bookmark.rollbackTo();
                if (parseAssignExpression() == null) {
                    cleanup(m, DlangTypes.TYPE_SUFFIX);
                    return false;
                }
                if (currentIs(DlangTypes.OP_DDOT)) {
                    advance();
                    if (parseAssignExpression() == null) {
                        cleanup(m, DlangTypes.TYPE_SUFFIX);
                        return false;
                    }
                }
            }
            if (!tokenCheck(DlangTypes.OP_BRACKET_RIGHT)) {
                cleanup(m, DlangTypes.TYPE_SUFFIX);
                return false;
            }
            exit_section_modified(myBuilder, m, DlangTypes.TYPE_SUFFIX, true);
            return true;
        }
        else if (i == DlangTypes.KW_DELEGATE || i == DlangTypes.KW_FUNCTION) {
            advance();
            if (!parseParameters()) {
                cleanup(m, DlangTypes.TYPE_SUFFIX);
                return false;
            }
            while (currentIsMemberFunctionAttribute()) {
                if (!parseMemberFunctionAttribute()) {
                    cleanup(m, DlangTypes.TYPE_SUFFIX);
                    return false;
                }
            }
            exit_section_modified(myBuilder, m, DlangTypes.TYPE_SUFFIX, true);
            return true;
        }
        else {
            m.drop();
            return false;
        }
    }

    /**
     * Parses a TypeidExpression
     *
     *
     * $(GRAMMAR $(RULEDEF typeidExpression):
     * $(LITERAL 'typeid') $(LITERAL '$(LPAREN)') ($(RULE type) | $(RULE expression)) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseTypeidExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_TYPEID);
        expect(DlangTypes.OP_PAR_LEFT);
        PsiBuilder.Marker bookmark = myBuilder.mark();
        boolean t = parseType();
        if (!t || !currentIs(DlangTypes.OP_PAR_RIGHT)) {
            bookmark.rollbackTo();
            if (!parseExpression()) {
                cleanup(m, DlangTypes.TYPEID_EXPRESSION);
                return null;
            }
        }
        else {
            bookmark.drop();
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        exit_section_modified(myBuilder, m, DlangTypes.TYPEID_EXPRESSION, true);
        return m;
    }

    /**
     * Parses a TypeofExpression
     *
     *
     * $(GRAMMAR $(RULEDEF typeofExpression):
     * $(LITERAL 'typeof') $(LITERAL '$(LPAREN)') ($(RULE expression) | $(LITERAL 'return')) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseTypeofExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        expect(DlangTypes.KW_TYPEOF);
        expect(DlangTypes.OP_PAR_LEFT);
        if (currentIs(DlangTypes.KW_RETURN)) {
            advance();
        }
        else if (!parseExpression()) {
            cleanup(m, DlangTypes.TYPEOF_EXPRESSION);
            return null;
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        exit_section_modified(myBuilder, m, DlangTypes.TYPEOF_EXPRESSION, true);
        return m;
    }

    /**
     * Parses a UnaryExpression
     *
     *
     * $(GRAMMAR $(RULEDEF unaryExpression):
     * $(RULE primaryExpression)
     * | $(LITERAL '&') $(RULE unaryExpression)
     * | $(LITERAL '!') $(RULE unaryExpression)
     * | $(LITERAL '*') $(RULE unaryExpression)
     * | $(LITERAL '+') $(RULE unaryExpression)
     * | $(LITERAL '-') $(RULE unaryExpression)
     * | $(LITERAL '~') $(RULE unaryExpression)
     * | $(LITERAL '++') $(RULE unaryExpression)
     * | $(LITERAL '--') $(RULE unaryExpression)
     * | $(RULE newExpression)
     * | $(RULE deleteExpression)
     * | $(RULE castExpression)
     * | $(RULE assertExpression)
     * | $(RULE throwExpression)
     * | $(RULE functionCallExpression)
     * | $(RULE indexExpression)
     * | $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL '$(RPAREN)') $(LITERAL '.') $(RULE identifierOrTemplateInstance)
     * | $(RULE unaryExpression) $(LITERAL '.') $(RULE newExpression)
     * | $(RULE unaryExpression) $(LITERAL '.') $(RULE identifierOrTemplateInstance)
     * | $(RULE unaryExpression) $(LITERAL '--')
     * | $(RULE unaryExpression) $(LITERAL '++')
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseUnaryExpression() {
        IElementType i = myBuilder.getTokenType();
        if (i == DlangTypes.OP_AND || i == DlangTypes.OP_NOT || i == DlangTypes.OP_ASTERISK || i == DlangTypes.OP_PLUS || i == DlangTypes.OP_MINUS || i == DlangTypes.OP_TILDA || i == DlangTypes.OP_PLUS_PLUS || i == DlangTypes.OP_MINUS_MINUS) {
            PsiBuilder.Marker m = myBuilder.mark();
            advance();
            if (parseUnaryExpression() == null) {
                m.done(DlangTypes.UNARY_EXPRESSION);
                return null;
            }
            m.done(DlangTypes.UNARY_EXPRESSION);
            return m;
        }
        else if (i == DlangTypes.ID && "delete".equals(myBuilder.getTokenText())) {
            // TODO add check for compiler version
            // Delete is no more a keyword, but it can be in certain cases
            myBuilder.remapCurrentToken(DlangTypes.KW_DELETE);
            return parseDeleteExpression();
        }
        else if (i == DlangTypes.KW_CAST) {
            return parseCastExpression();
        }
        else if (i == DlangTypes.KW_THROW) {
            return parseThrowExpression();
        }
        return parsePowExpression();
    }

    public PsiBuilder.@Nullable Marker parsePostfixExpressionTerminal() {
        PsiBuilder.Marker m = parsePrimaryExpression();
        if (m != null) {
            return m;
        }
        // TypeCtor
        IElementType i = current();
        if (isTypeCtor(i)) {
            m = myBuilder.mark();
            myBuilder.advanceLexer();
            parseBasicType();
            if (expect(DlangTypes.OP_PAR_LEFT) == null) {
                m.rollbackTo();
                return null;
            }
            parseNamedArgumentList();
            expect(DlangTypes.OP_PAR_RIGHT);
            m.done(DlangTypes.POSTFIX_EXPRESSION);
            return m;
        }
        return null;
    }

    public PsiBuilder.@Nullable Marker parsePostfixExpression0(PsiBuilder.Marker m) {
        if (currentIs(DlangTypes.OP_DOT)) {
            PsiBuilder.Marker marker = m.precede();
            myBuilder.advanceLexer();
            PsiBuilder.Marker bookmark = myBuilder.mark();
            if (currentIs(DlangTypes.KW_NEW)) {
                bookmark.drop();
                parseNewExpression();
            }
            else if (parseTemplateInstance()) {
                bookmark.drop();
            }
            else {
                bookmark.rollbackTo();
                if (myBuilder.getTokenType() == DlangTypes.ID) {
                    advance();
                }
                else {
                    myBuilder.error(LocalizeValue.localizeTODO("Identifier, template instance or new expression expected"));
                }
            }
            marker.done(DlangTypes.REFERENCE_EXPRESSION);
            return marker;
        }
        if (currentIsOneOf(DlangTypes.OP_PLUS_PLUS, DlangTypes.OP_MINUS_MINUS)) {
            PsiBuilder.Marker marker = m.precede();
            myBuilder.advanceLexer();
            marker.done(DlangTypes.POSTFIX_EXPRESSION);
            return marker;
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            PsiBuilder.Marker marker = m.precede();
            myBuilder.advanceLexer();
            parseNamedArgumentList();
            expect(DlangTypes.OP_PAR_RIGHT);
            marker.done(DlangTypes.FUNCTION_CALL_EXPRESSION);
            return marker;
        }
        if (currentIs(DlangTypes.OP_BRACKET_LEFT)) {
            PsiBuilder.Marker marker = m.precede();
            parseSliceOrIndexExpression();
            marker.done(DlangTypes.ARRAY_ACCESS_EXPRESSION);
            return marker;
        }
        return null;
    }

    public void parseSliceOrIndexExpression() {
        PsiBuilder.Marker m = myBuilder.mark();
        advance();
        if (currentIs(DlangTypes.OP_BRACKET_RIGHT)) {
            advance();
            m.done(DlangTypes.INDEX_EXPRESSION);
            return;
        }
        if (parseAssignExpression() == null) {
            m.drop();
            expect(DlangTypes.OP_BRACKET_RIGHT);
            return;
        }
        while (!myBuilder.eof() && myBuilder.getTokenType() == DlangTypes.OP_COMMA) {
            advance();
            if (myBuilder.getTokenType() != DlangTypes.OP_BRACKET_RIGHT) {
                parseAssignExpression();
            }
        }
        if (myBuilder.getTokenType() == DlangTypes.OP_BRACKET_RIGHT) {
            advance();
            m.done(DlangTypes.INDEX_EXPRESSION);
            return;
        }
        while (!myBuilder.eof() && (myBuilder.getTokenType() == DlangTypes.OP_COMMA || myBuilder.getTokenType() == DlangTypes.OP_DDOT)) {
            advance();
            parseAssignExpression();
        }
        expect(DlangTypes.OP_BRACKET_RIGHT);
        m.done(DlangTypes.INDEX_EXPRESSION);
    }

    public PsiBuilder.@Nullable Marker parsePostfixExpression() {
        PsiBuilder.Marker m = parsePostfixExpressionTerminal();
        if (m == null) {
            return m;
        }
        while (true) {
            PsiBuilder.Marker marker = parsePostfixExpression0(m);
            if (marker == null) {
                break;
            }
            m = marker;
        }
        return m;
    }

    /**
     * Parses an UnionDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF unionDeclaration):
     * $(LITERAL 'union') $(LITERAL Identifier) ($(RULE templateParameters) $(RULE constraint)?)? ($(RULE structBody) | $(LITERAL ';'))
     * | $(LITERAL 'union') $(RULE structBody)
     * ;)
     */
    public boolean parseUnionDeclaration(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_UNION) {
            return false;
        }
        myBuilder.advanceLexer();
        if (currentIs(DlangTypes.ID)) {
            advance();
            if (currentIs(DlangTypes.OP_PAR_LEFT)) {
                if (!parseTemplateParameters()) {
                    m.done(DlangTypes.UNION_DECLARATION);
                    return true;
                }
                if (currentIs(DlangTypes.KW_IF)) {
                    if (!parseConstraint()) {
                        m.done(DlangTypes.UNION_DECLARATION);
                        return true;
                    }
                }
            }
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
        }
        else if (!parseStructBody()) {
            m.done(DlangTypes.UNION_DECLARATION);
            return true;
        }
        m.done(DlangTypes.UNION_DECLARATION);
        return true;
    }

    /**
     * Parses a Unittest
     *
     *
     * $(GRAMMAR $(RULEDEF unittest):
     * $(LITERAL 'unittest') $(RULE blockStatement)
     * ;)
     */
    public boolean parseUnittest(PsiBuilder.Marker marker) {
        if (myBuilder.getTokenType() != DlangTypes.KW_UNITTEST) {
            return false;
        }
        myBuilder.advanceLexer();
        parseBlockStatement();
        marker.done(DlangTypes.UNITTEST);
        return true;
    }

    /**
     * Parses a VariableDeclaration
     *
     *
     * $(GRAMMAR $(RULEDEF variableDeclaration):
     * $(RULE storageClass)* $(RULE type) $(RULE declarator) ($(LITERAL ',') $(RULE declarator))* $(LITERAL ';')
     * | $(RULE autoDeclaration)
     * $(GRAMMAR $(RULEDEF autoDeclaration):
     * $(RULE storageClass)+  $(RULE autoDeclarationPart) ($(LITERAL ',') $(RULE autoDeclarationPart))* $(LITERAL ';')
     * ;)
     */
    public boolean parseVariableDeclaration(PsiBuilder.Marker m) {
        PsiBuilder.Marker preCheckBookmark = myBuilder.mark();
        while (myBuilder.getTokenType() == DlangTypes.OP_AT) {
            parseAtAttribute();
        }
        // It is not a variable declaration but an enum declaration with an empty body
        if (myBuilder.getTokenType() == DlangTypes.KW_ENUM && peekAre(DlangTypes.ID, DlangTypes.OP_SCOLON)) {
            preCheckBookmark.rollbackTo();
            return false;
        }
        preCheckBookmark.rollbackTo();

        PsiBuilder.Marker bookmark = myBuilder.mark();
        boolean hasStorageClass = false;
        while (!myBuilder.eof()) {
            if (!parseStorageClass()) {
                break;
            }
            hasStorageClass = true;
        }
        PsiBuilder.Marker bookmark2 = myBuilder.mark();
        boolean parsedVariable = parseNonAutoVariableDeclaration();
        if (!parsedVariable && hasStorageClass) {
            bookmark2.rollbackTo();
            // Then it’s an auto declaration
            parseAutoAssignments();
            if (myBuilder.getTokenType() != DlangTypes.OP_SCOLON) {
                bookmark.rollbackTo();
                return false;
            }
            bookmark.drop();
            myBuilder.advanceLexer();
            m.done(DlangTypes.AUTO_DECLARATION);
            return true;
        }
        else {
            bookmark2.drop();
        }
        if (!parsedVariable) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        expect(DlangTypes.OP_SCOLON);
        m.done(DlangTypes.VARIABLE_DECLARATION);
        return true;
    }

    private boolean parseNonAutoVariableDeclaration() {
        if (!parseBasicType()) {
            return false;
        }
        parseTypeSuffixes();
        boolean hasDeclarator = false;
        while (!myBuilder.eof()) {
            if (!parseIdentifierInitializer()) {
                break;
            }
            hasDeclarator = true;
            if (currentIs(DlangTypes.OP_COMMA)) {
                advance();
            }
            else {
                break;
            }
        }
        return hasDeclarator;
    }

    /**
     * Parses a Vector
     *
     *
     * $(GRAMMAR $(RULEDEF vector):
     * $(LITERAL '___vector') $(LITERAL '$(LPAREN)') $(RULE type) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseVector() {
        PsiBuilder.Marker marker = myBuilder.mark();
        if (expect(DlangTypes.KW___VECTOR) == null) {
            cleanup(marker, DlangTypes.VECTOR);
            return false;
        }
        if (expect(DlangTypes.OP_PAR_LEFT) == null) {
            cleanup(marker, DlangTypes.VECTOR);
            return false;
        }
        if (!parseType()) {
            cleanup(marker, DlangTypes.VECTOR);
            return false;
        }
        if (expect(DlangTypes.OP_PAR_RIGHT) == null) {
            cleanup(marker, DlangTypes.VECTOR);
            return false;
        }
        exit_section_modified(myBuilder, marker, DlangTypes.VECTOR, true);
        return true;
    }

    /**
     * Parses a VersionCondition
     *
     *
     * $(GRAMMAR $(RULEDEF versionCondition):
     * $(LITERAL 'version') $(LITERAL '$(LPAREN)') ($(LITERAL IntegerLiteral) | $(LITERAL Identifier) | $(LITERAL 'unittest') | $(LITERAL 'assert')) $(LITERAL '$(RPAREN)')
     * ;)
     */
    public boolean parseVersionCondition() {
        PsiBuilder.Marker m = myBuilder.mark();
        IElementType v = expect(DlangTypes.KW_VERSION);
        if (v == null) {
            cleanup(m, DlangTypes.VERSION_CONDITION);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.VERSION_CONDITION);
            return false;
        }
        if (currentIsOneOf(
            DlangTypes.INTEGER_LITERAL,
            DlangTypes.ID,
            DlangTypes.KW_UNITTEST,
            DlangTypes.KW_ASSERT
        )) {
            advance();
        }
        else {
            error("Expected an integer literal, an identifier, `assert`, or `unittest`");
            exit_section_modified(myBuilder, m, DlangTypes.VERSION_CONDITION, true);
            return false;
        }
        expect(DlangTypes.OP_PAR_RIGHT);
        exit_section_modified(myBuilder, m, DlangTypes.VERSION_CONDITION, true);
        return true;
    }

    /**
     * Parses a VersionSpecification
     *
     *
     * $(GRAMMAR $(RULEDEF versionSpecification):
     * $(LITERAL 'version') $(LITERAL '=') ($(LITERAL Identifier) | $(LITERAL IntegerLiteral)) $(LITERAL ';')
     * ;)
     */
    public boolean parseVersionSpecification(PsiBuilder.Marker m) {
        if (myBuilder.getTokenType() != DlangTypes.KW_VERSION) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        myBuilder.advanceLexer();
        if (!tokenCheck(DlangTypes.OP_EQ)) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.drop();
        if (!currentIsOneOf(
            DlangTypes.ID,
            DlangTypes.INTEGER_LITERAL
        )) { // Note: that using integer for version is deprecated since 2.101.0
            error("Identifier or integer literal expected");
            m.done(DlangTypes.VERSION_SPECIFICATION);
            return true;
        }
        advance();
        expect(DlangTypes.OP_SCOLON);
        m.done(DlangTypes.VERSION_SPECIFICATION);
        return true;
    }

    /**
     * Parses a WhileStatement
     *
     *
     * $(GRAMMAR $(RULEDEF whileStatement):
     * $(LITERAL 'while') $(LITERAL '$(LPAREN)') $(RULE ifCondition) $(LITERAL '$(RPAREN)') $(RULE declarationOrStatement)
     * ;)
     */
    public boolean parseWhileStatement() {
        PsiBuilder.Marker m = myBuilder.mark();
        if (!tokenCheck(DlangTypes.KW_WHILE)) {
            cleanup(m, DlangTypes.WHILE_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            cleanup(m, DlangTypes.WHILE_STATEMENT);
            return false;
        }
        if (!parseIfCondition()) {
            cleanup(m, DlangTypes.WHILE_STATEMENT);
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            cleanup(m, DlangTypes.WHILE_STATEMENT);
            return false;
        }
        if (currentIs(DlangTypes.OP_BRACES_RIGHT)) {
            error("Statement expected");
            exit_section_modified(myBuilder, m, DlangTypes.WHILE_STATEMENT, true);
            return true; // this line makes DCD better
        }
        if (!parseScopeStatement()) {
            cleanup(m, DlangTypes.WHILE_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, m, DlangTypes.WHILE_STATEMENT, true);
        return true;
    }

    /**
     * Parses a WithStatement
     *
     *
     * $(GRAMMAR $(RULEDEF withStatement):
     * $(LITERAL 'with') $(LITERAL '$(LPAREN)') $(RULE expression) $(LITERAL '$(RPAREN)') $(RULE declarationOrStatement)
     * ;)
     */
    public boolean parseWithStatement() {
        PsiBuilder.Marker marker = myBuilder.mark();
        if (expect(DlangTypes.KW_WITH) == null) {
            cleanup(marker, DlangTypes.WITH_STATEMENT);
            return false;
        }
        if (expect(DlangTypes.OP_PAR_LEFT) == null) {
            cleanup(marker, DlangTypes.WITH_STATEMENT);
            return false;
        }
        if (!parseExpression()) {
            cleanup(marker, DlangTypes.WITH_STATEMENT);
            return false;
        }
        if (expect(DlangTypes.OP_PAR_RIGHT) == null) {
            cleanup(marker, DlangTypes.WITH_STATEMENT);
            return false;
        }
        if (!parseScopeStatement()) {
            cleanup(marker, DlangTypes.WITH_STATEMENT);
            return false;
        }
        exit_section_modified(myBuilder, marker, DlangTypes.WITH_STATEMENT, true);
        return true;
    }

    /**
     * Parses an XorExpression
     *
     *
     * $(GRAMMAR $(RULEDEF xorExpression):
     * $(RULE andExpression)
     * | $(RULE xorExpression) $(LITERAL '^') $(RULE andExpression)
     * ;)
     */
    public PsiBuilder.@Nullable Marker parseXorExpression() {
        PsiBuilder.Marker marker = parseAndExpression();
        if (marker == null) {
            return null;
        }
        while (currentIs(DlangTypes.OP_XOR)) {
            marker = marker.precede();
            myBuilder.advanceLexer();
            if (parseAndExpression() == null) {
                marker.done(DlangTypes.XOR_EXPRESSION);
                return null;
            }
            marker.done(DlangTypes.XOR_EXPRESSION);
        }
        return marker;
    }

    public void recover(String errorMessage) {
        PsiBuilder.Marker recovery = myBuilder.mark();
        int parentLevel = 0;
        while (!myBuilder.eof()) {
            if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_RIGHT) {
                parentLevel--;
                if (parentLevel == 0) {
                    // this one belongs to the current scope, so go further
                    myBuilder.advanceLexer();
                    // If we opened block, then it’s the end of the current declaration
                    break;
                }
                if (parentLevel < 0) {
                    break;
                }
            }
            else if (myBuilder.getTokenType() == DlangTypes.OP_BRACES_LEFT) {
                parentLevel++;
            }
            else if (myBuilder.getTokenType() == DlangTypes.OP_SCOLON) {
                if (parentLevel <= 0) {
                    break;
                }
            }
            myBuilder.advanceLexer();
        }
        if (myBuilder.getTokenType() == DlangTypes.OP_SCOLON) {
            myBuilder.advanceLexer();
        }
        recovery.error(LocalizeValue.localizeTODO(errorMessage));
    }

    public void initializerRecover(String errorMessage, IElementType openingTokenType, IElementType closingTokenType) {
        PsiBuilder.Marker recovery = myBuilder.mark();
        int parentLevel = 0;
        while (!myBuilder.eof()) {
            if (myBuilder.getTokenType() == closingTokenType) {
                parentLevel--;
                if (parentLevel == 0) {
                    // we are at the closing character of the array, stop it
                    break;
                }
                if (parentLevel < 0) {
                    break;
                }
            }
            else if (myBuilder.getTokenType() == openingTokenType) {
                parentLevel++;
            }
            myBuilder.advanceLexer();
        }
        recovery.error(LocalizeValue.localizeTODO(errorMessage));
    }

    /**
     * Returns: true if there are more tokens
     */
    public boolean moreTokens() {
        return !myBuilder.eof();
    }

    public boolean isCastQualifier() {
        IElementType i = current();
        if (i == DlangTypes.KW_CONST) {
            if (peekIs(DlangTypes.OP_PAR_RIGHT)) {
                return true;
            }
            return startsWith(
                DlangTypes.KW_CONST,
                DlangTypes.KW_SHARED,
                DlangTypes.OP_PAR_RIGHT
            );
        }
        else if (i == DlangTypes.KW_IMMUTABLE) {
            return peekIs(DlangTypes.OP_PAR_RIGHT);
        }
        else if (i == DlangTypes.KW_INOUT) {
            if (peekIs(DlangTypes.OP_PAR_RIGHT)) {
                return true;
            }
            return startsWith(
                DlangTypes.KW_INOUT,
                DlangTypes.KW_SHARED,
                DlangTypes.OP_PAR_RIGHT
            );
        }
        else if (i == DlangTypes.KW_SHARED) {
            if (peekIs(DlangTypes.OP_PAR_RIGHT)) {
                return true;
            }
            if (startsWith(
                DlangTypes.KW_SHARED,
                DlangTypes.KW_CONST,
                DlangTypes.OP_PAR_RIGHT
            )) {
                return true;
            }
            return startsWith(
                DlangTypes.KW_SHARED,
                DlangTypes.KW_INOUT,
                DlangTypes.OP_PAR_RIGHT
            );
        }
        else {
            return false;
        }
    }

    public boolean isAssociativeArrayLiteral() {
        if (myCachedAAChecks.containsKey(myBuilder.getCurrentOffset())) {
            return Objects.requireNonNull(myCachedAAChecks.get(myBuilder.getCurrentOffset()));
        }
        int currentIndex = myBuilder.getCurrentOffset();
        PsiBuilder.Marker bookmark = myBuilder.mark();
        advance();
        boolean result =
            !currentIs(DlangTypes.OP_BRACKET_RIGHT) && parseExpression() && currentIs(
                DlangTypes.OP_COLON
            );
        myCachedAAChecks.put(currentIndex, result);
        bookmark.rollbackTo();
        return result;
    }

    /// Only use this in template parameter parsing
    public boolean isType() {
        if (!moreTokens()) {
            return false;
        }
        PsiBuilder.Marker bookmark = myBuilder.mark();
        if (!parseType()) {
            bookmark.rollbackTo();
            return false;
        }
        bookmark.rollbackTo();
        return currentIsOneOf(
            DlangTypes.OP_COMMA,
            DlangTypes.OP_PAR_RIGHT,
            DlangTypes.OP_EQ
        );
    }

    private boolean isStorageClass() {
        if (myBuilder.eof()) {
            return false;
        }
        IElementType i = myBuilder.getTokenType();
        if (i == DlangTypes.KW_CONST || i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_INOUT || i == DlangTypes.KW_SHARED) {
            return !peekIs(DlangTypes.OP_PAR_LEFT);
        }
        else {
            return i == DlangTypes.OP_AT || i == DlangTypes.KW_DEPRECATED || i == DlangTypes.KW_ABSTRACT || i == DlangTypes.KW_ALIGN || i == DlangTypes.KW_AUTO || i == DlangTypes.KW_ENUM || i == DlangTypes.KW_EXTERN || i == DlangTypes.KW_FINAL || i == DlangTypes.KW_NOTHROW || i == DlangTypes.KW_OVERRIDE || i == DlangTypes.KW_PURE || i == DlangTypes.KW_REF || i == DlangTypes.KW___GSHARED || i == DlangTypes.KW_SCOPE || i == DlangTypes.KW_STATIC || i == DlangTypes.KW_SYNCHRONIZED;
        }
    }

    public boolean isAttribute() {
        if (myBuilder.eof()) {
            return false;
        }
        IElementType i = myBuilder.getTokenType();
        if (i == DlangTypes.KW_CONST || i == DlangTypes.KW_IMMUTABLE || i == DlangTypes.KW_INOUT || i == DlangTypes.KW_SCOPE) {
            return !peekIs(DlangTypes.OP_PAR_LEFT);
        }
        else if (i == DlangTypes.KW_STATIC) {
            return !peekIsOneOf(
                DlangTypes.KW_ASSERT,
                DlangTypes.KW_THIS,
                DlangTypes.KW_IF,
                DlangTypes.OP_TILDA,
                DlangTypes.KW_FOREACH,
                DlangTypes.KW_FOREACH_REVERSE
            );
        }
        else if (i == DlangTypes.KW_SHARED) {
            return !(startsWith(
                DlangTypes.KW_SHARED,
                DlangTypes.KW_STATIC,
                DlangTypes.KW_THIS
            ) || startsWith(
                DlangTypes.KW_SHARED,
                DlangTypes.KW_STATIC,
                DlangTypes.OP_TILDA
            ) || peekIs(DlangTypes.OP_PAR_LEFT));
        }
        else if (i == DlangTypes.KW_PRAGMA) {
            PsiBuilder.Marker bookmark = myBuilder.mark();
            advance();
            IElementType past = peekPastParens();
            if (past == null || past == DlangTypes.OP_SCOLON) {
                bookmark.rollbackTo();
                return false;
            }
            bookmark.rollbackTo();
            return true;
        }
        else {
            return i == DlangTypes.KW_DEPRECATED || i == DlangTypes.KW_PRIVATE || i == DlangTypes.KW_PACKAGE || i == DlangTypes.KW_PROTECTED || i == DlangTypes.KW_PUBLIC || i == DlangTypes.KW_EXPORT || i == DlangTypes.KW_FINAL || i == DlangTypes.KW_SYNCHRONIZED || i == DlangTypes.KW_OVERRIDE || i == DlangTypes.KW_ABSTRACT || i == DlangTypes.KW_AUTO || i == DlangTypes.KW___GSHARED || i == DlangTypes.KW_PURE || i == DlangTypes.KW_NOTHROW || i == DlangTypes.OP_AT || i == DlangTypes.KW_REF || i == DlangTypes.KW_EXTERN || i == DlangTypes.KW_ALIGN;
        }
    }

    private boolean isMemberFunctionAttribute(@Nullable IElementType t) {
        return t == DlangTypes.KW_CONST || t == DlangTypes.KW_IMMUTABLE || t == DlangTypes.KW_INOUT || t == DlangTypes.KW_SHARED || t == DlangTypes.OP_AT || t == DlangTypes.KW_PURE || t == DlangTypes.KW_NOTHROW || t == DlangTypes.KW_RETURN || t == DlangTypes.KW_SCOPE;
    }

    private boolean isTypeCtor(@Nullable IElementType t) {
        return t == DlangTypes.KW_CONST || t == DlangTypes.KW_IMMUTABLE || t == DlangTypes.KW_INOUT || t == DlangTypes.KW_SHARED;
    }

    private boolean currentIsMemberFunctionAttribute() {
        return !myBuilder.eof() && isMemberFunctionAttribute(myBuilder.getTokenType());
    }

    private void error(String message) {
        myBuilder.error(LocalizeValue.localizeTODO(message));
        while (moreTokens()) {
            if (currentIsOneOf(
                DlangTypes.OP_SCOLON, DlangTypes.OP_BRACES_RIGHT,
                DlangTypes.OP_PAR_RIGHT, DlangTypes.OP_BRACKET_RIGHT
            )) {
                advance();
                break;
            }
            else {
                advance();
            }
        }
    }

    private void errorUpToExcluded(String message, IElementType... errorElements) {
        PsiBuilder.Marker error = myBuilder.mark();
        while (moreTokens()) {
            if (currentIsOneOf(
                errorElements
            )) {
                break;
            }
            else {
                advance();
            }
        }
        error.error(LocalizeValue.localizeTODO(message));
    }

    private @Nullable IElementType peekPast(IElementType o, IElementType c) {
        if (myBuilder.eof()) {
            return null;
        }
        int depth = 1;
        PsiBuilder.Marker marker = myBuilder.mark();
        myBuilder.advanceLexer();
        while (myBuilder.getTokenType() != null) {
            if (myBuilder.getTokenType() == o) {
                ++depth;
            }
            else if (myBuilder.getTokenType() == c) {
                --depth;
            }
            myBuilder.advanceLexer();
            if (depth <= 0) {
                break;
            }
        }
        IElementType token = myBuilder.getTokenType();
        marker.rollbackTo();
        return depth == 0 ? token : null;
    }

    private @Nullable IElementType peekPastParens() {
        return peekPast(DlangTypes.OP_PAR_LEFT, DlangTypes.OP_PAR_RIGHT);
    }

    private @Nullable IElementType peekPastBraces() {
        return peekPast(DlangTypes.OP_BRACES_LEFT, DlangTypes.OP_BRACES_RIGHT);
    }

    private @Nullable IElementType peekPastBrackets() {
        return peekPast(DlangTypes.OP_BRACKET_LEFT, DlangTypes.OP_BRACKET_RIGHT);
    }

    /**
     * Returns: `true` if there is a next token and that token has the type `t`.
     */
    private boolean peekIs(IElementType t) {
        return peekNIs(t, 1);
    }

    /**
     * Returns: `true` if the token `offset` tokens ahead exists and is of type `t`.
     */
    private boolean peekNIs(IElementType t, int offset) {
        return myBuilder.lookAhead(offset) == t;
    }

    /**
     * Returns: `true` if there are at least `types.length` tokens following the
     * current one and they have types matching the corresponding elements of
     * `types`.
     */
    private boolean peekAre(IElementType... types) {
        int i = 0;
        for (IElementType type : types) {
            if (!peekNIs(type, i + 1)) {
                return false;
            }
            i++;
        }
        return true;
    }

    /**
     * Returns: `true` if there is a next token and its type is one of the given
     * `types`.
     */
    private boolean peekIsOneOf(IElementType... types) {
        IElementType needle = myBuilder.lookAhead(1);
        if (needle == null) {
            return false;
        }
        for (IElementType type : types) {
            if (type == needle) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a token of the specified type if it was the next token, otherwise
     * calls the error function and returns null. Advances the lexer by one token.
     */
    private @Nullable IElementType expect(IElementType tok) {
        if (!myBuilder.eof() && myBuilder.getTokenType() == tok) {
            return advance();
        }
        else {
            String tokenString = getDebugName(tok);
            String token = (!myBuilder.eof() ? (String.valueOf(myBuilder.getTokenType())) : "EOF");
            myBuilder.error(LocalizeValue.localizeTODO("Expected " + tokenString + " instead of " + token));
            return null;
        }
    }

    /**
     * Consulo's {@link IElementType} has no {@code getDebugName()}: its {@code toString()} returns the debug name
     * unless overridden, and {@link DlangTokenType} overrides it with a prefix, so take the raw name from there.
     */
    private static String getDebugName(IElementType type) {
        if (type instanceof DlangTokenType tokenType) {
            return tokenType.getName();
        }
        return type.toString();
    }

    /**
     * Returns: the _current token
     */
    private @Nullable IElementType current() {
        return myBuilder.getTokenType();
    }

    /**
     * Advances to the next token and returns the current token
     */
    private @Nullable IElementType advance() {
        IElementType token = myBuilder.getTokenType();
        myBuilder.advanceLexer();
        return token;
    }

    /**
     * Returns: true if the current token has the given type
     */
    private boolean currentIs(IElementType type) {
        return !myBuilder.eof() && myBuilder.getTokenType() == type;
    }

    /**
     * Returns: true if the current token is one of the given types
     */
    private boolean currentIsOneOf(IElementType... types) {
        if (myBuilder.eof()) {
            return false;
        }

        IElementType curr = myBuilder.getTokenType();

        if (curr != null) {
            return Arrays.asList(types).contains(curr);
        }
        return false;
    }

    private boolean startsWith(IElementType... types) {
        for (int i = 0; i < types.length; i++) {
            IElementType token = myBuilder.lookAhead(i);
            if (token == null || token != types[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean tokenCheck(IElementType tok) {
        return expect(tok) != null;
    }

    private boolean parseStaticCtorDtorCommon() {
        if (!tokenCheck(DlangTypes.KW_THIS)) {
            return false;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_LEFT)) {
            return true;
        }
        if (!tokenCheck(DlangTypes.OP_PAR_RIGHT)) {
            return true;
        }
        while (moreTokens() && !currentIsOneOf(
            DlangTypes.OP_BRACES_LEFT, DlangTypes.KW_IN, DlangTypes.KW_OUT,
            DlangTypes.KW_DO, DlangTypes.OP_SCOLON
        )) {
            if (!(parseMemberFunctionAttribute())) {
                return true;
            }
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
        }
        else {
            return parseFunctionBody();
        }
        return true;
    }

    public void parseInterfaceOrClass() {
        if (expect(DlangTypes.ID) == null) {
            return;
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            advance();
            return;
        }
        if (currentIs(DlangTypes.OP_BRACES_LEFT)) {
            parseStructBody();
            return;
        }
        if (currentIs(DlangTypes.OP_PAR_LEFT)) {
            if (!parseTemplateParameters()) {
                return;
            }
            if (currentIs(DlangTypes.OP_SCOLON)) {
                advance();
                return;
            }
            constraint(false);
            return;
        }
        if (currentIs(DlangTypes.OP_COLON)) {
            baseClassList();
            return;
        }
        parseStructBody();
    }

    private boolean baseClassList() {
        advance(); // :
        parseBaseClassList();
        if (currentIs(DlangTypes.KW_IF)) {
            return constraint(true);
        }
        return parseStructBody();
    }

    private boolean constraint(boolean baseClassListQ) {
        if (currentIs(DlangTypes.KW_IF)) {
            if (!parseConstraint()) {
                return false;
            }
        }
        if (baseClassListQ) {
            if (currentIs(DlangTypes.OP_BRACES_LEFT)) {
                return parseStructBody();
            }
            else if (currentIs(DlangTypes.OP_SCOLON)) {
                myBuilder.advanceLexer();
                return true;
            }
            else {
                error("Struct body or ';' expected");
                return false;
            }
        }
        if (currentIs(DlangTypes.OP_COLON)) {
            return baseClassList();
        }
        if (currentIs(DlangTypes.KW_IF)) {
            return constraint(false);
        }
        if (currentIs(DlangTypes.OP_SCOLON)) {
            myBuilder.advanceLexer();
            return true;
        }
        if (currentIs(DlangTypes.OP_COLON)) {
            return baseClassList();
        }
        return parseStructBody();
    }

    /**
     * Return true if it’s an expression initializer, false if its a struct initializer
     */
    private boolean isExpInitializer() {
        /* Scan ahead to discern between a struct initializer and
         * parameterless function literal.
         *
         * We'll scan the topmost curly bracket level for statement-related
         * tokens, thereby ruling out a struct initializer.  (A struct
         * initializer which itself contains function literals may have
         * statements at nested curly bracket levels.)
         *
         * It's important that this function literal check not be
         * pendantic, otherwise a function having the slightest syntax
         * error would emit confusing errors when we proceed to parse it
         * as a struct initializer.
         *
         * The following two ambiguous cases will be treated as a struct
         * initializer (best we can do without type info):
         *     {}
         *     {{statements...}}  - i.e. it could be struct initializer
         *        with one function literal, or function literal having an
         *        extra level of curly brackets
         * If a function literal is intended in these cases (unlikely),
         * source can use a more explicit function literal syntax
         * (e.g. prefix with "()" for empty parameter list).
         */
        PsiBuilder.Marker bookmark = myBuilder.mark();
        int parentheses = 0;
        int braces = 1;
        while (!myBuilder.eof()) {
            myBuilder.advanceLexer();
            IElementType tokenType = myBuilder.getTokenType();
            if (tokenType == DlangTypes.OP_PAR_LEFT) {
                parentheses++;
            }
            else if (tokenType == DlangTypes.OP_PAR_RIGHT) {
                parentheses--;
            }
            else if (tokenType == DlangTypes.KW_SCOPE) {
                // lambda param can have the `scope` storage (S s = { (scope Type Id){} })
                if (parentheses != 0 && braces == 1) {
                    bookmark.rollbackTo();
                    return false;
                }
            }
            else if (tokenType == DlangTypes.KW_ASM
                || tokenType == DlangTypes.KW_CLASS
                || tokenType == DlangTypes.KW_DEBUG
                || tokenType == DlangTypes.KW_ENUM
                || tokenType == DlangTypes.KW_IF
                || tokenType == DlangTypes.KW_INTERFACE
                || tokenType == DlangTypes.KW_PRAGMA
                || tokenType == DlangTypes.OP_SCOLON
                || tokenType == DlangTypes.KW_STRUCT
                || tokenType == DlangTypes.KW_SWITCH
                || tokenType == DlangTypes.KW_SYNCHRONIZED
                || tokenType == DlangTypes.KW_TRY
                || tokenType == DlangTypes.KW_UNION
                || tokenType == DlangTypes.KW_VERSION
                || tokenType == DlangTypes.KW_WHILE
                || tokenType == DlangTypes.KW_WITH) {
                if (braces == 1) {
                    bookmark.rollbackTo();
                    return true;
                }
            }
            else if (tokenType == DlangTypes.OP_BRACES_LEFT) {
                braces++;
            }
            else if (tokenType == DlangTypes.OP_BRACES_RIGHT) {
                if (--braces == 0) {
                    break;
                }
            }
        }
        bookmark.rollbackTo();
        return false;
    }

    /**
     * Port of IntelliJ's {@code com.intellij.lang.PsiBuilderUtil#parseBlockLazy}, which has no counterpart in Consulo's
     * {@code consulo.language.parser.PsiBuilderUtil}.
     * <p>
     * Tries to parse a code block with corresponding left and right braces.
     *
     * @return collapsed marker of the block or {@code null} if there is no code block at all.
     */
    private static PsiBuilder.@Nullable Marker parseBlockLazy(PsiBuilder builder,
                                                              IElementType leftBrace,
                                                              IElementType rightBrace,
                                                              IElementType codeBlock) {
        if (builder.getTokenType() != leftBrace) {
            return null;
        }

        PsiBuilder.Marker marker = builder.mark();

        builder.advanceLexer();

        int braceCount = 1;

        while (braceCount > 0 && !builder.eof()) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == leftBrace) {
                braceCount++;
            }
            else if (tokenType == rightBrace) {
                braceCount--;
            }
            builder.advanceLexer();
        }

        marker.collapse(codeBlock);

        if (braceCount > 0) {
            marker.setCustomEdgeTokenBinders(null, WhitespacesBinders.GREEDY_RIGHT_BINDER);
        }

        return marker;
    }
}
