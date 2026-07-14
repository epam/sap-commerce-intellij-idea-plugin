# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

The **SAP Commerce Developers Toolset** — an IntelliJ IDEA plugin (published as plugin `12867`, id `com.intellij.idea.plugin.sap.commerce`) that adds SAP Commerce / Hybris support to IntelliJ-based IDEs. It is built on the IntelliJ Platform Gradle Plugin 2.x, written in **Kotlin (K2)**, targeting **JDK 21 (JetBrains runtime)**.

## Build, run, test

All commands use the Gradle wrapper. The build uses the configuration cache and build cache (see `gradle.properties`).

- **Run the plugin in a sandbox IDE:** `./gradlew runIde` (or the `Run IDE` run configuration). Sandbox runs set `sap.commerce.toolset.mode=sandbox`, which enables the `sap.commerce.toolset.isSandbox` flag for dev-only features.
- **Build the distributable:** `./gradlew buildPlugin`
- **Run all tests:** `./gradlew test` (JUnit Platform). Tests live in each module's `tests/` dir and in the root `tests/`.
- **Run a single test class:** `./gradlew test --tests "sap.commerce.toolset.businessProcess.util.BpHelperTest"`
- **Verify plugin compatibility:** `./gradlew verifyPlugin` (the `Run Verifications` run configuration); checks against IDEA EAP + RELEASE channels between `intellij.plugin.since.build` and `until.build`.
- **Target IDE / plugin version** are set in `gradle.properties` (`intellij.version`, `intellij.plugin.version`, `since.build`, `until.build`), *not* in `build.gradle.kts`.

### GITHUB_TOKEN for builds

`processResources` runs a `fetchPRs` task that queries GitHub for PRs labeled `Requires - Project Refresh` / `Requires - Project Reimport` and writes `resources/prs.json` (drives the project-import-state dialogs). It needs env var `GITHUB_TOKEN` (a classic PAT with `repo:public_repo`). To skip it locally, set `GITHUB_SKIP_TASK_FETCH_PRS=true`.

## Architecture

### Multi-module layout

The plugin is split into multiple Gradle subprojects under `modules/`. `settings.gradle.kts` auto-discovers every `modules/**/build.gradle.kts` (depth ≤ 4) and derives the Gradle project name by stripping the `modules/` prefix and replacing path separators with `-`. So `modules/impex/core` → project `:impex-core`, consumed as `project(":impex-core")`.

Modules are grouped by **feature** (e.g. `impex`, `flexibleSearch`, `acl`, `polyglotQuery`, `typeSystem`, `beanSystem`, `cockpitNG`, `project`, `hac`, `solr`, `ccv2`, `spring`, `debugger`, …), and each feature is further split by **layer** by a consistent sub-module naming convention:

- **`core`** — language/PSI/domain model: lexers, parsers, PSI, references, inspections, domain services. No remote/IO concerns.
- **`ui`** — actions, tool windows, dialogs, editors, line markers, settings UI.
- **`exec`** — execution against a *remote* SAP Commerce instance (e.g. run ImpEx/Groovy/FlexibleSearch/Solr queries).
- **`project`** — project-import configurators and facet/module setup.
- **`diagram` / `monitoring` / `manifest` / `decompiler` / etc.** — narrower concerns.

`modules/shared/{core,ui,emitter}` holds cross-cutting code depended on by most features. Module dependencies are explicit `implementation(project(":..."))` entries in each module's `build.gradle.kts`; respect the layering (e.g. `core` should not depend on `ui`).

The **root project** (`src/`, `resources/`) is the assembling plugin module — startup activities, the main tool window, language injectors, editors. The root `build.gradle.kts` pulls every subproject in via `pluginComposedModule(...)` so they ship as composed plugin modules of one distributable.

### Plugin descriptor composition

There is **no single monolithic plugin.xml**. `resources/META-INF/plugin.xml` declares the plugin id/dependencies and then `<xi:include>`s one descriptor per module (named `sap.commerce.toolset-<group>-<layer>.xml`, located in each module's `resources/META-INF/`). When you add an extension point / action / service in a module, register it in *that module's* descriptor, and add the corresponding `<xi:include>` to the root `plugin.xml` if the module is new.

### Code conventions

All Kotlin code-style, file-organization, package, and IntelliJ Platform SDK-safety conventions for
writing code in this plugin — including how to verify generated code follows them — live in
**`skills/sap-commerce-plugin-dev/SKILL.md`**. Read it before adding or modifying Kotlin source.
See **`TECH_NOTES.md`** for other established patterns: invoking actions, refreshing `AnAction`
state from background threads, DSL form validators, resizing Kotlin DSL dialogs, and `GotItTooltip`
registration (`modules/shared/core/.../GotItTooltips.kt`).

### Custom languages (Grammar-Kit / JFlex)

ImpEx, ACL, FlexibleSearch, and Polyglot Query are full custom languages. Each `*-core` module has a `.flex` lexer grammar and a `.bnf` Grammar-Kit grammar (e.g. `modules/impex/core/src/sap/commerce/toolset/impex/ImpEx.{flex,bnf}`). Generated lexers/parsers/PSI are committed under the module's **`gen/`** dir (a source root, listed as a generated source dir). Regenerate them with the JetBrains **Grammar-Kit** IDE plugin (the root `jflex-1.9.2.jar` and `idea-flex.skeleton` support JFlex generation) — there is no Gradle task for this. Do not hand-edit files in `gen/`.

### Dependencies & versions

Library and plugin versions are centralized in the Gradle **version catalog** `gradle/libs.versions.toml` (referenced as `libs.*`). The build also depends on bundled IntelliJ plugins (Java, Spring, Database, Groovy, Diagram, JavaEE, Maven, Eclipse, Kotlin, etc.) and optional/`compatiblePlugins` (Ant, JRebel, Angular, PsiViewer) — declared in the root `build.gradle.kts`.

### Other build pieces

- **`gradle/build-logic/`** — an included build providing the `sap-commerce-developers-toolset-gradle-plugin`, whose main job is the `CxFetchPRsGradleTask` (the `fetchPRs` task above).
- **`jps-plugin/`** — a separate JPS (build-process) module for SAP Commerce's custom compilation; depended on by the root project but excluded from `pluginComposedModule`.

## Releasing / PR conventions

- Changelog is maintained in **`CHANGELOG.md`** and surfaced via the `org.jetbrains.changelog` Gradle plugin (don't hand-craft plugin change notes; they're rendered from the changelog). The README's `<!-- Plugin description -->` / `<!-- Plugin description end -->` block is the source of the marketplace description — keep those markers intact.
- If a change requires users to refresh or reimport their SAP Commerce project, label the PR `Requires - Project Refresh` or `Requires - Project Reimport` and bump `ProjectImportConstants.MIN_REFRESH_API_VERSION` / `MIN_IMPORT_API_VERSION` to the exact plugin version (see `TECH_NOTES.md`).
- Commits and PRs must include `Signed-off-by: Real Name <email>` (DCO), per `CONTRIBUTING.md`.
