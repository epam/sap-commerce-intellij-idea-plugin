/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.view

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "[y] Bean System View settings", category = SettingsCategory.PLUGINS)
@Storage(value = HybrisConstants.STORAGE_HYBRIS_BS_VIEW, roamingType = RoamingType.DISABLED)
@Service(Service.Level.PROJECT)
class BSViewSettings(myProject: Project) : PersistentStateComponent<BSViewSettings.Settings> {

    private val myMessageBus: MessageBus
    private val mySettings: Settings

    init {
        mySettings = Settings()
        myMessageBus = myProject.messageBus
    }

    fun fireSettingsChanged(changeType: ChangeType) = changeType.also { myMessageBus.syncPublisher(TOPIC).settingsChanged(changeType) }

    fun isShowOnlyCustom(): Boolean = mySettings.showCustomOnly
    fun setShowOnlyCustom(state: Boolean) = state.also { mySettings.showCustomOnly = state }

    fun isShowOnlyDeprecated(): Boolean = mySettings.showDeprecatedOnly
    fun setShowOnlyDeprecated(state: Boolean) = state.also { mySettings.showDeprecatedOnly = state }

    fun isShowEnumValues(): Boolean = mySettings.showEnumValues
    fun setShowEnumValues(state: Boolean) = state.also { mySettings.showEnumValues = state }

    fun isShowBeanProperties(): Boolean = mySettings.showBeanProperties
    fun setShowBeanProperties(state: Boolean) = state.also { mySettings.showBeanProperties = state }

    override fun getState(): Settings = mySettings
    override fun loadState(settings: Settings) = XmlSerializerUtil.copyBean(settings, mySettings)

    class Settings {
        var showCustomOnly = false
        var showDeprecatedOnly = false
        var showEnumValues = true
        var showBeanProperties = true
    }

    enum class ChangeType {
        FULL
    }

    interface Listener {
        fun settingsChanged(changeType: ChangeType)
    }

    companion object {
        val TOPIC: Topic<Listener> = Topic("Hybris Bean System View settings", Listener::class.java)

        fun getInstance(project: Project): BSViewSettings = project.getService(BSViewSettings::class.java)
    }
}