# FlexibleSearch PSI — Plugin Dev Reference

## PSI Hierarchy

```
FlexibleSearchFile
└── select_statement
    ├── select_core_select[]
    │   ├── SELECT (DISTINCT|ALL)?
    │   ├── result_columns → result_column[]
    │   │   └── expression (AS? column_alias_name)? | '*' | table.*
    │   ├── from_clause
    │   │   ├── from_clause_expr[]
    │   │   │   ├── y_from_clause → from_clause_simple          (SAP: { … })
    │   │   │   ├── from_clause_select → from_clause_select_query | from_clause_subqueries
    │   │   │   └── table_or_subquery
    │   │   │       ├── from_table (defined_table_name + alias?)
    │   │   │       └── select_subquery ({{ SELECT }} in FROM)
    │   │   └── join_operator[] → join_constraint (ON expr | USING cols)
    │   ├── where_clause?
    │   ├── group_by_clause? → having_clause?
    │   └── compound_operator? (UNION ALL?)
    ├── order_clause?
    └── limit_clause?

expression
├── column_ref_y_expression  { [alias.] col [lang] [:o] }   ← SAP-specific
├── column_ref_expression    alias.col | col
├── bind_parameter           ? | ?paramName[.order|.property]
└── … (standard SQL nodes: or/and/comparison/like/in/exists/cast/case/function_call/literal/paren)

defined_table_name  TYPE[!|*|^]?
```

## SAP-Specific Syntax

Type scope markers (FROM clause):
| Marker | Meaning |
|--------|---------|
| none   | Include subtypes; search restrictions apply |
| `!`    | Exact type only |
| `*`    | All types + supertypes; restrictions disabled |
| `^`    | Deployment table |

Y-column `{ [alias.] col [lang] [:o] }`:
- `[EN]`/`[DE]` → localized attribute; resolved via `FlexibleSearchColumnLocalizedNameMixin`
- `:o` → outer join marker; produces `OUTER_JOIN` token

SAP subquery forms (in addition to standard SQL `(SELECT…) alias`):
- `( {{ SELECT … }} ) alias` — double-brace subquery
- `{{ SELECT … }}` — used in UNION context

Bind parameter `?paramName[.order|.property]`: PSI methods `getExpression()`, `getValue()`, `getItemType()` on `FlexibleSearchBindParameterMixin`.

## PSI Mixins & Cache

| Mixin | Responsibility |
|-------|---------------|
| `FlexibleSearchTableNameMixin` | TypeSystem lookup; validates marker (!/*/^) |
| `FlexibleSearchColumnNameMixin` | TypeSystem attribute lookup in table context |
| `FlexibleSearchColumnLocalizedNameMixin` | lang code → attribute |
| `FlexibleSearchBindParameterMixin` | `getItemType()` via expression type resolution |

Cache keys (always both): `PsiModificationTracker.MODIFICATION_COUNT` + `TSModificationTracker.getInstance(project)`
