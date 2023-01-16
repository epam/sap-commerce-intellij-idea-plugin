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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.tree

import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.TSNode
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.TSRootNode
import com.intellij.idea.plugin.hybris.toolwindow.system.type.view.TSViewSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.treeStructure.Tree
import org.jetbrains.annotations.NonNls
import java.util.function.Function
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

private const val SHOW_LOADING_NODE = true
private const val SEARCH_CAN_EXPAND = true

class TSTree(val myProject: Project) : Tree(), DataProvider, Disposable {

    private val myTreeModel = TSTreeModel(TSRootNode(this))

    init {
        isRootVisible = false
        model = AsyncTreeModel(myTreeModel, SHOW_LOADING_NODE, this)

        TreeSpeedSearch(this, SEARCH_CAN_EXPAND, Function { treePath: TreePath ->
            when (val uObj = (treePath.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is TSNode -> return@Function uObj.name
                else -> return@Function ""
            }
        })
    }

    override fun getData(dataId: @NonNls String): Any? {
        return null
    }

    override fun dispose() {
    }

    fun update(changeType: TSViewSettings.ChangeType) {
        // handle UPDATE case better, adjust visibility of the existing nodes
        if (changeType == TSViewSettings.ChangeType.FULL || changeType == TSViewSettings.ChangeType.UPDATE) {
            myTreeModel.reload()
        }
    }

    companion object {
        private const val serialVersionUID: Long = -4523404713991136984L
    }
}