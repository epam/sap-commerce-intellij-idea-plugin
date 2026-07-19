---
name: sap-commerce-plugin-dev
description: >-
  Kotlin style, file/package organization, and IntelliJ Platform SDK safety for this plugin —
  single source of truth for writing code here. Use for any Kotlin add/modify/review: classes,
  DTOs, services, MCP tools, inspections, actions, extensions, settings, exec clients, UI dialogs,
  new modules — and as final self-check before finishing a Kotlin change. Trigger on "which
  package", "one class per file", "column style", "can I use this IntelliJ API",
  "ApiStatus.Internal", "verify conventions", "check code style".
---

# SAP Commerce Developers Toolset — plugin dev conventions

Project overview, module layout, build/test commands: root `CLAUDE.md` (read first). Shared
helpers: `modules/shared/core` (`Util.kt`, `Notifications.kt`, `HybrisIcons.kt`,
`HybrisConstants.kt`, `i18n`) and `modules/shared/ui` — check these before writing a new helper.

## Packages & extensions

- Production code under `sap.commerce.toolset.<feature>`, mirroring the module layout. Class names
  carry the feature prefix: `ImpEx*`, `FlexibleSearch*`/`FxS*`, `PgQ*`, `Acl*`, `TS*`, `BS*`,
  `Cng*`, `Cx*`, `Hac*`, `CCv2*`.
- Extensions bail out early on non-Hybris projects:
  `init { if (project.isNotHybrisProject) throw ExtensionNotApplicableException.create() }`.
  Per-call gating via `isHybrisProject`/`isNotHybrisProject`/`ifHybrisProject` (on `Project`,
  `PsiElement`, `AnActionEvent`, `DataContext` — `shared/core/.../Util.kt`).
- `project.directory` over deprecated nullable `project.basePath`.
- `import com.intellij.util.application` over `ApplicationManager.getApplication()`; check
  `com.intellij.util.*` convenience properties before reaching for `XyzManager.getInstance(...)`.
- Never hand-edit `gen/` (generated lexer/parser/PSI) — regenerate via Grammar-Kit (`CLAUDE.md`).

## Kotlin house style

Formatting governed by committed IDE style (`.idea/codeStyles/Project.xml`, `KOTLIN_OFFICIAL`
base) — no `.editorconfig`.

- LGPL license header on every hand-written `.kt`/`.java` (copy from a recent file; never
  hand-tweak years; `gen/` exempt).
- Indent 4 spaces; max line 180 (Kotlin) / 150 (Java); trailing commas on multiline lists;
  wildcard imports fine.
- Expression-body functions for single expressions, including overrides.
- Nullability idioms: `?.let {}`, `?.takeIf {}`, elvis (`?: return`, `?: false`). Never `as?` —
  use `asSafely<T>()` (`com.intellij.util`).
- Kotlin stdlib collections only (never Java streams); `mapNotNull` over `filter{}.map{}`/
  `filterNotNull`; `firstOrNull`/`find` for lookups; `.asSequence()` only for genuinely lazy
  pipelines.
- Enums: data as constructor `val`s with defaults; reverse-lookup companion `of(...)`/`from(...)`
  via `entries.find { ... }`.
- Constants: `object HybrisConstants` (global) / `object <Prefix>Constants` (per feature);
  `const val`, SCREAMING_SNAKE_CASE, grouped into nested objects.
- Prefer extension functions where a receiver-based form simplifies; add to the existing `Util.kt`
  matching the receiver's domain — don't create parallel `*Extensions.kt`.
- Visibility: `private` liberally, `internal` deliberately, never explicit `public`.
  DTOs/value objects: `data class` in `dto/`.
- Member order: constructor properties → computed properties → functions → nested types →
  `companion object` last.
- `// TODO` (spaced, no FIXME); deprecations always carry a migration message.
- **One top-level class per file**, named after the type — including small `@Serializable` DTOs in
  `dto/`. Exception: a `private` helper nested *inside* the only class using it (e.g. settings
  `Mutable` holders, `ExecContext.Settings`).
- **Column style for multi-arg calls**: >2-3 args (especially named) → one per line, trailing
  comma. Applies to positional calls too — add names and go column style over a long unnamed
  inline call.

## Services & DI

- Light services only: `@Service(Service.Level.PROJECT)` / bare `@Service` (app) on the class —
  never `<projectService>`/`<applicationService>` XML.
- `getInstance` companion on every service: `project.service()` / `application.service()`; MCP
  `suspend` contexts: `currentCoroutineContext().project.service<Xyz>()`. Never
  `project.getService(X::class.java)`.
- `CoroutineScope` constructor-injected (second param after `project`) — never `CoroutineScope()`/
  `GlobalScope`.
