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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.tree

import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.LoggersNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.LoggersNodeParameters
import com.intellij.openapi.Disposable
import com.intellij.ui.tree.BaseTreeModel
import com.intellij.util.asSafely
import com.intellij.util.concurrency.Invoker
import com.intellij.util.concurrency.InvokerSupplier
import javax.swing.tree.TreePath

class LoggersOptionsModel(
    private val rootTreeNode: LoggersOptionsTreeNode
) : BaseTreeModel<LoggersOptionsTreeNode>(), Disposable, InvokerSupplier {

    private var connections: Map<RemoteConnectionSettings, Boolean>? = null
    private val nodes = mutableMapOf<LoggersNode, LoggersOptionsTreeNode>()
    private val myInvoker = Invoker.forBackgroundThreadWithReadAction(this)

    override fun getRoot() = rootTreeNode

    override fun getChildren(parent: Any?) = parent
        .asSafely<LoggersOptionsTreeNode>()
        ?.userObject
        ?.asSafely<LoggersNode>()
        ?.getChildren(LoggersNodeParameters(connections ?: emptyMap()))
        ?.onEach { it.update() }
        ?.map {
            nodes.computeIfAbsent(it) { _ -> LoggersOptionsTreeNode(it) }
        }

    override fun getInvoker() = myInvoker

    fun reload(connections: Map<RemoteConnectionSettings, Boolean>) {
        this.connections = connections

        treeStructureChanged(TreePath(root), null, null)
    }

    fun reload() = treeStructureChanged(TreePath(root), null, null)

    override fun dispose() {
        super.dispose()
        nodes.clear()
    }
}