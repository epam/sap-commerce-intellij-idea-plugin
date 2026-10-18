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

package sap.commerce.toolset.properties.mcp

object CxPropertiesMcpConstants {

    const val DEFAULT_LIMIT = 100

    object Descriptions {
        const val KEY_FILTER = """Optional property-key filter, applied case-insensitively.
            |If the value is a valid regular expression it is matched with a regex search (e.g. '^db\.' or 'solr|search'); otherwise it is treated as a plain substring ('contains').
            |An instance and a project both hold thousands of properties, so pass this whenever you are asking about one area — it is what keeps the response, and the token cost, small."""

        const val LIMIT = "Maximum properties to return. Default 100. 'total' always reports how many matched, so 'truncated' tells you when a narrower filter is needed."
    }
}
