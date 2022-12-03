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

package com.intellij.idea.plugin.hybris.toolwindow.beans.tree

import com.intellij.idea.plugin.hybris.toolwindow.beans.tree.nodes.BeansNode
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.tree.BaseTreeModel
import com.intellij.util.concurrency.Invoker
import com.intellij.util.concurrency.InvokerSupplier
import javax.swing.tree.DefaultMutableTreeNode

class BeansTreeModel(private val root: BeansNode)
    : BaseTreeModel<BeansTreeModel.Node>(), Disposable, InvokerSupplier {

    private val myInvoker = if (ApplicationManager.getApplication().isUnitTestMode) {
        Invoker.forEventDispatchThread(this)
    } else {
        Invoker.forBackgroundThreadWithReadAction(this)
    }

    override fun getRoot() = Node(root)

    override fun getChildren(parent: Any?): List<Node> {
        if (parent !is Node || !parent.allowsChildren || parent.userObject !is BeansNode) {
            return emptyList();
        }

        return (parent.userObject as BeansNode).getChildren()
            .onEach { it!!.update() }
            .map { Node(it) }
    }

    fun reload() {
        treeNodesChanged(null, null, null)
    }

    class Node(private val node : BeansNode?) : DefaultMutableTreeNode(node) {

        override fun toString() = node.toString()
    }

    override fun getInvoker() = myInvoker

}