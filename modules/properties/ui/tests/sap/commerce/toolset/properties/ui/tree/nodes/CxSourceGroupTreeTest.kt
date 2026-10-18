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
import kotlin.test.Test
import kotlin.test.assertEquals

class CxSourceGroupTreeTest {

    @Test
    fun `an extension without a group path sits at the root`() {
        val tree = CxSourceGroupTree().apply { put(emptyList(), "core", ModuleDescriptorType.PLATFORM) }

        assertEquals(setOf("core"), tree.extensions.keys)
    }

    @Test
    fun `an extension is nested under every segment of its group path`() {
        val tree = CxSourceGroupTree().apply { put(listOf("Hybris", "platform"), "core", ModuleDescriptorType.PLATFORM) }

        assertEquals<Set<String>?>(setOf("core"), tree.subGroups["Hybris"]?.subGroups["platform"]?.extensions?.keys)
    }

    @Test
    fun `extensions sharing a group path share the group`() {
        val tree = CxSourceGroupTree().apply {
            put(listOf("Custom"), "a", ModuleDescriptorType.CUSTOM)
            put(listOf("Custom"), "b", ModuleDescriptorType.CUSTOM)
        }

        assertEquals(1, tree.subGroups.size)
    }

    @Test
    fun `the extension count includes every nested group`() {
        val tree = CxSourceGroupTree().apply {
            put(listOf("Hybris"), "a", ModuleDescriptorType.OOTB)
            put(listOf("Hybris", "platform"), "b", ModuleDescriptorType.PLATFORM)
            put(emptyList(), "c", ModuleDescriptorType.CUSTOM)
        }

        assertEquals(3, tree.extensionCount())
    }
}
