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

package sap.commerce.toolset.logging.ui.tree

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.containers.Convertor
import sap.commerce.toolset.logging.ui.tree.nodes.CxLoggersNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxLoggersRootNode
import sap.commerce.toolset.ui.toolwindow.CxToolWindowActivationAware
import java.io.Serial
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

class CxLoggersTree(project: Project) : Tree(), CxToolWindowActivationAware, Disposable {

    private val rootNode = CxLoggersTreeNode(CxLoggersRootNode(project))
    private val myTreeModel = CxLoggersTreeModel(rootNode)

    init {
        isRootVisible = false

        TreeUIHelper.getInstance().installTreeSpeedSearch(this, Convertor { treePath: TreePath ->
            when (val uObj = (treePath.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is CxLoggersNode -> return@Convertor uObj.name
                else -> return@Convertor ""
            }
        }, SEARCH_CAN_EXPAND)
    }

    override fun dispose() = Unit

    override fun onActivated() = update()

    fun update() {
        if (model !is AsyncTreeModel) {
            model = AsyncTreeModel(myTreeModel, SHOW_LOADING_NODE, this)
        }

        update(TreePath(rootNode))
    }

    fun update(path: TreePath) = myTreeModel.reload(path)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -8893365004297012022L
        private const val SHOW_LOADING_NODE = true
        private const val SEARCH_CAN_EXPAND = true
    }
}