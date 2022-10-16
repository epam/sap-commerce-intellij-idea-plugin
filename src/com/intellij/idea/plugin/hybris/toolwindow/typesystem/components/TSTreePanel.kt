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
import com.intellij.idea.plugin.hybris.type.system.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.type.system.meta.TSListener
import com.intellij.idea.plugin.hybris.type.system.meta.impl.TSMetaModelAccessImpl
import com.intellij.idea.plugin.hybris.type.system.utils.TypeSystemUtils
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane

class TSTreePanel(
    val myProject: Project,
    myGroupId: String = "HybrisTypeSystemTreePanel"
) : OnePixelSplitter(false, 0.25f), Disposable {
    private var myTree = TSTree(myProject)
    private var myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val myMetaItemView: TSMetaItemView by lazy { TSMetaItemView(myProject) }
    private val myMetaEnumView: TSMetaEnumView by lazy { TSMetaEnumView(myProject) }
    private val myMetaAtomicView: TSMetaAtomicView by lazy { TSMetaAtomicView(myProject) }
    private val myMetaCollectionView: TSMetaCollectionView by lazy { TSMetaCollectionView(myProject) }
    private val myMetaRelationView: TSMetaRelationView by lazy { TSMetaRelationView(myProject) }
    private val myMetaMapView: TSMetaMapView by lazy { TSMetaMapView(myProject) }

    init {
        firstComponent = JBScrollPane(myTree)
        secondComponent = myDefaultPanel

        myTree.addTreeSelectionListener { tls ->
            val path = tls.newLeadSelectionPath
            val component = path?.lastPathComponent
            if (component != null && component is TSTreeModel.Node && component.userObject is TSNode) {
                secondComponent = myDefaultPanel

                when (val tsNode = component.userObject) {
                    is TSMetaItemNode -> secondComponent = myMetaItemView.getContent(tsNode.meta)
                    is TSMetaEnumNode -> secondComponent = myMetaEnumView.getContent(tsNode.meta)
                    is TSMetaAtomicNode -> secondComponent = myMetaAtomicView.getContent(tsNode.meta)
                    is TSMetaCollectionNode -> secondComponent = myMetaCollectionView.getContent(tsNode.meta)
                    is TSMetaRelationNode -> secondComponent = myMetaRelationView.getContent(tsNode.meta)
                    is TSMetaMapNode -> secondComponent = myMetaMapView.getContent(tsNode.meta)
                }
            }
        }

        myProject.messageBus.connect(this).subscribe(TSMetaModelAccessImpl.topic, object : TSListener {
            override fun typeSystemChanged(globalMetaModel: TSGlobalMetaModel) {
                AppUIExecutor.onUiThread().expireWith(myTree).submit {
                    secondComponent = myDefaultPanel;
                    myTree.update()
                }
            }
        })

        PsiManager.getInstance(myProject).addPsiTreeChangeListener(object : PsiTreeChangeListener {
            override fun beforeChildAddition(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun beforeChildRemoval(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun beforeChildReplacement(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun beforeChildMovement(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun beforeChildrenChange(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun beforePropertyChange(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun childAdded(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun childRemoved(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun childReplaced(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun childrenChanged(event: PsiTreeChangeEvent) {
                if (event.file == null) {
                    return
                }
                if (TypeSystemUtils.isTsFile(event.file!!)) {
//                    myTree.update()
                }

                println(event)
            }

            override fun childMoved(event: PsiTreeChangeEvent) {
                println(event)
            }

            override fun propertyChanged(event: PsiTreeChangeEvent) {
                println(event)
            }

        }, this)
    }

    companion object {
        private const val serialVersionUID: Long = 4773839682466559598L
    }
}
