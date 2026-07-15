---
name: sap-commerce-plugin-dev
description: >-
  Kotlin code-style, file-organization, package, and IntelliJ Platform SDK safety conventions for
  the SAP Commerce Developers Toolset plugin. Single source of truth for "how to write code in this
  repo" — use for new classes, DTOs, services, MCP tools, inspections, actions, extensions,
  settings, exec clients, UI dialogs. Trigger on "add a class/DTO/service/extension", "which
  package", "one class per file", "column style", "constructor formatting", "can I use this
  IntelliJ API", "is this API internal", "ApiStatus.Internal", "ApplicationManager", "add an
  inspection/action/settings page/MCP tool", "new module", "verify conventions", "check code
  style", or any add/modify/review of Kotlin source in this plugin — also use as final self-check
  before considering a Kotlin change finished.
---

# SAP Commerce Developers Toolset — plugin dev conventions

Project overview, module layout, build/test commands: repo root `CLAUDE.md` (read first).
This skill = conventions only; `CLAUDE.md` stays high-level, points here.

Shared helpers: `modules/shared/core` (non-UI: `Util.kt`, `Notifications.kt`, `HybrisIcons.kt`,
`HybrisConstants.kt`, `i18n`) and `modules/shared/ui` (UI DSL extensions,
`InEditorResultsView`). Check these before writing a new helper.

## Package & extension conventions

- All production code under `sap.commerce.toolset`, sub-packaged per feature
  (`sap.commerce.toolset.impex...`), mirroring module layout in `CLAUDE.md`.
- Class names carry feature prefix: `ImpEx*`, `FlexibleSearch*`/`FxS*`, `PgQ*` (Polyglot Query),
  `Acl*`, `TS*` (type system), `BS*` (bean system), `Cng*` (cockpitNG), `Cx*` (logging), `Hac*`,
  `CCv2*`.
- Extensions bail out early on non-Hybris projects:
  `init { if (project.isNotHybrisProject) throw ExtensionNotApplicableException.create() }`.
  Per-call gating: `project.isHybrisProject`/`isNotHybrisProject` (also on `PsiElement`,
  `AnActionEvent`, `DataContext`), guard `if (project.isNotHybrisProject) return`, or
  `project.ifHybrisProject { ... }` — all from `shared/core/.../Util.kt`.
- `project.directory` (or `PathMacroManager...expandPath("\$PROJECT_DIR$")`) over deprecated
  nullable `project.basePath`.
- `import com.intellij.util.application` (lowercase `application`) over
  `ApplicationManager.getApplication()` — e.g. `application.runReadAction { ... }`,
  `application.isUnitTestMode`. Same for other `com.intellij.util.*` convenience properties —
  check before reaching for `XyzManager.getInstance(...)`.
