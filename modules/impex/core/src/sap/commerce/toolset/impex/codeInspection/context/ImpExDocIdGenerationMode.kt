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

package sap.commerce.toolset.impex.codeInspection.context

import sap.commerce.toolset.HybrisIcons
import javax.swing.Icon

enum class ImpExDocIdGenerationMode(
    val icon: Icon,
    val title: String,
    val description: String
) {
    COUNTER(
        HybrisIcons.ImpEx.DOC_ID_MODE_COUNTER,
        "Counter",
        "This mode generates numeric ids for each data row."
    ),
    COLUMN_BASED(
        HybrisIcons.ImpEx.DOC_ID_MODE_COLUMNS,
        "Columns",
        "This mode generates ids based on the respective values of the included columns"
    ),
}