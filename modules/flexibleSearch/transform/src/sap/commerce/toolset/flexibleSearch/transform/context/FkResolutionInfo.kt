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
 * Describes a follow-up FlexibleSearch query needed to resolve FK PK values to their natural key strings.
 *
 * The [fxsLookupQuery] returns rows where column 0 is the PK and columns 1..N are the natural key
 * component values. These components are joined with `:` (the ImpEx path delimiter) to form the
 * final importable string, e.g. `mcProductCatalog:Staged` for a CatalogVersion reference.
 *
 * @param typeName       SAP Commerce type name of the FK attribute (e.g. `CatalogVersion`).
 * @param fxsLookupQuery Pre-built FxS SELECT query (e.g.
 *                       `SELECT {root.pk}, {j0.id}, {root.version} FROM {CatalogVersion AS root JOIN Catalog AS j0 ON {j0.pk} = {root.catalog}}`).
 */
data class FkResolutionInfo(
    val typeName: String,
    val fxsLookupQuery: String,
)