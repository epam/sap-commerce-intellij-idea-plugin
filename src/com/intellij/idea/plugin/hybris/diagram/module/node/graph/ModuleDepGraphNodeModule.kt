/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.diagram.module.node.graph

import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.openapi.module.Module

data class ModuleDepGraphNodeModule(
    val module: Module,
    val type: ModuleDescriptorType,
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