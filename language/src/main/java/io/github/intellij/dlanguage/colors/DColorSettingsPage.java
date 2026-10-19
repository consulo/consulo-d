package io.github.intellij.dlanguage.colors;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.colorScheme.setting.ColorDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.localize.LocalizeValue;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.highlighting.DHighlighter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@ExtensionImpl
public class DColorSettingsPage implements ColorSettingsPage {
    private final AttributesDescriptor[] myAttributes = attributes();

    private final Map<String, TextAttributesKey> myAnnotatorTags = annotatorTags();

    private final String myDemoTxt = demoText();

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.of(DLanguage.INSTANCE.getID());
    }

    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return myAttributes;
    }

    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public SyntaxHighlighter getHighlighter() {
        return new DHighlighter();
    }

    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return myAnnotatorTags;
    }

    @Override
    public String getDemoText() {
        return myDemoTxt;
    }

    private static AttributesDescriptor[] attributes() {
        DColor[] colors = DColor.values();
        AttributesDescriptor[] result = new AttributesDescriptor[colors.length];
        for (int i = 0; i < colors.length; i++) {
            result[i] = colors[i].getAttributesDescriptor();
        }
        return result;
    }

    private static Map<String, TextAttributesKey> annotatorTags() {
        Map<String, TextAttributesKey> result = new LinkedHashMap<>();
        for (DColor color : DColor.values()) {
            result.put(color.name(), color.getTextAttributesKey());
        }
        return result;
    }

    private String demoText() {
        try (InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("color_page.d"))) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
