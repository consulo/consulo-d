package io.github.intellij.dlanguage.parser;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiParser;
import consulo.language.version.LanguageVersion;

/**
 * Created by francis on 7/2/2017.
 */
public class ParserWrapper implements PsiParser {
    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder, LanguageVersion languageVersion) {
        DLangParser parser = new DLangParser(builder);
        PsiBuilder.Marker m = builder.mark();
        parser.parseModule();
        m.done(root);
        return builder.getTreeBuilt();
    }
}