- Generated lexer/parser/PSI (ImpEx, ACL, FlexibleSearch, Polyglot Query) live in each module's
  `gen/` — never hand-edit; regenerate via JetBrains Grammar-Kit plugin (`CLAUDE.md` "Custom
  languages").

## Kotlin house style

Formatting governed by committed IDE style (`.idea/codeStyles/Project.xml`, `KOTLIN_OFFICIAL`
base) — no `.editorconfig`.

- **License header**: every hand-written `.kt`/`.java` starts with the LGPL block (Copyright
  2019-&lt;year&gt; EPAM Systems). Produced by IDE Copyright profile (`.idea/copyright/LGPL.xml`) —
  copy from a recent file when creating outside the IDE; never hand-tweak years. `gen/` exempt.
- Indent 4 spaces; max line 180 (Kotlin) / 150 (Java) — don't wrap at 120.
- Trailing commas on multiline param/arg lists.
- Wildcard imports fine (IDE default) — don't force single-name imports.
- Expression-body functions default for single expressions, including overrides
  (`override fun canCreateConfigurable() = project.isHybrisProject`).
- Nullability: `?.let {}` / `?.takeIf {}`, elvis (`?: return`, `?: false`).
- Safe casts: never `as?` — use `asSafely<T>()` from `com.intellij.util`:
  ```kotlin
  import com.intellij.util.asSafely

  val header = element.parent.asSafely<ImpExHeaderLine>() ?: return
  val name = event.getData(CommonDataKeys.PSI_ELEMENT)
      ?.asSafely<PsiNamedElement>()
      ?.name
  ```
- Collections: Kotlin stdlib only, never Java streams. `mapNotNull` over `filter{}.map{}`/
  `filterNotNull`; `firstOrNull`/`find` for lookups; `.asSequence()` only for genuinely lazy
  pipelines.
- Enums: data as constructor `val`s with defaults; reverse-lookup factory in companion `of(...)`/
  `from(...)` via `entries.find { ... }` — see `shared/core/.../Plugin.kt`.
- Constants: global in `object HybrisConstants`; per-feature in `object <Prefix>Constants`
  (`CngConstants`, `BSConstants`...). `const val`, SCREAMING_SNAKE_CASE, grouped into nested
  objects.
- Extension functions: use whenever a receiver-based form simplifies code (first-arg-is-receiver
  helper → extension). Add to existing `Util.kt` matching receiver's domain (project/action guards
  → `shared/core/.../Util.kt`, PSI helpers → `psi/Util.kt`) — don't create parallel
  `*Extensions.kt`.
- Visibility: `private` liberally; `internal` deliberately for module-internal API; never explicit
  `public`. DTOs/value objects: `data class` in `dto/`.
- Member order in class: constructor properties → computed properties → functions → nested types →
  `companion object` last.
- TODOs: `// TODO` (spaced, no FIXME). Deprecations always carry migration message:
  `@Deprecated("review this usage, migrate to LoggersConstants")`.

## One top-level class per file

Every top-level `class`/`data class`/`interface`/`object` → own file, named after the type.
Applies especially to serializable DTOs under `dto/` — don't bundle multiple `@Serializable`
classes in one file even when small and co-used.

Precedent: `logging/mcp/.../dto/LoggerListResponse.kt` references `LoggerDto` from separate
`LoggerDto.kt`; `hac/mcp/.../dto/HacConnectionListResponse.kt` references `HacConnectionDto` the
same way.

Exception: `private` helper class nested *inside* another class body, used only by that class
(e.g. internal accumulator for one method) — not top-level, not reused, no extraction needed.
Nested `Mutable` classes in settings-state, nested `Settings` of `ExecContext` are established
examples.

## Column style for multi-arg calls

>2-3 args (especially named) → one per line, trailing comma:

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

Applies to positional calls too once several params — add names, go column style, over long
unnamed inline call.

## Services & DI

- **Light services only.** `@Service(Service.Level.PROJECT)` (project) or bare `@Service` (app)
  directly on class — never `<projectService>`/`<applicationService>` XML (2 legacy exceptions
  only).
- `getInstance` companion on every service:
  `companion object { fun getInstance(project: Project): Xyz = project.service() }` (app:
  `fun getInstance(): Xyz = application.service()`). Never `project.getService(X::class.java)`.
  MCP `suspend` contexts: `suspend fun getInstance() = currentCoroutineContext().project.service<Xyz>()`.
- **CoroutineScope constructor-injected**: `class Xyz(private val project: Project, private val
  coroutineScope: CoroutineScope)`, `coroutineScope.launch { ... }` — never `CoroutineScope()`/
  `GlobalScope`. See `ccv2/core/.../CCv2Service.kt`.
- Naming: `*Service` (active logic), `*Settings` (persisted config, `settings/`), `*Client`
  (exec/HTTP), `*Manager` (app-level registries), `*Access`/`*StateService` (meta-model pipeline).
- Swappable impls (Community vs Ultimate): plugin extension point — interface with
  `companion object { val EP = ExtensionPointName.create<T>("sap.commerce.toolset...") }`,
  `dynamic="true"` EP in module descriptor, impl registered in XML. See
  `shared/core/.../spring/SpringService.kt`.

