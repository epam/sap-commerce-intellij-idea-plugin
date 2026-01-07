/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.java.configurator.library

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.DependencyScope
import com.intellij.platform.workspace.jps.entities.LibraryRoot
import com.intellij.platform.workspace.jps.entities.LibraryRoot.InclusionOptions
import com.intellij.platform.workspace.jps.entities.LibraryRootTypeId
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.util.asSafely
import com.intellij.util.containers.addIfNotNull
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.java.configurator.contentEntry.isCustomModuleDescriptor
import sap.commerce.toolset.java.descriptor.JavaLibraryDescriptor
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.*
import sap.commerce.toolset.project.descriptor.impl.*
import sap.commerce.toolset.project.fromJar
import sap.commerce.toolset.project.fromPath
import sap.commerce.toolset.util.directoryExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

// TODO: Split this to LibraryDescriptors provider

internal fun ModuleDescriptor.collectLibraryDescriptors(importContext: ProjectImportContext, workspaceModel: WorkspaceModel): Collection<JavaLibraryDescriptor> = when (this) {
    is YRegularModuleDescriptor -> this.libs(importContext, workspaceModel.getVirtualFileUrlManager())
    is YWebSubModuleDescriptor -> this.webLibs(importContext, workspaceModel.getVirtualFileUrlManager())
    is YCommonWebSubModuleDescriptor -> this.commonWebLibs(importContext, workspaceModel.getVirtualFileUrlManager())
    is YBackofficeSubModuleDescriptor -> this.libs(importContext, workspaceModel.getVirtualFileUrlManager())
    is YAcceleratorAddonSubModuleDescriptor -> this.libs(importContext, workspaceModel.getVirtualFileUrlManager())
    is YHacSubModuleDescriptor -> this.libs(importContext, workspaceModel.getVirtualFileUrlManager())
    is YHmcSubModuleDescriptor -> this.libs(importContext, workspaceModel.getVirtualFileUrlManager())
    is PlatformModuleDescriptor -> this.libs(importContext, workspaceModel.getVirtualFileUrlManager())
    else -> emptyList()
}

private fun YRegularModuleDescriptor.libs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager) = buildList {
    addAll(this@libs.nonCustomModuleLibs(importContext, virtualFileUrlManager))

    addIfNotNull(this@libs.rootLib(virtualFileUrlManager))
    addIfNotNull(this@libs.serverLibs(importContext, virtualFileUrlManager))
    addIfNotNull(this@libs.backofficeClassesLib(importContext, virtualFileUrlManager))
}

private fun YRegularModuleDescriptor.backofficeClassesLib(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager) = this.getSubModules()
    .takeIf { this.extensionInfo.backofficeModule }
    ?.firstOrNull { it is YBackofficeSubModuleDescriptor }
    ?.let { yModule ->
        val attachSources = this.type == ModuleDescriptorType.CUSTOM || importContext.settings.importOOTBModulesInWriteMode
        JavaLibraryDescriptor(
            name = "${this.name} - Backoffice Classes",
            scope = DependencyScope.PROVIDED,
            libraryRoots = buildList {
                virtualFileUrlManager.fromPath(yModule.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES))
                    ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                    ?.let { add(it) }

                if (attachSources) (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                    .mapNotNull { virtualFileUrlManager.fromPath(yModule.moduleRootPath.resolve(it)) }
                    .map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                    .forEach { add(it) }
            },
        )
    }

private fun YHmcSubModuleDescriptor.hmcLibs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager) = buildList {
    virtualFileUrlManager.fromPath(this@hmcLibs.moduleRootPath.resolve(ProjectConstants.Directory.BIN))
        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
        ?.let {
            JavaLibraryDescriptor(
                name = "${this@hmcLibs.name} - HMC Bin",
                exported = true,
                libraryRoots = listOf(it)
            )
        }
        ?.let { add(it) }

    importContext.chosenHybrisModuleDescriptors
        .firstOrNull { it.name == EiConstants.Extension.HMC }
        ?.let { virtualFileUrlManager.fromPath(it.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES)) }
        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
        ?.let {
            JavaLibraryDescriptor(
                name = "${this@hmcLibs.name} - Web Classes",
                libraryRoots = listOf(it),
            )
        }
        ?.let { add(it) }
}

