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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.view

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "[y] Type System View settings", category = SettingsCategory.PLUGINS)
@Storage(value = HybrisConstants.STORAGE_HYBRIS_TS_VIEW, roamingType = RoamingType.DISABLED)
@Service(Service.Level.PROJECT)
class TSViewSettings(myProject: Project) : PersistentStateComponent<TSViewSettings.Settings> {

    private val myMessageBus: MessageBus
    private val mySettings: Settings

    init {
        mySettings = Settings()
        myMessageBus = myProject.messageBus
    }

    fun fireSettingsChanged(changeType: ChangeType) = changeType.also { myMessageBus.syncPublisher(TOPIC).settingsChanged(changeType) }

    fun isShowOnlyCustom(): Boolean = mySettings.showOnlyCustom
    fun setShowOnlyCustom(state: Boolean) = state.also { mySettings.showOnlyCustom = state }

    fun isShowMetaItems(): Boolean = mySettings.showMetaItems
    fun setShowMetaItems(state: Boolean) = state.also { mySettings.showMetaItems = state }

    fun isShowMetaMaps(): Boolean = mySettings.showMetaMaps
    fun setShowMetaMaps(state: Boolean) = state.also { mySettings.showMetaMaps = state }

    fun isShowMetaRelations(): Boolean = mySettings.showMetaRelations
    fun setShowMetaRelations(state: Boolean) = state.also { mySettings.showMetaRelations = state }

    fun isShowMetaEnums(): Boolean = mySettings.showMetaEnums
    fun setShowMetaEnums(state: Boolean) = state.also { mySettings.showMetaEnums = state }

    fun isShowMetaCollections(): Boolean = mySettings.showMetaCollections
    fun setShowMetaCollections(state: Boolean) = state.also { mySettings.showMetaCollections = state }

    fun isShowMetaAtomics(): Boolean = mySettings.showMetaAtomics
    fun setShowMetaAtomics(state: Boolean) = state.also { mySettings.showMetaAtomics = it }

    fun isShowMetaEnumValues(): Boolean = mySettings.showMetaEnumValues
    fun setShowMetaEnumValues(state: Boolean) = state.also { mySettings.showMetaEnumValues = state }

    fun isShowMetaItemIndexes(): Boolean = mySettings.showMetaItemIndexes
    fun setShowMetaItemIndexes(state: Boolean) = state.also { mySettings.showMetaItemIndexes = state }

    fun isShowMetaItemAttributes(): Boolean = mySettings.showMetaItemAttributes
    fun setShowMetaItemAttributes(state: Boolean) = state.also { mySettings.showMetaItemAttributes = state }

    fun isShowMetaItemCustomProperties(): Boolean = mySettings.showMetaItemCustomProperties
    fun setShowMetaItemCustomProperties(state: Boolean) = state.also { mySettings.showMetaItemCustomProperties = state }

    fun isGroupItemByParent(): Boolean = mySettings.groupItemByParent
    fun setGroupItemByParent(state: Boolean) = state.also { mySettings.groupItemByParent = state }

    override fun getState(): Settings = mySettings
    override fun loadState(settings: Settings) = XmlSerializerUtil.copyBean(settings, mySettings)

    class Settings {
        var showOnlyCustom = false
        var showMetaItems = true
        var showMetaRelations = true
        var showMetaEnums = true
        var showMetaCollections = true
        var showMetaAtomics = true
        var showMetaMaps = true
        var showMetaEnumValues = true
        var showMetaItemIndexes = true
        var showMetaItemAttributes = true
        var showMetaItemCustomProperties = true
        var groupItemByParent = false
    }

    enum class ChangeType {
        FULL,UPDATE
    }

    interface Listener {
        fun settingsChanged(changeType: ChangeType)
    }

    companion object {
        val TOPIC: Topic<Listener> = Topic("Hybris Type System View settings", Listener::class.java)

        fun getInstance(project: Project): TSViewSettings = project.getService(TSViewSettings::class.java)
    }
}