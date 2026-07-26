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
