# TypeSystem — Plugin Dev Reference

Source: `*-items.xml`. Module: `modules/typeSystem/`.

## XML Element → Meta Type

| XML element                                   | Meta type                   | How to get it                                                              |
|-----------------------------------------------|-----------------------------|----------------------------------------------------------------------------|
| `<itemtype code="X">`                         | `TSGlobalMetaItem`          | `findMetaItemByName("X")`                                                  |
| `<attribute qualifier="q">` (inside itemtype) | `TSGlobalMetaItemAttribute` | `item.allAttributes["q"]`                                                  |
| `<relation code="X">`                         | `TSGlobalMetaRelation`      | `findMetaRelationByName("X")`                                              |
| `<sourceElement>` / `<targetElement>`         | `TSMetaRelationElement`     | `item.allRelationEnds`                                                     |
| `<enumtype code="X">`                         | `TSGlobalMetaEnum`          | `findMetaEnumByName("X")`                                                  |
| `<collectiontype code="X">`                   | `TSGlobalMetaCollection`    | `findMetaCollectionByName("X")`                                            |
| `<maptype code="X">`                          | `TSGlobalMetaMap`           | `findMetaMapByName("X")`                                                   |
| `<atomictype class="X">`                      | `TSGlobalMetaAtomic`        | `findMetaAtomicByName("X")`                                                |
| any                                           | `TSGlobalMetaClassifier`    | `findMetaClassifierByName("X")` — item→collection→relation→enum→map→atomic |

Critical rule: `<attribute>` elements → `allAttributes`; `<relation>` elements →
`allRelationEnds`. Mutually exclusive — relation-declared attributes like `catalogVersion` are NEVER in `allAttributes`.

## Entry Point

`TSMetaModelAccess.getInstance(project)` — project service; call inside a read action.

**All `findMetaXxxByName()` lookups are case-insensitive** — backed by `CaseInsensitiveConcurrentHashMap`. `findMetaClassifierByName("product")` returns the `Product` meta item. Returns `null` only when the type genuinely doesn't exist (not a case mismatch). Use `.name` on the result to get the canonical casing.

Additional finders:

- `findAttributeByName(item, name, includeInherited)` — shortcut for `item.allAttributes[name]`
- `getRelationEnds(item, includeInherited)` — shortcut for `item.allRelationEnds`

## Base Fields (all types)

Every meta type implements `TSGlobalMetaClassifier`:
`.name: String?`, `.extensionName: String` (owning extension), `.moduleName: String`, `.isCustom: Boolean`
`.domAnchor` / `.retrieveDom()` — navigate back to the XML DOM source element

## Item Types

### TSGlobalMetaItem

Hierarchy: `.extendedMetaItemName: String?`, `.allExtends: Set<TSGlobalMetaItem>`, `.hierarchy: Set<TSGlobalMetaItem>`

Attributes (`<attribute>` XML only):

- `.allAttributes: Map<String, TSGlobalMetaItemAttribute>` — own + inherited
- `.attributes: Map<String, TSGlobalMetaItemAttribute>` — own only

Relation ends (`<relation>` XML only):

- `.allRelationEnds: List<TSMetaRelationElement>` — own + inherited
- `.relationEnds: List<TSMetaRelationElement>` — own only
- `.allOrderingAttributes: Map<String, TSMetaOrderingAttribute>`

Other: `.allIndexes / .indexes`, `.allCustomProperties / .customProperties`, `.deployment: TSMetaDeployment?`

Flags: `.isAbstract`, `.isAutoCreate`, `.isGenerate`, `.isSingleton`, `.isJaloOnly`, `.isCatalogAware`, `.isDeprecated`
Meta: `.description: String?`, `.jaloClass: String?`, `.deprecatedSince: String?`

### TSGlobalMetaItemAttribute

