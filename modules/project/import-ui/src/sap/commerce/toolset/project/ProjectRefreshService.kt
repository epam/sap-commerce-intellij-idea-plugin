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

package sap.commerce.toolset.project

import com.intellij.ide.util.newProjectWizard.AddModuleWizard
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerProjectExtension
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.projectImport.ProjectImportProvider
import sap.commerce.toolset.directory
import sap.commerce.toolset.project.configurator.ProjectRefreshConfigurator
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.wizard.RefreshSupport

@Service(Service.Level.PROJECT)
class ProjectRefreshService(private val project: Project) {

    @Throws(ConfigurationException::class)
    fun refresh() {
        val projectDirectory = project.directory ?: return
        val provider = getHybrisProjectImportProvider() ?: return
        val compilerProjectExtension = CompilerProjectExtension.getInstance(project) ?: return
        val projectSettings = ProjectSettings.getInstance(project)

        removeOldProjectData()

        val wizard = object : AddModuleWizard(project, projectDirectory, provider) {
            override fun init() = Unit
        }

        wizard.wizardContext.also {
            it.projectJdk = ProjectRootManager.getInstance(project).projectSdk
            it.projectName = project.name
            it.compilerOutputDirectory = compilerProjectExtension.compilerOutputUrl
        }

        wizard.sequence.getAllSteps()
            .filterIsInstance<RefreshSupport>()
            .forEach { step -> step.refresh(projectSettings) }

        wizard.projectBuilder
            .commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
    }

    private fun removeOldProjectData() {
        val projectSettings = ProjectSettings.getInstance(project)
        val moduleModel = ModuleManager.getInstance(project).getModifiableModel()
        val libraryModel = LibraryTablesRegistrar.getInstance().getLibraryTable(project).modifiableModel
        val removeExternalModulesOnRefresh = projectSettings.removeExternalModulesOnRefresh

        moduleModel.modules
            .filter { removeExternalModulesOnRefresh || YFacet.get(it) != null }
            .forEach { moduleModel.disposeModule(it) }

        libraryModel.libraries.forEach { libraryModel.removeLibrary(it) }

        ApplicationManager.getApplication().runWriteAction {
            moduleModel.commit()
            libraryModel.commit()
        }

        ProjectRefreshConfigurator.EP.extensionList.forEach { it.beforeRefresh(project) }
    }

    private fun getHybrisProjectImportProvider() = ProjectImportProvider.PROJECT_IMPORT_PROVIDER.extensionsIfPointIsRegistered
        .filterIsInstance<HybrisProjectImportProvider>()
        .firstOrNull()

    companion object {
        fun getInstance(project: Project): ProjectRefreshService = project.service()
    }
}