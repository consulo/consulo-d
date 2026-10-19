package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.PsiFileStubImpl;
import io.github.intellij.dlanguage.psi.DlangPsiFileImpl;
import org.jspecify.annotations.Nullable;

/**
 * Basic implementation of a stub for a D file so we can index its contents.
 */
public class DlangFileStub extends PsiFileStubImpl<DlangPsiFileImpl> {
    public DlangFileStub(@Nullable DlangPsiFileImpl file) {
        super(file);
    }
}
