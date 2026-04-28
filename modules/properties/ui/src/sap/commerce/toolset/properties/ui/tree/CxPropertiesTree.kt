/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.ui.tree

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.containers.Convertor
import sap.commerce.toolset.properties.ui.tree.nodes.CxPropertiesNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxPropertiesRootNode
import sap.commerce.toolset.ui.toolwindow.CxToolWindowActivationAware
import java.io.Serial
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

class CxPropertiesTree(project: Project) : Tree(), CxToolWindowActivationAware, Disposable {
    private val rootNode = CxPropertiesTreeNode(CxPropertiesRootNode(project))
    private val myTreeModel = CxPropertiesTreeModel(rootNode)

    init {
        Disposer.register(this, myTreeModel)
        isRootVisible = false

        TreeUIHelper.getInstance().installTreeSpeedSearch(this, Convertor { path: TreePath ->
            when (val userObject = (path.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is CxPropertiesNode -> userObject.name
                else -> ""
            }
        }, true)
    }

    override fun onActivated() = update()
    override fun dispose() = Unit

    fun update() {
        if (model !is AsyncTreeModel) {
            model = AsyncTreeModel(myTreeModel, true, this)
        }

        myTreeModel.reload(TreePath(rootNode))
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3253518826500184703L
    }
}
