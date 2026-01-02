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
import com.intellij.util.asSafely
import com.intellij.util.containers.addIfNotNull
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.*
import sap.commerce.toolset.project.descriptor.impl.*
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

internal fun ModuleDescriptor.collectLibraryDescriptors(importContext: ProjectImportContext): Collection<JavaLibraryDescriptor> = when (this) {
    is YRegularModuleDescriptor -> this.libs(importContext)
    is YWebSubModuleDescriptor -> this.webLibs(importContext)
    is YCommonWebSubModuleDescriptor -> this.commonWebLibs(importContext)
    is YBackofficeSubModuleDescriptor -> this.libs(importContext)
    is YAcceleratorAddonSubModuleDescriptor -> this.libs(importContext)
    is YHacSubModuleDescriptor -> this.libs(importContext)
    is YHmcSubModuleDescriptor -> this.libs(importContext)
    is PlatformModuleDescriptor -> this.libs(importContext)
    is ConfigModuleDescriptor -> this.libs()
    else -> emptyList()
}

internal fun addBackofficeRootProjectLibrary(
    importContext: ProjectImportContext,
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
        if (sourcesDirRoot != null && importContext.settings.withStandardProvidedSources) {
            libraryModel.addJarDirectory(VfsUtil.getUrlForLibraryRoot(sourcesDirRoot), true, OrderRootType.SOURCES)
        }
    }
}

private fun YRegularModuleDescriptor.libs(importContext: ProjectImportContext) = buildList {
    add(this@libs.rootLib)

    addAll(this@libs.nonCustomModuleLibs(importContext))
    addAll(this@libs.serverLibs(importContext))

    addIfNotNull(this@libs.backofficeClassesLib(importContext))
}

private fun YRegularModuleDescriptor.backofficeClassesLib(importContext: ProjectImportContext) = this.getSubModules()
    .takeIf { this.extensionInfo.backofficeModule }
    ?.firstOrNull { it is YBackofficeSubModuleDescriptor }
    ?.let { yModule ->
        val attachSources = this.type == ModuleDescriptorType.CUSTOM || !importContext.settings.importOOTBModulesInReadOnlyMode
        val sourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
            .map { yModule.moduleRootPath.resolve(it) }
            .filter { it.directoryExists }

        JavaLibraryDescriptor(
            name = "${this.name} - Backoffice Classes",
            libraryFile = yModule.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES),
            sourceFiles = if (attachSources) sourceFiles
            else emptyList(),
            directoryWithClasses = true,
            scope = DependencyScope.PROVIDED
        )
    }

private fun YHmcSubModuleDescriptor.hmcLibs(importContext: ProjectImportContext) = buildList {
    add(
        JavaLibraryDescriptor(
            name = "${this@hmcLibs.name} - HMC Bin",
            libraryFile = this@hmcLibs.moduleRootPath.resolve(ProjectConstants.Directory.BIN),
            exported = true
        )
    )

    importContext.chosenHybrisModuleDescriptors
        .firstOrNull { it.name == EiConstants.Extension.HMC }
        ?.let {
            JavaLibraryDescriptor(
                name = "${this@hmcLibs.name} - Web Classes",
                libraryFile = it.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES),
                directoryWithClasses = true
            )
        }
        ?.let { add(it) }
}

private val YModuleDescriptor.rootLib
    get() = JavaLibraryDescriptor(
        name = "${this.name} - lib",
        libraryFile = this.moduleRootPath.resolve(ProjectConstants.Directory.LIB),
        exported = true,
        descriptorType = LibraryDescriptorType.LIB
    )

private fun YModuleDescriptor.nonCustomModuleLibs(importContext: ProjectImportContext): Collection<JavaLibraryDescriptor> {
    if (!importContext.settings.importOOTBModulesInReadOnlyMode) return emptyList()
    if (this.type == ModuleDescriptorType.CUSTOM) return emptyList()

    val sourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
        .map { this.moduleRootPath.resolve(it) }
        .filter { it.directoryExists }

    return buildList {
        if (EiConstants.Extension.PLATFORM_SERVICES != this@nonCustomModuleLibs.name) {
            add(
                JavaLibraryDescriptor(
                    name = "${this@nonCustomModuleLibs.name} - compiler output",
                    libraryFile = this@nonCustomModuleLibs.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES),
                    sourceFiles = sourceFiles,
                    exported = true,
                    directoryWithClasses = true
                )
            )
        }

        add(
            JavaLibraryDescriptor(
                name = "${this@nonCustomModuleLibs.name} - resources",
                libraryFile = this@nonCustomModuleLibs.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES),
                exported = true,
                directoryWithClasses = true
            )
        )
    }
}

