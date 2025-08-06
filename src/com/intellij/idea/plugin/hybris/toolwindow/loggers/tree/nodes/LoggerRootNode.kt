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
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.LoggersOptionsTree
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.remote.RemoteHacInstancesLoggersOptionsNode
import com.intellij.ui.SimpleTextAttributes

//BSRootNode
class LoggerRootNode(tree: LoggersOptionsTree) : LoggerNode(tree.myProject) {

    override fun update(presentation: PresentationData) {
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    override fun getNewChildren(parameters: LoggerNodeParameters) = listOf(
        RemoteHacInstancesLoggersOptionsNode(project)
        //LoggersTemplateLoggersOptionsNode(project)
    ).associateBy { it.name }
}