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

package com.intellij.idea.plugin.hybris.acl.file.actions

import com.intellij.idea.plugin.hybris.acl.AclLanguage
import com.intellij.idea.plugin.hybris.actions.ExecuteStatementAction
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisImpexConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionContext
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class AclExecuteAction : ExecuteStatementAction<HybrisImpexConsole>(
    AclLanguage,
    HybrisImpexConsole::class,
    message("hybris.acl.actions.execute_query"),
    message("hybris.acl.actions.execute_query.description"),
    HybrisIcons.Console.Actions.EXECUTE
) {

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val console = openConsole(project, content) ?: return
        val context = ImpExExecutionContext(
            content = content,
        )

        project.service<ImpExExecutionClient>().execute(context) { coroutineScope, result ->
            console.print(result)
        }
    }
}