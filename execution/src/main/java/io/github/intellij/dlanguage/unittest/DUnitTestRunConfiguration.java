package io.github.intellij.dlanguage.unittest;

import consulo.d.localize.DLocalize;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.LocatableConfigurationBase;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.DlangFileType;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Consulo: the configuration belongs to the registered factory of {@link DUnitTestRunConfigurationType} (upstream
 * created a new factory for every configuration), and its fields are written as upstream's {@code XmlSerializer}
 * wrote them ({@code <option name=".." value=".."/>}, the environment as a map).
 */
public class DUnitTestRunConfiguration extends LocatableConfigurationBase {

    private static final String OPTION = "option";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String MAP = "map";
    private static final String ENTRY = "entry";
    private static final String KEY = "key";

    public @Nullable String getdFilePath() {
        return dFilePath;
    }

    public void setdFilePath(final @Nullable String dFilePath) {
        this.dFilePath = dFilePath;
    }

    public @Nullable String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(final @Nullable String workingDir) {
        this.workingDir = workingDir;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(final Map<String, String> envVars) {
        this.envVars = envVars;
    }

    private @Nullable String dFilePath;
    private @Nullable String workingDir;
    private Map<String, String> envVars;

    public DUnitTestRunConfiguration(final Project project) {
        this(DLanguage.INSTANCE.getDisplayName().get(), project);
    }

    public DUnitTestRunConfiguration(String name, Project project) {
        super(project, DUnitTestRunConfigurationType.getInstance().getConfigurationFactories()[0], name);
        envVars = new HashMap<>();
    }

    @Override
    public void readExternal(final Element element) throws InvalidDataException {
        super.readExternal(element);
        dFilePath = readOption(element, "dFilePath");
        workingDir = readOption(element, "workingDir");
        final Map<String, String> envVars = new HashMap<>();
        for (final Element option : element.getChildren(OPTION)) {
            if ("envVars".equals(option.getAttributeValue(NAME))) {
                final Element map = option.getChild(MAP);
                if (map != null) {
                    for (final Element entry : map.getChildren(ENTRY)) {
                        final String key = entry.getAttributeValue(KEY);
                        if (key != null) {
                            envVars.put(key, StringUtil.notNullize(entry.getAttributeValue(VALUE)));
                        }
                    }
                }
            }
        }
        this.envVars = envVars;
    }

    @Override
    public void writeExternal(final Element element) throws WriteExternalException {
        super.writeExternal(element);
        writeOption(element, "dFilePath", dFilePath);
        writeOption(element, "workingDir", workingDir);
        final Element map = new Element(MAP);
        for (final Map.Entry<String, String> entry : envVars.entrySet()) {
            map.addContent(new Element(ENTRY).setAttribute(KEY, entry.getKey()).setAttribute(VALUE, entry.getValue()));
        }
        element.addContent(new Element(OPTION).setAttribute(NAME, "envVars").addContent(map));
    }

    private static void writeOption(final Element element, final String name, final @Nullable String value) {
        if (value != null) {
            element.addContent(new Element(OPTION).setAttribute(NAME, name).setAttribute(VALUE, value));
        }
    }

    private static @Nullable String readOption(final Element element, final String name) {
        for (final Element option : element.getChildren(OPTION)) {
            if (name.equals(option.getAttributeValue(NAME))) {
                return option.getAttributeValue(VALUE);
            }
        }
        return null;
    }

    @Override
    public SettingsEditor<DUnitTestRunConfiguration> getConfigurationEditor() {
        return new DUnitTestRunConfigurationEditor(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException
    {
        // TODO: Fill this in
    }

    @Override
    public RunProfileState getState(final Executor executor, final ExecutionEnvironment environment) throws ExecutionException {
        return new DUnitTestRunProfileState(environment);
    }

    public VirtualFile getDFile() throws RuntimeConfigurationError {
        final String filePath = getdFilePath();
        if (StringUtil.isEmptyOrSpaces(filePath)) {
            throw new RuntimeConfigurationError(DLocalize.pathToDlanguageFileNotSet());
        }

        final VirtualFile dFile = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (dFile == null || dFile.isDirectory()) {
            throw new RuntimeConfigurationError(DLocalize.dlanguageFileNotFound(FileUtil.toSystemDependentName(filePath)));
        }

        if (dFile.getFileType() != DlangFileType.INSTANCE) {
            throw new RuntimeConfigurationError(DLocalize.notADlanguageFile(FileUtil.toSystemDependentName(filePath)));
        }

        return dFile;
    }

    @Override
    public @Nullable String suggestedName() {
        final String filePath = getdFilePath();
        return filePath == null ? null : PathUtil.getFileName(filePath);
    }
//
//    public DUnitTestRunConfiguration clone() {
//        final DUnitTestRunConfiguration clone = (DUnitTestRunConfiguration)super.clone();
//        clone.myRunnerParameters = myRunnerParameters.clone();
//        return clone;
//    }
}
