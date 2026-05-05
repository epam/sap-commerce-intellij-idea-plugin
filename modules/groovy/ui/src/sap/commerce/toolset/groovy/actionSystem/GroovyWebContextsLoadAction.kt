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

package sap.commerce.toolset.groovy.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.ui.AnimatedIcon
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.groovy.GroovyExecService
import sap.commerce.toolset.groovy.groovyWebContexts
import sap.commerce.toolset.groovy.groovyWebContextsFetching

class GroovyWebContextsLoadAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (virtualFile.groovyWebContextsFetching) {
            e.presentation.isEnabled = false
            e.presentation.disabledIcon = HybrisIcons.Groovy.WEB_CONTEXTS_LOAD
            e.presentation.text = "Loading Web Contexts..."
            return
        }

        val webContexts = virtualFile.groovyWebContexts

        e.presentation.text = "${if (webContexts == null) "Load" else "Reload"} Web Contexts"
        e.presentation.icon = if (webContexts == null) HybrisIcons.Groovy.WEB_CONTEXTS_LOAD
        else HybrisIcons.Groovy.WEB_CONTEXTS_RELOAD
        e.presentation.disabledIcon = AnimatedIcon.Default.INSTANCE
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        GroovyExecService.getInstance(project).fetchWebApplicationContexts(virtualFile)
    }
}
