package io.github.intellij.dlanguage.documentation;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.ILazyParseableElementType;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.version.LanguageVersion;
import consulo.language.version.LanguageVersionUtil;
import consulo.project.Project;
import io.github.intellij.dlanguage.DLangParserDefinition;
import io.github.intellij.dlanguage.documentation.psi.DlangDocTypes;
import io.github.intellij.dlanguage.documentation.psi.impl.DlangDocCommentImpl;
import org.jspecify.annotations.Nullable;

public class DlangDocCommentType extends ILazyParseableElementType {
    public DlangDocCommentType(String debugName) {
        super(debugName, DDocLanguage.INSTANCE);
    }

    @Override
    @RequiredReadAction
    protected @Nullable ASTNode doParseContents(ASTNode chameleon, PsiElement psi) {
        Project project = psi.getProject();
        Language language = getLanguage();
        ParserDefinition parserDefinition = ParserDefinition.forLanguage(project.getApplication(), language);

        // Happens in parser test where the DDoc language definition is not injected
        // We don’t want to parse the comments in this case anyway, the comments parsing
        // is done in another tests.
        // Consulo returns no parser definition at all (instead of the base language one) when DDoc's is missing.
        if (parserDefinition == null || parserDefinition instanceof DLangParserDefinition) {
            DlangDocCommentImpl root = new DlangDocCommentImpl(this, null);
            root.rawAddChildrenWithoutNotifications(new LeafPsiElement(DlangDocTypes.INSTANCE.getDocDataType(), chameleon.getChars()));
            return root.getFirstChildNode();
        }

        LanguageVersion languageVersion = LanguageVersionUtil.findDefaultVersion(language);
        PsiBuilder builder = PsiBuilderFactory.getInstance()
            .createBuilder(
                project,
                chameleon,
                parserDefinition.createLexer(languageVersion),
                language,
                languageVersion,
                chameleon.getText()
            );

        return parserDefinition
            .createParser(languageVersion)
            .parse(this, builder, languageVersion)
            .getFirstChildNode();
    }

    @Override
    public ASTNode createNode(CharSequence text) {
        return new DlangDocCommentImpl(this, text);
    }

    @Override
    public String toString() {
        return "DlangTokenType." + super.toString();
    }
}
