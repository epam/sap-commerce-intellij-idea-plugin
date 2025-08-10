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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.templates

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.LoggersNodeParameters
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.LoggersOptionsNode
import com.intellij.openapi.project.Project

class LoggersTemplateLoggersOptionsNode(project: Project) : LoggersOptionsNode("Logger Templates", HybrisIcons.Log.Template.TEMPLATES, project) {

    override fun getNewChildren(parameters: LoggersNodeParameters) = listOf(
        BundledLoggersTemplateLoggersOptionsNode(project),
        CustomLoggersTemplateLoggersOptionsNode(project)
    ).associateBy { it.name }
}

class BundledLoggersTemplateLoggersOptionsNode(project: Project) : LoggersOptionsNode("Bundled", HybrisIcons.Log.Template.BUNDLED, project)
class CustomLoggersTemplateLoggersOptionsNode(project: Project) : LoggersOptionsNode("Custom", HybrisIcons.Log.Template.CUSTOM, project)