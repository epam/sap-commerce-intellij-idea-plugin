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
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.workspace.jps.entities.*
import sap.commerce.toolset.java.configurator.library.ModuleLibraryConfigurator
import sap.commerce.toolset.java.configurator.library.collectLibraryDescriptors
import sap.commerce.toolset.java.descriptor.JavaLibraryDescriptor
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.time.measureTime

class JavaModuleLibrariesConfigurator : ModuleImportConfigurator {

    private val logger = thisLogger()

    override val name: String
        get() = "Library Roots"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val configurators = ModuleLibraryConfigurator.EP.extensionList
            .filter { configurator -> configurator.isApplicable(importContext, moduleDescriptor) }

        workspaceModel.update("Remove existing libraries for module ${moduleEntity.name}") { storage ->
            storage.modifyModuleEntity(moduleEntity) {
                this.dependencies.removeIf { it is LibraryDependency }
            }
        }

        reportProgressScope(configurators.size) { reporter ->
            configurators.forEach { configurator ->
                reporter.itemStep("Applying library '${configurator.name}' configurator...") {
                    checkCanceled()

                    val duration = measureTime { configurator.configure(importContext, workspaceModel, moduleDescriptor, moduleEntity) }
                    logger.info("Library configurator [${moduleDescriptor.name} | ${configurator.name} | $duration]")
                }
            }
        }

        for (javaLibraryDescriptor in moduleDescriptor.collectLibraryDescriptors(importContext, workspaceModel)) {
            val noValidClassesPaths = javaLibraryDescriptor.libraryRoots.none { it.type == LibraryRootTypeId.COMPILED }

            if (noValidClassesPaths) {
                thisLogger().debug("Library paths with CLASSES root type are not found: ${moduleDescriptor.name} | ${javaLibraryDescriptor.name}")
                continue
            }

            createLibrary(importContext, workspaceModel, moduleEntity, javaLibraryDescriptor)
        }
    }

    private suspend fun createLibrary(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleEntity: ModuleEntity,
        javaLibraryDescriptor: JavaLibraryDescriptor
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val moduleId = ModuleId(moduleEntity.name)
        val libraryTableId = LibraryTableId.ModuleLibraryTableId(moduleId)
        val libraryId = LibraryId(javaLibraryDescriptor.name, libraryTableId)

        val libraryRoots = buildList {
            javaLibraryDescriptor.libraryRoots
                .forEach { add(it) }

            // applicable only for non-custom modules, to be respected with in custom Configurator
//            val sourceCodeFile = importContext.sourceCodeFile
//            sourceCodeFile
//                ?.takeIf { it.fileExists }
//                ?.takeIf { javaLibraryDescriptor.libraryPaths.any { it.path.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) } }
//                ?.let { VfsUtil.findFile(it, true) }
//                ?.let {
//                    LibraryRoot(
//                        url = virtualFileUrlManager.fromPath(sourceCodeFile.pathString),
//                        type = LibraryRootTypeId.SOURCES,
//                    )
//                }
//                ?.also { add(it) }
        }

        workspaceModel.update("Update library ${libraryId.name} for module ${moduleEntity.name}") { storage ->
            val libraryEntity = moduleEntity.getModuleLibraries(storage)
                .find { it.name == javaLibraryDescriptor.name }
                ?: storage.addEntity(
                    LibraryEntity(
                        name = javaLibraryDescriptor.name,
                        tableId = libraryTableId,
                        roots = emptyList(),
                        entitySource = moduleEntity.entitySource,
                    )
                )

            storage.modifyLibraryEntity(libraryEntity) {
                this.roots.clear()
                this.roots.addAll(libraryRoots)
            }

            storage.modifyModuleEntity(moduleEntity) {
                this.dependencies += LibraryDependency(
                    libraryId,
                    javaLibraryDescriptor.exported,
                    javaLibraryDescriptor.scope
                )
            }
        }
    }
}
