package net.masterthought.dlanguage.psi.interfaces.containers;

import net.masterthought.dlanguage.psi.DLanguageAutoDeclarationPart;
import net.masterthought.dlanguage.psi.DLanguageDeclaratorInitializer;
import net.masterthought.dlanguage.psi.interfaces.VariableDeclaration;

/**
 * Created by francis on 2/28/2017.
 */
public interface LocalVarContainer extends Container {
    Class variableDeclarationClass = VariableDeclaration.class;
    Class autoDeclarationClass = DLanguageAutoDeclarationPart.class;
    Class declaratorInitializer = DLanguageDeclaratorInitializer.class;

}
