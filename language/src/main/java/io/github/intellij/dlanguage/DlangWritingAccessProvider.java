package io.github.intellij.dlanguage;

import consulo.language.psi.PsiFile;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.WritingAccessProvider;

import java.util.Collection;
import java.util.List;

public class DlangWritingAccessProvider extends WritingAccessProvider {

    private final Project myProject;

    public DlangWritingAccessProvider(final Project project) {
        myProject = project;
    }

    public static boolean isInDLanguageSdkOrDLanguagePackagesFolder(final PsiFile psiFile) {
        final VirtualFile vFile = psiFile.getOriginalFile().getVirtualFile();
        return vFile != null && isInDLanguageSdkOrDLanguagePackagesFolder(psiFile.getProject(), vFile);
    }

    public static boolean isInDLanguageSdkOrDLanguagePackagesFolder(final Project project, final VirtualFile file) {
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

        if (fileIndex.isInLibraryClasses(file)) {
            return true; // file in SDK or in custom package root
        }

//    if (fileIndex.isExcluded(file) || (fileIndex.isInContent(file) && isInDartPackagesFolder(fileIndex, file))) {
//      return true; // symlinked child of 'packages' folder. Real location is in user cache folder for Dart packages, not in project
//    }

        return false;
    }

    // Consulo declares requestWriting abstract; nothing is refused here
    @Override
    public Collection<VirtualFile> requestWriting(final VirtualFile... files) {
        return List.of();
    }

    @Override
    public boolean isPotentiallyWritable(final VirtualFile file) {
        if (DlangFileType.INSTANCE != file.getFileType()) return true;
        return !isInDLanguageSdkOrDLanguagePackagesFolder(myProject, file);
    }

//  private static boolean isInDartPackagesFolder(final ProjectFileIndex fileIndex, final VirtualFile file) {
//    VirtualFile parent = file;
//    while ((parent = parent.getParent()) != null && fileIndex.isInContent(parent)) {
//      if (DartUrlResolver.PACKAGES_FOLDER_NAME.equals(parent.getName())) {
//        return VfsUtilCore.findRelativeFile("../" + PUBSPEC_YAML, parent) != null;
//      }
//    }
//
//    return false;
//  }
}
