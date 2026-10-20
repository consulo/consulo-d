package io.github.intellij.dlanguage.features.documentation;

import consulo.annotation.access.RequiredReadAction;
import consulo.d.language.DDocHtmlUtil;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.editor.documentation.DocumentationMarkup;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.documentation.psi.DlangDocComment;
import io.github.intellij.dlanguage.features.documentation.psi.DlangDocPsiElement;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocDescriptionSectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocKeyValueImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocLinkDeclarationImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocMacroSectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocNamedSectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocParamsSectionImpl;
import io.github.intellij.dlanguage.features.documentation.psi.impl.DDocSummarySectionImpl;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.interfaces.VariableDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes.DDOC_COMMENT_LEADING_ASTERISKS;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_COLON;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_COMMENT_DATA;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_DOUBLE_EMPHASIS;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_EMBEDDED_CODE;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_EMBEDDED_CODE_DELIMITER;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_HEADING;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_HEADING_CHARS;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_HORIZONTAL_RULE;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_IMAGE;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_INLINE_CODE;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_LINK;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_LINK_DECLARATION;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_LINK_INLINE_REFERENCE_TEXT;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_LINK_REFERENCE_TO;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_LINK_TEXT;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_MACRO_CALL;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_ORDERED_LIST;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_QUOTE;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_QUOTE_CHAR;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_SIMPLE_EMPHASIS;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_TRIPLE_EMPHASIS;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_UNORDERED_LIST;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_WHITESPACE;

public class DDocGenerator {

    private boolean myRenderedDoc = false;

    @RequiredReadAction
    public String generateDoc(PsiElement element) {
        myRenderedDoc = false;
        StringBuilder builder = new StringBuilder();

        if (element instanceof DLanguageFunctionDeclaration ||
            element instanceof VariableDeclaration ||
            element instanceof DLanguageClassDeclaration ||
            element instanceof DLanguageInterfaceDeclaration ||
            element instanceof DLanguageAliasDeclaration ||
            element instanceof DLanguageEnumDeclaration ||
            element instanceof DLanguageStructDeclaration ||
            element instanceof DLanguageUnionDeclaration ||
            element instanceof DLanguageTemplateDeclaration) {
            List<DlangDocComment> elements = new ArrayList<>();
            for (PsiElement child : element.getChildren()) {
                if (!(child instanceof DlangDocComment || child instanceof PsiWhiteSpace)) {
                    break;
                }
                if (child instanceof DlangDocComment docComment) {
                    elements.add(docComment);
                }
            }
            if (!elements.isEmpty()) {
                appendDdoc(builder, elements);
            }
        }
        if (element instanceof DLanguageEnumMember) {
            // TODO find enum member documentation
        }
        return builder.toString();
    }

    @RequiredReadAction
    public String generateDocRendered(DlangDocComment comment) {
        myRenderedDoc = true;
        StringBuilder builder = new StringBuilder();
        // Consulo: upstream rendered a comment without sections (`/// ditto`) too, which failed in appendContentSections
        if (comment.sections().length == 0) {
            return "";
        }
        Collection<DDocLinkDeclarationImpl> linksDeclarations = PsiTreeUtil.findChildrenOfType(comment, DDocLinkDeclarationImpl.class);
        appendContentSections(builder, comment.sections(), linksDeclarations);
        return builder.toString();
    }

    @RequiredReadAction
    private void appendDdoc(StringBuilder builder, List<DlangDocComment> elements) {
        StringBuilder sectionsBuilder = new StringBuilder();
        // TODO actually choose which section to add (example copyright to display only for module)
        for (DlangDocComment el : elements) {
            Collection<DDocLinkDeclarationImpl> linksDeclarations = PsiTreeUtil.findChildrenOfType(el, DDocLinkDeclarationImpl.class);
            if (el.sections().length == 0) {
                continue;
            }
            appendContentSections(sectionsBuilder, el.sections(), linksDeclarations);
        }

        if (sectionsBuilder.toString().isBlank()) {
            return;
        }
        builder.append(sectionsBuilder);
    }