- Naming: `*Service` (logic), `*Settings` (persisted config), `*Client` (exec/HTTP), `*Manager`
  (app registries), `*Access`/`*StateService` (meta-model pipeline).
- Swappable impls (Community vs Ultimate): extension point — interface with companion
  `val EP = ExtensionPointName.create<T>("sap.commerce.toolset...")`, `dynamic="true"` in the
  module descriptor, impl registered in XML. See `shared/core/.../spring/SpringService.kt`.
- Events: `*Listener` interfaces in `event/` with no-op default bodies and companion `TOPIC`.
  Publish via `messageBus.syncPublisher(TOPIC)`, subscribe via `messageBus.connect(disposable)`
  (app bus: `connect(coroutineScope)`) — never listener XML.

## Settings & persistence

- Immutable state: `SerializablePersistentStateComponent<XxxState>` with a separate all-`val`
  `*SettingsState` data class (defaults) in `settings/state/`; writes via copy-on-write setters
  (`updateState { it.copy(...) }`); usually implements `ModificationTracker` via
  `stateModificationCount`. Never `BaseState`/`SimplePersistentStateComponent`/mutable fields.
- `@State`: `name = "[y] ..."`; storage filenames from `HybrisConstants.STORAGE_*`;
  `roamingType = RoamingType.LOCAL` default; app-level adds `category = SettingsCategory.PLUGINS`;
  workspace-scoped uses `Storage(StoragePathMacros.WORKSPACE_FILE)`. State classes: `@Tag` on
  class, `@JvmField @OptionTag` per scalar (collections: `@JvmField` only).
- UI editing via immutable⇄mutable pair: `mutable()` → nested `Mutable` of
  `ObservableMutableProperty` fields, `immutable()` back. See
  `hac/exec/.../HacConnectionSettingsState.kt`.
- Secrets never in `*State` — `PasswordSafe.instance` keyed by
  `CredentialAttributes("SAP CX - <uuid>")`, read/written on background task.
- Settings pages: `BoundSearchableConfigurable` + UI DSL bindings, registered via
  `ConfigurableProvider` with `canCreateConfigurable() = project.isHybrisProject`.
- Remote connections (hAC/Solr): `*ConnectionSettingsState : ExecConnectionSettingsState`,
  persisted as lists in `*ExecDeveloperSettings` (personal)/`*ExecProjectSettings` (shared),
  active connection by UUID, managed by `*ExecConnectionService : ExecConnectionService<T>`
  routing by `ExecConnectionScope`, firing `*ConnectionSettingsListener`. Defaults seeded from
  project properties via `PropertyService.getPropertyOrDefault`.

## Coroutines & threading

- Suspend-first platform API: `readAction { }` (not `runReadAction`), `edtWriteAction { }`,
  `withContext(Dispatchers.EDT)`. Long ops: `withBackgroundProgress(project, title, true)` +
  `reportProgressScope`/`reportSequentialProgress` — never manual `ProgressIndicator`.
  `coroutineToIndicator` only where a legacy indicator-based API is unavoidable (see
  ApiStatus.Internal section); `runBlockingCancellable` unused in this codebase.
- Scope read actions to bare data retrieval, not whole method: `readAction { }` next to each
  locked call, assign raw result, filter/map/assemble after — never chain post-processing onto
  the block.
- Read actions side-effect-free: nullable return, `?: error(...)`/assembly after block returns —
  never throw inside.
- Guard `if (project.isDisposed) return@launch` at the top of launched blocks touching the project.

## Exec layer (remote SAP Commerce execution)

New remote-execution feature = `modules/<feature>/exec` module, four pieces (base classes in
`exec/core/.../exec/`); exemplar `groovy/exec/`.

1. **`XxxExecContext`** — immutable `data class : ExecContext`: `connection`, `content`,
   `timeout`, overrides `executionTitle`, `params()`, nested `Settings`/`Settings.Mutable`,
   companion `defaultSettings(...)`.
2. **`XxxExecResult`** — usually shared `DefaultExecResult`; own type only for extra fields.
3. **`XxxExecClient`** — project light service extending `DefaultExecClient<XxxExecContext>`;
   implement only `suspend fun execute(context)`. Base wraps calls in `withBackgroundProgress`
   and provides the callback-style `execute(...)`.
4. Optional **`XxxExecService`** façade: builds contexts, delegates to client, handles
   notifications/post-processing.

**HTTP**: never inline HAC calls — use `HacHttpClient.getInstance(project).post(...)`
(`hac/exec/.../http/HacHttpClient.kt`); it owns login, cookie/CSRF caching, auto re-login,
replica routing, SSL trust, timeouts. Pass `context.timeout`/`context.replicaContext` through.

