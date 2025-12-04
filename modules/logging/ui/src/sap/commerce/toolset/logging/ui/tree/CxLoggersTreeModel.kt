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
import com.intellij.ui.tree.BaseTreeModel
import com.intellij.util.asSafely
import com.intellij.util.concurrency.Invoker
import com.intellij.util.concurrency.InvokerSupplier
import sap.commerce.toolset.logging.ui.tree.nodes.CxLoggersNode
import javax.swing.tree.TreePath

class CxLoggersTreeModel(
    private val rootTreeNode: CxLoggersTreeNode
) : BaseTreeModel<CxLoggersTreeNode>(), Disposable, InvokerSupplier {

    private val nodes = mutableMapOf<CxLoggersNode, CxLoggersTreeNode>()
    private val myInvoker = Invoker.forBackgroundThreadWithReadAction(this)

    override fun getRoot() = rootTreeNode

    override fun getChildren(parent: Any?) = parent
        .asSafely<CxLoggersTreeNode>()
        ?.userObject
        ?.asSafely<CxLoggersNode>()
        ?.getChildren()
        ?.onEach { it.update() }
        ?.map {
            nodes.computeIfAbsent(it) { _ -> CxLoggersTreeNode(it) }
        }

    override fun getInvoker() = myInvoker

    fun reload(path: TreePath) = treeStructureChanged(path, null, null)

    override fun dispose() {
        super.dispose()
        nodes.clear()
    }
}