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
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.util.progress.reportProgressScope
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.java.configurator.library.ModuleLibraryConfigurator
import sap.commerce.toolset.java.descriptor.JavaLibraryDescriptor
import sap.commerce.toolset.java.descriptor.JavaLibraryPath
import sap.commerce.toolset.java.descriptor.addBackofficeRootProjectLibrary
import sap.commerce.toolset.java.descriptor.collectLibraryDescriptors
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCoreExtModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.time.measureTime

class JavaModuleLibrariesConfigurator : ModuleImportConfigurator {

    private val logger = thisLogger()

    override val name: String
        get() = "Library Roots"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        module: Module,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(module);
        val sourceCodeRoot = getSourceCodeRoot(importContext)

        // TODO: migrate to new Configurator for JavaLibraryDescriptor

        val configurators = ModuleLibraryConfigurator.EP.extensionList
            .filter { configurator -> configurator.isApplicable(importContext, moduleDescriptor) }

        reportProgressScope(configurators.size) { reporter ->
            configurators.forEach { configurator ->
                reporter.itemStep("Applying library '${configurator.name}' configurator...") {
                    checkCanceled()

                    val duration = measureTime { configurator.configure(importContext, moduleDescriptor) }
                    logger.info("Content root configurator [${moduleDescriptor.name} | ${configurator.name} | $duration]")
                }
            }
        }

        for (javaLibraryDescriptor in moduleDescriptor.collectLibraryDescriptors(importContext)) {
            val noValidClassesPaths = javaLibraryDescriptor.libraryPaths
                .filter { it.rootType == OrderRootType.CLASSES }
                .takeIf { it.isNotEmpty() }
                ?.none { it.path.exists() }

                ?: true

            if (noValidClassesPaths) {
                thisLogger().warn("Library paths with CLASSES root type are not found: ${moduleDescriptor.name} | ${javaLibraryDescriptor.name}")
                continue
            }

            addRoots(modifiableRootModel, modifiableModelsProvider, sourceCodeRoot, javaLibraryDescriptor)
        }

        when (moduleDescriptor) {
            is YCoreExtModuleDescriptor -> addLibsToModule(modifiableRootModel, modifiableModelsProvider, HybrisConstants.PLATFORM_LIBRARY_GROUP, true)
            is YOotbRegularModuleDescriptor -> {
                if (moduleDescriptor.extensionInfo.backofficeModule) {
                    val backofficeJarDirectory = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.BACKOFFICE_JAR)
                    if (backofficeJarDirectory.directoryExists) {
                        addBackofficeRootProjectLibrary(importContext, modifiableModelsProvider, backofficeJarDirectory)
                    }
                }
                if (moduleDescriptor.name == EiConstants.Extension.BACK_OFFICE) {
                    addLibsToModule(modifiableRootModel, modifiableModelsProvider, HybrisConstants.BACKOFFICE_LIBRARY_GROUP, true)
                }
            }

            is YWebSubModuleDescriptor -> {
                if (moduleDescriptor.owner.name == EiConstants.Extension.BACK_OFFICE) {
                    val classes = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES)
                    val library = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_LIB)
                    val sources = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.RELATIVE_DOC_SOURCES)

                    addBackofficeRootProjectLibrary(importContext, modifiableModelsProvider, classes, null, false)
                    addBackofficeRootProjectLibrary(importContext, modifiableModelsProvider, library, sources)
                }
            }
        }
    }

    private fun getSourceCodeRoot(importContext: ProjectImportContext) = importContext.sourceCodeFile
        ?.let { VfsUtil.findFile(it, true) }
        ?.let { vf ->
            if (vf.isDirectory) vf
            else JarFileSystem.getInstance().getJarRootForLocalFile(vf)
        }

    private fun addRoots(
        modifiableRootModel: ModifiableRootModel,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        sourceCodeRoot: VirtualFile?,
        javaLibraryDescriptor: JavaLibraryDescriptor
    ) {
        val library = modifiableRootModel.moduleLibraryTable.createLibrary(javaLibraryDescriptor.name)
        val libraryModifiableModel = modifiableModelsProvider.getModifiableLibraryModel(library)

        setLibraryEntryScope(modifiableRootModel, library, javaLibraryDescriptor.scope)

        if (javaLibraryDescriptor.exported) setLibraryEntryExported(modifiableRootModel, library)

        if (sourceCodeRoot != null && javaLibraryDescriptor.libraryPaths.any { it.path.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }) {
            libraryModifiableModel.addRoot(sourceCodeRoot, OrderRootType.SOURCES)
        }

        javaLibraryDescriptor.libraryPaths.forEach {
            when (it) {
                is JavaLibraryPath.Root -> libraryModifiableModel.addRoot(it.url, it.rootType)
                is JavaLibraryPath.JarDirectory -> libraryModifiableModel.addJarDirectory(it.url, true, it.rootType)
            }
        }
    }

    private fun addLibsToModule(
        modifiableRootModel: ModifiableRootModel,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        libraryName: String,
        export: Boolean
    ) {
        val libraryTableModifiableModel = modifiableModelsProvider.modifiableProjectLibrariesModel
        val library = libraryTableModifiableModel.getLibraryByName(libraryName)
            ?: libraryTableModifiableModel.createLibrary(libraryName)
        modifiableRootModel.addLibraryEntry(library)

        if (export) {
            setLibraryEntryExported(modifiableRootModel, library)
        }
    }

    private fun setLibraryEntryExported(modifiableRootModel: ModifiableRootModel, library: Library) {
        findOrderEntryForLibrary(modifiableRootModel, library).isExported = true
    }

    private fun setLibraryEntryScope(modifiableRootModel: ModifiableRootModel, library: Library, scope: DependencyScope) {
        findOrderEntryForLibrary(modifiableRootModel, library).scope = scope
    }

    // Workaround of using Library.equals in findLibraryOrderEntry, which doesn't work here, because all empty libs are equal. Use == instead.
    private fun findOrderEntryForLibrary(
        modifiableRootModel: ModifiableRootModel,
        library: Library
    ) = modifiableRootModel.orderEntries
        .mapNotNull { it as? LibraryOrderEntry }
        .find { it.library == library }
        ?: (modifiableRootModel.findLibraryOrderEntry(library) as LibraryOrderEntry)
}