// TODO: server jar may not present, example -> apparelstore, electronicsstore, core
private fun YModuleDescriptor.serverLibs(importContext: ProjectImportContext): Collection<JavaLibraryDescriptor> {
    val serverJars = this.moduleRootPath.resolve(ProjectConstants.Directory.BIN)
        .takeIf { it.directoryExists }
        ?.listDirectoryEntries()
        ?.filter { it.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }
        ?.takeIf { it.isNotEmpty() }
        ?: return emptyList()

    val sourceFiles = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
        .map { this.moduleRootPath.resolve(it) }
        .filter { it.directoryExists }

    // Attach standard sources to server jar
    val sourceJarDirectories = this.getStandardSourceJarDirectory(importContext)

    return serverJars
        .map { serverJar ->
            JavaLibraryDescriptor(
                name = "${this.name} - server",
                libraryFile = serverJar,
                sourceFiles = sourceFiles,
                sourceJarDirectories = sourceJarDirectories,
                exported = true,
                directoryWithClasses = true
            )
        }
}

private fun YBackofficeSubModuleDescriptor.libs(importContext: ProjectImportContext) = buildList {
    add(this@libs.rootLib)
    add(this@libs.backofficeLib())

    addAll(this@libs.nonCustomModuleLibs(importContext))
    addAll(this@libs.serverLibs(importContext))
}

private fun YBackofficeSubModuleDescriptor.backofficeLib() = JavaLibraryDescriptor(
    name = "${this.name} - Backoffice lib",
    libraryFile = this.moduleRootPath.resolve(ProjectConstants.Paths.BACKOFFICE_BIN),
    exported = true
)

/**
 * https://hybris-integration.atlassian.net/browse/IIP-355
 * HAC addons can not be compiled correctly by Intellij build because
 * "hybris/bin/platform/ext/hac/web/webroot/WEB-INF/classes" from "hac" extension is not registered
 * as a dependency for HAC addons.
 */
private fun YHacSubModuleDescriptor.libs(importContext: ProjectImportContext) = buildList {
    add(this@libs.rootLib)

    addAll(this@libs.nonCustomModuleLibs(importContext))
    addAll(this@libs.serverLibs(importContext))

    importContext.platformDirectory.resolve(ProjectConstants.Paths.HAC_WEB_INF_CLASSES)
        .takeIf { it.directoryExists }
        ?.let {
            add(
                JavaLibraryDescriptor(
                    name = "${this@libs.name} - HAC Web Classes",
                    libraryFile = it,
                    directoryWithClasses = true
                )
            )
        }
}

private fun YHmcSubModuleDescriptor.libs(importContext: ProjectImportContext) = buildList {
    add(this@libs.rootLib)

    addAll(this@libs.nonCustomModuleLibs(importContext))
    addAll(this@libs.hmcLibs(importContext))
    addAll(this@libs.serverLibs(importContext))
}

private fun YAcceleratorAddonSubModuleDescriptor.libs(importContext: ProjectImportContext): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    libs.addAll(this.nonCustomModuleLibs(importContext))
    libs.addAll(this.serverLibs(importContext))
    libs.add(this.rootLib)

    val attachSources = this.type == ModuleDescriptorType.CUSTOM || !importContext.settings.importOOTBModulesInReadOnlyMode

    val allYModules = importContext.chosenHybrisModuleDescriptors
        .filterIsInstance<YModuleDescriptor>()
        .distinct()
        .associateBy { it.name }
    allYModules.values
        .filter { it.getDirectDependencies().contains(this.owner) }
        .filter { it != this }
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