    @RequiredReadAction
    private void appendContentSections(StringBuilder builder, PsiElement[] sections, Collection<DDocLinkDeclarationImpl> linksDeclarations) {
        assert sections.length != 0;

        int i = 0;
        // TODO refactor to have section only being sections (not polluted)
        while (PsiUtilCore.getElementType(sections[i]) == DDOC_WHITESPACE || PsiUtilCore.getElementType(sections[i]) == TokenType.WHITE_SPACE || PsiUtilCore.getElementType(sections[i]) == DDOC_COMMENT_LEADING_ASTERISKS) {
            i++;
            if (i >= sections.length) {
                return;
            }
        }
        PsiElement summary = sections[i];
        if (summary instanceof DDocSummarySectionImpl summarySection) {
            builder.append(DocumentationMarkup.CONTENT_START);
            builder.append("<p>");
            builder.append(buildContent(summarySection.getSection(), linksDeclarations));
            builder.append("</p>");
            i++;
            if (i >= sections.length) {
                builder.append(DocumentationMarkup.CONTENT_END);
                return;
            }
            while (PsiUtilCore.getElementType(sections[i]) == DDOC_WHITESPACE || PsiUtilCore.getElementType(sections[i]) == DDOC_COMMENT_LEADING_ASTERISKS) {
                i++;
                if (i >= sections.length) {
                    builder.append(DocumentationMarkup.CONTENT_END);
                    return;
                }
            }
            while (PsiUtilCore.getElementType(sections[i]) == DDOC_WHITESPACE || PsiUtilCore.getElementType(sections[i]) == TokenType.WHITE_SPACE || PsiUtilCore.getElementType(sections[i]) == DDOC_COMMENT_LEADING_ASTERISKS) {
                i++;
                if (i >= sections.length) {
                    builder.append(DocumentationMarkup.CONTENT_END);
                    return;
                }
            }
            PsiElement description = sections[i];
            if (description instanceof DDocDescriptionSectionImpl descriptionSection) {
                for (PsiElement content : descriptionSection.getSections()) {
                    builder.append("<p>");
                    builder.append(buildContent(content, linksDeclarations));
                    builder.append("</p>");
                }
                i++;
            }
            builder.append(DocumentationMarkup.CONTENT_END);
        }
        if (i >= sections.length) {
            return;
        }
        builder.append(DocumentationMarkup.SECTIONS_START);
        for (PsiElement section : Arrays.asList(sections).subList(i, sections.length)) {
            if (section instanceof DDocParamsSectionImpl paramsSection) {
                builder.append(DocumentationMarkup.SECTION_HEADER_START);
                builder.append(DLocalize.ddocSectionParameters().get());
                builder.append(DocumentationMarkup.SECTION_SEPARATOR);
                List<DDocKeyValueImpl> parameters = paramsSection.getParameters();
                for (DDocKeyValueImpl parameter : parameters) {
                    builder.append(buildParameter(parameter, linksDeclarations));
                }
                builder.append(DocumentationMarkup.SECTION_END);
                builder.append("</tr>");
            }
            else if (section instanceof DDocMacroSectionImpl) {
                /* nothing because macro is not intended to be rendered */
            }
            else if (section instanceof DDocNamedSectionImpl namedSection) {
                builder.append(DocumentationMarkup.SECTION_HEADER_START);
                builder.append(namedSection.getTitle().getText());
                builder.append(DocumentationMarkup.SECTION_SEPARATOR);
                List<PsiElement> contents = namedSection.getContents();
                for (PsiElement content : contents) {
                    builder.append("<p>");
                    builder.append(buildContent(content, linksDeclarations));
                    builder.append("</p>");
                }
                builder.append(DocumentationMarkup.SECTION_END);
                builder.append("</tr>");
            }
        }
        builder.append(DocumentationMarkup.SECTIONS_END);
    }

    @RequiredReadAction
    private String buildParameter(DDocKeyValueImpl parameter, Collection<DDocLinkDeclarationImpl> linksDeclarations) {
        StringBuilder builder = new StringBuilder();

        builder.append("<code>").append(parameter.getName()).append("</code>");
        builder.append(" &ndash; ");
        for (PsiElement content : parameter.getDescriptionElements()) {
            builder.append(buildContent(content, linksDeclarations));
        }
        builder.append("<br/>");
        return builder.toString();
    }

