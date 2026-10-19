
package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageMemberFunctionAttribute;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.FunctionBody;
import io.github.intellij.dlanguage.stubs.DLanguageDestructorStub;
import java.util.List;
import org.jspecify.annotations.Nullable;


public interface DLanguageDestructor extends PsiElement, Declaration,
    StubBasedPsiElement<DLanguageDestructorStub> {

    @Nullable FunctionBody getFunctionBody();

    @Nullable PsiElement getOP_SCOLON();

    @Nullable PsiElement getOP_PAR_RIGHT();

    @Nullable PsiElement getOP_PAR_LEFT();

    @Nullable PsiElement getKW_THIS();

    @Nullable PsiElement getOP_TILDA();

    List<DLanguageMemberFunctionAttribute> getMemberFunctionAttributes();
}
