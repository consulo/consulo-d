package io.github.intellij.dlanguage.resolve.processors.parameters;

import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageAtAttribute;
import io.github.intellij.dlanguage.psi.DLanguageAttribute;
import io.github.intellij.dlanguage.psi.DLanguageAttributeSpecifier;
import io.github.intellij.dlanguage.psi.DLanguageAutoDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageIfCondition;
import io.github.intellij.dlanguage.psi.DLanguageImportBindings;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageLabeledStatement;
import io.github.intellij.dlanguage.psi.DLanguageMemberFunctionAttribute;
import io.github.intellij.dlanguage.psi.DLanguageNamedImportBind;
import io.github.intellij.dlanguage.psi.DLanguageParameterAttribute;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageSpecifiedVariableDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageStorageClass;
import io.github.intellij.dlanguage.psi.DLanguageStructBody;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.DLanguageUnittest;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.FunctionBody;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.psi.named.DLanguageCatch;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.psi.named.DLanguageForeachType;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageVersionSpecification;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Created by francis on 7/17/2017.
 */
public class DAttributesFinder {

    private final PsiElement myStartingPoint;

    private final DirectApplication myDirectApplication;

    private final BulkAttributeApplication myBulkAttributeApplied = new BulkAttributeApplication();

    private final DefaultAttributes myDefualts;

    public DAttributesFinder(PsiElement startingPoint) {
        myStartingPoint = startingPoint;

        DNamedElement elem;
        if (!(startingPoint instanceof DNamedElement)) {
            PsiElement parentNamedElement = PsiTreeUtil.findFirstParent(startingPoint, it -> it instanceof DNamedElement);
            if (parentNamedElement == null) {
                throw new IllegalArgumentException("Asked for attributes of something that isn't a DNamedElement, or the child of a DNamedElement");
            }
            elem = (DNamedElement) parentNamedElement;
        }
        else {
            elem = (DNamedElement) startingPoint;
        }
        if (elem instanceof DLanguageConstructor constructor) {
            myDefualts = defaultConstructor(constructor);
            myDirectApplication = handleConstructor(constructor);
        }
        else if (elem instanceof DLanguageFunctionDeclaration functionDeclaration) {
            myDefualts = defaultFunctionDeclaration(functionDeclaration);
            myDirectApplication = handleFunctionDeclaration(functionDeclaration);
        }
        else if (elem instanceof DLanguageClassDeclaration classDeclaration) {
            myDefualts = defaultClassDeclaration(classDeclaration);
            myDirectApplication = handleClassDeclaration(classDeclaration);
        }
        else if (elem instanceof DLanguageInterfaceDeclaration interfaceDeclaration) {
            myDefualts = defaultInterfaceDeclaration(interfaceDeclaration);
            myDirectApplication = handleInterfaceDeclaration(interfaceDeclaration);
        }
        else if (elem instanceof DLanguageUnionDeclaration unionDeclaration) {
            myDefualts = defaultUnionDeclaration(unionDeclaration);
            myDirectApplication = handleUnionDeclaration(unionDeclaration);
        }
        else if (elem instanceof DLanguageStructDeclaration structDeclaration) {
            myDefualts = defaultStructDeclaration(structDeclaration);
            myDirectApplication = handleStructDeclaration(structDeclaration);
        }
        else if (elem instanceof DLanguageLabeledStatement labeledStatement) {
            myDefualts = defaultLabeledStatement(labeledStatement);
            myDirectApplication = handleLabeledStatement(labeledStatement);
        }
        else if (elem instanceof DLanguageAutoAssignment autoAssignment) {
            myDefualts = defaultAutoDeclarationPart(autoAssignment);
            myDirectApplication = handleAutoDeclarationPart(autoAssignment);
        }
        else if (elem instanceof DLanguageEnumDeclaration enumDeclaration) {
            myDefualts = defaultEnumDeclaration(enumDeclaration);
            myDirectApplication = handleEnumDeclaration(enumDeclaration);
        }
        else if (elem instanceof DLanguageCatch aCatch) {
            myDefualts = defaultCatch(aCatch);
            myDirectApplication = handleCatch(aCatch);
        }
        else if (elem instanceof DLanguageIdentifierInitializer identifierInitializer) {
            myDefualts = defaultIdentifierInitializer(identifierInitializer);
            myDirectApplication = handleIdentifierInitializer(identifierInitializer);
        }
        else if (elem instanceof DLanguageForeachType foreachType) {
            myDefualts = defaultForeachType(foreachType);
            myDirectApplication = handleForeachType(foreachType);
        }
        else if (elem instanceof DLanguageIfCondition ifCondition) {
            myDefualts = defaultIfCondition(ifCondition);
            myDirectApplication = handleIfCondition(ifCondition);
        }
        else if (elem instanceof DLanguageTemplateDeclaration templateDeclaration) {
            myDefualts = defaultTemplateDeclaration(templateDeclaration);
            myDirectApplication = handleTemplateDeclaration(templateDeclaration);
        }
        else if (elem instanceof DLanguageSingleImport singleImport) {
            myDefualts = defaultSingleImport(singleImport);
            myDirectApplication = handleSingleImport(singleImport);
        }
        else if (elem instanceof DLanguageParameter parameter) {
            myDefualts = defaultParameter(parameter);
            myDirectApplication = handleParameter(parameter);
        }
        else if (elem instanceof DLanguageModuleDeclaration moduleDeclaration) {
            myDefualts = defaultModuleDecl(moduleDeclaration);
            myDirectApplication = handleModuleDecl(moduleDeclaration);
        }
        else if (elem instanceof DLanguageAliasInitializer aliasInitializer) {
            myDefualts = defaultAliasInit(aliasInitializer);
            myDirectApplication = handleAliasInit(aliasInitializer);
        }
        else if (elem instanceof DLanguageEnumMember enumMember) {
            myDefualts = defaultEnumMember(enumMember);
            myDirectApplication = handleEnumMember(enumMember);
        }
        else if (elem instanceof DLanguageNamedImportBind namedImportBind) {
            myDefualts = defaultNamedImportBind(namedImportBind);
            myDirectApplication = handleNamedImportBind(namedImportBind);
        }
        else if (elem instanceof DLanguageVersionSpecification versionSpecification) {
            myDefualts = defaultVersionSpecifcation(versionSpecification);
            myDirectApplication = handleVersionSpecification(versionSpecification);
        }
        else {
            throw new IllegalArgumentException("bad type sent to AttributesFinder");
        }
        recurseUp();

    }

