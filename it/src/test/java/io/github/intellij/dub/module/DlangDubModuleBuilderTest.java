package io.github.intellij.dub.module;

import consulo.content.bundle.UnknownSdkType;
import consulo.it.HeadlessApplicationExtension;
import io.github.intellij.dlanguage.DlangSdkType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of upstream {@code DlangDubModuleBuilderTest.kt}.
 */
@ExtendWith(HeadlessApplicationExtension.class)
public class DlangDubModuleBuilderTest {

    private final DlangDubModuleBuilder moduleBuilder = new DlangDubModuleBuilder();

    @Test
    public void testIsSuitableSdkTypeReturnsTrueOnlyForDlangSdkType() {
        assertTrue(moduleBuilder.isSuitableSdkType(DlangSdkType.getInstance()));
        assertFalse(moduleBuilder.isSuitableSdkType(UnknownSdkType.getInstance("some other sdk type")));
    }

    @Test
    public void testItCreatesSourceDirectoryIfSourcePathsWasEmpty() {
        List<String> sourcePaths = moduleBuilder.getSourcePaths();
        assertNotNull(sourcePaths);
        assertEquals(1, sourcePaths.size());
    }
}
