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
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.forms.*
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.TSTree
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.TSTreeModel
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.nodes.*
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.view.TSViewSettings
import com.intellij.idea.plugin.hybris.type.system.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.type.system.meta.TSListener
import com.intellij.idea.plugin.hybris.type.system.meta.impl.TSMetaModelAccessImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import javax.swing.event.TreeSelectionListener

class TSTreePanel(
    private val myProject: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {
    private var myTree = TSTree(myProject)
    private var myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val myMetaItemView: TSMetaItemView by lazy { TSMetaItemView(myProject) }
    private val myMetaEnumView: TSMetaEnumView by lazy { TSMetaEnumView(myProject) }
    private val myMetaAtomicView: TSMetaAtomicView by lazy { TSMetaAtomicView(myProject) }
    private val myMetaCollectionView: TSMetaCollectionView by lazy { TSMetaCollectionView(myProject) }
    private val myMetaRelationView: TSMetaRelationView by lazy { TSMetaRelationView(myProject) }
    private val myMetaMapView: TSMetaMapView by lazy { TSMetaMapView(myProject) }
    private val myTreeSelectionListener: TreeSelectionListener = treeSelectionListener()

    init {
        firstComponent = JBScrollPane(myTree)
        secondComponent = myDefaultPanel

        myTree.addTreeSelectionListener(myTreeSelectionListener)

        myProject.messageBus.connect(this).subscribe(TSMetaModelAccessImpl.topic, object : TSListener {
            override fun typeSystemChanged(globalMetaModel: TSGlobalMetaModel) {
                AppUIExecutor.onUiThread().expireWith(myTree).submit {
                    secondComponent = myDefaultPanel;
                    myTree.update(TSViewSettings.ChangeType.FULL)
                }
            }
        })
    }

    fun update(changeType: TSViewSettings.ChangeType) {
        myTree.update(changeType)
    }

    override fun dispose() {
        myTree.removeTreeSelectionListener { myTreeSelectionListener }
    }

    private fun treeSelectionListener() = TreeSelectionListener { tls ->
        val path = tls.newLeadSelectionPath
        val component = path?.lastPathComponent
        if (component != null && component is TSTreeModel.Node && component.userObject is TSNode) {
            secondComponent = myDefaultPanel

            when (val node = component.userObject) {
                is TSMetaAtomicNode -> secondComponent = myMetaAtomicView.getContent(node.meta)
                is TSMetaCollectionNode -> secondComponent = myMetaCollectionView.getContent(node.meta)
                is TSMetaEnumNode -> secondComponent = myMetaEnumView.getContent(node.meta)
                is TSMetaEnumValueNode -> secondComponent = myMetaEnumView.getContent(node.parent.meta, node.meta)
                is TSMetaItemNode -> secondComponent = myMetaItemView.getContent(node.meta)
                is TSMetaItemIndexNode -> secondComponent = myMetaItemView.getContent(node.parent.meta, node.meta)
                is TSMetaItemAttributeNode -> secondComponent = myMetaItemView.getContent(node.parent.meta, node.meta)
                is TSMetaItemCustomPropertyNode -> secondComponent = myMetaItemView.getContent(node.parent.meta, node.meta)
                is TSMetaMapNode -> secondComponent = myMetaMapView.getContent(node.meta)
                is TSMetaRelationNode -> secondComponent = myMetaRelationView.getContent(node.meta)
                is TSMetaRelationElementNode -> secondComponent = myMetaRelationView.getContent(node.meta)
            }
        } else {
            secondComponent = myDefaultPanel
        }
    }

    companion object {
        private const val serialVersionUID: Long = 4773839682466559598L
    }
}
