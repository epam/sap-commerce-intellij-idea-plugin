/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.ui.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.tree.LeafState
import javax.swing.Icon

abstract class CxPropertiesNode(
    project: Project,
    protected var presentationName: String = "",
    private val icon: Icon? = null,
) : PresentableNodeDescriptor<CxPropertiesNode>(project, null), LeafState.Supplier, Disposable {
    internal val children = mutableMapOf<String, CxPropertiesNode>()

    override fun getElement() = this
    override fun getLeafState() = LeafState.ASYNC
    override fun dispose() = children.clear()
    override fun getName(): String = presentationName
    override fun toString() = name

    fun getChildren(): Collection<CxPropertiesNode> {
        val newChildren = getNewChildren()

        children.keys
            .filterNot(newChildren::containsKey)
            .forEach {
                children[it]?.dispose()
                children.remove(it)
            }

        newChildren.forEach { (key, value) ->
            children[key] = children[key]?.also { it.merge(value) } ?: value
        }

        return children.values
    }

    open fun getNewChildren(): Map<String, CxPropertiesNode> = emptyMap()
    protected open fun merge(newNode: CxPropertiesNode) = Unit

    override fun update(presentation: PresentationData) {
        presentation.clearText()
        presentation.addText(presentationName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.setIcon(icon)
    }
}
