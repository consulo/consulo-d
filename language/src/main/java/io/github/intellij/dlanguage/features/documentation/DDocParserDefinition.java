package io.github.intellij.dlanguage.features.documentation;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import io.github.intellij.dlanguage.documentation.DDocLanguage;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocDescriptionSectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocKeyValueImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocLinkDeclarationImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocMacroSectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocNamedSectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocParamsSectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocSummarySectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DlangDocPsiElementImpl;
import io.github.intellij.dlanguage.psi.DlangPsiFileImpl;

import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_AUTHORS_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_BUGS_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_COPYRIGHT_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_DATE_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_DEPRECATED_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_DESCRIPTION_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_EXAMPLES_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_HISTORY_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_KEY_VALUE_ELEMENT;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_LICENSE_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_LINK_DECLARATION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_MACROS_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_NAMED_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_PARAMS_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_RETURNS_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_SEE_ALSO_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_STANDARDS_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_SUMMARY_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_THROWS_SECTION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_VERSION_SECTION;

/**
 * Registered in upstream's {@code plugin.xml} as {@code lang.parserDefinition} for the {@code DDoc} language.
 */
@ExtensionImpl
public class DDocParserDefinition implements ParserDefinition {
    // Consulo registers every element type globally: one file node type instead of a new one per call
    private static final IFileElementType FILE_NODE_TYPE = new IFileElementType(DDocLanguage.INSTANCE);

    @Override
    public Language getLanguage() {
        return DDocLanguage.INSTANCE;
    }

    @Override
    public Lexer createLexer(LanguageVersion languageVersion) {
        return new DDocLexer();
    }

    @Override
    public PsiParser createParser(LanguageVersion languageVersion) {
        return new DDocParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE_NODE_TYPE;
    }

    @Override
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return TokenSet.EMPTY;
    }

    @Override
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
        return TokenSet.EMPTY;
    }

    @Override
    @RequiredReadAction
    public PsiElement createElement(ASTNode node) {
        IElementType type = node.getElementType();
        if (type == DDOC_NAMED_SECTION) {
            return new DDocNamedSectionImpl(node);
        }
        if (type == DDOC_SUMMARY_SECTION) {
            return new DDocSummarySectionImpl(node);
        }
        if (type == DDOC_DESCRIPTION_SECTION) {
            return new DDocDescriptionSectionImpl(node);
        }
        if (type == DDOC_AUTHORS_SECTION
            || type == DDOC_BUGS_SECTION
            || type == DDOC_DATE_SECTION
            || type == DDOC_DEPRECATED_SECTION
            || type == DDOC_EXAMPLES_SECTION
            || type == DDOC_HISTORY_SECTION
            || type == DDOC_LICENSE_SECTION
            || type == DDOC_RETURNS_SECTION
            || type == DDOC_SEE_ALSO_SECTION
            || type == DDOC_STANDARDS_SECTION
            || type == DDOC_THROWS_SECTION
            || type == DDOC_VERSION_SECTION) {
            return new DDocNamedSectionImpl(node);
        }

        if (type == DDOC_COPYRIGHT_SECTION) {
            return new DDocNamedSectionImpl(node);
        }
        if (type == DDOC_PARAMS_SECTION) {
            return new DDocParamsSectionImpl(node);
        }
        if (type == DDOC_MACROS_SECTION) {
            return new DDocMacroSectionImpl(node);
        }

        if (type == DDOC_LINK_DECLARATION) {
            return new DDocLinkDeclarationImpl(node);
        }
        if (type == DDOC_KEY_VALUE_ELEMENT) {
            return new DDocKeyValueImpl(node);
        }
        return new DlangDocPsiElementImpl(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new DlangPsiFileImpl(viewProvider);
    }

}
