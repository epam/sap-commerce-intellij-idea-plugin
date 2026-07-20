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

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the PSI-free path-merging logic of [FxSQueryAnalyzer].
 *
 * [FxSQueryAnalyzer.mergeNaturalKeyPaths] turns WHERE-condition attribute paths (relative to a
 * JOIN chain's root FK type) into a single nested ImpEx parameter path. Multi-level JOIN chains
 * contribute nested segments (e.g. `{t1.id}` reached via `{t0.catalog}` → `[catalog, id]`).
 */
class FxSQueryAnalyzerTest {

    // -------------------------------------------------------------------------
    // mergeNaturalKeyPaths()
    // -------------------------------------------------------------------------

    @Test
    fun mergeNaturalKeyPaths_singleFlatAttr() {
        assertEquals("identifier", FxSQueryAnalyzer.mergeNaturalKeyPaths(listOf(listOf("identifier"))))
    }

    @Test
    fun mergeNaturalKeyPaths_multipleFlatAttrs_preserveOrder() {
        assertEquals(
            "identifier,indexname",
            FxSQueryAnalyzer.mergeNaturalKeyPaths(listOf(listOf("identifier"), listOf("indexname"))),
        )
    }

    /**
     * Example 2 from the reported issue:
     * `GenericVariantProduct JOIN CatalogVersion t0 ON {t0.pk}={t.catalogversion} JOIN Catalog t1 ON {t1.pk}={t0.catalog}`
     * with `{t1.id}='mcProductCatalog'` and `{t0.version}='Staged'`.
     *
     * `{t1.id}` contributes `[catalog, id]`, `{t0.version}` contributes `[version]` →
     * merged nested path `catalog(id),version` — NOT the flat (and wrong) `id,version`.
     */
    @Test
    fun mergeNaturalKeyPaths_secondLevelJoinAttr_nestedUnderChainSegment() {
        assertEquals(
            "catalog(id),version",
            FxSQueryAnalyzer.mergeNaturalKeyPaths(listOf(listOf("catalog", "id"), listOf("version"))),
        )
    }

    /**
     * Example 1 from the reported issue — three-level chain off `baseProduct`:
     * `{t0.code}` → `[code]`, `{t2.id}` → `[catalogVersion, catalog, id]`,
     * `{t1.version}` → `[catalogVersion, version]`.
     *
     * Sibling paths sharing the `catalogVersion` prefix merge under one nested group.
     */
    @Test
    fun mergeNaturalKeyPaths_threeLevelChain_mergesSharedPrefix() {
        assertEquals(
            "code,catalogVersion(catalog(id),version)",
            FxSQueryAnalyzer.mergeNaturalKeyPaths(
                listOf(
                    listOf("code"),
                    listOf("catalogVersion", "catalog", "id"),
                    listOf("catalogVersion", "version"),
                )
            ),
        )
    }

    @Test
    fun mergeNaturalKeyPaths_orderFollowsFirstAppearance() {
        // version condition appears before the catalog(id) one in the WHERE clause
        assertEquals(
            "version,catalog(id)",
            FxSQueryAnalyzer.mergeNaturalKeyPaths(listOf(listOf("version"), listOf("catalog", "id"))),
        )
    }

    @Test
    fun mergeNaturalKeyPaths_duplicatePaths_collapsed() {
        assertEquals(
            "catalog(id),version",
            FxSQueryAnalyzer.mergeNaturalKeyPaths(
                listOf(
                    listOf("catalog", "id"),
                    listOf("version"),
                    listOf("catalog", "id"),
                )
            ),
        )
    }
}
