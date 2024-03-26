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

package com.intellij.idea.plugin.hybris.startup

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.project.actions.ProjectRefreshAction
import com.intellij.idea.plugin.hybris.project.configurators.ConfiguratorFactory
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class HybrisProjectStructureStartupActivity : ProjectActivity {

    private val logger = Logger.getInstance(HybrisProjectStructureStartupActivity::class.java)

    override suspend fun execute(project: Project) {
        if (project.isDisposed) return

        val commonIdeaService = CommonIdeaService.getInstance()
        val settingsComponent = ProjectSettingsComponent.getInstance(project)
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
        val settings = ProjectSettingsComponent.getInstance(project).state
        val importedBy = settings.importedByVersion
        val hybrisVersion = settings.hybrisVersion
        val plugin = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID)) ?: return
        val pluginVersion = plugin.version
        logger.info("Opening hybris version $hybrisVersion which was imported by $importedBy. Current plugin is $pluginVersion")
    }

    private fun continueOpening(project: Project) {
        if (project.isDisposed) return

        with(ConfiguratorFactory.getInstance()) {
            getSpringConfigurator()
                .resetSpringGeneralSettings(project)
            getJRebelConfigurator()
                ?.fixBackOfficeJRebelSupport(project)
        }
    }

}