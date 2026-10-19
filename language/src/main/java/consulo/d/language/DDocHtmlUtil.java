package consulo.d.language;

import consulo.annotation.access.RequiredReadAction;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.EditorColorsScheme;
import consulo.colorScheme.EffectType;
import consulo.colorScheme.TextAttributes;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.language.lexer.Lexer;
import consulo.project.Project;
import consulo.ui.color.ColorValue;
import consulo.ui.font.Font;
import consulo.ui.util.ColorValueUtil;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The parts of IntelliJ's {@code HtmlSyntaxInfoUtil} and {@code QuickDocHighlightingHelper} the D documentation uses;
 * Consulo has neither. There is no highlighting saturation setting either: colors are used as the scheme defines
 * them, as IntelliJ does with its default saturation for quick documentation (100%).
 */
public final class DDocHtmlUtil {
    public static final String CODE_BLOCK_PREFIX = "<pre><code>";
    public static final String CODE_BLOCK_SUFFIX = "</code></pre>";

    private DDocHtmlUtil() {
    }

    /**
     * {@code HtmlSyntaxInfoUtil.appendStyledSpan(buffer, attributesKey, value, saturationFactor)}: {@code value}
     * (raw HTML) in a span styled as {@code attributesKey} in the global color scheme.
     */
    public static StringBuilder appendStyledSpan(StringBuilder buffer, TextAttributesKey attributesKey, @Nullable String value) {
        TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(attributesKey);
        return appendStyledSpan(buffer, attributes, value);
    }

    private static StringBuilder appendStyledSpan(StringBuilder buffer, @Nullable TextAttributes attributes, @Nullable String value) {
        buffer.append("<span style=\"");
        if (attributes != null) {
            appendStyle(buffer, attributes);
        }
        buffer.append("\">");
        buffer.append(StringUtil.notNullize(value));
        buffer.append("</span>");
        return buffer;
    }

    /**
     * {@code QuickDocHighlightingHelper.getStyledCodeBlock}: {@code code} highlighted by the lexer of {@code language},
     * between {@link #CODE_BLOCK_PREFIX} and {@link #CODE_BLOCK_SUFFIX}. HTML special characters are escaped.
     */
    @RequiredReadAction
    public static String getStyledCodeBlock(Project project, Language language, CharSequence code) {
        StringBuilder builder = new StringBuilder(CODE_BLOCK_PREFIX);
        String processedCode = StringUtil.trimTrailing(StringUtil.trim(code.toString(), ch -> ch != '\n' && ch != '\r')
            .replace(' ', ' '));
        String text = trimIndent(processedCode);

        SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(language, project, null);
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        Lexer lexer = highlighter.getHighlightingLexer();
        lexer.start(text);
        IElementType tokenType;
        while ((tokenType = lexer.getTokenType()) != null) {
            String tokenText = StringUtil.escapeXmlEntities(text.substring(lexer.getTokenStart(), lexer.getTokenEnd()));
            TextAttributes attributes = null;
            for (TextAttributesKey key : highlighter.getTokenHighlights(tokenType)) {
                attributes = TextAttributes.merge(attributes, scheme.getAttributes(key));
            }
            if (attributes == null || attributes.isEmpty()) {
                builder.append(tokenText);
            }
            else {
                appendStyledSpan(builder, attributes, tokenText);
            }
            lexer.advance();
        }
        builder.append(CODE_BLOCK_SUFFIX);
        return builder.toString();
    }

    private static void appendStyle(StringBuilder style, TextAttributes attributes) {
        ColorValue foregroundColor = attributes.getForegroundColor();
        ColorValue backgroundColor = attributes.getBackgroundColor();
        ColorValue effectTypeColor = attributes.getEffectColor();

        if (foregroundColor != null) {
            appendProperty(style, "color", toHtmlColor(foregroundColor));
        }
        if (backgroundColor != null) {
            appendProperty(style, "background-color", toHtmlColor(backgroundColor));
        }

        switch (attributes.getFontType()) {
            case Font.BOLD -> appendProperty(style, "font-weight", "bold");
            case Font.ITALIC -> appendProperty(style, "font-style", "italic");
            default -> {
            }
        }

        EffectType effectType = attributes.getEffectType();
        if (attributes.hasEffects() && effectType != null) {
            switch (effectType) {
                case LINE_UNDERSCORE -> appendProperty(style, "text-decoration-line", "underline");
                case WAVE_UNDERSCORE -> {
                    appendProperty(style, "text-decoration-line", "underline");
                    appendProperty(style, "text-decoration-style", "wavy");
                }
                case BOLD_LINE_UNDERSCORE -> {
                    appendProperty(style, "text-decoration-line", "underline");
                    appendProperty(style, "text-decoration-thickness", "2px");
                }
                case BOLD_DOTTED_LINE -> {
                    appendProperty(style, "text-decoration-line", "underline");
                    appendProperty(style, "text-decoration-thickness", "2px");
                    appendProperty(style, "text-decoration-style", "dotted");
                }
                case STRIKEOUT -> appendProperty(style, "text-decoration-line", "line-through");
                case BOXED, SLIGHTLY_WIDER_BOX, SEARCH_MATCH -> appendProperty(style, "border", "1px solid");
                case ROUNDED_BOX -> {
                    appendProperty(style, "border", "1px solid");
                    appendProperty(style, "border-radius", "2px");
                }
                default -> {
                }
            }

            if (effectTypeColor != null) {
                switch (effectType) {
                    case LINE_UNDERSCORE, WAVE_UNDERSCORE, BOLD_LINE_UNDERSCORE, BOLD_DOTTED_LINE, STRIKEOUT ->
                        appendProperty(style, "text-decoration-color", toHtmlColor(effectTypeColor));
                    case BOXED, ROUNDED_BOX, SEARCH_MATCH, SLIGHTLY_WIDER_BOX ->
                        appendProperty(style, "border-color", toHtmlColor(effectTypeColor));
                    default -> {
                    }
                }
            }
        }
    }

    private static void appendProperty(StringBuilder builder, String name, String value) {
        builder.append(name);
        builder.append(":");
        builder.append(value);
        builder.append(";");
    }

    private static String toHtmlColor(ColorValue color) {
        return "#" + ColorValueUtil.toHex(color);
    }

    /**
     * Kotlin's {@code String.trimIndent()}: removes the common minimal indent of the lines, and the first and the last
     * lines when they are blank.
     */
    private static String trimIndent(String text) {
        List<String> lines = new ArrayList<>(List.of(text.split("\n", -1)));
        int minIndent = Integer.MAX_VALUE;
        for (String line : lines) {
            if (!line.isBlank()) {
                int indent = 0;
                while (indent < line.length() && Character.isWhitespace(line.charAt(indent))) {
                    indent++;
                }
                minIndent = Math.min(minIndent, indent);
            }
        }
        if (!lines.isEmpty() && lines.get(0).isBlank()) {
            lines.remove(0);
        }
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
        int indent = minIndent == Integer.MAX_VALUE ? 0 : minIndent;
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(line.isBlank() ? "" : line.substring(Math.min(indent, line.length())));
        }
        return String.join("\n", result);
    }
}
