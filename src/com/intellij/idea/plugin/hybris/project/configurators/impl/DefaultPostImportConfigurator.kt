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

package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.project.configurators.PostImportConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.MavenModuleDescriptor
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

class DefaultPostImportConfigurator(val project: Project) : PostImportConfigurator {

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        allHybrisModules: List<HybrisModuleDescriptor>,
        refresh: Boolean,
    ) {
        DumbService.getInstance(project).runWhenSmart {
            finishImport(
                project,
                hybrisProjectDescriptor,
                allHybrisModules
            ) { notifyImportFinished(project, refresh) }
        }
    }

    private fun finishImport(
        project: Project,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        allHybrisModules: List<HybrisModuleDescriptor>,
        callback: Runnable
    ) {
        val configuratorFactory = ConfiguratorFactoryProvider.get()

        try {
            if (PluginCommon.isPluginActive(PluginCommon.ANT_SUPPORT_PLUGIN_ID)) {
                configuratorFactory.antConfigurator
                    ?.configure(hybrisProjectDescriptor, allHybrisModules, project)
            }
        } catch (e: Exception) {
            LOG.error("Can not configure Ant due to an error.", e)
        }
        try {
            configuratorFactory.dataSourcesConfigurator
                ?.configure(project)
        } catch (e: Exception) {
            LOG.error("Can not import data sources due to an error.", e)
        }
        // invokeLater is needed to avoid a problem with transaction validation:
        // "Write-unsafe context!...", "Do not use API that changes roots from roots events..."
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater

            configuratorFactory.kotlinCompilerConfigurator
                ?.configureAfterImport(project)

            configuratorFactory.mavenConfigurator
                ?.let {
                    try {
                        val mavenModules = hybrisProjectDescriptor.modulesChosenForImport
                            .filterIsInstance<MavenModuleDescriptor>()
                        if (mavenModules.isNotEmpty()) {
                            it.configure(hybrisProjectDescriptor, project, mavenModules, configuratorFactory)
                        }
                    } catch (e: Exception) {
                        LOG.error("Can not import Maven modules due to an error.", e)
                    } finally {
                        callback.run()
                    }
                } ?: callback.run()
        }
    }

    private fun notifyImportFinished(project: Project, refresh: Boolean) {
        val notificationContent = if (refresh) HybrisI18NBundleUtils.message("hybris.notification.project.refresh.finished.content")
        else HybrisI18NBundleUtils.message("hybris.notification.project.import.finished.content")
        val notificationTitle = if (refresh) HybrisI18NBundleUtils.message("hybris.notification.project.refresh.title")
        else HybrisI18NBundleUtils.message("hybris.notification.project.import.title")

        Notifications.create(NotificationType.INFORMATION, notificationTitle, notificationContent)
            .notify(project)
        Notifications.showSystemNotificationIfNotActive(project, notificationContent, notificationTitle, notificationContent)
    }

    companion object {
        private val LOG = Logger.getInstance(DefaultPostImportConfigurator::class.java)
    }
}