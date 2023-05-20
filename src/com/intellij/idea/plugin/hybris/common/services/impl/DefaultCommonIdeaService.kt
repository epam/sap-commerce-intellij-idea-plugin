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

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.Version
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.util.text.VersionComparatorUtil
import org.apache.commons.lang3.StringUtils
import java.util.function.Consumer

val regex = Regex("https?://")

class DefaultCommonIdeaService : CommonIdeaService {
    private val commandProcessor: CommandProcessor = CommandProcessor.getInstance()
    override fun isTypingActionInProgress(): Boolean {
        val isTyping = StringUtils.equalsAnyIgnoreCase(
            commandProcessor.currentCommandName, *HybrisConstants.TYPING_EDITOR_ACTIONS
        )
        val isUndoOrRedo = StringUtils.startsWithAny(
            commandProcessor.currentCommandName, *HybrisConstants.UNDO_REDO_EDITOR_ACTIONS
        )
        return isTyping || isUndoOrRedo
    }

    override fun isOutDatedHybrisProject(project: Project): Boolean {
        val hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project).state
        val lastImportVersion = hybrisProjectSettings.importedByVersion ?: return true
        val currentVersion = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID))?.version
            ?: return true

        return VersionComparatorUtil.compare(currentVersion, lastImportVersion) > 0
    }

    override fun isPotentiallyHybrisProject(project: Project): Boolean {
        val modules = ModuleManager.getInstance(project).modules
        if (modules.isEmpty()) {
            return false
        }
        val moduleNames = modules.map { it.name }
        val acceleratorNames: Collection<String> = listOf("*cockpits", "*core", "*facades", "*storefront")
        if (matchAllModuleNames(acceleratorNames, moduleNames)) {
            return true
        }
        val webservicesNames: Collection<String> = listOf(
            "*${HybrisConstants.EXTENSION_NAME_HMC}",
            HybrisConstants.EXTENSION_NAME_HMC,
            HybrisConstants.EXTENSION_NAME_PLATFORM
        )
        return matchAllModuleNames(webservicesNames, moduleNames)
    }

    override fun getPlatformDescriptor(hybrisProjectDescriptor: HybrisProjectDescriptor): PlatformHybrisModuleDescriptor? {
        return hybrisProjectDescriptor.foundModules
            .firstNotNullOfOrNull { it as? PlatformHybrisModuleDescriptor }
    }

    override fun getActiveHacUrl(project: Project): String {
        return HybrisDeveloperSpecificProjectSettingsComponent
            .getInstance(project)
            .getActiveHacRemoteConnectionSettings(project)
            .let { getUrl(it) }
    }

    override fun getActiveSslProtocol(project: Project, settings: HybrisRemoteConnectionSettings?): String {
        var mySettings = settings
        if (mySettings == null) {
            mySettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
                .getActiveHacRemoteConnectionSettings(project)
        }
        return mySettings?.sslProtocol ?: HybrisConstants.DEFAULT_SSL_PROTOCOL
    }

    override fun getHostHacUrl(project: Project, settings: HybrisRemoteConnectionSettings?): String {
        var mySettings = settings
        if (mySettings == null) {
            mySettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
                .getActiveHacRemoteConnectionSettings(project)
        }
        return getUrl(mySettings)
    }

    override fun getSolrUrl(project: Project, settings: HybrisRemoteConnectionSettings?): String {
        var mySettings = settings
        val sb = StringBuilder()
        if (mySettings == null) {
            mySettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
                .getActiveSolrRemoteConnectionSettings(project)
        }
        if (mySettings!!.isSsl) {
            sb.append(HybrisConstants.HTTPS_PROTOCOL)
        } else {
            sb.append(HybrisConstants.HTTP_PROTOCOL)
        }
        sb.append(mySettings.hostIP)
        sb.append(":")
        sb.append(mySettings.port)
        sb.append("/")
        sb.append(mySettings.solrWebroot)
        val result = sb.toString()
        LOG.debug("Calculated host SOLR URL=$result")
        return result
    }

    private fun is2019plus(project: Project): Boolean {
        val hybrisVersion = HybrisProjectSettingsComponent.getInstance(project).state.hybrisVersion
        if (StringUtils.isBlank(hybrisVersion)) {
            return false
        }
        val projectVersion = Version.parseVersion(hybrisVersion)
        return projectVersion.compareTo(_1905) >= 0
    }

    override fun getBackofficeWebInfLib(project: Project): String {
        return if (is2019plus(project)) HybrisConstants.BACKOFFICE_WEB_INF_LIB_2019 else HybrisConstants.BACKOFFICE_WEB_INF_LIB
    }

    override fun getBackofficeWebInfClasses(project: Project): String {
        return if (is2019plus(project)) HybrisConstants.BACKOFFICE_WEB_INF_CLASSES_2019 else HybrisConstants.BACKOFFICE_WEB_INF_CLASSES
    }

    override fun fixRemoteConnectionSettings(project: Project) {
        val settingsComponent = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
        val state = settingsComponent.state
        val connectionList = state.remoteConnectionSettingsList
        connectionList.forEach(Consumer {
            prepareSslRemoteConnectionSettings(it)
        })

        if (settingsComponent.hacRemoteConnectionSettings.isEmpty()) {
            val newSettings = settingsComponent.getDefaultHacRemoteConnectionSettings(project)
            connectionList.add(newSettings)
            state.activeRemoteConnectionID = newSettings.uuid
        }

        if (settingsComponent.solrRemoteConnectionSettings.isEmpty()) {
            val newSettings = settingsComponent.getDefaultSolrRemoteConnectionSettings(project)
            connectionList.add(newSettings)
            state.activeSolrConnectionID = newSettings.uuid
        }
    }

    override fun refreshProjectSettings(project: Project) {
        HybrisProjectSettingsComponent.getInstance(project).registerCloudExtensions()
    }

    private fun prepareSslRemoteConnectionSettings(connectionSettings: HybrisRemoteConnectionSettings) {
        connectionSettings.isSsl = StringUtils.startsWith(connectionSettings.generatedURL, HybrisConstants.HTTPS_PROTOCOL)
        cleanUpRemoteConnectionSettingsHostIp(connectionSettings)
    }

    private fun cleanUpRemoteConnectionSettingsHostIp(connectionSettings: HybrisRemoteConnectionSettings) {
        connectionSettings.hostIP = connectionSettings.hostIP?.replace(regex, "")
    }

    /*private fun getLocalProperties(project: Project): Properties? {
        val configDir = HybrisProjectSettingsComponent.getInstance(project).state.configDirectory ?: return null
        val propFile = File(configDir, HybrisConstants.LOCAL_PROPERTIES)
        if (!propFile.exists()) {
            return null
        }
        val prop = Properties()
        try {
            FileReader(propFile).use { fr ->
                prop.load(fr)
                return prop
            }
        } catch (e: IOException) {
            LOG.info(e.message, e)
        }
        return null
    }*/

    private fun matchAllModuleNames(
        namePatterns: Collection<String>,
        moduleNames: Collection<String>
    ): Boolean {
        return namePatterns.stream()
            .allMatch { pattern: String -> matchModuleName(pattern, moduleNames) }
    }

    private fun matchModuleName(pattern: String, moduleNames: Collection<String>): Boolean {
        val regex = Regex("\\Q$pattern\\E".replace("*", "\\E.*\\Q"))
        return moduleNames.stream()
            .parallel()
            .anyMatch { p: String -> p.matches(regex) }
    }

    private fun getUrl(settings: HybrisRemoteConnectionSettings?): String {
        val ip = settings!!.hostIP
        val sb = StringBuilder()
        if (settings.isSsl) {
            sb.append(HybrisConstants.HTTPS_PROTOCOL)
        } else {
            sb.append(HybrisConstants.HTTP_PROTOCOL)
        }
        sb.append(ip)
        sb.append(HybrisConstants.URL_PORT_DELIMITER)
        sb.append(settings.port)
        val hac = settings.hacWebroot
        if (StringUtils.isNoneBlank(hac)) {
            sb.append('/')
            sb.append(StringUtils.strip(hac, " /"))
        }
        val result = sb.toString()
        LOG.debug("Calculated hostHacURL=$result")
        return result
    }

    companion object {
        private val LOG = Logger.getInstance(DefaultCommonIdeaService::class.java)
        private val _1905 = Version.parseVersion("1905.0")
    }
}
