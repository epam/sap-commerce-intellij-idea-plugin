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

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.java.descriptor.JavaLibraryDescriptor
import sap.commerce.toolset.java.descriptor.addBackofficeRootProjectLibrary
import sap.commerce.toolset.java.descriptor.collectLibraryDescriptors
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCoreExtModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import kotlin.io.path.exists
import kotlin.io.path.name

class JavaModuleLibrariesConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Library Roots"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        module: Module,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(module);
        val sourceCodeRoot = getSourceCodeRoot(importContext)

        for (javaLibraryDescriptor in moduleDescriptor.collectLibraryDescriptors(importContext)) {
            if (!javaLibraryDescriptor.libraryFile.exists() && javaLibraryDescriptor.scope == DependencyScope.COMPILE) {
                continue
            }
            if (javaLibraryDescriptor.directoryWithClasses) {
                addClassesToModuleLibs(modifiableRootModel, modifiableModelsProvider, sourceCodeRoot, javaLibraryDescriptor)
            } else {
                addJarFolderToModuleLibs(modifiableRootModel, modifiableModelsProvider, javaLibraryDescriptor)
            }
        }

        when (moduleDescriptor) {
            is PlatformModuleDescriptor -> moduleDescriptor.createBootstrapLib(sourceCodeRoot, modifiableModelsProvider)
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

    private fun addClassesToModuleLibs(
        modifiableRootModel: ModifiableRootModel,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        sourceCodeRoot: VirtualFile?,
        javaLibraryDescriptor: JavaLibraryDescriptor
    ) {
        val library = javaLibraryDescriptor.name
            ?.let { modifiableRootModel.moduleLibraryTable.createLibrary(it) }
            ?: modifiableRootModel.moduleLibraryTable.createLibrary()
        val libraryModifiableModel = modifiableModelsProvider.getModifiableLibraryModel(library)
        libraryModifiableModel.addRoot(VfsUtil.getUrlForLibraryRoot(javaLibraryDescriptor.libraryFile), OrderRootType.CLASSES)

        val sourceDirAttached = attachSourceFiles(javaLibraryDescriptor, libraryModifiableModel).isNotEmpty()
        attachSourceJarDirectories(javaLibraryDescriptor, libraryModifiableModel)

        if (sourceCodeRoot != null
            && !sourceDirAttached
            && javaLibraryDescriptor.libraryFile.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX)
        ) {
            libraryModifiableModel.addRoot(sourceCodeRoot, OrderRootType.SOURCES)
        }

        if (javaLibraryDescriptor.exported) {
            setLibraryEntryExported(modifiableRootModel, library)
        }

        setLibraryEntryScope(modifiableRootModel, library, javaLibraryDescriptor.scope)
    }

    private fun addJarFolderToModuleLibs(
        modifiableRootModel: ModifiableRootModel,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        javaLibraryDescriptor: JavaLibraryDescriptor
    ) {
        val projectLibraryTable = modifiableRootModel.moduleLibraryTable
        val library = javaLibraryDescriptor.name
            ?.let { projectLibraryTable.createLibrary(it) }
            ?: projectLibraryTable.createLibrary()

        val libraryModifiableModel = modifiableModelsProvider.getModifiableLibraryModel(library)
        libraryModifiableModel.addJarDirectory(VfsUtil.getUrlForLibraryRoot(javaLibraryDescriptor.libraryFile), true)
        // we have to add each jar file explicitly, otherwise Spring will not recognise `classpath:/META-INF/my.xml` in the jar files
        // Jetbrains IntelliJ IDEA issue - https://youtrack.jetbrains.com/issue/IDEA-257819
        javaLibraryDescriptor.jarFiles.forEach {
            libraryModifiableModel.addRoot(VfsUtil.getUrlForLibraryRoot(it), OrderRootType.CLASSES)
        }

        attachSourceFiles(javaLibraryDescriptor, libraryModifiableModel)
        attachSourceJarDirectories(javaLibraryDescriptor, libraryModifiableModel)

        if (javaLibraryDescriptor.exported) {
            setLibraryEntryExported(modifiableRootModel, library)
        }

        setLibraryEntryScope(modifiableRootModel, library, javaLibraryDescriptor.scope)
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

    private fun attachSourceFiles(
        javaLibraryDescriptor: JavaLibraryDescriptor,
        libraryModifiableModel: Library.ModifiableModel
    ) = javaLibraryDescriptor.sourceFiles
        .mapNotNull { VfsUtil.findFile(it, true) }
        .onEach { libraryModifiableModel.addRoot(it, OrderRootType.SOURCES) }

    private fun attachSourceJarDirectories(
        javaLibraryDescriptor: JavaLibraryDescriptor,
        libraryModifiableModel: Library.ModifiableModel
    ) {
        javaLibraryDescriptor.sourceJarDirectories
            .mapNotNull { VfsUtil.findFile(it, true) }
            .forEach { libraryModifiableModel.addJarDirectory(it, true, OrderRootType.SOURCES) }
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