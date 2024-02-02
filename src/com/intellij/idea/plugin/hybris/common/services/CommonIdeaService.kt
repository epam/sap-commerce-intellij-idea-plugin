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
package com.intellij.idea.plugin.hybris.common.services

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.PlatformModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

@Service
class CommonIdeaService {
    private val commandProcessor: CommandProcessor = CommandProcessor.getInstance()
    fun isTypingActionInProgress(): Boolean {
        val currentCommandName = commandProcessor.currentCommandName ?: return false

        return HybrisConstants.TYPING_EDITOR_ACTIONS.any {
            currentCommandName.equals(it, true)
        } || HybrisConstants.UNDO_REDO_EDITOR_ACTIONS.any {
            currentCommandName.startsWith(it)
        }
    }

    fun isPotentiallyHybrisProject(project: Project): Boolean {
        val modules = ModuleManager.getInstance(project).modules
        if (modules.isEmpty()) return false

        val moduleNames = modules
            .map { it.yExtensionName() }

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

    fun getPlatformDescriptor(hybrisProjectDescriptor: HybrisProjectDescriptor) = hybrisProjectDescriptor
        .foundModules
        .firstNotNullOfOrNull { it as? PlatformModuleDescriptor }

    fun fixRemoteConnectionSettings(project: Project) {
        listOf(
            RemoteConnectionType.Hybris,
            RemoteConnectionType.SOLR
        ).forEach {
            val remoteConnections = RemoteConnectionUtil.getRemoteConnections(project, it)

            if (remoteConnections.isEmpty()) {
                val settings = RemoteConnectionUtil.createDefaultRemoteConnectionSettings(project, it)
                RemoteConnectionUtil.addRemoteConnection(project, settings)
                RemoteConnectionUtil.setActiveRemoteConnectionSettings(project, settings)
            } else {
                fixSslRemoteConnectionSettings(remoteConnections)
            }
        }
    }

    private fun fixSslRemoteConnectionSettings(connectionSettings: Collection<HybrisRemoteConnectionSettings>) {
        connectionSettings.forEach {
            it.isSsl = it.generatedURL.startsWith(HybrisConstants.HTTPS_PROTOCOL)
            it.hostIP = it.hostIP?.replace(regex, "")
        }
    }

    private fun matchModuleName(pattern: String, moduleNames: Collection<String>) = moduleNames
        .any { it.matches(Regex("\\Q$pattern\\E".replace("*", "\\E.*\\Q"))) }

    companion object {
        private val regex = "https?://".toRegex()

        @JvmStatic
        fun getInstance(): CommonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService::class.java)
    }
}
