package io.github.intellij.dlanguage.features.formatter;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.ast.TokenType;
import consulo.language.codeStyle.AbstractBlock;
import consulo.language.codeStyle.Alignment;
import consulo.language.codeStyle.AlignmentStrategy;
import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.ChildAttributes;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.FormattingContext;
import consulo.language.codeStyle.FormattingModel;
import consulo.language.codeStyle.FormattingModelBuilder;
import consulo.language.codeStyle.FormattingModelProvider;
import consulo.language.codeStyle.Indent;
import consulo.language.codeStyle.Spacing;
import consulo.language.codeStyle.SpacingBuilder;
import consulo.language.codeStyle.Wrap;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageDeclarationBlock;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.interfaces.Statement;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static io.github.intellij.dlanguage.features.formatter.impl.SpacingKt.createSpacingBuilder;
import static io.github.intellij.dlanguage.psi.DlangTypes.ARGUMENT_LIST;
import static io.github.intellij.dlanguage.psi.DlangTypes.BLOCK_COMMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.BLOCK_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.CASE_RANGE_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.CASE_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.CONDITIONAL_DECLARATION;
import static io.github.intellij.dlanguage.psi.DlangTypes.CONDITIONAL_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.DECLARATION_BLOCK;
import static io.github.intellij.dlanguage.psi.DlangTypes.DEFAULT_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.ENUM_BODY;
import static io.github.intellij.dlanguage.psi.DlangTypes.EXPRESSION_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.FOREACH_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.FOR_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.IF_STATEMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_CASE;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_ELSE;
import static io.github.intellij.dlanguage.psi.DlangTypes.LINE_COMMENT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_BRACES_LEFT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_BRACES_RIGHT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_BRACKET_LEFT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_BRACKET_RIGHT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_COLON;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_LEFT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_RIGHT;
import static io.github.intellij.dlanguage.psi.DlangTypes.STRUCT_BODY;
import static io.github.intellij.dlanguage.psi.DlangTypes.TEMPLATE_DECLARATION;
import static io.github.intellij.dlanguage.psi.DlangTypes.WHILE_STATEMENT;


/**
 * Registered in upstream's {@code plugin.xml} as {@code lang.formatter} for D.
 */
@ExtensionImpl
public class DFormattingModelBuilder implements FormattingModelBuilder {

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    @RequiredReadAction
    public FormattingModel createModel(FormattingContext formattingContext) {
        PsiElement element = formattingContext.getPsiElement();
        CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
        DCodeStyleSettings dSettings = settings.getCustomSettings(DCodeStyleSettings.class);

        DFormattingBlock block = new DFormattingBlock(element.getNode(), AlignmentStrategy.getNullStrategy(), Indent.getNoneIndent(), null, createSpacingBuilder(settings, dSettings));
        return FormattingModelProvider.createFormattingModelForPsiFile(Objects.requireNonNull(element.getContainingFile()), block, settings);
    }

    private static class DFormattingBlock extends AbstractBlock {
        //            private val TokenSet BLOCKS_TOKEN_SET = TokenSet.create(
        //                BLOCK_STATEMENT,
        //                STRUCT_DECLARATION,
        //                INTERFACE_DECLARATION,
        //                EXPRESSION_STATEMENT
        //        )

        private static final TokenSet BRACES_TOKEN_SET = TokenSet.create(
            OP_BRACES_LEFT,
            OP_BRACES_RIGHT,
            OP_BRACKET_LEFT,
            OP_BRACKET_RIGHT,
            OP_PAR_LEFT,
            OP_PAR_RIGHT
        );

        private final AlignmentStrategy myAlignmentStrategy;
        private final @Nullable Indent myIndent;
        private final SpacingBuilder mySpacingBuilder;

        DFormattingBlock(ASTNode node,
                         AlignmentStrategy alignmentStrategy,
                         @Nullable Indent indent,
                         @Nullable Wrap wrap,
                         SpacingBuilder spacingBuilder) {
            super(node, wrap, alignmentStrategy.getAlignment(node.getElementType()));
            myAlignmentStrategy = alignmentStrategy;
            myIndent = indent;
            mySpacingBuilder = spacingBuilder;
        }

