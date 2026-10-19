package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.ObjectStubSerializerProvider;
import consulo.language.psi.stub.StubElementTypeHolder;
import io.github.intellij.dlanguage.psi.impl.DStubElementTypes;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Registers the stub element types of {@link DStubElementTypes}; upstream did it in {@code plugin.xml}
 * ({@code <stubElementTypeHolder class="io.github.intellij.dlanguage.psi.impl.DStubElementTypes" externalIdPrefix="d."/>}).
 */
@ExtensionImpl
public class DStubElementTypeHolder extends StubElementTypeHolder<DStubElementTypes> {
    @Override
    public @Nullable String getExternalIdPrefix() {
        return "d.";
    }

    @Override
    public List<ObjectStubSerializerProvider> loadSerializers() {
        return allFromStaticFields(DStubElementTypes.class, Field::get);
    }
}
