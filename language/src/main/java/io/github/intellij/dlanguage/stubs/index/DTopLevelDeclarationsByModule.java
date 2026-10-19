package io.github.intellij.dlanguage.stubs.index;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.NamedStubBase;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.stub.StubIndexKey;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.HasMembers;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static io.github.intellij.dlanguage.stubs.index.DTopLevelDeclarationIndex.getTopLevelSymbols;

/**
 * Created by francis on 6/17/2017.
 */
@ExtensionImpl
public class DTopLevelDeclarationsByModule extends StringStubIndexExtension<DNamedElement> {
    public static final StubIndexKey<String, DNamedElement> KEY = StubIndexKey.createIndexKey("d.globally.accessible.module");
    public static final int VERSION = 5;

    @Override
    public StubIndexKey<String, DNamedElement> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    public static <S extends NamedStubBase<T>, T extends DNamedElement> void indexTopLevelDeclarationsByModule(S stub, IndexSink sink) {
        if (StubIndexUtilKt.topLevelDeclaration(stub)) {
            T psi = Objects.requireNonNull(stub.getPsi());
            String fileName = ((DlangPsiFile) Objects.requireNonNull(psi.getContainingFile())).getFullyQualifiedModuleName();
            sink.occurrence(KEY, fileName);
        }
    }

    //todo better name/stop repeating type signature?
    public static Set<DNamedElement> getSymbolsFromImport(DLanguageSingleImport anImport) {
        if (anImport.getApplicableImportBinds().size() == 0) {
            return new LinkedHashSet<>(StubIndex.getElements(KEY, anImport.getImportedModuleName(), anImport.getProject(), GlobalSearchScope.allScope(anImport.getProject()), DNamedElement.class));
        }
        Set<DNamedElement> symbols = new LinkedHashSet<>();
        for (String bind : anImport.getApplicableImportBinds()) {
            for (DNamedElement resolvedBind : getTopLevelSymbols(bind, anImport.getImportedModuleName(), anImport.getProject())) {
                symbols.add(resolvedBind);
                if (resolvedBind instanceof HasMembers<?> hasMembers) {
                    for (NamedStubBase member : hasMembers.getMembers()) {
                        symbols.add((DNamedElement) Objects.requireNonNull(member.getPsi()));
                    }
                }
            }
        }
        return symbols;
    }
}
