# SAP Commerce plugin — dev conventions

Shared helpers: `modules/shared/core` (`Util.kt`, `Notifications.kt`, `HybrisIcons.kt`, `HybrisConstants.kt`, `i18n`) and `modules/shared/ui` — check before writing a new helper.

## Packages & extensions

- Production code under `sap.commerce.toolset.<feature>`. Class prefixes: `ImpEx*`, `FlexibleSearch*`/`FxS*`, `PgQ*`, `Acl*`, `TS*`, `BS*`, `Cng*`, `Cx*`, `Hac*`, `CCv2*`.
- Extensions bail out early: `init { if (project.isNotHybrisProject) throw ExtensionNotApplicableException.create() }`. Per-call: `isHybrisProject`/`isNotHybrisProject`/`ifHybrisProject` on `Project`, `PsiElement`, `AnActionEvent`, `DataContext`.
- `project.directory` over deprecated nullable `project.basePath`.
- `import com.intellij.util.application` over `ApplicationManager.getApplication()`; check `com.intellij.util.*` before `XyzManager.getInstance(...)`.
- Never hand-edit `gen/` — regenerate via Grammar-Kit.

## Kotlin style

Formatting: `.idea/codeStyles/Project.xml` (`KOTLIN_OFFICIAL`). No `.editorconfig`.

- LGPL license header on every hand-written `.kt`/`.java` (`gen/` exempt; never hand-tweak years).
- 4-space indent; max line 180 (Kotlin) / 150 (Java); trailing commas on multiline; wildcard imports fine.
- Expression-body for single-expression functions including overrides.
- Nullability: `?.let {}`, `?.takeIf {}`, elvis (`?: return`, `?: false`). Never `as?` — use `asSafely<T>()`.
- Kotlin stdlib collections only (no Java streams); `mapNotNull` over `filter{}.map{}`; `firstOrNull`/`find` for lookups; `.asSequence()` only for genuinely lazy pipelines.
- Enums: data as constructor `val`s; reverse-lookup companion `of(...)`/`from(...)` via `entries.find { ... }`.
- Constants: `object HybrisConstants` (global) / `object <Prefix>Constants` (per feature); `const val`, SCREAMING_SNAKE_CASE, grouped into nested objects.
- Extension functions go into the matching `Util.kt` — no parallel `*Extensions.kt`.
- Visibility: `private` liberally, `internal` deliberately, never explicit `public`. DTOs: `data class` in `dto/`.
- Member order: constructor properties → computed → functions → nested types → `companion object` last.
- `// TODO` (spaced); deprecations carry a migration message.
- **One top-level class per file.** Exception: a `private` helper used only by that class.
- **Column style** for >2–3 args: one per line, trailing comma; add names to positional calls going column style.

## Services & DI

- Light services only: `@Service(Service.Level.PROJECT)` / `@Service` (app) on the class — no `<projectService>`/`<applicationService>` XML.
- `getInstance` companion on every service: `project.service()` / `application.service()`; MCP suspend: `currentCoroutineContext().project.service<Xyz>()`. Never `project.getService(X::class.java)`.
- `CoroutineScope` constructor-injected (second param after `project`) — never `CoroutineScope()`/`GlobalScope`.
- Naming: `*Service` (logic), `*Settings` (config), `*Client` (exec/HTTP), `*Manager` (app registries), `*Access`/`*StateService` (meta-model).
- Swappable impls: extension point — interface with `val EP = ExtensionPointName.create<T>("sap.commerce.toolset...")`, `dynamic="true"` in descriptor, impl registered in XML.
- Events: `*Listener` in `event/` with no-op defaults and companion `TOPIC`. Publish via `messageBus.syncPublisher(TOPIC)`, subscribe via `messageBus.connect(disposable)` — no listener XML.

## Settings & persistence

