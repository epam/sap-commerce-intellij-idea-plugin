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
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel
import com.intellij.openapi.vfs.VfsUtil
import java.io.File

object YModuleLibDescriptorUtil {

    fun getLibraryDescriptors(descriptor: ModuleDescriptor): List<JavaLibraryDescriptor> = when (descriptor) {
        is YRegularModuleDescriptor -> getLibraryDescriptors(descriptor)
        is PlatformModuleDescriptor -> getLibraryDescriptors(descriptor)
        is ConfigModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YWebSubModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YBackofficeSubModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YHmcSubModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YHacSubModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YAcceleratorAddonSubModuleDescriptor -> getLibraryDescriptors(descriptor)
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

    private fun getLibraryDescriptors(descriptor: YRegularModuleDescriptor): List<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addServerLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        return libs
    }

    private fun addRootLib(
        descriptor: YModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.LIB_DIRECTORY),
                exported = true,
                descriptorType = LibraryDescriptorType.LIB
            )
        )
    }

    private fun addBackofficeLibs(
        descriptor: YBackofficeSubModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.BACKOFFICE_LIB_DIRECTORY),
                exported = true
            )
        )

        val project = descriptor.rootProjectDescriptor.project ?: return

        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(
                    descriptor.rootProjectDescriptor.hybrisDistributionDirectory,
                    HybrisProjectSettingsComponent.getInstance(project).getBackofficeWebInfLib()
                )
            )
        )
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(
                    descriptor.rootProjectDescriptor.hybrisDistributionDirectory,
                    HybrisProjectSettingsComponent.getInstance(project).getBackofficeWebInfClasses()
                ),
                directoryWithClasses = true
            )
        )
    }

    private fun addHmcLibs(
        descriptor: YHmcSubModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.BIN_DIRECTORY),
                exported = true
            )
        )

        descriptor.rootProjectDescriptor.modulesChosenForImport
            .firstOrNull { it.name == HybrisConstants.EXTENSION_NAME_HMC }
            ?.let {
                libs.add(
                    JavaLibraryDescriptor(
                        libraryFile = File(it.moduleRootDirectory, HybrisConstants.WEB_INF_CLASSES_DIRECTORY),
                        directoryWithClasses = true
                    )
                )
            }
    }

    private fun addLibrariesToNonCustomModule(
        descriptor: YModuleDescriptor,
        descriptorType: ModuleDescriptorType?,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        if (!descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode) return
        if (descriptorType == ModuleDescriptorType.CUSTOM) return

        val sourceFiles = HybrisConstants.ALL_SRC_DIR_NAMES
            .map { File(descriptor.moduleRootDirectory, it) }
            .filter { it.isDirectory }

        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.JAVA_COMPILER_OUTPUT_PATH),
                sourceFiles = sourceFiles,
                exported = true,
                directoryWithClasses = true
            )
        )
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.RESOURCES_DIRECTORY),
                exported = true,
                directoryWithClasses = true
            )
        )
    }

    private fun addServerLibs(descriptor: YModuleDescriptor, libs: MutableList<JavaLibraryDescriptor>) {
        val binDir = File(descriptor.moduleRootDirectory, HybrisConstants.BIN_DIRECTORY)
            .takeIf { it.isDirectory }
            ?: return
        val serverJars = binDir
            .listFiles { _, name: String -> name.endsWith(HybrisConstants.HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX) }
            ?.takeIf { it.isNotEmpty() }
            ?: return

        val sourceDirNames = HybrisConstants.ALL_SRC_DIR_NAMES + HybrisConstants.TEST_SRC_DIR_NAMES
        val sourceFiles = sourceDirNames
            .map { File(descriptor.moduleRootDirectory, it) }
            .filter { it.isDirectory }

        for (serverJar in serverJars) {
            libs.add(
                JavaLibraryDescriptor(
                    libraryFile = serverJar,
                    sourceFiles = sourceFiles,
                    exported = true,
                    directoryWithClasses = true
                )
            )
        }
    }

    private fun processAddOnBackwardDependencies(
        descriptor: YModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        if (!descriptor.rootProjectDescriptor.isCreateBackwardCyclicDependenciesForAddOn) return

        val backwardDependencies = descriptor.dependenciesTree
            .filter { YModuleDescriptorUtil.getRequiredExtensionNames(it).contains(descriptor.name) }
            .map {
                JavaLibraryDescriptor(
                    libraryFile = File(it.moduleRootDirectory, HybrisConstants.WEB_WEBINF_LIB_DIRECTORY),
                )
            }
        libs.addAll(backwardDependencies)
    }

    private fun getLibraryDescriptors(descriptor: YBackofficeSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addServerLibs(descriptor, libs)
        addBackofficeLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        return libs
    }

    /**
     * https://hybris-integration.atlassian.net/browse/IIP-355
     * HAC addons can not be compiled correctly by Intellij build because
     * "hybris/bin/platform/ext/hac/web/webroot/WEB-INF/classes" from "hac" extension is not registered
     * as a dependency for HAC addons.
     */
    private fun getLibraryDescriptors(descriptor: YHacSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addServerLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.rootProjectDescriptor.hybrisDistributionDirectory, HybrisConstants.HAC_WEB_INF_CLASSES),
                directoryWithClasses = true
            )
        )

        return libs
    }

    private fun getLibraryDescriptors(descriptor: YHmcSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addHmcLibs(descriptor, libs)
        addServerLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        return libs
    }

    private fun getLibraryDescriptors(descriptor: YAcceleratorAddonSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addServerLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        // TODO: add module dependency, not lib dependency
        processAddOnBackwardDependencies(descriptor, libs)
        return libs
    }

    private fun getLibraryDescriptors(descriptor: YWebSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addServerLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        val attachSources = descriptor.descriptorType != ModuleDescriptorType.CUSTOM && descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode
        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.WEB_INF_CLASSES_DIRECTORY),
                sourceFiles = if (attachSources) listOf(File(descriptor.moduleRootDirectory, HybrisConstants.WEB_SRC_DIRECTORY))
                else emptyList(),
                exported = true,
                directoryWithClasses = true
            )
        )

        libs.add(
            JavaLibraryDescriptor(
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.WEB_WEBINF_LIB_DIRECTORY),
                descriptorType = LibraryDescriptorType.WEB_INF_LIB
            )
        )

        return libs
    }

    private fun getLibraryDescriptors(descriptor: ConfigModuleDescriptor) = listOf(
        JavaLibraryDescriptor(
            libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.CONFIG_LICENCE_DIRECTORY),
            exported = true
        )
    )

    private fun getLibraryDescriptors(descriptor: PlatformModuleDescriptor) = listOf(
        JavaLibraryDescriptor(
            libraryFile = getDbDriversDirectory(descriptor),
            exported = true,
        )
    )

    private fun getDbDriversDirectory(descriptor: PlatformModuleDescriptor) = descriptor.rootProjectDescriptor.externalDbDriversDirectory
        ?: File(descriptor.moduleRootDirectory, HybrisConstants.PLATFORM_DB_DRIVER)
}