    public PsiElement getStartingPoint() {
        return myStartingPoint;
    }

    private DirectApplication handleVersionSpecification(DLanguageVersionSpecification elem) {
        return new DirectApplication(null, Visibility.PUBLIC, null, null, null, null, null, null, null, null);
    }

    private DefaultAttributes defaultVersionSpecifcation(DLanguageVersionSpecification elem) {
        return new DefaultAttributes();
    }

    private DirectApplication handleNamedImportBind(DLanguageNamedImportBind elem) {
        return new DirectApplication();//perhaps check if private/public on mport also applies to this.
    }

    private DefaultAttributes defaultNamedImportBind(DLanguageNamedImportBind elem) {
        return new DefaultAttributes();
    }

    private DirectApplication handleEnumMember(DLanguageEnumMember elem) {
        return new DirectApplication();
    }

    private DefaultAttributes defaultEnumMember(DLanguageEnumMember elem) {
        return new DefaultAttributes(
            /*static*/ false,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ true,
            /*immutable*/ true,
            /*enum*/ false
        );
    }

    private DirectApplication handleAliasInit(DLanguageAliasInitializer aliasInit) {
        DLanguageAliasDeclaration aliasDecl = (DLanguageAliasDeclaration) Objects.requireNonNull(aliasInit.getParent());
        DirectApplication attribs = updateFromParentDecl(aliasDecl);
        for (DLanguageStorageClass storageClasss : aliasInit.getStorageClasss()) {
            updateFromStorageClass(storageClasss, attribs);
        }
        for (DLanguageStorageClass storageClasss : aliasDecl.getStorageClasss()) {
            updateFromStorageClass(storageClasss, attribs);
        }
        return attribs;
    }

    private DefaultAttributes defaultAliasInit(DLanguageAliasInitializer startingPoint) {
        return new DefaultAttributes();
    }

    private DirectApplication handleModuleDecl(DLanguageModuleDeclaration startingPoint) {
        return new DirectApplication();
    }

    private DefaultAttributes defaultModuleDecl(DLanguageModuleDeclaration startingPoint) {
        return new DefaultAttributes();
    }

