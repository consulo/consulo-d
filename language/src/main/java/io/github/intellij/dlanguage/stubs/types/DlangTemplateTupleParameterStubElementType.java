package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.DLanguageTemplateTupleParameter;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageTemplateTupleParameterImpl;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateTupleParameterStub;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class DlangTemplateTupleParameterStubElementType
    extends DNamedStubElementType<DLanguageTemplateTupleParameterStub, DLanguageTemplateTupleParameter> {
    public DlangTemplateTupleParameterStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageTemplateTupleParameter createPsi(DLanguageTemplateTupleParameterStub stub) {
        return new DLanguageTemplateTupleParameterImpl(stub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return DUtil.INSTANCE.definitionNode(node) && super.shouldCreateStub(node);
    }

    @Override
    public DLanguageTemplateTupleParameterStub createStub(DLanguageTemplateTupleParameter psi, StubElement parentStub) {
        return new DLanguageTemplateTupleParameterStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageTemplateTupleParameterStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageTemplateTupleParameterStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub)
        throws IOException {
        return new DLanguageTemplateTupleParameterStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}
