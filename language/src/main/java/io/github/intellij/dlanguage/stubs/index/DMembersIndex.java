package io.github.intellij.dlanguage.stubs.index;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.NamedStubBase;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.Stub;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.stub.StubIndexKey;
import consulo.project.Project;
import io.github.intellij.dlanguage.index.DModuleIndex;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.stubs.DLanguageConstructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageDestructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageFunctionDeclarationStub;
import io.github.intellij.dlanguage.stubs.DLanguageSharedStaticConstructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageSharedStaticDestructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageStaticConstructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageStaticDestructorStub;
import io.github.intellij.dlanguage.stubs.DLanguageUnittestStub;
import io.github.intellij.dlanguage.stubs.interfaces.HasMembersStub;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by francis on 8/8/2017.
 * index contains members of string key. Does not handle inheritance/mixins
 * <p>
 * Port of upstream {@code MembersIndex.kt}.
 */
@ExtensionImpl
public class DMembersIndex extends StringStubIndexExtension<DNamedElement> {

    private static final StubIndexKey<String, DNamedElement> KEY = StubIndexKey.createIndexKey("d.globally.members");
    public static final int VERSION = 3;

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    //use with caution, prefer static methods
    @Override
    public StubIndexKey<String, DNamedElement> getKey() {
        return KEY;
    }

    public static <S extends NamedStubBase<?>> void indexMembers(S stub, IndexSink sink) {
        if (getParentHasMembers(stub).size() > 1) {
            return;
        }
        for (HasMembersStub hasMembers : getParentHasMembers(stub)) {
            sink.occurrence(DMembersIndex.KEY, hasMembers.getName());
        }
    }

    @SuppressWarnings("deprecation")
    public static Set<DNamedElement> getMemberSymbols(String name, String module, Project project) {
        Set<DNamedElement> elements = new LinkedHashSet<>();
        for (DlangPsiFile file : DModuleIndex.getFilesByModuleName(project, module, GlobalSearchScope.allScope(project))) {
            elements.addAll(StubIndex.getElements(KEY, name, project, GlobalSearchScope.fileScope(file), DNamedElement.class));//todp assert that this should only be called once
        }
        return elements;
    }

    private static Set<HasMembersStub> getParentHasMembers(Stub stub) {
        Set<HasMembersStub> result = new HashSet<>();
        getParentHasMembersImpl(stub, result);
        return result;
    }

    private static void getParentHasMembersImpl(Stub stub, Set<HasMembersStub> result) {
        if (stub instanceof HasMembersStub hasMembersStub) {
            result.add(hasMembersStub);
        }
        Stub parentStub = stub.getParentStub();
        if (parentStub == null) {
            return;
        }
        getParentHasMembersImpl(parentStub, result);
        if (parentStub instanceof DLanguageUnittestStub || parentStub instanceof DLanguageFunctionDeclarationStub || parentStub instanceof DLanguageConstructorStub || parentStub instanceof DLanguageSharedStaticConstructorStub || parentStub instanceof DLanguageStaticConstructorStub || parentStub instanceof DLanguageDestructorStub || parentStub instanceof DLanguageSharedStaticDestructorStub || parentStub instanceof DLanguageStaticDestructorStub) {
            result.clear();
        }
    }
}
