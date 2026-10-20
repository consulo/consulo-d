import org.jspecify.annotations.NullMarked;

/**
 * D and assembly: the GNU as dialect of the instruction templates of GCC style asm statements and of the assembly files
 * of D modules, and navigation between body-less {@code extern} D functions and the assembly labels implementing them.
 */
@NullMarked
module consulo.d.asm {
    requires consulo.d.base;
    requires consulo.d.language;
    requires consulo.asm.language.api;
    requires consulo.asm.language.impl;
    requires consulo.asm.gas.impl;
    requires consulo.language.editor.api;
    requires consulo.language.editor.ui.api;
    requires consulo.module.content.api;

    exports consulo.d.asm;
}
