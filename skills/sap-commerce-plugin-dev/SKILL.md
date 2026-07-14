---
name: sap-commerce-plugin-dev
description: >-
  All Kotlin code-style, file-organization, package, and IntelliJ Platform SDK safety conventions
  for the SAP Commerce Developers Toolset plugin. This is the single source of truth for "how do I
  write code in this repository" — use it whenever writing or reviewing Kotlin source: new classes,
  DTOs, services, MCP tools, inspections, actions, extensions, settings, exec clients, UI dialogs.
  Trigger on "add a class", "add a DTO", "create a service", "create an extension", "which package",
  "one class per file", "column style", "constructor formatting", "can I use this IntelliJ API",
  "is this API internal", "ApiStatus.Internal", "ApplicationManager", "add an inspection", "add an
  action", "add a settings page", "add an MCP tool", "new module", "verify conventions", "check
  code style", or any request to add, modify, or review Kotlin source files in this plugin — also
  use it as a final self-check before considering a Kotlin change finished.
---

# SAP Commerce Developers Toolset — plugin development conventions

Project overview, module layout, and build/test commands are in the repo's root `CLAUDE.md` —
read that first for architecture. This skill is the single place for code-writing conventions in
this repository; `CLAUDE.md` intentionally stays high-level and points here instead of duplicating
these rules.

Canonical shared helpers live in `modules/shared/core` (non-UI: `Util.kt`, `Notifications.kt`,
`HybrisIcons.kt`, `HybrisConstants.kt`, `i18n`) and `modules/shared/ui` (UI DSL extensions,
`InEditorResultsView`). Before writing a helper, check whether one already exists there.

## Package & extension conventions

- All production code lives under the `sap.commerce.toolset` package namespace, sub-packaged per
  feature (e.g. `sap.commerce.toolset.impex...`), mirroring the module layout described in
  `CLAUDE.md`.
- Class names carry the feature's short prefix: `ImpEx*`, `FlexibleSearch*`/`FxS*`, `PgQ*`
  (Polyglot Query), `Acl*`, `TS*` (type system), `BS*` (bean system), `Cng*` (cockpitNG), `Cx*`
  (logging), `Hac*`, `CCv2*`.
- Plugin extensions should bail out early on non-Hybris projects:
  `init { if (project.isNotHybrisProject) throw ExtensionNotApplicableException.create() }`.
  For per-call gating use the shared extension properties from
  `modules/shared/core/src/sap/commerce/toolset/Util.kt` — `project.isHybrisProject`,
  `project.isNotHybrisProject` (also defined on `PsiElement`, `AnActionEvent`, `DataContext`), the
  early-return guard `if (project.isNotHybrisProject) return`, or `project.ifHybrisProject { ... }`.
- Prefer `project.directory` (or `PathMacroManager...expandPath("\$PROJECT_DIR$")`) over the
  deprecated, possibly-null `project.basePath`.
- Use the Kotlin extension property `import com.intellij.util.application` (referenced as the
  lowercase `application` value) instead of the verbose Java-style
  `ApplicationManager.getApplication()` call — e.g. `application.runReadAction { ... }`,
  `application.isUnitTestMode`. The same applies to other `com.intellij.util.*` convenience
  properties where they exist (check for one before reaching for the `XyzManager.getInstance(...)`
  form).
- Regenerated lexer/parser/PSI sources for the custom languages (ImpEx, ACL, FlexibleSearch,
  Polyglot Query) live under each module's `gen/` dir — never hand-edit files there; regenerate via
  the JetBrains Grammar-Kit IDE plugin instead (see `CLAUDE.md` "Custom languages" for details).

## Kotlin house style

Formatting is governed by the committed IDE code style (`.idea/codeStyles/Project.xml`,
`KOTLIN_OFFICIAL` base); there is no `.editorconfig`.

