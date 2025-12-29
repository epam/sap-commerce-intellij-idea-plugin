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

package sap.commerce.toolset.project.tasks

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.util.progress.reportSequentialProgress
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.configurator.ModuleProvider
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectPreImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor

@Service(Service.Level.PROJECT)
class ImportProjectTask(private val project: Project) {

    fun import(importContext: ProjectImportContext) = runWithModalProgressBlocking(
        owner = ModalTaskOwner.guess(),
        title = i18n("hybris.project.import.commit"),
    ) {
        val modifiableModelsProvider = IdeModifiableModelsProviderImpl(project)

        reportSequentialProgress { spr ->
            spr.nextStep(5) { preConfigureProject(importContext) }
            spr.nextStep(95) { importModules(importContext, modifiableModelsProvider) }
            spr.nextStep(100) { configureProject(importContext, modifiableModelsProvider) }

            spr.indeterminateStep(i18n("hybris.project.import.saving.project"))

            saveProject(modifiableModelsProvider)
        }
    }

    private suspend fun preConfigureProject(importContext: ProjectImportContext) {
        val configurators = ProjectPreImportConfigurator.EP.extensionList

        reportProgressScope(configurators.size) { reporter ->
            configurators.forEach { configurator ->
                reporter.itemStep("Configuring project using '${configurator.name}' configurator...") {
                    checkCanceled()

                    configurator.preConfigure(importContext)
                }
            }
        }
    }

    private suspend fun importModules(importContext: ProjectImportContext, modifiableModelsProvider: IdeModifiableModelsProviderImpl) {
        val chosenModuleDescriptors = importContext.allChosenModuleDescriptors
        val moduleProviders = ModuleProvider.EP.extensionList

        reportProgressScope(chosenModuleDescriptors.size) { moduleReporter ->
            chosenModuleDescriptors.forEach { moduleDescriptor ->
                val provider = moduleProviders.find { provider -> provider.isApplicable(moduleDescriptor) }
                    ?: return@forEach moduleReporter.itemStep("Skipping '${moduleDescriptor.name}' module...") {
                        thisLogger().warn("Unable to find suitable module provider for '${moduleDescriptor}' module...")
                    }

                moduleReporter.itemStep("Importing '${moduleDescriptor.name}' module...") {
                    checkCanceled()

                    importModule(importContext, moduleDescriptor, provider, modifiableModelsProvider)
                }
            }
        }
    }

    private suspend fun importModule(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        provider: ModuleProvider,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val module = provider.create(importContext, moduleDescriptor, modifiableModelsProvider)
        val configurators = ModuleImportConfigurator.EP.extensionList
            .filter { it.isApplicable(provider.moduleTypeId) }

        reportProgressScope(configurators.size) { moduleConfiguratorReporter ->
            configurators.forEach { configurator ->
                moduleConfiguratorReporter.itemStep("Applying '${configurator.name} configurator...") {
                    checkCanceled()

                    configurator.configure(importContext, moduleDescriptor, module, modifiableModelsProvider)
                }
            }
        }
    }

    private suspend fun configureProject(importContext: ProjectImportContext, modifiableModelsProvider: IdeModifiableModelsProvider) {
        val configurators = ProjectImportConfigurator.EP.extensionList

        reportProgressScope(configurators.size) {
            configurators.forEach { configurator ->
                it.itemStep("Applying '${configurator.name}' configurator...") {
                    checkCanceled()

                    configurator.configure(importContext, modifiableModelsProvider)
                }
            }
        }
    }

    private suspend fun saveProject(modifiableModelsProvider: IdeModifiableModelsProvider) {
        edtWriteAction { modifiableModelsProvider.commit() }

        project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, true)
    }

    companion object {
        fun getInstance(project: Project): ImportProjectTask = project.service()
    }
}
