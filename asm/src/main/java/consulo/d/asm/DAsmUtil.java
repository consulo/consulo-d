package consulo.d.asm;

import consulo.annotation.access.RequiredReadAction;
import consulo.asm.language.impl.psi.AsmLabelElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.project.Project;
import io.github.intellij.dlanguage.psi.DLanguageMissingFunctionBody;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.index.DAllNameIndex;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An {@code extern} D function declared without a body ({@code extern (C) int add(int, int);}) can be implemented by an
 * assembly label of the same name; targets that prefix C symbols with an underscore (macOS, 32-bit Windows) name it
 * {@code _add}.
 */
public final class DAsmUtil {
    private DAsmUtil() {
    }

    @RequiredReadAction
    public static boolean isExternWithoutBody(DLanguageFunctionDeclaration function) {
        DAttributes attributes = function.getAttributes();
        if (attributes == null || !attributes.getExtern()) {
            return false;
        }
        return function.getFunctionBody() == null || function.getFunctionBody() instanceof DLanguageMissingFunctionBody;
    }

    @RequiredReadAction
    public static List<AsmLabelElement> findAsmImplementations(DLanguageFunctionDeclaration function) {
        String name = function.getName();
        if (name == null || name.isEmpty()) {
            return List.of();
        }
        Project project = function.getProject();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        List<AsmLabelElement> result = new ArrayList<>(DAsmSymbolIndex.findLabels(project, name, scope));
        result.addAll(DAsmSymbolIndex.findLabels(project, "_" + name, scope));
        return result;
    }

    /**
     * The {@code extern} D functions a label implements. Whether they have a body is not checked: that would parse their
     * files, and a D body and an assembly label of one name would be a duplicate symbol anyway.
     */
    @RequiredReadAction
    public static List<DLanguageFunctionDeclaration> findDDeclarations(AsmLabelElement label) {
        String name = label.getName();
        Set<String> names = new LinkedHashSet<>();
        names.add(name);
        if (name.length() > 1 && name.charAt(0) == '_') {
            names.add(name.substring(1));
        }
        Project project = label.getProject();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        List<DLanguageFunctionDeclaration> result = new ArrayList<>();
        for (String candidate : names) {
            for (DNamedElement element : StubIndex.getElements(DAllNameIndex.KEY, candidate, project, scope, DNamedElement.class)) {
                if (element instanceof DLanguageFunctionDeclaration function) {
                    DAttributes attributes = function.getAttributes();
                    if (attributes != null && attributes.getExtern()) {
                        result.add(function);
                    }
                }
            }
        }
        return result;
    }
}