- **License header**: every hand-written `.kt`/`.java` file starts with the LGPL block comment
  ("This file is part of "SAP Commerce Developers Toolset" plugin ... Copyright (C) 2019-&lt;year&gt;
  EPAM Systems &lt;hybrisideaplugin@epam.com&gt; and contributors"). It is produced by the committed
  IDE Copyright profile (`.idea/copyright/LGPL.xml`) — copy it from any recent source file when
  creating files outside the IDE; never hand-tweak years on existing files. Generated `gen/` files
  are the only exception.
- **Indent 4 spaces; max line length 180 for Kotlin** (150 for Java) — don't wrap at 120.
- **Trailing commas** on multiline parameter/argument lists.
- **Wildcard imports are fine** (IDE default on-demand threshold) — don't force single-name imports.
- **Expression-body functions** (`fun x() = ...`) are the default for single-expression functions,
  including overrides (`override fun canCreateConfigurable() = project.isHybrisProject`).
- **Nullability**: chain `?.let { }` / `?.takeIf { }` and elvis (`?: return`, `?: false`).
- **Safe casts**: never use the `as?` operator — use `asSafely<T>()` from `com.intellij.util`
  (defined in the platform's `com/intellij/util/KtUtils.kt`), including on nullable receivers with
  the safe-call form:
  ```kotlin
  import com.intellij.util.asSafely

  val header = element.parent.asSafely<ImpExHeaderLine>() ?: return
  val name = event.getData(CommonDataKeys.PSI_ELEMENT)
      ?.asSafely<PsiNamedElement>()
      ?.name
  ```
- **Collections**: Kotlin stdlib only — never Java streams. Prefer `mapNotNull` over
  `filter{}.map{}`/`filterNotNull`; `firstOrNull`/`find` for lookups; `.asSequence()` only for
  genuinely lazy pipelines.
- **Enums**: data as constructor `val` properties with defaults; reverse-lookup factory in the
  companion named `of(...)` (or `from(...)`) implemented with `entries.find { ... }` — see
  `modules/shared/core/src/sap/commerce/toolset/Plugin.kt`.
- **Constants**: global ones in `object HybrisConstants`
  (`modules/shared/core/src/sap/commerce/toolset/HybrisConstants.kt`); per-feature ones in an
  `object <Prefix>Constants` (`CngConstants`, `BSConstants`, ...). `const val`,
  SCREAMING_SNAKE_CASE, related constants grouped into nested objects.
- **Extension functions** go in the `Util.kt` of the package matching the receiver's domain
  (project/action guards in `shared/core/.../Util.kt`, PSI helpers in `psi/Util.kt`, etc.) — add to
  the existing file, don't create parallel `*Extensions.kt` files.
- **Visibility**: `private` liberally; `internal` deliberately for module-internal API; never write
  explicit `public`. DTOs/value objects are `data class`es in a `dto/` package.
- **Member ordering** within a class: constructor properties → computed properties → functions →
  nested types → `companion object` last.
- **TODOs** are `// TODO` (spaced, no FIXME). **Deprecations** always carry an actionable migration
  message: `@Deprecated("review this usage, migrate to LoggersConstants")`.

## One top-level class per file

Every top-level `class` / `data class` / `interface` / `object` gets its own file, named after the
type. This applies especially to serializable DTOs under `dto/` packages — do not bundle multiple
`@Serializable` classes into one file, even when they're small and only used together.

Existing precedent (each is a single class per file, cross-referencing siblings by import):
- `modules/logging/mcp/src/sap/commerce/toolset/logging/mcp/dto/LoggerListResponse.kt` references
  `LoggerDto` from a separate `LoggerDto.kt` in the same package.
- `modules/hac/mcp/src/sap/commerce/toolset/hac/mcp/dto/HacConnectionListResponse.kt` references
  `HacConnectionDto` from a separate file the same way.

Exception: a `private` helper class/data class nested *inside* another class body, used only by
that one class's implementation (e.g. an internal accumulator type used by a single method), does
not need to be extracted — it isn't a top-level type and isn't reused elsewhere. The nested
`Mutable` classes inside settings-state classes and the nested `Settings` of an `ExecContext` are
established examples of intentional nesting.

## Column style for constructor calls with many parameters

When a constructor call (or any function call) has more than two or three arguments — especially
with named arguments — put each argument on its own line with a trailing comma, rather than
inlining them:

```kotlin
// Prefer:
ImpExSyntaxIssueDto(
    severity = it.severity.name,
    message = it.message,
    line = it.line,
    column = it.column,
)

// Not:
ImpExSyntaxIssueDto(severity = it.severity.name, message = it.message, line = it.line, column = it.column)
```

This applies to positional-argument calls too once there are several parameters — prefer adding
argument names and going to column style over a long unnamed inline call.

## Services & dependency injection

- **Light services only.** Declare services with `@Service(Service.Level.PROJECT)` (project) or
  bare `@Service` (application) directly on the class — do NOT register `<projectService>` /
  `<applicationService>` in XML (only 2 legacy interface-split exceptions exist in the codebase).
- **`getInstance` companion**: every service exposes
  `companion object { fun getInstance(project: Project): Xyz = project.service() }` (app-level:
  `fun getInstance(): Xyz = application.service()`). Never use `project.getService(X::class.java)`.
  In MCP `suspend` contexts: `suspend fun getInstance() = currentCoroutineContext().project.service<Xyz>()`.
- **CoroutineScope is constructor-injected**: services that launch async work declare
  `class Xyz(private val project: Project, private val coroutineScope: CoroutineScope)` and
  `coroutineScope.launch { ... }` — never create a `CoroutineScope()`/`GlobalScope`. See
  `modules/ccv2/core/src/sap/commerce/toolset/ccv2/CCv2Service.kt`.
- **Naming**: `*Service` (active logic), `*Settings` (persisted config, `settings/` package),
  `*Client` (exec/HTTP), `*Manager` (app-level registries), `*Access`/`*StateService`
  (meta-model pipeline).
- **Swappable implementations** (e.g. Community vs Ultimate) use a plugin-defined extension point:
  interface with `companion object { val EP = ExtensionPointName.create<T>("sap.commerce.toolset...") }`,
  `dynamic="true"` EP declared in the module descriptor, impl registered in XML — see
  `modules/shared/core/src/sap/commerce/toolset/spring/SpringService.kt`.

### Message bus / events

- Event interfaces are named `*Listener`, live in an `event/` sub-package, have default no-op
  method bodies (`fun onChange(...) = Unit`), and hold their topic in the companion:
  `companion object { val TOPIC = Topic(XxxListener::class.java) }`.
- Publish with `project.messageBus.syncPublisher(Xxx.TOPIC).onChange(...)`; subscribe
  programmatically with `project.messageBus.connect(disposable).subscribe(TOPIC, object : ... { })`
  (app bus: `application.messageBus.connect(coroutineScope)`) — no `<projectListeners>` /
  `<applicationListeners>` XML registration anywhere.

## Settings & persistence

- **Immutable state**: persistent components extend
  `SerializablePersistentStateComponent<XxxState>` where `XxxState` is a separate immutable
  `data class` (all `val` with defaults) in a `settings/state/` package, named `*SettingsState`.
  Never use `BaseState`/`SimplePersistentStateComponent` or mutable state fields.
- **Writes** go through copy-on-write property setters on the component:
  `var groupModules: Boolean get() = state.groupModules; set(value) { updateState { it.copy(groupModules = value) } }`
  — see `modules/shared/core/src/sap/commerce/toolset/settings/ApplicationSettings.kt`. Components
  usually also implement `ModificationTracker` via `stateModificationCount`.
- **`@State` conventions**: `name = "[y] ..."` prefix; storage file names come from
  `HybrisConstants.STORAGE_*` constants, `roamingType = RoamingType.LOCAL` by default; app-level
  plugin settings add `category = SettingsCategory.PLUGINS`; workspace-scoped state uses
  `Storage(StoragePathMacros.WORKSPACE_FILE)`. State classes carry `@Tag` on the class and
  `@JvmField @OptionTag` per scalar field (collections: `@JvmField` only).
- **UI editing of state** uses the immutable⇄mutable pair: state classes expose `mutable()`
  returning a nested `Mutable` holder of `ObservableMutableProperty` fields and `immutable()` to
  convert back — see `modules/hac/exec/src/sap/commerce/toolset/hac/exec/settings/state/HacConnectionSettingsState.kt`.
- **Secrets never go into `*State`** — store via `PasswordSafe.instance` keyed by
  `CredentialAttributes("SAP CX - <uuid>")`, read/written on a background task; see
  `modules/exec/core/src/sap/commerce/toolset/exec/ExecConnectionService.kt`.
- **Settings pages**: `BoundSearchableConfigurable` + Kotlin UI DSL `panel {}` with
  `bindSelected`/`bindText`/`bindItem` against the settings component; register through a
  `ConfigurableProvider` whose `canCreateConfigurable() = project.isHybrisProject`.
- **Remote connections** (hAC/Solr): a `*ConnectionSettingsState` implementing
  `ExecConnectionSettingsState`, persisted as lists in `*ExecDeveloperSettings` (personal) and
  `*ExecProjectSettings` (shared), active connection stored by UUID, managed by a
  `*ExecConnectionService : ExecConnectionService<T>` that routes by `ExecConnectionScope` and
  fires the `*ConnectionSettingsListener` topic. Defaults are seeded from project `*.properties`
  via `PropertyService` (`getPropertyOrDefault`).

## Coroutines & threading

- Prefer the modern suspend-based platform API throughout: `readAction { }` (not `runReadAction`),
  `edtWriteAction { }`, `withContext(Dispatchers.EDT)`.
- Long operations: `withBackgroundProgress(project, title, true) { ... }` with
  `reportProgressScope { }` (or `reportProgressScope(n) { reporter -> ... }` /
  `reportSequentialProgress` for determinate steps). Never create a `ProgressIndicator` manually.
- `runBlockingCancellable` is not used in this codebase; `coroutineToIndicator` only where a legacy
  indicator-based API is unavoidable from a `suspend` context (see the `@ApiStatus.Internal`
  section below).
- Guard `if (project.isDisposed) return@launch` at the top of launched blocks that touch the
  project.

## Exec layer (remote SAP Commerce execution)

A new "run something on a remote instance" feature is a `modules/<feature>/exec` module with four
pieces (base classes in `modules/exec/core/src/sap/commerce/toolset/exec/`):

1. **`XxxExecContext`** — immutable `data class` implementing `ExecContext`: carries `connection`,
   `content`, `timeout`, overrides `executionTitle` (drives the progress title), exposes
   `params(): Map<String, String>`, plus nested `Settings`/`Settings.Mutable` and a companion
   `defaultSettings(...)`.
2. **`XxxExecResult`** — usually the shared `DefaultExecResult`; define your own only when extra
   fields are needed (`ExecResult` contract: `errorMessage`, `errorDetailMessage`).
3. **`XxxExecClient`** — `@Service(Service.Level.PROJECT) class XxxExecClient(project, coroutineScope)
   : DefaultExecClient<XxxExecContext>(project, coroutineScope)`; implement only
   `suspend fun execute(context)`. The base `ExecClient` already wraps calls in
   `withBackgroundProgress` and provides the callback-style `execute(context, onError,
   beforeCallback, resultCallback)`.
4. Optionally a **`XxxExecService`** façade that builds contexts, delegates to the client, and
   handles notifications/post-processing.

Exemplar to copy: `modules/groovy/exec/src/sap/commerce/toolset/groovy/exec/`
(`GroovyExecClient.kt`, `GroovyExecService.kt`, `context/GroovyExecContext.kt`).

**HTTP**: never build HAC HTTP calls inline — go through
`HacHttpClient.getInstance(project).post(actionUrl, params, canReLoginIfNeeded, timeout, settings,
replicaContext)` (`modules/hac/exec/src/sap/commerce/toolset/hac/exec/http/HacHttpClient.kt`); it
owns Spring-Security login, cookie/CSRF caching (`AuthContextCache`), auto re-login, replica
routing, SSL trust and timeouts. Pass `context.timeout`/`context.replicaContext` straight through.

## MCP toolsets (`modules/<feature>/mcp`)

- **Toolset class is thin**: a plain `class XxxMcpToolset : McpToolset` (no `@Service`), registered
  via `<mcpServer.mcpToolset implementation="..."/>` in the module descriptor. Each tool is a
  `suspend fun` annotated `@McpTool(name = "sap_commerce_...")` + `@McpDescription("""...""")`,
  with every parameter `@McpDescription`-annotated and defaulted where optional. Tools return
  `String` (serialized JSON). Get the project via `currentCoroutineContext().project`.
- **Business logic** lives in a `@Service(Service.Level.PROJECT) class XxxMcpService`; inputs are a
  small `XxxMcpContext`, outputs `@Serializable` DTOs in `dto/`. Bridge to the exec layer by
  resolving the connection through `HacMcpService.resolveConnection(connectionName)` and building
  an `ExecContext`.
- **Serialization**: never call `Json` directly from a tool — resolve an `McpMapper` via
  `resolveMapper(outputFormat)` and `mapper.map(dto)`
  (`modules/ai/mcp/src/sap/commerce/toolset/ai/mcp/` — `McpMapper.kt`, `McpUtils.kt`,
  `json/McpJsonMapper.kt`; the shared `Json` is `prettyPrint = false, explicitNulls = false`).
  DTO fields that may be absent are nullable with default `null` so they're omitted from output;
  rename Kotlin `isX` flags with `@SerialName("x")`.
- **Error handling**: no try/catch in tools — validate with `error("...")`/`require(...)` (the
  message surfaces to the MCP client), or model domain failure as DTO fields
  (`success = false`, `error`, `errorDetail`).
- Exemplar to copy: `modules/groovy/mcp/src/sap/commerce/toolset/groovy/mcp/`
  (`GroovyMcpToolset.kt`, `GroovyMcpService.kt`, `dto/GroovyResult.kt`).

## Actions & UI

- **Actions** extend `DumbAwareAction` (or mix in `DumbAware`), override
  `getActionUpdateThread() = ActionUpdateThread.BGT` (EDT only when `update()` touches Swing state
  directly), guard with `event.presentation.isEnabledAndVisible = event.isHybrisProject`, and
  suppress Search-Everywhere noise with `e.ifNotFromSearchPopup { ... }` / `hideFromSearchPopup()`
  (from `shared/core/.../Util.kt`). Register in the `<actions>` block of the module descriptor;
  dotted ids (`sap.commerce.toolset.reimport`).
- **Forms**: Kotlin UI DSL `panel { group { row { ... } } }` with `bindSelected`/`bindText`/
  `bindItem`, `contextHelp(...)`, `.visibleIf(...)`/`.enabledIf(...)`,
  `.layout(RowLayout.PARENT_GRID)`; alignment via `AlignX.FILL` / `Align.FILL`; scrollable content
  gets `.align(Align.FILL).resizableColumn()` inside `.resizableRow()`. Validation via
  `validationOnInput { }` / `.addValidationRule(...)` + `registerValidators(disposable)`.
  **Reuse the shared DSL extensions in `modules/shared/ui/src/sap/commerce/toolset/ui/Dsl.kt`**
  (`previewEditor`, `nullableIntTextField`, `copyLink`, `browserLink`, `inlineBanner`,
  `actionButton(s)`, `scrollPanel`, cell decorators `italic()`/`border()`/`background()`) before
  hand-rolling components.
- **Dialogs**: subclass `DialogWrapper` with `Project` as first constructor arg; `init` block sets
  `title` (and flags like `isResizable`) first and calls `super.init()` last; body in
  `override fun createCenterPanel() = panel { ... }`; compact dialogs override
  `getStyle() = DialogStyle.COMPACT`. Canonical example:
  `modules/project/import-ui/src/.../ProjectRefreshDialog.kt`.
- **Notifications**: use the fluent builder in
  `modules/shared/core/src/sap/commerce/toolset/Notifications.kt` —
  `Notifications.error/warning/info(title, content).hideAfter(10).addAction(text) { e, n -> }
  .notify(project)` — never `NotificationGroupManager` directly. There is a single group
  `"[y] SAP Commerce"` (`HybrisConstants.NOTIFICATION_GROUP_HYBRIS`, registered in
  `resources/META-INF/plugin-internal.xml`).
- **Icons**: all declared as `val`s in `object HybrisIcons`
  (`modules/shared/core/src/sap/commerce/toolset/HybrisIcons.kt`), grouped into nested per-feature
  objects; SVGs under `modules/shared/core/resources/icons/`; load with the private
  `getIcon(path)` helper. Platform icons via `AllIcons.*`.
- **i18n**: all user-facing strings through the top-level `i18n("key", vararg params)` function
  (`shared/core/.../Util.kt`, backed by `i18n/HybrisBundle.properties`); `i18nFallback` when a key
  may be absent.
- **In-editor result panels** extend
  `modules/shared/ui/src/sap/commerce/toolset/ui/editor/InEditorResultsView.kt`
  (`executingView(...)`, `panelView { }`, abstract `render(...)`).
- **Line markers** extend `HybrisLineMarkerProvider<T>`
  (`modules/shared/core/src/sap/commerce/toolset/codeInsight/daemon/HybrisLineMarkerProvider.kt`),
  which forces slow-marker collection and applies the hybris-project guard — implement
  `canProcess`, `tryCast`, `collectDeclarations`. Never extend `LineMarkerProvider` directly.
- The plugin has a single tool window `"SAP CX"` — contribute content to it rather than
  registering new tool windows.

## Custom language & PSI features

- **Inspections (custom languages)**: extend `LocalInspectionTool`, name
  `<Lang><Meaning>Inspection`, delegate to a private inner visitor extending the generated
  `<Lang>Visitor`, register problems via `holder.registerProblem(element, i18n("hybris.inspections.<Key>", ...),
  ProblemHighlightType...)`, override `getDefaultLevel()` for severity.
- **Inspections (XML DOM models)**: extend the per-model abstract base (`TSInspection`,
  `BSInspection`, `CngConfigInspection`, ...) built on the shared
  `AbstractInspection<T : DomElement>`
  (`modules/shared/core/src/sap/commerce/toolset/codeInspection/AbstractInspection.kt`), which
  handles the hybris-project guard and profile-severity resolution.
- **Registration**: `<localInspection>` in the module descriptor (not `inspectionToolProvider`)
  with `groupPath="SAP Commerce"`, `groupName="[y] <Lang>"`, `enabledByDefault="true"`, a `level`,
  and `shortName` matching the class name. Every inspection ships a description HTML named
  `<ShortName>.html` under the module's `resources/inspectionDescriptions/`. There are no bundled
  inspection-profile XMLs — defaults live on the `<localInspection>` element. Quick fixes go in a
  sibling `codeInspection/fix/` package.
- **PSI helpers**: shared accessors are top-level functions in `<Lang>PsiUtil.kt` annotated
  `@file:JvmName("<Lang>PsiUtil")`, wired via the BNF `psiImplUtilClass` attribute. Hand-written
  PSI behavior lives in `psi/impl/<Lang><Rule>Mixin.kt` abstract classes extending
  `ASTWrapperPsiElement` and attached per-rule via the BNF `mixin="..."` attribute.
- **References**: extend the per-language `<Lang>ReferenceBase` (a `PsiReferenceBase.Poly`);
  implement `multiResolve` through
  `CachedValuesManager.getManager(project).getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)`
  with the `Key` and stateless provider as companion singletons, filtering with the shared
  `getValidResults(...)`. Cache dependency: the feature's own tracker
  (`TSModificationTracker.getInstance(project)`) for meta-model-backed resolves,
  `PsiModificationTracker.MODIFICATION_COUNT` for pure-PSI resolves. Canonical:
  `modules/impex/core/src/sap/commerce/toolset/impex/psi/references/ImpExTSItemReference.kt`.
- **Completion**: one `<Lang>CompletionContributor` wiring `extend(CompletionType.BASIC, pattern,
  provider)` in its `init`; patterns grouped in a `<Lang>Patterns` object; providers as separate
  classes in `codeInsight/completion/provider/`. Lookup elements are never built inline — use the
  per-language `object <Lang>LookupElementFactory` in `codeInsight/lookup/`
  (`LookupElementBuilder.create(...).withIcon(HybrisIcons...).withTypeText(...)`).
- **Meta-model access**: never read the type-system/bean-system model directly — go through
  `TSMetaModelAccess.getInstance(project)` / `BSMetaModelAccess.getInstance(project)`. These throw
  `ProcessCanceledException` while the model is uncomputed or in dumb mode — call from a
  cancellable read-action context and never swallow PCE.
- **Annotators** extend the shared `AbstractAnnotator`
  (`modules/shared/core/src/sap/commerce/toolset/lang/annotation/AbstractAnnotator.kt`); syntax
  highlighters, folding builders (`<Lang>FoldingBuilder`), and formatters
  (`<Lang>FormattingModelBuilder`) follow the same per-language file layout and are registered per
  language in the module descriptor.

## New module checklist

1. Create `modules/<feature>/<layer>/build.gradle.kts` — `settings.gradle.kts` auto-discovers it;
   the Gradle project name is the path with `/` → `-` (`modules/groovy/mcp` → `:groovy-mcp`). Keep
   it minimal: `org.jetbrains.intellij.platform.module` + `alias(libs.plugins.kotlin)` (+
   `libs.plugins.serialization` if needed), source sets `src`/`resources`/`tests`, explicit
   `implementation(project(":..."))` deps (respect layering — `core` never depends on `ui`),
   catalog libs via `libs.*`, bundled IDE plugins under `intellijPlatform { bundledPlugins(...) }`.
   Copy `modules/groovy/mcp/build.gradle.kts` as a template.
2. Add the descriptor `resources/META-INF/sap.commerce.toolset-<group>-<layer>.xml` and register
   the module's extensions/actions there.
3. Add the matching `<xi:include>` to the root `resources/META-INF/plugin.xml` and the module to
   `pluginComposedModule(...)` in the root `build.gradle.kts`.
4. Versions live in `gradle/libs.versions.toml` and `gradle.properties` — never inline them.

## Never call `@ApiStatus.Internal` IntelliJ Platform SDK APIs

JetBrains Marketplace plugin verification (`./gradlew verifyPlugin`) rejects usage of platform SDK
symbols annotated `@ApiStatus.Internal`, and using them risks silent breakage across IDE releases
since JetBrains gives no compatibility guarantee for internal APIs. This is easy to violate by
accident because many internal classes are NOT obviously named — they can look like ordinary,
plausible entry points (e.g. `DaemonCodeAnalyzerImpl`, `DaemonProgressIndicator`,
`HighlightingSessionImpl`), and IntelliJ's own decompiled/bundled code (including JetBrains' own
bundled plugins) sometimes uses them internally in ways that are not safe to copy into a
third-party plugin.

**Before using an unfamiliar or internal-looking platform class or method**, verify it first:

1. Locate the platform sources jar (already a project dependency), e.g.:
   `~/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea/idea/<version>/*/idea-<version>-sources.jar`
2. Extract and grep the relevant `.java`/`.kt` source for the class declaration and check the line(s)
   immediately above the `class`/`interface`/method declaration for `@ApiStatus.Internal`:
   ```
   unzip -p idea-<version>-sources.jar "com/intellij/some/Package/SomeClass.java" \
     | grep -n -B3 "class SomeClass\|public .*someMethod("
   ```
3. If no sources jar is available, decompile the compiled class from the platform/plugin jar with
   the bundled Fernflower decompiler (`plugins/java-decompiler/lib/java-decompiler.jar` inside an
   extracted IDE distribution — found under `~/.gradle/caches/*/transforms/*/idea-<version>/`):
   ```
   java -cp java-decompiler.jar org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler SomeClass.class out/
   ```
4. Only a class-level (or member-level) `@ApiStatus.Internal` annotation matters — package name
   alone is not disqualifying. Many stable, intended-for-plugin-use APIs live in `.impl` / `.ex`
   packages (e.g. `LocalInspectionToolWrapper` in `com.intellij.codeInspection.ex`) without being
   internal. Conversely, being in a "normal-looking" package doesn't make a class safe — check the
   annotation, not the package name. `@Obsolete` is a *soft* deprecation marker (a modern
   alternative exists) and is fine to use; it does not block Marketplace verification the way
   `@ApiStatus.Internal` does.

When the internal API's functionality is genuinely needed, look for the public entry point one
layer up instead of reaching into daemon/highlighting internals. Two concrete examples encountered
in this codebase (see `modules/impex/mcp/src/sap/commerce/toolset/impex/mcp/ImpExValidationMcpService.kt`):
- Instead of `DaemonCodeAnalyzerImpl.runMainPasses` + `HighlightingSessionImpl` (both internal) to
  get "editor parity" highlighting headlessly, use `PsiErrorElement` PSI-tree walks for syntax
  errors and `LocalInspectionTool.processFile(file, InspectionManager)` (public) per registered
  inspection from `InspectionProfile.getInspectionTools(psiElement)` (public) for semantic checks.
- `LocalInspectionTool.processFile` still requires an active progress indicator context (a
  different, non-internal requirement — it calls `ProgressManager.checkCanceled()` while walking
  the PSI tree, and asserts some indicator is registered). From a `suspend` function, prefer
  `com.intellij.openapi.progress.coroutineToIndicator { indicator -> ... }` over the older
  `ProgressManager.getInstance().runProcess(Computable { ... }, EmptyProgressIndicator())` —
  `coroutineToIndicator` ties the indicator's cancellation to the current coroutine `Job`, so a
  cancelled/timed-out caller (e.g. an MCP client) actually interrupts the work; a bare
  `EmptyProgressIndicator` never gets cancelled by anything. `ProgressIndicator` itself, its
  `EmptyProgressIndicator` implementation, and `ProgressManager.runProcess`/`executeProcessUnderProgress`
  all carry an `@Obsolete`/"Obsolescence notice" Javadoc pointing at `coroutineToIndicator` (and
  `blockingContext`, `runBlockingCancellable`) as the coroutine-native replacements — still legal to
  use (not `@ApiStatus.Internal`), but prefer the coroutine-native form whenever the call site is
  already `suspend`, which is the norm for MCP tool services in this codebase.

## Verify generated code before finishing

Before treating any Kotlin change in this repository as done, re-read the diff once against each
section heading above, in order — a clean compile only proves correctness, not convention
compliance, so it complements this pass rather than replacing it:

1. Package & extension conventions
2. Kotlin house style (license header, formatting, idioms)
3. One top-level class per file
4. Column style for constructor calls with many parameters
5. Services & DI / Settings & persistence (light service? `getInstance` idiom? immutable state?)
6. The area-specific section that matches the change (exec layer, MCP toolset, actions & UI,
   custom language & PSI, new module checklist)
7. Never call `@ApiStatus.Internal` IntelliJ Platform SDK APIs — for anything unfamiliar or
   internal-looking that was actually touched, confirm the sources-jar/decompile check was done,
   not assumed.

Then run the mechanical backstop for the affected module(s):
```
./gradlew :<module>:compileKotlin
```
