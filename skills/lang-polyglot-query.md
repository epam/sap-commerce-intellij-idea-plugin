# Polyglot Query PSI — Plugin Dev Reference

Single-type, no JOINs, no subqueries, no literal values (bind params only). Target type must be polyglot-configured.

Grammar: `modules/polyglotQuery/core/src/sap/commerce/toolset/polyglotQuery/polyglotQuery.bnf`
Lexer: `modules/polyglotQuery/core/src/sap/commerce/toolset/polyglotQuery/polyglotQuery.flex`

## PSI Hierarchy

```
PolyglotQueryStatement
└── PolyglotQueryQuery+
    ├── GET
    ├── PolyglotQueryTypeKey
    │   └── PolyglotQueryTypeKeyName          ← reference → TSGlobalMetaItem
    ├── PolyglotQueryWhereClause?
    │   └── WHERE PolyglotQueryExprOr
    │       └── PolyglotQueryExprAnd+ (OR-joined)
    │           └── PolyglotQueryExprAtom+ (AND-joined)
    │               ├── AttributeKey CmpOperator PolyglotQueryBindParameter
    │               ├── AttributeKey NullOperator  (IS [NOT] NULL)
    │               └── LPAREN ExprOr RPAREN
    └── PolyglotQueryOrderBy?
        └── PolyglotQueryOrderKey+ (COMMA-separated)
            └── PolyglotQueryAttributeKey (ASC|DESC)?
```

## Reference Nodes & Mixins

| Node | Mixin | Creates | Resolves to |
|------|-------|---------|-------------|
| `PolyglotQueryTypeKeyName` | `PolyglotQueryTypeKeyNameMixin` | `PolyglotQueryDefinedTableReference` | `TSGlobalMetaItem` (project-wide) |
| `PolyglotQueryAttributeKeyName` | `PolyglotQueryAttributeKeyNameMixin` | `PolyglotQueryAttributeKeyNameReference` | attribute on query's type context |

`PolyglotQueryBindParameter` methods: `getOperator()`, `getItemType()`, `getValue()` (strips `?`).
`getItemType()` → walks parent `PolyglotQueryExprAtom` → resolves `attributeKey` reference → attribute's TypeSystem type.

`PolyglotQueryLocalizedName` (`[lang]`) → creates `LanguageReference`.

## Non-Obvious Constraints

- No dotted attribute paths (`{relation.attribute}` not supported; grammar permits but semantics undefined — use FlexibleSearch)
- No literal values on RHS — only `?paramName` or `?name.prop` (property extension: parsed, not validated; runtime-specific)
- `[lang]` on non-localized attribute → inspection error (class greppable)
- `polyglotPersisted=true` flag not validated at parse/inspection time

Lexer: `{}` does NOT nest (unlike FlexibleSearch); keywords are case-insensitive (`%caseless`).

## PSI Caching

Cache keys (always both): `PsiModificationTracker.MODIFICATION_COUNT` + `TSModificationTracker.getInstance(project)`