### Message bus / events

- Event interfaces named `*Listener`, in `event/`, default no-op bodies (`fun onChange(...) =
  Unit`), topic in companion: `companion object { val TOPIC = Topic(XxxListener::class.java) }`.
- Publish: `project.messageBus.syncPublisher(Xxx.TOPIC).onChange(...)`. Subscribe:
  `project.messageBus.connect(disposable).subscribe(TOPIC, object : ... { })` (app bus:
  `application.messageBus.connect(coroutineScope)`) — no `<projectListeners>`/
  `<applicationListeners>` XML anywhere.

## Settings & persistence

- Immutable state: `SerializablePersistentStateComponent<XxxState>`, `XxxState` = separate
  immutable `data class` (all `val`, defaults) in `settings/state/`, named `*SettingsState`. Never
  `BaseState`/`SimplePersistentStateComponent`/mutable fields.
- Writes via copy-on-write setters:
  `var groupModules: Boolean get() = state.groupModules; set(value) { updateState { it.copy(groupModules = value) } }`
  — see `shared/core/.../settings/ApplicationSettings.kt`. Components usually implement
  `ModificationTracker` via `stateModificationCount`.
- `@State`: `name = "[y] ..."` prefix; storage filenames from `HybrisConstants.STORAGE_*`;
  `roamingType = RoamingType.LOCAL` default; app-level plugin settings add
  `category = SettingsCategory.PLUGINS`; workspace-scoped uses
  `Storage(StoragePathMacros.WORKSPACE_FILE)`. State classes: `@Tag` on class, `@JvmField
  @OptionTag` per scalar field (collections: `@JvmField` only).
- UI editing: immutable⇄mutable pair — state exposes `mutable()` → nested `Mutable` holder of
  `ObservableMutableProperty` fields, `immutable()` converts back. See
  `hac/exec/.../HacConnectionSettingsState.kt`.
- Secrets never in `*State` — `PasswordSafe.instance` keyed by
  `CredentialAttributes("SAP CX - <uuid>")`, read/written on background task. See
  `exec/core/.../ExecConnectionService.kt`.
- Settings pages: `BoundSearchableConfigurable` + Kotlin UI DSL `panel {}` with
  `bindSelected`/`bindText`/`bindItem`; register via `ConfigurableProvider` with
  `canCreateConfigurable() = project.isHybrisProject`.
- Remote connections (hAC/Solr): `*ConnectionSettingsState : ExecConnectionSettingsState`,
  persisted as lists in `*ExecDeveloperSettings` (personal)/`*ExecProjectSettings` (shared),
  active connection by UUID, managed by `*ExecConnectionService : ExecConnectionService<T>`
  routing by `ExecConnectionScope`, firing `*ConnectionSettingsListener`. Defaults seeded from
  project `*.properties` via `PropertyService.getPropertyOrDefault`.

## Coroutines & threading

- Prefer suspend-based platform API: `readAction { }` (not `runReadAction`), `edtWriteAction { }`,
  `withContext(Dispatchers.EDT)`.
- Long ops: `withBackgroundProgress(project, title, true) { ... }` with `reportProgressScope { }`
  (or `reportProgressScope(n) { reporter -> ... }` / `reportSequentialProgress` for determinate
  steps). Never manual `ProgressIndicator`.
- `runBlockingCancellable` unused in this codebase; `coroutineToIndicator` only where a legacy
  indicator-based API is unavoidable from `suspend` (see ApiStatus.Internal section).
- **Scope read actions to the operation, not the whole method.** Wrap only the part needing the
  lock (legacy indicator APIs also get their own `coroutineToIndicator { application.runReadAction
  { … } }`), pushed down next to the individual call. `ImpExValidationMcpService` splits collection
  into a `readAction`-wrapped syntax pass + indicator-wrapped inspection pass, each PSI/document
  lookup in `resolveTarget` its own small `readAction`.
