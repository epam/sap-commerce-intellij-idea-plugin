# CockpitNG — Plugin Dev Reference

Backoffice/Cockpit XML configuration. Module: `modules/cockpitNG/`.

## XML File → Model Field

| File pattern | `CngGlobalMetaModel` field | Meta type |
|---|---|---|
| `*-widgets-definition.xml` | `.widgetDefinitions` | `CngMetaWidgetDefinition` |
| `widgets.xml` | `.widgets` (flat map, all depths) | `CngMetaWidget` |
| `*-actions.xml` | `.actionDefinitions` | `CngMetaActionDefinition` |
| `*-editors.xml` | `.editorDefinitions` | `CngMetaEditorDefinition` |
| `*-config.xml` | `.components`, `.contextAttributes` | `CngMetaConfig` / `CngMetaContext` |

## Entry Point

`CngMetaModelStateService.state(project)` → `CngGlobalMetaModel` (snapshot; no read action needed)

## Base Fields (all types)

Every meta type extends `CngMeta<DOM>`:
`.fileName: String`, `.custom: Boolean`, `.domAnchor`, `.retrieveDom(): DOM?` (nullable — DOM may be GC'd)

## CngGlobalMetaModel

```
.widgetDefinitions: Map<String, CngMetaWidgetDefinition>   // CaseInsensitiveConcurrentHashMap
.widgets:           Map<String, CngMetaWidget>             // CaseInsensitiveConcurrentHashMap; ALL widgets flat
.actionDefinitions: Map<String, CngMetaActionDefinition>   // CaseInsensitiveConcurrentHashMap
.editorDefinitions: Map<String, CngMetaEditorDefinition>   // CaseInsensitiveConcurrentHashMap
.components:        Set<String>                            // component strings from *-config.xml
.contextAttributes: Map<String, MutableSet<String>>        // context type → attribute qualifiers
```

## Meta Types

### CngMetaWidgetDefinition
`.id`, `.name: String?`, `.description: String?`, `.settings: Map<String, CngMetaWidgetSetting>`

### CngMetaWidgetSetting
`.id` (key), `.type: String?`, `.defaultValue: String?`

### CngMetaWidget
`.id`, `.name: String?`, `.widgetDefinitionId: String?`, `.slotId: String?`
`.access: String?`, `.template: Boolean`
`.widgets: Collection<CngMetaWidget>` — direct children (tree structure; use model `.widgets` map for flat lookup)

### CngMetaWidgetExtension
`.id` (= `widgetId` being extended), `.widgets: Collection<CngMetaWidget>` — injected children

### CngMetaActionDefinition
`.id`, `.name: String?`, `.description: String?`

### CngMetaEditorDefinition
`.id`, `.name: String?`, `.description: String?`

### CngMetaConfig *(intermediate parse container; not in CngGlobalMetaModel)*
`.contexts: List<CngMetaContext>`

### CngMetaContext
`.name: String`, `.attributes: Map<String, String>` — all raw XML attributes of the `<context>` element

## Non-obvious Behaviors

- All `CngGlobalMetaModel` maps use `CaseInsensitiveConcurrentHashMap` — key lookups are case-insensitive.
- `CngGlobalMetaModel.widgets` is a **flat** map of all widgets at all nesting levels. `CngMetaWidget.widgets` is the direct-children tree.
- `CngMetaConfig` / `CngMetaContext` are intermediate parse containers. Aggregated context data ends up in `CngGlobalMetaModel.contextAttributes` (context type → qualifier set).
- `retrieveDom()` may return null even on a valid meta object — the DOM anchor holds a weak reference.

## Caching

```kotlin
CachedValueProvider.Result.create(value,
    CngModificationTracker.getInstance(project),
    PsiModificationTracker.MODIFICATION_COUNT)
```

## Inspections

`CngConfigDomInspection`, `CngWidgetsDomInspection`, `CngActionsDomInspection` — XML DOM-based; extend the appropriate base.
