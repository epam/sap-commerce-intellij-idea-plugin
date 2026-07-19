/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.flexibleSearch.impex

import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.model.Cardinality

/**
 * Resolves the natural key path for a [TSGlobalMetaItem] for use as ImpEx nested parameters.
 *
 * For example, given `CatalogVersion`, the resolved path is `catalog(id),version`
 * which produces the ImpEx header `catalogVersion(catalog(id),version)`.
 *
 * ### Resolution strategy
 * 1. Find the type's unique index (first index where `isUnique=true` and has non-pk keys).
 * 2. For each key attribute in that index:
 *    - If the attribute type is itself a `ComposedType`, recurse (up to [MAX_DEPTH]).
 *    - Otherwise, use the key name directly.
 * 3. If no unique index is found, fall back to `code` (SAP Commerce convention) or `pk`.
 */
object FxSNaturalKeyResolver {

    private const val MAX_DEPTH = 3
    private const val ATTR_PK = "pk"
    private const val ATTR_CODE = "code"

    /**
     * Resolves the natural key path for [meta].
     * Returns a comma-separated string suitable for use as ImpEx nested parameter list,
     * e.g. `catalog(id),version`.
     */
    fun resolve(meta: TSGlobalMetaItem, tsAccess: TSMetaModelAccess): String =
        resolveInternal(meta, tsAccess, depth = 0)

    private fun resolveInternal(meta: TSGlobalMetaItem, tsAccess: TSMetaModelAccess, depth: Int): String {
        if (depth >= MAX_DEPTH) return ATTR_PK

        val uniqueKeys = findUniqueIndexKeys(meta)
        if (uniqueKeys.isEmpty()) {
            // Fall back to `code` if it exists as a simple attribute, otherwise `pk`
            return if (meta.allAttributes.containsKey(ATTR_CODE)) ATTR_CODE else ATTR_PK
        }

        return uniqueKeys.joinToString(",") { keyAttrName ->
            // Relation-declared FK attrs (e.g. CatalogVersion.catalog) live in allRelationEnds,
            // not allAttributes — check both to get the FK target type for recursion.
            val attrType = meta.allAttributes[keyAttrName]?.type
                ?: meta.allRelationEnds
                    .firstOrNull { it.qualifier?.equals(keyAttrName, ignoreCase = true) == true && it.cardinality == Cardinality.ONE }
                    ?.type

            if (attrType != null) {
                val attrMeta = tsAccess.findMetaItemByName(attrType)
                if (attrMeta != null) {
                    // Recursively resolve the FK's natural key
                    val subPath = resolveInternal(attrMeta, tsAccess, depth + 1)
                    "$keyAttrName($subPath)"
                } else {
                    keyAttrName
                }
            } else {
                keyAttrName
            }
        }
    }

    /**
     * Returns the ordered keys of the first unique index on [meta] that contains at least
     * one non-pk attribute.
     *
     * Primary strategy: explicit `<index unique="true">` declarations (captures composite keys).
     * Fallback: individual unique-modifier attributes and ONE-cardinality relation ends (best-effort
     * for types without explicit index declarations).
     */
    private fun findUniqueIndexKeys(meta: TSGlobalMetaItem): List<String> {
        val fromIndexes = meta.allIndexes
            .filter { it.isUnique }
            .map { it.keys.filterNot { key -> key.equals(ATTR_PK, ignoreCase = true) }.toList() }
            .filter { it.isNotEmpty() }
            .minByOrNull { it.size }  // prefer the smallest unique key
        if (fromIndexes != null) return fromIndexes

        // No explicit unique index — fall back to attributes/relation-ends declared with unique=true modifier.
        // This only captures single-attribute uniqueness; composite keys require an index declaration.
        val fromModifiers = mutableListOf<String>()
        meta.allAttributes.entries
            .filter { (_, attr) -> attr.modifiers.isUnique }
            .mapTo(fromModifiers) { (name, _) -> name }
        meta.allRelationEnds
            .filter { it.cardinality == Cardinality.ONE && it.modifiers.isUnique }
            .mapNotNull { it.qualifier }
            .forEach { fromModifiers.add(it) }
        return fromModifiers
    }
}