    private DefaultAttributes defaultParameter(DLanguageParameter parameter) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.LOCAL,
            /*property*/ false,
            /*noGC*/ false,
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    private DirectApplication handleParameter(DLanguageParameter param) {
        DirectApplication attributes = new DirectApplication();
        for (DLanguageParameterAttribute attribute : param.getParameterAttributes()) {
//            attribute.kW_AUTO
//            attribute.kW_FINAL
//            attribute.kW_IN
//            attribute.kW_LAZY
//            attribute.kW_OUT
//            attribute.kW_REF
//            attribute.kW_SCOPE
        }
        return attributes;
    }

    private DirectApplication handleSingleImport(DLanguageSingleImport startingPoint) {
        Declaration decl;
        if (startingPoint.getParent() instanceof DLanguageImportDeclaration importDeclaration) {
            decl = importDeclaration;
        }
        else {
            DLanguageImportBindings bind = (DLanguageImportBindings) Objects.requireNonNull(startingPoint.getParent());
            decl = (DLanguageImportDeclaration) Objects.requireNonNull(bind.getParent());
        }
        return updateFromParentDecl(decl);
    }

    public DefaultAttributes defaultSingleImport(DLanguageSingleImport startingPoint) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.PRIVATE,
            /*property*/ false,
            /*noGC*/ false,
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public enum Visibility {
        PUBLIC, PRIVATE, PROTECTED, LOCAL;

        public void write(StubOutputStream stream) throws IOException {
            switch (this) {
                case PUBLIC -> stream.writeInt(1);
                case PRIVATE -> stream.writeInt(2);
                case PROTECTED -> stream.writeInt(3);
                case LOCAL -> stream.writeInt(4);
            }
        }

        public static Visibility read(StubInputStream stream) throws IOException {
            switch (stream.readInt()) {
                case 1:
                    return PUBLIC;
                case 2:
                    return PRIVATE;
                case 3:
                    return PROTECTED;
                case 4:
                    return LOCAL;
            }
            throw new IOException("read illegal int when deserializing Visibility");
        }
    }

    public DirectApplication getDirectApplication() {
        return myDirectApplication;
    }

    public static class DirectApplication {
        private @Nullable Boolean myStatic;
        private @Nullable Visibility myVisibility;
        private @Nullable Boolean myProperty;
        private @Nullable Boolean myNoGC;
        private @Nullable Boolean myExtern;
        private @Nullable Boolean myPure;
        private @Nullable Boolean myNothrow;
        private @Nullable Boolean myConst;
        private @Nullable Boolean myImmutable;
        private @Nullable Boolean myEnum;//enum refers to compile time not enum type

        public DirectApplication() {
        }

        public DirectApplication(@Nullable Boolean isStatic,
                                 @Nullable Visibility visibility,
                                 @Nullable Boolean property,
                                 @Nullable Boolean noGC,
                                 @Nullable Boolean extern,
                                 @Nullable Boolean pure,
                                 @Nullable Boolean nothrow,
                                 @Nullable Boolean isConst,
                                 @Nullable Boolean immutable,
                                 @Nullable Boolean isEnum) {
            myStatic = isStatic;
            myVisibility = visibility;
            myProperty = property;
            myNoGC = noGC;
            myExtern = extern;
            myPure = pure;
            myNothrow = nothrow;
            myConst = isConst;
            myImmutable = immutable;
            myEnum = isEnum;
        }

        public @Nullable Boolean getStatic() {
            return myStatic;
        }

        public void setStatic(@Nullable Boolean isStatic) {
            myStatic = isStatic;
        }

        public @Nullable Visibility getVisibility() {
            return myVisibility;
        }

        public void setVisibility(@Nullable Visibility visibility) {
            myVisibility = visibility;
        }

        public @Nullable Boolean getProperty() {
            return myProperty;
        }

        public void setProperty(@Nullable Boolean property) {
            myProperty = property;
        }

        public @Nullable Boolean getNoGC() {
            return myNoGC;
        }

        public void setNoGC(@Nullable Boolean noGC) {
            myNoGC = noGC;
        }

        public @Nullable Boolean getExtern() {
            return myExtern;
        }

        public void setExtern(@Nullable Boolean extern) {
            myExtern = extern;
        }

        public @Nullable Boolean getPure() {
            return myPure;
        }

        public void setPure(@Nullable Boolean pure) {
            myPure = pure;
        }

        public @Nullable Boolean getNothrow() {
            return myNothrow;
        }

