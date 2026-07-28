# SAP Commerce plugin — dev conventions

Shared helpers: `modules/shared/core` (`Util.kt`, `Notifications.kt`, `HybrisIcons.kt`, `HybrisConstants.kt`, `i18n`) and `modules/shared/ui` — check before writing a new helper.

## Packages & extensions

- Code under `sap.commerce.toolset.<feature>`. Prefixes: `ImpEx*`, `FlexibleSearch*`/`FxS*`, `PgQ*`, `Acl*`, `TS*`, `BS*`, `Cng*`, `Cx*`, `Hac*`, `CCv2*`.
- Extensions bail out early: `init { if (project.isNotHybrisProject) throw ExtensionNotApplicableException.create() }`. Per-call: `isHybrisProject`/`isNotHybrisProject`/`ifHybrisProject` on `Project`, `PsiElement`, `AnActionEvent`, `DataContext`.
- `project.directory` not `project.basePath`. `import com.intellij.util.application` not `ApplicationManager.getApplication()`; check `com.intellij.util.*` before `XyzManager.getInstance(...)`.
- Never hand-edit `gen/` — regenerate via Grammar-Kit.

## Kotlin style

Formatting: `.idea/codeStyles/Project.xml` (`KOTLIN_OFFICIAL`). No `.editorconfig`.

- LGPL header on every `.kt`/`.java` (`gen/` exempt; never tweak years).
- 4-space indent; line ≤180 (Kotlin) / ≤150 (Java); trailing commas; wildcard imports ok.
- Expression-body for single-expression functions/overrides.
- Nullability: `?.let`, `?.takeIf`, elvis (`?: return`, `?: false`). Never `as?` — use `asSafely<T>()`.
- Collections: stdlib only; `mapNotNull`; `firstOrNull`/`find`; `.asSequence()` for genuinely lazy pipelines only.
- Enums: data as constructor `val`s; companion `of`/`from` via `entries.find { }`.
- Constants: `object HybrisConstants` / `object <Prefix>Constants`; `const val`, SCREAMING_SNAKE_CASE, nested objects.
- Extension functions → matching `Util.kt`. No parallel `*Extensions.kt`.
- Visibility: `private` liberally, `internal` deliberately, no explicit `public`. DTOs: `data class` in `dto/`.
- Member order: constructor props → computed → functions → nested types → `companion object` last.
- `// TODO` (spaced). **One top-level class per file** (exception: `private` helper for that class). Column style for >2–3 args.

## Services & DI

- Light services only: `@Service(Service.Level.PROJECT)` / `@Service` (app) — no `<projectService>`/`<applicationService>` XML.
- `getInstance` companion; `project.service()` / `application.service()`; MCP suspend: `currentCoroutineContext().project.service<Xyz>()`. Never `project.getService(X::class.java)`.
- `CoroutineScope` constructor-injected (2nd param after `project`) — never `CoroutineScope()`/`GlobalScope`.
- Naming: `*Service` (logic), `*Settings` (config), `*Client` (exec/HTTP), `*Manager` (registries), `*Access`/`*StateService` (meta-model).
- Swappable impls: EP with `val EP = ExtensionPointName.create<T>("...")`, `dynamic="true"`. Events: `*Listener` in `event/` with no-op defaults + companion `TOPIC`; `messageBus.syncPublisher(TOPIC)` / `messageBus.connect(disposable)` — no XML.

## Settings & persistence

- `SerializablePersistentStateComponent<XxxState>`; all-`val` `*SettingsState` in `settings/state/`; `updateState { it.copy(...) }`; usually implements `ModificationTracker`. Never `BaseState`/`SimplePersistentStateComponent`/mutable fields.
- `@State`: `name = "[y] ..."`, `HybrisConstants.STORAGE_*`, `RoamingType.LOCAL`; app adds `SettingsCategory.PLUGINS`; workspace uses `StoragePathMacros.WORKSPACE_FILE`. State class: `@Tag`, `@JvmField @OptionTag` per scalar, `@JvmField` for collections.
- UI: `mutable()` → `Mutable` of `ObservableMutableProperty`, `immutable()` back.
- Secrets: `PasswordSafe.instance` keyed by `CredentialAttributes("SAP CX - <uuid>")` on background. Never in `*State`.
- Settings pages: `BoundSearchableConfigurable` + DSL via `ConfigurableProvider`; `canCreateConfigurable() = project.isHybrisProject`.
- Remote connections: `*ConnectionSettingsState : ExecConnectionSettingsState`, persisted as lists in `*ExecDeveloperSettings`/`*ExecProjectSettings`, active by UUID via `*ExecConnectionService`.

## Coroutines & threading

