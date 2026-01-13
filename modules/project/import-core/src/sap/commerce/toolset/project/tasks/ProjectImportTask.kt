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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.util.progress.reportSequentialProgress
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.configurator.*
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@Service(Service.Level.PROJECT)
class ProjectImportTask(private val project: Project) {

    private val logger = thisLogger()

    fun execute(importContext: ProjectImportContext) = runWithModalProgressBlocking(
        owner = ModalTaskOwner.guess(),
        title = if (importContext.refresh) i18n("hybris.project.refresh.commit")
        else i18n("hybris.project.import.commit"),
    ) {
        val workspaceModel = WorkspaceModel.getInstance(project)

        reportSequentialProgress { spr ->
            spr.nextStep(5) { preConfigureProject(importContext, workspaceModel) }
            spr.nextStep(90) { importModules(importContext, workspaceModel) }
            spr.nextStep(95) { saveWorkspaceModel(importContext, workspaceModel) }
            spr.nextStep(100) { configureProject(importContext, workspaceModel) }

            project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, true)
        }
    }

    private suspend fun preConfigureProject(importContext: ProjectImportContext, workspaceModel: WorkspaceModel) {
        val configurators = ProjectPreImportConfigurator.EP.extensionList

        reportProgressScope(configurators.size) { reporter ->
            configurators.forEach { configurator ->
                reporter.itemStep("Configuring project using '${configurator.name}' configurator...") {
                    checkCanceled()

                    val duration = measureTime { configurator.preConfigure(importContext, workspaceModel) }
                    logger.info("Pre-configured project [${configurator.name} | $duration]")
                }
            }
        }
    }

    private suspend fun importModules(importContext: ProjectImportContext, workspaceModel: WorkspaceModel) {
        val chosenModuleDescriptors = importContext.allChosenModuleDescriptors
        val moduleProviders = ModuleProvider.EP.extensionList

        val moduleImportContexts = createModuleEntities(chosenModuleDescriptors, moduleProviders, importContext, workspaceModel)

        reportProgressScope(moduleImportContexts.size) { moduleReporter ->
            moduleImportContexts.forEach { context ->
                moduleReporter.itemStep("Importing '${context.moduleDescriptor.name}' module...") {
                    checkCanceled()

                    logger.debug("Configuring module [${context.moduleDescriptor.name}].")
                    val duration = measureTime { configureModule(context) }
                    logger.info("Configured module [${context.moduleDescriptor.name} | ${duration}].")
                }
            }
        }
    }

    private suspend fun createModuleEntities(
        chosenModuleDescriptors: List<ModuleDescriptor>,
        moduleProviders: List<ModuleProvider>,
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ): List<ProjectModuleConfigurationContext> = reportProgressScope(chosenModuleDescriptors.size) { moduleReporter ->
        chosenModuleDescriptors.mapNotNull { moduleDescriptor ->
            val provider = moduleProviders.find { provider -> provider.isApplicable(moduleDescriptor) }
            if (provider == null) {
                moduleReporter.itemStep("Skipping '${moduleDescriptor.name}' module...") {
                    logger.warn("Unable to find suitable module provider for '${moduleDescriptor}' module...")
                }
                return@mapNotNull null
            }

            return@mapNotNull moduleReporter.itemStep("Creating '${moduleDescriptor.name}' module...") {
                checkCanceled()

                logger.debug("Creating module [${moduleDescriptor.name}].")
                val timedValue = measureTimedValue { provider.getOrCreate(importContext, workspaceModel, moduleDescriptor) }
                logger.info("Created module [${moduleDescriptor.name} | ${timedValue.duration}].")

                return@itemStep ProjectModuleConfigurationContext(
                    importContext = importContext,
                    workspaceModel = workspaceModel,
                    moduleDescriptor = moduleDescriptor,
                    moduleEntity = timedValue.value,
                    moduleTypeId = provider.moduleTypeId
                )
            }
        }
    }

    private suspend fun configureModule(context: ProjectModuleConfigurationContext) {
        val configurators = ModuleImportConfigurator.EP.extensionList
            .filter { it.isApplicable(context.moduleTypeId) }

        reportProgressScope(configurators.size) { moduleConfiguratorReporter ->
            configurators.forEach { configurator ->
                moduleConfiguratorReporter.itemStep("Applying '${configurator.name} configurator...") {
                    checkCanceled()

                    runCatching {
                        val duration = measureTime { configurator.configure(context) }
                        logger.info("Applied module configurator [${context.moduleDescriptor.name} | ${configurator.name} | ${duration}].")
                    }
                        .exceptionOrNull()
                        ?.let { logger.warn("Module configurator '${configurator.name}' error: ${it.message}", it) }
                }
            }
        }
    }

    private suspend fun saveWorkspaceModel(importContext: ProjectImportContext, workspaceModel: WorkspaceModel) {
        workspaceModel.update("Saving Workspace Model") { storage ->
            ProjectStorageConfigurator.EP.extensionList.forEach { configurator ->
                logger.info("Saving workspace using ${configurator.name} configurator...")
                configurator.configure(importContext, storage)
            }
        }

        importContext.mutableStorage.clear()
    }

    private suspend fun configureProject(importContext: ProjectImportContext, workspaceModel: WorkspaceModel) {
        val configurators = ProjectImportConfigurator.EP.extensionList

        reportProgressScope(configurators.size) {
            configurators.forEach { configurator ->
                it.itemStep("Applying '${configurator.name}' configurator...") {
                    checkCanceled()

                    val duration = measureTime { configurator.configure(importContext, workspaceModel) }

                    logger.info("Applied project configurator [${configurator.name} | ${duration}].")
                }
            }
        }
    }

    companion object {
        fun getInstance(project: Project): ProjectImportTask = project.service()
    }
}
