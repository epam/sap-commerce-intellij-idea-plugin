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
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType

/** One kind of extension — Custom, Ext, Ootb, Platform or Config — mirroring how the Project view groups the modules. */
class CxSourceExtensionGroupNode(
    project: Project,
    private val type: ModuleDescriptorType,
    private var extensions: List<String>,
) : CxPropertiesNode(project, presentationName = type.title) {

    override fun getNewChildren(): Map<String, CxPropertiesNode> = extensions
        .map { CxSourceExtensionNode(project, it, type) }
        .associateBy { it.name }

    override fun update(presentation: PresentationData) {
        presentation.clearText()
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.addText(" ${extensions.size} extension(s)", SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES)
        presentation.setIcon(type.lazyIcon())
    }

    override fun merge(newNode: CxPropertiesNode) {
        newNode.asSafely<CxSourceExtensionGroupNode>()?.let { extensions = it.extensions }
    }
}