- `readAction { }` (not `runReadAction`), `edtWriteAction { }`, `withContext(Dispatchers.EDT)`.
- Long ops: `withBackgroundProgress(project, title, true)` + `reportProgressScope` — never manual `ProgressIndicator`. `coroutineToIndicator` only for unavoidable legacy indicator APIs.
- Read actions: bare data retrieval only; filter/map outside the block. Nullable return, `?: error(...)` after — never throw inside.
- `if (project.isDisposed) return@launch` at top of launched blocks touching project.

## Exec layer

`modules/<feature>/exec` — four pieces (base classes in `exec/core/`; exemplar: `groovy/exec/`):

1. **`XxxExecContext`** — `data class : ExecContext`; `connection`, `content`, `timeout`; `executionTitle`, `params()`; nested `Settings`/`Settings.Mutable`; companion `defaultSettings(...)`.
2. **`XxxExecResult`** — usually `DefaultExecResult`; own type only for extra fields.
3. **`XxxExecClient`** — `@Service` extending `DefaultExecClient<XxxExecContext>`; implement `suspend fun execute(context)` only.
4. **`XxxExecService`** (optional) — builds contexts, delegates to client, handles notifications.

Never inline HAC calls — `HacHttpClient.getInstance(project).post(actionUrl, params, canReLogin, timeout, settings, replicaContext)`.
- `actionUrl` = `"${context.connection.generatedURL}/<path>"`; `params` = `context.params().map { BasicNameValuePair(it.key, it.value) }`; `settings` = `context.connection`.
- Check `statusCode == HttpStatus.SC_OK` before reading `response.entity.content`.

`DefaultExecResult` fields: `statusCode`, `output`, `result`, `errorMessage`, `errorDetailMessage`, `replicaContext`.

## MCP toolsets

- `class XxxMcpToolset : McpToolset` (no `@Service`); registered via `<mcpServer.mcpToolset implementation="..."/>`.
- Each tool: `suspend fun` + `@McpTool(name = "sap_commerce_...")` + `@McpDescription`; all params `@McpDescription`-annotated and defaulted if optional; returns `String`. Project via `currentCoroutineContext().project`.
- **Toolset = no logic**: `resolveMapper(outputFormat)` → resolve project → call service → `mapper.map(result)`.
- Logic in `@Service(Level.PROJECT) XxxMcpService`; `@Serializable` DTOs in `dto/`. Never call `Json` directly.
- No try/catch — `error(...)`/`require(...)` or failure as DTO fields (`success = false`, `error`).
- Exemplars: `typeSystem/mcp/` (typed meta-model search with detail levels — canonical), `shared/mcp/` (local EP query), `groovy/mcp/` (remote exec).
- When adding search-style tools: use a request object (`XxxSearchMcpRequest`) with `filter?`, `rawExtensions`, and an optional `detailLevel` enum (see `typeSystem/mcp/context/`). `TSMcpDataProvider` shows the standard pipeline: dumb-mode guard → `readAction {}` → `getAll<T>` → filter.
- Read `skills/dev-type-system.md` § "TypeSystem MCP Layer" before touching `typeSystem/mcp/`.

## Actions & UI

- Actions: `DumbAwareAction`, `getActionUpdateThread() = ActionUpdateThread.BGT`, guard `isEnabledAndVisible = event.isHybrisProject`. Register in module `<actions>` with dotted ids.
- Forms: `panel { group { row { } } }` with `bindSelected`/`bindText`/`bindItem`, `visibleIf`/`enabledIf`. Reuse `shared/ui/.../ui/Dsl.kt` before hand-rolling.
- Dialogs: `DialogWrapper`, `Project` first; `init` sets `title` first, `super.init()` last; `createCenterPanel() = panel { }`. Compact: `getStyle() = DialogStyle.COMPACT`.
- Notifications: `Notifications.error(...).hideAfter(...).addAction(...) { }.notify(project)`.
- Icons: `object HybrisIcons` nested per feature; SVGs in `shared/core/resources/icons/`.
- i18n: `i18n("key", vararg params)`; `i18nFallback` when key may be absent.
- In-editor results panels: extend `InEditorResultsView.kt` (`shared/ui`).
- Line markers: `HybrisLineMarkerProvider<T>`. Single tool window `"SAP CX"` — contribute content, don't register new.

### Split editor hierarchy

All language split editors share one abstract base and a fixed interface chain:

```
SplitEditor (shared/core)                         — textEditor val; DATA_KEY_SPLIT_EDITOR companion
  └─ ResultsSplitEditor (shared/core)             — inEditorResults, inEditorResultsTitle, inEditorResultsActions(), dismissInEditorResults()
       └─ ParameterizedSplitEditor (shared/core)  — inEditorParameters, virtualParameters (ImpEx / FxS / PgQ only)
            └─ SplitEditorEx (shared/ui)          — reparseTextEditor(delayMs)
                 └─ SplitEditorBase (shared/ui)   — abstract class; owns splitters, results panel, action bar
                      ├─ ImpExSplitEditorBase
                      ├─ FlexibleSearchSplitEditorBase
                      ├─ PolyglotQuerySplitEditorBase
                      ├─ GroovySplitEditorBase
                      └─ AclSplitEditorBase
```

