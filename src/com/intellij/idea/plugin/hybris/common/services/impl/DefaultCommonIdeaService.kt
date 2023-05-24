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
package com.intellij.idea.plugin.hybris.common.services.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

class DefaultCommonIdeaService : CommonIdeaService {
    private val commandProcessor: CommandProcessor = CommandProcessor.getInstance()
    override fun isTypingActionInProgress(): Boolean {
        val currentCommandName = commandProcessor.currentCommandName ?: return false

        return HybrisConstants.TYPING_EDITOR_ACTIONS.any {
            currentCommandName.equals(it, true)
        } || HybrisConstants.UNDO_REDO_EDITOR_ACTIONS.any {
            currentCommandName.startsWith(it)
        }
    }

    override fun isPotentiallyHybrisProject(project: Project): Boolean {
        val modules = ModuleManager.getInstance(project).modules
        if (modules.isEmpty()) return false

        val moduleNames = modules
            .map { it.name }

        val acceleratorNames = listOf("*cockpits", "*core", "*facades", "*storefront")
        if (matchAllModuleNames(acceleratorNames, moduleNames)) return true

        val webservicesNames = listOf(
            "*${HybrisConstants.EXTENSION_NAME_HMC}",
            HybrisConstants.EXTENSION_NAME_HMC,
            HybrisConstants.EXTENSION_NAME_PLATFORM
        )

        return matchAllModuleNames(webservicesNames, moduleNames)
    }

    private fun matchAllModuleNames(namePatterns: Collection<String>, moduleNames: Collection<String>) = namePatterns
        .all { matchModuleName(it, moduleNames) }

    override fun getPlatformDescriptor(hybrisProjectDescriptor: HybrisProjectDescriptor) = hybrisProjectDescriptor
        .foundModules
        .firstNotNullOfOrNull { it as? PlatformHybrisModuleDescriptor }

    override fun getActiveHacUrl(project: Project) = getActiveHacRemoteConnectionSettings(project)
        .let { getUrl(it) }

    override fun getActiveSslProtocol(project: Project, settings: HybrisRemoteConnectionSettings?) = getProjectSettings(project, settings)
        ?.sslProtocol
        ?: HybrisConstants.DEFAULT_SSL_PROTOCOL

    override fun getHostHacUrl(project: Project, settings: HybrisRemoteConnectionSettings?) = getProjectSettings(project, settings)
        .let(::getUrl)

    private fun getProjectSettings(project: Project, settings: HybrisRemoteConnectionSettings?) =
        settings ?: getActiveHacRemoteConnectionSettings(project)

    private fun getActiveHacRemoteConnectionSettings(project: Project) =
        HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
            .getActiveHacRemoteConnectionSettings(project)

    //TODO: with enabling kotlin DSL 2.0 settings shouldn't be required anymore
    override fun getSolrUrl(project: Project, settings: HybrisRemoteConnectionSettings?): String {
        val currentSettings = settings ?: HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
            .getActiveSolrRemoteConnectionSettings(project)

        return buildString {
            if (currentSettings.isSsl) append(HybrisConstants.HTTPS_PROTOCOL) else append(HybrisConstants.HTTP_PROTOCOL)
            append(currentSettings.hostIP)
            append(":")
            append(currentSettings.port)
            append("/")
            append(currentSettings.solrWebroot)
        }
    }

    override fun fixRemoteConnectionSettings(project: Project) {
        val settings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
        val state = settings.state
        val connectionList = state.remoteConnectionSettingsList
        connectionList.forEach {
            prepareSslRemoteConnectionSettings(it)
        }

        if (settings.hacRemoteConnectionSettings.isEmpty()) {
            val newSettings = settings.getDefaultHacRemoteConnectionSettings(project)
            connectionList.add(newSettings)
            state.activeRemoteConnectionID = newSettings.uuid
        }

        if (settings.solrRemoteConnectionSettings.isEmpty()) {
            val newSettings = settings.getDefaultSolrRemoteConnectionSettings(project)
            connectionList.add(newSettings)
            state.activeSolrConnectionID = newSettings.uuid
        }
    }

    private fun prepareSslRemoteConnectionSettings(connectionSettings: HybrisRemoteConnectionSettings) {
        connectionSettings.isSsl = connectionSettings.generatedURL
            ?.startsWith(HybrisConstants.HTTPS_PROTOCOL)
            ?: false

        cleanUpRemoteConnectionSettingsHostIp(connectionSettings)
    }

    private fun cleanUpRemoteConnectionSettingsHostIp(connectionSettings: HybrisRemoteConnectionSettings) {
        val regex = Regex("https?://")
        connectionSettings.hostIP = connectionSettings.hostIP
            ?.replace(regex, "")
    }

    private fun matchModuleName(pattern: String, moduleNames: Collection<String>) = moduleNames
        .any { it.matches(Regex("\\Q$pattern\\E".replace("*", "\\E.*\\Q"))) }

    private fun getUrl(settings: HybrisRemoteConnectionSettings) =
        buildString {
            if (settings.isSsl) append(HybrisConstants.HTTPS_PROTOCOL) else append(HybrisConstants.HTTP_PROTOCOL)
            append(settings.hostIP)
            append(HybrisConstants.URL_PORT_DELIMITER)
            append(settings.port)

            settings.hacWebroot
                ?.takeUnless { it.isBlank() }
                ?.let {
                    append('/')
                    append(
                        it.trimStart(' ', '/')
                            .trimEnd(' ', '/')
                    )
                }
        }
}
