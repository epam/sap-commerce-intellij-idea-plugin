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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.components

import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.toolwindow.TSMetaItemView
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.TSTree
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.TSTreeModel
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.nodes.TSMetaItemNode
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.nodes.TSNode
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane

class TSTreePanel(
    val myProject: Project,
    myGroupId: String = "HybrisTypeSystemTreePanel"
) : OnePixelSplitter(), Disposable {
    private var myTree = TSTree(myProject)
    private var myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))

    init {
        firstComponent = JBScrollPane(myTree)
        secondComponent = myDefaultPanel

        myTree.addTreeSelectionListener { tls ->
            val path = tls.newLeadSelectionPath
            val component = path.lastPathComponent
            if (component is TSTreeModel.Node && component.userObject is TSNode) {
                secondComponent = myDefaultPanel

                when (val tsNode = component.userObject) {
                    is TSMetaItemNode -> secondComponent = TSMetaItemView.create(myProject, tsNode.meta)
                }
            }
        }
    }

    companion object {
        private const val serialVersionUID: Long = 4773839682466559598L
    }
}
