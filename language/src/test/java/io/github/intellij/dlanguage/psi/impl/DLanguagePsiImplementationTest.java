package io.github.intellij.dlanguage.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.ast.CompositeElement;
import consulo.test.junit.impl.extension.ConsuloApplicationLoader;
import io.github.intellij.dlanguage.psi.DLanguageAddExpression;
import io.github.intellij.dlanguage.psi.DLanguageAliasAssign;
import io.github.intellij.dlanguage.psi.DLanguageAndExpression;
import io.github.intellij.dlanguage.psi.DlangTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;

/*
* Doesn't do much in the way of testing but at least ensures that our codegen has compiled correctly
* with the interfaces made available from psi-api
*/
@ExtendWith(ConsuloApplicationLoader.class)
public class DLanguagePsiImplementationTest {

    @Test
    @DisplayName("test Add Expression")
    public void testAddExpression() {
        ASTNode node = new CompositeElement(DlangTypes.ADD_EXPRESSION);
        DLanguageAddExpression addExpression = new DLanguageAddExpressionImpl(node);
        assertFalse(addExpression.isValid());
    }

    @Test
    @DisplayName("test Alias Assign")
    public void testAliasAssign() {
        ASTNode node = new CompositeElement(DlangTypes.ALIAS_ASSIGN);
        DLanguageAliasAssign aliasAssign = new DLanguageAliasAssignImpl(node);
        assertFalse(aliasAssign.isValid());
    }

    @Test
    @DisplayName("test And Expression")
    public void testAndExpression() {
        ASTNode node = new CompositeElement(DlangTypes.AND_EXPRESSION);
        DLanguageAndExpression andExpression = new DLanguageAndExpressionImpl(node);
        assertFalse(andExpression.isValid());
    }
}
