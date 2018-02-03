package io.github.intellij.dlanguage.stubs;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import io.github.intellij.dlanguage.psi.DLanguageSharedStaticDestructor;

/**
 * Created by francis on 1/14/2017.
 */
public class DlangSharedStaticDestructorStub extends StubBase<DLanguageSharedStaticDestructor> implements StubElement<DLanguageSharedStaticDestructor> {
    public DlangSharedStaticDestructorStub(final StubElement parent, @NotNull final IStubElementType elementType) {
        super(parent, elementType);
    }
}