Core: `.name`, `.type: String?`, `.owner: TSGlobalMetaItem`
Flags: `.isLocalized`, `.isDynamic`, `.isDeprecated`, `.isAutoCreate`, `.isRedeclare`, `.isGenerate`
Details: `.modifiers: TSMetaModifiers`, `.persistence: TSMetaPersistence`, `.defaultValue: String?`,
`.isSelectionOf: String?`, `.description: String?`

### TSMetaPersistence

`.type: PersistenceType?` — `PROPERTY` (DB column) | `DYNAMIC` (computed, no DB) | `CMP` | `JALO`
`.qualifier: String?` — DB column name override (default: `p_<attributeQualifier>`)
`.attributeHandler: String?` — Spring bean id for `DYNAMIC` attributes

### TSGlobalMetaItemIndex

`.name`, `.keys: Set<String>`, `.includes: Set<String>`, `.isUnique`, `.isRemove`, `.isReplace`,
`.creationMode: CreationMode?`

## Relation Types

### TSGlobalMetaRelation

`.source: TSMetaRelationElement`, `.target: TSMetaRelationElement`
`.deployment: TSMetaDeployment?` — junction table for many-to-many
`.isLocalized`, `.isAutoCreate`, `.isGenerate`, `.description: String?`, `.orderingAttribute: TSMetaOrderingAttribute?`

### TSMetaRelationElement

Accessed via `item.allRelationEnds`. Represents one end of a `<relation>` as seen from the item on that end.

`.qualifier: String?` — attribute name on this end
`.type: String` — item type on the **other** end
`.cardinality: Cardinality` — `ONE` (FK in this item's table) | `MANY` (junction table or other side)
`.collectionType: Type`, `.end: RelationEnd` (`SOURCE`/`TARGET`)
`.modifiers: TSMetaModifiers`, `.isNavigable`, `.isOrdered`, `.isDeprecated`, `.description: String?`

FK rule: filter `.cardinality == Cardinality.ONE` for attributes with a physical FK column in this item's table.

## Enum Types

### TSGlobalMetaEnum

`.name`, `.values: Map<String, TSMetaEnumValue>` — each: `.name`, `.description: String?`
`.isDynamic`, `.isAutoCreate`, `.isGenerate`, `.isDeprecated`, `.description: String?`, `.deprecatedSince: String?`

## Scalar Types

| Type                     | Key fields                                                                     |
|--------------------------|--------------------------------------------------------------------------------|
| `TSGlobalMetaCollection` | `.elementType: String`, `.type: Type` (collection/list/set)                    |
| `TSGlobalMetaMap`        | `.argumentType: String?` (key), `.returnType: String?` (value), `.isRedeclare` |
| `TSGlobalMetaAtomic`     | `.extends: String` (Java class name)                                           |

All have `.isAutoCreate`, `.isGenerate`.

## Shared

### TSMetaModifiers

`.isOptional`, `.isUnique`, `.isInitial`, `.isPartOf`, `.isRead`, `.isWrite`, `.isSearch`, `.isEncrypted`

### TSMetaDeployment

`.table: String?`, `.typeCode: String?`, `.propertyTable: String`

### TSMetaCustomProperty

`.name: String`, `.rawValue: String?`

## Non-obvious Behaviors

DB-storage implications not inferrable from field names:

- `localized="true"` on `<attribute>` → platform stores a separate DB column per language; NOT in the item's main table row.
- Relation end `cardinality="one"` → FK column in that item's DB table row. Both ends `cardinality="many"` → junction table; relation needs its own `<deployment>`.
- `atomictype` uses `class` attribute as its name (not `code` like all other types).

## Caching

```kotlin
CachedValueProvider.Result.create(
    value,
    TSModificationTracker.getInstance(project),
    PsiModificationTracker.MODIFICATION_COUNT
)
```

## Inspections

Extend `TSInspection` (XML DOM-based). TypeSystem-specific item inspections go in
`modules/typeSystem/core/src/.../codeInspection/`.