        @Override
        public @Nullable Indent getIndent() {
            return myIndent;
        }

        @Override
        @RequiredReadAction
        protected List<Block> buildChildren() {
            List<Block> blocks = new ArrayList<>();
            ASTNode child = myNode.getFirstChildNode();
            while (child != null) {
                IElementType childType = child.getElementType();
                if (child.getTextRange().getLength() == 0) {
                    child = child.getTreeNext();
                    continue;
                }
                if (childType == TokenType.WHITE_SPACE) {
                    child = child.getTreeNext();
                    continue;
                }
                IElementType substitutor = childType == BLOCK_COMMENT ? LINE_COMMENT : childType;
                Alignment alignment = myAlignmentStrategy.getAlignment(substitutor);
                DFormattingBlock e = buildSubBlock(child, alignment);
                blocks.add(e);
                child = child.getTreeNext();
            }
            return Collections.unmodifiableList(blocks);
        }

        @RequiredReadAction
        private DFormattingBlock buildSubBlock(ASTNode child, @Nullable Alignment alignment) {
            Indent indent = calcIndent(child);
            return new DFormattingBlock(child, AlignmentStrategy.wrap(alignment), indent, null, mySpacingBuilder);
        }

        @RequiredReadAction
        private Indent calcIndent(ASTNode child) {
            IElementType parentType = myNode.getElementType();
            IElementType type = child.getElementType();

            if (parentType == CONDITIONAL_DECLARATION) {
                return indentOfMultipleDeclarationChild(type, LINE_COMMENT, BLOCK_COMMENT);
            }
            if (child.getPsi() instanceof DLanguageDeclarationBlock declarationBlock && declarationBlock.getOP_BRACES_LEFT() != null) {
                return Indent.getNoneIndent();
            }
            if (parentType == IF_STATEMENT) {
                return indentOfMultipleDeclarationChild(type, LINE_COMMENT, BLOCK_COMMENT);
            }
            else if (parentType == FOREACH_STATEMENT) {
                return indentOfMultipleDeclarationChild(type, LINE_COMMENT, BLOCK_COMMENT);
            }
            else if (parentType == FOR_STATEMENT) {
                return indentOfMultipleDeclarationChild(type, LINE_COMMENT, BLOCK_COMMENT);
            }
            else if (parentType == WHILE_STATEMENT) {
                return indentOfMultipleDeclarationChild(type, LINE_COMMENT, BLOCK_COMMENT);
            }
            else if (parentType == CONDITIONAL_STATEMENT) {
                return indentOfMultipleDeclarationChild(type, LINE_COMMENT, BLOCK_COMMENT);
            }
            if (parentType == CASE_STATEMENT || parentType == CASE_RANGE_STATEMENT || parentType == DEFAULT_STATEMENT) {
                if (type == KW_CASE || type == ARGUMENT_LIST || type == OP_COLON) {
                    return indentOfMultipleDeclarationChild(type, LINE_COMMENT, BLOCK_COMMENT);
                }
                return Indent.getNormalIndent();
            }
            if (type == OP_BRACES_RIGHT || type == OP_BRACES_LEFT) {
                return Indent.getNoneIndent();
            }
            if (DUtil.INSTANCE.getPrevSiblingOfType(child, OP_BRACES_LEFT, new HashSet<>(List.of(/*OP_BRACES_RIGHT,*/ KW_ELSE))) != null) {
                return Indent.getNormalIndent();
            }
            if (DUtil.INSTANCE.getPrevSiblingOfType(child, OP_BRACKET_LEFT, new HashSet<>(List.of(OP_BRACKET_RIGHT))) != null) {
                return Indent.getNormalIndent();
            }
            if (DUtil.INSTANCE.getPrevSiblingOfType(child, OP_PAR_LEFT, new HashSet<>(List.of(OP_PAR_RIGHT))) != null) {
                return Indent.getNormalIndent();
            }
//            if(type == TEMPLATE_PARAMETERS){
//                return Indent.getContinuationIndent()
//            }
//            if(parentType == EXPRESSION){
//                return Indent.getContinuationIndent()
//            }
            //todo continuation indent
            return Indent.getNoneIndent();
        }

