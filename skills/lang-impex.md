# ImpEx PSI — Plugin Dev Reference

## PSI Hierarchy

```
ImpExFile
├── macro_declaration → macro_name_dec ($NAME) + macro_values_dec
├── ImpExHeaderLine
│   ├── any_header_mode (INSERT|UPDATE|REMOVE|INSERT_UPDATE)
│   ├── full_header_type → header_type_name + modifiers?
│   └── full_header_parameter[]
│       ├── any_header_parameter_name → document_id_dec | HEADER_PARAMETER_NAME
│       ├── parameters? → parameter[]
│       │   ├── HEADER_PARAMETER_NAME | FUNCTION | document_id_usage | sub_parameters? | modifiers*
│       └── modifiers* → attribute[] (any_attribute_name + any_attribute_value)
├── ImpExValueLine[]
│   ├── sub_type_name?
│   └── value_group[]                        ← index N maps to header column N
│       ├── FIELD_VALUE_SEPARATOR (;)
│       └── value? → string | value_dec+ | FIELD_VALUE_IGNORE | FIELD_VALUE_NULL
├── ImpExUserRights ($userRights … block)
├── ImpExScriptLine (groovy:|javascript:|beanshell: + script_action?)
└── comment (LINE_COMMENT)

Macro tokens: MACRO_USAGE (lexer-matched) | POSSIBLE_MACRO_USAGE (partial)
```

## Column Alignment — Critical

`ImpExValueLine.getValueGroups()` is ordered; index N → `full_header_parameter[N]`.
Value line MUST have exactly N `value_group` elements. Empty cell = two adjacent `;`.

## Macro Semantics — Non-Obvious Rules

- Declaration must precede use — enforced via `getLineNumber()`; lexer populates `macroDeclarations` HashSet during parse (not lex)
- Longest-prefix matching via `escapeName()`
- `$config-` prefix is reserved (maps to platform config; never a user macro)
- `$MACRO` in `default='$MACRO'` modifier → expanded at import time
- `$MACRO` in abbreviation template → expanded at reference resolution time (not lex)
- `file.getExternalImpExFiles()` → transitive imports; no circular detection

## TypeSystem Column Resolution

- `full_header_type.header_type_name.getHeaderTypeNameAsString()` → TypeSystem lookup
- `any_header_parameter_name` text → TypeSystem attribute; resolved iteratively per nesting level

Modifier constraints:
| Modifier | Constraint |
|----------|-----------|
| `lang=X` | `attribute.isLocalized` must be true |
| `unique=true` | error if `cardinality=MANY` |
| `mode=append\|replace` | `cardinality=MANY` only |
| `class=X` | TypeSystem classifier |

Relation `cardinality=MANY` + `document_id_usage` → error.

## Document IDs

- `document_id_dec` in header parameter → named reference
- `document_id_usage` (`$docId(ID)`) in parameter value → resolves by walking file backward; finds `ImpExDocumentIdDeclaration` matching `getName()`
- Type: from containing header's TypeSystem type
- `cardinality=ONE` → valid; `MANY` → error; cross-file reference → not supported

## PSI Caching

Cache keys: `PsiModificationTracker.MODIFICATION_COUNT` + `TSModificationTracker.getInstance(project)` + `PropertyService` (abbreviations)

```kotlin
CachedValueProvider.Result.create(value,
    PsiModificationTracker.MODIFICATION_COUNT,
    TSModificationTracker.getInstance(project))
```

## Inspections

Module: `modules/impex/core/src/.../codeInspection/`; fixes in `fix/` sub-package.
Registration: `modules/impex/core/resources/META-INF/sap.commerce.toolset-impex-core.xml`.
i18n: `modules/shared/core/resources/i18n/HybrisBundle.properties`.
HTML descriptions: `modules/impex/core/resources/inspectionDescriptions/<ClassName>.html`.

**Pattern:**
```kotlin
class ImpExXxxInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : ImpExVisitor() {
        override fun visitHeaderTypeName(element: ImpExHeaderTypeName) { ... }
        override fun visitAnyHeaderMode(element: ImpExAnyHeaderMode) { ... }
        // other visitXxx overrides
    }
}
```

**XML registration:**
```xml
<localInspection groupPath="SAP Commerce" shortName="ImpExXxxInspection" displayName="[y] ..."
                 groupName="[y] ImpEx" level="WEAK WARNING" language="ImpEx" enabledByDefault="true"
                 implementationClass="sap.commerce.toolset.impex.codeInspection.ImpExXxxInspection"/>
```

**Quick fix pattern** — extend `LocalQuickFixOnPsiElement`:
```kotlin
class ImpExXxxQuickFix(element: ImpExXxx, private val replacement: String) : LocalQuickFixOnPsiElement(element) {
    override fun getFamilyName() = i18n("hybris.inspections.fix.impex.Xxx")
    override fun getText() = i18n("hybris.inspections.fix.impex.Xxx.text", replacement)
    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val newElement = ImpExElementFactory.createXxx(project, replacement) ?: return
        startElement.replace(newElement)
    }
}
```

