package consulo.d.dub.project;

import io.github.intellij.dub.project.DubProject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The D version identifiers behind the {@code platform}, {@code architecture} and {@code compiler} that
 * {@code dub describe} reports. DUB gets those by compiling a probe with the real compiler, so they are what the
 * compiler predefines; names DUB knows but D does not are skipped.
 */
public final class DubPredefinedVersions {
    private static final Map<String, String> PLATFORMS = Map.ofEntries(
        Map.entry("windows", "Windows"),
        Map.entry("linux", "linux"),
        Map.entry("posix", "Posix"),
        Map.entry("osx", "OSX"),
        Map.entry("ios", "iOS"),
        Map.entry("tvos", "TVOS"),
        Map.entry("watchos", "WatchOS"),
        Map.entry("freebsd", "FreeBSD"),
        Map.entry("openbsd", "OpenBSD"),
        Map.entry("netbsd", "NetBSD"),
        Map.entry("dragonflybsd", "DragonFlyBSD"),
        Map.entry("bsd", "BSD"),
        Map.entry("solaris", "Solaris"),
        Map.entry("aix", "AIX"),
        Map.entry("haiku", "Haiku"),
        Map.entry("android", "Android"),
        Map.entry("cygwin", "Cygwin"),
        Map.entry("mingw", "MinGW"),
        Map.entry("emscripten", "Emscripten")
    );

    private static final Map<String, String> ARCHITECTURES = Map.ofEntries(
        Map.entry("x86", "X86"),
        Map.entry("x86_64", "X86_64"),
        Map.entry("arm", "ARM"),
        Map.entry("aarch64", "AArch64"),
        Map.entry("ppc", "PPC"),
        Map.entry("ppc64", "PPC64"),
        Map.entry("mips32", "MIPS32"),
        Map.entry("mips64", "MIPS64"),
        Map.entry("sparc", "SPARC"),
        Map.entry("sparc64", "SPARC64"),
        Map.entry("s390", "S390"),
        Map.entry("systemz", "SystemZ"),
        Map.entry("hppa", "HPPA"),
        Map.entry("hppa64", "HPPA64"),
        Map.entry("sh", "SH"),
        Map.entry("alpha", "Alpha"),
        Map.entry("wasm", "WebAssembly"),
        Map.entry("riscv32", "RISCV32"),
        Map.entry("riscv64", "RISCV64"),
        Map.entry("loongarch64", "LoongArch64")
    );

    private static final Map<String, String> COMPILERS = Map.of(
        "dmd", "DigitalMars",
        "ldc", "LDC",
        "gdc", "GNU",
        "sdc", "SDC"
    );

    private DubPredefinedVersions() {
    }

    public static List<String> of(DubProject project) {
        return of(project.platform(), project.architecture(), project.compiler());
    }

    public static List<String> of(List<String> platforms, List<String> architectures, String compiler) {
        List<String> result = new ArrayList<>();
        map(platforms, PLATFORMS, result);
        map(architectures, ARCHITECTURES, result);
        map(List.of(compiler), COMPILERS, result);
        return result;
    }

    private static void map(List<String> names, Map<String, String> table, List<String> result) {
        for (String name : names) {
            String identifier = table.get(name);
            if (identifier != null && !result.contains(identifier)) {
                result.add(identifier);
            }
        }
    }
}
