# SAP Commerce Developers Toolset – AI Agent Guidelines

## Project
IntelliJ IDEA plugin · id `com.intellij.idea.plugin.sap.commerce` · marketplace `12867`
Kotlin (K2) · JDK 25 (JetBrains runtime) · IntelliJ Platform Gradle Plugin 2.x

## Build & Test
```bash
./gradlew runIde                              # run in sandbox IDE
./gradlew buildPlugin                         # distributable
./gradlew test                                # all tests (JUnit Platform)
./gradlew test --tests "a.b.ClassName"        # single class
./gradlew verifyPlugin                        # IDE compatibility check
GITHUB_SKIP_TASK_FETCH_PRS=true ./gradlew … # skip GitHub PR fetch locally
```
Versions in `gradle.properties` (`intellij.version`, `intellij.plugin.version`, `since/until.build`) — not in `build.gradle.kts`.

## Architecture

### Module layout
Subprojects under `modules/`, auto-discovered (depth ≤ 4).
Name: strip `modules/`, replace `/` with `-` → `modules/impex/core` = `:impex-core`.

Feature layers:
- `core` — PSI, references, inspections, domain. No IO/remote.
- `ui` — actions, tool windows, dialogs, editors.
- `exec` — execution against a remote SAP Commerce instance.
- `project` — project import, facets.

Shared: `modules/shared/{core,ui,emitter}`. Root assembles all via `pluginComposedModule(...)`.
Module dependencies: explicit `implementation(project(":…"))` in each module's `build.gradle.kts`; respect layering (`core` must not depend on `ui`).

### Plugin XML
No monolithic `plugin.xml`. Root uses `<xi:include>` per module.
Register EPs/services/actions in the module's own `resources/META-INF/sap.commerce.toolset-<group>-<layer>.xml`; add `<xi:include>` to root `plugin.xml` only when adding a new module.

### Custom languages
ImpEx, ACL, FlexibleSearch, Polyglot Query: JFlex + Grammar-Kit.
Generated PSI lives under `gen/` (committed, listed as generated source root).
Regenerate with the JetBrains Grammar-Kit IDE plugin. **Never hand-edit `gen/`.**

### Dependencies
All versions in `gradle/libs.versions.toml` (use `libs.*` references).

### Other build pieces
- `gradle/build-logic/` — included build; provides `CxFetchPRsGradleTask` (`fetchPRs`).
- `jps-plugin/` — JPS module for SAP Commerce compilation; excluded from `pluginComposedModule`.

### Code conventions
See `TECH_NOTES.md` for: action invocation, background-thread patterns, dialog sizing, GotItTooltip.

## Workflow

### Phase 1 — Read skills first (mandatory; no exploration or coding before this is done)

Read each applicable skill **completely and sequentially** before opening any file, running any search, or writing any code. Reading in parallel with exploration defeats the purpose — skills exist to replace exploration.

| Always read | Skill |
|---|---|
| Any Kotlin change | `skills/dev-plugin.md` |
| Any commit or PR | `skills/workflow-github.md` |

| Area | Skill |
|---|---|
| ImpEx | `skills/lang-impex.md` |
| FlexibleSearch | `skills/lang-flexible-search.md` |
| Polyglot Query | `skills/lang-polyglot-query.md` |
| Type System (items.xml, MCP, TSMeta*) | `skills/dev-type-system.md` |
| Bean System (beans.xml, BSMeta*) | `skills/dev-bean-system.md` |
| Cockpit NG | `skills/dev-cockpit-ng.md` |

### Phase 2 — Implementation

1. Identify the affected area and create a feature branch: `git checkout -b <area>-<short-topic>`.
2. Implement focused change; commit iterative progress as you work — don't accumulate all changes into one diff.
3. Before each commit verify affected modules compile: `GITHUB_SKIP_TASK_FETCH_PRS=true ./gradlew <module>:compileKotlin`.
4. Run relevant tests.
5. Review modified files — remove unrelated changes, no formatting noise.
6. Commit message matches repository style (see `skills/workflow-github.md`).

Because PRs are squash merged: intermediate commits are for convenience; do not create artificial micro-commits.

## Maintenance
Whenever `AGENTS.md` is modified, update the agent's persistent memory (`MEMORY.md`) to reflect the change so that in-context guidance stays consistent with the file.

## Code Quality
- No secrets, credentials, or local env files.
- No hand-editing `gen/` files.
- Avoid unnecessary generated-file commits.
- Small, focused, reviewable, easy-to-revert changes.
- New/changed logic: cover with atomic unit tests (single behavior per test) in the relevant module's own test source set — required before merge, not necessarily written first. TDD encouraged for `core`/pure logic; test-after acceptable for `exec` (remote IO) and `ui` (Swing glue).
