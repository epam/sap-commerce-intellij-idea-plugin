/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.tree

import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.TSNode
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.TSRootNode
import com.intellij.idea.plugin.hybris.toolwindow.system.type.view.TSViewSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.containers.Convertor
import java.io.Serial
import javax.swing.event.TreeModelListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

private const val SHOW_LOADING_NODE = true
private const val SEARCH_CAN_EXPAND = true

class TSTree(val myProject: Project) : Tree(), DataProvider, Disposable {

    private val myTreeModel = TSTreeModel(TreeNode(TSRootNode(this)))
    private var previousSelection: TreeNode? = null

    init {
        isRootVisible = false
        model = AsyncTreeModel(myTreeModel, SHOW_LOADING_NODE, this)

        TreeUIHelper.getInstance().installTreeSpeedSearch(this, Convertor { treePath: TreePath ->
            when (val uObj = (treePath.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is TSNode -> return@Convertor uObj.name
                else -> return@Convertor ""
            }
        }, SEARCH_CAN_EXPAND)
    }

    override fun getData(dataId: String) = null
    override fun dispose() = Unit

    fun update(globalMetaModel: TSGlobalMetaModel, changeType: TSViewSettings.ChangeType) {
        if (changeType == TSViewSettings.ChangeType.FULL || changeType == TSViewSettings.ChangeType.UPDATE) {
            previousSelection = lastSelectedPathComponent as? TreeNode

            myTreeModel.reload(globalMetaModel)
        }
    }

    fun addTreeModelListener(listener: TreeModelListener) = model.addTreeModelListener(listener)

    companion object {
        @Serial
        private val serialVersionUID: Long = -4523404713991136984L
    }
}