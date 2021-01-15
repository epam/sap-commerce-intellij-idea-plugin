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

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.Version
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings
import java.lang.NumberFormatException
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import java.lang.StringBuilder
import com.intellij.idea.plugin.hybris.common.services.impl.DefaultCommonIdeaService
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettings
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.Validate
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Collectors
import kotlin.collections.ArrayList

/**
 * Created 10:24 PM 10 February 2016.
 *
 * @author Alexander Bartash <AlexanderBartash></AlexanderBartash>@gmail.com>
 */
class DefaultCommonIdeaService(commandProcessor: CommandProcessor) : CommonIdeaService {
    private val commandProcessor: CommandProcessor
    override fun isTypingActionInProgress(): Boolean {
        val isTyping = StringUtils.equalsAnyIgnoreCase(
                commandProcessor.currentCommandName, *HybrisConstants.TYPING_EDITOR_ACTIONS
        )
        val isUndoOrRedo = StringUtils.startsWithAny(
                commandProcessor.currentCommandName, *HybrisConstants.UNDO_REDO_EDITOR_ACTIONS
        )
        return isTyping || isUndoOrRedo
    }

    override fun getHybrisDirectory(project: Project): Optional<String> {
        return Optional.ofNullable(HybrisProjectSettingsComponent.getInstance(project))
                .map { it.state}
                .map { it.hybrisDirectory }
    }

    override fun getCustomDirectory(project: Project): Optional<String> {
        return Optional.ofNullable(HybrisProjectSettingsComponent.getInstance(project))
                .map { it.state }
                .map { it.customDirectory }
    }

    override fun isHybrisProject(project: Project): Boolean {
        return HybrisProjectSettingsComponent.getInstance(project).state.isHybrisProject
    }

    override fun isOutDatedHybrisProject(project: Project): Boolean {
        val hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project)
                .state
        val version = hybrisProjectSettings.importedByVersion ?: return true
        val versionParts = version.split("\\.").toTypedArray()
        if (versionParts.size < 2) {
            return true
        }
        val majorVersion = versionParts[0]
        val minorVersion = versionParts[1]
        return try {
            val majorVersionNumber = majorVersion.toInt()
            val minorVersionNumber = minorVersion.toInt()
            val versionNumber = majorVersionNumber * 100 + minorVersionNumber
            versionNumber < 900
        } catch (nfe: NumberFormatException) {
            true
        }
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
        val webservicesNames: Collection<String> = listOf("*hmc", "hmc", "platform")
        return matchAllModuleNames(webservicesNames, moduleNames)
    }

    override fun getPlatformDescriptor(hybrisProjectDescriptor: HybrisProjectDescriptor): PlatformHybrisModuleDescriptor {
        return hybrisProjectDescriptor.foundModules
                .first { e: HybrisModuleDescriptor? -> e is PlatformHybrisModuleDescriptor } as PlatformHybrisModuleDescriptor


    }

    override fun getActiveHacUrl(project: Project): String {
        return HybrisDeveloperSpecificProjectSettingsComponent
                .getInstance(project)
                .getActiveHybrisRemoteConnectionSettings(project)
                .let { getUrl(it) }
    }

    override fun getHostHacUrl(project: Project, settings: HybrisRemoteConnectionSettings?): String {
        var settings = settings
        if (settings == null) {
            settings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
                    .getActiveHybrisRemoteConnectionSettings(project)
        }
        return getUrl(settings)
    }

    override fun getSolrUrl(project: Project, settings: HybrisRemoteConnectionSettings?): String {
        var settings = settings
        val sb = StringBuilder()
        if (settings == null) {
            settings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
                    .getActiveSolrConnectionSettings(project)
        }
        if (settings!!.isSolrSsl) {
            sb.append(HybrisConstants.HTTPS_PROTOCOL)
        } else {
            sb.append(HybrisConstants.HTTP_PROTOCOL)
        }
        sb.append(settings.hostIP)
        sb.append(":")
        sb.append(settings.port)
        sb.append("/")
        sb.append(settings.solrWebroot)
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
        val developerSpecificSettings = HybrisDeveloperSpecificProjectSettingsComponent
                .getInstance(project)
        val state = developerSpecificSettings.state
        if (state != null) {
            val connectionList = state.remoteConnectionSettingsList
            connectionList.forEach(Consumer {
                if (it.type == null) {
                    it.type = HybrisRemoteConnectionSettings.Type.Hybris
                }
            })
            val remoteList = connectionList
                    .stream().filter { it.type == HybrisRemoteConnectionSettings.Type.Hybris }.collect(Collectors.toList())
            if (remoteList.isEmpty()) {
                val newSettings = developerSpecificSettings.getDefaultHybrisRemoteConnectionSettings(
                        project)
                connectionList.add(newSettings)
                state.activeRemoteConnectionID = newSettings.uuid
            }
            val solrList = connectionList
                    .stream().filter { it.type == HybrisRemoteConnectionSettings.Type.SOLR }.collect(Collectors.toList())
            if (solrList.isEmpty()) {
                val newSettings = developerSpecificSettings.getDefaultSolrRemoteConnectionSettings(
                        project)
                connectionList.add(newSettings)
                state.activeSolrConnectionID = newSettings.uuid
            }
        }
    }

    private fun getLocalProperties(project: Project): Properties? {
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
    }

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
        if (settings.isHacSsl) {
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

    init {
        Validate.notNull(commandProcessor)
        this.commandProcessor = commandProcessor
    }
}