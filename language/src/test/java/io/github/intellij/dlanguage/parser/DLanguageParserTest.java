package io.github.intellij.dlanguage.parser;

import org.junit.jupiter.api.Test;

public class DLanguageParserTest extends DLanguageParserTestBase {
    public DLanguageParserTest() {
        super("parser", "d");
    }
    /**
     * Checks the shebang.d file in /parser directory. File content is:
     * '#!/usr/bin/env rdmd -unittest -Isrc'
     */
    @Test
    public void testshebang(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // modules
    @Test
    public void testmodule1(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testmodule2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testmodule_deprecated1(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testmodule_deprecated2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testmodule_scope(Context context) throws Exception {
        doDlangParserTest(context);
    }

    @Test
    public void testimport(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // declarations
    @Test
    public void testdeclaration1(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_alias(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_alias2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_anonymous_function_call(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_symbols(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_type_storage2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_type_storage1(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_alias3(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_alias4(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_extern(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_typeof2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_void(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_storage(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_type_storage3(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_type_storage4(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_alias_symbol(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_type_inference(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_array_literal(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_type_qualifier(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_typeof3(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_typeof(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_static_foreach(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_strings(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_vector(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testdeclaration_block_with_invalid_function_declaration(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }

    //  types
    @Test
    public void testtype_arithmetic_conversion(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtype_implicit_conversion(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtype_delegates(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtype_delegates2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtype_delegates3(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtype_delegates4(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtype_c_style(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtype_expression_with_array_indexing(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // documentation
    @Test
    public void testdocumentation_attached_declaration(Context context) throws Exception {
        doDlangParserTest(context);
    }

    //  properties
    @Test
    public void testproperties_init(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testproperties_init2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testproperties_init3(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testproperties_user_defined(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testproperties_stringof(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testproperties_sizeof(Context context) throws Exception {
        doDlangParserTest(context);
    }

    //  attributes
    @Test
    public void testattributes_align(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_auto(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_const(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_deprecated(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_deprecated_optional(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_disabled(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_extern(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_gshared(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_namespaces(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_override(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_private(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_protection(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_static(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_traits(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_namespaces_multiple(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_user_defined(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testattributes_nogc(Context context) throws Exception {
        doDlangParserTest(context);
    }

    //  pragmas
    @Test
    public void testpragmas(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testpragmas_inline(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testpragmas_methods(Context context) throws Exception {
        doDlangParserTest(context);
    }

    //  expressions
    @Test
    public void testexpressions_array_literal(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_assert(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_associative_array_literal(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_cast(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_cast2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_cast_float(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_cast_pointer(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_is(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_lambda(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_lambda_auto_ref(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_new(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_class_comparison(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_equality(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_function_literal(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_function_literal2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_function_literal3(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_function_literal_parameterless(Context context) throws Exception {
        doDlangParserTest(context);
    } // issue #1174
    @Test
    public void testexpressions_ies_string(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_import(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_in(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_mixin(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_named_parameters(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_operators(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_precedence(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_scalar(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_slice(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_string_literal(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_this(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_throw(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testexpressions_type_info(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // asm
    @Test
    public void testasm(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // statements
    @Test
    public void teststatements_asm(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_asm_inline(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_asm_gcc(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_associative_array(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_delegates(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_templated(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_tuples(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_switch(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_switch2(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_switch_unterminated_case(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }
    @Test
    public void teststatements_block(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_break(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_declaration(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_declarator(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_do(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_expression(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_for(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_invalid(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }
    @Test
    public void teststatements_foreach_aggregates(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_array(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_range(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_alias_params(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_alias_params_invalid(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }
    @Test
    public void teststatements_foreach_ref_params(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_scoped_params(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_reverse(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_foreach_types(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_goto(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_mixin(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_pragma(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_return(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_scope(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_synchronised(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_if(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_while(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_while_condition_assignment(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_with(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststatements_unterminated_if(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }

    // arrays
    @Test
    public void testarrays(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_concatenation(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_copying(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_declarations(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_dynamic(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_function_properties(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_bounds_checking(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_dynamic_length(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_initialization(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_rectangular(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_length(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_operations(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_pointer_arithmetic(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_pointers(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_printf_c(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_properties(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_setting(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_slicing(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_special_strings(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_static(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testarrays_void(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // associative arrays
    @Test
    public void testassocarrays(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_literal(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_classes(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_constructor(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_removing(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_immutable(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_membership(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_structs(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_word_count(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testassocarrays_invalid_syntax(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }

    // structs and unions
    @Test
    public void teststructs(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststructs_constructors(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststructs_dynamic_init(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststructs_initialization(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststructs_literal(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststructs_nested(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststructs_overload(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testunion_initialization(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststructs_postblits(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststructs_shared(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // classes
    @Test
    public void testclasses(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_alias_this(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_constructors_fields(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_nested(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_properties(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_allocators(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_constructors(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_deallocators(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_destructors(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_field_properties(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_fields(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_final(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_members(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_scope(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_static_constructors(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_static_destructor(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testclasses_synchronized(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // interfaces
    @Test
    public void testinterfaces(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testinterfaces_com(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testinterfaces_contracts(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testinterfaces_cplus(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // enums
    @Test
    public void testenums(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testenums_errors(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }
    @Test
    public void testenums_error_unfinished(Context context) throws Exception {
        doDlangParserTest(context, false, false);
    }

    // immutable const
    @Test
    public void testconst_immutable(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // functions
    @Test
    public void testfunctions_auto(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_auto_ref(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_default_args(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_inheritance(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_nested(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_optional_parens(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_overloading(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_templates(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_ucfs(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_delegated_fp_closures(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_inout(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_local_static(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_local_vars(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_overload_sets(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_parameters(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_properties(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_pure(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_ref(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_shortened_body(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_string_mixin(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_variadic(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_variadic_d(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_variadic_lazy(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_variadic_typesafe(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testfunctions_virtual(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // operator overloading
    @Test
    public void testoperator_overloading(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // templates
    @Test
    public void testtemplates(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_alias(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_alias_assign(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_defaults(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_function_templates(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_alias_params(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_argument_deduction(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_instant_scope(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_limitation(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_recursive(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_specialization(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_typed_params(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_mixins(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_mixins_scope(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_nested(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_params(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_ref(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_this_params(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_tuples(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_typed_specialization(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_variable(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtemplates_variable_declaration(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // contract programming
    @Test
    public void testcontract_programming(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // conditional compilation
    @Test
    public void testconditional_compilation(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // traits
    @Test
    public void testtraits_allmembers(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_derivedmembers(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_getfunctionattributes(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_getpointerbitmap(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_getprotection(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_isabstractionfunction(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_isarithmetic(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_isfinalfunction(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_isrefoutlazy(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_isvirtualmethod(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_compiles(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_getmember(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_getoverloads(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_getvirtualmethods(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_hasmember(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_isabstractclass(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_isoverridefunction(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_issame(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_istemplate(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_getunittests(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_special_keywords(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testtraits_getattributes(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // types
    @Test
    public void testtype_mixin(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // unittest
    @Test
    public void testunittests(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // bug fixes
    @Test
    public void testbitwise_or_bug(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testbug_assoc_array(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testbug_try_catch(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testbug_fixes(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void teststaticifbug(Context context) throws Exception {
        doDlangParserTest(context);
    }
    @Test
    public void testcollection_of_bugs(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // standard library
    @Test
    public void teststdio(Context context) throws Exception {
        doDlangParserTest(context, false, true);
    }
    @Test
    public void testtraits(Context context) throws Exception {
        doDlangParserTest(context, false, true);
    }
    @Test
    public void teststring(Context context) throws Exception {
        doDlangParserTest(context, false, true);
    }
    @Test
    public void testbase64(Context context) throws Exception {
        doDlangParserTest(context, false, true);
    }
    @Test
    public void testarray(Context context) throws Exception {
        doDlangParserTest(context, false, true);
    }
    @Test
    public void testtypecons(Context context) throws Exception {
        doDlangParserTest(context, false, true);
    }
    @Test
    public void testparallelism(Context context) throws Exception {
        doDlangParserTest(context, false, true);
    }

    //dmd source
    @Test
    public void testexpression(Context context) throws Exception {
        doDlangParserTest(context);
    }

    // dmd fail_compilation test file
    @Test
    public void teste15876_1(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }

    // libdparse incompleteStatement198_1.d test file
    @Test
    public void testincompleteStatement198_1(Context context) throws Exception {
        doDlangParserTest(context, true, false);
    }
    @Test
    public void testkiller(Context context) throws Exception {
        doDlangParserTest(context, false, false);
    }
    @Test
    public void testkiller2(Context context) throws Exception {
        doDlangParserTest(context, false, false);
    }
    @Test
    public void testobject(Context context) throws Exception {
        doDlangParserTest(context);
    }
}
