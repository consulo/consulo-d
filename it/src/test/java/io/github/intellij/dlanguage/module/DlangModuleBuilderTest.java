package io.github.intellij.dlanguage.module;

import consulo.application.Application;
import consulo.content.bundle.SdkType;
import consulo.content.bundle.UnknownSdkType;
import consulo.it.HeadlessApplicationExtension;
import io.github.intellij.dlanguage.DlangSdkType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of upstream {@code DlangModuleBuilderTest.java}. Upstream's {@code testSetupRootModel} mocked IntelliJ's
 * {@code ModifiableRootModel} to check {@code setSdk} / {@code inheritSdk}, which Consulo does not have (the SDK belongs
 * to the D module extension), so it is not ported. {@code testGetSourcePaths} gets a temporary content root instead of
 * none: upstream created {@code null/source} in the working directory.
 */
@ExtendWith(HeadlessApplicationExtension.class)
public class DlangModuleBuilderTest {

    private final DlangModuleBuilder moduleBuilder = new DlangModuleBuilder();

    @Test
    public void testGetSourcePaths(@TempDir Path contentRoot) {
        moduleBuilder.setContentEntryPath(contentRoot.toString());

        List<String> sourcePaths = moduleBuilder.getSourcePaths();

        assertNotNull(sourcePaths);
        assertEquals(1, sourcePaths.size());
        assertEquals(contentRoot.resolve("source").toString(), sourcePaths.get(0));
        assertTrue(Files.isDirectory(contentRoot.resolve("source")));
    }

    @Test
    public void testIsSuitableSdkType() {
        assertTrue(moduleBuilder.isSuitableSdkType(DlangSdkType.getInstance()));

        assertFalse(moduleBuilder.isSuitableSdkType(UnknownSdkType.getInstance("nonsense")));

        // make sure any available SDK's that are not Dlang assert as false
        Application.get().getExtensionPoint(SdkType.class).getExtensionList().stream()
            .filter(sdkType -> DlangSdkType.class != sdkType.getClass())
            .forEach(sdkType -> assertFalse(moduleBuilder.isSuitableSdkType(sdkType)));
    }
}
