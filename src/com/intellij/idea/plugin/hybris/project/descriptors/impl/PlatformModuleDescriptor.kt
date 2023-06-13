/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.project.descriptors.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.io.filefilter.DirectoryFileFilter
import java.io.File
import java.io.FileFilter

class PlatformModuleDescriptor(
    moduleRootDirectory: File,
    rootProjectDescriptor: HybrisProjectDescriptor,
    name: String = HybrisConstants.EXTENSION_NAME_PLATFORM,
    override val descriptorType: ModuleDescriptorType = ModuleDescriptorType.PLATFORM,
) : AbstractModuleDescriptor(moduleRootDirectory, rootProjectDescriptor, name) {

    override fun isPreselected() = true

    override fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>) = moduleDescriptors.values
        .filterIsInstance<YPlatformExtModuleDescriptor>()
        .map { it.name }
        .toSet()

    fun createBootstrapLib(
        sourceCodeRoot: VirtualFile?,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val libraryDirectories = getLibraryDirectories()
        val bootStrapSrc = File(moduleRootDirectory, HybrisConstants.PL_BOOTSTRAP_GEN_SRC_PATH)
        val libraryTableModifiableModel = modifiableModelsProvider.modifiableProjectLibrariesModel
        val library = libraryTableModifiableModel.getLibraryByName(HybrisConstants.PLATFORM_LIBRARY_GROUP)
            ?: libraryTableModifiableModel.createLibrary(HybrisConstants.PLATFORM_LIBRARY_GROUP)

        if (libraryTableModifiableModel is LibrariesModifiableModel) {
            with(libraryTableModifiableModel.getLibraryEditor(library)) {
                for (libRoot in libraryDirectories) {
                    addJarDirectory(VfsUtil.getUrlForLibraryRoot(libRoot), true, OrderRootType.CLASSES)

                    sourceCodeRoot
                        ?.let {
                            if (sourceCodeRoot.fileSystem is JarFileSystem) {
                                addJarDirectory(sourceCodeRoot, true, OrderRootType.SOURCES)
                            } else {
                                addRoot(sourceCodeRoot, OrderRootType.SOURCES)
                            }
                        }
                }
                addRoot(VfsUtil.getUrlForLibraryRoot(bootStrapSrc), OrderRootType.SOURCES)
            }
        } else {
            with(modifiableModelsProvider.getModifiableLibraryModel(library)) {
                for (libRoot in libraryDirectories) {
                    addJarDirectory(VfsUtil.getUrlForLibraryRoot(libRoot), true)
                }
                addRoot(VfsUtil.getUrlForLibraryRoot(bootStrapSrc), OrderRootType.SOURCES)
            }
        }
    }

    private fun getLibraryDirectories(): Collection<File> {
        val libraryDirectories = mutableListOf<File>()
        File(moduleRootDirectory, HybrisConstants.RESOURCES_DIRECTORY)
            .takeIf { it.exists() }
            ?.listFiles(DirectoryFileFilter.DIRECTORY as FileFilter)
            ?.let { resourcesInnerDirectories ->
                for (resourcesInnerDirectory in resourcesInnerDirectories) {
                    addLibraryDirectories(libraryDirectories, File(resourcesInnerDirectory, HybrisConstants.LIB_DIRECTORY))
                    addLibraryDirectories(libraryDirectories, File(resourcesInnerDirectory, HybrisConstants.BIN_DIRECTORY))
                }
            }
        addLibraryDirectories(libraryDirectories, File(moduleRootDirectory, HybrisConstants.PL_BOOTSTRAP_LIB_PATH))
        addLibraryDirectories(libraryDirectories, File(moduleRootDirectory, HybrisConstants.PL_TOMCAT_BIN_PATH))
        addLibraryDirectories(libraryDirectories, File(moduleRootDirectory, HybrisConstants.PL_TOMCAT_6_BIN_PATH))
        addLibraryDirectories(libraryDirectories, File(moduleRootDirectory, HybrisConstants.PL_TOMCAT_LIB_PATH))
        addLibraryDirectories(libraryDirectories, File(moduleRootDirectory, HybrisConstants.PL_TOMCAT_6_LIB_PATH))

        return libraryDirectories
    }

    private fun addLibraryDirectories(libraryDirectories: MutableList<File>, file: File) {
        if (file.exists()) {
            libraryDirectories.add(file)
        }
    }

}
