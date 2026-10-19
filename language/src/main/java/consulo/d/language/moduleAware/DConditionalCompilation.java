package consulo.d.language.moduleAware;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.util.lang.ThreeState;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.resolve.DResolveUtil;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Decides which branches of {@code version(...)} / {@code debug(...)} declarations are compiled, for one file under
 * one set of {@link DCompileOptions}.
 * <p>
 * The answer is three-valued; a condition that cannot be decided keeps both of its branches, which is how upstream
 * treats every condition:
 * <ul>
 * <li>{@code static if} is never evaluated;</li>
 * <li>with no options at all nothing is decided;</li>
 * <li>an identifier that is set - by the options or by a {@code version = X;} / {@code debug = X;} of the file
 * itself - is on;</li>
 * <li>an operating system, architecture or compiler identifier is off when another one of the same kind is set
 * (a DUB import sets those it knows about), and undecided otherwise;</li>
 * <li>any other predefined identifier ({@code D_LP64}, {@code LittleEndian}, ...) is undecided unless set;</li>
 * <li>an identifier of the program's own is off when not set;</li>
 * <li>plain {@code debug} is undecided: it follows the {@code -debug} switch, which the options do not carry.</li>
 * </ul>
 */
public final class DConditionalCompilation {
    private static final Set<String> OPERATING_SYSTEMS = Set.of(
        "Windows", "linux", "OSX", "iOS", "TVOS", "WatchOS", "VisionOS", "FreeBSD", "OpenBSD", "NetBSD", "DragonFlyBSD",
        "BSD", "Solaris", "Posix", "AIX", "Haiku", "SkyOS", "SysV3", "SysV4", "Hurd", "Android", "Emscripten",
        "PlayStation", "PlayStation4", "Cygwin", "MinGW", "FreeStanding"
    );

    private static final Set<String> ARCHITECTURES = Set.of(
        "X86", "X86_64", "ARM", "AArch64", "Epiphany", "PPC", "PPC64", "IA64", "MIPS32", "MIPS64", "SPARC", "SPARC64",
        "S390", "SystemZ", "HPPA", "HPPA64", "SH", "Alpha", "WebAssembly", "RISCV32", "RISCV64", "LoongArch64", "MSP430",
        "NVPTX", "NVPTX64", "AVR"
    );

    private static final Set<String> COMPILERS = Set.of("DigitalMars", "GNU", "LDC", "SDC");

    private final @Nullable DCompileOptions myOptions;
    private final Set<String> myVersions = new HashSet<>();
    private final Set<String> myDebugIdentifiers = new HashSet<>();
    private final Map<ASTNode, ThreeState> myConditions = new HashMap<>();

    private DConditionalCompilation(ASTNode fileNode, @Nullable DCompileOptions options) {
        myOptions = options;
        if (options != null) {
            myVersions.addAll(options.versions());
            myDebugIdentifiers.addAll(options.debugIdentifiers());
            collectSpecifications(fileNode);
        }
    }

    public static DConditionalCompilation of(ASTNode fileNode, @Nullable DCompileOptions options) {
        return new DConditionalCompilation(fileNode, options);
    }

    /**
     * @param conditionalDeclaration a {@code CONDITIONAL_DECLARATION}
     * @param child                  one of its children
     * @return {@code true} when {@code child} belongs to a branch that is not compiled
     */
    public boolean isInactive(ASTNode conditionalDeclaration, ASTNode child) {
        if (myOptions == null) {
            return false;
        }
        IElementType type = child.getElementType();
        if (type == DlangTypes.COMPILE_CONDITION || type == DlangTypes.KW_ELSE) {
            return false;
        }
        ThreeState condition = myConditions.computeIfAbsent(conditionalDeclaration, this::evaluateDeclaration);
        if (condition == ThreeState.UNSURE) {
            return false;
        }
        boolean inElseBranch = isAfterElse(conditionalDeclaration, child);
        return inElseBranch ? condition == ThreeState.YES : condition == ThreeState.NO;
    }

    private ThreeState evaluateDeclaration(ASTNode conditionalDeclaration) {
        ASTNode compileCondition = conditionalDeclaration.findChildByType(DlangTypes.COMPILE_CONDITION);
        return compileCondition == null ? ThreeState.UNSURE : evaluate(compileCondition);
    }

    public ThreeState evaluate(ASTNode compileCondition) {
        ASTNode condition = compileCondition.getFirstChildNode();
        if (condition == null) {
            return ThreeState.UNSURE;
        }
        IElementType type = condition.getElementType();
        if (type == DlangTypes.VERSION_CONDITION) {
            return evaluateVersion(condition);
        }
        if (type == DlangTypes.DEBUG_CONDITION) {
            return evaluateDebug(condition);
        }
        return ThreeState.UNSURE;
    }

