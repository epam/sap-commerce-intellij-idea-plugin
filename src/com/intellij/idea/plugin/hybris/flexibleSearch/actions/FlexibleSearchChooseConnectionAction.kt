/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.flexibleSearch.actions

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectRemoteInstancesSettingsConfigurableProvider
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.toolwindow.RemoteHacConnectionDialog
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.InplaceButton
import com.intellij.ui.popup.PopupFactoryImpl
import java.awt.Component
import java.awt.event.InputEvent
import javax.swing.Icon
import javax.swing.JComponent

class FlexibleSearchChooseConnectionAction : ActionGroup() {

    init {
        isPopup = true
        templatePresentation.isPerformGroup = true
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun displayTextInToolbar() = true
    override fun getChildren(e: AnActionEvent?) = emptyArray<AnAction>()

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val presentation = e.presentation

        val devSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
        val hacSettings = devSettings.getActiveHacRemoteConnectionSettings(project)
        presentation.text = "Active Connection: $hacSettings"
        presentation.isEnabledAndVisible = true
        presentation.icon = HybrisIcons.Y_REMOTE
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val inputEvent: InputEvent? = e.inputEvent
        val eventSource = inputEvent?.source
        val component = (eventSource as? Component)
            ?: return

        val devSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
        val items = getItems(devSettings, project)
        val step = ListPopupStep(project, component, devSettings, items)
        val popup = PopupFactoryImpl.getInstance().createListPopup(project, step) { superRenderer -> superRenderer }

        if (eventSource !is InplaceButton && eventSource !is ActionButton) {
            popup.showInBestPositionFor(e.dataContext)
        } else {
            popup.setMinimumSize((eventSource as JComponent).size)
            popup.showUnderneathOf((eventSource as Component?)!!)
        }

        popup.pack(true, true)
    }

    private fun getItems(
        devSettings: HybrisDeveloperSpecificProjectSettingsComponent,
        project: Project
    ): List<ListItem> {
        val activeConnection = devSettings.getActiveHacRemoteConnectionSettings(project)
        val connectionItems = devSettings.hacRemoteConnectionSettings
            .map {
                if (it == activeConnection) ActiveConnectionItem(it)
                else ConnectionItem(it)
            }
        val actionItems = listOf(
            CreateConnectionItem(),
            EditConnectionItem(),
            ConnectionSettingsItem()
        )
        return actionItems + connectionItems
    }

    abstract class ListItem {
        abstract fun getText(): String
        abstract fun getIcon(): Icon
    }

    class CreateConnectionItem : ListItem() {
        override fun getText() = "Create new connection"
        override fun getIcon() = AllIcons.General.Add
    }

    class EditConnectionItem : ListItem() {
        override fun getText() = "Edit active connection"
        override fun getIcon() = AllIcons.Actions.Edit
    }

    class ConnectionSettingsItem : ListItem() {
        override fun getText() = "Connection settings"
        override fun getIcon(): Icon = HybrisIcons.SETTINGS
    }

    open class ConnectionItem(val settings: HybrisRemoteConnectionSettings) : ListItem() {
        override fun getText() = settings.toString()
        override fun getIcon(): Icon = HybrisIcons.Y_REMOTE_GREEN
    }

    class ActiveConnectionItem(settings: HybrisRemoteConnectionSettings) : ConnectionItem(settings) {
        override fun getIcon(): Icon = HybrisIcons.Y_REMOTE
    }

    private class ListPopupStep(
        private val project: Project,
        private val owner: Component,
        private val devSettings: HybrisDeveloperSpecificProjectSettingsComponent,
        items: List<ListItem>
    ) : BaseListPopupStep<ListItem>(null, items) {

        private val connectionItems = items
            .filterIsInstance<ConnectionItem>()

        override fun onChosen(selectedValue: ListItem?, finalChoice: Boolean): PopupStep<*>? {
            invokeLater {
                when (selectedValue) {
                    is CreateConnectionItem -> consumeConnectionSettings(devSettings.getDefaultHacRemoteConnectionSettings(project))
                    is EditConnectionItem -> consumeConnectionSettings(devSettings.getActiveHacRemoteConnectionSettings(project))
                    is ConnectionSettingsItem -> ShowSettingsUtil.getInstance()
                        .showSettingsDialog(project, HybrisProjectRemoteInstancesSettingsConfigurableProvider.SettingsConfigurable::class.java)

                    is ConnectionItem -> devSettings.setActiveHacRemoteConnectionSettings(selectedValue.settings)
                }
            }

            return FINAL_CHOICE
        }

        private fun consumeConnectionSettings(connection: HybrisRemoteConnectionSettings) {
            val dialog = RemoteHacConnectionDialog(project, owner, connection)
            if (dialog.showAndGet()) {
                val connections = HashSet(devSettings.hacRemoteConnectionSettings)
                connections.add(connection)
                devSettings.saveRemoteConnectionSettingsList(HybrisRemoteConnectionSettings.Type.Hybris, connections.toList())
            }
        }

        override fun getSeparatorAbove(value: ListItem?) = if (connectionItems.firstOrNull() == value) ListSeparator("Available Connections")
        else null

        override fun isAutoSelectionEnabled() = false
        override fun isSpeedSearchEnabled() = true
        override fun getTextFor(value: ListItem?) = value?.getText() ?: "?"
        override fun getIconFor(value: ListItem?) = value?.getIcon()
    }

}
