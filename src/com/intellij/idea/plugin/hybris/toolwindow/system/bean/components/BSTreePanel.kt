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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.components

import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms.BSMetaBeanView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms.BSMetaEnumView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.BSTree
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.BSTreeModel
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes.*
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.view.BSViewSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import java.io.Serial
import javax.swing.event.TreeSelectionListener

class BSTreePanel(
    private val myProject: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {
    val tree = BSTree(myProject)
    private val myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val myMetaEnumView: BSMetaEnumView by lazy {
        BSMetaEnumView(
            myProject
        )
    }
    private val myMetaBeanView: BSMetaBeanView by lazy {
        BSMetaBeanView(
            myProject
        )
    }
    private val myTreeSelectionListener: TreeSelectionListener = treeSelectionListener()

    init {
        firstComponent = JBScrollPane(tree)
        secondComponent = myDefaultPanel

        tree.addTreeSelectionListener(myTreeSelectionListener)
        PopupHandler.installPopupMenu(tree, "BSView.ToolWindow.TreePopup", "BSView.ToolWindow.TreePopup")

        Disposer.register(this, tree)
    }

    fun update(changeType: BSViewSettings.ChangeType) {
        secondComponent = myDefaultPanel;

        tree.update(changeType)
    }

    override fun dispose() {
        tree.removeTreeSelectionListener { myTreeSelectionListener }
    }

    private fun treeSelectionListener() = TreeSelectionListener { tls ->
        val path = tls.newLeadSelectionPath
        val component = path?.lastPathComponent
        if (component != null && component is BSTreeModel.Node && component.userObject is BSNode) {
            secondComponent = myDefaultPanel

            when (val node = component.userObject) {
                is BSMetaEnumNode -> secondComponent = myMetaEnumView.getContent(node.meta)
                is BSMetaEnumValueNode -> secondComponent = myMetaEnumView.getContent(node.parent.meta, node.meta)
                is BSMetaBeanNode -> secondComponent = myMetaBeanView.getContent(node.meta)
                is BSMetaPropertyNode -> secondComponent = myMetaBeanView.getContent(node.parent.meta, node.meta)
            }
        } else {
            secondComponent = myDefaultPanel
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 7171096529464716313L
    }

}
