<!-- TOC -->
  * [Technical Notes & How-To(s)](#technical-notes--how-tos)
    * [Enable specific feature only during development](#enable-specific-feature-only-during-development)
    * [Get project's base directory](#get-projects-base-directory)
    * [Invoke AnAction](#invoke-anaction)
    * [Refresh state of an AnAction in case of background thread](#refresh-state-of-an-anaction-in-case-of-background-thread)
    * [Forms](#forms)
      * [Register form validators for DSL DialogPanel created outside the DialogWrapper](#register-form-validators-for-dsl-dialogpanel-created-outside-the-dialogwrapper)
      * [Example of the scrollable DialogWrapper with validations](#example-of-the-scrollable-dialogwrapper-with-validations)
    * [Resize Kotlin Dsl Dialog on content change](#resize-kotlin-dsl-dialog-on-content-change)
    * [Notifications](#notifications)
<!-- TOC -->

---

## Technical Notes & How-To(s)

### Enable specific feature only during development

Rely on `sap.commerce.toolset.isSandbox` flag set via `build.gradle.kts # applyRunIdeSystemSettings`, which is available only on "SandBox" environments.

### Get project's base directory

> OOTB property `project.basePath` is deprecated and may return
`null`, to overcome this problem it is possible to rely on a project path - `$PROJECT_DIR$`.

```kotlin
project.directory
---or-- -
PathMacroManager.getInstance(project).expandPath("\$PROJECT_DIR$")
```

### Invoke AnAction

```kotlin
triggerAction("action_id", event)
---or-- -
project.triggerAction("action_id") { customDataContext }
```

### Refresh state of an AnAction in case of background thread

> Example: [FlexibleSearchExecuteAction](modules/flexibleSearch/ui/src/sap/commerce/toolset/flexibleSearch/actionSystem/FlexibleSearchExecuteAction.kt)

```kotlin
coroutineScope.launch {
    readAction { ActivityTracker.getInstance().inc() }
}
```

### Forms

#### Register form validators for DSL DialogPanel created outside the DialogWrapper

```kotlin
val _ui = panel {}
_ui.registerValidators(parentDisposable)
```

#### Example of the scrollable DialogWrapper with validations
```kotlin
ProjectRefreshDialog
```

### Resize Kotlin Dsl Dialog on content change

```kotlin
invokeLater {
    peer.window?.pack()
}
```

### Notifications

* Register id for each `Got It Tooltip` in the [GotItTooltips](modules/shared/core/src/sap/commerce/toolset/GotItTooltips.kt).
* Use `Reset Got It Tooltips` internal action to simplify testing of the `GotItToolip`.