private fun YSubModuleDescriptor.webLibs(importContext: ProjectImportContext): Collection<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    libs.addAll(this.nonCustomModuleLibs(importContext))
    libs.addAll(this.serverLibs(importContext))
    libs.add(this.rootLib)

    val sourceFiles = ProjectConstants.Directory.ALL_SRC_DIR_NAMES
        .map { this.moduleRootPath.resolve(it) }
        .filter { it.directoryExists }
        .toMutableList()
    val testSourceFiles = ProjectConstants.Directory.TEST_SRC_DIR_NAMES
        .map { this.moduleRootPath.resolve(it) }
        .filter { it.directoryExists }
        .toMutableList()

    listOf(
        this.moduleRootPath.resolve(ProjectConstants.Directory.ADDON_SRC),
        this.moduleRootPath.resolve(ProjectConstants.Directory.COMMON_WEB_SRC),
    )
        .filter { it.directoryExists }
        .flatMap { srcDir ->
            srcDir.listDirectoryEntries()
                .filter { it.directoryExists }
        }
        .forEach { sourceFiles.add(it) }

    if (this.owner.name == EiConstants.Extension.BACK_OFFICE) return libs

    if (this.type != ModuleDescriptorType.CUSTOM && this is YWebSubModuleDescriptor) {
        libs.add(
            JavaLibraryDescriptor(
                name = "${this.name} - Web Classes",
                libraryFile = this.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES),
                sourceFiles = sourceFiles,
                exported = true,
                directoryWithClasses = true
            )
        )
        libs.add(
            JavaLibraryDescriptor(
                name = "${this.name} - Test Classes",
                libraryFile = this.moduleRootPath.resolve(ProjectConstants.Directory.TEST_CLASSES),
                sourceFiles = testSourceFiles,
                exported = true,
                directoryWithClasses = true,
                scope = DependencyScope.TEST
            )
        )
    }

    this.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_LIB)
        .takeIf { it.directoryExists }
        ?.let { libFolder ->
            libs.add(
                JavaLibraryDescriptor(
                    name = "${this.name} - Web Library",
                    libraryFile = libFolder,
                    jarFiles = libFolder.listDirectoryEntries()
                        .filter { it.name.endsWith(".jar") }
                        .toSet(),
                    sourceJarDirectories = this.getStandardSourceJarDirectory(importContext),
                    exported = true,
                    descriptorType = LibraryDescriptorType.WEB_INF_LIB
                )
            )
        }
    return libs.toList()
}

private fun YCommonWebSubModuleDescriptor.commonWebLibs(importContext: ProjectImportContext) = buildList {
    val classesLibs = this@commonWebLibs
        .dependantWebExtensions
        .map {
            val webSourceFiles = ProjectConstants.Directory.ALL_SRC_DIR_NAMES
                .map { dir -> this@commonWebLibs.moduleRootPath.resolve(dir) }
                .filter { dir -> dir.directoryExists }
            JavaLibraryDescriptor(
                name = "${it.name} - Common Web Classes",
                libraryFile = it.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES),
                sourceFiles = webSourceFiles,
                exported = true,
                directoryWithClasses = true
            )
        }

    addAll(this@commonWebLibs.webLibs(importContext))
    addAll(classesLibs)
}

private fun ConfigModuleDescriptor.libs() = listOf(
    JavaLibraryDescriptor(
        name = "Config License",
        libraryFile = this.moduleRootPath.resolve(ProjectConstants.Directory.LICENCE),
        exported = true
    )
)

private fun PlatformModuleDescriptor.libs(importContext: ProjectImportContext): List<JavaLibraryDescriptor> {
    val dbDriversPath = (importContext.externalDbDriversDirectory
        ?: this.moduleRootPath.resolve(ProjectConstants.Paths.LIB_DB_DRIVER))
    return listOf(
        JavaLibraryDescriptor(
            name = HybrisConstants.PLATFORM_DATABASE_DRIVER_LIBRARY,
            libraryFile = dbDriversPath,
            exported = true,
        )
    )
}

private fun YModuleDescriptor.getStandardSourceJarDirectory(importContext: ProjectImportContext) = if (importContext.settings.withStandardProvidedSources) {
    val rootDescriptor = this.asSafely<YSubModuleDescriptor>()?.owner
        ?: this

    rootDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.DOC_SOURCES)
        .takeIf { it.directoryExists }
        ?.let { listOf(it) }
        ?: emptyList()
} else emptyList()