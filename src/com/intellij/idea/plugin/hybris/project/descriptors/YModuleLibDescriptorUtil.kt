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

    fun getLibraryDescriptors(descriptor: HybrisModuleDescriptor): List<JavaLibraryDescriptor> = when (descriptor) {
        is RegularHybrisModuleDescriptor -> getLibraryDescriptors(descriptor)
        is PlatformHybrisModuleDescriptor -> getLibraryDescriptors(descriptor)
        is ConfigHybrisModuleDescriptor -> getLibraryDescriptors(descriptor)
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
        descriptor: PlatformHybrisModuleDescriptor,
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

    private fun getLibraryDirectories(descriptor: PlatformHybrisModuleDescriptor): Collection<File> {
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

    private fun getLibraryDescriptors(descriptor: RegularHybrisModuleDescriptor): List<JavaLibraryDescriptor> {
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
        descriptor: RegularHybrisModuleDescriptor
    ) {
        libs.add(
            DefaultJavaLibraryDescriptor(
                File(descriptor.rootDirectory, HybrisConstants.LIB_DIRECTORY),
                true,
                LibraryDescriptorType.LIB
            )
        )
    }

    private fun addBackofficeLibs(
        libs: MutableList<JavaLibraryDescriptor>,
        descriptor: RegularHybrisModuleDescriptor
    ) {
        libs.add(
            DefaultJavaLibraryDescriptor(
                File(descriptor.rootDirectory, HybrisConstants.BACKOFFICE_LIB_DIRECTORY),
                true
            )
        )

        descriptor.rootProjectDescriptor.project
            ?.takeIf { YModuleDescriptorUtil.hasBackofficeModule(descriptor) }
            ?.let {
                libs.add(
                    DefaultJavaLibraryDescriptor(
                        File(
                            descriptor.rootProjectDescriptor.hybrisDistributionDirectory,
                            HybrisProjectSettingsComponent.getInstance(it).getBackofficeWebInfLib()
                        ),
                        false, false
                    )
                )
                libs.add(
                    DefaultJavaLibraryDescriptor(
                        File(
                            descriptor.rootProjectDescriptor.hybrisDistributionDirectory,
                            HybrisProjectSettingsComponent.getInstance(it).getBackofficeWebInfClasses()
                        ),
                        false, true
                    )
                )
            }
    }

    private fun addHmcLibs(
        descriptor: RegularHybrisModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        libs.add(
            DefaultJavaLibraryDescriptor(
                File(descriptor.rootDirectory, HybrisConstants.HMC_BIN_LIB_DIRECTORY),
                true
            )
        )

        if (YModuleDescriptorUtil.hasHmcModule(descriptor)) {
            descriptor.rootProjectDescriptor.modulesChosenForImport
                .firstOrNull { it.name == HybrisConstants.EXTENSION_NAME_HMC }
                ?.let {
                    libs.add(
                        DefaultJavaLibraryDescriptor(
                            File(it.rootDirectory, HybrisConstants.WEB_INF_CLASSES_DIRECTORY),
                            false, true
                        )
                    )
                }
        }
    }

    private fun addWebLibs(
        descriptor: RegularHybrisModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        File(descriptor.rootDirectory, HybrisConstants.WEB_SRC_DIRECTORY)
            .takeUnless { it.exists() }
            .takeUnless { descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode }
            ?.let {
                libs.add(
                    DefaultJavaLibraryDescriptor(
                        File(descriptor.rootDirectory, HybrisConstants.WEB_INF_CLASSES_DIRECTORY), false, true
                    )
                )
            }

        libs.add(
            DefaultJavaLibraryDescriptor(
                File(descriptor.rootDirectory, HybrisConstants.WEB_WEBINF_LIB_DIRECTORY),
                false,
                LibraryDescriptorType.WEB_INF_LIB
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
        descriptor: RegularHybrisModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        if (YModuleDescriptorUtil.isHacAddon(descriptor)) {
            libs.add(
                DefaultJavaLibraryDescriptor(
                    File(descriptor.rootProjectDescriptor.hybrisDistributionDirectory, HybrisConstants.HAC_WEB_INF_CLASSES),
                    false, true
                )
            )
        }
    }

    private fun addLibrariesToNonCustomModule(
        descriptor: RegularHybrisModuleDescriptor,
        descriptorType: HybrisModuleDescriptorType?,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        if (!descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode) return
        if (descriptorType == HybrisModuleDescriptorType.CUSTOM) return

        libs.add(
            DefaultJavaLibraryDescriptor(
                File(descriptor.rootDirectory, HybrisConstants.WEB_INF_CLASSES_DIRECTORY),
                File(descriptor.rootDirectory, HybrisConstants.WEB_SRC_DIRECTORY),
                false, true
            )
        )
        for (srcDirName in HybrisConstants.SRC_DIR_NAMES) {
            libs.add(
                DefaultJavaLibraryDescriptor(
                    File(descriptor.rootDirectory, HybrisConstants.JAVA_COMPILER_OUTPUT_PATH),
                    File(descriptor.rootDirectory, srcDirName),
                    true, true
                )
            )
        }
        libs.add(
            DefaultJavaLibraryDescriptor(
                File(descriptor.rootDirectory, HybrisConstants.RESOURCES_DIRECTORY),
                true, true
            )
        )
        val hmcModuleDirectory = File(descriptor.rootDirectory, HybrisConstants.HMC_MODULE_DIRECTORY)
        libs.add(
            DefaultJavaLibraryDescriptor(
                File(hmcModuleDirectory, HybrisConstants.RESOURCES_DIRECTORY),
                true, true
            )
        )
    }

    private fun addServerLibs(descriptor: RegularHybrisModuleDescriptor, libs: MutableList<JavaLibraryDescriptor>) {
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
                libs.add(DefaultJavaLibraryDescriptor(serverJar, if (srcDir.isDirectory) srcDir else null, true, true))
            }
        }
    }

    private fun processAddOnBackwardDependencies(descriptor: RegularHybrisModuleDescriptor, libs: MutableList<JavaLibraryDescriptor>) {
        if (!descriptor.rootProjectDescriptor.isCreateBackwardCyclicDependenciesForAddOn) return

        val backwardDependencies = descriptor.dependenciesTree
            .filter { YModuleDescriptorUtil.getRequiredExtensionNames(it).contains(descriptor.name) }
            .map {
                DefaultJavaLibraryDescriptor(
                    File(it.rootDirectory, HybrisConstants.WEB_WEBINF_LIB_DIRECTORY),
                    false, false
                )
            }
        libs.addAll(backwardDependencies)
    }


    private fun getLibraryDescriptors(descriptor: ConfigHybrisModuleDescriptor) = listOf(
        DefaultJavaLibraryDescriptor(File(descriptor.rootDirectory, HybrisConstants.CONFIG_LICENCE_DIRECTORY), true)
    )

    private fun getLibraryDescriptors(descriptor: PlatformHybrisModuleDescriptor) = listOf(
        DefaultJavaLibraryDescriptor(getDbDriversDirectory(descriptor), true, false)
    )

    private fun getDbDriversDirectory(descriptor: PlatformHybrisModuleDescriptor) = descriptor.rootProjectDescriptor.externalDbDriversDirectory
        ?: File(descriptor.rootDirectory, HybrisConstants.PLATFORM_DB_DRIVER)
}