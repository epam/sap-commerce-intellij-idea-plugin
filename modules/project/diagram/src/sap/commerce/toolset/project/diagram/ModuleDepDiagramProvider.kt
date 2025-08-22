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
package sap.commerce.toolset.project.diagram

import com.intellij.diagram.BaseDiagramProvider
import com.intellij.diagram.DiagramPresentationModel
import com.intellij.diagram.extras.DiagramExtras
import com.intellij.diagram.settings.DiagramConfigElement
import com.intellij.diagram.settings.DiagramConfigGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Pattern
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.diagram.node.ModuleDepDiagramDataModel
import sap.commerce.toolset.project.diagram.node.graph.ModuleDepGraphNode
import javax.swing.Icon

class ModuleDepDiagramProvider : BaseDiagramProvider<ModuleDepGraphNode>() {
    @Pattern("[a-zA-Z0-9_-]*")
    override fun getID() = "HybrisModuleDependencies"
    override fun getPresentableName() = i18n("hybris.diagram.module.dependencies.provider.name")
    override fun getActionIcon(isPopup: Boolean): Icon = HybrisIcons.Module.Diagram.Actions.SHOW

    override fun createVisibilityManager() = ModuleDepDiagramVisibilityManager()
    override fun createNodeContentManager() = ModuleDepDiagramNodeContentManager()
    override fun getElementManager() = ModuleDepDiagramElementManager()
    override fun getVfsResolver() = ModuleDepDiagramVfsResolver()
    override fun getColorManager() = ModuleDepDiagramColorManager()

    override fun createDataModel(
        project: Project,
        item: ModuleDepGraphNode?,
        file: VirtualFile?,
        model: DiagramPresentationModel
    ) = ModuleDepDiagramDataModel(project, this)


    override fun getExtras(): DiagramExtras<ModuleDepGraphNode> {
        return object : DiagramExtras<ModuleDepGraphNode>() {
            override fun getAdditionalDiagramSettings() = with(DiagramConfigGroup("Categories")) {
                ModuleDepDiagramNodeContentManager.CATEGORIES
                    .map { DiagramConfigElement(it.name, true) }
                    .forEach { addElement(it) }

                arrayOf(this)
            }
        }
    }
}
