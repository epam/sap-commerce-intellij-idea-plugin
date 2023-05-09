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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.tree.LeafState
import com.intellij.ui.tree.LeafState.Supplier

abstract class TSNode : PresentableNodeDescriptor<TSNode>, Supplier, Disposable {

    private val myChildren = mutableMapOf<String, TSNode>()
    var globalMetaModel: TSGlobalMetaModel? = null

    protected constructor(project: Project) : super(project, null)
    protected constructor(parent: TSNode) : super(parent.project, parent)

    abstract override fun getName(): String

    protected abstract fun update(project: Project, presentation: PresentationData)

    override fun getElement() = this

    open fun getNewChildren(): Map<String, TSNode> = emptyMap()

    override fun dispose() {
        myChildren.clear()
    }

    fun getChildren(globalMetaModel: TSGlobalMetaModel?): Collection<TSNode> {
        this.globalMetaModel = globalMetaModel

        val newChildren = getNewChildren()

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

        return myChildren.keys
            .sorted()
            .map { myChildren[it]!! }
            .onEach { it.globalMetaModel = globalMetaModel }
    }

    open fun update(existingNode: TSNode, newNode: TSNode) {
    }

    override fun update(presentation: PresentationData) {
        if (myProject == null || myProject.isDisposed) return
        update(myProject, presentation)
    }

    override fun toString() = name

    override fun getLeafState() = LeafState.ASYNC

}