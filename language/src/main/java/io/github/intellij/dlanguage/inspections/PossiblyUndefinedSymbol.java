package io.github.intellij.dlanguage.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageDebugCondition;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.DLanguageImportBind;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageReferenceExpression;
import io.github.intellij.dlanguage.psi.DLanguageVersionCondition;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.resolve.DResolveUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@ExtensionImpl
public class PossiblyUndefinedSymbol extends LocalInspectionTool {
    public static class UndefinedSymbolVisitor extends DlangVisitor {
        private final ProblemsHolder myHolder;
        private final Set<String> myObjectDotDContents = Set.copyOf(List.of("string", "size_t", "ptrdiff_t", "_d_newclass", "rt_finalize", "object", "sizediff_t", "hash_t", "equals_t", "wstring", "dstring", "selector", "Object", "toString", "toHash", "opCmp", "opEquals", "Monitor", "lock", "unlock", "factory", "opEquals", "_d_setSameMutex", "setSameMutex", "Interface", "classinfo", "vtbl", "offset", "OffsetTypeInfo", "ti", "TypeInfo", "getHash", "equals", "compare", "tsize", "swap", "next", "initializer", "init", "flags", "offTi", "destroy", "postblit", "talign", "argTypes", "rtInfo", "TypeInfo_Typedef", "base", "name", "m_init", "TypeInfo_Enum", "TypeInfo_Pointer", "m_next", "TypeInfo_Array", "TypeInfo_StaticArray", "value", "len", "TypeInfo_AssociativeArray", "value", "key", "TypeInfo_Vector", "TypeInfo_Function", "deco", "TypeInfo_Delegate", "TypeInfo_Class", "interfaces", "ClassFlags", "classInvariant", "m_flags", "deallocator", "m_offTi", "defaultConstructor", "m_RTInfo", "find", "create", "ClassInfo", "TypeInfo_Interface", "TypeInfo_Struct", "xtoHash", "xopEquals", "xopCmp", "xtoString", "StructFlags", "hasPointers", "isDynamicType", "m_flags", "xdtor", "xdtorti", "xpostblit", "m_align", "TypeInfo_Tuple", "elements", "TypeInfo_Invariant", "TypeInfo_Const", "TypeInfo_Shared", "TypeInfo_Inout", "MIctorstart", "MIctordone", "MIstandalone", "MItlsctor", "MItlsdtor", "MIctor", "MIdtor", "MIxgetMembers", "MIictor", "MIunitTest", "MIimportedModules", "MIlocalClasses", "MIname", "ModuleInfo", "_flags", "_index", "opAssign", "ctor", "dtor", "ictor", "Throwable", "TraceInfo", "Exception", "Error"));//todo this isn't all public symbols in object.d, and this is probably not the best way of detecting if runtime is not configured
        private final Logger myLog = Logger.getInstance(getClass());

        public UndefinedSymbolVisitor(ProblemsHolder holder) {
            myHolder = holder;
        }

        public ProblemsHolder getHolder() {
            return myHolder;
        }

        @Override
        public void visitElement(PsiElement element) {
            long start = System.currentTimeMillis();
            if (element.getReference() == null && element.getReferences().length == 0) {
                return;
            }

            if (DResolveUtil.INSTANCE.shouldNotResolveToAnything(element)) {
                return;
            }
            PsiReference reference = element.getReference();
            if (reference != null) {
                handleReference(reference);
            }
            else {
                for (PsiReference ref : element.getReferences()) {
                    handleReference(ref);
                }
            }
            long end = System.currentTimeMillis();
            if (end - start > 50) {
                myLog.info("resolve took a while" + (end - start));
            }
        }

        private void handleReference(PsiReference reference) {
            PsiElement element = reference.getElement();
            @Nullable Object resolved = reference instanceof PsiPolyVariantReference polyVariantReference
                ? polyVariantReference.multiResolve(false)
                : reference.resolve();
            if (resolved == null && !PossiblyUndefinedSymbolKt.symbolIsDefinedByDefault(element)) {
                if (element instanceof DLanguageIdentifierChain) {
                    DLanguageSingleImport importElt = PsiTreeUtil.getParentOfType(element, DLanguageSingleImport.class);
                    if (importElt != null && importElt.getIdentifierChain() == element) {
                        myHolder.newProblem(DLocalize.dInspectionsSymbolPossiblyundefinedsymbolMessageUnresolvedImport())
                            .range(Objects.requireNonNull(element.getParent()))
                            .highlightType(ProblemHighlightType.ERROR)
                            .create();
                    }
                    // else it’s a package. Packages may not reflect a real folder, it’s fine
                }
                else if (element.getParent() instanceof DLanguageSingleImport) {
                    // Its new name of a renamed import, it’s a new identifier fine
                }
                else if (myObjectDotDContents.contains(element.getText())) {
                    myHolder.newProblem(DLocalize.dInspectionsSymbolPossiblyundefinedsymbolMessageSdkNotSetup())
                        .range(element)
                        .withFixes(new SetupSdkQuickFix(Objects.requireNonNull(element.getContainingFile())))
                        .create();
                }
                else if (element.getParent() instanceof DLanguageVersionCondition) {
                    // If version identifier is not defined, this mean that the version is not enabled
                }
                else if (element.getParent() instanceof DLanguageDebugCondition) {
                    // If version identifier is not defined, this mean that the debug is not enabled
                }
                else {
                    PsiElement elementToRegister = element;
                    if (element instanceof DLanguageQualifiedIdentifier qualifiedIdentifier) {
                        PsiElement identifier = qualifiedIdentifier.getIdentifier();
                        elementToRegister = identifier != null
                            ? identifier
                            : Objects.requireNonNull(Objects.requireNonNull(qualifiedIdentifier.getTemplateInstance()).getIdentifier());
                    }
                    else if (element instanceof DLanguageReferenceExpression referenceExpression) {
                        if (referenceExpression.getDType() != null) {
                            return;
                        }
                        PsiElement identifier = referenceExpression.getIdentifier();
                        elementToRegister = identifier != null
                            ? identifier
                            : Objects.requireNonNull(Objects.requireNonNull(referenceExpression.getTemplateInstance()).getIdentifier());
                    }
                    else if (element instanceof DLanguageImportBind importBind) {
                        PsiElement identifier = importBind.getIdentifier();
                        if (identifier == null) {
                            return;
                        }
                        elementToRegister = identifier;
                    }
                    myHolder.newProblem(DLocalize.dInspectionsSymbolPossiblyundefinedsymbolMessage())
                        .range(elementToRegister)
                        .create(); // todo: add quick fix
                }
            }
        }
    }

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new UndefinedSymbolVisitor(holder);
    }

    @Override
    public LocalizeValue getDescription() {
        return DLocalize.dInspectionsDescriptionPossiblyundefinedsymbol();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.dInspectionsSymbolPossiblyundefinedsymbolDisplayname();
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return DLocalize.inspectionGroupDLanguage();
    }

    @Override
    public LocalizeValue[] getGroupPath() {
        return new LocalizeValue[]{getGroupDisplayName()};
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}
