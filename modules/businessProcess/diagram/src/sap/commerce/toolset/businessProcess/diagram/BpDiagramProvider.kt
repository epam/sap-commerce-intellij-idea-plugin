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
package sap.commerce.toolset.businessProcess.diagram

import com.intellij.diagram.*
import com.intellij.diagram.extras.DiagramExtras
import com.intellij.diagram.settings.DiagramConfigElement
import com.intellij.diagram.settings.DiagramConfigGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Pattern
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.businessProcess.diagram.node.BpDiagramDataModel
import sap.commerce.toolset.businessProcess.diagram.node.graph.BpGraphNode
import sap.commerce.toolset.i18n
import javax.swing.Icon

class BpDiagramProvider : BaseDiagramProvider<BpGraphNode>() {

    @Pattern("[a-zA-Z0-9_-]*")
    override fun getID() = "HybrisBusinessProcessDiagramProvider"
    override fun getPresentableName() = i18n("hybris.diagram.bp.provider.name")
    override fun getActionIcon(isPopup: Boolean): Icon = HybrisIcons.BusinessProcess.FILE

    override fun createNodeContentManager(): DiagramNodeContentManager = BpDiagramNodeContentManager()
    override fun getElementManager(): DiagramElementManager<BpGraphNode> = BpDiagramElementManager()
    override fun getVfsResolver(): BpDiagramVfsResolver = BpDiagramVfsResolver()
    override fun getColorManager(): DiagramColorManager = BpDiagramColorManager()

    override fun createDataModel(
        project: Project,
        node: BpGraphNode?,
        virtualFile: VirtualFile?,
        diagramPresentationModel: DiagramPresentationModel
    ) = BpDiagramDataModel(project, node, this)

    override fun getExtras(): DiagramExtras<BpGraphNode> {
        return object : DiagramExtras<BpGraphNode>() {
            override fun getAdditionalDiagramSettings() = with(DiagramConfigGroup("Categories")) {
                BpDiagramNodeContentManager.CATEGORIES
                    .map { DiagramConfigElement(it.name, true) }
                    .forEach { addElement(it) }

                arrayOf(this)
            }
        }
    }

}
