<!-- TOC -->
  * [Technical Notes & How-To(s)](#technical-notes--how-tos)
    * [Get project's base directory](#get-projects-base-directory)
    * [Invoke AnAction](#invoke-anaction)
    * [Refresh state of an AnAction in case of background thread](#refresh-state-of-an-anaction-in-case-of-background-thread)
<!-- TOC -->

---

## Technical Notes & How-To(s)

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