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

package sap.commerce.toolset.logging.ui.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.tree.LeafState

abstract class LoggersNode : PresentableNodeDescriptor<LoggersNode>, LeafState.Supplier, Disposable {

    internal val myChildren = mutableMapOf<String, LoggersNode>()
    internal var parameters: LoggersNodeParameters? = null

    protected constructor(project: Project) : super(project, null)

    override fun getElement() = this
    override fun getLeafState() = LeafState.ASYNC
    override fun toString(): String = name
    override fun dispose() = myChildren.clear()

    abstract override fun update(presentation: PresentationData)

    fun getChildren(parameters: LoggersNodeParameters): Collection<LoggersNode> {
        val newChildren = getNewChildren(parameters)

        myChildren.keys
            .filterNot { newChildren.containsKey(it) }
            .forEach {
                myChildren[it]?.dispose()
                myChildren.remove(it)
            }

        newChildren.forEach { (newName, newNode) ->
            if (myChildren[newName] == null) {
                myChildren[newName] = newNode
            } else {
                update(myChildren[newName]!!, newNode)
            }
        }

        return myChildren.values
            .onEach { it.parameters = parameters }
    }

    open fun getNewChildren(nodeParameters: LoggersNodeParameters): Map<String, LoggersNode> = emptyMap()
    open fun update(existingNode: LoggersNode, newNode: LoggersNode) = Unit
}