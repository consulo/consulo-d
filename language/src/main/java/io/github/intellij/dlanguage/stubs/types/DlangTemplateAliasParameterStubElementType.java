package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.DLanguageTemplateAliasParameter;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageTemplateAliasParameterImpl;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateAliasParameterStub;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class DlangTemplateAliasParameterStubElementType
    extends DNamedStubElementType<DLanguageTemplateAliasParameterStub, DLanguageTemplateAliasParameter> {
    public DlangTemplateAliasParameterStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageTemplateAliasParameter createPsi(DLanguageTemplateAliasParameterStub stub) {
        return new DLanguageTemplateAliasParameterImpl(stub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return DUtil.INSTANCE.definitionNode(node) && super.shouldCreateStub(node);
    }

    @Override
    public DLanguageTemplateAliasParameterStub createStub(DLanguageTemplateAliasParameter psi, StubElement parentStub) {
        return new DLanguageTemplateAliasParameterStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageTemplateAliasParameterStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageTemplateAliasParameterStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub)
        throws IOException {
        return new DLanguageTemplateAliasParameterStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}
