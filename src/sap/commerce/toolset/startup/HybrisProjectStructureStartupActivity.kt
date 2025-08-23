/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.startup

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.actionSystem.ProjectRefreshAction
import sap.commerce.toolset.project.configurator.ProjectStartupConfigurator
import sap.commerce.toolset.project.settings.ProjectSettings

class HybrisProjectStructureStartupActivity : ProjectActivity {

    private val logger = Logger.getInstance(HybrisProjectStructureStartupActivity::class.java)

    override suspend fun execute(project: Project) {
        if (project.isDisposed) return

        val settingsComponent = ProjectSettings.getInstance(project)
        val isHybrisProject = project.isHybrisProject

        if (!isHybrisProject) return

        if (settingsComponent.isOutdatedHybrisProject()) {
            Notifications.create(
                NotificationType.INFORMATION,
                i18n("hybris.notification.project.open.outdated.title"),
                i18n(
                    "hybris.notification.project.open.outdated.text",
                    settingsComponent.importedByVersion ?: "old"
                )
            )
                .important(true)
                .addAction(i18n("hybris.notification.project.open.outdated.action")) { _, _ -> ProjectRefreshAction.triggerAction() }
                .notify(project)
        }

        logVersion(project)
        continueOpening(project)
    }

    private fun logVersion(project: Project) {
        val settings = ProjectSettings.getInstance(project)
        val importedBy = settings.importedByVersion
        val hybrisVersion = settings.hybrisVersion
        val plugin = Plugin.HYBRIS.pluginDescriptor ?: return
        val pluginVersion = plugin.version
        logger.info("Opening hybris version $hybrisVersion which was imported by $importedBy. Current plugin is $pluginVersion")
    }

    private fun continueOpening(project: Project) {
        if (project.isDisposed) return

        ProjectStartupConfigurator.EP.extensionList.forEach { it.onStartup(project) }
    }

}