- `SerializablePersistentStateComponent<XxxState>` with all-`val` `*SettingsState` data class in `settings/state/`; writes via `updateState { it.copy(...) }`; usually implements `ModificationTracker`. Never `BaseState`/`SimplePersistentStateComponent`/mutable fields.
- `@State`: `name = "[y] ..."`, storage from `HybrisConstants.STORAGE_*`, `roamingType = RoamingType.LOCAL`; app-level adds `category = SettingsCategory.PLUGINS`; workspace-scoped uses `Storage(StoragePathMacros.WORKSPACE_FILE)`. State class: `@Tag` on class, `@JvmField @OptionTag` per scalar (collections: `@JvmField` only).
- UI: immutable⇄mutable pair — `mutable()` → nested `Mutable` of `ObservableMutableProperty`, `immutable()` back.
- Secrets: `PasswordSafe.instance` keyed by `CredentialAttributes("SAP CX - <uuid>")`, read/written on background task. Never in `*State`.
- Settings pages: `BoundSearchableConfigurable` + UI DSL, via `ConfigurableProvider` with `canCreateConfigurable() = project.isHybrisProject`.
- Remote connections: `*ConnectionSettingsState : ExecConnectionSettingsState`, persisted as lists in `*ExecDeveloperSettings`/`*ExecProjectSettings`, active by UUID, managed by `*ExecConnectionService`.

## Coroutines & threading

- `readAction { }` (not `runReadAction`), `edtWriteAction { }`, `withContext(Dispatchers.EDT)`.
- Long ops: `withBackgroundProgress(project, title, true)` + `reportProgressScope` — never manual `ProgressIndicator`. `coroutineToIndicator` only for unavoidable legacy indicator APIs.
- Read actions: scope to bare data retrieval only; filter/map after the block. Nullable return, `?: error(...)` after block — never throw inside.
- `if (project.isDisposed) return@launch` at top of launched blocks touching the project.

## Exec layer

New remote feature = `modules/<feature>/exec`, four pieces (base classes in `exec/core/.../exec/`; exemplar: `groovy/exec/`):

1. **`XxxExecContext`** — immutable `data class : ExecContext`; `connection`, `content`, `timeout`; overrides `executionTitle`, `params()`; nested `Settings`/`Settings.Mutable`; companion `defaultSettings(...)`.
2. **`XxxExecResult`** — usually `DefaultExecResult`; own type only for extra fields.
3. **`XxxExecClient`** — project light service extending `DefaultExecClient<XxxExecContext>`; implement only `suspend fun execute(context)`.
4. Optional **`XxxExecService`** façade: builds contexts, delegates to client, handles notifications.

Never inline HAC calls — use `HacHttpClient.getInstance(project).post(...)`.

## MCP toolsets

- Thin `class XxxMcpToolset : McpToolset` (no `@Service`), registered via `<mcpServer.mcpToolset implementation="..."/>`.
- Each tool: `suspend fun` with `@McpTool(name = "sap_commerce_...")` + `@McpDescription`; every param `@McpDescription`-annotated and defaulted if optional; returns `String` (serialized JSON). Project via `currentCoroutineContext().project`.
- Business logic in `@Service(Service.Level.PROJECT) XxxMcpService`; small `XxxMcpContext` input, `@Serializable` DTOs in `dto/` output.
- Serialization: `resolveMapper(outputFormat)` + `mapper.map(dto)` — never call `Json` directly. Optional fields: nullable with default `null`; `isX` flags → `@SerialName("x")`.
- Computed DTO fields: defaulted constructor property ordered after its dependency.
- No try/catch — `error(...)`/`require(...)`, or model failure as DTO fields (`success = false`, `error`).
- Exemplar: `groovy/mcp/`.

## Actions & UI

