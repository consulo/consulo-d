package io.github.intellij.dlanguage.library;

import javax.swing.Icon;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ui.SdkPathEditor;
import com.intellij.openapi.roots.ui.OrderRootTypeUIFactory;
import io.github.intellij.dlanguage.icons.DlangIcons;

public class LibFileRootTypeUIFactory implements OrderRootTypeUIFactory
{
	@Override
	public SdkPathEditor createPathEditor(final Sdk sdk)
	{
		return new SdkPathEditor(getNodeText(), LibFileRootType.getInstance(), FileChooserDescriptorFactory.createSingleLocalFileDescriptor(), sdk);
	}

	@Override
	public Icon getIcon()
	{
		return DlangIcons.FILE;
	}

	@Override
	public String getNodeText()
	{
		return "External Attributes";
	}
}