- Keep read actions side-effect-free: return nullable, do `?: error(...)`/assembly *after* it
  returns — never `error(...)`/throw inside.
- Guard `if (project.isDisposed) return@launch` at top of launched blocks touching project.

## Exec layer (remote SAP Commerce execution)

New "run on remote instance" feature = `modules/<feature>/exec` module, four pieces (base classes
in `exec/core/.../exec/`):

1. **`XxxExecContext`** — immutable `data class : ExecContext`: `connection`, `content`,
   `timeout`, overrides `executionTitle` (progress title), `params(): Map<String, String>`, nested
   `Settings`/`Settings.Mutable`, companion `defaultSettings(...)`.
2. **`XxxExecResult`** — usually shared `DefaultExecResult`; own type only if extra fields needed
   (`ExecResult`: `errorMessage`, `errorDetailMessage`).
3. **`XxxExecClient`** — `@Service(Service.Level.PROJECT) class XxxExecClient(project,
   coroutineScope) : DefaultExecClient<XxxExecContext>(project, coroutineScope)`; implement only
   `suspend fun execute(context)`. Base `ExecClient` wraps calls in `withBackgroundProgress`,
   provides callback-style `execute(context, onError, beforeCallback, resultCallback)`.
4. Optional **`XxxExecService`** façade: builds contexts, delegates to client, handles
   notifications/post-processing.

Exemplar: `groovy/exec/.../GroovyExecClient.kt`, `GroovyExecService.kt`,
`context/GroovyExecContext.kt`.

**HTTP**: never inline HAC calls — use `HacHttpClient.getInstance(project).post(actionUrl, params,
canReLoginIfNeeded, timeout, settings, replicaContext)` (`hac/exec/.../http/HacHttpClient.kt`); it
owns Spring-Security login, cookie/CSRF caching (`AuthContextCache`), auto re-login, replica
routing, SSL trust, timeouts. Pass `context.timeout`/`context.replicaContext` through.

## MCP toolsets (`modules/<feature>/mcp`)

- Toolset class thin: plain `class XxxMcpToolset : McpToolset` (no `@Service`), registered via
  `<mcpServer.mcpToolset implementation="..."/>`. Each tool: `suspend fun` with `@McpTool(name =
  "sap_commerce_...")` + `@McpDescription("""...""")`, every param `@McpDescription`-annotated,
  defaulted if optional. Tools return `String` (serialized JSON). Project via
  `currentCoroutineContext().project`.
- Business logic in `@Service(Service.Level.PROJECT) class XxxMcpService`; inputs a small
  `XxxMcpContext`, outputs `@Serializable` DTOs in `dto/`. Bridge to exec layer via
  `HacMcpService.resolveConnection(connectionName)` → `ExecContext`.
- Serialization: never call `Json` directly from a tool — resolve `McpMapper` via
  `resolveMapper(outputFormat)`, `mapper.map(dto)` (`ai/mcp/.../McpMapper.kt`, `McpUtils.kt`,
  `json/McpJsonMapper.kt`; shared `Json` is `prettyPrint = false, explicitNulls = false`).
  Fields that may be absent: nullable, default `null` (omitted from output); `isX` flags →
  `@SerialName("x")`.
- Derive computed fields in the DTO, not call site: defaulted constructor properties referencing
  earlier param (`val valid: Boolean = issues.none { it.severity == HighlightSeverity.ERROR.name }`),
  ordered after dependency — callers construct DTO with only real inputs.
- Error handling: no try/catch in tools — `error("...")`/`require(...)` (message surfaces to MCP
  client), or model domain failure as DTO fields (`success = false`, `error`, `errorDetail`).
- Exemplar: `groovy/mcp/.../GroovyMcpToolset.kt`, `GroovyMcpService.kt`, `dto/GroovyResult.kt`.

## Actions & UI

