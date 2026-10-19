package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.language.editor.template.LiveTemplateContributor;
import consulo.localize.LocalizeValue;
import io.github.intellij.dlanguage.template.DlangContext;

/**
 * Upstream's default live templates, {@code <defaultLiveTemplates file="/dlang/ide/liveTemplates/D"/>} in
 * {@code plugin.xml}, converted from {@code D.xml}. Its {@code toShortenFQNames} has no D implementation here (only the
 * Java plugin has one), and its second, identical {@code template} entry is dropped, as IntelliJ keeps one template per
 * group and name.
 */
@ExtensionImpl
public class DLiveTemplateContributor implements LiveTemplateContributor {
    @Override
    public String groupId() {
        return "d";
    }

    @Override
    public LocalizeValue groupName() {
        return DLocalize.liveTemplateGroupD();
    }

    @Override
    public void contribute(Factory factory) {
        try (Builder builder = factory.newBuilder("dIf", "if", "if($BOOL$) {\n   /* statement(s) will execute if the boolean expression is true */\n}", DLocalize.liveTemplateDescriptionIf())) {
            builder.withVariable("BOOL", "", "\"true\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dModule", "module", "//          Copyright $AUTHOR2$ $YEAR$. \n// Distributed under the Boost Software License, Version 1.0. \n//    (See accompanying file LICENSE_1_0.txt or copy at \n//          http://www.boost.org/LICENSE_1_0.txt)} \n \nmodule $PACKAGE$.$NAME$; \n \n \n$END$ \n", DLocalize.liveTemplateDescriptionModule())) {
            builder.withVariable("AUTHOR2", "user()", "", false);
            builder.withVariable("YEAR", "date(\"yyyy\")", "", false);
            builder.withVariable("PACKAGE", "", "\"package\"", true);
            builder.withVariable("NAME", "fileNameWithoutExtension()", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dImp", "imp", "import $STD$.$STDIO$; \n$END$ \n", DLocalize.liveTemplateDescriptionImp())) {
            builder.withVariable("STD", "", "\"std\"", true);
            builder.withVariable("STDIO", "", "\"stdio\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dAl", "al", "alias $ALIAS$ = $NAME$; \n$END$\n", DLocalize.liveTemplateDescriptionAl())) {
            builder.withReformat();

            builder.withVariable("ALIAS", "", "\"alias_\"", true);
            builder.withVariable("NAME", "", "\"definition\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dSt", "st", "static $END$ \n", DLocalize.liveTemplateDescriptionSt())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dNew", "new", "new $TYPE$($END$) \n", DLocalize.liveTemplateDescriptionNew())) {
            builder.withVariable("TYPE", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dSpn", "spn", "$safe$$pure$$nothrow$$const$$nogc$\n", DLocalize.liveTemplateDescriptionSpn())) {
            builder.withVariable("safe", "", "\"@safe \"", true);
            builder.withVariable("pure", "", "\"pure \"", true);
            builder.withVariable("nothrow", "", "\"nothrow \"", true);
            builder.withVariable("const", "", "\"const \"", true);
            builder.withVariable("nogc", "", "\"@nogc\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dCont", "cont", "continue; \n", DLocalize.liveTemplateDescriptionCont())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dPub", "pub", "public: \n $END$ \n", DLocalize.liveTemplateDescriptionPub())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dPriv", "priv", "private: \n $END$ \n", DLocalize.liveTemplateDescriptionPriv())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dProt", "prot", "protected: \n $END$ \n", DLocalize.liveTemplateDescriptionProt())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dPack", "pack", "package: \n $END$ \n", DLocalize.liveTemplateDescriptionPack())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dRet", "ret", "return $END$; \n", DLocalize.liveTemplateDescriptionRet())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dAvar", "avar", "auto $VARIABLE1$ = $SELECTION$; \n$END$ \n", DLocalize.liveTemplateDescriptionAvar())) {
            builder.withVariable("VARIABLE1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dCvar", "cvar", "const $VARIABLE1$ = $SELECTION$; \n$END$ \n", DLocalize.liveTemplateDescriptionCvar())) {
            builder.withVariable("VARIABLE1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dIvar", "ivar", "immutable $VARIABLE1$ = $SELECTION$; \n$END$ \n", DLocalize.liveTemplateDescriptionIvar())) {
            builder.withVariable("VARIABLE1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dSupe", "supe", "super($END$); \n", DLocalize.liveTemplateDescriptionSupe())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dScope", "scope", "scope($exit$) \n{ \n    $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionScope())) {
            builder.withVariable("exit", "enum(\"exit\", \"success\", \"failure\")", "\"exit\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dWith", "with", "with($CONDITION$) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionWith())) {
            builder.withVariable("CONDITION", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dTup", "tup", "tuple($FIRST$, $MORE$) \n", DLocalize.liveTemplateDescriptionTup())) {
            builder.withVariable("FIRST", "", "", true);
            builder.withVariable("MORE", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dWr", "wr", "writeln($SELECTION$); \n$END$ \n", DLocalize.liveTemplateDescriptionWr())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dWrf", "wrf", "writefln(\"$FORMAT_STRING1$\", $SELECTION$); \n$END$ \n", DLocalize.liveTemplateDescriptionWrf())) {
            builder.withVariable("FORMAT_STRING1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dEnf", "enf", "enforce($CONDITION1$, new $TYPE$Exception(\"$MSG$\")); \n$END$ \n", DLocalize.liveTemplateDescriptionEnf())) {
            builder.withVariable("CONDITION1", "", "\"true\"", true);
            builder.withVariable("TYPE", "", "", true);
            builder.withVariable("MSG", "", "\"this should not be thrown\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dFormat", "format", "\"$FORMAT_STRING1$$END$\".format($VALUES0$)$ENDSEMI$", DLocalize.liveTemplateDescriptionFormat())) {
            builder.withVariable("FORMAT_STRING1", "", "\"%12,3.3f\"", true);
            builder.withVariable("VALUES0", "", "\"1234.5678\"", true);
            builder.withVariable("ENDSEMI", "", "\";\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dIfe", "ife", "if($CONDITION1$) \n{ \n $CODE2$ \n} \nelse \n{ \n $ELSE3$ \n} \n", DLocalize.liveTemplateDescriptionIfe())) {
            builder.withVariable("CONDITION1", "", "", true);
            builder.withVariable("CODE2", "", "", true);
            builder.withVariable("ELSE3", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dElse", "else", "else \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionElse())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dElif", "elif", "else if($CONDITION1$) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionElif())) {
            builder.withVariable("CONDITION1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dSw", "sw", "switch($VAR1$) \n{ \n case $VALUE2$: \n  $CODE3$ \n  break; \n case $VALUE4$: \n  $CODE5$ \n  break; \n $more_cases$ \n default: \n  $assert$ \n} \n", DLocalize.liveTemplateDescriptionSw())) {
            builder.withVariable("VAR1", "", "", true);
            builder.withVariable("VALUE2", "", "", true);
            builder.withVariable("CODE3", "", "", true);
            builder.withVariable("VALUE4", "", "", true);
            builder.withVariable("CODE5", "", "", true);
            builder.withVariable("more_cases", "", "", true);
            builder.withVariable("assert", "", "\"assert(false);\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dFsw", "fsw", "final switch($VAR1$) \n{ \n case $VALUE2$: \n  $CODE3$ \n  break; \n case $VALUE4$: \n  $CODE5$ \n  break; \n $MoreCases$ \n} \n", DLocalize.liveTemplateDescriptionFsw())) {
            builder.withVariable("VAR1", "", "", true);
            builder.withVariable("VALUE2", "", "", true);
            builder.withVariable("CODE3", "", "", true);
            builder.withVariable("VALUE4", "", "", true);
            builder.withVariable("CODE5", "", "", true);
            builder.withVariable("MoreCases", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dCase", "case", "case $VALUE1$: \n $VALUE2$\n break; \n$END$ \n", DLocalize.liveTemplateDescriptionCase())) {
            builder.withVariable("VALUE1", "", "", true);
            builder.withVariable("VALUE2", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dDo", "do", "do \n{ \n $SELECTION$$END$ \n} while($CONDITION1$); \n", DLocalize.liveTemplateDescriptionDo())) {
            builder.withVariable("CONDITION1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dWh", "wh", "while($CONDITION1$) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionWh())) {
            builder.withVariable("CONDITION1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dFor", "for", "for($ITERTYPE$ $ITER$ $ITERINIT$; $ITER$ $RANGE$; $ITER$$ITERINC$) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionFor())) {
            builder.withVariable("ITERTYPE", "", "\"int\"", true);
            builder.withVariable("ITER", "", "\"i\"", true);
            builder.withVariable("ITERINIT", "", "\"= 0\"", true);
            builder.withVariable("RANGE", "", "\"< count\"", true);
            builder.withVariable("ITERINC", "", "\"++\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dForever", "forever", "for(;;) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionForever())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dFore", "fore", "foreach($item$; $RANGE2$) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionFore())) {
            builder.withVariable("item", "", "\"item\"", true);
            builder.withVariable("RANGE2", "", "\"range\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dForei", "forei", "foreach(i, $ITEM$; $RANGE2$) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionForei())) {
            builder.withVariable("ITEM", "", "\"item\"", true);
            builder.withVariable("RANGE2", "", "\"range\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dForif", "forif", "foreach($ITEM$; $RANGE2$) if($CONDITION3$) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionForif())) {
            builder.withVariable("ITEM", "", "\"item\"", true);
            builder.withVariable("RANGE2", "", "\"range\"", true);
            builder.withVariable("CONDITION3", "", "\"condition\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dIn", "in", "in \n{ \n $END$ \n} \nbody \n", DLocalize.liveTemplateDescriptionIn())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dOut", "out", "out($result$) \n{ \n    $END$assert(r > 0, \"return must be positive\");\n} \ndo", DLocalize.liveTemplateDescriptionOut())) {
            builder.withVariable("result", "", "\"r\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dOuts", "outs", "out ($END$r; r > 0, \"return must be positive\")", DLocalize.liveTemplateDescriptionOuts())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dInvar", "invar", "invariant() \n{ \n $END$ \n} \n", DLocalize.liveTemplateDescriptionInvar())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dAs", "as", "assert($FALSE$, \"$TODO$\"); \n$END$ \n", DLocalize.liveTemplateDescriptionAs())) {
            builder.withVariable("FALSE", "", "", true);
            builder.withVariable("TODO", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dUtest", "utest", "unittest \n{ \n $END$ \n} \n", DLocalize.liveTemplateDescriptionUtest())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dDoctest", "doctest", "/// \nunittest \n{ \n $END$ \n} \n", DLocalize.liveTemplateDescriptionDoctest())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dFun", "fun", "/***********************************\n * $BRIEFSUMMARY$ $DNAME$ $BRIEFSUMMARY2$.\n *\n * $Description$\n *\n * Params:\n *      $DPARAMETER1NAME$ =     is for this\n *              and not for that\n *\n * Returns: $DVOID$.\n */\n$VOID$ $NAME$($PARAMETER1TYPE$ $PARAMETER1NAME$) $EX$ \n{ \n $END$\n} \n", DLocalize.liveTemplateDescriptionFun())) {
            builder.withReformat();

            builder.withVariable("NAME", "", "\"myfunction\"", true);
            builder.withVariable("PARAMETER1TYPE", "", "\"int\"", true);
            builder.withVariable("PARAMETER1NAME", "", "\"x\"", true);
            builder.withVariable("DPARAMETER1NAME", "PARAMETER1NAME", "", false);
            builder.withVariable("VOID", "", "\"void\"", true);
            builder.withVariable("EX", "", "\"@safe pure nothrow @nogc\"", true);
            builder.withVariable("BRIEFSUMMARY", "", "\"Brief summary of what\"", true);
            builder.withVariable("DNAME", "NAME", "", false);
            builder.withVariable("BRIEFSUMMARY2", "", "\"does, forming the summary section\"", true);
            builder.withVariable("Description", "", "\"First paragraph of synopsis description.\"", true);
            builder.withVariable("DVOID", "VOID", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dThis", "this", "/// Description \nthis($PARAMETER1TYPE$ $PARAMETER1NAME$) $EX$\n{ \n    $END$\n}\n", DLocalize.liveTemplateDescriptionThis())) {
            builder.withVariable("PARAMETER1TYPE", "", "\"int\"", true);
            builder.withVariable("PARAMETER1NAME", "", "\"x\"", true);
            builder.withVariable("EX", "", "\"@safe pure nothrow @nogc\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dGet", "get", "$TYPE1$ $NAME2$() $ACCESS$ \n{ \n return $FIELD$; \n} \n", DLocalize.liveTemplateDescriptionGet())) {
            builder.withVariable("TYPE1", "", "\"int\"", true);
            builder.withVariable("NAME2", "", "\"getter\"", true);
            builder.withVariable("ACCESS", "", "\"@safe pure nothrow const @nogc\"", true);
            builder.withVariable("FIELD", "concat(NAME2, \"_\")", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dSet", "set", "void $SET$$NAME1$($TYPE2$ rhs) @safe pure nothrow @nogc\n{ \n    $VAR1$ = rhs; \n} \n", DLocalize.liveTemplateDescriptionSet())) {
            builder.withVariable("SET", "", "\"set\"", true);
            builder.withVariable("NAME1", "", "\"Value\"", true);
            builder.withVariable("TYPE2", "concat(expressions...)", "\"int\"", true);
            builder.withVariable("VAR1", "concat(\"_\", camelCase(NAME1))", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dMain", "main", "void main(string[] args) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionMain())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dToStr", "toStr", "string toString() @safe const pure nothrow \n{ \n $END$ \n} \n", DLocalize.liveTemplateDescriptionTostr())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dTry", "try", "try \n{ \n $SELECTION$$2$\n} \ncatch($3$Exception e) \n{ \n $HANDLE_EXCEPTION4$ \n} \n$END$ \n", DLocalize.liveTemplateDescriptionTry())) {
            builder.withVariable("2", "", "", true);
            builder.withVariable("3", "", "", true);
            builder.withVariable("HANDLE_EXCEPTION4", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dTryf", "tryf", "try \n{ \n $SELECTION$$2$ \n} \ncatch($3$Exception e) \n{ \n $HANDLE_EXCEPTION4$ \n} \nfinally \n{ \n $CLEANUP5$ \n} \n", DLocalize.liveTemplateDescriptionTryf())) {
            builder.withVariable("2", "", "", true);
            builder.withVariable("3", "", "", true);
            builder.withVariable("HANDLE_EXCEPTION4", "", "", true);
            builder.withVariable("CLEANUP5", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dCatch", "catch", "catch($EX$ Exception e) \n{ \n $HANDLETHEEXCEPTION0$ \n} \n", DLocalize.liveTemplateDescriptionCatch())) {
            builder.withVariable("EX", "", "", true);
            builder.withVariable("HANDLETHEEXCEPTION0", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dThr", "thr", "throw new $NAME1$Exception($END$); \n", DLocalize.liveTemplateDescriptionThr())) {
            builder.withVariable("NAME1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dStruct", "struct", "struct $typeName$ \n{ \n $END$ \n} \n", DLocalize.liveTemplateDescriptionStruct())) {
            builder.withVariable("typeName", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dUnion", "union", "union $TYPENAME$} \n{ \n $END$ \n} \n", DLocalize.liveTemplateDescriptionUnion())) {
            builder.withVariable("TYPENAME", "fileNameWithoutExtension()", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dClass", "class", "class $CLASSNAME$\n{ \n    this()\n    {\n        $END$ \n    }\n\n    ~this()\n    {\n\n    }\n} \n", DLocalize.liveTemplateDescriptionClass())) {
            builder.withVariable("CLASSNAME", "fileNameWithoutExtension()", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dInter", "inter", "interface $TYPENAME$\n{ \n $END$ \n} \n", DLocalize.liveTemplateDescriptionInter())) {
            builder.withVariable("TYPENAME", "fileNameWithoutExtension()", "\"typename\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dEnum", "enum", "enum $BASENAME$} \n{ \n $END$ \n} \n", DLocalize.liveTemplateDescriptionEnum())) {
            builder.withVariable("BASENAME", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dExc", "exc", "/// $DNAME1$ $END$Exception. \nclass $NAME1$Exception : $Type$Exception \n{ \n this(string msg, string file = __FILE__, int line = __LINE__) \n  @safe pure nothrow \n { \n  super(msg, file, line); \n } \n} \n", DLocalize.liveTemplateDescriptionExc())) {
            builder.withReformat();

            builder.withVariable("NAME1", "", "\"My\"", true);
            builder.withVariable("Type", "", "", true);
            builder.withVariable("DNAME1", "NAME1", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dMix", "mix", "template $DMIXNAME$(string Name, string M1)\n{\n    const char[] $DMIXNAME$ = $END$\"struct \" ~ Name ~ \"{ int \" ~ M1 ~ \"; }\";\n}\n\nmixin($MIXNAME$!(\"Foo\", \"bar\"));\n", DLocalize.liveTemplateDescriptionMix())) {
            builder.withVariable("MIXNAME", "", "\"GenStruct\"", true);
            builder.withVariable("DMIXNAME", "MIXNAME", "", false);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dTemplate", "template", "$mixin$template $NAME2$($PARAMETERS3$) \n{ \n    $END$ \n} \n", DLocalize.liveTemplateDescriptionTemplate())) {
            builder.withVariable("mixin", "", "\"mixin \"", true);
            builder.withVariable("NAME2", "", "\"TemplateName\"", true);
            builder.withVariable("PARAMETERS3", "", "\"alias a\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dVersion", "version", "version($VERSION_NAME1$) \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionVersion())) {
            builder.withVariable("VERSION_NAME1", "", "", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dDebug", "debug", "debug \n{ \n $SELECTION$$END$ \n} \n", DLocalize.liveTemplateDescriptionDebug())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dPragma", "pragma", "pragma($IDENT$, \"$ARG$\"); \n$END$ \n", DLocalize.liveTemplateDescriptionPragma())) {
            builder.withVariable("IDENT", "enum(\"msg\", \"lib\", \"startaddress\", \"mangle\", \"linkerDirective\")", "\"msg\"", true);
            builder.withVariable("ARG", "", "\"Compiling...\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dOpDis", "opDis", "$auto$ opDispatch(string $name$, $T$)($T$ $rhs$)\n{ \n    $END$return mixin(\"value.\" ~ $name$ ~ \" = $rhs$\");\n} \n", DLocalize.liveTemplateDescriptionOpdis())) {
            builder.withVariable("auto", "", "\"auto\"", true);
            builder.withVariable("name", "", "\"name\"", true);
            builder.withVariable("T", "", "\"T\"", true);
            builder.withVariable("rhs", "", "\"rhs\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dOpCmp", "opCmp", "int opCmp($TYPE1$ rhs) @safe pure nothrow const\n{ \n    $END$ \n} \n", DLocalize.liveTemplateDescriptionOpcmp())) {
            builder.withVariable("TYPE1", "", "\"int\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dOpApply", "opApply", "int opApply(int delegate($Type$) $dg$) \n{ \n    $END$int result = 0;\n    if ($dg$(this))\n        result = 1;\n    return result;\n} \n", DLocalize.liveTemplateDescriptionOpapply())) {
            builder.withVariable("Type", "className()", "\"className\"", true);
            builder.withVariable("dg", "", "\"dg\"", true);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dTodo", "todo", "// TODO: $END$. $DATE$ $TIME$ \n", DLocalize.liveTemplateDescriptionTodo())) {
            builder.withVariable("DATE", "date()", "", false);
            builder.withVariable("TIME", "time()", "", false);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dGpl", "gpl", "// This program is free software; you can redistribute it and/or modify \n// it under the terms of the GNU General Public License as published by \n// the Free Software Foundation; either version 2 of the License, or \n// (at your option) any later version. \n// \n// This program is distributed in the hope that it will be useful, \n// but WITHOUT ANY WARRANTY; without even the implied warranty of \n// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the \n// GNU General Public License for more details. \n// \n// You should have received a copy of the GNU General Public License \n// along with this program; if not, write to the Free Software \n// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. \n// \n// Copyright (C) $Author$, $DATE$ $TIME$ \n", DLocalize.liveTemplateDescriptionGpl())) {
            builder.withVariable("Author", "user()", "\"Author\"", false);
            builder.withVariable("DATE", "date()", "", false);
            builder.withVariable("TIME", "time()", "", false);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dBsl", "bsl", "//          Copyright $Author$ $DATE$ $TIME$. \n// Distributed under the Boost Software License, Version 1.0. \n//    (See accompanying file LICENSE_1_0.txt or copy at \n//          http://www.boost.org/LICENSE_1_0.txt) \n", DLocalize.liveTemplateDescriptionBsl())) {
            builder.withVariable("Author", "user()", "\"Author\"", false);
            builder.withVariable("DATE", "date()", "", false);
            builder.withVariable("TIME", "time()", "", false);

            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("d~this", "~this", "~this()\n{\n    $END$\n}", DLocalize.liveTemplateDescriptionTildeThis())) {
            builder.withContext(DlangContext.class, true);
        }

        try (Builder builder = factory.newBuilder("dF", "f", "$TYPE$ $IDENT$;", DLocalize.liveTemplateDescriptionF())) {
            builder.withReformat();

            builder.withVariable("IDENT", "", "\"foo\"", true);
            builder.withVariable("TYPE", "", "\"int\"", true);

            builder.withContext(DlangContext.class, true);
        }
    }
}
