/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.project.configurator

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.checkCanceled
import com.intellij.platform.util.progress.reportProgressScope
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

class ModulesConfigurator : ProjectImportConfigurator {

    private val logger = thisLogger()

    override val name: String
        get() = "Modules"

    override suspend fun configure(context: ProjectImportContext) {
        val moduleImportContexts = createModuleEntities(context)

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

    private suspend fun createModuleEntities(context: ProjectImportContext): Collection<ProjectModuleConfigurationContext<ModuleDescriptor>> {
        val chosenModuleDescriptors = context.allChosenModuleDescriptors
        val moduleProviders = ModuleProvider.EP.extensionList

        return reportProgressScope(chosenModuleDescriptors.size) { moduleReporter ->
            chosenModuleDescriptors.mapNotNull { moduleDescriptor ->
                val provider = moduleProviders.find { provider -> provider.isApplicable(moduleDescriptor) }
                if (provider == null) {
                    moduleReporter.itemStep("Skipping '${moduleDescriptor.name}' module...") {
                        logger.warn("Unable to find suitable module provider for '${moduleDescriptor}' module.")
                    }
                    return@mapNotNull null
                }

                return@mapNotNull moduleReporter.itemStep("Creating '${moduleDescriptor.name}' module...") {
                    checkCanceled()

                    logger.debug("Creating module [${moduleDescriptor.name}].")
                    val timedValue = measureTimedValue { provider.create(context, moduleDescriptor) }
                    val moduleEntity = timedValue.value
                    logger.info("Created module [${moduleDescriptor.name} | ${timedValue.duration}].")

                    context.mutableStorage.add(moduleDescriptor, moduleEntity)

                    return@itemStep ProjectModuleConfigurationContext(
                        importContext = context,
                        moduleDescriptor = moduleDescriptor,
                        moduleEntity = moduleEntity,
                        moduleTypeId = provider.moduleTypeId
                    )
                }
            }
        }
    }

    private suspend fun configureModule(context: ProjectModuleConfigurationContext<ModuleDescriptor>) {
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

}
