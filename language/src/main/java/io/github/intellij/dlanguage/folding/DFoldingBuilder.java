package io.github.intellij.dlanguage.folding;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.codeEditor.FoldingGroup;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.codeStyle.CodeStyle;
import consulo.language.editor.folding.CodeFoldingSettings;
import consulo.language.editor.folding.FoldingBuilderEx;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageAnonymousEnumDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageArrayInitializer;
import io.github.intellij.dlanguage.psi.DLanguageBlockStatement;
import io.github.intellij.dlanguage.psi.DLanguageConditionalDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageDeclarationBlock;
import io.github.intellij.dlanguage.psi.DLanguageEnumBody;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageLinkageAttribute;
import io.github.intellij.dlanguage.psi.DLanguageStructBody;
import io.github.intellij.dlanguage.psi.DLanguageStructInitializer;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.ext.PsiElementKt;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.Statement;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.intellij.dlanguage.psi.DlangTypes.OP_BRACES_LEFT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_BRACES_RIGHT;

@ExtensionImpl
public class DFoldingBuilder extends FoldingBuilderEx implements DumbAware {
    private static final int ONE_LINER_PLACEHOLDERS_EXTRA_LENGTH = 4;

    private String getPlaceholderTextForPsi(PsiElement psi) {
        if (psi instanceof PsiComment) {
            return "/* ... */";
        }
        if (psi instanceof DLanguageArrayInitializer) {
            return "[...]";
        }
        if (psi instanceof DLanguageStructBody
            || psi instanceof DLanguageBlockStatement
            || psi instanceof DLanguageTemplateDeclaration
            || psi instanceof DLanguageStructInitializer
            || psi instanceof DLanguageAnonymousEnumDeclaration
            || psi instanceof DLanguageEnumBody
            || psi instanceof DLanguageDeclarationBlock
            || psi instanceof DLanguageConditionalDeclaration) {
            return "{...}";
        }
        if (psi instanceof DLanguageLinkageAttribute linkageAttribute) {
            return isExternal(linkageAttribute) ? "{...}" : "...";
        }
        return "...";
    }

    @Override
    @RequiredReadAction
    public String getPlaceholderText(ASTNode node) {
        //OP_BRACES_LEFT -> " { "
        //OP_BRACES_RIGHT -> " }"
        PsiElement psi = node.getPsi();
        return psi == null ? "..." : getPlaceholderTextForPsi(psi);
    }

    @Override
    @RequiredReadAction
    public FoldingDescriptor[] buildFoldRegions(PsiElement root, Document document, boolean quick) {
        if (!(root instanceof DlangPsiFile)) {
            return FoldingDescriptor.EMPTY;
        }

        List<FoldingDescriptor> descriptors = new ArrayList<>();
        int rightMargin = CodeStyle.getSettings(root.getContainingFile()).getRightMargin(DLanguage.INSTANCE);
        FoldingVisitor visitor = new FoldingVisitor(descriptors, rightMargin);
        PsiTreeUtil.processElements(root, it -> {
            // required in case the psi element has been deleted
            if (it.isPhysical() && it.isValid() && it.isWritable() && !it.getText().isEmpty()) {
                it.accept(visitor);
                return true;
            }
            else {
                return false;
            }
        });

        return descriptors.toArray(FoldingDescriptor.EMPTY);
    }

