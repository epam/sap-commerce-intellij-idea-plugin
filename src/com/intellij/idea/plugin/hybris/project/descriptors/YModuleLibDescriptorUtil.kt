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

package com.intellij.idea.plugin.hybris.project.descriptors

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.LibraryDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.impl.RootModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YConfigModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YRegularModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.io.filefilter.DirectoryFileFilter
import java.io.File
import java.io.FileFilter

object YModuleLibDescriptorUtil {

    fun getLibraryDescriptors(descriptor: ModuleDescriptor): List<JavaLibraryDescriptor> = when (descriptor) {
        is YRegularModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YPlatformModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YConfigModuleDescriptor -> getLibraryDescriptors(descriptor)
        is RootModuleDescriptor -> emptyList()
        else -> emptyList()
    }

    fun createGlobalLibrary(
        modifiableModelsProvider: IdeModifiableModelsProvider,
        libraryDirRoot: File,
        libraryName: String
    ) {
        val libraryTableModifiableModel = modifiableModelsProvider.modifiableProjectLibrariesModel
        val library = libraryTableModifiableModel.getLibraryByName(libraryName)
            ?: libraryTableModifiableModel.createLibrary(libraryName)

        if (libraryTableModifiableModel is LibrariesModifiableModel) {
            libraryTableModifiableModel
                .getLibraryEditor(library)
                .addJarDirectory(VfsUtil.getUrlForLibraryRoot(libraryDirRoot), true, OrderRootType.CLASSES)
        } else {
            modifiableModelsProvider
                .getModifiableLibraryModel(library)
                .addJarDirectory(VfsUtil.getUrlForLibraryRoot(libraryDirRoot), true)
        }
    }

    fun createBootstrapLib(
        descriptor: YPlatformModuleDescriptor,
        sourceCodeRoot: VirtualFile?,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val libraryDirectories = getLibraryDirectories(descriptor)
        val bootStrapSrc = File(descriptor.rootDirectory, HybrisConstants.PL_BOOTSTRAP_GEN_SRC_DIRECTORY)
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

    private fun getLibraryDirectories(descriptor: YPlatformModuleDescriptor): Collection<File> {
        val libraryDirectories = mutableListOf<File>()
        File(descriptor.rootDirectory, HybrisConstants.RESOURCES_DIRECTORY)
            .takeIf { it.exists() }
            ?.listFiles(DirectoryFileFilter.DIRECTORY as FileFilter)
            ?.let { resourcesInnerDirectories ->
                for (resourcesInnerDirectory in resourcesInnerDirectories) {
                    addLibraryDirectories(libraryDirectories, File(resourcesInnerDirectory, HybrisConstants.LIB_DIRECTORY))
                    addLibraryDirectories(libraryDirectories, File(resourcesInnerDirectory, HybrisConstants.BIN_DIRECTORY))
                }
            }
        addLibraryDirectories(libraryDirectories, File(descriptor.rootDirectory, HybrisConstants.PL_BOOTSTRAP_LIB_DIRECTORY))
        addLibraryDirectories(libraryDirectories, File(descriptor.rootDirectory, HybrisConstants.PL_TOMCAT_BIN_DIRECTORY))
        addLibraryDirectories(libraryDirectories, File(descriptor.rootDirectory, HybrisConstants.PL_TOMCAT_6_BIN_DIRECTORY))
        addLibraryDirectories(libraryDirectories, File(descriptor.rootDirectory, HybrisConstants.PL_TOMCAT_LIB_DIRECTORY))
        addLibraryDirectories(libraryDirectories, File(descriptor.rootDirectory, HybrisConstants.PL_TOMCAT_6_LIB_DIRECTORY))

        return libraryDirectories
    }

    private fun addLibraryDirectories(libraryDirectories: MutableList<File>, file: File) {
        if (file.exists()) {
            libraryDirectories.add(file)
        }
    }

    private fun getLibraryDescriptors(descriptor: YRegularModuleDescriptor): List<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()
        val descriptorType = YModuleDescriptorUtil.getDescriptorType(descriptor)

        addLibrariesToNonCustomModule(descriptor, descriptorType, libs)
        addHacLibs(descriptor, libs)
        addHmcLibs(descriptor, libs)
        addWebLibs(descriptor, libs)
        addServerLibs(descriptor, libs)
        addBackofficeLibs(libs, descriptor)
        addRootLib(libs, descriptor)

        if (YModuleDescriptorUtil.isAcceleratorAddOnModuleRoot(descriptor)) {
            processAddOnBackwardDependencies(descriptor, libs)
        }
        return libs
    }

