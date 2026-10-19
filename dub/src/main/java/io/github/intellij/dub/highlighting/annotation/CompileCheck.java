package io.github.intellij.dub.highlighting.annotation;

import consulo.document.util.TextRange;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.cmd.GeneralCommandLine;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.highlighting.annotation.DProblem;
import io.github.intellij.dlanguage.highlighting.annotation.external.DlangLinter;
import io.github.intellij.dlanguage.highlighting.annotation.external.LinterHelper;
import io.github.intellij.dub.service.DubBinaryPathProvider;
import io.github.intellij.dub.tools.DubProcessListener;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses dub (with the default compiler) to check for compile errors periodically
 */
public class CompileCheck implements DlangLinter {
    private static final Logger LOG = Logger.getInstance(CompileCheck.class);

    @Override
    public DProblem[] checkFileSyntax(PsiFile file) {
        String dubPath = DubBinaryPathProvider.INSTANCE.getDubPath();
        if (!DubBinaryPathProvider.INSTANCE.isDubAvailable() || dubPath == null) {
            return new DProblem[0];
        }
        String result = processFile(file, dubPath);
        return StringUtil.isNotEmpty(result) ? findProblems(result, file).toArray(new DProblem[0]) : new DProblem[0];
    }

    private String processFile(PsiFile file, String dubPath) {
        GeneralCommandLine cmd = new GeneralCommandLine()
            .withWorkDirectory(file.getProject().getBasePath())
            .withExePath(dubPath)
            .withEnvironment("DFLAGS", "-o-")
            .withParameters("build", "--combined", "-q");
        try {
            DubProcessListener listener = new DubProcessListener();
            ProcessHandler process = ProcessHandlerBuilder.create(cmd).build();
            process.addProcessListener(listener);
            process.startNotify();
            process.waitFor();
            return listener.getOutput();
        }
        catch (ExecutionException e) {
            LOG.warn("There was a problem running 'dub build --combined -q'", e);
        }
        return "";
    }

    private List<DProblem> findProblems(String stdout, PsiFile file) {
        List<String> lints = StringUtil.split(stdout, "\n");
        List<DProblem> problems = new ArrayList<>();
        for (String lint : lints) {
            ContainerUtil.addIfNotNull(problems, parseProblem(lint, file));
        }
        return problems;
    }

    private @Nullable CompilerProblem parseProblem(String lint, PsiFile file) {
        LOG.debug(lint);

        // Example DUB error:
        // src/hello.d(3,1): Error: only one main allowed
        Pattern p = Pattern.compile("([\\w\\\\/]+\\.d)\\((\\d+),(\\d+)\\):\\s(\\w+):(.+)");
        Matcher m = p.matcher(lint);
        String sourceFile = "";
        String message = "";
        int line = 0;
        int column = 0;
        String severity = "";
        boolean hasMatch = false;
        while (m.find()) {
            hasMatch = true;
            sourceFile = m.group(1);
            line = Integer.parseInt(m.group(2));
            column = Integer.parseInt(m.group(3));
            severity = m.group(4);
            message = m.group(5);
        }
        if (hasMatch && isSameFile(file, sourceFile)) {
            TextRange range = LinterHelper.calculateTextRange(file, line, column);
            if (range != null) {
                return new CompilerProblem(range, message, severity);
            }
        }
        return null;
    }

    private boolean isSameFile(PsiFile file, String relativeOtherFilePath) {
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }
        String filePath = virtualFile.getPath();
        String unixRelativeOtherFilePath = relativeOtherFilePath.replace('\\', '/');
        return filePath.endsWith(unixRelativeOtherFilePath);
    }

    public static class CompilerProblem implements DProblem {
        private final String mySeverity;
        private final TextRange myRange;
        private final String myMessage;

        public CompilerProblem(TextRange range, String message, String severity) {
            myRange = range;
            mySeverity = severity;
            myMessage = message;
        }

        public String getSeverity() {
            return mySeverity;
        }

        public TextRange getRange() {
            return myRange;
        }

        public String getMessage() {
            return myMessage;
        }

        @Override
        public void createAnnotations(PsiFile file, AnnotationHolder holder) {
            if (mySeverity.equals("Error")) {
                holder.newAnnotation(HighlightSeverity.ERROR, LocalizeValue.of(myMessage)).range(myRange).create();
            }
            else {
                holder.newAnnotation(HighlightSeverity.WARNING, LocalizeValue.of(myMessage)).range(myRange).create();
            }
        }
    }
}
