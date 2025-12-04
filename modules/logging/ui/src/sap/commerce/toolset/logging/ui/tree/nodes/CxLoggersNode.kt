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
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import javax.swing.Icon

abstract class CxLoggersNode(
    project: Project,
    private val presentationName: String = "",
    private val icon: Icon? = null,
) : PresentableNodeDescriptor<CxLoggersNode>(project, null), LeafState.Supplier, Disposable {

    internal val myChildren = mutableMapOf<String, CxLoggersNode>()

    override fun getElement() = this
    override fun getLeafState() = LeafState.ASYNC
    override fun dispose() = myChildren.clear()
    override fun toString() = name
    override fun getName(): String = presentationName

    fun getChildren(): Collection<CxLoggersNode> {
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

        return myChildren.values
    }

    open fun getNewChildren(): Map<String, CxLoggersNode> = emptyMap()
    open fun update(existingNode: CxLoggersNode, newNode: CxLoggersNode) = Unit

    override fun update(presentation: PresentationData) {
        presentation.clearText()

        presentation.addText(presentationName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.setIcon(icon)
    }
}