## MCP toolsets (`modules/<feature>/mcp`)

- Toolset class thin: plain `class XxxMcpToolset : McpToolset` (no `@Service`), registered via
  `<mcpServer.mcpToolset implementation="..."/>`. Each tool: `suspend fun` with
  `@McpTool(name = "sap_commerce_...")` + `@McpDescription`, every param `@McpDescription`-annotated
  and defaulted if optional; returns `String` (serialized JSON). Project via
  `currentCoroutineContext().project`.
- Business logic in a `@Service(Service.Level.PROJECT) XxxMcpService`; inputs a small
  `XxxMcpContext`, outputs `@Serializable` DTOs in `dto/`. Bridge to exec layer via
  `HacMcpService.resolveConnection(connectionName)` → `ExecContext`.
- Serialization: never call `Json` directly from a tool — `resolveMapper(outputFormat)` +
  `mapper.map(dto)` (`ai/mcp/`; shared `Json` is `prettyPrint = false, explicitNulls = false`).
  Optional fields: nullable with default `null` (omitted from output); `isX` flags →
  `@SerialName("x")`.
- Derive computed fields in the DTO itself (defaulted constructor property referencing earlier
  params, ordered after its dependency) — callers construct DTOs with only real inputs.
- No try/catch in tools — `error(...)`/`require(...)` (surfaces to MCP client), or model failure
  as DTO fields (`success = false`, `error`, `errorDetail`).
- Exemplar: `groovy/mcp/` (`GroovyMcpToolset.kt`, `GroovyMcpService.kt`, `dto/GroovyResult.kt`).

## Actions & UI

- Actions extend `DumbAwareAction` (or mix in `DumbAware`), `getActionUpdateThread() =
  ActionUpdateThread.BGT` (EDT only if `update()` touches Swing), guard
  `event.presentation.isEnabledAndVisible = event.isHybrisProject`, suppress Search-Everywhere
  noise via `ifNotFromSearchPopup`/`hideFromSearchPopup` (`shared/core/.../Util.kt`). Register in
  the module descriptor `<actions>` with dotted ids.
- Forms: Kotlin UI DSL `panel { group { row { ... } } }` with `bindSelected`/`bindText`/`bindItem`,
  `contextHelp`, `visibleIf`/`enabledIf`, `RowLayout.PARENT_GRID`; scrollable content
  `.align(Align.FILL).resizableColumn()` inside `.resizableRow()`. Validation:
  `validationOnInput`/`addValidationRule` + `registerValidators(disposable)`. Reuse shared DSL
  extensions in `shared/ui/.../ui/Dsl.kt` before hand-rolling.
- Dialogs: subclass `DialogWrapper`, `Project` first constructor arg; `init` sets `title` (+flags)
  first, `super.init()` last; body in `createCenterPanel() = panel { ... }`; compact dialogs
  override `getStyle() = DialogStyle.COMPACT`. Canonical:
  `project/import-ui/.../ProjectRefreshDialog.kt`.
- Notifications: fluent builder in `shared/core/.../Notifications.kt`
  (`Notifications.error(...).hideAfter(...).addAction(...) { }.notify(project)`) — never
  `NotificationGroupManager` directly; single group `"[y] SAP Commerce"`.
- Icons: `val`s in `object HybrisIcons`, nested per feature; SVGs under
  `shared/core/resources/icons/`; platform icons via `AllIcons.*`.
- i18n: all user-facing strings via top-level `i18n("key", vararg params)`
  (`i18n/HybrisBundle.properties`); `i18nFallback` when the key may be absent.
- In-editor result panels extend `shared/ui/.../ui/editor/InEditorResultsView.kt`.
- Line markers extend `HybrisLineMarkerProvider<T>` (forces slow-marker collection + hybris
  guard) — never `LineMarkerProvider` directly.
- Single tool window `"SAP CX"` — contribute content, don't register new tool windows.

## Custom language & PSI features

- Inspections (custom languages): extend `LocalInspectionTool`, name `<Lang><Meaning>Inspection`,
  delegate to a private inner visitor extending the generated `<Lang>Visitor`, report via
  `holder.registerProblem(element, i18n(...), ...)`, override `getDefaultLevel()`.
- Inspections (XML DOM models): extend the per-model abstract base (`TSInspection`,
  `BSInspection`, `CngConfigInspection`, ...) built on shared `AbstractInspection<T : DomElement>`.
- Registration: `<localInspection>` in the module descriptor with `groupPath="SAP Commerce"`,
  `groupName="[y] <Lang>"`, `enabledByDefault="true"`, `level`, `shortName` = class name; ship
  description HTML `<ShortName>.html` under `resources/inspectionDescriptions/`. Quick fixes in
  sibling `codeInspection/fix/`.
