package io.github.intellij.dlanguage.quickfix;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.application.internal.TransferredWriteActionService;
import consulo.document.Document;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.undoRedo.CommandProcessor;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of upstream {@code DQuickFixTestBase.kt}, with the parts of IntelliJ's {@code LightQuickFixTestCase} its tests
 * use: {@link #doTest} checks the highlighting marked up in a test data file, {@link #doSingleTest} applies the fix
 * named in the first line of {@code before<name>} ({@code // "<fix text>" "<available>"}) at the caret and compares
 * the result with {@code after<name>}.
 */
@ExtendWith(HeadlessProjectExtension.class)
public abstract class DQuickFixTestBase {
    private static final Pattern ACTION_HINT = Pattern.compile("^// \"(.*)\" \"(true|false)\"");

    protected String getTestDataPath() {
        return "gold/quickfix/";
    }

    protected String loadFile(String fileName) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(getTestDataPath() + fileName)) {
            assertNotNull(stream, getTestDataPath() + fileName + " is missing");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected void doTest(CodeInsightTestFixture fixture, String fileName) throws Exception {
        fixture.configureByText(fileName, loadFile(fileName));
        fixture.checkHighlighting();
    }

    protected void doSingleTest(CodeInsightTestFixture fixture, String fileSuffix) throws Exception {
        String before = loadFile("before" + fileSuffix);
        Matcher hint = ACTION_HINT.matcher(before);
        assertTrue(hint.find(), "before" + fileSuffix + " does not start with // \"<fix text>\" \"<available>\"");
        String actionText = hint.group(1);
        boolean available = Boolean.parseBoolean(hint.group(2));

        PsiFile file = fixture.configureByText("before" + fileSuffix, before);
        int caret = fixture.getExpectedHighlightingData().getCaretOffset();

        List<IntentionAction> actions = new ArrayList<>();
        for (HighlightInfo info : fixture.doHighlighting()) {
            info.forEachQuickFix((action, range) -> {
                if (range.getStartOffset() <= caret && caret <= range.getEndOffset()
                    && actionText.equals(ReadAction.compute(() -> action.getText().get()))) {
                    actions.add(action);
                }
            });
        }

        if (!available) {
            assertTrue(actions.isEmpty(), "\"" + actionText + "\" is not expected at the caret");
            return;
        }
        assertEquals(1, actions.size(), "\"" + actionText + "\" is expected once at the caret, found " + actions);

        IntentionAction action = actions.get(0);
        Document document = fixture.getDocument();
        // write authority taken off the UI thread, the change itself made on it (document listeners assert that)
        WriteAction.run(() -> Application.get().getInstance(TransferredWriteActionService.class).runOnEdtWithTransferredWriteActionAndWait(
            () -> CommandProcessor.getInstance().newCommand()
                .project(fixture.getProject())
                .document(document)
                .run(() -> {
                    action.invoke(fixture.getProject(), null, file);
                    PsiDocumentManager.getInstance(fixture.getProject()).doPostponedOperationsAndUnblockDocument(document);
                })
        ));
        assertEquals(loadFile("after" + fileSuffix), ReadAction.compute(document::getText));
    }
}
