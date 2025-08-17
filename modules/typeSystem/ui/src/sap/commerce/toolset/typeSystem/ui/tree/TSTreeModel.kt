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
import com.intellij.ui.tree.BaseTreeModel
import com.intellij.util.application
import com.intellij.util.concurrency.Invoker
import com.intellij.util.concurrency.InvokerSupplier
import sap.commerce.toolset.typeSystem.meta.TSGlobalMetaModel
import sap.commerce.toolset.typeSystem.ui.tree.nodes.TSNode
import javax.swing.tree.TreePath

class TSTreeModel(private val rootTreeNode: TSTreeNode) : BaseTreeModel<TSTreeNode>(), Disposable, InvokerSupplier {

    private var globalMetaModel: TSGlobalMetaModel? = null
    private val nodes = mutableMapOf<TSNode, TSTreeNode>()
    private val myInvoker = if (application.isUnitTestMode) {
        Invoker.forEventDispatchThread(this)
    } else {
        Invoker.forBackgroundThreadWithReadAction(this)
    }

    override fun getRoot() = rootTreeNode

    override fun getChildren(parent: Any?) = if (parent == rootTreeNode
        || (
            globalMetaModel != null
                && parent is TSTreeNode
                && parent.allowsChildren
                && parent.userObject is TSNode
            )
    ) {
        ((parent as TSTreeNode).userObject as TSNode).getChildren(globalMetaModel)
            .onEach { it.update() }
            .map { nodes.computeIfAbsent(it) { tsNode -> TSTreeNode(tsNode) } }
    } else {
        emptyList()
    }

    fun reload(globalMetaModel: TSGlobalMetaModel) {
        this.globalMetaModel = globalMetaModel

        treeStructureChanged(TreePath(root), null, null)
    }

    override fun getInvoker() = myInvoker

    override fun dispose() {
        super.dispose()
        nodes.clear()
    }

}