        public void setNothrow(@Nullable Boolean nothrow) {
            myNothrow = nothrow;
        }

        public @Nullable Boolean getConst() {
            return myConst;
        }

        public void setConst(@Nullable Boolean isConst) {
            myConst = isConst;
        }

        public @Nullable Boolean getImmutable() {
            return myImmutable;
        }

        public void setImmutable(@Nullable Boolean immutable) {
            myImmutable = immutable;
        }

        public @Nullable Boolean getEnum() {
            return myEnum;
        }

        public void setEnum(@Nullable Boolean isEnum) {
            myEnum = isEnum;
        }
    }

    public BulkAttributeApplication getBulkAttributeApplied() {
        return myBulkAttributeApplied;
    }

    public static class BulkAttributeApplication {
        private @Nullable Boolean myStatic;
        private @Nullable Visibility myVisibility;
        private @Nullable Boolean myProperty;
        private @Nullable Boolean myNoGC;
        private @Nullable Boolean myExtern;
        private @Nullable Boolean myPure;
        private @Nullable Boolean myNothrow;
        private @Nullable Boolean myConst;
        private @Nullable Boolean myImmutable;
        /*var enum: Boolean? = null*/

        public BulkAttributeApplication() {
        }

        public @Nullable Boolean getStatic() {
            return myStatic;
        }

        public void setStatic(@Nullable Boolean isStatic) {
            myStatic = isStatic;
        }

        public @Nullable Visibility getVisibility() {
            return myVisibility;
        }

        public void setVisibility(@Nullable Visibility visibility) {
            myVisibility = visibility;
        }

        public @Nullable Boolean getProperty() {
            return myProperty;
        }

        public void setProperty(@Nullable Boolean property) {
            myProperty = property;
        }

        public @Nullable Boolean getNoGC() {
            return myNoGC;
        }

        public void setNoGC(@Nullable Boolean noGC) {
            myNoGC = noGC;
        }

        public @Nullable Boolean getExtern() {
            return myExtern;
        }

        public void setExtern(@Nullable Boolean extern) {
            myExtern = extern;
        }

        public @Nullable Boolean getPure() {
            return myPure;
        }

        public void setPure(@Nullable Boolean pure) {
            myPure = pure;
        }

        public @Nullable Boolean getNothrow() {
            return myNothrow;
        }

        public void setNothrow(@Nullable Boolean nothrow) {
            myNothrow = nothrow;
        }

        public @Nullable Boolean getConst() {
            return myConst;
        }

        public void setConst(@Nullable Boolean isConst) {
            myConst = isConst;
        }

        public @Nullable Boolean getImmutable() {
            return myImmutable;
        }

        public void setImmutable(@Nullable Boolean immutable) {
            myImmutable = immutable;
        }
    }

    public DefaultAttributes getDefualts() {
        return myDefualts;
    }

    public static class DefaultAttributes {
        private final boolean myStatic;
        private final Visibility myVisibility;
        private final boolean myProperty;
        private final boolean myNoGC;
        private final boolean myExtern;
        private final boolean myPure;
        private final boolean myNothrow;
        private final boolean myConst;
        private final boolean myImmutable;
        private final boolean myEnum;

        public DefaultAttributes() {
            this(true, Visibility.PUBLIC, false, false, false, false, false, false, false, false);
        }

        public DefaultAttributes(boolean isStatic,
                                 Visibility visibility,
                                 boolean property,
                                 boolean noGC,
                                 boolean extern,
                                 boolean pure,
                                 boolean nothrow,
                                 boolean isConst,
                                 boolean immutable,
                                 boolean isEnum) {
            myStatic = isStatic;
            myVisibility = visibility;
            myProperty = property;
            myNoGC = noGC;
            myExtern = extern;
            myPure = pure;
            myNothrow = nothrow;
            myConst = isConst;
            myImmutable = immutable;
            myEnum = isEnum;
        }

        public boolean getStatic() {
            return myStatic;
        }

        public Visibility getVisibility() {
            return myVisibility;
        }

        public boolean getProperty() {
            return myProperty;
        }

        public boolean getNoGC() {
            return myNoGC;
        }

        public boolean getExtern() {
            return myExtern;
        }

        public boolean getPure() {
            return myPure;
        }

        public boolean getNothrow() {
            return myNothrow;
        }

        public boolean getConst() {
            return myConst;
        }

