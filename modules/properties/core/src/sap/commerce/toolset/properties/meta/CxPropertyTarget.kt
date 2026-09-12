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

package sap.commerce.toolset.properties.meta

import com.intellij.openapi.vfs.VirtualFile

/**
 * A properties file of the project which new declarations may be written to.
 *
 * Only two kinds qualify: the `local.properties` of the config extension, and the `project.properties` of a custom
 * extension. Everything else in the chain belongs to the platform or to out-of-the-box extensions, where a hand-written
 * declaration would be lost on the next update.
 */
data class CxPropertyTarget(
    val file: VirtualFile,
    val scope: CxPropertyScope,
    val extension: String? = null,
) {

    val presentableName
        get() = extension
            ?.let { "$it/${file.name}" }
            ?: file.name

    override fun toString() = presentableName
}
