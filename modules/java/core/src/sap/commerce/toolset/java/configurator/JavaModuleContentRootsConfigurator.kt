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
import com.intellij.openapi.progress.checkCanceled
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleContentRootEntryConfigurator
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.time.measureTime

class JavaModuleContentRootsConfigurator : ModuleImportConfigurator {

    private val logger = thisLogger()
    private val rootsToIgnore = mapOf(
        "acceleratorstorefrontcommons" to listOf(Path("commonweb", "testsrc"))
    )

    override val name: String
        get() = "Content Roots"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(context: ProjectModuleConfigurationContext<ModuleDescriptor>) {
        val importContext = context.importContext
        val moduleDescriptor = context.moduleDescriptor
        val moduleEntity = context.moduleEntity
        val moduleRootPath = moduleDescriptor.moduleRootPath
        val virtualFileUrlManager = importContext.workspace.getVirtualFileUrlManager()
        val contentRootUrl = virtualFileUrlManager.fromPath(moduleDescriptor.moduleRootPath.pathString)

        val pathsToIgnore = rootsToIgnore[moduleEntity.name]
            ?.map { moduleRootPath.resolve(it) }
            ?: emptyList()

        val configurators = ModuleContentRootEntryConfigurator.EP.extensionList
            .filter { configurator -> configurator.isApplicable(importContext, moduleDescriptor) }

        val contentRootEntity = ContentRootEntity(
            contentRootUrl,
            emptyList(),
            moduleEntity.entitySource
        )
        moduleEntity.contentRoots += contentRootEntity

        reportProgressScope(configurators.size) { reporter ->
            configurators.forEach { configurator ->
                reporter.itemStep("Applying content root '${configurator.name}' configurator...") {
                    checkCanceled()

                    val duration = measureTime { configurator.configure(context, contentRootEntity, pathsToIgnore) }
                    logger.debug("Content root configurator [${moduleDescriptor.name} | ${configurator.name} | $duration]")
                }
            }
        }
    }
}