        public boolean getImmutable() {
            return myImmutable;
        }

        public boolean getEnum() {
            return myEnum;
        }
    }


    public void recurseUp() {
        // make sure that the starting point is an identifier

        recurseUpImpl(myStartingPoint);
    }

    private void recurseUpImpl(PsiElement startingPoint) {
        PsiElement point = startingPoint;
        while (true) {
            while (true) {
                if (!execute(point)) {
                    return;
                }
                PsiElement prevSibling = point.getPrevSibling();
                if (point instanceof DlangPsiFile || prevSibling == null) {
                    break;
                }
                point = prevSibling;
            }
            PsiElement parent = point.getParent();
            if (parent == null || point instanceof PsiFile || point instanceof PsiDirectory) {
                break;
            }
            point = parent;
        }
    }

    public boolean isParent(PsiElement parent, PsiElement child) {
        if (child.equals(parent)) {
            return true;
        }
        if (child instanceof DlangPsiFile) {
            return false;
        }
        return isParent(parent, Objects.requireNonNull(child.getParent()));
    }

    public boolean execute(PsiElement element) {
        if (element instanceof DLanguageSingleImport && isParent(element, myStartingPoint)) {
            myBulkAttributeApplied.setStatic(false);
        }
        if (element instanceof DLanguageFunctionDeclaration || element instanceof DLanguageUnittest || element instanceof DLanguageParameters || element instanceof DLanguageTemplateParameters) {
            if (element instanceof DLanguageFunctionDeclaration functionDeclaration) {
                FunctionBody functionBody = functionDeclaration.getFunctionBody();
                DLanguageParameters parameters = functionDeclaration.getParameters();
                DLanguageTemplateParameters templateParameters = functionDeclaration.getTemplateParameters();
                if ((functionBody != null && isParent(functionBody, myStartingPoint))
                    || (parameters != null && isParent(parameters, myStartingPoint))
                    || (templateParameters != null && isParent(templateParameters, myStartingPoint))) {
                    if (myBulkAttributeApplied.getVisibility() == null) {
                        myBulkAttributeApplied.setVisibility(Visibility.LOCAL);
                    }
                    return false;
                }
            }
            return true;
        }
        if (element instanceof DLanguageStructBody) {
            if (isParent(element, myStartingPoint)) {
                myBulkAttributeApplied.setStatic(false);
                return false;
            }
        }
        if (element instanceof DLanguageAttribute attribute) {
            updateFromAttribute(attribute);
        }
        if (element instanceof DLanguageAttributeSpecifier attributeSpecifier) {
            updateFromAttribute(Objects.requireNonNull(attributeSpecifier.getAttribute()));
        }
        return true;
    }

    public void updateFromAttribute(DLanguageAttribute attribute) {
        if (attribute.getTextOffset() < myStartingPoint.getTextOffset()) {

            if (attribute.getKW_EXPORT() != null) {
            }
            else if (attribute.getKW_PACKAGE() != null) {
            }
            else if (attribute.getKW_PRIVATE() != null) {
                if (myBulkAttributeApplied.getVisibility() == null) {
                    myBulkAttributeApplied.setVisibility(Visibility.PRIVATE);
                }
            }
            else if (attribute.getKW_PROTECTED() != null) {
                if (myBulkAttributeApplied.getVisibility() == null) {
                    myBulkAttributeApplied.setVisibility(Visibility.PROTECTED);
                }
            }
            else if (attribute.getKW_PUBLIC() != null) {
                if (myBulkAttributeApplied.getVisibility() == null) {
                    myBulkAttributeApplied.setVisibility(Visibility.PUBLIC);
                }
            }
            else if (attribute.getPragmaExpression() != null) {
            }
            else if (attribute.getKW_SYNCHRONIZED() != null) {
            }
            else if (attribute.getKW_ABSTRACT() != null) {
            }
            else if (attribute.getKW_AUTO() != null) {
            }
            else if (attribute.getKW_ENUM() != null) {
                /*if(bulkAttributeApplied.enum == null){
                    bulkAttributeApplied.enum = true
                }*/
            }
            else if (attribute.getKW_EXTERN() != null) {
                if (myBulkAttributeApplied.getExtern() == null) {
                    myBulkAttributeApplied.setExtern(true);
                }
            }
            else if (attribute.getKW_FINAL() != null) {
            }
            else if (attribute.getKW_INOUT() != null) {
            }
            else if (attribute.getKW_NOTHROW() != null) {
                if (myBulkAttributeApplied.getNothrow() == null) {
                    myBulkAttributeApplied.setNothrow(true);
                }
            }
            else if (attribute.getKW_OVERRIDE() != null) {
            }
            else if (attribute.getKW_PURE() != null) {
                if (myBulkAttributeApplied.getPure() == null) {
                    myBulkAttributeApplied.setPure(true);
                }
            }
            else if (attribute.getKW_REF() != null) {
            }
            else if (attribute.getKW___GSHARED() != null) {
            }
            else if (attribute.getKW_SCOPE() != null) {
            }
            else if (attribute.getKW_STATIC() != null) {
                if (myBulkAttributeApplied.getStatic() == null) {
                    myBulkAttributeApplied.setStatic(true);
                }
            }
            else if (attribute.getAlignAttribute() != null) {
            }
            else if ("property".equals(getAtAttributeIdentifierText(attribute))) {
                if (myBulkAttributeApplied.getProperty() == null) {
                    myBulkAttributeApplied.setProperty(true);
                }
            }
            else if (attribute.getLinkageAttribute() != null) {
                // Consulo: extern (C), extern (C++) ... was not recorded
                if (myBulkAttributeApplied.getExtern() == null) {
                    myBulkAttributeApplied.setExtern(true);
                }
            }
            else if (attribute.getKW_CONST() != null) {
                myBulkAttributeApplied.setConst(true);
            }
        }
    }

