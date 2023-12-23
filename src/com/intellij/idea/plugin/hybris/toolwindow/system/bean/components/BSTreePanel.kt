/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.system.bean.meta.BSGlobalMetaModel
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms.BSMetaBeanView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms.BSMetaEnumView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.BSTree
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.TreeNode
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
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener

class BSTreePanel(
    private val myProject: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {
    val tree: BSTree
    private val myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val myMetaEnumView: BSMetaEnumView by lazy { BSMetaEnumView(myProject) }
    private val myMetaBeanView: BSMetaBeanView by lazy { BSMetaBeanView(myProject) }
    private val myTreeSelectionListener: TreeSelectionListener = treeSelectionListener()
    private val myTreeModelListener: TreeModelListener = treeModelListener()

    init {
        tree = BSTree(myProject)
        firstComponent = JBScrollPane(tree)
        secondComponent = myDefaultPanel

        tree.addTreeSelectionListener(myTreeSelectionListener)
        tree.addTreeModelListener(myTreeModelListener)
        PopupHandler.installPopupMenu(tree, "BSView.ToolWindow.TreePopup", "BSView.ToolWindow.TreePopup")

        Disposer.register(this, tree)
    }

    fun update(globalMetaModel: BSGlobalMetaModel, changeType: BSViewSettings.ChangeType) {
        tree.update(globalMetaModel, changeType)
    }

    override fun dispose() {
        tree.removeTreeSelectionListener { myTreeSelectionListener }
    }

    private fun treeSelectionListener() = TreeSelectionListener { tls ->
        val path = tls.newLeadSelectionPath
        val component = path?.lastPathComponent
        val node = (component as? TreeNode)?.userObject as? BSNode

        updateSecondComponent(node)
    }

    private fun treeModelListener() = object : TreeModelListener {
        override fun treeNodesChanged(e: TreeModelEvent) {
            if (e.treePath?.lastPathComponent == tree.selectionPath?.parentPath?.lastPathComponent) {
                val node = tree
                    .selectionPath
                    ?.lastPathComponent
                    ?.let { it as? TreeNode }
                    ?.userObject
                    ?.let { it as? BSNode }
                updateSecondComponent(node)
            }
        }
        override fun treeNodesInserted(e: TreeModelEvent) = Unit
        override fun treeNodesRemoved(e: TreeModelEvent) = Unit
        override fun treeStructureChanged(e: TreeModelEvent) = Unit
    }

    private fun updateSecondComponent(node: BSNode?) {
        secondComponent = when (node) {
            is BSMetaEnumNode -> myMetaEnumView.getContent(node.meta)
            is BSMetaEnumValueNode -> myMetaEnumView.getContent(node.parent.meta, node.meta)
            is BSMetaBeanNode -> myMetaBeanView.getContent(node.meta)
            is BSMetaPropertyNode -> myMetaBeanView.getContent(node.parent.meta, node.meta)
            else -> myDefaultPanel
        }
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 7171096529464716313L
    }

}