- Actions extend `DumbAwareAction` (or mix in `DumbAware`), override
  `getActionUpdateThread() = ActionUpdateThread.BGT` (EDT only if `update()` touches Swing
  directly), guard `event.presentation.isEnabledAndVisible = event.isHybrisProject`, suppress
  Search-Everywhere noise with `e.ifNotFromSearchPopup { ... }`/`hideFromSearchPopup()`
  (`shared/core/.../Util.kt`). Register in module descriptor `<actions>`; dotted ids
  (`sap.commerce.toolset.reimport`).
- Forms: Kotlin UI DSL `panel { group { row { ... } } }` with `bindSelected`/`bindText`/
  `bindItem`, `contextHelp(...)`, `.visibleIf(...)`/`.enabledIf(...)`,
  `.layout(RowLayout.PARENT_GRID)`; alignment `AlignX.FILL`/`Align.FILL`; scrollable content gets
  `.align(Align.FILL).resizableColumn()` inside `.resizableRow()`. Validation:
  `validationOnInput { }`/`.addValidationRule(...)` + `registerValidators(disposable)`. Reuse
  shared DSL extensions in `shared/ui/.../ui/Dsl.kt` (`previewEditor`, `nullableIntTextField`,
  `copyLink`, `browserLink`, `inlineBanner`, `actionButton(s)`, `scrollPanel`, cell decorators
  `italic()`/`border()`/`background()`) before hand-rolling.
- Dialogs: subclass `DialogWrapper`, `Project` as first constructor arg; `init` sets `title`
  (+flags like `isResizable`) first, `super.init()` last; body in `override fun
  createCenterPanel() = panel { ... }`; compact dialogs override `getStyle() =
  DialogStyle.COMPACT`. Canonical: `project/import-ui/.../ProjectRefreshDialog.kt`.
- Notifications: fluent builder in `shared/core/.../Notifications.kt` —
  `Notifications.error/warning/info(title, content).hideAfter(10).addAction(text) { e, n -> }
  .notify(project)` — never `NotificationGroupManager` directly. Single group `"[y] SAP Commerce"`
  (`HybrisConstants.NOTIFICATION_GROUP_HYBRIS`, `resources/META-INF/plugin-internal.xml`).
- Icons: `val`s in `object HybrisIcons` (`shared/core/.../HybrisIcons.kt`), nested per-feature;
  SVGs under `shared/core/resources/icons/`; load via private `getIcon(path)`. Platform icons via
  `AllIcons.*`.
- i18n: all user-facing strings through top-level `i18n("key", vararg params)`
  (`shared/core/.../Util.kt`, backed by `i18n/HybrisBundle.properties`); `i18nFallback` when key
  may be absent.
- In-editor result panels extend `shared/ui/.../ui/editor/InEditorResultsView.kt`
  (`executingView(...)`, `panelView { }`, abstract `render(...)`).
- Line markers extend `HybrisLineMarkerProvider<T>`
  (`shared/core/.../codeInsight/daemon/HybrisLineMarkerProvider.kt`) — forces slow-marker
  collection, applies hybris-project guard — implement `canProcess`, `tryCast`,
  `collectDeclarations`. Never extend `LineMarkerProvider` directly.
- Single tool window `"SAP CX"` — contribute content to it, don't register new tool windows.

## Custom language & PSI features

- Inspections (custom languages): extend `LocalInspectionTool`, name `<Lang><Meaning>Inspection`,
  delegate to private inner visitor extending generated `<Lang>Visitor`, register problems via
  `holder.registerProblem(element, i18n("hybris.inspections.<Key>", ...),
  ProblemHighlightType...)`, override `getDefaultLevel()` for severity.
- Inspections (XML DOM models): extend per-model abstract base (`TSInspection`, `BSInspection`,
  `CngConfigInspection`...) built on shared `AbstractInspection<T : DomElement>`
  (`shared/core/.../codeInspection/AbstractInspection.kt`), handles hybris-project guard +
  profile-severity resolution.
