# TypeSystem — Plugin Dev Reference

Source: `*-items.xml` files. Module: `modules/typeSystem/`.

## Entry Point

`TSMetaModelAccess.getInstance(project)` — project service; call inside a read action.

Key finders (all nullable):
- `findMetaItemByName(name?)` → `TSGlobalMetaItem?`
- `findMetaEnumByName(name?)` → `TSGlobalMetaEnum?`
- `findMetaCollectionByName(name?)` → `TSGlobalMetaCollection?`
- `findMetaMapByName(name?)` → `TSGlobalMetaMap?`
- `findMetaAtomicByName(name?)` → `TSGlobalMetaAtomic?`
- `findMetaRelationByName(name?)` → `TSGlobalMetaRelation?`
- `findMetaClassifierByName(name?)` → item → collection → relation → enum → map → atomic (first non-null)
- `findAttributeByName(meta, name, includeInherited)` — shortcut for `meta.allAttributes[name]`
- `getRelationEnds(meta, includeInherited)` — shortcut for `meta.allRelationEnds`

## TSGlobalMetaClassifier (base for all types)

`.name: String?`, `.extensionName: String` (owning extension), `.moduleName: String`, `.isCustom: Boolean`, `.domAnchor` / `.retrieveDom()` (navigate to XML source)

## TSGlobalMetaItem

- `allAttributes: Map<String, TSGlobalMetaItemAttribute>` — only `<attribute>` XML elements; includes inherited
- `allRelationEnds: List<TSMetaRelation.TSMetaRelationElement>` — from `<relation>` XML; includes inherited
- `relationEnds: List<TSMetaRelation.TSMetaRelationElement>` — own only (non-inherited)
- `allIndexes: List<TSGlobalMetaItemIndex>`, `indexes: Map<String, TSGlobalMetaItemIndex>` — own
- `allCustomProperties: List<TSMetaCustomProperty>`, `customProperties: Map<String, TSMetaCustomProperty>`
- `allOrderingAttributes: Map<String, TSMetaRelation.TSMetaOrderingAttribute>`
- `deployment: TSMetaDeployment?`
- `.extendedMetaItemName: String?`, `.allExtends: Set<TSGlobalMetaItem>`, `.hierarchy: Set<TSGlobalMetaItem>`
- `.isAbstract`, `.isAutoCreate`, `.isGenerate`, `.isSingleton`, `.isJaloOnly`, `.isCatalogAware`, `.isDeprecated`
- `.description: String?`, `.jaloClass: String?`, `.deprecatedSince: String?`

Critical: relation-declared FK attributes (e.g. `catalogVersion`) are in `allRelationEnds`, NOT `allAttributes`. Filter `.cardinality == Cardinality.ONE` for single-value FK columns stored in this item's DB table.

## TSGlobalMetaItemAttribute

`.name`, `.type: String?`, `.isLocalized`, `.isDynamic`, `.isDeprecated`, `.isAutoCreate`, `.isRedeclare`, `.isGenerate`
`.modifiers: TSMetaModifiers`, `.persistence: TSMetaPersistence`, `.defaultValue: String?`, `.isSelectionOf: String?`
`.description: String?`, `.owner: TSGlobalMetaItem`

## TSMetaPersistence

`.type: PersistenceType?` — `PROPERTY` (column), `DYNAMIC` (no DB), `CMP`, `JALO`
`.qualifier: String?` — DB column name override (if different from attribute name)
`.attributeHandler: String?` — Spring bean id for dynamic attributes

## TSMetaRelation.TSMetaRelationElement

`.qualifier: String?` (attribute name on this end), `.type: String` (other-end item type), `.cardinality: Cardinality`
`.collectionType: Type` (collection kind for MANY end), `.end: RelationEnd` (`SOURCE`/`TARGET`)
`.modifiers: TSMetaModifiers`, `.isNavigable`, `.isOrdered`, `.isDeprecated`, `.description: String?`
`.metaType: String?`, `.customProperties`, `.customGetters`, `.customSetters`

## TSGlobalMetaRelation

`.source: TSMetaRelationElement`, `.target: TSMetaRelationElement`, `.deployment: TSMetaDeployment?`
`.isLocalized`, `.isAutoCreate`, `.isGenerate`, `.description: String?`
`.orderingAttribute: TSMetaOrderingAttribute?`

## TSMetaModifiers

`.isOptional`, `.isUnique`, `.isInitial`, `.isPartOf`, `.isRead`, `.isWrite`, `.isSearch`, `.isEncrypted`

## TSGlobalMetaEnum

`.name`, `.values: Map<String, TSMetaEnumValue>` — each has `.name`, `.description: String?`
`.isDynamic`, `.isAutoCreate`, `.isGenerate`, `.isDeprecated`, `.description: String?`, `.deprecatedSince: String?`

## TSGlobalMetaCollection

`.elementType: String`, `.type: Type` (collection kind: `collection`/`list`/`set`), `.isAutoCreate`, `.isGenerate`

## TSGlobalMetaMap

`.argumentType: String?` (key type), `.returnType: String?` (value type), `.isAutoCreate`, `.isGenerate`, `.isRedeclare`

## TSGlobalMetaAtomic

`.name`, `.extends: String` (Java class), `.isAutoCreate`, `.isGenerate`

## TSMetaDeployment

