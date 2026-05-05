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

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.groovy.GroovyExecConstants
import sap.commerce.toolset.groovy.editor.groovyExecContextSettings
import sap.commerce.toolset.groovy.editor.groovyWebContexts
import sap.commerce.toolset.groovy.editor.groovyWebContextsFetching
import sap.commerce.toolset.ui.ActionButtonWithTextAndDescriptionComponent

class GroovyWebContextActionGroup : DefaultActionGroup(
    "Choose Spring Web Context",
    true
), CustomComponentAction {

    init {
        templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
    }

    override fun getChildren(e: AnActionEvent?): Array<out AnAction> {
        val editor = e?.getData(CommonDataKeys.EDITOR) ?: return emptyArray()
        val webContexts = editor.groovyWebContexts

        return buildList {
            add(GroovyWebContextAction(GroovyExecConstants.DEFAULT_WEB_CONTEXT))
            add(Separator.create())

            if (!editor.groovyWebContextsFetching) {
                add(Separator.create())

                if (webContexts != null) {
                    webContexts.forEach { webContext -> add(GroovyWebContextAction(webContext)) }
                    add(Separator.create())
                }
            }

            add(ActionManager.getInstance().getAction("yGroovyWebContextsLoad"))
        }
            .toTypedArray()
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val activeWebContext = editor.groovyExecContextSettings?.webContext
            ?: GroovyExecConstants.DEFAULT_WEB_CONTEXT

        e.presentation.text = "Context: $activeWebContext"
        e.presentation.description = "Web application context"
        e.presentation.icon = HybrisIcons.Groovy.WEB_CONTEXT_ACTIVE
    }

    override fun createCustomComponent(presentation: Presentation, place: String) = ActionButtonWithTextAndDescriptionComponent(
        actionGroup = this,
        presentation = presentation,
        place = place,
        title = "Web Application Context"
    )

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}