    @RequiredReadAction
    private String buildContent(PsiElement section, Collection<DDocLinkDeclarationImpl> linksDeclarations) {
        StringBuilder builder = new StringBuilder();

        List<PsiElement> children = section instanceof DlangDocPsiElement docPsiElement
            ? Arrays.asList(docPsiElement.getDescriptionElements())
            : childLeafs(section);
        for (PsiElement element : children) {
            IElementType elementType = PsiUtilCore.getElementType(element);
            if (elementType == DDOC_COMMENT_LEADING_ASTERISKS) {
                continue;
            }
            if (elementType == DDOC_WHITESPACE) {
                builder.append(" ");
            }
            else if (elementType == DDOC_COMMENT_DATA) {
                builder.append(element.getText());
            }
            else if (elementType == DDOC_COLON) {
                builder.append(element.getText());
            }
            else if (elementType == DDOC_MACRO_CALL) {
                builder.append(element.getText()); // TODO interpret macro (substitute by the value)
            }
            else if (elementType == DDOC_HORIZONTAL_RULE) {
                builder.append("<hr/>");
            }
            else if (elementType == DDOC_HEADING) {
                int level = indexOfFirstNot(element.getText(), '#');
                builder.append("<h");
                builder.append(level);
                builder.append(">");
                builder.append(buildContent(element, linksDeclarations));
                builder.append("</h");
                builder.append(level);
                builder.append(">");
            }
            else if (elementType == DDOC_HEADING_CHARS) {
                continue; // not displayed, it’s delimiter of heading
            }
            else if (elementType == DDOC_INLINE_CODE) {
                builder.append("<code>");
                builder.append(StringUtil.escapeXmlEntities(element.getText().substring(1, element.getText().length() - 1))); // text without ` `
                builder.append("</code>");
            }
            else if (elementType == DDOC_EMBEDDED_CODE) {
                builder.append(buildEmbeddedCodeContent((DlangDocPsiElement) element));
            }
            else if (elementType == DDOC_SIMPLE_EMPHASIS) {
                builder.append("<i>");
                builder.append(element.getText(), 1, element.getText().length() - 1); // text without * *
                builder.append("</i>");
            }
            else if (elementType == DDOC_DOUBLE_EMPHASIS) {
                builder.append("<b>");
                builder.append(element.getText(), 2, element.getText().length() - 2); // text without ** **
                builder.append("</b>");
            }
            else if (elementType == DDOC_TRIPLE_EMPHASIS) {
                builder.append("<b><i>");
                builder.append(element.getText(), 3, element.getText().length() - 3); // text without *** ***
                builder.append("</i></b>");
            }
            else if (elementType == DDOC_QUOTE) {
                builder.append("<blockquote>");
                builder.append(buildContent(element, linksDeclarations));
                builder.append("</blockquote>");
            }
            else if (elementType == DDOC_QUOTE_CHAR) {
                continue; // not displayed, already handled in DDOC_QUOTE
            }
            else if (elementType == DDOC_LINK) {
                builder.append(buildLinkContent((DlangDocPsiElement) element, false, linksDeclarations));
            }
            else if (elementType == DDOC_IMAGE) {
                DlangDocPsiElement linkContent = (DlangDocPsiElement) element.getFirstChild().getNextSibling();
                builder.append(buildLinkContent(linkContent, true, linksDeclarations));
            }
            else if (elementType == DDOC_LINK_DECLARATION) {
                continue; // its content is replaced in DDOC_LINKs
            }
            else if (elementType == DDOC_ORDERED_LIST) {
                builder.append("<ol>");
                buildListContent(builder, element, linksDeclarations);
                builder.append("</ol>");
            }
            else if (elementType == DDOC_UNORDERED_LIST) {
                builder.append("<ul>");
                buildListContent(builder, element, linksDeclarations);
                builder.append("</ul>");
            }
            else {
                builder.append(element.getText());
            }
        }
        return builder.toString();
    }

