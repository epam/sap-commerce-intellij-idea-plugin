/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.diagram.module.node

import com.intellij.diagram.DiagramNodeBase
import com.intellij.diagram.DiagramProvider
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.module.ModuleDepDiagramColors
import com.intellij.idea.plugin.hybris.diagram.module.node.graph.ModuleDepGraphNode
import com.intellij.idea.plugin.hybris.diagram.module.node.graph.ModuleDepGraphNodeModule
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.openapi.editor.colors.EditorColorsManager
import java.awt.Color
import java.io.Serial
import javax.swing.Icon

class ModuleDepDiagramNode(private val graphNode: ModuleDepGraphNode, provider: DiagramProvider<ModuleDepGraphNode>) : DiagramNodeBase<ModuleDepGraphNode>(provider) {

    override fun getIdentifyingElement() = graphNode
    override fun getTooltip() = identifyingElement.name
    override fun getIcon(): Icon? = graphNode
        .takeIf { it is ModuleDepGraphNodeModule }
        ?.let { (it as ModuleDepGraphNodeModule).type }
        ?.let {
            when (it) {
                ModuleDescriptorType.CUSTOM -> HybrisIcons.Extension.CUSTOM
                ModuleDescriptorType.OOTB -> HybrisIcons.Extension.OOTB
                ModuleDescriptorType.PLATFORM -> HybrisIcons.Extension.PLATFORM
                ModuleDescriptorType.EXT -> HybrisIcons.Extension.EXT
                else -> null
            }
        }

    override fun getFileTabForeground(): Color? = graphNode
        .takeIf { it is ModuleDepGraphNodeModule }
        ?.let { (it as ModuleDepGraphNodeModule).type }
        ?.let {
            when (it) {
                ModuleDescriptorType.CUSTOM -> ModuleDepDiagramColors.NODE_HEADER_CUSTOM_COLOR
                ModuleDescriptorType.OOTB -> ModuleDepDiagramColors.NODE_HEADER_OOTB_COLOR
                ModuleDescriptorType.PLATFORM,
                ModuleDescriptorType.EXT -> ModuleDepDiagramColors.NODE_HEADER_CORE_COLOR
                else -> null
            }
        }
        ?.let { EditorColorsManager.getInstance().schemeForCurrentUITheme.getColor(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = -2453613292735510114L
    }

}