package io.github.intellij.dlanguage.sdlang.psi;

import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.virtualFileSystem.fileType.FileType;
import io.github.intellij.dlanguage.sdlang.SDLangFileType;
import io.github.intellij.dlanguage.sdlang.SimpleDefinitionLanguage;

public class SDLangFile extends PsiFileBase {
    public SDLangFile(FileViewProvider viewProvider) {
        super(viewProvider, SimpleDefinitionLanguage.INSTANCE);
    }

    @Override
    public FileType getFileType() {
        return SDLangFileType.INSTANCE;
    }
}
