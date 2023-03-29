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
import com.intellij.idea.plugin.hybris.project.configurators.PostImportConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsListener
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.removeUserData

private const val SYNC_PROJECT_SETTINGS = "hybrisProjectImportSyncProjectSettings"
private const val FINALIZE_PROJECT_IMPORT = "hybrisProjectImportFinalize"

class HybrisProjectImportStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (!HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()) return

        RunOnceUtil.runOnceForProject(project, "afterHybrisProjectImport") {
            HybrisToolWindowService.getInstance(project).activateToolWindow()

            project.getUserData(syncProjectSettingsKey)
                ?.let {
                    project.removeUserData(syncProjectSettingsKey)
                    syncProjectSettingsForProject(project)
                }
            project.getUserData(finalizeProjectImportKey)
                ?.let {
                    project.removeUserData(finalizeProjectImportKey)

                    PostImportConfigurator.getInstance(project).configure(
                        it.first,
                        it.second,
                        it.third
                    )
                }
        }

    }

    private fun syncProjectSettingsForProject(project: Project) {
        with (project.messageBus.syncPublisher(HybrisDeveloperSpecificProjectSettingsListener.TOPIC)) {
            hacConnectionSettingsChanged()
            solrConnectionSettingsChanged()
        }
    }


    companion object {
        val syncProjectSettingsKey: Key<Boolean> = Key.create(SYNC_PROJECT_SETTINGS);
        val finalizeProjectImportKey: Key<Triple<HybrisProjectDescriptor, List<HybrisModuleDescriptor>, Boolean>> = Key.create(FINALIZE_PROJECT_IMPORT);
    }
}