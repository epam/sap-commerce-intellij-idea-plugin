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

import sap.commerce.toolset.flexibleSearch.impex.FxSNaturalKeyResolver.FxSTypeSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [FxSNaturalKeyResolver] using [FxSTypeSnapshot] — no IntelliJ platform required.
 *
 * Each test drives the pure resolution algorithm directly:
 * - [FxSTypeSnapshot.uniqueIndexKeys] simulates the unique index declared in items.xml
 * - [FxSTypeSnapshot.attrTypes] maps attribute/relation-end names to their SAP Commerce type
 * - The `lookupSnapshot` lambda simulates [TSMetaModelAccess.findMetaItemByName]
 */
class FxSNaturalKeyResolverTest {

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private fun resolve(snapshot: FxSTypeSnapshot, db: Map<String, FxSTypeSnapshot> = emptyMap()): String =
        FxSNaturalKeyResolver.resolve(snapshot) { db[it] }

    // -------------------------------------------------------------------------
    // Single-attribute unique index
    // -------------------------------------------------------------------------

    @Test
    fun resolve_typeWithCodeAttr_noIndex_fallsBackToCode() {
        val snapshot = FxSTypeSnapshot(
            hasCodeAttr = true,
            uniqueIndexKeys = emptyList(),
            attrTypes = emptyMap(),
        )
        assertEquals("code", resolve(snapshot))
    }

    @Test
    fun resolve_typeWithoutCodeOrIndex_fallsBackToPk() {
        val snapshot = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = emptyList(),
            attrTypes = emptyMap(),
        )
        assertEquals("pk", resolve(snapshot))
    }

    @Test
    fun resolve_scalarUniqueKey_emittedAsIs() {
        // e.g. Language with unique index on isocode
        val snapshot = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = listOf("isocode"),
            attrTypes = mapOf("isocode" to "java.lang.String"),
        )
        // java.lang.String is not in the type DB → no recursion → plain "isocode"
        assertEquals("isocode", resolve(snapshot))
    }

    @Test
    fun resolve_fkUniqueKey_recursesToCode() {
        // e.g. some type with unique FK to Currency (which has `isocode` index)
        val currencySnapshot = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = listOf("isocode"),
            attrTypes = mapOf("isocode" to "java.lang.String"),
        )
        val snapshot = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = listOf("currency"),
            attrTypes = mapOf("currency" to "Currency"),
        )
        assertEquals("currency(isocode)", resolve(snapshot, mapOf("Currency" to currencySnapshot)))
    }

    // -------------------------------------------------------------------------
    // CatalogVersion — composite unique index (catalog FK + version scalar)
    // -------------------------------------------------------------------------

    @Test
    fun resolve_catalogVersion_withUniqueIndex_producesCatalogIdCommaVersion() {
        // Simulates: CatalogVersion has <index unique="true"> with keys [catalog, version]
        // catalog is a relation end → type = Catalog
        // Catalog has unique index on [id]
        val catalogSnapshot = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = listOf("id"),
            attrTypes = mapOf("id" to "java.lang.String"),
        )
        val catalogVersionSnapshot = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = listOf("catalog", "version"),
            attrTypes = mapOf(
                "catalog" to "Catalog",
                "version" to "java.lang.String",
            ),
        )
        assertEquals(
            "catalog(id),version",
            resolve(catalogVersionSnapshot, mapOf("Catalog" to catalogSnapshot))
        )
    }

    @Test
    fun resolve_catalogVersion_catalogNotInDb_emitsCatalogAsIs() {
        // Unique index found but Catalog type is not in the snapshot DB (e.g. not loaded)
        val catalogVersionSnapshot = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = listOf("catalog", "version"),
            attrTypes = mapOf(
                "catalog" to "Catalog",
                "version" to "java.lang.String",
            ),
        )
        assertEquals("catalog,version", resolve(catalogVersionSnapshot, emptyMap()))
    }

    @Test
    fun resolve_catalogVersion_attrTypeMissing_emitsKeyAsIs() {
        // attrTypes map doesn't contain catalog — falls through to plain key name
        val catalogVersionSnapshot = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = listOf("catalog", "version"),
            attrTypes = emptyMap(),  // no type info
        )
        assertEquals("catalog,version", resolve(catalogVersionSnapshot))
    }

    // -------------------------------------------------------------------------
    // Product — single unique key (code)
    // -------------------------------------------------------------------------

    @Test
    fun resolve_product_uniqueIndexOnCode_returnsCode() {
        val productSnapshot = FxSTypeSnapshot(
            hasCodeAttr = true,
            uniqueIndexKeys = listOf("code"),
            attrTypes = mapOf("code" to "java.lang.String"),
        )
        assertEquals("code", resolve(productSnapshot))
    }

    // -------------------------------------------------------------------------
    // Depth limit
    // -------------------------------------------------------------------------

    @Test
    fun resolve_maxDepthReached_returnsPk() {
        // A → B → C → D: at depth 3 (D) resolution returns "pk"
        fun snap(type: String) = FxSTypeSnapshot(
            hasCodeAttr = false,
            uniqueIndexKeys = listOf("ref"),
            attrTypes = mapOf("ref" to type),
        )
        val db = mapOf(
            "B" to snap("C"),
            "C" to snap("D"),
            "D" to snap("E"),
        )
        val snapshotA = snap("B")
        // depth 0→A, 1→B, 2→C, 3→MAX_DEPTH → pk
        assertEquals("ref(ref(ref(pk)))", resolve(snapshotA, db))
    }

    // -------------------------------------------------------------------------
    // Modifier-fallback (no explicit unique index)
    // -------------------------------------------------------------------------

    @Test
    fun resolve_noIndex_uniqueModifierFallback_singleAttr() {
        // items.xml declares <modifiers unique="true"/> on `code` but no <index>
        // uniqueIndexKeys is empty — modifier fallback should have populated it via buildSnapshot
        // but here we simulate the already-computed result:
        val snapshot = FxSTypeSnapshot(
            hasCodeAttr = true,
            uniqueIndexKeys = listOf("code"),  // populated by modifier fallback in buildSnapshot
            attrTypes = mapOf("code" to "java.lang.String"),
        )
        assertEquals("code", resolve(snapshot))
    }
}
