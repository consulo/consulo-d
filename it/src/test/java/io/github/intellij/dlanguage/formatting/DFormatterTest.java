package io.github.intellij.dlanguage.formatting;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.application.internal.TransferredWriteActionService;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.undoRedo.CommandProcessor;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DlangFileType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code DFormatterTest.kt}, an IntelliJ {@code FormatterTestCase}: the text before is written to a
 * file of the headless project, reformatted in a write command (on the UI thread, as document changes must be) and
 * compared with the text after, as {@code FormatterTestCase.checkDocument} does.
 */
@Disabled("consulo-it cannot format: CodeStyleInternalHelper (implemented in ide-impl) is not bound, and HeadlessCommandProcessor "
    + "overrides neither getCurrentCommandNameValue() nor getCurrentCommandName(), which call each other, while every formatter run "
    + "asks for it (FormatterUtil.isFormatterCalledExplicitly): StackOverflowError")
@ExtendWith(HeadlessProjectExtension.class)
public class DFormatterTest {
    private String getBasePath() {
        return "formatting";
    }

    private String getTestDataPath() {
        return "gold";
    }

    private String getFileExtension() {
        return DlangFileType.DEFAULT_EXTENSION;
    }

    private void doTest(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        doTest(fixture, testName + "." + getFileExtension(), testName + "-after." + getFileExtension());
    }

    private void doTest(CodeInsightTestFixture fixture, String fileNameBefore, String fileNameAfter) throws Exception {
        doTextTest(fixture, loadFile(fileNameBefore), loadFile(fileNameAfter));
    }

//    @Test
//    public void testFormattingFunctionOpeningBrace(CodeInsightTestFixture fixture) throws Exception {
//        doTextTest(fixture, "void main(){\n}", "void main() {\n}");
//    }

    @Test
    public void testFormattingFunctionClosingBrace(CodeInsightTestFixture fixture) throws Exception {
        doTextTest(fixture, "void main() {\n  }", "void main() {\n}");
    }

    @Test
    public void testFormattingLinesWithinFunction(CodeInsightTestFixture fixture) throws Exception {
        doTextTest(fixture, "void main() {\n    foo();\n  bar();\n}", "void main() {\n    foo();\n    bar();\n}");
    }

    @Test
    public void testFormattingImport(CodeInsightTestFixture fixture) throws Exception {
        doTextTest(fixture, "import std.stdio:stderr,writeln;", "import std.stdio : stderr, writeln;");
    }

    @Test
    public void testMultilineArrays(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testMultilineFuncArgs(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testCallChaining(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testhello(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testDeclSpacing(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testattribute_constraint(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testdlanguage_issue_497(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

//
//    @Test
//    public void testbreakOnDots(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testcatchExceptionNested(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testcomments(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testcontracts(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testfrontpage(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testguessnumber(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testhigherorder(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0017(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0018(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0021(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0022(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0023(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0024(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0025(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0026(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0027(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0028(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0029(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0030(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0031(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0032(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0033(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0034(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0035(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0037(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0038(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0039(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0041(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0042(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0043(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0044(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0045(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0046(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0047(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0048(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0049(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0050(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0051(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0052(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0053(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0054(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0056(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0057(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0058(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0059(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0060(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0061(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0062(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0063(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0064(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0065(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0066(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0067(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0068(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0069(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0070(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0073(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0074(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0076(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0079(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0080(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0081(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0082(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0083(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0085(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0086(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0088(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0089(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0090(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0091(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0092(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0093(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0094(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0095(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0096(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0097(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0098(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0099(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0100(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0101(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0102(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0106(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0107(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0108(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0109(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0111(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0112(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0114(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0116(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0117(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0118(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0119(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0120(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0123(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0125(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0126(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0127(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0128(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0130(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0136(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0138(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0139(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0140(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0142(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0146(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0147(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0148(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0150(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0151(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0152(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0153(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0154(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0155(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0156(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0158(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0162(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0166(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0169(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0172(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0174(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0177(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0185(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0186(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0187(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0189(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0190(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0194(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0204(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0205(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0206(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0207(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0208(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0209(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0210(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0212(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0213(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0215a(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0215b(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0215c(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0215d(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0216(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0219(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0220(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0221(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0222(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0223(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0224(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0225(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0226(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0229(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0241(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0251(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testissue0273(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testlongParamList(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testminimizeLength(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testmultiline_string(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testparenIndent(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testpropertySpacing(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testswap(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }
//
//    @Test
//    public void testwrapping1(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
//        doTest(fixture, testInfo);
//    }

    /**
     * {@code FormatterTestCase.doTextTest} and {@code checkDocument}.
     */
    private void doTextTest(CodeInsightTestFixture fixture, String text, String textAfter) throws Exception {
        String fileName = "before." + getFileExtension();
        PsiFile file = fixture.addFileToProject(CodeInsightTestFixture.SOURCE_ROOT + "/" + fileName, text);
        Project project = fixture.getProject();
        Document document = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(file.getVirtualFile()));
        assertNotNull(document);
        assertEquals(ReadAction.compute(file::getText), ReadAction.compute(document::getText));

        WriteAction.run(() -> Application.get().getInstance(TransferredWriteActionService.class).runOnEdtWithTransferredWriteActionAndWait(
            () -> {
                CommandProcessor.getInstance().newCommand().project(project).run(() -> CodeStyleManager.getInstance(project)
                    .reformatText(file, file.getTextRange().getStartOffset(), file.getTextRange().getEndOffset()));
                PsiDocumentManager.getInstance(project).commitDocument(document);
            }));

        assertEquals(textAfter, ReadAction.compute(document::getText));
        assertEquals(textAfter, ReadAction.compute(file::getText));
    }

    private String loadFile(String name) throws Exception {
        String fullName = getTestDataPath() + "/" + getBasePath() + "/" + name;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(fullName)) {
            assertNotNull(stream, fullName);
            return StringUtil.convertLineSeparators(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * IntelliJ's {@code getTestName(false)}: the method name without {@code test}, case kept.
     */
    private static String getTestName(TestInfo testInfo) {
        String name = testInfo.getTestMethod().orElseThrow().getName();
        return name.startsWith("test") ? name.substring("test".length()) : name;
    }
}
