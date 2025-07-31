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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.tree.LeafState

abstract class LoggerNode : PresentableNodeDescriptor<LoggerNode>, LeafState.Supplier, Disposable {

    private val myChildren = mutableMapOf<LoggerNode, MutableSet<LoggerNode>>()

    protected constructor(project: Project) : super(project, null)
    protected constructor(parent: LoggerNode) : super(parent.project, parent)

    abstract override fun update(presentation: PresentationData)

    override fun getElement() = this

    override fun getLeafState() = LeafState.ASYNC

    override fun dispose() {
        myChildren.clear()
    }

    override fun toString() = name

    open fun getChildren(parameters: LoggerNodeParameters): Collection<LoggerNode> = emptyList()
}

data class LoggerNodeParameters(val connections: Map<Boolean, RemoteConnectionSettings>)