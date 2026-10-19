package io.github.intellij.dlanguage.stubs.index;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;

/**
 * Stub index to store all names defined in the project; specifically for the "go to symbol" feature.
 */
@ExtensionImpl
public class DAllNameIndex extends StringStubIndexExtension<DNamedElement> {
    public static final StubIndexKey<String, DNamedElement> KEY = StubIndexKey.createIndexKey("d.all.name");
    public static final int VERSION = 1;

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @Override
    public StubIndexKey<String, DNamedElement> getKey() {
        return KEY;
    }
}

