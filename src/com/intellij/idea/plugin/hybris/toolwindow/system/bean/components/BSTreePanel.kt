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
import com.intellij.idea.plugin.hybris.system.bean.meta.BSChangeListener
import com.intellij.idea.plugin.hybris.system.bean.meta.BSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.bean.meta.impl.BSMetaModelAccessImpl
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms.BSMetaBeanView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms.BSMetaEnumView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.BSTree
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.BSTreeModel
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.view.BSViewSettings
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import javax.swing.event.TreeSelectionListener

class BSTreePanel(
    private val myProject: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {
    private var myTree = BSTree(myProject)
    private var myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
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
        firstComponent = JBScrollPane(myTree)
        secondComponent = myDefaultPanel

        myTree.addTreeSelectionListener(myTreeSelectionListener)

        myProject.messageBus.connect(this).subscribe(BSMetaModelAccessImpl.topic, object : BSChangeListener {
            override fun beanSystemChanged(globalMetaModel: BSGlobalMetaModel) {
                secondComponent = myDefaultPanel;
                myTree.update(BSViewSettings.ChangeType.FULL)
            }
        })
    }

    fun update(changeType: BSViewSettings.ChangeType) {
        myTree.update(changeType)
    }

    override fun dispose() {
        myTree.removeTreeSelectionListener { myTreeSelectionListener }
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

}
