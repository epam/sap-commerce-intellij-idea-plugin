/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "HybrisProjectSettings", storages = [Storage(STORAGE_HYBRIS_PROJECT_SETTINGS)])
class HybrisProjectSettingsComponent : PersistentStateComponent<HybrisProjectSettings> {
    private val hybrisProjectSettings = HybrisProjectSettings()

    override fun getState() = hybrisProjectSettings
    override fun loadState(state: HybrisProjectSettings) = XmlSerializerUtil.copyBean(state, hybrisProjectSettings)

    fun isHybrisProject() = state.hybrisProject

    fun getModuleSettings(module: Module): ModuleSettings = getModuleSettings(module.name)
    fun getAvailableExtensions(): Map<String, ExtensionDescriptor> {
        if (state.availableExtensions.isEmpty()) {
            synchronized(hybrisProjectSettings) {
                state.availableExtensions.clear()

                val availableExtensions = state.completeSetOfAvailableExtensionsInHybris
                        .map { Pair(it, ExtensionDescriptor(name = it)) }
                state.availableExtensions.putAll(availableExtensions)
                registerCloudExtensions()
            }
        }
        return state.availableExtensions
    }

    fun setAvailableExtensions(descriptors: Set<HybrisModuleDescriptor>) {
        state.availableExtensions.clear()
        descriptors
                .map { it.extensionDescriptor }
                .forEach { state.availableExtensions[it.name] = it }
        registerCloudExtensions()
    }

    fun registerCloudExtensions() = HybrisConstants.CCV2_COMMERCE_CLOUD_EXTENSIONS
            .forEach { state.availableExtensions[it] = ExtensionDescriptor(it, HybrisModuleDescriptorType.CCV2) }

    private fun getModuleSettings(moduleName: String) = state.moduleSettings
            .computeIfAbsent(moduleName) { _ -> ModuleSettings() }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): HybrisProjectSettingsComponent = project.getService(HybrisProjectSettingsComponent::class.java)
    }
}
