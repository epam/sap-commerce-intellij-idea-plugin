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

package sap.commerce.toolset.project.refresh

import com.intellij.ide.util.newProjectWizard.AddModuleWizard
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerProjectExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.projectImport.ProjectImportProvider
import com.intellij.util.asSafely
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.HybrisProjectImportProvider
import sap.commerce.toolset.project.configurator.ProjectRefreshConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.project.factories.ModuleDescriptorFactory
import sap.commerce.toolset.project.wizard.RefreshSupport
import kotlin.io.path.absolutePathString

@Service(Service.Level.PROJECT)
class ProjectRefreshService(private val project: Project) {

    @Throws(ConfigurationException::class)
    fun refresh(refreshContext: ProjectRefreshContext) {
        val project = refreshContext.project
        val projectDirectory = refreshContext.projectPath.absolutePathString()
        val provider = getHybrisProjectImportProvider() ?: return
        val compilerProjectExtension = CompilerProjectExtension.getInstance(project) ?: return

        ProjectRefreshConfigurator.EP.extensionList.forEach { it.beforeRefresh(refreshContext) }

        val wizard = object : AddModuleWizard(project, projectDirectory, provider) {
            override fun init() = Unit
        }

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

    fun openModuleDescriptors(importContext: ProjectImportContext): List<ModuleDescriptor> = ModuleManager.getInstance(project).modules
        .filter { module -> YFacet.getState(module)?.subModuleType == null }
        .mapNotNull { module ->
            ModuleRootManager.getInstance(module).contentRoots
                .firstOrNull()
                ?.let { VfsUtil.virtualToIoFile(it) }
                ?.let {
                    try {
                        ModuleDescriptorFactory.createDescriptor(it, importContext)
                    } catch (e: HybrisConfigurationException) {
                        thisLogger().error(e)
                        return@let null
                    }
                }
        }
        .flatMap { moduleDescriptor ->
            buildList {
                add(moduleDescriptor)
                moduleDescriptor.asSafely<YModuleDescriptor>()
                    ?.let {
                        addAll(it.getSubModules())
                    }
            }
        }

    private fun getHybrisProjectImportProvider() = ProjectImportProvider.PROJECT_IMPORT_PROVIDER.extensionsIfPointIsRegistered
        .filterIsInstance<HybrisProjectImportProvider>()
        .firstOrNull()

    companion object {
        fun getInstance(project: Project): ProjectRefreshService = project.service()
    }
}