    private fun addRootLib(
        libs: MutableList<JavaLibraryDescriptor>,
        descriptor: YRegularModuleDescriptor
    ) {
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.rootDirectory, HybrisConstants.LIB_DIRECTORY),
                exported = true,
                descriptorType = LibraryDescriptorType.LIB
            )
        )
    }

    private fun addBackofficeLibs(
        libs: MutableList<JavaLibraryDescriptor>,
        descriptor: YRegularModuleDescriptor
    ) {
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.rootDirectory, HybrisConstants.BACKOFFICE_LIB_DIRECTORY),
                exported = true
            )
        )

        descriptor.rootProjectDescriptor.project
            ?.takeIf { YModuleDescriptorUtil.hasBackofficeModule(descriptor) }
            ?.let {
                libs.add(
                    JavaLibraryDescriptor(
                        libraryFile = File(
                            descriptor.rootProjectDescriptor.hybrisDistributionDirectory,
                            HybrisProjectSettingsComponent.getInstance(it).getBackofficeWebInfLib()
                        )
                    )
                )
                libs.add(
                    JavaLibraryDescriptor(
                        libraryFile = File(
                            descriptor.rootProjectDescriptor.hybrisDistributionDirectory,
                            HybrisProjectSettingsComponent.getInstance(it).getBackofficeWebInfClasses()
                        ),
                        directoryWithClasses = true
                    )
                )
            }
    }

    private fun addHmcLibs(
        descriptor: YRegularModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.rootDirectory, HybrisConstants.HMC_BIN_LIB_DIRECTORY),
                exported = true
            )
        )

        if (YModuleDescriptorUtil.hasHmcModule(descriptor)) {
            descriptor.rootProjectDescriptor.modulesChosenForImport
                .firstOrNull { it.name == HybrisConstants.EXTENSION_NAME_HMC }
                ?.let {
                    libs.add(
                        JavaLibraryDescriptor(
                            libraryFile = File(it.rootDirectory, HybrisConstants.WEB_INF_CLASSES_DIRECTORY),
                            directoryWithClasses = true
                        )
                    )
                }
        }
    }

    private fun addWebLibs(
        descriptor: YRegularModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        File(descriptor.rootDirectory, HybrisConstants.WEB_SRC_DIRECTORY)
            .takeUnless { it.exists() }
            .takeUnless { descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode }
            ?.let {
                libs.add(
                    JavaLibraryDescriptor(
                        libraryFile = File(descriptor.rootDirectory, HybrisConstants.WEB_INF_CLASSES_DIRECTORY),
                        directoryWithClasses = true
                    )
                )
            }

        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.rootDirectory, HybrisConstants.WEB_WEBINF_LIB_DIRECTORY),
                descriptorType = LibraryDescriptorType.WEB_INF_LIB
            )
        )

    }

    /**
     * https://hybris-integration.atlassian.net/browse/IIP-355
     * HAC addons can not be compiled correctly by Intellij build because
     * "hybris/bin/platform/ext/hac/web/webroot/WEB-INF/classes" from "hac" extension is not registered
     * as a dependency for HAC addons.
     */
    private fun addHacLibs(
        descriptor: YRegularModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        if (YModuleDescriptorUtil.isHacAddon(descriptor)) {
            libs.add(
                JavaLibraryDescriptor(
                    libraryFile = File(descriptor.rootProjectDescriptor.hybrisDistributionDirectory, HybrisConstants.HAC_WEB_INF_CLASSES),
                    directoryWithClasses = true
                )
            )
        }
    }

    private fun addLibrariesToNonCustomModule(
        descriptor: YRegularModuleDescriptor,
        descriptorType: ModuleDescriptorType?,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        if (!descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode) return
        if (descriptorType == ModuleDescriptorType.CUSTOM) return

        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.rootDirectory, HybrisConstants.WEB_INF_CLASSES_DIRECTORY),
                sourcesFile = File(descriptor.rootDirectory, HybrisConstants.WEB_SRC_DIRECTORY),
                directoryWithClasses = true
            )
        )
        for (srcDirName in HybrisConstants.SRC_DIR_NAMES) {
            libs.add(
                JavaLibraryDescriptor(
                    libraryFile = File(descriptor.rootDirectory, HybrisConstants.JAVA_COMPILER_OUTPUT_PATH),
                    sourcesFile = File(descriptor.rootDirectory, srcDirName),
                    exported = true,
                    directoryWithClasses = true
                )
            )
        }
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.rootDirectory, HybrisConstants.RESOURCES_DIRECTORY),
                exported = true,
                directoryWithClasses = true
            )
        )
        val hmcModuleDirectory = File(descriptor.rootDirectory, HybrisConstants.HMC_MODULE_DIRECTORY)
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(hmcModuleDirectory, HybrisConstants.RESOURCES_DIRECTORY),
                exported = true,
                directoryWithClasses = true
            )
        )
    }

    private fun addServerLibs(descriptor: YRegularModuleDescriptor, libs: MutableList<JavaLibraryDescriptor>) {
        val binDir = File(descriptor.rootDirectory, HybrisConstants.BIN_DIRECTORY)
            .takeIf { it.isDirectory }
            ?: return
        val serverJars = binDir
            .listFiles { _, name: String -> name.endsWith(HybrisConstants.HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX) }
            ?.takeIf { it.isNotEmpty() }
            ?: return

        for (srcDirName in HybrisConstants.SRC_DIR_NAMES) {
            val srcDir = File(descriptor.rootDirectory, srcDirName)
            for (serverJar in serverJars) {
                libs.add(
                    JavaLibraryDescriptor(
                        libraryFile = serverJar,
                        sourcesFile = if (srcDir.isDirectory) srcDir else null,
                        exported = true,
                        directoryWithClasses = true
                    )
                )
            }
        }
    }

    private fun processAddOnBackwardDependencies(descriptor: YRegularModuleDescriptor, libs: MutableList<JavaLibraryDescriptor>) {
        if (!descriptor.rootProjectDescriptor.isCreateBackwardCyclicDependenciesForAddOn) return

        val backwardDependencies = descriptor.dependenciesTree
            .filter { YModuleDescriptorUtil.getRequiredExtensionNames(it).contains(descriptor.name) }
            .map {
                JavaLibraryDescriptor(
                    libraryFile = File(it.rootDirectory, HybrisConstants.WEB_WEBINF_LIB_DIRECTORY),
                )
            }
        libs.addAll(backwardDependencies)
    }


    private fun getLibraryDescriptors(descriptor: YConfigModuleDescriptor) = listOf(
        JavaLibraryDescriptor(
            libraryFile = File(descriptor.rootDirectory, HybrisConstants.CONFIG_LICENCE_DIRECTORY),
            exported = true
        )
    )

    private fun getLibraryDescriptors(descriptor: YPlatformModuleDescriptor) = listOf(
        JavaLibraryDescriptor(
            libraryFile = getDbDriversDirectory(descriptor),
            exported = true,
        )
    )

    private fun getDbDriversDirectory(descriptor: YPlatformModuleDescriptor) = descriptor.rootProjectDescriptor.externalDbDriversDirectory
        ?: File(descriptor.rootDirectory, HybrisConstants.PLATFORM_DB_DRIVER)
}