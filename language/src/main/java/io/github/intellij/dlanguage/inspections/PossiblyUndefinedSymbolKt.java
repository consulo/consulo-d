package io.github.intellij.dlanguage.inspections;

import consulo.language.psi.PsiElement;

/**
 * Created by francis on 7/25/2017.
 * due to string mixins it is very impractical to determine whether something is actually an undefined symbol
 */
public final class PossiblyUndefinedSymbolKt {
    private PossiblyUndefinedSymbolKt() {
    }

    public static boolean symbolIsDefinedByDefault(PsiElement identifier) {
        String name = identifier.getText();
        return name.equals("sizeof") || name.equals("nan") || name.equals("init") || name.equals("mangleof") || name.equals("stringof") || name.equals("alignof") || name.equals("max") || name.equals("min") || name.equals("infinity") || name.equals("dig") || name.equals("epsilon") || name.equals("mant_dig") || name.equals("max_10_exp") || name.equals("max_exp") || name.equals("min_10_exp") || name.equals("min_exp") || name.equals("min_normal") || name.equals("re") || name.equals("im") || name.equals("classinfo");
    }

//fun isSDKSetup(project: Project){
//    ProjectJdkTable.getInstance().allJdks
//}
}