private fun YModuleDescriptor.rootLib(virtualFileUrlManager: VirtualFileUrlManager): JavaLibraryDescriptor? {
    val libraryRoot = virtualFileUrlManager.fromPath(this.moduleRootPath.resolve(ProjectConstants.Directory.LIB))
        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED, InclusionOptions.ARCHIVES_UNDER_ROOT) }
        ?: return null

    return JavaLibraryDescriptor(
        name = "${this.name} - lib",
        exported = true,
        libraryRoots = listOf(libraryRoot)
    )
}

private fun YModuleDescriptor.nonCustomModuleLibs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager): Collection<JavaLibraryDescriptor> {
    if (this.type == ModuleDescriptorType.CUSTOM) return emptyList()
    if (importContext.settings.importOOTBModulesInWriteMode) return emptyList()

    return buildList {
        val moduleRootPath = this@nonCustomModuleLibs.moduleRootPath

        if (EiConstants.Extension.PLATFORM_SERVICES != this@nonCustomModuleLibs.name) {
            add(
                JavaLibraryDescriptor(
                    name = "${this@nonCustomModuleLibs.name} - compiler output",
                    exported = true,
                    libraryRoots = buildList {
                        virtualFileUrlManager.fromPath(moduleRootPath.resolve(ProjectConstants.Directory.CLASSES))
                            ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                            ?.let { add(it) }

                        (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                            .mapNotNull { virtualFileUrlManager.fromPath(moduleRootPath.resolve(it)) }
                            .map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                            .forEach { add(it) }
                    }
                )
            )
        }

        virtualFileUrlManager.fromPath(moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES))
            ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
            ?.let {
                JavaLibraryDescriptor(
                    name = "${this@nonCustomModuleLibs.name} - resources",
                    exported = true,
                    libraryRoots = listOf(it)
                )
            }
            ?.let { add(it) }
    }
}

private fun YModuleDescriptor.serverLibs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager): JavaLibraryDescriptor? {
    if (this.isCustomModuleDescriptor) return null

    // Attach standard sources to server jar
    return JavaLibraryDescriptor(
        name = "${this.name} - server",
        exported = true,
        libraryRoots = buildList {
            val moduleRootPath = this@serverLibs.moduleRootPath

            virtualFileUrlManager.fromPath(moduleRootPath.resolve(ProjectConstants.Directory.CLASSES))
                ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                ?.let { add(it) }

            moduleRootPath.resolve(ProjectConstants.Directory.BIN)
                .takeIf { it.directoryExists }
                ?.listDirectoryEntries()
                ?.filter { it.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }
                ?.mapNotNull { virtualFileUrlManager.fromJar(it) }
                ?.map { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                ?.forEach { add(it) }

            (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                .mapNotNull { virtualFileUrlManager.fromPath(moduleRootPath.resolve(it)) }
                .map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                .forEach { add(it) }

            this@serverLibs.getStandardSourceJarDirectory(importContext)
                .mapNotNull { virtualFileUrlManager.fromPath(it) }
                .map { LibraryRoot(it, LibraryRootTypeId.SOURCES, InclusionOptions.ARCHIVES_UNDER_ROOT) }
                .forEach { add(it) }
        }
    )
}

private fun YBackofficeSubModuleDescriptor.libs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager) = buildList {
    addAll(this@libs.nonCustomModuleLibs(importContext, virtualFileUrlManager))

    addIfNotNull(this@libs.rootLib(virtualFileUrlManager))
    addIfNotNull(this@libs.backofficeLib(virtualFileUrlManager))
    addIfNotNull(this@libs.serverLibs(importContext, virtualFileUrlManager))
}

private fun YBackofficeSubModuleDescriptor.backofficeLib(virtualFileUrlManager: VirtualFileUrlManager): JavaLibraryDescriptor? {
    val libraryRoot = virtualFileUrlManager.fromPath(this.moduleRootPath.resolve(ProjectConstants.Paths.BACKOFFICE_BIN))
        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
        ?: return null

    return JavaLibraryDescriptor(
        name = "${this.name} - Backoffice lib",
        exported = true,
        libraryRoots = listOf(libraryRoot)
    )
}

/**
 * https://hybris-integration.atlassian.net/browse/IIP-355
 * HAC addons can not be compiled correctly by Intellij build because
 * "hybris/bin/platform/ext/hac/web/webroot/WEB-INF/classes" from "hac" extension is not registered
 * as a dependency for HAC addons.
 */
private fun YHacSubModuleDescriptor.libs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager) = buildList {
    addAll(this@libs.nonCustomModuleLibs(importContext, virtualFileUrlManager))

    addIfNotNull(this@libs.rootLib(virtualFileUrlManager))
    addIfNotNull(this@libs.serverLibs(importContext, virtualFileUrlManager))

    virtualFileUrlManager.fromPath(importContext.platformDirectory.resolve(ProjectConstants.Paths.HAC_WEB_INF_CLASSES))
        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
        ?.let {
            JavaLibraryDescriptor(
                name = "${this@libs.name} - HAC Web Classes",
                libraryRoots = listOf(it)
            )
        }
        ?.let { add(it) }
}

