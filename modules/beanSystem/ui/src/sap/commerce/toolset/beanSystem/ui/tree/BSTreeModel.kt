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

package sap.commerce.toolset.beanSystem.ui.tree

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.application
import com.intellij.util.concurrency.Invoker
import com.intellij.util.concurrency.InvokerSupplier
import sap.commerce.toolset.beanSystem.meta.BSGlobalMetaModel
import sap.commerce.toolset.beanSystem.ui.tree.nodes.BSNode
import javax.swing.tree.TreePath

class BSTreeModel(private val rootTreeNode: BSTreeNode, val project: Project) : com.intellij.ui.tree.BaseTreeModel<BSTreeNode>(), Disposable, InvokerSupplier {

    private var globalMetaModel: BSGlobalMetaModel? = null
    private val nodes = mutableMapOf<BSNode, BSTreeNode>()
    private val myInvoker = if (application.isUnitTestMode) {
        Invoker.forEventDispatchThread(this)
    } else {
        Invoker.forBackgroundThreadWithReadAction(this)
    }

    override fun getRoot() = rootTreeNode

    override fun getChildren(parent: Any?) = if (parent == rootTreeNode
        || (
            globalMetaModel != null
                && parent is BSTreeNode
                && parent.allowsChildren
                && parent.userObject is BSNode
            )
    ) {
        ((parent as BSTreeNode).userObject as BSNode).getChildren(globalMetaModel)
            .onEach { it.update() }
            .map { nodes.computeIfAbsent(it) { bsNode -> BSTreeNode(bsNode) } }
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