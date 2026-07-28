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

package sap.commerce.toolset.ai.mcp

object McpConstants {

    object Formats {
        const val JSON = "JSON"
        const val FILE = "FILE"
    }

    object Descriptions {
        const val OUTPUT_FORMAT = """Output format for the response. Supported formats: JSON, FILE.
            |Use FILE to write the result to a temporary file and return its absolute path — avoids inline token limits for large responses.
            |Default: JSON."""
        const val HAC_CONNECTION_NAME = "Optional HAC connection name. Uses the active connection if not specified"
        const val HAC_CONNECTION_NAME_AUTH = "Optional HAC connection name. Uses the active connection if not specified. Must refer to a connection with AUTOMATIC authentication; MANUAL (browser) connections are rejected"
        const val SOLR_CONNECTION_NAME = "Optional Solr connection name. Uses the active connection if not specified"
    }
}