/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.diagram.node.graph

import com.intellij.openapi.module.Module
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.SubModuleDescriptorType

data class ModuleDepGraphNodeModule(
    val module: Module,
    val type: ModuleDescriptorType,
    val subModuleType: SubModuleDescriptorType?,
    override val name: String,
    override val properties: Array<ModuleDepGraphField>
) : ModuleDepGraphNode {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModuleDepGraphNodeModule) return false

        if (module != other.module) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = module.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}