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
package com.intellij.idea.plugin.hybris.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.options.ProjectIntegrationsSettingsConfigurableProvider
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.toolwindow.RemoteHacConnectionDialog
import com.intellij.idea.plugin.hybris.ui.ActionButtonWithTextAndDescription
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.options.ShowSettingsUtil
import kotlinx.html.div
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import java.awt.Component
import java.awt.event.InputEvent
import javax.swing.Icon

class HacChooseConnectionAction : DefaultActionGroup() {

    init {
        templatePresentation.icon = HybrisIcons.Y.REMOTE
        templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
        templatePresentation.putClientProperty(ActionUtil.COMPONENT_PROVIDER, ActionButtonWithTextAndDescription(this))
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun getChildren(e: AnActionEvent?): Array<out AnAction> {
        val project = e?.project ?: return emptyArray()
        val actions = super.getChildren(e)

        val activeConnection = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        val connectionActions = RemoteConnectionUtil.getRemoteConnections(project, RemoteConnectionType.Hybris)
            .map {
                if (it == activeConnection) object : AbstractHacConnectionAction(it.toString(), HybrisIcons.Y.REMOTE) {
                    override fun actionPerformed(e: AnActionEvent) = RemoteConnectionUtil.setActiveRemoteConnectionSettings(project, it)
                }
                else object : AbstractHacConnectionAction(it.toString(), HybrisIcons.Y.REMOTE_GREEN) {
                    override fun actionPerformed(e: AnActionEvent) = RemoteConnectionUtil.setActiveRemoteConnectionSettings(project, it)
                }
            }
        val replicas = listOf(ActionManager.getInstance().getAction("hybris.hac.configureReplica"))

        return actions +
            Separator.create("Available Connections") +
            connectionActions +
            Separator.create("Replicas") +
            replicas
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val presentation = e.presentation

        val hacSettings = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        presentation.text = when (e.place) {
            ActionPlaces.EDITOR_TOOLBAR -> hacSettings.toString()
            HybrisActionPlaces.CONSOLE_TOOLBAR -> null
            else -> hacSettings.shortenConnectionName()
        }
        if (e.place == HybrisActionPlaces.CONSOLE_TOOLBAR) {
            presentation.putClientProperty(ActionUtil.HIDE_DROPDOWN_ICON, true)
        }
        presentation.isEnabledAndVisible = true

        presentation.description = createHTML().div {
            p { +"Switch active connection" }
            hacSettings.generatedURL
                .let { p { +it } }
        }
    }
}

abstract class AbstractHacConnectionAction(private val actionName: String, val icon: Icon) : AnAction(actionName, "", icon) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        super.update(e)

        val presentation = e.presentation

        presentation.text = actionName
        presentation.icon = icon
        presentation.isEnabledAndVisible = true
    }
}

class AddHacConnectionAction : AbstractHacConnectionAction("Create new connection", HybrisIcons.Connection.ADD) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val inputEvent: InputEvent? = e.inputEvent
        val eventSource = inputEvent?.source
        val component = (eventSource as? Component)
            ?: return

        val settings = RemoteConnectionUtil.createDefaultRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        if (RemoteHacConnectionDialog(project, component, settings).showAndGet()) {
            RemoteConnectionUtil.addRemoteConnection(project, settings)
        }
    }
}

class EditActiveHacConnectionAction : AbstractHacConnectionAction("Edit active connection", HybrisIcons.Connection.EDIT) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val inputEvent: InputEvent? = e.inputEvent
        val eventSource = inputEvent?.source
        val component = (eventSource as? Component)
            ?: return

        val settings = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        RemoteHacConnectionDialog(project, component, settings).showAndGet()
    }
}

class ConfigureHacConnectionAction : AbstractHacConnectionAction("Connection settings", HybrisIcons.SETTINGS) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        ShowSettingsUtil.getInstance()
            .showSettingsDialog(project, ProjectIntegrationsSettingsConfigurableProvider.SettingsConfigurable::class.java)
    }
}
