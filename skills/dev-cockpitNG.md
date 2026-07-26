# CockpitNG — Plugin Dev Reference

Backoffice/Cockpit XML configuration. Module: `modules/cockpitNG/`.

## Entry Point

`CngMetaModelStateService.state(project)` → `CngGlobalMetaModel`

No `findBy*` finders — access maps directly on the model:

```kotlin
val model = CngMetaModelStateService.state(project)
model.widgetDefinitions["myWidgetId"]   // CaseInsensitiveConcurrentHashMap — key lookup is case-insensitive
model.widgets["myWidgetId"]
model.actionDefinitions["myActionId"]
model.editorDefinitions["myEditorId"]
model.components                        // Set<String>
model.contextAttributes                 // Map<String, Set<String>>
```

## CngGlobalMetaModel Fields

- `widgetDefinitions: Map<String, CngMetaWidgetDefinition>` — from `*-widgets-definition.xml`
- `widgets: Map<String, CngMetaWidget>` — from `widgets.xml`
- `actionDefinitions: Map<String, CngMetaActionDefinition>` — from `*-actions.xml`
- `editorDefinitions: Map<String, CngMetaEditorDefinition>` — from `*-editors.xml`
- `components: Set<String>` — component strings from cng-config XML
- `contextAttributes: Map<String, Set<String>>` — context type → attribute qualifiers

## Meta Objects (all extend CngMeta<DOM>)

Common fields: `.fileName: String`, `.custom: Boolean`, `.retrieveDom(): DOM?` (nullable — DOM may be GC'd)

`CngMetaWidgetDefinition`: `.id`, `.name: String?`, `.settings: Map<String, CngMetaWidgetSetting>`
`CngMetaWidgetSetting`: `.id` (key), `.type: String?`, `.defaultValue: String?`
`CngMetaWidget`: `.id`, `.widgetDefinitionId: String?`, `.name: String?`, `.slotId: String?`, `.template: Boolean`, `.widgets: Collection<CngMetaWidget>` (children)
`CngMetaActionDefinition`: `.id`, `.name: String?`
`CngMetaEditorDefinition`: `.id`, `.name: String?`

## DOM File Types

| File pattern | DomFileDescription |
|---|---|
| `*-widgets-definition.xml` | `CngWidgetDefinitionDomFileDescription` |
| `widgets.xml` | `CngWidgetsDomFileDescription` |
| `*-config.xml` / `cng-config` | `CngConfigDomFileDescription` |
| `*-actions.xml` | `CngActionDefinitionDomFileDescription` |
| `*-editors.xml` | `CngEditorDefinitionDomFileDescription` |

## Cache Keys

`CngModificationTracker.getInstance(project)` — use for Cng-backed PSI caches.

## Inspections

`CngConfigDomInspection`, `CngWidgetsDomInspection`, `CngActionsDomInspection` — XML DOM-based; extend the appropriate base.
