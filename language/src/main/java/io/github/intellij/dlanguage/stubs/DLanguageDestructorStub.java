package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.named.DLanguageDestructor;
import org.jspecify.annotations.Nullable;

/**
 * Created by franc on 1/14/2017.
 */
public class DLanguageDestructorStub extends StubBase<DLanguageDestructor> implements
    StubElement<DLanguageDestructor> {

    public DLanguageDestructorStub(final @Nullable StubElement parent, final IStubElementType elementType) {
        super(parent, elementType);
    }
}
