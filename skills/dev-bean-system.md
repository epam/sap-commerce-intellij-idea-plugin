# BeanSystem — Plugin Dev Reference

Source: `*-beans.xml`. Module: `modules/beanSystem/`.

## XML Element → Meta Type

| XML element | `BSMetaType` | Meta type | How to get it |
|---|---|---|---|
| `<bean class="X">` (no wsRelated hint) | `META_BEAN` | `BSGlobalMetaBean` | `findMetaBeanByName("X")` |
| `<bean class="X">` (with wsRelated hint) | `META_WS_BEAN` | `BSGlobalMetaBean` | `findMetaBeanByName("X")` |
| `<bean class="X" type="event">` | `META_EVENT` | `BSGlobalMetaBean` | `findMetaBeanByName("X")` |
| `<enum class="X">` | `META_ENUM` | `BSGlobalMetaEnum` | `findMetaEnumByName("X")` |

All three bean kinds are `BSGlobalMetaBean`; `.metaType` distinguishes them at runtime.

## Entry Point

`BSMetaModelAccess.getInstance(project)` — project service; call inside a read action.

- `findMetaBeanByName(name?)` → first match across META_BEAN → META_WS_BEAN → META_EVENT
- `findMetaBeansByName(name?)` → all matches across all three bean types
- `findMetaEnumByName(name?)` → `BSGlobalMetaEnum?`
- `findMetasByName(name)` → enum + all bean types combined
- `getAllBeans()` / `getAllEnums()` — full collections

## Base Fields (all types)

Every meta type implements `BSGlobalMetaClassifier`:
`.name: String?`, `.extensionName: String`, `.moduleName: String`, `.isCustom: Boolean`
`.domAnchor` / `.retrieveDom()` — navigate back to the XML DOM source element

## BSGlobalMetaBean

Identity: `.name: String?` (FQN), `.shortName: String?`, `.genericName: String?` (e.g. `Foo<T>`)
Hierarchy: `.extends: String?`, `.fullExtends: String?`, `.extendsGenericName: String?`, `.allExtends: Set<BSGlobalMetaBean>`
Type: `.metaType: BSMetaType` (`META_BEAN` / `META_WS_BEAN` / `META_EVENT`)
Properties: `.properties: Map<String, BSMetaProperty>` (own only), `.allProperties: Map<String, BSMetaProperty>` (own + inherited)
Flags: `.isAbstract`, `.isDeprecated`, `.isSuperEquals`
Meta: `.description: String?`, `.template: String?`, `.deprecatedSince: String?`
Code gen: `.imports: List<BSMetaImport>`, `.annotations: List<BSMetaAnnotations>`, `.hints: Map<String, BSMetaHint>`

## BSMetaProperty

`.name`, `.type: String?` (raw declared type, e.g. `List<FooData>`), `.referencedType: String?` (resolved element/inner type, e.g. `FooData`)
`.isEquals`, `.isDeprecated`, `.description: String?`
`.annotations: List<BSMetaAnnotations>`, `.hints: Map<String, BSMetaHint>`

## BSGlobalMetaEnum

`.name` (FQN), `.shortName: String?`, `.values: Map<String, BSMetaEnumValue>` — each: `.name`
`.isDeprecated`, `.description: String?`, `.template: String?`, `.deprecatedSince: String?`

## Shared

`BSMetaAnnotations`: `.scope: Scope`, `.value: String?`
`BSMetaImport`: `.type: String?`, `.isStatic: Boolean`
`BSMetaHint`: `.name` (from base), `.value: String?`

## Non-obvious Behaviors

- `.name` = FQN from the `class` attribute; `.shortName` = simple class name portion.
- `findMetaBeanByName` returns the first match only. Use `findMetaBeansByName` when the same FQN can appear across META_BEAN / META_WS_BEAN / META_EVENT.
- `properties` = own only; `allProperties` = own + inherited (same pattern as TypeSystem `attributes` vs `allAttributes`).
- `.referencedType` on `BSMetaProperty` is the resolved inner type of a generic — for `type="List<FooData>"`, `referencedType = "FooData"`. Null for primitives and non-generic types.

## Caching

```kotlin
CachedValueProvider.Result.create(value,
    BSModificationTracker.getInstance(project),
    PsiModificationTracker.MODIFICATION_COUNT)
```

## Inspections

Extend `BSInspection` (XML DOM-based).
