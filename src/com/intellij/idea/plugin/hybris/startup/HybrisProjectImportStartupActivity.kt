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

import com.intellij.ide.util.RunOnceUtil
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.project.configurators.ConfiguratorFactory
import com.intellij.idea.plugin.hybris.project.configurators.impl.ConfiguratorFactoryProvider
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.MavenModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsListener
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.IdeaProjectSettingsService
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindowManager

private const val OPEN_SETTINGS_DIALOG = "hybrisProjectImportOpenSettingsDialog"
private const val SYNC_PROJECT_SETTINGS = "hybrisProjectImportSyncProjectSettings"
private const val FINALIZE_PROJECT_IMPORT = "hybrisProjectImportFinalize"

class HybrisProjectImportStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (!HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()) return

        RunOnceUtil.runOnceForProject(project, "afterHybrisProjectImport") {

            project.getUserData(openSettingsKey)
                ?.let { openSettingsForProject(project) }
            project.getUserData(syncProjectSettingsKey)
                ?.let { syncProjectSettingsForProject(project) }

            project.getUserData(finalizeProjectImportKey)
                ?.let {
                    activateToolWindow(project)

                    DumbService.getInstance(project).runWhenSmart {
                        finishImport(
                            project,
                            it.first,
                            it.second,
                            ConfiguratorFactoryProvider.get(),
                            Runnable { notifyImportFinished(project, it.third) }
                        )
                    }
                }
        }
    }

    private fun activateToolWindow(project: Project) = ToolWindowManager.getInstance(project).getToolWindow(HybrisToolWindowFactory.ID)
        ?.let {
            ApplicationManager.getApplication().invokeLater {
                it.isAvailable = true
                it.activate(null, true)
            }
        }

    // ensure the dialog is shown after all startup activities are done
    private fun openSettingsForProject(project: Project) = ApplicationManager.getApplication().invokeLater({
        IdeaProjectSettingsService.getInstance(project).openProjectSettings()
    }, ModalityState.NON_MODAL, project.disposed)

    private fun syncProjectSettingsForProject(project: Project) {
        project.messageBus.syncPublisher(HybrisDeveloperSpecificProjectSettingsListener.TOPIC).hacConnectionSettingsChanged()
        project.messageBus.syncPublisher(HybrisDeveloperSpecificProjectSettingsListener.TOPIC).solrConnectionSettingsChanged()
    }

    private fun finishImport(
        project: Project,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        allHybrisModules: List<HybrisModuleDescriptor>,
        configuratorFactory: ConfiguratorFactory,
        callback: Runnable
    ) {
        try {
            configuratorFactory.antConfigurator
                ?.configure(hybrisProjectDescriptor, allHybrisModules, project)
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
                }
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
        private val LOG = Logger.getInstance(HybrisProjectImportStartupActivity::class.java)
        val openSettingsKey: Key<Boolean> = Key.create(OPEN_SETTINGS_DIALOG);
        val syncProjectSettingsKey: Key<Boolean> = Key.create(SYNC_PROJECT_SETTINGS);
        val finalizeProjectImportKey: Key<Triple<HybrisProjectDescriptor, List<HybrisModuleDescriptor>, Boolean>> = Key.create(FINALIZE_PROJECT_IMPORT);
    }
}