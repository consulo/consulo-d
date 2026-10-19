package io.github.intellij.dlanguage.documentation.psi;

public final class DDocMetaElementTypes {
    public static final DDocMetaElementTypes INSTANCE = new DDocMetaElementTypes();

    public static final DlangDocElementType DDOC_COMMENT_LEADING_ASTERISKS = new DlangDocElementType("DDOC_COMMENT_LEADING_ASTERISKS");
    public static final DlangDocElementType DDOC_COMMENT_START = new DlangDocElementType("DDOC_COMMENT_START");
    public static final DlangDocElementType DDOC_COMMENT_END = new DlangDocElementType("DDOC_COMMENT_END");

    private DDocMetaElementTypes() {
    }
}
