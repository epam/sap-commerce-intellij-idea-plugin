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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.components

import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.system.bean.meta.BSGlobalMetaModel
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms.BSMetaBeanView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms.BSMetaEnumView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.BSTree
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes.*
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.view.BSViewSettings
import com.intellij.idea.plugin.hybris.ui.UiUtil.addTreeModelListener
import com.intellij.idea.plugin.hybris.ui.UiUtil.addTreeSelectionListener
import com.intellij.idea.plugin.hybris.ui.UiUtil.pathData
import com.intellij.idea.plugin.hybris.ui.event.TreeModelListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import java.io.Serial
import javax.swing.event.TreeModelEvent

class BSTreePanel(
    private val myProject: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {

    val tree: BSTree = BSTree(myProject).apply { registerListeners(this) }
    private val myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val myMetaEnumView: BSMetaEnumView by lazy { BSMetaEnumView(myProject) }
    private val myMetaBeanView: BSMetaBeanView by lazy { BSMetaBeanView(myProject) }

    init {
        firstComponent = JBScrollPane(tree)
        secondComponent = myDefaultPanel

        PopupHandler.installPopupMenu(tree, "BSView.ToolWindow.TreePopup", "BSView.ToolWindow.TreePopup")

        Disposer.register(this, tree)
    }

    fun update(globalMetaModel: BSGlobalMetaModel, changeType: BSViewSettings.ChangeType) {
        tree.update(globalMetaModel, changeType)
    }

    private fun registerListeners(tree: BSTree) = tree
        .addTreeSelectionListener(tree) { event ->
            event.newLeadSelectionPath
                ?.pathData(BSNode::class)
                ?.let { node -> updateSecondComponent(node) }
        }
        .addTreeModelListener(tree, object : TreeModelListener {
            override fun treeNodesChanged(e: TreeModelEvent) {
                tree.selectionPath
                    ?.takeIf { it.parentPath?.lastPathComponent == e.treePath?.lastPathComponent }
                    ?.pathData(BSNode::class)
                    ?.let { updateSecondComponent(it) }
            }
        })

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