    /**
     * {@code attribute.atAttribute?.identifier?.text}
     */
    private static @Nullable String getAtAttributeIdentifierText(DLanguageAttribute attribute) {
        DLanguageAtAttribute atAttribute = attribute.getAtAttribute();
        if (atAttribute == null) {
            return null;
        }
        PsiElement identifier = atAttribute.getIdentifier();
        return identifier != null ? identifier.getText() : null;
    }


    public DirectApplication handleConstructor(DLanguageConstructor constructor) {
        return updateFromParentDecl(constructor);
    }

    //todo at some later date maybe make these members of there respective functions to make things more object -oriented
    public DefaultAttributes defaultConstructor(DLanguageConstructor constructor) {
        return new DefaultAttributes(
            /*static*/ false,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ true,
            /*immutable*/ false,
            /*enum*/ false
        );

    }

    public DirectApplication handleFunctionDeclaration(DLanguageFunctionDeclaration function) {
        DirectApplication attribs = updateFromParentDecl(function);
        // Consulo: the storage classes of the function itself (extern (C), static, ...) were not read
        for (DLanguageStorageClass storageClass : function.getStorageClasses()) {
            updateFromStorageClass(storageClass, attribs);
        }
        handle(function.getMemberFunctionAttributes(), attribs);
        return attribs;
    }

    private void handle(List<DLanguageMemberFunctionAttribute> memberFunctionAttributes, DirectApplication attribs) {
        for (DLanguageMemberFunctionAttribute memberFunctionAttribute : memberFunctionAttributes) {
            if (memberFunctionAttribute.getKW_CONST() != null) {
                attribs.setConst(true);
            }
            if (memberFunctionAttribute.getKW_IMMUTABLE() != null) {
                attribs.setImmutable(true);
            }
        }
    }

    public DefaultAttributes defaultFunctionDeclaration(DLanguageFunctionDeclaration function) {
        return new DefaultAttributes(
            //functions are static by default
            /*static*/ true,
            //publicly accessible by default
            /*visibility*/ Visibility.PUBLIC,
            //functions that are properties by default sounds kind of like haskell in the sense that there would be an uncomfortable lack of parentheses
            /*property*/ false,
            //I believe this is false
            /*noGC*/ false,
            //Not extern by default I believe
            // todo need to distinguish between extern(C) and other kinds of extern
            /*extern*/ false,
            //functions are not pure by default
            /*pure*/ false,
            //I'm pretty sure functions are not nothrow by default
            /*nothrow*/ false,
            // functions can be overriden by default
            /*const*/ false,
            //it doesn't make sense for functions to be immutable either:
            /*immutable*/ false,
            /*enum*/ false
        );

    }

