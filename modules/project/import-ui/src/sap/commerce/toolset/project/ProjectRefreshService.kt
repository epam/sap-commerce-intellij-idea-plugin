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
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerProjectExtension
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.Disposer
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.projectImport.ProjectImportProvider
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.configurator.ProjectRefreshConfigurator
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.project.wizard.RefreshSupport
import kotlin.io.path.absolutePathString

@Service(Service.Level.PROJECT)
class ProjectRefreshService(private val project: Project) {

    @Throws(HybrisConfigurationException::class)
    fun refresh(refreshContext: ProjectRefreshContext) {
        val project = refreshContext.project
        val projectDirectory = refreshContext.projectPath.absolutePathString()
        val provider = getHybrisProjectImportProvider() ?: return
        val compilerProjectExtension = CompilerProjectExtension.getInstance(project) ?: return

        runWithModalProgressBlocking(
            owner = ModalTaskOwner.guess(),
            title = "Before Refresh Configurators",
        ) {
            ProjectRefreshConfigurator.EP.extensionList.forEach { it.configure(refreshContext) }
        }

        val wizard = object : AddModuleWizard(project, projectDirectory, provider) {
            override fun init() = Unit
        }

        try {
            doRefresh(project, wizard, refreshContext, compilerProjectExtension)
        } finally {
            Disposer.dispose(wizard.disposable)
        }
    }

    private fun doRefresh(
        project: Project,
        wizard: AddModuleWizard,
        refreshContext: ProjectRefreshContext,
        compilerProjectExtension: CompilerProjectExtension
    ) {
        wizard.wizardContext.also {
            it.projectJdk = ProjectRootManager.getInstance(project).projectSdk
            it.projectName = project.name
            it.compilerOutputDirectory = compilerProjectExtension.compilerOutputUrl
        }

        wizard.sequence.allSteps
            .filterIsInstance<RefreshSupport>()
            .forEach { step -> step.refresh(refreshContext) }

        wizard.projectBuilder.commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER)
        wizard.projectBuilder.cleanup()
    }

    private fun getHybrisProjectImportProvider() = ProjectImportProvider.PROJECT_IMPORT_PROVIDER.extensionsIfPointIsRegistered
        .filterIsInstance<HybrisProjectImportProvider>()
        .firstOrNull()

    companion object {
        fun getInstance(project: Project): ProjectRefreshService = project.service()
    }
}