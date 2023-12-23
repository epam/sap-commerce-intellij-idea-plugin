/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.settings

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.facet.ExtensionDescriptor
import com.intellij.idea.plugin.hybris.facet.YFacet
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import com.intellij.openapi.components.*
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.text.VersionComparatorUtil
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "HybrisProjectSettings", storages = [Storage(STORAGE_HYBRIS_PROJECT_SETTINGS, roamingType = RoamingType.DISABLED)])
@Service(Service.Level.PROJECT)
class HybrisProjectSettingsComponent : PersistentStateComponent<HybrisProjectSettings> {
    private val hybrisProjectSettings = HybrisProjectSettings()

    override fun getState() = hybrisProjectSettings
    override fun loadState(state: HybrisProjectSettings) = XmlSerializerUtil.copyBean(state, hybrisProjectSettings)

    // TODO: improve this logic for initially non-hybris projects
    fun isHybrisProject() = state.hybrisProject

    fun isOutdatedHybrisProject(): Boolean {
        val lastImportVersion = hybrisProjectSettings.importedByVersion ?: return true
        val currentVersion = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID))
            ?.version
            ?: return true

        return VersionComparatorUtil.compare(currentVersion, lastImportVersion) > 0
    }

    fun getModuleSettings(module: Module): ExtensionDescriptor = YFacet.getState(module)
        ?: ExtensionDescriptor(module.yExtensionName())

    fun getAvailableExtensions(): Map<String, ExtensionDescriptor> {
        if (state.availableExtensions.isEmpty()) {
            synchronized(hybrisProjectSettings) {
                state.availableExtensions.clear()

                val availableExtensions = state.completeSetOfAvailableExtensionsInHybris
                    .associateWith { ExtensionDescriptor(name = it) }
                state.availableExtensions.putAll(availableExtensions)
                registerCloudExtensions()
            }
        }
        return state.availableExtensions
    }

    fun setAvailableExtensions(descriptors: Set<YModuleDescriptor>) {
        state.availableExtensions.clear()
        descriptors
            .map { it.extensionDescriptor() }
            .forEach { state.availableExtensions[it.name] = it }
        registerCloudExtensions()
    }

    fun registerCloudExtensions() = HybrisConstants.CCV2_COMMERCE_CLOUD_EXTENSIONS
        .map { ExtensionDescriptor(name = it, type = ModuleDescriptorType.CCV2) }
        .forEach { state.availableExtensions[it.name] = it }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): HybrisProjectSettingsComponent = project.getService(HybrisProjectSettingsComponent::class.java)
    }
}