    private DirectApplication updateFromParentDecl(Declaration decl) {
        DirectApplication attribs = new DirectApplication();
        /*for (attribute in decl.attributes) {
            if (attribute.kW_ENUM != null) {
                attribs.enum = true
            }
            if (attribute.kW_CONST != null) {
                attribs.const = true
            }
            if (attribute.kW_EXTERN != null) {
                attribs.extern = true
            }
            if (attribute.kW_NOTHROW != null) {
                attribs.nothrow = true
            }
            if (attribute.kW_PURE != null) {
                attribs.pure = true
            }
            if (attribute.kW_PRIVATE != null) {
                attribs.visibility = Visibility.PRIVATE
            }
            if (attribute.kW_PROTECTED != null) {
                attribs.visibility = Visibility.PROTECTED
            }
            if (attribute.kW_PUBLIC != null) {
                attribs.visibility = Visibility.PUBLIC
            }
            if (attribute.kW_STATIC != null) {
                attribs.static = true
            }
        }*/
        return attribs;
    }

    public DirectApplication handleClassDeclaration(DLanguageClassDeclaration classDeclaration) {
        return updateFromParentDecl(classDeclaration);
    }

    public DirectApplication handleInterfaceDeclaration(DLanguageInterfaceDeclaration interfaceDeclaration) {
        return updateFromParentDecl(interfaceDeclaration);
    }

    public DefaultAttributes defaultClassDeclaration(DLanguageClassDeclaration classDeclaration) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DefaultAttributes defaultInterfaceDeclaration(DLanguageInterfaceDeclaration interfaceDeclaration) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DirectApplication handleUnionDeclaration(DLanguageUnionDeclaration union) {
        return updateFromParentDecl(union);
    }

    public DefaultAttributes defaultUnionDeclaration(DLanguageUnionDeclaration union) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe todo check this
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DirectApplication handleStructDeclaration(DLanguageStructDeclaration struct) {
        return updateFromParentDecl(struct);
    }

    public DefaultAttributes defaultStructDeclaration(DLanguageStructDeclaration struct) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe todo check this
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DirectApplication handleLabeledStatement(DLanguageLabeledStatement label) {
        return new DirectApplication();
    }

    public DefaultAttributes defaultLabeledStatement(DLanguageLabeledStatement label) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.LOCAL,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe todo check this
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DirectApplication handleAutoDeclarationPart(DLanguageAutoAssignment autoDeclPart) {
        DLanguageAutoDeclaration autoDecl = (DLanguageAutoDeclaration) Objects.requireNonNull(autoDeclPart.getParent());
        DirectApplication attribs = updateFromParentDecl(autoDecl);
        for (DLanguageStorageClass storageClasss : autoDecl.getStorageClasss()) {
            updateFromStorageClass(storageClasss, attribs);
        }
        return attribs;
    }

    public DefaultAttributes defaultAutoDeclarationPart(DLanguageAutoAssignment autoDeclPart) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe todo check this
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    private void updateFromStorageClass(DLanguageStorageClass storageClasss, DirectApplication attribs) {
        if (storageClasss.getKW_ENUM() != null) {
            attribs.setEnum(true);
        }
        if (storageClasss.getKW_CONST() != null) {
            attribs.setConst(true);
        }
        if (storageClasss.getKW_EXTERN() != null || storageClasss.getLinkageAttribute() != null) {
            attribs.setExtern(true);
        }
        if (storageClasss.getKW_NOTHROW() != null) {
            attribs.setNothrow(true);
        }
        if (storageClasss.getKW_PURE() != null) {
            attribs.setPure(true);
        }
        if (storageClasss.getKW_STATIC() != null) {
            attribs.setStatic(true);
        }
    }

    public DirectApplication handleEnumDeclaration(DLanguageEnumDeclaration enumDecl) {
        return updateFromParentDecl(enumDecl);
    }

    public DefaultAttributes defaultEnumDeclaration(DLanguageEnumDeclaration enumDecl) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe todo check this
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DirectApplication handleCatch(DLanguageCatch aCatch) {
        return new DirectApplication();
    }

