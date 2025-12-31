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

package sap.commerce.toolset.project.descriptor.impl

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorImportStatus
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

class PlatformModuleDescriptorImpl(
    moduleRootPath: Path,
    name: String = EiConstants.Extension.PLATFORM,
) : AbstractModuleDescriptor(moduleRootPath, name, ModuleDescriptorType.PLATFORM), PlatformModuleDescriptor {

    override var importStatus = ModuleDescriptorImportStatus.MANDATORY
    override fun isPreselected() = true

    override fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>) = moduleDescriptors.values
        .filterIsInstance<YPlatformExtModuleDescriptor>()
        .map { it.name }
        .toSet()

    override fun createBootstrapLib(
        sourceCodeRoot: VirtualFile?,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val libraryDirectories = getLibraryDirectories()
        val bootStrapSrc = this@PlatformModuleDescriptorImpl.moduleRootPath.resolve(ProjectConstants.Paths.BOOTSTRAP_GEN_SRC)
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

    private fun getLibraryDirectories(): Collection<Path> = buildList<Path> {
        this@PlatformModuleDescriptorImpl.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES)
            .takeIf { it.directoryExists }
            ?.listDirectoryEntries()
            ?.filter { it.directoryExists }
            ?.forEach { resourcesInnerDirectory ->
                add(resourcesInnerDirectory.resolve(ProjectConstants.Directory.LIB))
                add(resourcesInnerDirectory.resolve(ProjectConstants.Directory.BIN))
            }

        add(this@PlatformModuleDescriptorImpl.moduleRootPath.resolve(ProjectConstants.Paths.BOOTSTRAP_BIN))
        add(this@PlatformModuleDescriptorImpl.moduleRootPath.resolve(ProjectConstants.Paths.TOMCAT_BIN))
        add(this@PlatformModuleDescriptorImpl.moduleRootPath.resolve(ProjectConstants.Paths.TOMCAT_6_BIN))
        add(this@PlatformModuleDescriptorImpl.moduleRootPath.resolve(ProjectConstants.Paths.TOMCAT_LIB))
        add(this@PlatformModuleDescriptorImpl.moduleRootPath.resolve(ProjectConstants.Paths.TOMCAT_6_LIB))
    }
        .filter { it.directoryExists }
}