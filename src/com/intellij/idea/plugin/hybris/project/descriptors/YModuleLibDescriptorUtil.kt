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

package com.intellij.idea.plugin.hybris.project.descriptors

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.LibraryDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*
import com.intellij.idea.plugin.hybris.settings.ApplicationSettings
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel
import com.intellij.openapi.vfs.VfsUtil
import java.io.File

object YModuleLibDescriptorUtil {

    fun getLibraryDescriptors(descriptor: ModuleDescriptor, allYModules: Map<String, YModuleDescriptor>): List<JavaLibraryDescriptor> = when (descriptor) {
        is YRegularModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YWebSubModuleDescriptor -> getWebLibraryDescriptors(descriptor)
        is YCommonWebSubModuleDescriptor -> getCommonWebSubModuleDescriptor(descriptor)
        is YBackofficeSubModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YAcceleratorAddonSubModuleDescriptor -> getLibraryDescriptors(descriptor, allYModules)
        is YHacSubModuleDescriptor -> getLibraryDescriptors(descriptor)
        is YHmcSubModuleDescriptor -> getLibraryDescriptors(descriptor)
        is PlatformModuleDescriptor -> getLibraryDescriptors(descriptor)
        is ConfigModuleDescriptor -> getLibraryDescriptors(descriptor)
        is RootModuleDescriptor -> emptyList()
        else -> emptyList()
    }

