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
package com.intellij.idea.plugin.hybris.project

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.IdeaProjectSettingsService
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.startup.StartupManager
import com.intellij.packaging.artifacts.ModifiableArtifactModel

class OpenHybrisProjectImportBuilder : DefaultHybrisProjectImportBuilder() {

    override fun commit(
        project: Project,
        model: ModifiableModuleModel?,
        modulesProvider: ModulesProvider?,
        artifactModel: ModifiableArtifactModel?
    ): MutableList<Module>? {
        if (isOpenProjectSettingsAfter) {
            StartupManager.getInstance(project).runAfterOpened {
                // ensure the dialog is shown after all startup activities are done
                ApplicationManager.getApplication().invokeLater({
                    IdeaProjectSettingsService.getInstance(project).openProjectSettings()
                }, ModalityState.NON_MODAL, project.disposed)
            }
        }
        getHybrisProjectDescriptor().setHybrisProject(project)

        return super.commit(project, model, modulesProvider, artifactModel)
    }

}