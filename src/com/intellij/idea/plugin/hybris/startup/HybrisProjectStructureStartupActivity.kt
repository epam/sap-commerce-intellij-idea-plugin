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

package com.intellij.idea.plugin.hybris.startup

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.project.actions.ProjectRefreshAction
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.ConsolePersistenceService
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.spring.settings.SpringGeneralSettings
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.Validate
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class HybrisProjectStructureStartupActivity : ProjectActivity {

    private val logger = Logger.getInstance(HybrisProjectStructureStartupActivity::class.java)

    override suspend fun execute(project: Project) {
        if (project.isDisposed) return

        val commonIdeaService = ApplicationManager.getApplication().getService(CommonIdeaService::class.java)
        val settingsComponent = HybrisProjectSettingsComponent.getInstance(project)
        val isHybrisProject = settingsComponent.isHybrisProject()

        if (isHybrisProject) {
            settingsComponent.registerCloudExtensions()

            if (settingsComponent.isOutdatedHybrisProject()) {
                Notifications.create(
                    NotificationType.INFORMATION,
                    HybrisI18NBundleUtils.message("hybris.notification.project.open.outdated.title"),
                    HybrisI18NBundleUtils.message(
                        "hybris.notification.project.open.outdated.text",
                        settingsComponent.state.importedByVersion ?: "old"
                    )
                )
                    .important(true)
                    .addAction(HybrisI18NBundleUtils.message("hybris.notification.project.open.outdated.action")) { _, _ -> ProjectRefreshAction.triggerAction() }
                    .notify(project)
            }
        } else if (commonIdeaService.isPotentiallyHybrisProject(project)) {
            Notifications.create(
                NotificationType.INFORMATION,
                HybrisI18NBundleUtils.message("hybris.notification.project.open.potential.title"),
                HybrisI18NBundleUtils.message("hybris.notification.project.open.potential.text")
            )
                .important(true)
                .notify(project)
        }

        if (!isHybrisProject) return

        logVersion(project)
        continueOpening(project)
    }

    private fun logVersion(project: Project) {
        val settings = HybrisProjectSettingsComponent.getInstance(project).state
        val importedBy = settings.importedByVersion
        val hybrisVersion = settings.hybrisVersion
        val plugin = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID)) ?: return
        val pluginVersion = plugin.version
        logger.info("Opening hybris version $hybrisVersion which was imported by $importedBy. Current plugin is $pluginVersion")
    }

    private fun continueOpening(project: Project) {
        if (project.isDisposed) return

        resetSpringGeneralSettings(project)
        fixBackOfficeJRebelSupport(project)

        CommonIdeaService.instance.fixRemoteConnectionSettings(project)
        ConsolePersistenceService.getInstance(project).loadPersistedQueries()
    }

    private fun resetSpringGeneralSettings(project: Project) {
        if (HybrisProjectSettingsComponent.getInstance(project).isHybrisProject() && PluginCommon.isPluginActive(PluginCommon.SPRING_PLUGIN_ID)) {
            val springGeneralSettings = SpringGeneralSettings.getInstance(project)
            springGeneralSettings.isShowMultipleContextsPanel = false
            springGeneralSettings.isShowProfilesPanel = false
        }
    }

    private fun fixBackOfficeJRebelSupport(project: Project) {
        Validate.notNull(project)
        val jRebelPlugin = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.JREBEL_PLUGIN_ID))

        if (jRebelPlugin == null || !jRebelPlugin.isEnabled) return

        val hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project).state
        val compilingXml = File(
            FileUtilRt.toSystemDependentName(
                project.basePath + "/" + hybrisProjectSettings.hybrisDirectory
                    + HybrisConstants.PLATFORM_MODULE_PREFIX + HybrisConstants.ANT_COMPILING_XML
            )
        )
        if (!compilingXml.isFile) return

        var content = try {
            IOUtils.toString(FileInputStream(compilingXml), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            logger.error(e)
            return
        }
        if (!content.contains("excludes=\"**/rebel.xml\"")) {
            return
        }
        content = content.replace("excludes=\"**/rebel.xml\"", "")
        try {
            IOUtils.write(content, FileOutputStream(compilingXml), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            logger.error(e)
        }
    }

}