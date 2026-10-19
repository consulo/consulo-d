package io.github.intellij.dlanguage;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.component.util.Iconable;
import consulo.language.icon.IconDescriptor;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.BitUtil;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.presentation.UtilsKt;
import io.github.intellij.dlanguage.psi.impl.DLanguageModuleImpl;
import io.github.intellij.dlanguage.psi.impl.DLanguagePackageImpl;

/**
 * This class allows us to have different icons for D source files
 * <p>
 * Upstream's {@code IconProvider}; in Consulo element icons come from an {@code IconDescriptorUpdater}, which also gives
 * declarations the icon of their kind (upstream's presentation icons) and their visibility.
 */
@ExtensionImpl
public class DlangIconProvider implements IconDescriptorUpdater {
    @Override
    @RequiredReadAction
    public void updateIcon(IconDescriptor iconDescriptor, PsiElement element, int flags) {
        // upstream's DLanguageModuleImpl / DLanguagePackageImpl.getElementIcon
        if (element instanceof DLanguageModuleImpl || element instanceof DLanguagePackageImpl) {
            iconDescriptor.setMainIcon(PlatformIconGroup.nodesPackage());
            return;
        }
        if (element.getLanguage() != DLanguage.INSTANCE) {
            return;
        }
        if (element instanceof PsiFile file) {
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile != null) {
                iconDescriptor.setMainIcon(switch (virtualFile.getName()) {
                    case "app.d" -> DLanguage.Icons.SRC_FILE_RUNNABLE; // common to have main entry point in app.d
                    case "package.d" -> DLanguage.Icons.SRC_FILE_PACKAGE;
                    default -> DLanguage.Icons.SRC_FILE;
                });
            }
            return;
        }

        Image icon = UtilsKt.getPresentationIcon(element);
        if (icon == null) {
            return;
        }
        iconDescriptor.setMainIcon(icon);
        if (BitUtil.isSet(flags, Iconable.ICON_FLAG_VISIBILITY)) {
            iconDescriptor.setRightIcon(switch (UtilsKt.psiElementGetVisibility(element)) {
                case PRIVATE -> PlatformIconGroup.nodesC_private();
                case PROTECTED -> PlatformIconGroup.nodesC_protected();
                case PACKAGE -> PlatformIconGroup.nodesC_plocal();
                case PUBLIC, NONE -> PlatformIconGroup.nodesC_public();
            });
        }
    }
}
