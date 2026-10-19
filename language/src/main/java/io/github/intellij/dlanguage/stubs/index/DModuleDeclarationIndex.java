package io.github.intellij.dlanguage.stubs.index;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.NamedStubBase;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;

/**
 * Created by francis on 6/16/2017.
 */
@ExtensionImpl
public class DModuleDeclarationIndex extends StringStubIndexExtension<DLanguageModuleDeclaration> {
    public static final StubIndexKey<String, DLanguageModuleDeclaration> KEY = StubIndexKey.createIndexKey("d.module");
    public static final int VERSION = 4;

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @Override
    public StubIndexKey<String, DLanguageModuleDeclaration> getKey() {
        return KEY;
    }

    public static <S extends NamedStubBase<?>> void indexModuleDeclarations(S stub, IndexSink sink, String name) {
        // as upstream: a stub is never a PSI element, so nothing is indexed here
        if (stub instanceof DLanguageModuleDeclaration) {
            sink.occurrence(DModuleDeclarationIndex.KEY, name);
        }
    }
}