private fun YHmcSubModuleDescriptor.libs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager) = buildList {
    addAll(this@libs.nonCustomModuleLibs(importContext, virtualFileUrlManager))
    addAll(this@libs.hmcLibs(importContext, virtualFileUrlManager))

    addIfNotNull(this@libs.rootLib(virtualFileUrlManager))
    addIfNotNull(this@libs.serverLibs(importContext, virtualFileUrlManager))
}

private fun YAcceleratorAddonSubModuleDescriptor.libs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    libs.addAll(this.nonCustomModuleLibs(importContext, virtualFileUrlManager))
    libs.addIfNotNull(this.serverLibs(importContext, virtualFileUrlManager))
    libs.addIfNotNull(this.rootLib(virtualFileUrlManager))

    val attachSources = this.type == ModuleDescriptorType.CUSTOM || importContext.settings.importOOTBModulesInWriteMode

    val allYModules = importContext.chosenHybrisModuleDescriptors
        .filterIsInstance<YModuleDescriptor>()
        .distinct()
        .associateBy { it.name }
    allYModules.values
        .filter { it.getDirectDependencies().contains(this.owner) }
        .filter { it != this }
        .forEach { yModule ->
            // process owner extension dependencies

            libs.add(
                JavaLibraryDescriptor(
                    name = "${yModule.name} - Addon's Target Classes",
                    libraryRoots = buildList {
                        virtualFileUrlManager.fromPath(yModule.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES))
                            ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                            ?.let { add(it) }

                        if (attachSources) {
                            (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                                .mapNotNull { virtualFileUrlManager.fromPath(yModule.moduleRootPath.resolve(it)) }
                                .map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                                .forEach { add(it) }
                        }
                    }
                )
            )

            virtualFileUrlManager.fromPath(yModule.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES))
                ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                ?.let {
                    JavaLibraryDescriptor(
                        name = "${yModule.name} - Addon's Target Resources",
                        libraryRoots = listOf(it)
                    )
                }
                ?.let { libs.add(it) }
        }
    return libs
}

