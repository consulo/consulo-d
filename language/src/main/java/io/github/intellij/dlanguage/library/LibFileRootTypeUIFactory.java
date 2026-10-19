package io.github.intellij.dlanguage.library;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.d.localize.DLocalize;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.ide.ui.OrderRootTypeUIFactory;
import consulo.ide.ui.SdkPathEditor;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import io.github.intellij.dlanguage.DLanguage;

@ExtensionImpl
public class LibFileRootTypeUIFactory implements OrderRootTypeUIFactory {
    @Override
    public String getOrderRootTypeId() {
        return LibFileRootType.ID;
    }

    @Override
    public SdkPathEditor createPathEditor(final Sdk sdk) {
        return new SdkPathEditor(getNodeText(), LibFileRootType.ID,
            FileChooserDescriptorFactory.createSingleLocalFileDescriptor(), sdk);
    }

    @Override
    public Image getIcon() {
        return DLanguage.Icons.FILE;
    }

    @Override
    public LocalizeValue getNodeText() {
        return DLocalize.libraryLibFileRootTypeNodeText();
    }
}
