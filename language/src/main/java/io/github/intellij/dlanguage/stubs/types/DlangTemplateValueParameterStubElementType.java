package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.DLanguageTemplateValueParameter;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageTemplateValueParameterImpl;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateValueParameterStub;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class DlangTemplateValueParameterStubElementType
    extends DNamedStubElementType<DLanguageTemplateValueParameterStub, DLanguageTemplateValueParameter> {
    public DlangTemplateValueParameterStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageTemplateValueParameter createPsi(DLanguageTemplateValueParameterStub stub) {
        return new DLanguageTemplateValueParameterImpl(stub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return DUtil.INSTANCE.definitionNode(node) && super.shouldCreateStub(node);
    }

    @Override
    public DLanguageTemplateValueParameterStub createStub(DLanguageTemplateValueParameter psi, StubElement parentStub) {
        return new DLanguageTemplateValueParameterStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageTemplateValueParameterStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageTemplateValueParameterStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub)
        throws IOException {
        return new DLanguageTemplateValueParameterStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}
