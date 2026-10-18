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

package sap.commerce.toolset.properties.ui.tree.nodes

import sap.commerce.toolset.project.descriptor.ModuleDescriptorType

/** A group path and the extensions sitting at each level of it. */
internal class CxSourceGroupTree {
    val subGroups = linkedMapOf<String, CxSourceGroupTree>()
    val extensions = linkedMapOf<String, ModuleDescriptorType>()

    fun put(groupPath: List<String>, extension: String, type: ModuleDescriptorType) {
        val target = groupPath.fold(this) { tree, group -> tree.subGroups.getOrPut(group) { CxSourceGroupTree() } }

        target.extensions[extension] = type
    }

    fun extensionCount(): Int = extensions.size + subGroups.values.sumOf { it.extensionCount() }
}
