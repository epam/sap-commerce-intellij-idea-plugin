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

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.asSafely
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.properties.presentation.CxPropertyTemplatePresentation

class CxCustomPropertyTemplateItemNode private constructor(
    val uuid: String,
    var properties: List<CxPropertyPresentation>,
    private var text: String,
    project: Project,
) : CxPropertiesNode(project, text) {

    fun update(template: CxPropertyTemplatePresentation) {
        if (uuid != template.uuid) return
        properties = template.properties
        presentationName = template.name
        update(presentation)
    }

    override fun merge(newNode: CxPropertiesNode) {
        newNode.asSafely<CxCustomPropertyTemplateItemNode>()?.let {
            properties = it.properties
            presentationName = it.presentationName
        }
    }

    override fun update(presentation: PresentationData) {
        super.update(presentation)
        presentation.addText(" ${properties.size} property(s)", SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES)
    }

    companion object {
        fun of(project: Project, template: CxPropertyTemplatePresentation) = CxCustomPropertyTemplateItemNode(
            uuid = template.uuid,
            properties = template.properties,
            text = template.name,
            project = project,
        )
    }
}
