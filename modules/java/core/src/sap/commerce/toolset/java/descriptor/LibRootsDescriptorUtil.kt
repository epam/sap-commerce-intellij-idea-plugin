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
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

internal fun ModuleDescriptor.getLibraryDescriptors(importContext: ProjectImportContext): List<JavaLibraryDescriptor> = when (this) {
    is YRegularModuleDescriptor -> getLibraryDescriptors(importContext, this)
    is YWebSubModuleDescriptor -> getWebLibraryDescriptors(importContext, this)
    is YCommonWebSubModuleDescriptor -> getCommonWebSubModuleDescriptor(importContext, this)
    is YBackofficeSubModuleDescriptor -> getLibraryDescriptors(importContext, this)
    is YAcceleratorAddonSubModuleDescriptor -> getLibraryDescriptors(importContext, this)
    is YHacSubModuleDescriptor -> getLibraryDescriptors(importContext, this)
    is YHmcSubModuleDescriptor -> getLibraryDescriptors(importContext, this)
    is PlatformModuleDescriptor -> getLibraryDescriptors(importContext, this)
    is ConfigModuleDescriptor -> getLibraryDescriptors(importContext, this)
    is ExternalModuleDescriptor -> emptyList()
    else -> emptyList()
}

internal fun addBackofficeRootProjectLibrary(
    modifiableModelsProvider: IdeModifiableModelsProvider,
    libraryDirRoot: Path,
    sourcesDirRoot: Path? = null,
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

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.type, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    if (descriptor.extensionInfo.backofficeModule) {
        descriptor.getSubModules()
            .firstOrNull { it is YBackofficeSubModuleDescriptor }
            ?.let { yModule ->
                val attachSources = descriptor.type == ModuleDescriptorType.CUSTOM || !importContext.settings.importOOTBModulesInReadOnlyMode
                val sourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                    .map { yModule.moduleRootPath.resolve(it) }
                    .filter { it.directoryExists }

                libs.add(
                    JavaLibraryDescriptor(
                        name = "${descriptor.name} - Backoffice Classes",
                        libraryFile = yModule.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES),
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
            libraryFile = descriptor.moduleRootPath.resolve(ProjectConstants.Directory.LIB),
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
            libraryFile = descriptor.moduleRootPath.resolve(ProjectConstants.Paths.BACKOFFICE_BIN),
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
            libraryFile = descriptor.moduleRootPath.resolve(ProjectConstants.Directory.BIN),
            exported = true
        )
    )

    importContext.chosenHybrisModuleDescriptors
        .firstOrNull { it.name == EiConstants.Extension.HMC }
        ?.let {
            libs.add(
                JavaLibraryDescriptor(
                    name = "${descriptor.name} - Web Classes",
                    libraryFile = it.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES),
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
        .map { descriptor.moduleRootPath.resolve(it) }
        .filter { it.directoryExists }

    if (EiConstants.Extension.PLATFORM_SERVICES != descriptor.name) {
        libs.add(
            JavaLibraryDescriptor(
                name = "${descriptor.name} - compiler output",
                libraryFile = descriptor.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES),
                sourceFiles = sourceFiles,
                exported = true,
                directoryWithClasses = true
            )
        )
    }
    libs.add(
        JavaLibraryDescriptor(
            name = "${descriptor.name} - resources",
            libraryFile = descriptor.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES),
            exported = true,
            directoryWithClasses = true
        )
    )
}

private fun addServerLibs(descriptor: YModuleDescriptor, libs: MutableList<JavaLibraryDescriptor>) {
    // TODO: server jar may not present, example -> apparelstore, electronicsstore,
    val serverJars = descriptor.moduleRootPath.resolve(ProjectConstants.Directory.BIN)
        .takeIf { it.directoryExists }
        ?.listDirectoryEntries()
        ?.filter { it.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }
        ?.takeIf { it.isNotEmpty() }
        ?: return

    val sourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
        .map { descriptor.moduleRootPath.resolve(it) }
        .filter { it.directoryExists }

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

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.type, libs)
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

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.type, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    val platformDirectory = importContext.platformDirectory ?: return libs

    libs.add(
        JavaLibraryDescriptor(
            name = "${descriptor.name} - HAC Web Classes",
            libraryFile = platformDirectory.resolve(ProjectConstants.Paths.HAC_WEB_INF_CLASSES),
            directoryWithClasses = true
        )
    )

    return libs
}

private fun getLibraryDescriptors(importContext: ProjectImportContext, descriptor: YHmcSubModuleDescriptor): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.type, libs)
    addHmcLibs(importContext, descriptor, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    return libs
}

