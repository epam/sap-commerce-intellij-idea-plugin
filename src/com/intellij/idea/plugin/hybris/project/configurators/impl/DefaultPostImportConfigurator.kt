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

package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.project.configurators.*
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.MavenModuleDescriptor
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil

class DefaultPostImportConfigurator(val project: Project) : PostImportConfigurator {

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        allHybrisModules: List<ModuleDescriptor>,
        refresh: Boolean,
    ) {
        ReadAction
            .nonBlocking<List<() -> Unit>> {
                listOf(
                    KotlinCompilerConfigurator.getInstance()
                        ?.configureAfterImport(project)
                        ?: emptyList(),

                    DataSourcesConfigurator.getInstance()
                        ?.configureAfterImport(project)
                        ?: emptyList(),

                    AntConfigurator.getInstance()
                        ?.configureAfterImport(hybrisProjectDescriptor, allHybrisModules, project)
                        ?: emptyList()
                )
                    .flatten()
            }
            .finishOnUiThread(ModalityState.defaultModalityState()) { actions ->
                actions.forEach { it() }

                notifyImportFinished(project, refresh)
            }
            .inSmartMode(project)
            .submit(AppExecutorUtil.getAppExecutorService())

//        DumbService.getInstance(project).runWhenSmart {
//            finishImport(
//                project,
//                hybrisProjectDescriptor,
//                allHybrisModules
//            ) { notifyImportFinished(project, refresh) }
//        }
    }

    private fun finishImport(
        project: Project,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        allHybrisModules: List<ModuleDescriptor>,
        callback: Runnable
    ) {
        val configuratorFactory = ApplicationManager.getApplication().getService(ConfiguratorFactory::class.java)

        // invokeLater is needed to avoid a problem with transaction validation:
        // "Write-unsafe context!...", "Do not use API that changes roots from roots events..."
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater

            configuratorFactory.xsdSchemaConfigurator
                ?.configure(project, hybrisProjectDescriptor, allHybrisModules)

            JRebelConfigurator.getInstance()
                ?.configure(project, allHybrisModules)

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
        val notificationContent = if (refresh) message("hybris.notification.project.refresh.finished.content")
        else message("hybris.notification.project.import.finished.content")
        val notificationTitle = if (refresh) message("hybris.notification.project.refresh.title")
        else message("hybris.notification.project.import.title")

        Notifications.create(NotificationType.INFORMATION, notificationTitle, notificationContent)
            .notify(project)
        Notifications.showSystemNotificationIfNotActive(project, notificationContent, notificationTitle, notificationContent)
    }

    companion object {
        private val LOG = Logger.getInstance(DefaultPostImportConfigurator::class.java)
    }
}