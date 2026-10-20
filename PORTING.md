# Porting

This repository is a port of [intellij-dlanguage](https://github.com/intellij-dlanguage/intellij-dlanguage) to the
[Consulo](https://github.com/consulo/consulo) platform. Upstream sources (Kotlin + Java, IntelliJ APIs) live in
`legacy/` until they are ported; nothing there is built. This file holds the rules every port follows.

## Layout

| Directory  | Artifact / JPMS module | Contents                                                                          |
|------------|------------------------|-----------------------------------------------------------------------------------|
| `base`     | `consulo.d.base`       | generated `consulo.d.icon.DIconGroup` and `consulo.d.localize.DLocalize`          |
| `language` | `consulo.d.language`   | upstream `utils`, `dlang/psi-api`, `dlang/psi-impl`, `dlang/idea` and root `src`  |
| `sdlang`   | `consulo.d.sdlang`     | upstream `sdlang`                                                                 |
| `dub`      | `consulo.d.dub`        | upstream `dub`, `dub/idea`; DUB import through the external system                |
| `execution`| `consulo.d.execution`  | D run configurations (DMD, run app), d-unit tests, `consulo.nativeDev` debugger support |
| `asm`      | `consulo.d.asm`        | `consulo.asm` integration: GNU as in GCC style asm templates and D `.s` files, extern function ↔ label markers |
| `plugin`   | `consulo.d`            | packaging only: `plugin.xml`                                                      |
| `it`       | `consulo.d.it`         | tests only, on the headless platform (`consulo-it`); no `module-info`, never ships |

Upstream's `utils`, `psi-api`, `psi-impl` and root sources share packages (`io.github.intellij.dlanguage`, `.psi`,
`.stubs`, `.utils`, `.lexer`, `.annotator`, `.settings`), and a package cannot be split between JPMS modules, so they
are one module.

Not ported: DCD (completion must work from our own PSI and stubs), `errorreporting`, CLion-only code, and the CIDR
`debugger` (debugging comes from the `consulo.nativeDev` plugin).

Waiting on the platform: the `dub.json` JSON schema (`DubJsonSchemaProviderFactory`, `jsonSchema/dub.json-schema.json`
stay in `legacy/`), because Consulo's JSON plugin has no JSON Schema support yet. JOM is no substitute: it reports every
key it does not know, and DUB recipes are full of platform-suffixed keys (`dflags-linux-x86_64`).

## Packages and names

* Ported upstream classes keep their **package and class name** (`io.github.intellij.dlanguage.*`,
  `io.github.intellij.dub.*`), so upstream changes can be found and merged by name. A file goes to the same package
  path under the module's `src/main/java`.
* Code that only exists because of Consulo (registrations that upstream did in `plugin.xml`, module extensions, order
  entries, the module-aware index) goes under `consulo.d.*`.

## Kotlin to Java

Everything is Java. Translate so that the API **Java callers saw** stays the same, which lets upstream `.java` files
compile with only import changes:

* `object Foo` → `public final class Foo` with `public static final Foo INSTANCE` and instance members
  (`Foo.INSTANCE.bar()`, as upstream Java code already calls it).
* top-level functions / properties of `Foo.kt` → `public final class FooKt` with static members (or the name given by
  `@file:JvmName`). Extension functions take the receiver as the first parameter; extension property `val X.foo` →
  `static getFoo(X)`.
* `companion object` members → static members of the enclosing class (fix the rare `Foo.Companion.x()` callers).
* properties → private field + `getX()` / `isX()` / `setX()` (Kotlin's JVM naming). `const val` / `@JvmField` →
  `public static final` field of the same name.
* `data class` → `record` when it has no superclass, otherwise a class with `equals` / `hashCode` / `toString`.
* default arguments → overloads, only for the arities that are actually called (`@JvmOverloads` arities otherwise).
* `when` → `switch` / `if` chains; `?.let`, `?:` → explicit null checks; `by lazy` → lazily initialised field.
* keep every comment and KDoc (as Javadoc). Keep the logic; do not "improve" it.

Upstream `.java` files: change only imports, APIs and nullability annotations. Translated Kotlin follows Consulo code
style: 4 spaces, braces always, `else` / `catch` on a new line, no `var`, `my` prefix on instance fields, interface
types on the left (`List<X> x = new ArrayList<>()`).

## Nullability

Every module is `@NullMarked` (jspecify): non-null is the default. Kotlin `T?` and upstream `@Nullable` →
`org.jspecify.annotations.Nullable`, placed right before the type (`public @Nullable PsiElement getX()`). Remove
`@NotNull` / `@Nonnull` everywhere.

The build runs ErrorProne with NullAway (`JSpecifyMode`, `OnlyNullMarked`, level ERROR), inherited from
`arch.bind.java`, so a nullability mistake fails the build:

* where Kotlin would have thrown on null (`!!`, `as X`, a Java value passed into a non-null Kotlin parameter), use
  `Objects.requireNonNull(...)` at that same place, so the behaviour stays upstream's;
* where upstream really returns or passes `null`, declare it `@Nullable` and follow the consequences;
* where a Consulo contract is non-null but upstream answers `null` on purpose, do what the Consulo platform does in the
  same situation (e.g. `TextRange.EMPTY_RANGE`), or `@SuppressWarnings("NullAway")` with a comment saying why;
* a package holding generated code that is not null-correct gets `@NullUnmarked` in its `package-info.java` (the lexer
  package, as consulo-google-go does).

## Platform APIs

Find the Consulo counterpart of `com.intellij.X` by **class name** in the Consulo sources
(`/run/media/vistall/repositories/consulo/consulo/modules`), not by package. consulo-rust
(`/run/media/vistall/repositories/consulo/consulo-rust`) is a large example of the same Kotlin → Java + IntelliJ →
Consulo translation. Frequent ones:

| IntelliJ                                         | Consulo                                                     |
|--------------------------------------------------|-------------------------------------------------------------|
| `com.intellij.lang.ASTNode`                       | `consulo.language.ast.ASTNode`                              |
| `com.intellij.psi.tree.IElementType`, `TokenSet` | `consulo.language.ast.IElementType`, `TokenSet`             |
| `com.intellij.psi.TokenType`                      | `consulo.language.ast.TokenType` (deprecated: prefer `StandardTokenTypes`) |
| `com.intellij.lang.PsiBuilder`, `PsiParser`       | `consulo.language.parser.PsiBuilder`, `PsiParser`           |
| `com.intellij.lexer.Lexer`, `FlexAdapter`        | `consulo.language.lexer.Lexer`, `FlexAdapter`               |
| `com.intellij.psi.*` (PsiElement, PsiFile, …)    | `consulo.language.psi.*`                                    |
| `com.intellij.psi.ResolveState`, `PsiScopeProcessor` | `consulo.language.psi.resolve.*`                        |
| `com.intellij.psi.stubs.*`                        | `consulo.language.psi.stub.*` (`StringRef`: `consulo.index.io`) |
| `com.intellij.extapi.psi.ASTWrapperPsiElement`, `StubBasedPsiElementBase` | `consulo.language.impl.psi.ASTWrapperPsiElement`, `consulo.language.impl.psi.stub.StubBasedPsiElementBase` |
| `com.intellij.psi.search.GlobalSearchScope`       | `consulo.language.psi.scope.GlobalSearchScope`              |
| `com.intellij.util.indexing.FileBasedIndex`       | `consulo.language.psi.stub.FileBasedIndex`                  |
| `com.intellij.openapi.diagnostic.Logger`          | `consulo.logging.Logger`                                    |
| `StringUtil`, `ContainerUtil`, `Key`              | `consulo.util.lang.StringUtil`, `consulo.util.collection.ContainerUtil`, `consulo.util.dataholder.Key` |
| `CachedValuesManager`, `CachedValueProvider`      | `consulo.application.util.*`                                |
| `Project`, `Module`, `VirtualFile`                | `consulo.project.Project`, `consulo.module.Module`, `consulo.virtualFileSystem.VirtualFile` |
| `ItemPresentation`                                | `consulo.navigation.ItemPresentation`                       |
| `IncorrectOperationException`, `ProcessingContext`| `consulo.language.util.*`                                   |
| `javax.swing.Icon`, `AllIcons.X.Y`                | `consulo.ui.image.Image`, `consulo.platform.base.icon.PlatformIconGroup.xY()` |

If a needed API is missing in Consulo, say so; do not invent a replacement.

## Registration

Consulo has no XML registration besides `plugin.xml` itself. What upstream registered in
`legacy/dlang/plugin-impl/src/main/resources/META-INF/*.xml` becomes an annotation on the class:
`@ExtensionImpl` for extension point implementations (Consulo extension classes declare `getLanguage()` instead of
the XML `language=` attribute), `@ServiceAPI` / `@ServiceImpl` for services, `@ActionImpl` for actions,
`@TopicAPI` / `@TopicImpl` for message-bus topics. Stub element types are registered by a `StubElementTypeHolder`
(`@ExtensionImpl`, see consulo-google-go's `GoTypesStubElementHolder`). IntelliJ's `@Service` does nothing here.
Default live templates (`<defaultLiveTemplates file=...>`) become a `LiveTemplateContributor` in the shape
consulo-devkit's `LiveTemplateConverter` generates (`consulo.d.language.DLiveTemplateContributor`), with the
descriptions as `DLocalize` keys; the template context is a `BaseTemplateContextType` extension.

A `localInspection` entry becomes `@ExtensionImpl` on the `LocalInspectionTool`, which returns what the entry said:
`getDisplayName()` (`key`), `getDefaultLevel()` (`level`), `isEnabledByDefault()` (`enabledByDefault`, default
`true`), and the group: `groupKey` → `getGroupPath()` = `{D language}`, `groupPathKey` + `groupName` →
`{D language, <groupName>}` (Consulo would otherwise prepend the language name). `getDescription()` carries the text of
upstream's `inspectionDescriptions/<Name>.html` as a `DLocalize` key (simple HTML is fine). Inspection classes upstream
leaves unregistered are ported without `@ExtensionImpl`. A `LocalQuickFix` has no family name in Consulo; its
`getText()` / `getName()` is a `LocalizeValue`.

* A Consulo topic is its listener interface. Upstream topics that share one listener (`Topics.DSCANNER_TOOL_CHANGE`,
  `DFMT_TOOL_CHANGE`, … all `ToolChangeListener`) become one `@TopicAPI` sub-interface each (`consulo.d.language.messagebus`),
  which declares the method again: the topic binding only sees the methods declared in the topic interface.
* Run configurations need no `ProgramRunner`: Run is Consulo's `BasicProgramRunner`, Debug is nativeDev's
  `NativeDebugRunner` for a configuration implementing `NativeProgramRunConfiguration` (`createDebugTarget`), whose
  editor builds on `NativeProgramParametersLayout`.

## Strings and icons

* `DlangBundle.message("some.key")` → `DLocalize.someKey()` (`consulo.d.localize.DLocalize`). Add the key to
  `base/src/main/resources/LOCALIZE-LIB/en_US/consulo.d.DLocalize.yaml` with upstream's English text from
  `legacy/utils/src/main/resources/i18n.properties`, the key lowercased (`action.NewDfile.text` →
  `action.newdfile.text` → `DLocalize.actionNewdfileText()`). User-visible text is `LocalizeValue`, never `String`.
  Only `en_US` is kept; translations come from verbaria.
* `DLanguage.Icons.X` keeps its names; the images come from `DIconGroup` and `PlatformIconGroup`. Prefer a platform
  icon (`nodesStruct`, `nodesFunction`, `nodesPropertyread`/`write`); the D-only ones are 16x16 SVGs in the platform's
  node style and palette, with a `dark/` variant. A D source file uses the language icon.
* Resources go next to the class that reads them (`io/github/intellij/dlanguage/colors/color_page.d`, read with
  `getClass().getResourceAsStream("color_page.d")`), not in a shared directory such as upstream's `settings/`: every
  resource directory that is a valid package name is a package of its module, and all D modules share one plugin class
  loader, so two modules with `settings/` stop the whole plugin from loading.
* IntelliJ utilities missing from Consulo (e.g. `StringUtil.isLatinAlphanumeric`) are copied into the `…Kt` facade
  that uses them, with a note.

## Generated code

* PSI interfaces (`io.github.intellij.dlanguage.psi.DLanguage*`) and implementations (`…psi.impl.DLanguage*Impl`)
  are generated at build time by `language/src/psi-generator/TypesRegenScript.java` (the Java port of upstream's
  `scripts/types_regen_script.d`) into `language/target/generated-sources/psi`.
  Change the tables there, never the output.
* Lexers are generated from `*.flex` in `src/main/java` by `maven-consulo-plugin`'s `generate-lexers`; a
  `<Name>.flex.idea` marker next to the file selects the IntelliJ-style skeleton (`%implements FlexLexer`, wrapped in
  `FlexAdapter`), which keeps upstream's `.flex` files unchanged apart from imports.
  `maven-consulo-plugin` 3.33 sets JFlex's static options (output directory, `no_constructor`) outside its lock, so
  two modules must not generate lexers at the same time under `-T 1C`: `sdlang` depends on `language` only to be
  built after it.
* Grammar-Kit parsers are generated from `*.bnf` in `src/main/java` by `generate-parsers` (upstream's `.bnf` is used
  as is). Consulo's Grammar-Kit puts the PSI factory in `<psiImplPackage>.<ElementTypeHolder>Factory`, not in a
  nested `Factory` class.

## Resolve

The rules follow `consulo-key-principles` (stubs and resolve):

- References are cached: an element hands out one reference object until its file changes
  (`DResolveCaching.reference`), and `resolve()`/`multiResolve()` go through `ResolveCache` with a static resolver
  (`DResolveCaching.resolve`), never in dumb mode. A new reference kind does the same.
- A module the walk does not start in (an imported module) is processed from its stub tree
  (`DStubDeclarationWalker.processModule`), as are the members of a type of another module (`processMembers`). Code
  reached from those walks must read stubs (`getGreenStub()`, `getStubOrPsiChildren`), not the AST. A declaration
  whose members must not be visible at module level needs a stub (that is why `mixin template` has one).
- Processors that look for one name expose `DNameHint`, so stub walks skip other names.
- `it/.../DStubCrossModuleResolveTest` fails when resolving parses a file other than the one resolved in; add a case
  there for new cross-module resolve paths.

## Tests

Upstream's tests that need a real project, file system or the highlighting passes (`myFixture`) go to `it`, on
`consulo-it` (`@ExtendWith(HeadlessProjectExtension.class)`, `CodeInsightTestFixture`, `@UseInspection`).

Upstream's tests are JUnit 3 on IntelliJ's test framework; ports are JUnit 5 on `consulo-test-junit-impl` (declared
first in the module's dependencies). Test data moves unchanged from `legacy/src/test/resources/gold` to
`<module>/src/test/resources/gold`, keeping upstream's layout (`<name>.d` next to `expected/<name>.txt`), and test
methods keep upstream's names (`testkeyword_abstract`) so a test maps to its data file exactly as before.

* Lexer tests: `DlangLexerTestBase` reproduces the parts of IntelliJ's `LexerTestCase` upstream used
  (`printTokens`, `doTest(text, expected)`), with IntelliJ's output format.
* Parser tests: `DLanguageParserTestBase` extends Consulo's `SimpleParsingTest`; like IntelliJ's `ParsingTestCase` it
  trims source and expected text and keeps the case of test names.
* The light test application has no `LocalFileSystem` (`LocalFileSystem.getInstance()` fails): code that resolves
  real files is tested through its pure parts (`DubBuildSourceFileFilterTest` checks the regex, not the hyperlink).
* `GeneratedParserUtilBase.DummyBlock` has no public constructor in Consulo; tests that need a detached node use
  `new CompositeElement(type)` (`SDLangPsiImplTest`).
* Quick-fix tests: `consulo-it` has no intention API, so `it`'s `DQuickFixTestBase` reproduces the parts of IntelliJ's
  `LightQuickFixTestCase` upstream used (`doTest` checks a marked-up file, `doSingleTest` applies the fix named in the
  first line of `before<name>` at the caret and compares with `after<name>`), with the change made on the UI thread
  under a write action taken off it (`TransferredWriteActionService`) inside a command.
* The light test application's `UIInternal` does not implement `ImageKey.of`, so nothing that loads an icon group
  (`DIconGroup`, `PlatformIconGroup`, so `DLanguage.Icons` and every presentation) runs there; such tests go to `it`.
* consulo-it binds nothing that lives in `ide-impl`: no `PsiSearchHelper` (so no `ReferencesSearch`, find usages,
  rename), no `ProjectUndoManager` (so no template expansion or other undoable command), no `ScratchFileService` (so no
  `EditorEx.reinitSettings`). Tests that need them are ported and `@Disabled` with the reason. Opening an editor needs
  `consulo-hacking-java-base` on the test classpath, and the headless editor builds its line cache from document
  changes only: open it on an empty document and set the text before moving the caret.
* The light test application registers every extension of the module, the `DDoc` parser definition included, so D
  parser trees show doc comments parsed as DDoc; upstream's `ParsingTestCase` registers only the definition under test
  and shows a `DOC_DATA` leaf. The expectations with doc comments (`documentation_attached_declaration`, `expression`,
  `unittests`) carry the DDoc trees.
* `consulo-it` cannot run the formatter (`CodeStyleInternalHelper` is not bound, and `HeadlessCommandProcessor` recurses
  on the current command name), so `DFormatterTest` is ported but disabled. Its headless editor cannot move the caret:
  tests pass offsets to the platform code instead (`DDocumentationProviderTest`).
