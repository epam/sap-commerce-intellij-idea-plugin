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
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.ui.tree.nodes.LoggersNode
import sap.commerce.toolset.logging.ui.tree.nodes.LoggersNodeParameters
import javax.swing.tree.TreePath

class LoggersOptionsModel(
    private val rootTreeNode: LoggersOptionsTreeNode
) : BaseTreeModel<LoggersOptionsTreeNode>(), Disposable, InvokerSupplier {

    //map of connections to their active state
    private var connections: List<HacConnectionSettingsState>? = null
    private val nodes = mutableMapOf<LoggersNode, LoggersOptionsTreeNode>()
    private val myInvoker = Invoker.forBackgroundThreadWithReadAction(this)

    override fun getRoot() = rootTreeNode

    override fun getChildren(parent: Any?) = parent
        .asSafely<LoggersOptionsTreeNode>()
        ?.userObject
        ?.asSafely<LoggersNode>()
        ?.getChildren(LoggersNodeParameters(connections ?: emptyList()))
        ?.onEach { it.update() }
        ?.map {
            nodes.computeIfAbsent(it) { _ -> LoggersOptionsTreeNode(it) }
        }

    override fun getInvoker() = myInvoker

    fun reload(connections: List<HacConnectionSettingsState>) {
        this.connections = connections

        treeStructureChanged(TreePath(root), null, null)
    }

    fun reload() = treeStructureChanged(TreePath(root), null, null)

    override fun dispose() {
        super.dispose()
        nodes.clear()
    }
}