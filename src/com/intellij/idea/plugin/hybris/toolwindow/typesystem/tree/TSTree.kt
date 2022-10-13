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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree

import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.nodes.TSNode
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.nodes.TSRootNode
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.containers.Convertor
import org.jetbrains.annotations.NonNls
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

private const val SEARCH_CAN_EXPAND = true

class TSTree(val myProject: Project) : Tree(), DataProvider, Disposable {

    init {
        isRootVisible = false
        model = buildTreeModel(TSRootNode(this))

        val tree = this
        addTreeSelectionListener { _ ->
            val node = tree.lastSelectedPathComponent;

        }
        TreeSpeedSearch(this, Convertor { treePath: TreePath ->
            when (val uObj = (treePath.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is TSNode -> return@Convertor uObj.name
                else -> return@Convertor ""
            }
        }, SEARCH_CAN_EXPAND)
    }

    override fun getData(dataId: @NonNls String): Any? {
        return null
    }

    companion object {
        private const val serialVersionUID: Long = -4523404713991136984L
    }

    private fun buildTreeModel(root: TSNode): TreeModel {
        val model = TSTreeModel(myProject, root)
        return AsyncTreeModel(model, SEARCH_CAN_EXPAND, this)
    }

    override fun dispose() {
    }
}