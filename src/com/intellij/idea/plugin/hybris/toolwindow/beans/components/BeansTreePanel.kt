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

package com.intellij.idea.plugin.hybris.toolwindow.beans.components

import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.beans.meta.BeansGlobalMetaModel
import com.intellij.idea.plugin.hybris.beans.meta.BeansListener
import com.intellij.idea.plugin.hybris.beans.meta.impl.BeansMetaModelAccessImpl
import com.intellij.idea.plugin.hybris.toolwindow.beans.forms.BeansMetaEnumView
import com.intellij.idea.plugin.hybris.toolwindow.beans.tree.BeansTree
import com.intellij.idea.plugin.hybris.toolwindow.beans.tree.BeansTreeModel
import com.intellij.idea.plugin.hybris.toolwindow.beans.tree.nodes.BeansMetaEnumNode
import com.intellij.idea.plugin.hybris.toolwindow.beans.tree.nodes.BeansMetaEnumValueNode
import com.intellij.idea.plugin.hybris.toolwindow.beans.tree.nodes.BeansNode
import com.intellij.idea.plugin.hybris.toolwindow.beans.view.BeansViewSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import javax.swing.event.TreeSelectionListener

class BeansTreePanel(
    private val myProject: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {
    private var myTree = BeansTree(myProject)
    private var myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val myMetaEnumView: BeansMetaEnumView by lazy { BeansMetaEnumView(myProject) }
    private val myTreeSelectionListener: TreeSelectionListener = treeSelectionListener()

    init {
        firstComponent = JBScrollPane(myTree)
        secondComponent = myDefaultPanel

        myTree.addTreeSelectionListener(myTreeSelectionListener)

        myProject.messageBus.connect(this).subscribe(BeansMetaModelAccessImpl.topic, object : BeansListener {
            override fun beansChanged(globalMetaModel: BeansGlobalMetaModel) {
                AppUIExecutor.onUiThread().expireWith(myTree).submit {
                    secondComponent = myDefaultPanel;
                    myTree.update(BeansViewSettings.ChangeType.FULL)
                }
            }
        })
    }

    fun update(changeType: BeansViewSettings.ChangeType) {
        myTree.update(changeType)
    }

    override fun dispose() {
        myTree.removeTreeSelectionListener { myTreeSelectionListener }
    }

    private fun treeSelectionListener() = TreeSelectionListener { tls ->
        val path = tls.newLeadSelectionPath
        val component = path?.lastPathComponent
        if (component != null && component is BeansTreeModel.Node && component.userObject is BeansNode) {
            secondComponent = myDefaultPanel

            when (val node = component.userObject) {
                is BeansMetaEnumNode -> secondComponent = myMetaEnumView.getContent(node.meta)
                is BeansMetaEnumValueNode -> secondComponent = myMetaEnumView.getContent(node.parent.meta, node.meta)
            }
        } else {
            secondComponent = myDefaultPanel
        }
    }

}
