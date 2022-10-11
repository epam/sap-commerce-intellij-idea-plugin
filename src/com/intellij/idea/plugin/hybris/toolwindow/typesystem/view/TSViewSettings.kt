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
class TSViewSettings(project: Project) : PersistentStateComponent<TSViewSettings.Settings> {

    private val myMessageBus: MessageBus
    private val mySettings: Settings

    init {
        mySettings = Settings()
        myMessageBus = project.getMessageBus()
    }

    fun fireSettingsChanged(changeType: ChangeType) {
        myMessageBus.syncPublisher(TOPIC).settingsChanged(changeType)
    }

    fun isShowOnlyCustom(): Boolean = mySettings.showCustomOnly

    fun setShowOnlyCustom(state: Boolean) {
        mySettings.showCustomOnly = state
    }

    fun isShowMetaClasses(): Boolean = mySettings.showMetaClasses

    fun setShowMetaClasses(state: Boolean) {
        mySettings.showMetaClasses = state
    }

    fun isShowMetaRelations(): Boolean = mySettings.showMetaRelations

    fun setShowMetaRelations(state: Boolean) {
        mySettings.showMetaRelations = state
    }

    fun isShowMetaEnums(): Boolean = mySettings.showMetaEnums

    fun setShowMetaEnums(state: Boolean) {
        mySettings.showMetaEnums = state
    }

    fun isShowMetaCollections(): Boolean = mySettings.showMetaCollections

    fun setShowMetaCollections(state: Boolean) {
        mySettings.showMetaCollections = state
    }

    fun isShowMetaAtomics(): Boolean = mySettings.showMetaAtomics

    fun setShowMetaAtomics(state: Boolean) {
        mySettings.showMetaAtomics = state
    }

    override fun getState(): Settings = mySettings
    override fun loadState(settings: Settings) = XmlSerializerUtil.copyBean(settings, mySettings)

    class Settings {
        var showCustomOnly = true
        var showMetaClasses = true
        var showMetaRelations = true
        var showMetaEnums = true
        var showMetaCollections = true
        var showMetaAtomics = true
    }

    enum class ChangeType {
        FULL, UPDATE_LIST, UPDATE_DETAILS, FORCE_UPDATE_RIGHT_COMPONENT
    }

    interface Listener {
        fun settingsChanged(changeType: ChangeType)
    }

    companion object {
        val TOPIC: Topic<Listener> = Topic("Hybris Type System View settings", Listener::class.java)

        fun getInstance(project: Project): TSViewSettings {
            return project.getService(TSViewSettings::class.java) as TSViewSettings
        }
    }
}