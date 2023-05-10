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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.components

import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.toolwindow.system.type.forms.*
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.TSTree
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.TreeNode
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.*
import com.intellij.idea.plugin.hybris.toolwindow.system.type.view.TSViewSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener

class TSTreePanel(
    private val myProject: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {
    val tree: TSTree
    private val myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val myMetaItemView: TSMetaItemView by lazy { TSMetaItemView(myProject) }
    private val myMetaEnumView: TSMetaEnumView by lazy { TSMetaEnumView(myProject) }
    private val myMetaAtomicView: TSMetaAtomicView by lazy { TSMetaAtomicView(myProject) }
    private val myMetaCollectionView: TSMetaCollectionView by lazy { TSMetaCollectionView(myProject) }
    private val myMetaRelationView: TSMetaRelationView by lazy { TSMetaRelationView(myProject) }
    private val myMetaMapView: TSMetaMapView by lazy { TSMetaMapView(myProject) }
    private val myTreeSelectionListener: TreeSelectionListener = treeSelectionListener()
    private val myTreeModelListener: TreeModelListener = treeModelListener()

    init {
        tree = TSTree(myProject)
        firstComponent = JBScrollPane(tree)
        secondComponent = myDefaultPanel

        tree.addTreeSelectionListener(myTreeSelectionListener)
        tree.addTreeModelListener(myTreeModelListener)
        PopupHandler.installPopupMenu(tree, "TSView.ToolWindow.TreePopup", "TSView.ToolWindow.TreePopup")

        Disposer.register(this, tree)
    }

    fun update(globalMetaModel: TSGlobalMetaModel, changeType: TSViewSettings.ChangeType) {
        tree.update(globalMetaModel, changeType)
    }

    override fun dispose() {
        tree.removeTreeSelectionListener { myTreeSelectionListener }
    }

    private fun treeSelectionListener() = TreeSelectionListener { tls ->
        val path = tls.newLeadSelectionPath
        val component = path?.lastPathComponent
        val node = (component as? TreeNode)?.userObject as? TSNode
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
                    ?.let { it as? TSNode }
                updateSecondComponent(node)
            }
        }

        override fun treeNodesInserted(e: TreeModelEvent) = Unit
        override fun treeNodesRemoved(e: TreeModelEvent) = Unit
        override fun treeStructureChanged(e: TreeModelEvent) = Unit
    }

    private fun updateSecondComponent(node: TSNode?) {
        secondComponent = when (node) {
            is TSMetaAtomicNode -> myMetaAtomicView.getContent(node.meta)
            is TSMetaCollectionNode -> myMetaCollectionView.getContent(node.meta)
            is TSMetaEnumNode -> myMetaEnumView.getContent(node.meta)
            is TSMetaEnumValueNode -> myMetaEnumView.getContent(node.parent.meta, node.meta)
            is TSMetaItemNode -> myMetaItemView.getContent(node.meta)
            is TSMetaItemIndexNode -> myMetaItemView.getContent(node.parent.meta, node.meta)
            is TSMetaItemAttributeNode -> myMetaItemView.getContent(node.parent.meta, node.meta)
            is TSMetaItemCustomPropertyNode -> myMetaItemView.getContent(node.parent.meta, node.meta)
            is TSMetaMapNode -> myMetaMapView.getContent(node.meta)
            is TSMetaRelationNode -> myMetaRelationView.getContent(node.meta)
            is TSMetaRelationElementNode -> myMetaRelationView.getContent(node.meta)
            else -> myDefaultPanel
        }
    }

    companion object {
        private const val serialVersionUID: Long = 4773839682466559598L
    }
}
