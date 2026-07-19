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
 *    - If the attribute type is itself a `ComposedType`, recurse (up to [FxSTypeSnapshot.MAX_DEPTH]).
 *    - Otherwise, use the key name directly.
 * 3. If no unique index is found, fall back to unique-modifier attributes/relation-ends,
 *    then `code` (SAP Commerce convention), then `pk`.
 */
object FxSNaturalKeyResolver {

    private const val ATTR_PK = "pk"
    private const val ATTR_CODE = "code"

    /**
     * Minimal type-system snapshot needed for natural key resolution — no IntelliJ
     * platform types, fully unit-testable.
     *
     * @param hasCodeAttr     True if the type declares a `code` attribute.
     * @param uniqueIndexKeys Ordered unique-index key attribute names (excluding `pk`).
     *                        Empty if no suitable `<index unique="true">` is declared.
     * @param attrTypes       Map of lower-case attribute/relation-end name → SAP Commerce type name
     *                        for every attribute whose type is potentially a `ComposedType` FK.
     *                        Scalar attributes may be omitted; missing entries cause the key name
     *                        to be emitted as-is without recursion.
     */
    internal data class FxSTypeSnapshot(
        val hasCodeAttr: Boolean,
        val uniqueIndexKeys: List<String>,
        val attrTypes: Map<String, String>,
    ) {
        companion object {
            internal const val MAX_DEPTH = 3
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Resolves the natural key path for [meta].
     * Returns a comma-separated string suitable for use as ImpEx nested parameter list,
     * e.g. `catalog(id),version`.
     */
    fun resolve(meta: TSGlobalMetaItem, tsAccess: TSMetaModelAccess): String =
        resolve(buildSnapshot(meta)) { name -> tsAccess.findMetaItemByName(name)?.let { buildSnapshot(it) } }

    // -------------------------------------------------------------------------
    // Internal testable API
    // -------------------------------------------------------------------------

    /**
     * Resolves the natural key path from a plain [FxSTypeSnapshot].
     *
     * [lookupSnapshot] is called for each FK attribute type to obtain the nested type's snapshot
     * for recursive resolution. Return `null` to use the attribute name as-is (no nesting).
     *
     * This overload has no IntelliJ platform dependencies and is used by unit tests.
     */
    internal fun resolve(
        snapshot: FxSTypeSnapshot,
        lookupSnapshot: (typeName: String) -> FxSTypeSnapshot?,
    ): String = resolveInternal(snapshot, lookupSnapshot, depth = 0)

    // -------------------------------------------------------------------------
    // Core algorithm
    // -------------------------------------------------------------------------

    private fun resolveInternal(
        snapshot: FxSTypeSnapshot,
        lookupSnapshot: (String) -> FxSTypeSnapshot?,
        depth: Int,
    ): String {
        if (depth >= FxSTypeSnapshot.MAX_DEPTH) return ATTR_PK

        val uniqueKeys = snapshot.uniqueIndexKeys
        if (uniqueKeys.isEmpty()) {
            return if (snapshot.hasCodeAttr) ATTR_CODE else ATTR_PK
        }

        return uniqueKeys.joinToString(",") { keyAttrName ->
            val attrType = snapshot.attrTypes[keyAttrName.lowercase()]
            if (attrType != null) {
                val childSnapshot = lookupSnapshot(attrType)
                if (childSnapshot != null) {
                    val subPath = resolveInternal(childSnapshot, lookupSnapshot, depth + 1)
                    "$keyAttrName($subPath)"
                } else {
                    keyAttrName
                }
            } else {
                keyAttrName
            }
        }
    }

    // -------------------------------------------------------------------------
    // Snapshot builder (platform-facing)
    // -------------------------------------------------------------------------

    private fun buildSnapshot(meta: TSGlobalMetaItem): FxSTypeSnapshot {
        val uniqueIndexKeys = findUniqueIndexKeys(meta)
        return FxSTypeSnapshot(
            hasCodeAttr = meta.allAttributes.containsKey(ATTR_CODE),
            uniqueIndexKeys = uniqueIndexKeys,
            attrTypes = buildAttrTypes(meta),
        )
    }

    private fun buildAttrTypes(meta: TSGlobalMetaItem): Map<String, String> {
        val result = mutableMapOf<String, String>()
        meta.allAttributes.forEach { (name, attr) ->
            val type = attr.type ?: return@forEach
            result[name.lowercase()] = type
        }
        // Relation-declared FK attrs (e.g. CatalogVersion.catalog) live in allRelationEnds,
        // not allAttributes — add them if not already present.
        meta.allRelationEnds
            .filter { it.cardinality == Cardinality.ONE }
            .forEach { end ->
                val qualifier = end.qualifier?.lowercase() ?: return@forEach
                result.putIfAbsent(qualifier, end.type)
            }
        return result
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
            .minByOrNull { it.size }
        if (fromIndexes != null) return fromIndexes

        // No explicit unique index — fall back to attributes/relation-ends declared with unique=true modifier.
        // This only captures single-attribute uniqueness; composite keys require an index declaration.
        // Relation-end (FK) attrs are added first — they typically precede scalar attrs in composite
        // SAP Commerce natural keys (e.g. catalogVersion: catalog first, then version).
        val fromModifiers = mutableListOf<String>()
        meta.allRelationEnds
            .filter { it.cardinality == Cardinality.ONE && it.modifiers.isUnique }
            .mapNotNull { it.qualifier }
            .forEach { fromModifiers.add(it) }
        meta.allAttributes.entries
            .filter { (_, attr) -> attr.modifiers.isUnique }
            .mapTo(fromModifiers) { (name, _) -> name }
        return fromModifiers
    }
}
