package io.github.intellij.dlanguage.stubs.index;

import consulo.language.psi.stub.NamedStubBase;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DVisibilityStubKind;
import io.github.intellij.dlanguage.stubs.DLanguageClassDeclarationStub;
import io.github.intellij.dlanguage.stubs.DLanguageConstructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageDestructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageEnumDeclarationStub;
import io.github.intellij.dlanguage.stubs.DLanguageEnumMemberStub;
import io.github.intellij.dlanguage.stubs.DLanguageForeachTypeStub;
import io.github.intellij.dlanguage.stubs.DLanguageFunctionDeclarationStub;
import io.github.intellij.dlanguage.stubs.DLanguageImportDeclarationStub;
import io.github.intellij.dlanguage.stubs.DLanguageInterfaceDeclarationStub;
import io.github.intellij.dlanguage.stubs.DLanguageParameterStub;
import io.github.intellij.dlanguage.stubs.DLanguageSharedStaticConstructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageSharedStaticDestructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageStaticConstructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageStaticDestructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageStructDeclarationStub;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateAliasParameterStub;
import io.github.intellij.dlanguage.stubs.DLanguageUnittestStub;

/**
 * Created by francis on 8/8/2017.
 */
public final class StubIndexUtilKt {
    private StubIndexUtilKt() {
    }

    public static <S extends NamedStubBase<T>, T extends DNamedElement> boolean topLevelDeclaration(S stub) {
        //switch the topLevel declaration to a file gist maybe

        if (stub instanceof DLanguageParameterStub || stub instanceof DLanguageForeachTypeStub || stub instanceof DLanguageTemplateAliasParameterStub) {
            return false;
        }

        if (stub instanceof DLanguageEnumMemberStub) {
            return !(stub.getParentStub() instanceof DLanguageEnumDeclarationStub);
        }


        StubElement stubParent = stub;
        while (true) {
            stubParent = stubParent.getParentStub();
            if (stubParent == null) {
                return true;
            }
            //stuff within func declarations does not count as top level b/c not globally accessible
            if (stubParent instanceof DLanguageFunctionDeclarationStub) {
                return false;
            }
            if (stubParent instanceof DLanguageConstructorStub || stubParent instanceof DLanguageSharedStaticConstructorStub || stubParent instanceof DLanguageStaticConstructorStub || stubParent instanceof DLanguageDestructorStub || stubParent instanceof DLanguageSharedStaticDestructorStub || stubParent instanceof DLanguageStaticDestructorStub) {
                return false;
            }
            if (stubParent instanceof DLanguageStructDeclarationStub || stubParent instanceof DLanguageInterfaceDeclarationStub || stubParent instanceof DLanguageClassDeclarationStub) {
                return false;
            }
            //stuff within unittests does not count as top level
            if (stubParent instanceof DLanguageUnittestStub) {
                return false;
            }
            if (stubParent instanceof DLanguageImportDeclarationStub importDeclarationStub && importDeclarationStub.getVisibility() != DVisibilityStubKind.PUBLIC) {
                return false;
            }
        }
    }
}
