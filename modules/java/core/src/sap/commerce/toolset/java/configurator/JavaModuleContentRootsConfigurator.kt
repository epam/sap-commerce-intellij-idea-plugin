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

package sap.commerce.toolset.java.configurator

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.util.progress.reportProgressScope
import sap.commerce.toolset.java.configurator.contentEntry.ModuleContentEntryConfigurator
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.io.path.Path
import kotlin.time.measureTime

class JavaModuleContentRootsConfigurator : ModuleImportConfigurator {

    private val logger = thisLogger()
    private val rootsToIgnore = mapOf(
        "acceleratorstorefrontcommons" to listOf(Path("commonweb", "testsrc"))
    )

    override val name: String
        get() = "Content Roots"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        module: Module,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val moduleRootPath = moduleDescriptor.moduleRootPath
        val contentEntry = VfsUtil.findFile(moduleRootPath, true)
            ?.let { modifiableModelsProvider.getModifiableRootModel(module).addContentEntry(it) }
            ?: return

        val pathsToIgnore = rootsToIgnore[module.name]
            ?.map { moduleRootPath.resolve(it) }
            ?: emptyList()

        val configurators = ModuleContentEntryConfigurator.EP.extensionList
            .filter { configurator -> configurator.isApplicable(importContext, moduleDescriptor) }

        reportProgressScope(configurators.size) { reporter ->
            configurators.forEach { configurator ->
                reporter.itemStep("Applying content root '${configurator.name}' configurator...") {
                    checkCanceled()

                    val duration = measureTime { configurator.configure(importContext, moduleDescriptor, contentEntry, pathsToIgnore) }
                    logger.info("Content root configurator [${moduleDescriptor.name} | ${configurator.name} | $duration]")
                }
            }
        }
    }
}