    fun addBackofficeRootProjectLibrary(
        modifiableModelsProvider: IdeModifiableModelsProvider,
        libraryDirRoot: File,
        sourcesDirRoot: File? = null,
        addJarDirectory: Boolean = true
    ) {
        val libraryName = HybrisConstants.BACKOFFICE_LIBRARY_GROUP
        val libraryTableModifiableModel = modifiableModelsProvider.modifiableProjectLibrariesModel
        val library = libraryTableModifiableModel.getLibraryByName(libraryName)
            ?: libraryTableModifiableModel.createLibrary(libraryName)

        if (libraryTableModifiableModel is LibrariesModifiableModel) {
            val libraryEditor = libraryTableModifiableModel.getLibraryEditor(library)
            if (addJarDirectory) libraryEditor.addJarDirectory(VfsUtil.getUrlForLibraryRoot(libraryDirRoot), true, OrderRootType.CLASSES)
            else libraryEditor.addRoot(VfsUtil.getUrlForLibraryRoot(libraryDirRoot), OrderRootType.CLASSES)
        } else {
            val libraryModel = modifiableModelsProvider.getModifiableLibraryModel(library)
            if (addJarDirectory) libraryModel.addJarDirectory(VfsUtil.getUrlForLibraryRoot(libraryDirRoot), true)
            else libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(libraryDirRoot), OrderRootType.CLASSES)
            if (sourcesDirRoot != null && ApplicationSettings.getInstance().withStandardProvidedSources) {
                libraryModel.addJarDirectory(VfsUtil.getUrlForLibraryRoot(sourcesDirRoot), true, OrderRootType.SOURCES)
            }
        }
    }

    private fun getLibraryDescriptors(descriptor: YRegularModuleDescriptor): List<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addServerLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        if (descriptor.hasBackofficeModule) {
            descriptor.getSubModules()
                .firstOrNull { it is YBackofficeSubModuleDescriptor }
                ?.let { yModule ->
                    val attachSources = descriptor.descriptorType == ModuleDescriptorType.CUSTOM || !descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode
                    val sourceFiles = (HybrisConstants.ALL_SRC_DIR_NAMES + HybrisConstants.TEST_SRC_DIR_NAMES)
                        .map { File(yModule.moduleRootDirectory, it) }
                        .filter { it.isDirectory }

                    libs.add(
                        JavaLibraryDescriptor(
                            name = "${descriptor.name} - Backoffice Classes",
                            libraryFile = File(yModule.moduleRootDirectory, HybrisConstants.JAVA_COMPILER_OUTPUT_PATH),
                            sourceFiles = if (attachSources) sourceFiles
                            else emptyList(),
                            directoryWithClasses = true,
                            scope = DependencyScope.PROVIDED
                        )
                    )
                }
        }

        return libs
    }

    private fun addRootLib(
        descriptor: YModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        libs.add(
            JavaLibraryDescriptor(
                name = "${descriptor.name} - lib",
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
                name = "${descriptor.name} - Backoffice lib",
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.BACKOFFICE_LIB_PATH),
                exported = true
            )
        )
    }

    private fun addHmcLibs(
        descriptor: YHmcSubModuleDescriptor,
        libs: MutableList<JavaLibraryDescriptor>
    ) {
        libs.add(
            JavaLibraryDescriptor(
                name = "${descriptor.name} - HMC Bin",
                libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.BIN_DIRECTORY),
                exported = true
            )
        )

        descriptor.rootProjectDescriptor.modulesChosenForImport
            .firstOrNull { it.name == HybrisConstants.EXTENSION_NAME_HMC }
            ?.let {
                libs.add(
                    JavaLibraryDescriptor(
                        name = "${descriptor.name} - Web Classes",
                        libraryFile = File(it.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_CLASSES_PATH),
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

        val sourceFiles = (HybrisConstants.ALL_SRC_DIR_NAMES + HybrisConstants.TEST_SRC_DIR_NAMES)
            .map { File(descriptor.moduleRootDirectory, it) }
            .filter { it.isDirectory }

        if (HybrisConstants.EXTENSION_NAME_PLATFORM_SERVICES != descriptor.name) {
            libs.add(
                JavaLibraryDescriptor(
                    name = "${descriptor.name} - compiler output",
                    libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.JAVA_COMPILER_OUTPUT_PATH),
                    sourceFiles = sourceFiles,
                    exported = true,
                    directoryWithClasses = true
                )
            )
        }
        libs.add(
            JavaLibraryDescriptor(
                name = "${descriptor.name} - resources",
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

        val sourceFiles = (HybrisConstants.ALL_SRC_DIR_NAMES + HybrisConstants.TEST_SRC_DIR_NAMES)
            .map { File(descriptor.moduleRootDirectory, it) }
            .filter { it.isDirectory }

        // Attach standard sources to server jar
        val sourceJarDirectories = getStandardSourceJarDirectory(descriptor)

        for (serverJar in serverJars) {
            libs.add(
                JavaLibraryDescriptor(
                    name = "${descriptor.name} - server",
                    libraryFile = serverJar,
                    sourceFiles = sourceFiles,
                    sourceJarDirectories = sourceJarDirectories,
                    exported = true,
                    directoryWithClasses = true
                )
            )
        }
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
                name = "${descriptor.name} - HAC Web Classes",
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

    private fun getLibraryDescriptors(descriptor: YAcceleratorAddonSubModuleDescriptor, allYModules: Map<String, YModuleDescriptor>): MutableList<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addServerLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        val attachSources = descriptor.descriptorType == ModuleDescriptorType.CUSTOM || !descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode
        allYModules.values
            .filter { it.getDirectDependencies().contains(descriptor.owner) }
            .filter { it != descriptor }
            .map { yModule ->
                // process owner extension dependencies
                val addonSourceFiles = (HybrisConstants.ALL_SRC_DIR_NAMES + HybrisConstants.TEST_SRC_DIR_NAMES)
                    .map { File(yModule.moduleRootDirectory, it) }
                    .filter { it.isDirectory }

                libs.add(
                    JavaLibraryDescriptor(
                        name = "${yModule.name} - Addon's Target Classes",
                        libraryFile = File(yModule.moduleRootDirectory, HybrisConstants.JAVA_COMPILER_OUTPUT_PATH),
                        sourceFiles = if (attachSources) addonSourceFiles
                        else emptyList(),
                        directoryWithClasses = true
                    )
                )
                libs.add(
                    JavaLibraryDescriptor(
                        name = "${yModule.name} - Addon's Target Resources",
                        libraryFile = File(yModule.moduleRootDirectory, HybrisConstants.RESOURCES_DIRECTORY),
                        directoryWithClasses = true
                    )
                )
            }
        return libs
    }

    private fun getWebLibraryDescriptors(
        descriptor: YSubModuleDescriptor,
        libName: String = "Web"
    ): MutableList<JavaLibraryDescriptor> {
        val libs = mutableListOf<JavaLibraryDescriptor>()

        addLibrariesToNonCustomModule(descriptor, descriptor.descriptorType, libs)
        addServerLibs(descriptor, libs)
        addRootLib(descriptor, libs)

        val libFolder = File(descriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_LIB_PATH)
        val sourceFiles = HybrisConstants.ALL_SRC_DIR_NAMES
            .map { File(descriptor.moduleRootDirectory, it) }
            .filter { it.isDirectory }
            .toMutableList()

        listOf(
            File(descriptor.moduleRootDirectory, HybrisConstants.ADDON_SRC_DIRECTORY),
            File(descriptor.moduleRootDirectory, HybrisConstants.COMMON_WEB_SRC_DIRECTORY),
        )
            .filter { it.isDirectory }
            .mapNotNull { srcDir ->
                srcDir.listFiles { it: File -> it.isDirectory }
                    ?.toList()
            }
            .flatten()
            .forEach { sourceFiles.add(it) }

        if (descriptor.owner.name != HybrisConstants.EXTENSION_NAME_BACK_OFFICE) {
            if (descriptor.descriptorType != ModuleDescriptorType.CUSTOM && descriptor.rootProjectDescriptor.isImportOotbModulesInReadOnlyMode) {
                libs.add(
                    JavaLibraryDescriptor(
                        name = "${descriptor.name} - $libName Classes",
                        libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_CLASSES_PATH),
                        sourceFiles = sourceFiles,
                        exported = true,
                        directoryWithClasses = true
                    )
                )
            }

            libs.add(
                JavaLibraryDescriptor(
                    name = "${descriptor.name} - $libName Library",
                    libraryFile = libFolder,
                    jarFiles = libFolder.listFiles { _, name: String -> name.endsWith(".jar") }
                        ?.toSet()
                        ?: emptySet(),
                    sourceJarDirectories = getStandardSourceJarDirectory(descriptor),
                    exported = true,
                    descriptorType = LibraryDescriptorType.WEB_INF_LIB
                )
            )
        }

        return libs
    }

    private fun getCommonWebSubModuleDescriptor(
        descriptor: YCommonWebSubModuleDescriptor,
        libName: String = "Common Web"
    ): MutableList<JavaLibraryDescriptor> {
        val libs = getWebLibraryDescriptors(descriptor, libName)

        descriptor
            .dependantWebExtensions
            .forEach {
                val webSourceFiles = HybrisConstants.ALL_SRC_DIR_NAMES
                    .map { dir -> File(descriptor.moduleRootDirectory, dir) }
                    .filter { dir -> dir.isDirectory }
                libs.add(
                    JavaLibraryDescriptor(
                        name = "${it.name} - $libName Classes",
                        libraryFile = File(it.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_CLASSES_PATH),
                        sourceFiles = webSourceFiles,
                        exported = true,
                        directoryWithClasses = true
                    )
                )
            }
        return libs
    }

    private fun getLibraryDescriptors(descriptor: ConfigModuleDescriptor) = listOf(
        JavaLibraryDescriptor(
            name = "Config License",
            libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.CONFIG_LICENCE_DIRECTORY),
            exported = true
        )
    )

    private fun getLibraryDescriptors(descriptor: PlatformModuleDescriptor) = listOf(
        JavaLibraryDescriptor(
            name = HybrisConstants.PLATFORM_DATABASE_DRIVER_LIBRARY,
            libraryFile = getDbDriversDirectory(descriptor),
            exported = true,
        )
    )

    private fun getDbDriversDirectory(descriptor: PlatformModuleDescriptor) = descriptor.rootProjectDescriptor.externalDbDriversDirectory
        ?: File(descriptor.moduleRootDirectory, HybrisConstants.PLATFORM_DB_DRIVER)

    private fun getStandardSourceJarDirectory(descriptor: YModuleDescriptor) = if (ApplicationSettings.getInstance().withStandardProvidedSources) {
        val rootDescriptor = if (descriptor is YSubModuleDescriptor) descriptor.owner
        else descriptor

        val sourcesDirectory = File(rootDescriptor.moduleRootDirectory, HybrisConstants.DOC_SOURCES_JAR_PATH)
        if (sourcesDirectory.exists() && sourcesDirectory.isDirectory) arrayListOf(sourcesDirectory)
        else emptyList()
    } else emptyList()
}
