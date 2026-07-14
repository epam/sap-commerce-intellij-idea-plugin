---
name: sap-commerce-plugin-dev
description: >-
  All Kotlin code-style, file-organization, package, and IntelliJ Platform SDK safety conventions
  for the SAP Commerce Developers Toolset plugin. This is the single source of truth for "how do I
  write code in this repository" — use it whenever writing or reviewing Kotlin source: new classes,
  DTOs, services, MCP tools, inspections, actions, extensions. Trigger on "add a class", "add a
  DTO", "create a service", "create an extension", "which package", "one class per file", "column
  style", "constructor formatting", "can I use this IntelliJ API", "is this API internal",
  "ApiStatus.Internal", "ApplicationManager", "verify conventions", "check code style", or any
  request to add, modify, or review Kotlin source files in this plugin — also use it as a final
  self-check before considering a Kotlin change finished.
---

# SAP Commerce Developers Toolset — plugin development conventions

Project overview, module layout, and build/test commands are in the repo's root `CLAUDE.md` —
read that first for architecture. This skill is the single place for code-writing conventions in
this repository; `CLAUDE.md` intentionally stays high-level and points here instead of duplicating
these rules.

## Package & extension conventions

- All production code lives under the `sap.commerce.toolset` package namespace, sub-packaged per
  feature (e.g. `sap.commerce.toolset.impex...`), mirroring the module layout described in
  `CLAUDE.md`.
- Plugin extensions should bail out early on non-Hybris projects:
  `init { if (project.isNotHybrisProject) throw ExtensionNotApplicableException.create() }`.
- Prefer `project.directory` (or `PathMacroManager...expandPath("\$PROJECT_DIR$")`) over the
  deprecated, possibly-null `project.basePath`.
- Use the Kotlin extension property `import com.intellij.util.application` (referenced as the
  lowercase `application` value) instead of the verbose Java-style
  `ApplicationManager.getApplication()` call — e.g. `application.runReadAction { ... }`,
  `application.isUnitTestMode`. See existing usages such as
  `modules/beanSystem/ui/src/sap/commerce/toolset/beanSystem/ui/tree/BSTreeModel.kt`. The same
  applies to other `com.intellij.util.*` convenience properties where they exist (check for one
  before reaching for the `XyzManager.getInstance(...)` form).
- Regenerated lexer/parser/PSI sources for the custom languages (ImpEx, ACL, FlexibleSearch,
  Polyglot Query) live under each module's `gen/` dir — never hand-edit files there; regenerate via
  the JetBrains Grammar-Kit IDE plugin instead (see `CLAUDE.md` "Custom languages" for details).

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
not need to be extracted — it isn't a top-level type and isn't reused elsewhere.

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
2. One top-level class per file
3. Column style for constructor calls with many parameters
4. Never call `@ApiStatus.Internal` IntelliJ Platform SDK APIs — for anything unfamiliar or
   internal-looking that was actually touched, confirm the sources-jar/decompile check was done,
   not assumed.

Then run the mechanical backstop for the affected module(s):
```
./gradlew :<module>:compileKotlin
```
