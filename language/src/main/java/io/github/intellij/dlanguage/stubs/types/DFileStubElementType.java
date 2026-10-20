package io.github.intellij.dlanguage.stubs.types;

import consulo.d.language.moduleAware.DCompileEnv;
import consulo.d.language.moduleAware.DConditionalCompilation;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.DefaultStubBuilder;
import consulo.language.psi.stub.IStubFileElementType;
import consulo.language.psi.stub.StubBuilder;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DlangPsiFileImpl;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.stubs.DlangFileStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class DFileStubElementType extends IStubFileElementType<DlangFileStub> {
    public static final int VERSION = 4;
    public static final DFileStubElementType INSTANCE = new DFileStubElementType();

    public DFileStubElementType() {
        super("FILE", DLanguage.INSTANCE);
    }

    @Override
    public StubBuilder getBuilder() {
        return new DefaultStubBuilder() {
            private @Nullable DConditionalCompilation myCompilation;

            @Override
            public boolean skipChildProcessingWhenBuildingStubs(ASTNode parent, ASTNode node) {
                if (parent.getElementType() != DlangTypes.CONDITIONAL_DECLARATION) {
                    return false;
                }
                DConditionalCompilation compilation = myCompilation;
                if (compilation == null) {
                    ASTNode fileNode = parent;
                    for (ASTNode treeParent = fileNode.getTreeParent(); treeParent != null; treeParent = treeParent.getTreeParent()) {
                        fileNode = treeParent;
                    }
                    PsiElement file = fileNode.getPsi();
                    compilation = DConditionalCompilation.of(fileNode, file == null ? null : DCompileEnv.optionsFor(file));
                    myCompilation = compilation;
                }
                return compilation.isInactive(parent, node);
            }

            @Override
            protected StubElement createStubForFile(PsiFile file) {
                if (file instanceof DlangPsiFileImpl dlangPsiFile) {
                    return new DlangFileStub(dlangPsiFile);
                }
                return super.createStubForFile(file);
            }
        };
    }

    @Override
    public int getStubVersion() {
        return VERSION;
    }

    @Override
    public void serialize(DlangFileStub stub, StubOutputStream dataStream) throws IOException {
        // todo make files named?
    }

    @Override
    public DlangFileStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DlangFileStub(null);
    }

    @Override
    public String getExternalId() {
        return "d.FILE";
    }
}
