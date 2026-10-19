package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.index.io.StringRef;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DLanguageSingleImportStub extends DNamedStubBase<DLanguageSingleImport> {

    private final int numBinds;
    private final Set<String> binds = new HashSet<>();

    private final int numNamedBinds;
    private final Map<String, String> namedBinds = new HashMap<>();

    private final String importedModule;
    private final boolean hasName;
    private final @Nullable String importName;

    public DLanguageSingleImportStub(final @Nullable StubElement parent,
                                     final IStubElementType elementType,
                                     final @Nullable String name,
                                     final int numBinds,
                                     final Set<StringRef> binds,
                                     final int numNamedBinds,
                                     final Map<StringRef, StringRef> namedBinds,
                                     final String importedModule,
                                     final boolean hasName,
                                     final @Nullable String importName,
                                     final @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
        this.numBinds = numBinds;
        for (final StringRef bind : binds) {
            this.binds.add(bind.getString());
        }
        this.numNamedBinds = numNamedBinds;
        namedBinds.forEach((bindName, bind) -> this.namedBinds.put(bindName.getString(), bind.getString()));
        this.importedModule = importedModule;
        assert importedModule == null || !importedModule.isBlank() : "imported Module should not be blank";
        this.hasName = hasName;
        this.importName = importName;
    }

    public DLanguageSingleImportStub(final @Nullable StubElement parent,
                                     final IStubElementType elementType,
                                     final @Nullable StringRef name,
                                     final int numBinds,
                                     final Set<StringRef> binds,
                                     final int numNamedBinds,
                                     final Map<StringRef, StringRef> namedBinds,
                                     final @Nullable StringRef importName,
                                     final boolean hasName,
                                     final StringRef importedModule,
                                     final @Nullable DAttributes attributes) {
        this(parent, elementType, name != null ? name.getString() : null,
            numBinds, binds, numNamedBinds, namedBinds, importedModule.getString(), hasName,
            importName == null ? null : importName.getString(),
            attributes);
    }

    public int numBinds() {
        return numBinds;
    }

    public Set<String> getBinds() {
        return binds;
    }

    public int numNamedBinds() {
        return numNamedBinds;
    }
    public Map<String, String> getNamedBinds() {
        return namedBinds;
    }

    public String getImportedModule() {
        return importedModule;
    }

    public boolean hasName() {
        return hasName;
    }

    public @Nullable String getName() {
        return importName;
    }

    public Set<String> getApplicableImportBinds() {
        return binds;
    }

    public Map<String, String> getApplicableNamedImportBinds() {
        return namedBinds;
    }
}
