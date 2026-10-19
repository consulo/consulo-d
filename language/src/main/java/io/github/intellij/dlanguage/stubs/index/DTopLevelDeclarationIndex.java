package io.github.intellij.dlanguage.stubs.index;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.NamedStubBase;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.stub.StubIndexKey;
import consulo.project.Project;
import io.github.intellij.dlanguage.index.DModuleIndex;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;

import java.util.LinkedHashSet;
import java.util.Set;

@ExtensionImpl
public class DTopLevelDeclarationIndex extends StringStubIndexExtension<DNamedElement> {

    public static final StubIndexKey<String, DNamedElement> KEY = StubIndexKey.createIndexKey("d.globally.accessible.name");
    public static final int VERSION = 9;

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @Override
    public StubIndexKey<String, DNamedElement> getKey() {
        return KEY;
    }

    public static <S extends NamedStubBase<T>, T extends DNamedElement> void indexTopLevelDeclarations(S stub, IndexSink sink, String name) {
        if (StubIndexUtilKt.topLevelDeclaration(stub)) {
            sink.occurrence(KEY, name);
        }
    }

    @SuppressWarnings("deprecation")
    public static Set<DNamedElement> getTopLevelSymbols(String name, String module, Project project) {
        if (module.isBlank()) {
            return new LinkedHashSet<>();
        }
        Set<DNamedElement> elements = new LinkedHashSet<>();
        for (DlangPsiFile file : DModuleIndex.getFilesByModuleName(project, module, GlobalSearchScope.allScope(project))) {
            elements.addAll(StubIndex.getElements(KEY, name, project, GlobalSearchScope.fileScope(file), DNamedElement.class));
        }
        return elements;
    }
}
