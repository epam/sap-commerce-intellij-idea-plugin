# TypeSystem — Plugin Dev Reference

Source: `*-items.xml` files. Module: `modules/typeSystem/`.

## Entry Point

`TSMetaModelAccess.getInstance(project)` — project service; call inside a read action.

Key finders (all nullable):
- `findMetaItemByName(name?)` → `TSGlobalMetaItem?`
- `findMetaEnumByName(name?)` → `TSGlobalMetaEnum?`
- `findMetaClassifierByName(name?)` → item → collection → relation → enum → map → atomic (first non-null)
- `findAttributeByName(meta, name, includeInherited)` — shortcut for `meta.allAttributes[name]`
- `getRelationEnds(meta, includeInherited)` — shortcut for `meta.allRelationEnds`

## TSGlobalMetaItem

- `allAttributes: Map<String, TSGlobalMetaItemAttribute>` — only `<attribute>` XML elements
- `allRelationEnds: List<TSMetaRelation.TSMetaRelationElement>` — from `<relation>` XML

Critical: relation-declared FK attributes (e.g. `catalogVersion`) are in `allRelationEnds`, NOT `allAttributes`. Filter `.cardinality == Cardinality.ONE` for single-value FK columns stored in this item's DB table.

Other: `.name`, `.extendedMetaItemName`, `.isAbstract`, `.isDeprecated`, `.allExtends: Set<TSGlobalMetaItem>`, `.hierarchy: Set<TSGlobalMetaItem>`, `.deployment: TSMetaDeployment?`

## TSGlobalMetaItemAttribute

`.name`, `.type: String?`, `.isLocalized`, `.isDynamic`, `.isDeprecated`, `.modifiers: TSMetaModifiers`, `.defaultValue: String?`

## TSMetaRelation.TSMetaRelationElement

`.qualifier: String?` (attribute name on this end), `.type: String` (other-end item type), `.cardinality: Cardinality`, `.modifiers: TSMetaModifiers`, `.isNavigable`, `.isDeprecated`

## TSMetaModifiers

`.isOptional`, `.isUnique`, `.isInitial`, `.isPartOf`, `.isRead`, `.isWrite`, `.isSearch`, `.isEncrypted`

## TSGlobalMetaEnum

`.name`, `.values: Map<String, TSGlobalMetaEnumValue>` (each has `.name`), `.isDynamic`

## Cache Keys

`TSModificationTracker.getInstance(project)` — use for meta-model-backed PSI caches.

```kotlin
CachedValueProvider.Result.create(value,
    TSModificationTracker.getInstance(project),
    PsiModificationTracker.MODIFICATION_COUNT)
```

## items.xml → Meta Model Mapping

Understanding which XML element maps to which meta object is essential for working with the TypeSystem API.

```xml
<!-- itemtype → TSGlobalMetaItem (name = code attribute) -->
<itemtype code="Product" extends="GenericItem" abstract="false">

    <!-- attribute → allAttributes["code"] -->
    <attribute qualifier="code" type="java.lang.String">
        <!-- modifiers → .modifiers.isOptional / .isUnique / etc. -->
        <modifiers optional="false" unique="true" read="true" write="true"
                   search="true" initial="false" partof="false" encrypted="false"/>
        <!-- persistence type="property" → isDynamic=false; type="dynamic" → isDynamic=true -->
        <persistence type="property"/>
    </attribute>

    <!-- deployment → .deployment (table + typecode) -->
    <deployment table="Products" typecode="1"/>

</itemtype>

<!-- relation → allRelationEnds entries on BOTH source and target types -->
<relation code="CategoryProductRelation">
    <!-- sourceElement → allRelationEnds entry on Category (.qualifier="products", cardinality=MANY) -->
    <sourceElement type="Category" qualifier="supercategories" cardinality="many">
        <modifiers read="true" write="true" search="true" optional="true"/>
    </sourceElement>
    <!-- targetElement → allRelationEnds entry on Product (.qualifier="supercategories", cardinality=MANY) -->
    <targetElement type="Product" qualifier="products" cardinality="many"/>
</relation>

<!-- enumtype → TSGlobalMetaEnum (name = code attribute) -->
<enumtype code="ArticleApprovalStatus" dynamic="false">
    <!-- value → .values["APPROVED"] (TSGlobalMetaEnumValue.name = "APPROVED") -->
    <value code="APPROVED"/>
    <value code="CHECK"/>
    <value code="UNAPPROVED"/>
</enumtype>
```

Key rules:
- `<attribute>` → `allAttributes`. `<relation>` → `allRelationEnds`. Never the other way.
- A relation end with `cardinality="one"` → FK column stored in that item's DB table.
- A relation end with `cardinality="many"` → junction table or pointer on the other side; no FK on this item.
- `localized="true"` on `<attribute>` → `.isLocalized = true` on `TSGlobalMetaItemAttribute`.
- `type="dynamic"` on `<persistence>` → `.isDynamic = true`; no DB column.
- Inherited attributes/relation-ends: pass `includeInherited = true` to the finders or use `allAttributes`/`allRelationEnds` directly (they include inherited by default).

## Inspections

Extend `TSInspection` (XML DOM-based). TypeSystem-specific item inspections go in `modules/typeSystem/core/src/.../codeInspection/`.
