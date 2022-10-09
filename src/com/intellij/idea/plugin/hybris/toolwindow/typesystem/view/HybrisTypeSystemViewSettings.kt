/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.view

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "HybrisTypeSystemView")
@Storage(StoragePathMacros.WORKSPACE_FILE)
class HybrisTypeSystemViewSettings(project: Project) : PersistentStateComponent<HybrisTypeSystemViewSettings.Settings> {

    private val myMessageBus: MessageBus
    private val mySettings: Settings
    val topic: Topic<Listener> = Topic("Hybris Type System View settings", Listener::class.java)

    init {
        mySettings = Settings()
        myMessageBus = project.getMessageBus()
    }

    fun fireSettingsChanged(changeType: ChangeType) {
        myMessageBus.syncPublisher(topic).settingsChanged(changeType)
    }

    fun isShowModules(): Boolean = mySettings.showModules

    fun setShowModules(state: Boolean) {
        mySettings.showModules = state
    }

    override fun getState(): Settings = mySettings
    override fun loadState(settings: Settings) = XmlSerializerUtil.copyBean(settings, mySettings)

    class Settings {
        var showModules = true
        var showFilesets = true
        var showFiles = true
        var showImplicitBeans = true
        var showInfrastructureBeans = true
        var showDoc = true
        var showGraph = true
        var beanDetailsProportion = -1.0f
    }

    enum class ChangeType {
        FULL, UPDATE_LIST, UPDATE_DETAILS, FORCE_UPDATE_RIGHT_COMPONENT
    }

    interface Listener {
        fun settingsChanged(changeType: ChangeType)
    }

    companion object {
        fun getInstance(project: Project): HybrisTypeSystemViewSettings {
            return project.getService(HybrisTypeSystemViewSettings::class.java) as HybrisTypeSystemViewSettings
        }
    }
}