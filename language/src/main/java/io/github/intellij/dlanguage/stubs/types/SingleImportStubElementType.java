package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangSingleImportImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageSingleImportStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by francis on 3/15/2017.
 */
public class SingleImportStubElementType extends DNamedStubElementType<DLanguageSingleImportStub, DLanguageSingleImport> {
    public SingleImportStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageSingleImport createPsi(DLanguageSingleImportStub stub) {
        return new DlangSingleImportImpl(stub, this);
    }

    @Override
    public DLanguageSingleImportStub createStub(DLanguageSingleImport psi, @Nullable StubElement parentStub) {
        Set<StringRef> binds = new HashSet<>();
        for (String bind : psi.getApplicableImportBinds()) {
            binds.add(StringRef.fromString(bind));
        }
        Map<StringRef, StringRef> namedBinds = new HashMap<>();
        psi.getApplicableNamedImportBinds().forEach((bindName, bind) -> namedBinds.put(
            StringRef.fromString(bindName),
            StringRef.fromString(bind)
        ));
        PsiElement identifier = psi.getIdentifier();
        return new DLanguageSingleImportStub(
            parentStub, this, psi.getName(),
            psi.getApplicableImportBinds().size(), binds,
            psi.getApplicableNamedImportBinds().size(), namedBinds,
            psi.getImportedModuleName(),
            psi.getIdentifier() != null,
            identifier != null ? identifier.getText() : null,
            psi.getAttributes()
        );
    }

    @Override
    public void serialize(DLanguageSingleImportStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeInt(stub.numBinds());
        for (String s : stub.getBinds()) {
            dataStream.writeName(s);
        }
        dataStream.writeInt(stub.numNamedBinds());
        for (Map.Entry<String, String> entry : stub.getNamedBinds().entrySet()) {
            dataStream.writeName(entry.getKey());
            dataStream.writeName(entry.getValue());
        }
        dataStream.writeName(stub.getName());
        dataStream.writeBoolean(stub.hasName());
        if ("".equals(stub.getImportedModule())) {
            throw new IllegalStateException("Check failed.");
        }
        dataStream.writeName(stub.getImportedModule());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageSingleImportStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        StringRef name = dataStream.readName();
        int numBinds = dataStream.readInt();
        Set<StringRef> binds = new HashSet<>();
        for (int i = 0; i < numBinds; i++) {
            binds.add(dataStream.readName());
        }
        int numNamedBinds = dataStream.readInt();
        Map<StringRef, StringRef> namedBinds = new HashMap<>();
        for (int i = 0; i < numNamedBinds; i++) {
            namedBinds.put(dataStream.readName(), dataStream.readName());
        }
        StringRef importName = dataStream.readName();
        boolean hasName = dataStream.readBoolean();
        StringRef importedModule = Objects.requireNonNull(dataStream.readName());
        DAttributes attributes = DAttributes.read(dataStream);
        return new DLanguageSingleImportStub(
            parentStub, this, name, numBinds, binds, numNamedBinds, namedBinds,
            importName, hasName, importedModule, attributes
        );
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return !((DLanguageSingleImport) Objects.requireNonNull(node.getPsi())).getImportedModuleName().isBlank();
    }
}
