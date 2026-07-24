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
 * Analyzed result of a FlexibleSearch query for ImpEx conversion.
 *
 * @param primaryType            The primary item type from the FROM clause.
 * @param columns                Ordered list of columns matching the HAC result headers.
 * @param uniqueAttributeNames   Attribute names that appear in equality conditions in the WHERE clause.
 * @param joinUniqueColumns      JOIN-resolved unique attributes absent from the SELECT list — must be
 *                               appended as synthetic columns in the ImpEx output.
 * @param joinNaturalKeyByAttr   Maps root-type FK attribute name (lowercase) → the nested natural
 *                               key path merged from all WHERE JOIN equality conditions on that FK's
 *                               chain (e.g. `catalog(id),version` for a two-level JOIN).
 *                               Used to override the type-system-derived composite key when the query
 *                               only constrains a subset of the FK type's unique attributes.
 */
data class FxSQueryInfo(
    val primaryType: String,
    val columns: List<FxSColumn>,
    val uniqueAttributeNames: Set<String>,
    val joinUniqueColumns: List<FxSJoinUniqueColumn> = emptyList(),
    val joinNaturalKeyByAttr: Map<String, String> = emptyMap(),
)