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

import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.properties.settings.CxPropertyViewSettings
import sap.commerce.toolset.properties.settings.state.CxPropertySourceMode

/**
 * The project's own property files, as opposed to what a running instance reports.
 *
 * What hangs underneath depends on [CxPropertyViewSettings.sourceMode]: either a single node standing for the project
 * as a whole, or one node per extension grouped the way the Project view groups the modules.
 */
class CxSourceCodeNode(project: Project) : CxPropertiesNode(
    project = project,
    presentationName = "Source Code",
    icon = HybrisIcons.Property.SOURCE_CODE,
) {

    override fun getNewChildren(): Map<String, CxPropertiesNode> = when (CxPropertyViewSettings.getInstance(project).sourceMode) {
        CxPropertySourceMode.PROJECT -> mapOf(PROJECT_KEY to CxSourceProjectNode(project))
        CxPropertySourceMode.EXTENSIONS -> extensionGroups()
    }

    private fun extensionGroups(): Map<String, CxPropertiesNode> = project.ySettings.extensionDescriptors
        .filter { it.type in GROUPED_TYPES }
        .groupBy { it.type }
        .toSortedMap(compareBy { GROUPED_TYPES.indexOf(it) })
        .map { (type, descriptors) -> CxSourceExtensionGroupNode(project, type, descriptors.map { it.name }.sorted()) }
        .associateBy { it.name }

    companion object {
        private const val PROJECT_KEY = "project"

        /** Group order, most interesting first — a developer looks at their own extensions far more often. */
        private val GROUPED_TYPES = listOf(
            ModuleDescriptorType.CUSTOM,
            ModuleDescriptorType.EXT,
            ModuleDescriptorType.OOTB,
            ModuleDescriptorType.PLATFORM,
            ModuleDescriptorType.CONFIG,
        )
    }
}
