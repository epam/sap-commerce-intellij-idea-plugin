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

package sap.commerce.toolset.flexibleSearch.transform.context

/**
 * A JOIN-resolved unique attribute that is NOT present in the SELECT list.
 *
 * Produced when a WHERE clause has `{joinAlias.naturalKeyAttr} = 'value'` and the
 * JOIN's ON condition maps `joinAlias` → `fkAttributeName` on the root type.
 *
 * @param fkAttributeName   The FK attribute on the root type (e.g. `solrIndexedType`).
 * @param naturalKeyAttr    Nested natural key path merged from all WHERE equalities on the FK's
 *                          JOIN chain (e.g. `identifier` or `catalog(id),version`).
 * @param constantValue     `:`-joined literal values from the WHERE clause in the path's depth-first
 *                          leaf order, or null when any value is not statically known (bind parameter).
 */
data class FxSJoinUniqueColumn(
    val fkAttributeName: String,
    val naturalKeyAttr: String,
    val constantValue: String?,
)