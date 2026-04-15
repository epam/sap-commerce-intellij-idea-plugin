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

package sap.commerce.toolset.project.exceptions

import java.io.Serial

class PlatformIsNotBuiltException : ProjectIsNotReadyForImportException(
    """
    Please do not import the project yet, as it has not been built - the directory <code>platform/bootstrap/gensrc</code> is missing.<br>
    Make sure to run <code>ant all</code> first, and only then proceed with importing the project.<br>
    If you proceed anyway, there is no guarantee that all plugin capabilities & features will work as expected.
""".trimIndent()
) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -2649805166814324969L
    }
}