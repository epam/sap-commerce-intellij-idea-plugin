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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.remote

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.HacConnectionLoggersNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.LoggerNodeParameters
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.LoggersOptionsNode
import com.intellij.openapi.project.Project

class RemoteHacInstancesLoggersOptionsNode(project: Project) : LoggersOptionsNode("Remote HAC Instances", HybrisIcons.Y.REMOTES, project) {

    override fun getNewChildren(parameters: LoggerNodeParameters): Map<String, HacConnectionLoggersNode> = parameters.connections
        .filter { it.value } // only active connections
        .map { (connection, active) -> HacConnectionLoggersNode(connection, active, project) }
        .associateBy { "${it.connectionSettings.uuid}_|_${it.activeConnection}" }
}