    private ThreeState evaluateVersion(ASTNode versionCondition) {
        String identifier = conditionIdentifier(versionCondition);
        if (identifier == null) {
            return ThreeState.UNSURE;
        }
        if (identifier.equals("all")) {
            return ThreeState.YES;
        }
        if (identifier.equals("none")) {
            return ThreeState.NO;
        }
        if (myVersions.contains(identifier)) {
            return ThreeState.YES;
        }
        for (Set<String> group : Set.of(OPERATING_SYSTEMS, ARCHITECTURES, COMPILERS)) {
            if (group.contains(identifier)) {
                return containsAny(myVersions, group) ? ThreeState.NO : ThreeState.UNSURE;
            }
        }
        if (isPredefined(identifier)) {
            return ThreeState.UNSURE;
        }
        return ThreeState.NO;
    }

    private ThreeState evaluateDebug(ASTNode debugCondition) {
        if (debugCondition.findChildByType(DlangTypes.OP_PAR_LEFT) == null) {
            return ThreeState.UNSURE;
        }
        String identifier = conditionIdentifier(debugCondition);
        if (identifier == null) {
            return ThreeState.UNSURE;
        }
        return myDebugIdentifiers.contains(identifier) ? ThreeState.YES : ThreeState.NO;
    }

    private static @Nullable String conditionIdentifier(ASTNode condition) {
        ASTNode id = condition.findChildByType(DlangTypes.ID);
        if (id != null) {
            return id.getText();
        }
        if (condition.findChildByType(DlangTypes.KW_UNITTEST) != null) {
            return "unittest";
        }
        if (condition.findChildByType(DlangTypes.KW_ASSERT) != null) {
            return "assert";
        }
        // version(1) / debug(1): levels, deprecated in D
        return null;
    }

    private static boolean isPredefined(String identifier) {
        return identifier.startsWith("D_")
            || identifier.equals("unittest")
            || identifier.equals("assert")
            || identifier.equals("LittleEndian")
            || identifier.equals("BigEndian")
            || DResolveUtil.INSTANCE.getVersionIdentifiers().contains(identifier);
    }

    private static boolean containsAny(Set<String> values, Set<String> group) {
        for (String value : values) {
            if (group.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAfterElse(ASTNode conditionalDeclaration, ASTNode child) {
        for (ASTNode node = conditionalDeclaration.getFirstChildNode(); node != null && node != child; node = node.getTreeNext()) {
            if (node.getElementType() == DlangTypes.KW_ELSE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Walks the module scope in order, as the compiler does, adding {@code version = X;} / {@code debug = X;} of the
     * branches that are compiled (or may be).
     */
    private void collectSpecifications(ASTNode scope) {
        for (ASTNode node = scope.getFirstChildNode(); node != null; node = node.getTreeNext()) {
            collectSpecification(node);
        }
    }

    private void collectSpecification(ASTNode node) {
        IElementType type = node.getElementType();
        if (type == DlangTypes.VERSION_SPECIFICATION) {
            ASTNode id = node.findChildByType(DlangTypes.ID);
            if (id != null) {
                myVersions.add(id.getText());
            }
        }
        else if (type == DlangTypes.DEBUG_SPECIFICATION) {
            ASTNode id = node.findChildByType(DlangTypes.ID);
            if (id != null) {
                myDebugIdentifiers.add(id.getText());
            }
        }
        else if (type == DlangTypes.CONDITIONAL_DECLARATION) {
            collectActiveBranches(node);
        }
        else if (type == DlangTypes.DECLARATION_BLOCK || type == DlangTypes.ATTRIBUTE_SPECIFIER) {
            collectSpecifications(node);
        }
    }

    private void collectActiveBranches(ASTNode conditionalDeclaration) {
        ASTNode compileCondition = conditionalDeclaration.findChildByType(DlangTypes.COMPILE_CONDITION);
        ThreeState condition = compileCondition == null ? ThreeState.UNSURE : evaluate(compileCondition);
        boolean inElseBranch = false;
        for (ASTNode node = conditionalDeclaration.getFirstChildNode(); node != null; node = node.getTreeNext()) {
            if (node.getElementType() == DlangTypes.KW_ELSE) {
                inElseBranch = true;
                continue;
            }
            boolean inactive = inElseBranch ? condition == ThreeState.YES : condition == ThreeState.NO;
            if (!inactive) {
                collectSpecification(node);
            }
        }
    }
}