- Registration: `<localInspection>` in module descriptor (not `inspectionToolProvider`) with
  `groupPath="SAP Commerce"`, `groupName="[y] <Lang>"`, `enabledByDefault="true"`, `level`,
  `shortName` matching class name. Every inspection ships description HTML
  `<ShortName>.html` under module's `resources/inspectionDescriptions/`. No bundled
  inspection-profile XMLs — defaults on `<localInspection>` element. Quick fixes in sibling
  `codeInspection/fix/`.
- PSI helpers: shared accessors as top-level functions in `<Lang>PsiUtil.kt` annotated
  `@file:JvmName("<Lang>PsiUtil")`, wired via BNF `psiImplUtilClass`. Hand-written PSI behavior in
  `psi/impl/<Lang><Rule>Mixin.kt` abstract classes extending `ASTWrapperPsiElement`, attached
  per-rule via BNF `mixin="..."`.
- In-memory PSI files: if language has `psi/<Lang>ElementFactory` with `createFile(project, text)`
  (currently ImpEx, FlexibleSearch, Polyglot Query), use it over hand-rolling
  `PsiFileFactory.getInstance(project).createFileFromText(name, <Lang>FileType, …)`. Languages
  without a factory (ACL) use `PsiFileFactory` directly.
- References: extend per-language `<Lang>ReferenceBase` (`PsiReferenceBase.Poly`); implement
  `multiResolve` via
  `CachedValuesManager.getManager(project).getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)`
  with `Key`/stateless provider as companion singletons, filter with shared `getValidResults(...)`.
  Cache dependency: feature's own tracker (`TSModificationTracker.getInstance(project)`) for
  meta-model-backed resolves, `PsiModificationTracker.MODIFICATION_COUNT` for pure-PSI resolves.
  Canonical: `impex/core/.../references/ImpExTSItemReference.kt`.
- Completion: one `<Lang>CompletionContributor` wiring `extend(CompletionType.BASIC, pattern,
  provider)` in `init`; patterns grouped in `<Lang>Patterns`; providers as separate classes in
  `codeInsight/completion/provider/`. Lookup elements never built inline — use per-language
  `object <Lang>LookupElementFactory` in `codeInsight/lookup/`
  (`LookupElementBuilder.create(...).withIcon(HybrisIcons...).withTypeText(...)`).
- Meta-model access: never read type-system/bean-system model directly — go through
  `TSMetaModelAccess.getInstance(project)`/`BSMetaModelAccess.getInstance(project)`. Throw
  `ProcessCanceledException` while model uncomputed/dumb mode — call from cancellable read-action
  context, never swallow PCE.
- Annotators extend shared `AbstractAnnotator`
  (`shared/core/.../lang/annotation/AbstractAnnotator.kt`); syntax highlighters, folding builders
  (`<Lang>FoldingBuilder`), formatters (`<Lang>FormattingModelBuilder`) follow same per-language
  layout, registered per language in module descriptor.

## New module checklist

1. `modules/<feature>/<layer>/build.gradle.kts` — `settings.gradle.kts` auto-discovers it; Gradle
   project name = path with `/` → `-` (`modules/groovy/mcp` → `:groovy-mcp`). Minimal:
   `org.jetbrains.intellij.platform.module` + `alias(libs.plugins.kotlin)` (+
   `libs.plugins.serialization` if needed), source sets `src`/`resources`/`tests`, explicit
   `implementation(project(":..."))` deps (respect layering — `core` never depends on `ui`),
   catalog libs via `libs.*`, bundled IDE plugins under `intellijPlatform { bundledPlugins(...) }`.
   Template: `groovy/mcp/build.gradle.kts`.
2. Add descriptor `resources/META-INF/sap.commerce.toolset-<group>-<layer>.xml`, register the
   module's extensions/actions there.
3. Add matching `<xi:include>` to root `resources/META-INF/plugin.xml` and module to
   `pluginComposedModule(...)` in root `build.gradle.kts`.
4. Versions in `gradle/libs.versions.toml` and `gradle.properties` — never inline.

## Never call `@ApiStatus.Internal` platform SDK APIs

