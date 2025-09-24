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

package sap.commerce.toolset.project.view

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacetConstants
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.yExtensionName

class HybrisProjectViewNodeDecorator : ProjectViewNodeDecorator {

    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        when (node) {
            is PsiDirectoryNode -> decorateModule(node, data)
        }
    }

    private fun decorateModule(node: PsiDirectoryNode, data: PresentationData) {
        val vf = node.virtualFile ?: return
        val module = ProjectRootManager.getInstance(node.project).fileIndex.getModuleForFile(vf) ?: return
        val projectSettings = ProjectSettings.getInstance(module.project)

        if (!projectSettings.showFullModuleName) {
            data.coloredText
                .firstOrNull { it.text == "[${module.name}]" }
                ?.let { data.coloredText.remove(it) }
        }

        ModuleRootManager.getInstance(module).contentRoots
            .find { it == node.virtualFile }
            ?: return

        val extensionDescriptor = YFacetConstants.getModuleSettings(module)

        if (HybrisConstants.EXTENSION_NAME_KOTLIN_NATURE == module.yExtensionName() && Plugin.KOTLIN.isActive()) {
            data.setIcon(HybrisIcons.Extension.KOTLIN_NATURE)
            return
        }

        val subModuleType = extensionDescriptor.subModuleType
        if (subModuleType != null) {
            data.setIcon(subModuleType.icon)
            return
        }

        if (extensionDescriptor.type != ModuleDescriptorType.NONE) data.setIcon(extensionDescriptor.type.icon)
    }
}