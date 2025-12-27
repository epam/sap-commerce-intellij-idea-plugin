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

package sap.commerce.toolset.java.descriptor

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel
import com.intellij.openapi.vfs.VfsUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.*
import sap.commerce.toolset.project.descriptor.impl.*
import sap.commerce.toolset.settings.ApplicationSettings
import java.io.File

internal fun getLibraryDescriptors(importContext: ProjectImportContext, descriptor: ModuleDescriptor, allYModules: Map<String, YModuleDescriptor>): List<JavaLibraryDescriptor> =
    when (descriptor) {
        is YRegularModuleDescriptor -> getLibraryDescriptors(importContext, descriptor)
        is YWebSubModuleDescriptor -> getWebLibraryDescriptors(importContext, descriptor)
        is YCommonWebSubModuleDescriptor -> getCommonWebSubModuleDescriptor(importContext, descriptor)
        is YBackofficeSubModuleDescriptor -> getLibraryDescriptors(importContext, descriptor)
        is YAcceleratorAddonSubModuleDescriptor -> getLibraryDescriptors(importContext, descriptor, allYModules)
        is YHacSubModuleDescriptor -> getLibraryDescriptors(importContext, descriptor)
        is YHmcSubModuleDescriptor -> getLibraryDescriptors(importContext, descriptor)
        is PlatformModuleDescriptor -> getLibraryDescriptors(importContext, descriptor)
        is ConfigModuleDescriptor -> getLibraryDescriptors(importContext, descriptor)
        is ExternalModuleDescriptor -> emptyList()
        else -> emptyList()
    }

