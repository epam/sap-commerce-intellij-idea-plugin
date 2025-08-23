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

package com.intellij.idea.plugin.hybris.groovy.actions

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.DeveloperSettings
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionContext
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import javax.swing.Icon

class GroovyChooseWebContextActionGroup : DefaultActionGroup({ "Choose Spring Web Context" }, true)  {

    var defaultAction : WebContextAction? = WebContextAction("default", HybrisIcons.Y.LOGO_BLUE, this)

    var currentAction : WebContextAction? = defaultAction

    val separator = Separator.create()

    val reloadWebContextAction = ReloadWebContextAction(this)

    val webContexts: MutableList<String> = mutableListOf("default")

    init {
        templatePresentation.icon = HybrisIcons.Y.LOGO_GREEN
        templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
    }

    override fun getChildren(e: AnActionEvent?): Array<out AnAction?> {
        val children = super.getChildren(e)

        val actions = children + webContexts.map { name ->
            WebContextAction(name, HybrisIcons.Y.LOGO_BLUE, this)
        }.toTypedArray()

        return actions + separator + reloadWebContextAction
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val presentation = e.presentation

        val groovySettings = DeveloperSettings.getInstance(project).state.groovySettings
        if (!groovySettings.enableScriptTemplate) {
            presentation.isVisible = false
            return
        } else {
            presentation.isVisible = true
        }

        presentation.text = this.currentAction?.actionName ?: "Select Spring Web Context"
        this.currentAction?.icon?.let { presentation.icon = it }
        presentation.isEnabledAndVisible = true
        // (this.currentAction?.actionName)?.let {
        //    val connectionContext = GroovyExecutionClient.getInstance(project).connectionContext
        //    connectionContext.springWebContexts = it
        // }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

}

class WebContextAction(val actionName: String, val icon: Icon, private val parent: GroovyChooseWebContextActionGroup) :
    AnAction(actionName, "", icon) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        parent.currentAction = this
        e.project?.let { project ->
            val connectionContext = GroovyExecutionClient.getInstance(project).connectionContext
            connectionContext.activeWebContext = parent.currentAction?.actionName
        }
    }

}

class ReloadWebContextAction(private val parent: GroovyChooseWebContextActionGroup) :
    AnAction("Reload Web Contexts", "Reloads the list of context", AllIcons.General.Refresh) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val executionClient = GroovyExecutionClient.getInstance(project)
        val context = GroovyExecutionContext(content = "springWeb.keySet().join('|')", scriptTemplate = GroovyExecutionClient.GHAC_SCRIPT_TEMPLATE_GROOVY)
        executionClient.execute(
            context = context,
            resultCallback = { _, result ->
                val contexts = result.result?.split("|")?.filter { it.isNotBlank() }?.sorted() ?: listOf("default")
                parent.webContexts.clear()
                parent.webContexts.add("default")
                parent.webContexts.addAll(contexts)
            }
        )
    }

}