**TypeSystem reference in inspections** — reuse reference infrastructure instead of calling TSMetaModelAccess directly:
```kotlin
val ref = element.references.firstOrNull() as? TSReferenceBase<*> ?: return
if (ref.multiResolve(false).isEmpty()) // → unknown type (ImpExUnknownTypeNameInspection pattern)
```
Or call TSMetaModelAccess directly for case/name checks:
```kotlin
val meta = TSMetaModelAccess.getInstance(element.project).findMetaClassifierByName(element.text) ?: return
// meta.name = canonical name; findMetaClassifierByName is case-insensitive
```

**`ImpExHeaderTypeName` specifics:**
- `.macroUsageDecList` / `.possibleMacroUsageDecList` — non-empty means macro in type name; skip inspection
- Backed by `ImpExHeaderTypeNameMixin` → `ImpExTSItemReference` (cache key `ImpExTSItemReference.CACHE_KEY`)

**`ImpExAnyHeaderParameterName` specifics** (gen'd interface):
- `.documentIdDec: ImpExDocumentIdDec?` — skip inspection if non-null (or if `firstChild is ImpExDocumentIdUsage`)
- `.macroUsageDecList` / `.possibleMacroUsageDecList` — skip if non-empty (or `firstChild is ImpExMacroUsageDec`)
- `.specialParameter: ImpExSpecialParameter?` — special tokens; no TS reference
- `.headerItemTypeName: ImpExHeaderTypeName?` — returns the type name of the containing header line
- `.isHeaderAbbreviation(): Boolean` — abbreviation uses `ImpExHeaderAbbreviationReference`, not `ImpExTSAttributeReference`
- Backed by `ImpExAnyHeaderParameterNameMixin` → reference is `ImpExTSAttributeReference` when leaf is `HEADER_PARAMETER_NAME` and not abbreviation

**Attribute case inspection pattern** (find `ImpExTSAttributeReference`, get canonical name from resolve result):
```kotlin
override fun visitAnyHeaderParameterName(element: ImpExAnyHeaderParameterName) {
    if (element.firstChild is ImpExMacroUsageDec || element.firstChild is ImpExDocumentIdUsage) return
    val ref = element.references.find { it is ImpExTSAttributeReference }.asSafely<ImpExTSAttributeReference>() ?: return
    val resolveResults = ref.multiResolve(false)
    if (resolveResults.isEmpty()) return  // unknown — other inspection handles it
    val canonicalName = resolveResults.first().asSafely<TSResolveResult<*>>()?.meta?.name ?: return
    if (canonicalName == ref.value) return
    // register problem ...
}
```

**`TSResolveResult` for attribute resolution** (`TSResolveResultUtil.tryResolveAttribute`):
- `AttributeResolveResult.meta: TSGlobalMetaItemAttribute` → `.name: String` (canonical qualifier)
- `RelationEndResolveResult.meta: TSMetaRelationElement` → `.name = qualifier` (overridden in impl)
- `OrderingAttributeResolveResult.meta: TSMetaOrderingAttribute` → `.name: String`
- All lookups case-insensitive (`allAttributes`, `allRelationEnds` use `CaseInsensitiveConcurrentHashMap` / `.equals(ignoreCase=true)`)
- `(resolveResult as? TSResolveResult<*>)?.meta?.name` works uniformly across all three types

## ImpExElementFactory

`object ImpExElementFactory` in `modules/impex/core/src/.../psi/ImpExElementFactory.kt`. All methods create PSI from in-memory ImpEx text.

| Method | Returns |
|--------|---------|
| `createHeaderMode(project, HeaderMode)` | first child of `ImpExAnyHeaderMode` token |
| `createHeaderTypeName(project, typeName)` | `ImpExHeaderTypeName` |
| `createAnyHeaderParameterName(project, attrName)` | `ImpExAnyHeaderParameterName` |
| `createParametersSeparator(project)` | last child of header line |
| `createMacroName(project, value)` | `ImpExMacroNameDec` |
| `createSingleQuotedString(project, value)` | `ImpExString` |
| `createValueGroup(project, value?)` | first `ImpExValueGroup` |
| `createFullHeaderParameter(project, headerTypeName, macros, parameterPlaceholder)` | `ImpExFullHeaderParameter` |
| `createDocumentIdDecElement(project, text)` | `ImpExDocumentIdDec` |
| `createValueElement(project, text)` | `ImpExValue` |
| `createDocumentIdUsageElement(project, text)` | `ImpExDocumentIdUsage` |
| `createFile(project, text)` | `ImpExFile` |