Marketplace verification (`./gradlew verifyPlugin`) rejects `@ApiStatus.Internal` platform
symbols; using them risks silent breakage (no compatibility guarantee). Internal classes often
look like ordinary entry points (`DaemonCodeAnalyzerImpl`, `DaemonProgressIndicator`,
`HighlightingSessionImpl`) — IntelliJ's own bundled code sometimes uses them in ways unsafe to
copy.

**Before using an unfamiliar/internal-looking platform class or method, verify:**

1. Locate platform sources jar (project dep):
   `~/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea/idea/<version>/*/idea-<version>-sources.jar`
2. Extract, grep class declaration, check line(s) above `class`/`interface`/method for
   `@ApiStatus.Internal`:
   ```
   unzip -p idea-<version>-sources.jar "com/intellij/some/Package/SomeClass.java" \
     | grep -n -B3 "class SomeClass\|public .*someMethod("
   ```
3. No sources jar available: decompile compiled class via bundled Fernflower
   (`plugins/java-decompiler/lib/java-decompiler.jar` in extracted IDE dist, under
   `~/.gradle/caches/*/transforms/*/idea-<version>/`):
   ```
   java -cp java-decompiler.jar org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler SomeClass.class out/
   ```
4. Only class-level/member-level `@ApiStatus.Internal` matters — package alone isn't
   disqualifying. Stable plugin-use APIs often live in `.impl`/`.ex` packages (e.g.
   `LocalInspectionToolWrapper` in `com.intellij.codeInspection.ex`) without being internal;
   conversely a normal-looking package isn't automatically safe — check the annotation, not the
   package. `@Obsolete` is soft deprecation (alternative exists), fine to use, doesn't block
   verification like `@ApiStatus.Internal` does.

When internal functionality is genuinely needed, find the public entry point one layer up instead
of reaching into daemon/highlighting internals. Examples (see
`impex/mcp/.../ImpExValidationMcpService.kt`):
- Instead of `DaemonCodeAnalyzerImpl.runMainPasses` + `HighlightingSessionImpl` (both internal) for
  headless "editor parity" highlighting: `PsiErrorElement` PSI-tree walks for syntax errors +
  `LocalInspectionTool.processFile(file, InspectionManager)` (public) per registered inspection
  from `InspectionProfile.getInspectionTools(psiElement)` (public) for semantic checks.
- `LocalInspectionTool.processFile` still needs an active progress indicator context (calls
  `ProgressManager.checkCanceled()`, asserts an indicator is registered) — from `suspend`, prefer
  `com.intellij.openapi.progress.coroutineToIndicator { indicator -> ... }` over
  `ProgressManager.getInstance().runProcess(Computable { ... }, EmptyProgressIndicator())`:
  `coroutineToIndicator` ties indicator cancellation to the current coroutine `Job`, so a
  cancelled/timed-out caller (MCP client) actually interrupts work — `EmptyProgressIndicator`
  never gets cancelled. `ProgressIndicator`, `EmptyProgressIndicator`,
  `ProgressManager.runProcess`/`executeProcessUnderProgress` all carry `@Obsolete` pointing at
  `coroutineToIndicator`/`blockingContext`/`runBlockingCancellable` — legal but prefer
  coroutine-native form when call site is already `suspend` (the norm for MCP tool services here).

## Verify before finishing

Re-read diff against each heading above, in order (clean compile proves correctness, not
convention compliance):

1. Package & extension conventions
2. Kotlin house style (license header, formatting, idioms)
3. One top-level class per file
4. Column style for multi-arg calls
5. Services & DI / Settings & persistence (light service? `getInstance`? immutable state?)
6. Area-specific section matching the change (exec layer, MCP toolset, actions & UI, custom
   language & PSI, new module checklist)
7. No `@ApiStatus.Internal` — for anything unfamiliar/internal-looking actually touched, confirm
   sources-jar/decompile check was done, not assumed.

Then:
```
./gradlew :<module>:compileKotlin
```