private fun getLibraryDescriptors(
    importContext: ProjectImportContext,
    descriptor: YAcceleratorAddonSubModuleDescriptor
): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.type, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    val attachSources = descriptor.type == ModuleDescriptorType.CUSTOM || !importContext.settings.importOOTBModulesInReadOnlyMode

    val allYModules = importContext.chosenHybrisModuleDescriptors
        .filterIsInstance<YModuleDescriptor>()
        .distinct()
        .associateBy { it.name }
    allYModules.values
        .filter { it.getDirectDependencies().contains(descriptor.owner) }
        .filter { it != descriptor }
        .forEach { yModule ->
            // process owner extension dependencies
            val addonSourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                .map { yModule.moduleRootPath.resolve(it) }
                .filter { it.directoryExists }

            libs.add(
                JavaLibraryDescriptor(
                    name = "${yModule.name} - Addon's Target Classes",
                    libraryFile = yModule.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES),
                    sourceFiles = if (attachSources) addonSourceFiles
                    else emptyList(),
                    directoryWithClasses = true
                )
            )
            libs.add(
                JavaLibraryDescriptor(
                    name = "${yModule.name} - Addon's Target Resources",
                    libraryFile = yModule.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES),
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

    addLibrariesToNonCustomModule(importContext, descriptor, descriptor.type, libs)
    addServerLibs(descriptor, libs)
    addRootLib(descriptor, libs)

    val sourceFiles = ProjectConstants.Directory.ALL_SRC_DIR_NAMES
        .map { descriptor.moduleRootPath.resolve(it) }
        .filter { it.directoryExists }
        .toMutableList()
    val testSourceFiles = ProjectConstants.Directory.TEST_SRC_DIR_NAMES
        .map { descriptor.moduleRootPath.resolve(it) }
        .filter { it.directoryExists }
        .toMutableList()

    listOf(
        descriptor.moduleRootPath.resolve(ProjectConstants.Directory.ADDON_SRC),
        descriptor.moduleRootPath.resolve(ProjectConstants.Directory.COMMON_WEB_SRC),
    )
        .filter { it.directoryExists }
        .flatMap { srcDir ->
            srcDir.listDirectoryEntries()
                .filter { it.directoryExists }
        }
        .forEach { sourceFiles.add(it) }

    if (descriptor.owner.name == EiConstants.Extension.BACK_OFFICE) return libs

    if (descriptor.type != ModuleDescriptorType.CUSTOM && descriptor is YWebSubModuleDescriptor) {
        libs.add(
            JavaLibraryDescriptor(
                name = "${descriptor.name} - $libName Classes",
                libraryFile = descriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES),
                sourceFiles = sourceFiles,
                exported = true,
                directoryWithClasses = true
            )
        )
        libs.add(
            JavaLibraryDescriptor(
                name = "${descriptor.name} - Test Classes",
                libraryFile = descriptor.moduleRootPath.resolve(ProjectConstants.Directory.TEST_CLASSES),
                sourceFiles = testSourceFiles,
                exported = true,
                directoryWithClasses = true,
                scope = DependencyScope.TEST
            )
        )
    }

    descriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_LIB)
        .takeIf { it.directoryExists }
        ?.let { libFolder ->
            libs.add(
                JavaLibraryDescriptor(
                    name = "${descriptor.name} - $libName Library",
                    libraryFile = libFolder,
                    jarFiles = libFolder.listDirectoryEntries()
                        .filter { it.name.endsWith(".jar") }
                        .toSet(),
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
                .map { dir -> descriptor.moduleRootPath.resolve(dir) }
                .filter { dir -> dir.directoryExists }
            libs.add(
                JavaLibraryDescriptor(
                    name = "${it.name} - $libName Classes",
                    libraryFile = it.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES),
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
        libraryFile = descriptor.moduleRootPath.resolve(ProjectConstants.Directory.LICENCE),
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
    ?: descriptor.moduleRootPath.resolve(ProjectConstants.Paths.LIB_DB_DRIVER)

private fun getStandardSourceJarDirectory(descriptor: YModuleDescriptor) = if (ApplicationSettings.getInstance().withStandardProvidedSources) {
    val rootDescriptor = if (descriptor is YSubModuleDescriptor) descriptor.owner
    else descriptor

    rootDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.DOC_SOURCES)
        .takeIf { it.directoryExists }
        ?.let { listOf(it) }
        ?: emptyList()
} else emptyList()