    public DefaultAttributes defaultCatch(DLanguageCatch aCatch) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.LOCAL,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe todo check this
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,//todo check this,
            /*immutable*/ false,
            /*enum*/ false
        );
        //cannot be const/immutable etc, so no need to update from attributes
    }

    public DirectApplication handleIdentifierInitializer(DLanguageIdentifierInitializer decl) {
        DLanguageSpecifiedVariableDeclaration varDecls = (DLanguageSpecifiedVariableDeclaration) Objects.requireNonNull(decl.getParent());
        DirectApplication attribs = new DirectApplication();
        for (DLanguageStorageClass storageClasss : varDecls.getStorageClasss()) {
            updateFromStorageClass(storageClasss, attribs);
        }
        return attribs;
    }

    public DefaultAttributes defaultIdentifierInitializer(DLanguageIdentifierInitializer decl) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.PUBLIC,
            /*property*/ false,
            /*noGC*/ false,
            //Not extern by default I believe todo check this
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DirectApplication handleForeachType(DLanguageForeachType foreachType) {
        return new DirectApplication();
    }

    public DefaultAttributes defaultForeachType(DLanguageForeachType foreachType) {
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.LOCAL,
            /*property*/ false,
            /*noGC*/ false,
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DirectApplication handleIfCondition(DLanguageIfCondition ifCondition) {
        return new DirectApplication();
    }

    public DefaultAttributes defaultIfCondition(DLanguageIfCondition ifCondition) {
        //todo get attributes
        return new DefaultAttributes(
            /*static*/ true,
            /*visibility*/ Visibility.LOCAL,
            /*property*/ false,
            /*noGC*/ false,
            /*extern*/ false,
            /*pure*/ false,
            /*nothrow*/ false,
            /*const*/ false,
            /*immutable*/ false,
            /*enum*/ false
        );
    }

    public DirectApplication handleTemplateDeclaration(DLanguageTemplateDeclaration templateDecl) {
        return new DirectApplication();
    }

    public DefaultAttributes defaultTemplateDeclaration(DLanguageTemplateDeclaration templateDecl) {
        //todo get attribues
        return new DefaultAttributes();
    }

    public boolean isStatic() {
        return firstNonNull(myDirectApplication.getStatic(), myBulkAttributeApplied.getStatic(), myDefualts.getStatic());
    }

    public boolean isPublic() {
        return visibility() == Visibility.PUBLIC;
    }

    public boolean isProtected() {
        return visibility() == Visibility.PROTECTED;
    }

    public boolean isPrivate() {
        return visibility() == Visibility.PRIVATE;
    }

    public boolean isLocal() {
        return visibility() == Visibility.LOCAL;
    }

    public Visibility visibility() {
        Visibility direct = myDirectApplication.getVisibility();
        if (direct != null) {
            return direct;
        }
        Visibility bulk = myBulkAttributeApplied.getVisibility();
        return bulk != null ? bulk : myDefualts.getVisibility();
    }

    public boolean isProperty() {
        return firstNonNull(myDirectApplication.getProperty(), myBulkAttributeApplied.getProperty(), myDefualts.getProperty());
    }

    public boolean isNoGC() {
        return firstNonNull(myDirectApplication.getNoGC(), myBulkAttributeApplied.getNoGC(), myDefualts.getNoGC());
    }

    public boolean isExtern() {
        return firstNonNull(myDirectApplication.getExtern(), myBulkAttributeApplied.getExtern(), myDefualts.getExtern());
    }

    public boolean isPure() {
        return firstNonNull(myDirectApplication.getPure(), myBulkAttributeApplied.getPure(), myDefualts.getPure());
    }

    public boolean isNothrow() {
        return firstNonNull(myDirectApplication.getNothrow(), myBulkAttributeApplied.getNothrow(), myDefualts.getNothrow());
    }

    public boolean isConst() {
        return firstNonNull(myDirectApplication.getConst(), myBulkAttributeApplied.getConst(), myDefualts.getConst());
    }

    public boolean isImmutable() {
        return firstNonNull(myDirectApplication.getImmutable(), myBulkAttributeApplied.getImmutable(), myDefualts.getImmutable());
    }

    public boolean isEnum() {
        Boolean direct = myDirectApplication.getEnum();
        return direct != null ? direct : myDefualts.getEnum();
    }

    public DAttributes getAttributes() {
        return new DAttributes(isStatic(), visibility(), isProperty(), isNoGC(), isExtern(), isPure(), isNothrow(), isConst(), isImmutable(), isEnum());
    }

    /**
     * {@code direct ?: (bulk ?: defaultValue)}
     */
    private static boolean firstNonNull(@Nullable Boolean direct, @Nullable Boolean bulk, boolean defaultValue) {
        if (direct != null) {
            return direct;
        }
        return bulk != null ? bulk : defaultValue;
    }
}
