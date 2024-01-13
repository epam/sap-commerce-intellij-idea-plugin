/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.project.configurators.LibRootsConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.JavaLibraryDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleLibDescriptorUtil
import com.intellij.idea.plugin.hybris.project.descriptors.impl.PlatformModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCoreExtModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YOotbRegularModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YWebSubModuleDescriptor
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class DefaultLibRootsConfigurator : LibRootsConfigurator {

    override fun configure(
        progressIndicator: ProgressIndicator,
        allYModules: Map<String, YModuleDescriptor>,
        modifiableRootModel: ModifiableRootModel,
        moduleDescriptor: ModuleDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        indicator: ProgressIndicator
    ) {
        indicator.text2 = HybrisI18NBundleUtils.message("hybris.project.import.module.libs")

        val sourceCodeRoot = getSourceCodeRoot(moduleDescriptor)
        for (javaLibraryDescriptor in YModuleLibDescriptorUtil.getLibraryDescriptors(moduleDescriptor, allYModules)) {
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
                if (moduleDescriptor.hasBackofficeModule) {
                    val backofficeJarDirectory = File(moduleDescriptor.moduleRootDirectory, HybrisConstants.BACKOFFICE_JAR_PATH)
                    if (backofficeJarDirectory.exists()) {
                        YModuleLibDescriptorUtil.addRootProjectLibrary(modifiableModelsProvider, backofficeJarDirectory, HybrisConstants.BACKOFFICE_LIBRARY_GROUP)
                    }
                }
                if (moduleDescriptor.name == HybrisConstants.EXTENSION_NAME_BACK_OFFICE) {
                    addLibsToModule(modifiableRootModel, modifiableModelsProvider, HybrisConstants.BACKOFFICE_LIBRARY_GROUP, true)
                }
            }
            is YWebSubModuleDescriptor -> {
                if (moduleDescriptor.owner.name == HybrisConstants.EXTENSION_NAME_BACK_OFFICE) {
                    val classes = File(moduleDescriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_CLASSES_PATH)
                    val library = File(moduleDescriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_LIB_PATH)
                    YModuleLibDescriptorUtil.addRootProjectLibrary(modifiableModelsProvider, classes, HybrisConstants.BACKOFFICE_LIBRARY_GROUP, false)
                    YModuleLibDescriptorUtil.addRootProjectLibrary(modifiableModelsProvider, library, HybrisConstants.BACKOFFICE_LIBRARY_GROUP)
                }
            }
        }
    }

    private fun getSourceCodeRoot(moduleDescriptor: ModuleDescriptor) = moduleDescriptor.rootProjectDescriptor.sourceCodeFile
        ?.let { VfsUtil.findFileByIoFile(it, true) }
        ?.let {
            if (it.isDirectory) {
                it
            } else {
                JarFileSystem.getInstance().getJarRootForLocalFile(it)
            }
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
            && javaLibraryDescriptor.libraryFile.name.endsWith(HybrisConstants.HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX)
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
        .mapNotNull { VfsUtil.findFileByIoFile(it, true) }
        .onEach { libraryModifiableModel.addRoot(it, OrderRootType.SOURCES) }

    private fun attachSourceJarDirectories(
        javaLibraryDescriptor: JavaLibraryDescriptor,
        libraryModifiableModel: Library.ModifiableModel
    ) {
        javaLibraryDescriptor.sourceJarDirectories
            .mapNotNull { VfsUtil.findFileByIoFile(it, true) }
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
