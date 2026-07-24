# SAP Commerce PolyglotQuery

PolyglotQuery: query language for SAP Commerce polyglot-persisted types. FlexibleSearch alternative-storage counterpart. Use only if target type confirmed polyglot-configured.
Grammar confirmed (SAP Help, Commerce 2205, PDF 2026-07-24).

## Precondition
Confirm type is polyglot-configured before use. Not a general FlexibleSearch substitute.

## Grammar (EBNF, confirmed)
```
query        = GET type_key expression EOF
type_key     = '{' IDENTIFIER '}'
IDENTIFIER   = LETTER (LETTER|DIGIT|'_'|'.')*
expression   = (WHERE expr_or)? order_by? | empty
order_by     = ORDER_BY order_key (',' order_key)*
order_key    = attribute_key ORDER_DIRECTION?
ORDER_DIRECTION = ASC | DESC
attribute_key = '{' IDENTIFIER ('[' IDENTIFIER ']')? '}'   // bracket = locale
expr_or      = expr_and (OR expr_and)*
expr_and     = expr_atom (AND expr_atom)*
expr_atom    = attribute_key CMP_OP '?' IDENTIFIER
             | attribute_key NULL_OP
             | '(' expr_or ')'
CMP_OP       = '=' | '<>' | '>' | '<' | '>=' | '<='
NULL_OP      = IS [NOT] NULL
```
No JOIN. No UNION. No subselect. No literal inline values (param binding only, `?name`). No SELECT projection — GET always returns full item.
Casing appears case-insensitive per official examples (`or`/`OR` both used).

## Confirmed examples
```
GET {Title} ORDER BY {code}
GET {Title} WHERE {code}=?code ORDER BY {code}
GET {Title} WHERE {code}=?code1 or {code}=?code2
GET {Title} WHERE {code}=?code1 or {code}=?code2 ORDER BY {code}
GET {Title}
GET {Product} WHERE {name}?=name ORDER BY {name[en]} ASC, {name[de]} DESC
GET {Title} WHERE {code}=?code1 OR {code}=?code2 AND {code} IS NOT NULL ORDER BY {code[en]} DESC
```

## Rules
- CMP_OP only valid against `?param` — no inline literals.
- `{attr[lang]}` = localized attribute access, in WHERE and ORDER BY.
- Dotted chars in IDENTIFIER: grammar-legal, semantics (relation-path traversal?) UNCONFIRMED. Do not assume FlexibleSearch-style `a.b.c` dotted navigation works.
- No JOIN → cross-type filtering (e.g. via CatalogVersion/Catalog) not expressible. If needed: denormalize target attribute onto polyglot type, or two-step Java-side lookup.
- Full item returned per match; project in code post-fetch if partial fields needed.
