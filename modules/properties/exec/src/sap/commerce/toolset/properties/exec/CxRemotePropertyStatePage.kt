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

package sap.commerce.toolset.properties.exec

import sap.commerce.toolset.properties.presentation.CxPropertyPresentation

/**
 * Cumulative snapshot of properties loaded so far from the backend for a given filter.
 *
 * The list grows as additional pages are fetched via infinite scroll. [lastLoadedPage]
 * tracks the highest 1-indexed page that has been merged in. [totalItems] is the server's
 * total count for the current filter, so [hasMore] = true means another page can be fetched.
 */
data class CxRemotePropertyStatePage(
    val lastLoadedPage: Int,
    val pageSize: Int,
    val totalItems: Int,
    val keyFilter: String,
    val valueFilter: String,
    val properties: List<CxPropertyPresentation>,
) {
    val hasMore: Boolean
        get() = properties.size < totalItems

    val loadedCount: Int
        get() = properties.size
}