    @RequiredReadAction
    private String buildLinkContent(DlangDocPsiElement element, boolean isImage, Collection<DDocLinkDeclarationImpl> linksDeclarations) {
        StringBuilder builder = new StringBuilder();
        String text = null;
        String referenceTo = null;
        String reference = null;
        String quote = null;
        for (PsiElement children : element.getDescriptionElements()) {
            IElementType elementType = PsiUtilCore.getElementType(children);
            if (elementType == DDOC_LINK_TEXT) {
                text = children.getText().substring(1, children.getText().length() - 1); // text without [ ]
            }
            else if (elementType == DDOC_LINK_REFERENCE_TO) {
                referenceTo = children.getText();
            }
            else if (elementType == DDOC_LINK_INLINE_REFERENCE_TEXT) {
                reference = children.getText().substring(1, children.getText().length() - 1);  // text without ( )
                int quoteIdx = reference.indexOf('\'');
                if (quoteIdx > 0 && quoteIdx < reference.length()) {
                    quote = reference.substring(quoteIdx + 1);
                }
            }
        }
        if (isImage) {
            builder.append("<img src=\"");
            builder.append(reference);
            builder.append("\"");
            if (text != null) {
                builder.append(" alt=\"");
                builder.append(text);
                builder.append("\"");
            }
            builder.append(">");
            builder.append("</img>");
        }
        else {
            if (referenceTo != null) {
                DDocLinkDeclarationImpl target = null;
                for (DDocLinkDeclarationImpl it : linksDeclarations) {
                    if (it.getFirstChild().getText().equals(referenceTo)) {
                        target = it;
                        break;
                    }
                }
                String referenceToText = "";
                for (PsiElement e : element.getDescriptionElements()) {
                    referenceToText += e.getText();
                }
                if (target == null) {
                    // TODO handle reference to code elements
                    builder.append(referenceToText);
                }
                else {
                    String targetText = target.getText();
                    int colon = targetText.indexOf(':');
                    String referencePointer = (colon < 0 ? targetText : targetText.substring(colon + 1)).trim();
                    reference = referencePointer;
                    if (text == null) {
                        text = referenceTo.substring(1, referenceTo.length() - 1); // text without [ ]
                    }
                    addLink(builder, reference, text, quote);
                }
            }
            else {
                addLink(builder, reference, text, quote);
            }
        }
        return builder.toString();
    }

    private void addLink(StringBuilder builder, String reference, String text, String quote) {
        builder.append("<a href=\"");
        builder.append(reference);
        builder.append("\"");
        if (quote != null) {
            builder.append(" title=\"");
            builder.append(quote);
            builder.append("\"");
        }
        builder.append(">");
        builder.append(text != null ? text : reference);
        builder.append("</a>");
    }

    @RequiredReadAction
    private String buildEmbeddedCodeContent(DlangDocPsiElement element) {
        PsiElement[] elements = element.getDescriptionElements();
        StringBuilder rawTextBuilder = new StringBuilder();
        for (PsiElement psiElement : elements) {
            IElementType elementType = PsiUtilCore.getElementType(psiElement);
            String txt;
            if (elementType == DDOC_QUOTE_CHAR) {
                txt = "";
            }
            else if (elementType == DDOC_EMBEDDED_CODE_DELIMITER) {
                txt = "";
            }
            else if (elementType == DDOC_COMMENT_LEADING_ASTERISKS) {
                txt = "";
            }
            else {
                txt = psiElement.getText();
            }
            rawTextBuilder.append(txt);
        }
        String rawText = rawTextBuilder.toString();
        int lineBreak = rawText.indexOf('\n');
        String rawLanguage = (lineBreak < 0 ? "" : rawText.substring(0, lineBreak)).trim();
        String codeSnippet = lineBreak < 0 ? rawText : rawText.substring(lineBreak + 1);
        Language language;
        if (!rawLanguage.isEmpty()) {
            Language byId = Language.findLanguageByID(rawLanguage);
            language = byId != null ? byId : DLanguage.INSTANCE;
        }
        else {
            language = DLanguage.INSTANCE;
        }
        return DDocHtmlUtil.getStyledCodeBlock(element.getProject(), language, codeSnippet);
    }

    @RequiredReadAction
    private void buildListContent(StringBuilder builder, PsiElement element, Collection<DDocLinkDeclarationImpl> linksDeclarations) {
        PsiElement[] children = element.getChildren();
        for (PsiElement child : children) {
            builder.append("<li>");
            builder.append(buildContent(child.getLastChild(), linksDeclarations));
            builder.append("</li>");
        }
    }

    /**
     * {@code text.indexOfFirst { it != c }}
     */
    private static int indexOfFirstNot(String text, char c) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * IntelliJ's {@code PsiElement.childLeafs()} ({@code psiTreeUtil.kt}), which Consulo does not have.
     */
    private static List<PsiElement> childLeafs(PsiElement element) {
        List<PsiElement> leafs = new ArrayList<>();
        PsiElement lastLeaf = PsiTreeUtil.lastChild(element);
        PsiElement leaf = PsiTreeUtil.firstChild(element);
        while (leaf != null) {
            leafs.add(leaf);
            if (leaf == lastLeaf) {
                break;
            }
            leaf = PsiTreeUtil.nextLeaf(leaf);
        }
        return leafs;
    }
}