    @Override
    @RequiredReadAction
    public boolean isCollapsedByDefault(ASTNode node) {
        if (node.getPsi() instanceof DLanguageImportDeclaration) {
            return CodeFoldingSettings.getInstance().COLLAPSE_IMPORTS;
        }
        if (TokenSet.create(OP_BRACES_LEFT, OP_BRACES_RIGHT).contains(node.getElementType())) {
            return DCodeFoldingSettings.getInstance().isCollapsibleOneLineMethods();
        }
        return false;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    private static class FoldingVisitor extends DlangVisitor {
        private final List<FoldingDescriptor> descriptors;
        private final int rightMargin;

        FoldingVisitor(List<FoldingDescriptor> descriptors, int rightMargin) {
            this.descriptors = descriptors;
            this.rightMargin = rightMargin;
        }

        private void fold(PsiElement element) {
            descriptors.add(new FoldingDescriptor(element.getNode(), element.getTextRange()));
        }

        @Override
        public void visitBlockStatement(DLanguageBlockStatement o) {
            if (tryFoldBlockWhitespaces(o)) {
                return;
            }
            fold(o);
        }

        @Override
        public void visitDeclarationBlock(DLanguageDeclarationBlock o) {
            fold(o);
        }

        @Override
        public void visitStructBody(DLanguageStructBody o) {
            fold(o);
        }

        @Override
        public void visitTemplateDeclaration(DLanguageTemplateDeclaration o) {
            foldBetween(o, leftBraces(o), rightBraces(o));
        }

        @Override
        public void visitArrayInitializer(DLanguageArrayInitializer o) {
            fold(o);
        }

        @Override
        public void visitStructInitializer(DLanguageStructInitializer o) {
            fold(o);
        }

        @RequiredReadAction
        private void foldBetween(PsiElement element, @Nullable PsiElement left, @Nullable PsiElement right) {
            if (left != null && right != null) {
                TextRange range = new TextRange(left.getTextOffset(), right.getTextOffset() + 1);
                descriptors.add(new FoldingDescriptor(element.getNode(), range));
            }
        }

        @Override
        public void visitLinkageAttribute(DLanguageLinkageAttribute o) {
            if (isExternal(o)) {
                if (!(o.getParent() instanceof Declaration decl)) {
                    return;
                }
                foldBetween(o, leftBraces(decl), rightBraces(decl));
            }
        }

        @Override
        public void visitAnonymousEnumDeclaration(DLanguageAnonymousEnumDeclaration o) {
            foldBetween(o, leftBraces(o), rightBraces(o));
        }

        @Override
        public void visitEnumBody(DLanguageEnumBody o) {
            fold(o);
        }

        // version statement
        @Override
        public void visitConditionalDeclaration(DLanguageConditionalDeclaration o) {
            foldBetween(o, leftBraces(o), rightBraces(o));
        }

        @Override
        @RequiredReadAction
        public void visitComment(PsiComment comment) {
            if (comment.getPrevSibling() instanceof PsiComment) {
                return;
            }
            PsiComment lastTrailing = comment;

            while (true) {
                if (lastTrailing.getNextSibling() == null) {
                    break;
                }
                if (!(lastTrailing.getNextSibling() instanceof PsiComment next)) {
                    break;
                }

                lastTrailing = next;
            }

            TextRange range = new TextRange(comment.getTextOffset(), lastTrailing.getTextRange().getEndOffset());
            descriptors.add(new FoldingDescriptor(comment.getNode(), range));
        }

        @Override
        @RequiredReadAction
        public void visitImportDeclaration(DLanguageImportDeclaration importDecl) {
            // Skip if previous line is import declaration
            if (PsiElementKt.getPrevNonCommentSibling(importDecl) instanceof DLanguageImportDeclaration) {
                return;
            }

            // Fold trailing imports
            DLanguageImportDeclaration lastTrailing = importDecl;

            while (true) {
                PsiElement nextSibling = PsiElementKt.getNextNonCommentSibling(lastTrailing);
                if (!(nextSibling instanceof DLanguageImportDeclaration next)) {
                    break;
                }

                lastTrailing = next;
            }

            // Don’t fold 1 line imports
            if (lastTrailing == importDecl) {
                return;
            }

            PsiElement kwImport = importDecl.getKW_IMPORT();
            if (kwImport == null) {
                return;
            }
            int startOffset = kwImport.getTextRange().getEndOffset();
            int endOffset = lastTrailing.getTextRange().getEndOffset();
            if (startOffset + 1 >= endOffset) {
                return;
            }
            TextRange range = new TextRange(startOffset + 1, endOffset);
            assert !range.isEmpty();
            descriptors.add(new FoldingDescriptor(importDecl, range));
        }

        @RequiredReadAction
        private boolean tryFoldBlockWhitespaces(DLanguageBlockStatement block) {
            PsiFile file = block.getContainingFile();
            Document doc = file == null ? null : PsiDocumentManager.getInstance(block.getProject()).getDocument(file);
            if (doc == null) {
                return false;
            }
            int maxLength = rightMargin - getOffsetInLine(block, doc) - ONE_LINER_PLACEHOLDERS_EXTRA_LENGTH;
            if (!isSingleLine(block, doc, maxLength)) {
                return false;
            }

            PsiElement leftBrace = block.getOP_BRACES_LEFT();
            if (leftBrace == null) {
                return false;
            }
            PsiElement rightBrace = block.getOP_BRACES_RIGHT();
            if (rightBrace == null) {
                return false;
            }

            // void foo() { \n
            //     writeln("Hello world"); \n
            // }
            if (block.getStatements().size() < 2) {
                return false;
            }

            /// Get previous whitespace if exist as start region
            PsiElement leftParent = leftBrace.getParent();
            PsiElement leftEl = leftParent != null && leftParent.getPrevSibling() instanceof PsiWhiteSpace whiteSpace ? whiteSpace : leftBrace;
            List<Statement> body = block.getStatements();
            if (body.isEmpty()) {
                return false;
            }

            TextRange range1 = new TextRange(leftEl.getTextOffset(), body.get(0).getTextOffset());
            TextRange range2 = new TextRange(body.get(body.size() - 1).getTextRange().getEndOffset(), rightBrace.getTextRange().getEndOffset());

            FoldingGroup group = FoldingGroup.newGroup("one-liner");
            descriptors.add(new FoldingDescriptor(leftBrace.getNode(), range1, group));
            descriptors.add(new FoldingDescriptor(rightBrace.getNode(), range2, group));

            return true;
        }
    }

    @RequiredReadAction
    private static int getOffsetInLine(PsiElement element, Document doc) {
        int blockLine = doc.getLineNumber(element.getTextRange().getStartOffset());
        return PsiElementKt.getLeftLeaves(element)
            .takeWhile(it -> doc.getLineNumber(it.getTextRange().getEndOffset()) == blockLine)
            .mapToInt(el -> el.getText().length() - Math.max(el.getText().lastIndexOf('\n') + 1, 0))
            .sum();
    }

    @RequiredReadAction
    private static boolean isSingleLine(DLanguageBlockStatement block, Document doc, int maxLength) {
        // remove all leading and trailing spaces before counting lines
        PsiElement leftBrace = block.getOP_BRACES_LEFT();
        PsiElement startContents = leftBrace == null ? null
            : PsiElementKt.getRightSiblings(leftBrace).dropWhile(it -> it instanceof PsiWhiteSpace).findFirst().orElse(null);
        if (startContents == null) {
            return false;
        }
        if (startContents.getNode().getElementType() == DlangTypes.OP_BRACES_RIGHT) {
            return false;
        }
        PsiElement rightBrace = block.getOP_BRACES_RIGHT();
        PsiElement endContents = rightBrace == null ? null
            : PsiElementKt.getLeftSiblings(rightBrace).dropWhile(it -> it instanceof PsiWhiteSpace).findFirst().orElse(null);
        if (endContents == null) {
            return false;
        }
        if (endContents.getTextRange().getEndOffset() - startContents.getTextOffset() > maxLength) {
            return false;
        }

        return doc.getLineNumber(startContents.getTextOffset()) == doc.getLineNumber(endContents.getTextRange().getEndOffset());
    }

    private static @Nullable PsiElement leftBraces(PsiElement element) {
        ASTNode child = element.getNode().findChildByType(DlangTypes.OP_BRACES_LEFT);
        return child == null ? null : child.getPsi();
    }

    private static @Nullable PsiElement rightBraces(PsiElement element) {
        ASTNode child = element.getNode().findChildByType(DlangTypes.OP_BRACES_RIGHT);
        return child == null ? null : child.getPsi();
    }

    private static boolean isExternal(DLanguageLinkageAttribute attribute) {
        return attribute.getNode().findChildByType(DlangTypes.KW_EXTERN) != null;
    }
}
