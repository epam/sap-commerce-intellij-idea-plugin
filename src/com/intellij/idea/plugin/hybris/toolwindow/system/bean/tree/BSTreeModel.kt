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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree

import com.intellij.idea.plugin.hybris.system.bean.meta.BSGlobalMetaModel
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes.BSNode
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.tree.BaseTreeModel
import com.intellij.util.concurrency.Invoker
import com.intellij.util.concurrency.InvokerSupplier
import javax.swing.tree.TreePath

class BSTreeModel(private val rootTreeNode: TreeNode, val project: Project) : BaseTreeModel<TreeNode>(), Disposable, InvokerSupplier {

    private var globalMetaModel: BSGlobalMetaModel? = null
    private val nodes = mutableMapOf<BSNode, TreeNode>()
    private val myInvoker = if (ApplicationManager.getApplication().isUnitTestMode) {
        Invoker.forEventDispatchThread(this)
    } else {
        Invoker.forBackgroundThreadWithReadAction(this)
    }

    override fun getRoot() = rootTreeNode

    override fun getChildren(parent: Any?) = if (parent == rootTreeNode
        || (
            globalMetaModel != null
                && parent is TreeNode
                && parent.allowsChildren
                && parent.userObject is BSNode
            )
    ) {
        ((parent as TreeNode).userObject as BSNode).getChildren(globalMetaModel)
            .onEach { it.update() }
            .map { nodes.computeIfAbsent(it) { bsNode -> TreeNode(bsNode) } }
    } else {
        emptyList()
    }

    fun reload(globalMetaModel: BSGlobalMetaModel) {
        this.globalMetaModel = globalMetaModel

        treeStructureChanged(TreePath(root), null, null)
    }

    override fun getInvoker() = myInvoker

    override fun dispose() {
        super.dispose()
        nodes.clear()
    }

}