- Actions: extend `DumbAwareAction`, `getActionUpdateThread() = ActionUpdateThread.BGT`, guard `event.presentation.isEnabledAndVisible = event.isHybrisProject`. Register in module descriptor `<actions>` with dotted ids.
- Forms: Kotlin UI DSL `panel { group { row { ... } } }` with `bindSelected`/`bindText`/`bindItem`, `contextHelp`, `visibleIf`/`enabledIf`. Reuse `shared/ui/.../ui/Dsl.kt` before hand-rolling.
- Dialogs: subclass `DialogWrapper`, `Project` first; `init` sets `title` first, `super.init()` last; body in `createCenterPanel() = panel { ... }`. Compact: `getStyle() = DialogStyle.COMPACT`.
- Notifications: `Notifications.error(...).hideAfter(...).addAction(...) { }.notify(project)` — never `NotificationGroupManager` directly.
- Icons: `object HybrisIcons`, nested per feature; SVGs under `shared/core/resources/icons/`.
- i18n: `i18n("key", vararg params)` for all user-facing strings; `i18nFallback` when key may be absent.
- In-editor result panels: extend `shared/ui/.../ui/editor/InEditorResultsView.kt`.
- Line markers: extend `HybrisLineMarkerProvider<T>` — never `LineMarkerProvider` directly.
- Single tool window `"SAP CX"` — contribute content, don't register new tool windows.

## Custom language & PSI

- Inspections (custom languages): `LocalInspectionTool`, name `<Lang><Meaning>Inspection`, delegate to inner `<Lang>Visitor`, report via `holder.registerProblem(...)`. Register as `<localInspection>` with `groupPath="SAP Commerce"`, `groupName="[y] <Lang>"`; ship description HTML under `resources/inspectionDescriptions/`; quick fixes in `codeInspection/fix/`.
- Inspections (XML DOM): extend per-model abstract base (`TSInspection`, `BSInspection`, etc.).
- PSI helpers: top-level functions in `<Lang>PsiUtil.kt` (`@file:JvmName`). Hand-written behavior in `psi/impl/<Lang><Rule>Mixin.kt`, attached via BNF `mixin="..."`.
- In-memory PSI: `psi/<Lang>ElementFactory.createFile(project, text)` when it exists; otherwise `PsiFileFactory`.
- References: extend `<Lang>ReferenceBase`; `multiResolve` via `getParameterizedCachedValue` with companion `Key`. Cache on feature's modification tracker (meta-model-backed) or `PsiModificationTracker.MODIFICATION_COUNT` (pure-PSI).
- Completion: one `<Lang>CompletionContributor` wiring in `init`; patterns in `<Lang>Patterns`; lookup elements via `object <Lang>LookupElementFactory`.
- Meta-model: never read type-/bean-system model directly — `TSMetaModelAccess`/`BSMetaModelAccess.getInstance(project)`. Never swallow `ProcessCanceledException`.
- Annotators: extend `AbstractAnnotator`.

## New module checklist

1. `modules/<feature>/<layer>/build.gradle.kts` — `org.jetbrains.intellij.platform.module` + kotlin plugin (+ serialization if needed); source sets `src`/`resources`/`tests`; explicit `implementation(project(":..."))` deps (`core` never depends on `ui`); `libs.*` versions. Template: `groovy/mcp/build.gradle.kts`.
2. Descriptor `resources/META-INF/sap.commerce.toolset-<group>-<layer>.xml`.
3. `<xi:include>` in root `plugin.xml` (MCP modules: `<depends optional="true" config-file="..."/>`); add to `pluginComposedModule(...)` in root `build.gradle.kts`.
4. All versions in `gradle/libs.versions.toml` / `gradle.properties` — never inline.

## @ApiStatus.Internal

`verifyPlugin` rejects internal platform symbols. Before using an unfamiliar platform API:

1. Check platform sources jar for `@ApiStatus.Internal` above the declaration:
   `~/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea/idea/<version>/*/idea-<version>-sources.jar`
2. No sources: decompile via bundled Fernflower (`plugins/java-decompiler/lib/java-decompiler.jar`).
3. Only class-/member-level annotation matters — `.impl` packages may be stable; normal packages may be internal. `@Obsolete` is fine.

When internal functionality is needed, find the public entry point one layer up. Example: instead of `DaemonCodeAnalyzerImpl` for headless highlighting — walk `PsiErrorElement`s + `LocalInspectionTool.processFile` from `InspectionProfile.getInspectionTools(...)`. Unavoidable indicator APIs: `coroutineToIndicator { ... }` from suspend (not `ProgressManager.runProcess(..., EmptyProgressIndicator())`).