private fun YSubModuleDescriptor.webLibs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager): Collection<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    libs.addAll(this.nonCustomModuleLibs(importContext, virtualFileUrlManager))
    libs.addIfNotNull(this.serverLibs(importContext, virtualFileUrlManager))
    libs.addIfNotNull(this.rootLib(virtualFileUrlManager))

    if (this.owner.name == EiConstants.Extension.BACK_OFFICE) return libs

    if (this.type != ModuleDescriptorType.CUSTOM && this is YWebSubModuleDescriptor) {
        libs.add(
            JavaLibraryDescriptor(
                name = "${this.name} - Web Classes",
                exported = true,
                libraryRoots = buildList {
                    virtualFileUrlManager.fromPath(this@webLibs.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES))
                        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                        ?.let { add(it) }

                    ProjectConstants.Directory.ALL_SRC_DIR_NAMES
                        .mapNotNull { virtualFileUrlManager.fromPath(this@webLibs.moduleRootPath.resolve(it)) }
                        .map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                        .forEach { add(it) }

                    listOf(
                        this@webLibs.moduleRootPath.resolve(ProjectConstants.Directory.ADDON_SRC),
                        this@webLibs.moduleRootPath.resolve(ProjectConstants.Directory.COMMON_WEB_SRC),
                    )
                        .filter { it.directoryExists }
                        .flatMap { it.listDirectoryEntries() }
                        .mapNotNull { virtualFileUrlManager.fromPath(it) }
                        .map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                        .forEach { add(it) }
                }
            )
        )

        libs.add(
            JavaLibraryDescriptor(
                name = "${this.name} - Test Classes",
                exported = true,
                scope = DependencyScope.TEST,
                libraryRoots = buildList {
                    virtualFileUrlManager.fromPath(this@webLibs.moduleRootPath.resolve(ProjectConstants.Directory.TEST_CLASSES))
                        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                        ?.let { add(it) }

                    ProjectConstants.Directory.TEST_SRC_DIR_NAMES
                        .mapNotNull { virtualFileUrlManager.fromPath(this@webLibs.moduleRootPath.resolve(it)) }
                        .map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                        .forEach { add(it) }
                }
            )
        )
    }

    this.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_LIB)
        .takeIf { it.directoryExists }
        ?.let { libPath ->
            libs.add(
                JavaLibraryDescriptor(
                    name = "${this.name} - Web Library",
                    exported = true,
                    libraryRoots = buildList {
                        virtualFileUrlManager.fromPath(libPath)
                            ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                            ?.let { add(it) }

                        // we have to add each jar file explicitly, otherwise Spring will not recognise `classpath:/META-INF/my.xml` in the jar files
                        // JetBrains IntelliJ IDEA issue - https://youtrack.jetbrains.com/issue/IDEA-257819
                        libPath.listDirectoryEntries()
                            .filter { it.name.endsWith(".jar") }
                            .mapNotNull { virtualFileUrlManager.fromJar(it) }
                            .map { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                            .forEach { add(it) }

                        this@webLibs.getStandardSourceJarDirectory(importContext)
                            .mapNotNull { virtualFileUrlManager.fromPath(it) }
                            .map { LibraryRoot(it, LibraryRootTypeId.SOURCES, InclusionOptions.ARCHIVES_UNDER_ROOT) }
                            .forEach { add(it) }
                    },
                )
            )
        }
    return libs.toList()
}

private fun YCommonWebSubModuleDescriptor.commonWebLibs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager) = buildList {
    val classesLibs = this@commonWebLibs
        .dependantWebExtensions
        .map { moduleDescriptor ->
            JavaLibraryDescriptor(
                name = "${moduleDescriptor.name} - Common Web Classes",
                exported = true,
                libraryRoots = buildList {
                    virtualFileUrlManager.fromPath(moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES))
                        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED, InclusionOptions.ARCHIVES_UNDER_ROOT) }
                        ?.let { add(it) }

                    ProjectConstants.Directory.ALL_SRC_DIR_NAMES
                        .mapNotNull { virtualFileUrlManager.fromPath(this@commonWebLibs.moduleRootPath.resolve(it)) }
                        .map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                        .forEach { add(it) }
                }
            )
        }

    addAll(this@commonWebLibs.webLibs(importContext, virtualFileUrlManager))
    addAll(classesLibs)
}

private fun PlatformModuleDescriptor.libs(importContext: ProjectImportContext, virtualFileUrlManager: VirtualFileUrlManager): Collection<JavaLibraryDescriptor> {
    val dbDriversPath = (importContext.externalDbDriversDirectory
        ?: this.moduleRootPath.resolve(ProjectConstants.Paths.LIB_DB_DRIVER))
    val libraryRoot = virtualFileUrlManager.fromPath(dbDriversPath)
        ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED, InclusionOptions.ARCHIVES_UNDER_ROOT) }
        ?: return emptyList()
    return listOf(
        JavaLibraryDescriptor(
            name = HybrisConstants.PLATFORM_DATABASE_DRIVER_LIBRARY,
            exported = true,
            libraryRoots = listOf(libraryRoot)
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