internal fun addBackofficeRootProjectLibrary(
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

private fun getLibraryDescriptors(importContext: ProjectImportContext, descriptor: YRegularModuleDescriptor): List<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.descriptorType, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    if (descriptor.extensionInfo.backofficeModule) {
        descriptor.getSubModules()
            .firstOrNull { it is YBackofficeSubModuleDescriptor }
            ?.let { yModule ->
                val attachSources = descriptor.descriptorType == ModuleDescriptorType.CUSTOM || !importContext.settings.importOOTBModulesInReadOnlyMode
                val sourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                    .map { File(yModule.moduleRootDirectory, it) }
                    .filter { it.isDirectory }

                libs.add(
                    JavaLibraryDescriptor(
                        name = "${descriptor.name} - Backoffice Classes",
                        libraryFile = File(yModule.moduleRootDirectory, ProjectConstants.Directory.CLASSES),
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
            libraryFile = File(descriptor.moduleRootDirectory, ProjectConstants.Directory.LIB),
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
    importContext: ProjectImportContext,
    descriptor: YHmcSubModuleDescriptor,
    libs: MutableList<JavaLibraryDescriptor>
) {
    libs.add(
        JavaLibraryDescriptor(
            name = "${descriptor.name} - HMC Bin",
            libraryFile = File(descriptor.moduleRootDirectory, ProjectConstants.Directory.BIN),
            exported = true
        )
    )

    importContext.chosenHybrisModuleDescriptors
        .firstOrNull { it.name == EiConstants.Extension.HMC }
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
    importContext: ProjectImportContext,
    descriptor: YModuleDescriptor,
    descriptorType: ModuleDescriptorType?,
    libs: MutableList<JavaLibraryDescriptor>
) {
    if (!importContext.settings.importOOTBModulesInReadOnlyMode) return
    if (descriptorType == ModuleDescriptorType.CUSTOM) return

    val sourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
        .map { File(descriptor.moduleRootDirectory, it) }
        .filter { it.isDirectory }

    if (EiConstants.Extension.PLATFORM_SERVICES != descriptor.name) {
        libs.add(
            JavaLibraryDescriptor(
                name = "${descriptor.name} - compiler output",
                libraryFile = File(descriptor.moduleRootDirectory, ProjectConstants.Directory.CLASSES),
                sourceFiles = sourceFiles,
                exported = true,
                directoryWithClasses = true
            )
        )
    }
    libs.add(
        JavaLibraryDescriptor(
            name = "${descriptor.name} - resources",
            libraryFile = File(descriptor.moduleRootDirectory, ProjectConstants.Directory.RESOURCES),
            exported = true,
            directoryWithClasses = true
        )
    )
}

private fun addServerLibs(descriptor: YModuleDescriptor, libs: MutableList<JavaLibraryDescriptor>) {
    val binDir = File(descriptor.moduleRootDirectory, ProjectConstants.Directory.BIN)
        .takeIf { it.isDirectory }
        ?: return
    // TODO: server jar may not present, example -> apparelstore, electronicsstore,
    val serverJars = binDir
        .listFiles { _, name: String -> name.endsWith(HybrisConstants.HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX) }
        ?.takeIf { it.isNotEmpty() }
        ?: return

    val sourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
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

private fun getLibraryDescriptors(importContext: ProjectImportContext, descriptor: YBackofficeSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.descriptorType, libs)
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
private fun getLibraryDescriptors(importContext: ProjectImportContext, descriptor: YHacSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.descriptorType, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    libs.add(
        JavaLibraryDescriptor(
            name = "${descriptor.name} - HAC Web Classes",
            libraryFile = File(importContext.platformDirectory, HybrisConstants.HAC_WEB_INF_CLASSES),
            directoryWithClasses = true
        )
    )

    return libs
}

private fun getLibraryDescriptors(importContext: ProjectImportContext, descriptor: YHmcSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.descriptorType, libs)
    addHmcLibs(importContext, descriptor, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    return libs
}

private fun getLibraryDescriptors(
    importContext: ProjectImportContext,
    descriptor: YAcceleratorAddonSubModuleDescriptor,
    allYModules: Map<String, YModuleDescriptor>
): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.descriptorType, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    val attachSources = descriptor.descriptorType == ModuleDescriptorType.CUSTOM || !importContext.settings.importOOTBModulesInReadOnlyMode
    allYModules.values
        .filter { it.getDirectDependencies().contains(descriptor.owner) }
        .filter { it != descriptor }
        .forEach { yModule ->
            // process owner extension dependencies
            val addonSourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                .map { File(yModule.moduleRootDirectory, it) }
                .filter { it.isDirectory }

            libs.add(
                JavaLibraryDescriptor(
                    name = "${yModule.name} - Addon's Target Classes",
                    libraryFile = File(yModule.moduleRootDirectory, ProjectConstants.Directory.CLASSES),
                    sourceFiles = if (attachSources) addonSourceFiles
                    else emptyList(),
                    directoryWithClasses = true
                )
            )
            libs.add(
                JavaLibraryDescriptor(
                    name = "${yModule.name} - Addon's Target Resources",
                    libraryFile = File(yModule.moduleRootDirectory, ProjectConstants.Directory.RESOURCES),
                    directoryWithClasses = true
                )
            )
        }
    return libs
}

private fun getWebLibraryDescriptors(
    importContext: ProjectImportContext,
    descriptor: YSubModuleDescriptor,
    libName: String = "Web"
): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.descriptorType, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    val libFolder = File(descriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_LIB_PATH)
    val sourceFiles = ProjectConstants.Directory.ALL_SRC_DIR_NAMES
        .map { File(descriptor.moduleRootDirectory, it) }
        .filter { it.isDirectory }
        .toMutableList()
    val testSourceFiles = ProjectConstants.Directory.TEST_SRC_DIR_NAMES
        .map { File(descriptor.moduleRootDirectory, it) }
        .filter { it.isDirectory }
        .toMutableList()

    listOf(
        File(descriptor.moduleRootDirectory, ProjectConstants.Directory.ADDON_SRC),
        File(descriptor.moduleRootDirectory, ProjectConstants.Directory.COMMON_WEB_SRC),
    )
        .filter { it.isDirectory }
        .mapNotNull { srcDir ->
            srcDir.listFiles { it: File -> it.isDirectory }
                ?.toList()
        }
        .flatten()
        .forEach { sourceFiles.add(it) }

    if (descriptor.owner.name != EiConstants.Extension.BACK_OFFICE) {
        if (descriptor.descriptorType != ModuleDescriptorType.CUSTOM && descriptor is YWebSubModuleDescriptor) {
            libs.add(
                JavaLibraryDescriptor(
                    name = "${descriptor.name} - $libName Classes",
                    libraryFile = File(descriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_CLASSES_PATH),
                    sourceFiles = sourceFiles,
                    exported = true,
                    directoryWithClasses = true
                )
            )
            libs.add(
                JavaLibraryDescriptor(
                    name = "${descriptor.name} - Test Classes",
                    libraryFile = File(descriptor.moduleRootDirectory, ProjectConstants.Directory.TEST_CLASSES),
                    sourceFiles = testSourceFiles,
                    exported = true,
                    directoryWithClasses = true,
                    scope = DependencyScope.TEST
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
    importContext: ProjectImportContext,
    descriptor: YCommonWebSubModuleDescriptor,
    libName: String = "Common Web"
): MutableList<JavaLibraryDescriptor> {
    val libs = getWebLibraryDescriptors(importContext, descriptor, libName)

    descriptor
        .dependantWebExtensions
        .forEach {
            val webSourceFiles = ProjectConstants.Directory.ALL_SRC_DIR_NAMES
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

private fun getLibraryDescriptors(importContext: ProjectImportContext, descriptor: ConfigModuleDescriptor) = listOf(
    JavaLibraryDescriptor(
        name = "Config License",
        libraryFile = File(descriptor.moduleRootDirectory, ProjectConstants.Directory.LICENCE),
        exported = true
    )
)

private fun getLibraryDescriptors(importContext: ProjectImportContext, descriptor: PlatformModuleDescriptor) = listOf(
    JavaLibraryDescriptor(
        name = HybrisConstants.PLATFORM_DATABASE_DRIVER_LIBRARY,
        libraryFile = getDbDriversDirectory(importContext, descriptor),
        exported = true,
    )
)

private fun getDbDriversDirectory(importContext: ProjectImportContext, descriptor: PlatformModuleDescriptor) = importContext.externalDbDriversDirectory
    ?: File(descriptor.moduleRootDirectory, HybrisConstants.PLATFORM_DB_DRIVER)

private fun getStandardSourceJarDirectory(descriptor: YModuleDescriptor) = if (ApplicationSettings.getInstance().withStandardProvidedSources) {
    val rootDescriptor = if (descriptor is YSubModuleDescriptor) descriptor.owner
    else descriptor

    val sourcesDirectory = File(rootDescriptor.moduleRootDirectory, HybrisConstants.DOC_SOURCES_JAR_PATH)
    if (sourcesDirectory.exists() && sourcesDirectory.isDirectory) arrayListOf(sourcesDirectory)
    else emptyList()
} else emptyList()