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

package sap.commerce.toolset.typeSystem.ui.components

import com.intellij.ide.IdeBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import sap.commerce.toolset.typeSystem.meta.TSGlobalMetaModel
import sap.commerce.toolset.typeSystem.settings.state.ChangeType
import sap.commerce.toolset.typeSystem.ui.forms.*
import sap.commerce.toolset.typeSystem.ui.tree.TSTree
import sap.commerce.toolset.typeSystem.ui.tree.nodes.*
import sap.commerce.toolset.ui.addTreeModelListener
import sap.commerce.toolset.ui.addTreeSelectionListener
import sap.commerce.toolset.ui.event.TreeModelListener
import sap.commerce.toolset.ui.pathData
import java.io.Serial
import javax.swing.event.TreeModelEvent

class TSTreePanel(
    private val myProject: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {

    val tree: TSTree = TSTree(myProject).apply { registerListeners(this) }
    private val myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val myMetaItemView: TSMetaItemView by lazy { TSMetaItemView(myProject) }
    private val myMetaEnumView: TSMetaEnumView by lazy { TSMetaEnumView(myProject) }
    private val myMetaAtomicView: TSMetaAtomicView by lazy { TSMetaAtomicView(myProject) }
    private val myMetaCollectionView: TSMetaCollectionView by lazy { TSMetaCollectionView(myProject) }
    private val myMetaRelationView: TSMetaRelationView by lazy { TSMetaRelationView(myProject) }
    private val myMetaMapView: TSMetaMapView by lazy { TSMetaMapView(myProject) }

    init {
        firstComponent = JBScrollPane(tree)
        secondComponent = myDefaultPanel

        PopupHandler.installPopupMenu(tree, "TSView.ToolWindow.TreePopup", "TSView.ToolWindow.TreePopup")

        Disposer.register(this, tree)
    }

    fun update(globalMetaModel: TSGlobalMetaModel, changeType: ChangeType) {
        tree.update(globalMetaModel, changeType)
    }

    private fun registerListeners(tree: TSTree) = tree
        .addTreeSelectionListener(tree) { event ->
            event.newLeadSelectionPath
                ?.pathData(TSNode::class)
                ?.let { node -> updateSecondComponent(node) }
        }
        .addTreeModelListener(tree, object : TreeModelListener {
            override fun treeNodesChanged(e: TreeModelEvent) {
                tree.selectionPath
                    ?.takeIf { it.parentPath?.lastPathComponent == e.treePath?.lastPathComponent }
                    ?.pathData(TSNode::class)
                    ?.let { updateSecondComponent(it) }
            }
        })

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
        @Serial
        private val serialVersionUID: Long = 4773839682466559598L
    }
}
