package io.github.intellij.dlanguage.tools;

import consulo.platform.Platform;
import consulo.platform.PlatformOperatingSystem;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.util.lang.SemVer;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samael Bate (singingbush)
 * created on 19/08/18
 */
public final class DtoolUtils {
    public static final DtoolUtils INSTANCE = new DtoolUtils();

    // This list of D Tool versions will need to be manually maintained until we have a web service in place
    public static final String DMD_LATEST = "2.106.0";
    public static final String LDC_LATEST = "1.35.0";

    public static final String SERVE_D_LATEST = "0.7.5";

    public static final String DUB_LATEST = "1.35.0";
    public static final String DSCANNER_LATEST = "v0.15.2"; // v0.16.0-beta.4
    public static final String DCD_LATEST = "v0.15.2"; // v0.9.10, OR v0.9.10-alpha.0
    public static final String DFORMAT_LATEST = "v0.15.1";


    private final List<Path> mySTANDARD_TOOL_PATHS;


    private DtoolUtils() {
        PlatformOperatingSystem os = Platform.current().os();
        if (os.isWindows()) {
            mySTANDARD_TOOL_PATHS = List.of(
                Paths.get("\\D\\dmd2\\windows\\bin")
            );
        }
        else if (os.isMac()) {
            mySTANDARD_TOOL_PATHS = List.of(
                Paths.get("/usr/local/opt") // homebrew
            );
        }
        else if (os.isUnix()) {
            mySTANDARD_TOOL_PATHS = List.of(
                Paths.get("/usr/local/bin"),
                Paths.get("/usr/bin"),
                Paths.get("/snap/bin"), // #575 support snaps
                Paths.get(System.getProperty("user.home") + "/bin")
            );
        }
        else {
            mySTANDARD_TOOL_PATHS = List.of();
        }
    }

    public List<Path> getSTANDARD_TOOL_PATHS() {
        return mySTANDARD_TOOL_PATHS;
    }

    public static boolean versionPredates(@Nullable String receiver, @Nullable String comparison) {
        if (!isSemVer(receiver) && !isSemVer(comparison)) {
            return false;
        }
        SemVer v = toSemVer(receiver);
        if (v == null) {
            return false;
        }
        SemVer it = toSemVer(comparison);
        if (it == null) {
            return false;
        }
        if (it.getMajor() != v.getMajor()) {
            return it.getMajor() > v.getMajor();
        }
        else if (it.getMinor() != v.getMinor()) {
            return it.getMinor() > v.getMinor();
        }
        else {
            return it.getPatch() > v.getPatch();
        }
    }

    public static @Nullable SemVer toSemVer(@Nullable String receiver) {
        if (receiver != null && isSemVer(receiver)) {
            List<Integer> parts = new ArrayList<>();
            for (String part : receiver.split("\\.")) {
                if (part.isEmpty()) {
                    continue;
                }
                String first = null;
                for (String digits : part.split("\\D")) {
                    if (!digits.isEmpty()) {
                        first = digits;
                        break;
                    }
                }
                if (first == null) {
                    throw new java.util.NoSuchElementException("List is empty.");
                }
                parts.add(Integer.parseInt(first));
            }

            return switch (parts.size()) {
                case 1 -> new SemVer(receiver, parts.get(0), 0, 0);
                case 2 -> new SemVer(receiver, parts.get(0), parts.get(1), 0);
                default -> new SemVer(receiver, parts.get(0), parts.get(1), parts.get(2));
            };
        }
        return null;
    }

    public static boolean isSemVer(@Nullable String receiver) {
        if (receiver == null || receiver.isBlank()) {
            return false;
        }
        if (!receiver.contains(".")) {
            return false;
        }
        for (String part : receiver.split("\\.", -1)) {
            if (!part.replaceAll("\\D", "").isBlank()) {
                return true;
            }
        }
        return false;
    }



    /**
     * Tries to get the absolute path for a command by trying standard installation directories (for dub).
     * This is mostly useful for dub, especially on Mac/Linux where various installation methods can differ
     */
    public @Nullable String lookInStandardDirectories(String binaryName) {
        for (Path path : mySTANDARD_TOOL_PATHS) {
            Path toolPath = path.resolve(binaryName);
            if (Files.exists(toolPath) && Files.isExecutable(toolPath)) {
                return toolPath.toAbsolutePath().toString();
            }
        }
        return null;
    }


    public static String getVersionOutput(String cmd) {
        if (StringUtil.isEmptyOrSpaces(cmd) || !Files.isExecutable(Paths.get(cmd)) || Files.isDirectory(Paths.get(cmd))) {
            return "";
        }
        GeneralCommandLine commandLine = new GeneralCommandLine(cmd, "--version");

        try {
            String versionOutput = CapturingProcessUtil.execAndGetOutput(commandLine, 800).getStdout();

            if (StringUtil.isNotEmpty(versionOutput)) {
                return versionOutput.split("\n")[0].trim();
            }
        }
        catch (ExecutionException ignored) {
        }
        return "";
    }
}
