# BeanSystem — Plugin Dev Reference

Source: `*-beans.xml` files. Module: `modules/beanSystem/`.

## Entry Point

`BSMetaModelAccess.getInstance(project)` — project service; call inside a read action.

Key finders:
- `findMetaBeanByName(name?)` → `BSGlobalMetaBean?` — searches META_BEAN → META_WS_BEAN → META_EVENT, returns first match
- `findMetaBeansByName(name?)` → `List<BSGlobalMetaBean>` — all matches across all bean types
- `findMetaEnumByName(name?)` → `BSGlobalMetaEnum?`
- `findMetasByName(name)` → `List<BSGlobalMetaClassifier<*>>` — enum + all bean types
- `getAllBeans()` — META_BEAN + META_WS_BEAN + META_EVENT combined
- `getAllEnums()` — all META_ENUM

## BSGlobalMetaBean

`.name` (FQN), `.shortName`, `.extends: String?`, `.fullExtends: String?`, `.type: BSMetaType` (META_BEAN / META_WS_BEAN / META_EVENT), `.isAbstract`, `.isDeprecated`, `.properties: Map<String, BSMetaProperty>` (declared only), `.allProperties: Map<String, BSMetaProperty>` (including inherited)

## BSMetaProperty

`.name`, `.type: String?` (raw declared type), `.referencedType: String?` (resolved collection element or FK type), `.isEquals`, `.isDeprecated`

## BSGlobalMetaEnum

`.name` (FQN), `.shortName`, `.values: Map<String, BSMetaEnumValue>` (each has `.name`)

## Cache Keys

`BSModificationTracker.getInstance(project)` — use for bean-model-backed PSI caches.

```kotlin
CachedValueProvider.Result.create(value,
    BSModificationTracker.getInstance(project),
    PsiModificationTracker.MODIFICATION_COUNT)
```

## Inspections

Extend `BSInspection` (XML DOM-based).