        private Indent indentOfMultipleDeclarationChild(IElementType childType, IElementType... specType) {
            for (IElementType iElementType : specType) {
                if (iElementType == childType) {
                    return Indent.getNormalIndent();
                }
            }
            return Indent.getNoneIndent();
        }

        @Override
        @RequiredReadAction
        public @Nullable Spacing getSpacing(@Nullable Block child1, Block child2) {
            if (child1 instanceof DFormattingBlock && child2 instanceof DFormattingBlock) {
                ASTNode n1 = ((DFormattingBlock) child1).getNode();
                ASTNode n2 = ((DFormattingBlock) child2).getNode();
                PsiElement psi1 = n1.getRequiredPsi();
                PsiElement psi2 = n2.getRequiredPsi();

                if (psi1 instanceof Declaration && psi2 instanceof Declaration) {
                    return lineBreak();
                }
                else if ("{".equals(psi1.getText()) && psi2 instanceof Statement) {
                    return lineBreak();
                }
                else if ("{".equals(psi1.getText()) && psi2 instanceof Declaration) {
                    return lineBreak();
                }
                else if ("}".equals(psi2.getText()) && psi1 instanceof Statement) {
                    return lineBreak();
                }
                else if ("}".equals(psi2.getText()) && psi1 instanceof Declaration) {
                    return lineBreak();
                }

                //
                if (n1.getElementType() == OP_BRACES_LEFT && psi2 instanceof Statement) {
                    return lineBreak();
                }
//
//                 if (isTopLevelDeclaration(psi2) && (isTopLevelDeclaration(psi1) || n1.getElementType() == SEMICOLON)) {
//                     Different declarations should be separated by blank line
//                    boolean sameKind = psi1.getClass().equals(psi2.getClass())
//                        || psi1 instanceof DLanguageDeclDefs && psi2 instanceof DLanguageDeclDefs;
//                    return sameKind ? lineBreak() : lineBreak(1, true);
//                }
            }
            return mySpacingBuilder.getSpacing(this, child1, child2);
        }

        @Override
        @RequiredReadAction
        public ChildAttributes getChildAttributes(int newChildIndex) {
            // This governs the indent on the new line when pressing the ENTER key

            if (myNode.getPsi() instanceof Expression) {
                new ChildAttributes(Indent.getContinuationWithoutFirstIndent(), null);
            }

            IElementType elementType = myNode.getElementType();
            if (elementType == EXPRESSION_STATEMENT) {
                return new ChildAttributes(Indent.getContinuationWithoutFirstIndent(), null);
            }
            else if (elementType == BLOCK_STATEMENT
                || elementType == CONDITIONAL_DECLARATION
                || elementType == CONDITIONAL_STATEMENT
                || elementType == DECLARATION_BLOCK
                || elementType == ENUM_BODY
                || elementType == STRUCT_BODY
                || elementType == TEMPLATE_DECLARATION) {
                return new ChildAttributes(Indent.getNormalIndent(), null);
            }
            else {
                return new ChildAttributes(Indent.getNoneIndent(), null);
            }
        }

        @Override
        public boolean isLeaf() {
            return getNode().getFirstChildNode() == null;
        }

        private static boolean isTopLevelDeclaration(PsiElement element) {
            return element instanceof Declaration
                || element instanceof Statement && element.getParent() instanceof DlangPsiFile;
        }

        private static Spacing lineBreak() {
            return lineBreak(true);
        }

        private static Spacing lineBreak(boolean keepLineBreaks) {
            return lineBreak(0, keepLineBreaks);
        }

        private static Spacing lineBreak(int lineBreaks, boolean keepLineBreaks) {
            return Spacing.createSpacing(0, 0, lineBreaks + 1, keepLineBreaks, keepLineBreaks ? 1 : 0);
        }

        private static Spacing none() {
            return Spacing.createSpacing(0, 0, 0, false, 0);
        }

        private static Spacing one() {
            return Spacing.createSpacing(1, 1, 0, false, 0);
        }
    }

}
