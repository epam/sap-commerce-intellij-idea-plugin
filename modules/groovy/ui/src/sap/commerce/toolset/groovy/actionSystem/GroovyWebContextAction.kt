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
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.CheckboxAction
import sap.commerce.toolset.groovy.GroovyExecConstants
import sap.commerce.toolset.groovy.editor.groovyExecContextSettings
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService

class GroovyWebContextAction(private val webContext: String) : CheckboxAction(webContext) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return false
        val activeWebContext = editor.groovyExecContextSettings?.webContext
            ?: GroovyExecConstants.DEFAULT_WEB_CONTEXT

        return activeWebContext == webContext
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val activeWebContext = if (webContext != GroovyExecConstants.DEFAULT_WEB_CONTEXT) webContext
        else null

        editor.groovyExecContextSettings = editor.groovyExecContextSettings {
            val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
            GroovyExecContext.defaultSettings(activeConnection)
        }.copy(webContext = activeWebContext)
    }
}