Key contracts on `ExecutableSplitEditor`:
- `inEditorResults: Boolean` — persistent toggle; controls whether results show after execution.
- `dismissInEditorResults()` — clears current view only, does **not** change the toggle.
- `inEditorResultsTitle: String` — abstract `val`; every concrete editor must supply a heading.
- `inEditorResultsActions(): List<AnAction>` — hook for language-specific actions in the results bar (left side; default empty).

`SplitEditorBase.buildResultsPanel` wraps every assigned view (loader and data) with the action bar automatically.

### DSL action buttons with context data (`DataKey` + `sinkExtender`)

Use when a DSL action button needs context unavailable via standard `DataContext`.

**1. `DataKey` on the domain interface `companion object`** (never on a registered action):
```kotlin
interface SplitEditor : FileEditor, TextEditor {
    companion object {
        val DATA_KEY_SPLIT_EDITOR: DataKey<ResultsSplitEditor> =
            DataKey.create("sap.commerce.toolset.splitEditor")
    }
}
```

**2. Action reads the key** — use `ActionUpdateThread.EDT` when the button's `addNotify` triggers `update` on EDT:
```kotlin
class MyAction : DumbAwareAction(...) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT   // EDT if inside DSL ActionButton
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.getData(MY_KEY) != null
    }
    override fun actionPerformed(e: AnActionEvent) {
        e.getData(MY_KEY)?.doSomething()
    }
}
```

**3. Register** in module descriptor `<actions>` (no `add-to-group` needed).

**4. Inject via `sinkExtender`** inside `panel { row { } }`:
```kotlin
actionButton(
    ActionManager.getInstance().getAction("sap.commerce.toolset.myAction"),
    sinkExtender = { sink -> sink[MY_KEY] = this@MyComponent },
)
```

`ActionButtonSink` (in `Dsl.kt`) implements `UiDataProvider`; delegates `uiDataSnapshot` to `sinkExtender`.

## Custom language & PSI

- Inspections: `LocalInspectionTool` → `<Lang>Visitor` → `holder.registerProblem(...)`. `<localInspection groupPath="SAP Commerce" groupName="[y] <Lang>"/>`; HTML in `inspectionDescriptions/`; fixes in `codeInspection/fix/`. XML DOM: extend per-model base (`TSInspection`, `BSInspection`, …).
- PSI helpers: top-level in `<Lang>PsiUtil.kt` (`@file:JvmName`). Mixins: `psi/impl/<Lang><Rule>Mixin.kt` via BNF `mixin="..."`.
- In-memory PSI: `<Lang>ElementFactory.createFile(project, text)` when it exists; otherwise `PsiFileFactory`.
- References: `<Lang>ReferenceBase`; `multiResolve` via `getParameterizedCachedValue`. Cache on feature tracker or `PsiModificationTracker.MODIFICATION_COUNT`.
- Completion: one `<Lang>CompletionContributor`; patterns in `<Lang>Patterns`; elements via `object <Lang>LookupElementFactory`.
- Meta-model: `TSMetaModelAccess`/`BSMetaModelAccess.getInstance(project)`. Never swallow `ProcessCanceledException`. See `skills/dev-type-system.md` / `skills/dev-bean-system.md`.
- Annotators: `AbstractAnnotator`.

## New module checklist

1. `build.gradle.kts` — `org.jetbrains.intellij.platform.module` + kotlin (+ serialization if needed); source sets; `implementation(project(":..."))` deps; `libs.*` versions. Template: `groovy/mcp/build.gradle.kts`.
2. Descriptor `resources/META-INF/sap.commerce.toolset-<group>-<layer>.xml`.
3. `<xi:include>` in root `plugin.xml`; add to `pluginComposedModule(...)`. MCP: `<depends optional="true" config-file="..."/>`.
4. All versions in `gradle/libs.versions.toml` / `gradle.properties` — never inline.

## @ApiStatus.Internal

`verifyPlugin` rejects internal symbols. Before using an unfamiliar API:
1. Check sources jar for `@ApiStatus.Internal`: `~/.gradle/caches/.../idea-<version>-sources.jar`.
2. No sources: decompile via `plugins/java-decompiler/lib/java-decompiler.jar`.
3. Class-/member-level annotation only matters; `.impl` packages may be stable; `@Obsolete` is fine.

Find the public entry point one layer up. Unavoidable legacy indicator APIs: `coroutineToIndicator { }` from suspend.