`.table: String?`, `.typeCode: String?`, `.propertyTable: String`

## TSMetaCustomProperty

`.name: String`, `.rawValue: String?`

## TSGlobalMetaItemIndex

`.name`, `.keys: Set<String>`, `.includes: Set<String>`, `.isUnique`, `.isRemove`, `.isReplace`, `.creationMode: CreationMode?`

## Cache Keys

`TSModificationTracker.getInstance(project)` — use for meta-model-backed PSI caches.

```kotlin
CachedValueProvider.Result.create(value,
    TSModificationTracker.getInstance(project),
    PsiModificationTracker.MODIFICATION_COUNT)
```

## items.xml → Meta Model Mapping

```xml
<!-- itemtype → TSGlobalMetaItem (name = code) -->
<itemtype code="Product" extends="GenericItem"
          abstract="false" singleton="false" jaloonly="false"
          autocreate="true" generate="true">

    <!-- attribute → allAttributes["code"] -->
    <attribute qualifier="code" type="java.lang.String"
               localized="false" autocreate="true" redeclare="false">
        <!-- modifiers → .modifiers.isOptional / .isUnique / .isInitial / etc. -->
        <modifiers optional="false" unique="true" read="true" write="true"
                   search="true" initial="false" partof="false" encrypted="false"/>
        <!-- persistence → .persistence (.type / .qualifier / .attributeHandler) -->
        <!-- type="property" → isDynamic=false; type="dynamic" → isDynamic=true -->
        <!-- qualifier="myCol" → DB column name override -->
        <persistence type="property" qualifier="p_code"/>
        <!-- defaultvalue → .defaultValue -->
        <defaultvalue>""</defaultvalue>
    </attribute>

    <!-- indexes → .indexes / .allIndexes -->
    <indexes>
        <index name="ProductCodeIdx" unique="true" remove="false" replace="false">
            <key attribute="code"/>
            <include attribute="catalogVersion"/>
        </index>
    </indexes>

    <!-- customproperties → .customProperties / .allCustomProperties -->
    <customproperties>
        <customproperty name="catalog.sync.default.root.type">
            <value>true</value>
        </customproperty>
    </customproperties>

    <!-- deployment → .deployment (.table / .typeCode / .propertyTable) -->
    <deployment table="Products" typecode="1"/>

</itemtype>

<!-- relation → TSGlobalMetaRelation; also adds allRelationEnds on BOTH item types -->
<relation code="CategoryProductRelation" localized="false"
          autocreate="true" generate="true">
    <!-- sourceElement → allRelationEnds entry on Category -->
    <!--   .qualifier="supercategories", .type="Product", cardinality=MANY  -->
    <sourceElement type="Category" qualifier="supercategories" cardinality="many"
                   navigable="true" ordered="false">
        <modifiers read="true" write="true" search="true" optional="true"/>
    </sourceElement>
    <!-- targetElement → allRelationEnds entry on Product -->
    <!--   .qualifier="products", .type="Category", cardinality=MANY  -->
    <targetElement type="Product" qualifier="products" cardinality="many"
                   navigable="true" ordered="false">
        <modifiers read="true" write="false"/>
    </targetElement>
    <!-- deployment → TSGlobalMetaRelation.deployment (junction table for many-to-many) -->
    <deployment table="cat2prodrel" typecode="322"/>
</relation>

<!-- enumtype → TSGlobalMetaEnum (name = code) -->
<enumtype code="ArticleApprovalStatus" dynamic="false"
          autocreate="true" generate="true">
    <!-- value → .values["APPROVED"] (.name / .description) -->
    <value code="APPROVED"/>
    <value code="CHECK"/>
    <value code="UNAPPROVED"/>
</enumtype>

<!-- collectiontype → TSGlobalMetaCollection (name = code) -->
<collectiontype code="StringList" elementtype="java.lang.String" type="list"
                autocreate="true" generate="true"/>

<!-- maptype → TSGlobalMetaMap (name = code) -->
<maptype code="localized:java.lang.String"
         argumenttype="java.util.Locale" returntype="java.lang.String"
         autocreate="true" generate="false"/>

<!-- atomictype → TSGlobalMetaAtomic (name = class) -->
<atomictype class="java.lang.String" extends="java.lang.Object"
            autocreate="true" generate="false"/>
```

Key rules:
- `<attribute>` → `allAttributes`. `<relation>` → `allRelationEnds`. Never the other way.
- A relation end with `cardinality="one"` → FK column in that item's DB table; no `<deployment>` on the relation needed.
- A relation end with `cardinality="many"` on both sides → junction table (relation has `<deployment>`).
- `localized="true"` on `<attribute>` → `.isLocalized = true`, separate DB table column per language.
- `type="dynamic"` on `<persistence>` → `.isDynamic = true`; no DB column; `attributeHandler` is the Spring bean.
- `qualifier` on `<persistence>` overrides the DB column name (otherwise it is `p_<qualifier>`).
- Inherited attributes/relation-ends: `allAttributes`/`allRelationEnds` include inherited; `attributes`/`relationEnds` are own-only.
- `extensionName` on any classifier tells which `*-items.xml` file declared it.

## Inspections

Extend `TSInspection` (XML DOM-based). TypeSystem-specific item inspections go in `modules/typeSystem/core/src/.../codeInspection/`.
