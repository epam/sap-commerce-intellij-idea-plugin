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

package sap.commerce.toolset.typeSystem.ui.tree

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.containers.Convertor
import sap.commerce.toolset.typeSystem.meta.TSGlobalMetaModel
import sap.commerce.toolset.typeSystem.settings.state.ChangeType
import sap.commerce.toolset.typeSystem.ui.tree.nodes.TSNode
import sap.commerce.toolset.typeSystem.ui.tree.nodes.TSRootNode
import java.io.Serial
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

private const val SHOW_LOADING_NODE = true
private const val SEARCH_CAN_EXPAND = true

class TSTree(val myProject: Project) : Tree(), Disposable {

    private val myTreeModel = TSTreeModel(TSTreeNode(TSRootNode(this)))
    private var previousSelection: TSTreeNode? = null

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

    override fun dispose() = Unit

    fun update(globalMetaModel: TSGlobalMetaModel, changeType: ChangeType) {
        if (changeType == ChangeType.FULL || changeType == ChangeType.UPDATE) {
            previousSelection = lastSelectedPathComponent as? TSTreeNode

            myTreeModel.reload(globalMetaModel)
        }
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -4523404713991136984L
    }
}