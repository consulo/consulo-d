package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.DLanguageTemplateTypeParameter;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageTemplateTypeParameterImpl;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateTypeParameterStub;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class DlangTemplateTypeParameterStubElementType
    extends DNamedStubElementType<DLanguageTemplateTypeParameterStub, DLanguageTemplateTypeParameter> {
    public DlangTemplateTypeParameterStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageTemplateTypeParameter createPsi(DLanguageTemplateTypeParameterStub stub) {
        return new DLanguageTemplateTypeParameterImpl(stub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return DUtil.INSTANCE.definitionNode(node) && super.shouldCreateStub(node);
    }

    @Override
    public DLanguageTemplateTypeParameterStub createStub(DLanguageTemplateTypeParameter psi, StubElement parentStub) {
        return new DLanguageTemplateTypeParameterStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageTemplateTypeParameterStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageTemplateTypeParameterStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageTemplateTypeParameterStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}
