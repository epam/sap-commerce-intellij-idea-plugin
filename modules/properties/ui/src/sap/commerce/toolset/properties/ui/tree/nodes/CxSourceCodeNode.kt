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

import com.intellij.icons.AllIcons
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.settings.ApplicationSettings
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.project.yExtensionDescriptor
import sap.commerce.toolset.project.yExtensionName
import sap.commerce.toolset.properties.settings.CxPropertyViewSettings
import sap.commerce.toolset.properties.settings.state.CxPropertySourceMode
import javax.swing.Icon

/**
 * The project's own property files, as opposed to what a running instance reports.
 *
 * What hangs underneath depends on [CxPropertyViewSettings.sourceMode]: either a single node standing for the project
 * as a whole, or one node per extension, grouped the way the Project view groups the modules.
 *
 * That grouping is not recomputed here - it is read back off the module names the import produced, where the group
 * path is the qualified prefix. Whatever the Project view shows, this shows: the configured group names, the nesting
 * of a custom extension's directories, and any `group.override` a project declares.
 */
class CxSourceCodeNode(project: Project) : CxPropertiesNode(
    project = project,
    presentationName = "Source Code",
    icon = HybrisIcons.Property.SOURCE_CODE,
) {

    override fun getNewChildren(): Map<String, CxPropertiesNode> = when (CxPropertyViewSettings.getInstance(project).sourceMode) {
        CxPropertySourceMode.PROJECT -> mapOf(PROJECT_KEY to CxSourceProjectNode(project))
        CxPropertySourceMode.EXTENSIONS -> groupTree().toNodes()
    }

    /** Extensions arranged under their module group path, the deepest group last. */
    private fun groupTree(): GroupTree {
        val moduleMapping = project.ySettings.module2extensionMapping
        val root = GroupTree()

        ModuleManager.getInstance(project).modules
            .forEach { module ->
                val descriptor = module.yExtensionDescriptor ?: return@forEach
                // Sub-modules carry their owner's properties, so only the extension itself is worth a node.
                if (descriptor.subModuleType != null || descriptor.type !in GROUPED_TYPES) return@forEach
                val extension = module.yExtensionName(moduleMapping) ?: return@forEach

                root.put(module.name.split(MODULE_GROUP_SEPARATOR).dropLast(1), extension, descriptor.type)
            }

        return root
    }

    private fun GroupTree.toNodes(): Map<String, CxPropertiesNode> {
        val groups = subGroups.entries
            .sortedWith(compareBy({ rankOf(it.key) }, { it.key }))
            .map { (title, subGroup) ->
                CxSourceExtensionGroupNode(project, title, iconOf(title), subGroup.toNodes(), subGroup.extensionCount())
            }
        val extensions = this.extensions.entries
            .sortedBy { it.key }
            .map { (extension, type) -> CxSourceExtensionNode(project, extension, type) }

        return (groups + extensions).associateBy { it.name }
    }

    /** Keeps the well-known groups in their familiar order; anything a project names itself follows, alphabetically. */
    private fun rankOf(title: String) = with(ApplicationSettings.getInstance()) {
        listOf(groupCustom, groupOtherCustom, groupHybris, groupPlatform)
            .indexOf(title)
            .takeIf { it >= 0 }
            ?: Int.MAX_VALUE
    }

    private fun iconOf(title: String): Icon = with(ApplicationSettings.getInstance()) {
        when (title) {
            groupCustom, groupOtherCustom -> ModuleDescriptorType.CUSTOM.lazyIcon()
            groupHybris -> ModuleDescriptorType.OOTB.lazyIcon()
            groupPlatform -> ModuleDescriptorType.PLATFORM.lazyIcon()
            else -> AllIcons.Nodes.ModuleGroup
        }
    }

    /** A group path and the extensions sitting at each level of it. */
    private class GroupTree {
        val subGroups = linkedMapOf<String, GroupTree>()
        val extensions = linkedMapOf<String, ModuleDescriptorType>()

        fun put(groupPath: List<String>, extension: String, type: ModuleDescriptorType) {
            val target = groupPath.fold(this) { tree, group -> tree.subGroups.getOrPut(group) { GroupTree() } }

            target.extensions[extension] = type
        }

        fun extensionCount(): Int = extensions.size + subGroups.values.sumOf { it.extensionCount() }
    }

    companion object {
        private const val PROJECT_KEY = "project"
        private const val MODULE_GROUP_SEPARATOR = '.'

        /**
         * The config extension is left out on purpose: it carries `local.properties`, never a `project.properties`,
         * so a group for it would only ever be a list of dead ends.
         */
        private val GROUPED_TYPES = setOf(
            ModuleDescriptorType.CUSTOM,
            ModuleDescriptorType.EXT,
            ModuleDescriptorType.OOTB,
            ModuleDescriptorType.PLATFORM,
        )
    }
}
