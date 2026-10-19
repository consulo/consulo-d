package io.github.intellij.dub.tools;

import consulo.process.ProcessOutputTypes;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samael Bate (singingbush)
 * created on 06/02/18
 */
public class DubProcessListener implements ProcessListener {
    // use StringBuffer because StringBuilder is not thread safe
    private final StringBuffer myOutput = new StringBuffer(); // all output from stdout and stderr
    private final StringBuffer myStdout = new StringBuffer();
    private final List<String> myErrors = new ArrayList<>();

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        if (outputType == ProcessOutputTypes.STDOUT) {
            myStdout.append(event.getText());
        }
        else if (outputType == ProcessOutputTypes.STDERR) {
            String text = event.getText();
            myErrors.add(text.endsWith("\n") ? text.substring(0, text.length() - 1) : text);
        }
        //ProcessOutputTypes.SYSTEM
        myOutput.append(event.getText()); // everything gets add to the general output
    }

    public String getOutput() {
        return myOutput.toString();
    }

    public String getStdOut() {
        return myStdout.toString();
    }

    public BufferedReader getStdOutReader() {
        return new BufferedReader(new StringReader(getStdOut()));
    }

    public List<String> getErrors() {
        return myErrors;
    }
}