- PSI helpers: top-level functions in `<Lang>PsiUtil.kt` (`@file:JvmName`), wired via BNF
  `psiImplUtilClass`. Hand-written PSI behavior in `psi/impl/<Lang><Rule>Mixin.kt` abstract
  classes, attached via BNF `mixin="..."`.
- In-memory PSI files: prefer the language's `psi/<Lang>ElementFactory.createFile(project, text)`
  when it exists; otherwise `PsiFileFactory` directly.
- References: extend the per-language `<Lang>ReferenceBase` (`PsiReferenceBase.Poly`); implement
  `multiResolve` via `getParameterizedCachedValue` with companion `Key`/stateless provider, filter
  with shared `getValidResults(...)`. Cache dependency: the feature's own modification tracker for
  meta-model-backed resolves, `PsiModificationTracker.MODIFICATION_COUNT` for pure-PSI. Canonical:
  `impex/core/.../references/ImpExTSItemReference.kt`.
- Completion: one `<Lang>CompletionContributor` wiring `extend(...)` in `init`; patterns in
  `<Lang>Patterns`; providers as separate classes in `codeInsight/completion/provider/`. Lookup
  elements only via the per-language `object <Lang>LookupElementFactory` in `codeInsight/lookup/`.
- Meta-model access: never read the type-/bean-system model directly — go through
  `TSMetaModelAccess`/`BSMetaModelAccess.getInstance(project)`. They throw
  `ProcessCanceledException` while the model is uncomputed/dumb — call from a cancellable
  read-action context, never swallow PCE.
- Annotators extend shared `AbstractAnnotator`; highlighters, folding builders, formatters follow
  the same per-language layout, registered per language in the module descriptor.

## New module checklist

1. `modules/<feature>/<layer>/build.gradle.kts` — auto-discovered by `settings.gradle.kts`
   (project name = path with `/` → `-`). Minimal: `org.jetbrains.intellij.platform.module` +
   `alias(libs.plugins.kotlin)` (+ serialization if needed), source sets `src`/`resources`/`tests`,
   explicit `implementation(project(":..."))` deps (respect layering — `core` never depends on
   `ui`), catalog libs via `libs.*`, bundled IDE plugins under `intellijPlatform {}`. Template:
   `groovy/mcp/build.gradle.kts`.
2. Descriptor `resources/META-INF/sap.commerce.toolset-<group>-<layer>.xml` — register the
   module's extensions/actions there.
3. Matching `<xi:include>` in root `resources/META-INF/plugin.xml` (mcp modules use
   `<depends optional="true" config-file="..."/>` instead) and module in
   `pluginComposedModule(...)` in root `build.gradle.kts`.
4. Versions in `gradle/libs.versions.toml` / `gradle.properties` — never inline.

## Never call `@ApiStatus.Internal` platform SDK APIs

`./gradlew verifyPlugin` rejects `@ApiStatus.Internal` platform symbols and they carry no
compatibility guarantee. Internal classes often look like ordinary entry points — IntelliJ's own
bundled code uses them in ways unsafe to copy. Before using an unfamiliar platform class/method:

1. Locate the platform sources jar:
   `~/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea/idea/<version>/*/idea-<version>-sources.jar`;
   `unzip -p` the class and grep above the `class`/method declaration for `@ApiStatus.Internal`.
2. No sources jar: decompile via the bundled Fernflower
   (`plugins/java-decompiler/lib/java-decompiler.jar` in the extracted IDE dist under
   `~/.gradle/caches/*/transforms/`).
3. Only class-/member-level `@ApiStatus.Internal` matters — judge by the annotation, not the
   package (`.impl`/`.ex` packages contain stable APIs; normal packages contain internal ones).
   `@Obsolete` is soft deprecation — fine to use, doesn't block verification.

When internal functionality is genuinely needed, find the public entry point one layer up instead
of reaching into internals. Precedent (`impex/mcp/.../ImpExValidationMcpService.kt`): instead of
internal `DaemonCodeAnalyzerImpl`/`HighlightingSessionImpl` for headless highlighting — walk
`PsiErrorElement`s for syntax plus public `LocalInspectionTool.processFile` per inspection from
`InspectionProfile.getInspectionTools(...)`. APIs asserting an active indicator get
`coroutineToIndicator { ... }` from `suspend` (ties cancellation to the coroutine `Job`), not
`ProgressManager.runProcess(..., EmptyProgressIndicator())` which never cancels.

## Verify before finishing

Compile ≠ convention compliance. Re-check diff against every touched heading above (house style
always; then area-specific); confirm unfamiliar/internal-looking APIs were checked, not assumed.

```
./gradlew :<module>:compileKotlin
```