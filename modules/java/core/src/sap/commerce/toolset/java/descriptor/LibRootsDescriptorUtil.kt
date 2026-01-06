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
import sap.commerce.toolset.java.configurator.contentEntry.isCustomModuleDescriptor
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.*
import sap.commerce.toolset.project.descriptor.impl.*
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

// TODO: Split this to LibraryDescriptors provider

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

    addIfNotNull(this@libs.serverLibs(importContext))
    addIfNotNull(this@libs.backofficeClassesLib(importContext))
}

private fun YRegularModuleDescriptor.backofficeClassesLib(importContext: ProjectImportContext) = this.getSubModules()
    .takeIf { this.extensionInfo.backofficeModule }
    ?.firstOrNull { it is YBackofficeSubModuleDescriptor }
    ?.let { yModule ->
        val attachSources = this.type == ModuleDescriptorType.CUSTOM || importContext.settings.importOOTBModulesInWriteMode
        val sourcePaths = (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
            .map { yModule.moduleRootPath.resolve(it) }
            .filter { it.directoryExists }
            .map { JavaLibraryPath.Root(OrderRootType.SOURCES, it) }

        JavaLibraryDescriptor(
            name = "${this.name} - Backoffice Classes",
            scope = DependencyScope.PROVIDED,
            libraryPaths = buildList {
                add(JavaLibraryPath.Root(OrderRootType.CLASSES, yModule.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES)))

                if (attachSources) addAll(sourcePaths)
            },
        )
    }

private fun YHmcSubModuleDescriptor.hmcLibs(importContext: ProjectImportContext) = buildList {
    add(
        JavaLibraryDescriptor(
            name = "${this@hmcLibs.name} - HMC Bin",
            exported = true,
            libraryPaths = listOf(
                JavaLibraryPath.JarDirectory(OrderRootType.CLASSES, this@hmcLibs.moduleRootPath.resolve(ProjectConstants.Directory.BIN))
            )
        )
    )

    importContext.chosenHybrisModuleDescriptors
        .firstOrNull { it.name == EiConstants.Extension.HMC }
        ?.let {
            JavaLibraryDescriptor(
                name = "${this@hmcLibs.name} - Web Classes",
                libraryPaths = listOf(
                    JavaLibraryPath.Root(OrderRootType.CLASSES, it.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES))
                ),
            )
        }
        ?.let { add(it) }
}

private val YModuleDescriptor.rootLib
    get() = JavaLibraryDescriptor(
        name = "${this.name} - lib",
        descriptorType = LibraryDescriptorType.LIB,
        exported = true,
        libraryPaths = listOf(
            JavaLibraryPath.JarDirectory(OrderRootType.CLASSES, this.moduleRootPath.resolve(ProjectConstants.Directory.LIB))
        )
    )

private fun YModuleDescriptor.nonCustomModuleLibs(importContext: ProjectImportContext): Collection<JavaLibraryDescriptor> {
    if (this.type == ModuleDescriptorType.CUSTOM) return emptyList()
    if (importContext.settings.importOOTBModulesInWriteMode) return emptyList()

    return buildList {
        val moduleRootPath = this@nonCustomModuleLibs.moduleRootPath

        if (EiConstants.Extension.PLATFORM_SERVICES != this@nonCustomModuleLibs.name) {
            add(
                JavaLibraryDescriptor(
                    name = "${this@nonCustomModuleLibs.name} - compiler output",
                    exported = true,
                    libraryPaths = buildList {
                        add(
                            JavaLibraryPath.Root(
                                OrderRootType.CLASSES,
                                moduleRootPath.resolve(ProjectConstants.Directory.CLASSES)
                            )
                        )

                        (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                            .map { moduleRootPath.resolve(it) }
                            .filter { it.directoryExists }
                            .map { JavaLibraryPath.Root(OrderRootType.SOURCES, it) }
                            .forEach { add(it) }
                    }
                )
            )
        }

        add(
            JavaLibraryDescriptor(
                name = "${this@nonCustomModuleLibs.name} - resources",
                exported = true,
                libraryPaths = listOf(
                    JavaLibraryPath.Root(OrderRootType.CLASSES, moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES))
                )
            )
        )
    }
}

// TODO: server jar may not present, example -> apparelstore, electronicsstore, core
private fun YModuleDescriptor.serverLibs(importContext: ProjectImportContext): JavaLibraryDescriptor? {
    if (this.isCustomModuleDescriptor) return null

    // Attach standard sources to server jar
    return JavaLibraryDescriptor(
        name = "${this.name} - server",
        exported = true,
        libraryPaths = buildList {
            val moduleRootPath = this@serverLibs.moduleRootPath

            moduleRootPath.resolve(ProjectConstants.Directory.CLASSES)
                .takeIf { it.directoryExists }
                ?.let { JavaLibraryPath.Root(OrderRootType.CLASSES, it) }
                ?.let { add(it) }


            moduleRootPath.resolve(ProjectConstants.Directory.BIN)
                .takeIf { it.directoryExists }
                ?.listDirectoryEntries()
                ?.filter { it.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }
                ?.takeIf { it.isNotEmpty() }
                ?.map { JavaLibraryPath.Root(OrderRootType.CLASSES, it) }
                ?.forEach { add(it) }

            (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                .map { moduleRootPath.resolve(it) }
                .filter { it.directoryExists }
                .map { JavaLibraryPath.Root(OrderRootType.SOURCES, it) }
                .forEach { add(it) }

            this@serverLibs.getStandardSourceJarDirectory(importContext)
                .map { JavaLibraryPath.JarDirectory(OrderRootType.SOURCES, it) }
                .forEach { add(it) }
        }
    )
}

private fun YBackofficeSubModuleDescriptor.libs(importContext: ProjectImportContext) = buildList {
    add(this@libs.rootLib)
    add(this@libs.backofficeLib())

    addAll(this@libs.nonCustomModuleLibs(importContext))
    addIfNotNull(this@libs.serverLibs(importContext))
}

private fun YBackofficeSubModuleDescriptor.backofficeLib() = JavaLibraryDescriptor(
    name = "${this.name} - Backoffice lib",
    exported = true,
    libraryPaths = listOf(
        JavaLibraryPath.JarDirectory(OrderRootType.CLASSES, this.moduleRootPath.resolve(ProjectConstants.Paths.BACKOFFICE_BIN))
    )
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
    addIfNotNull(this@libs.serverLibs(importContext))

    importContext.platformDirectory.resolve(ProjectConstants.Paths.HAC_WEB_INF_CLASSES)
        .takeIf { it.directoryExists }
        ?.let {
            add(
                JavaLibraryDescriptor(
                    name = "${this@libs.name} - HAC Web Classes",
                    libraryPaths = listOf(
                        JavaLibraryPath.Root(OrderRootType.CLASSES, it)
                    )
                )
            )
        }
}

private fun YHmcSubModuleDescriptor.libs(importContext: ProjectImportContext) = buildList {
    add(this@libs.rootLib)

    addAll(this@libs.nonCustomModuleLibs(importContext))
    addAll(this@libs.hmcLibs(importContext))
    addIfNotNull(this@libs.serverLibs(importContext))
}

private fun YAcceleratorAddonSubModuleDescriptor.libs(importContext: ProjectImportContext): MutableList<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    libs.addAll(this.nonCustomModuleLibs(importContext))
    libs.addIfNotNull(this.serverLibs(importContext))
    libs.add(this.rootLib)

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
                    libraryPaths = buildList {
                        add(JavaLibraryPath.Root(OrderRootType.CLASSES, yModule.moduleRootPath.resolve(ProjectConstants.Directory.CLASSES)))

                        if (attachSources) {
                            (ProjectConstants.Directory.ALL_SRC_DIR_NAMES + ProjectConstants.Directory.TEST_SRC_DIR_NAMES)
                                .map { yModule.moduleRootPath.resolve(it) }
                                .filter { it.directoryExists }
                                .map { JavaLibraryPath.Root(OrderRootType.SOURCES, it) }
                                .forEach { add(it) }
                        }
                    }
                )
            )
            libs.add(
                JavaLibraryDescriptor(
                    name = "${yModule.name} - Addon's Target Resources",
                    libraryPaths = listOf(
                        JavaLibraryPath.Root(OrderRootType.CLASSES, yModule.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES)),
                    )
                )
            )
        }
    return libs
}

private fun YSubModuleDescriptor.webLibs(importContext: ProjectImportContext): Collection<JavaLibraryDescriptor> {
    val libs = mutableListOf<JavaLibraryDescriptor>()

    libs.addAll(this.nonCustomModuleLibs(importContext))
    libs.addIfNotNull(this.serverLibs(importContext))
    libs.add(this.rootLib)

    if (this.owner.name == EiConstants.Extension.BACK_OFFICE) return libs

    if (this.type != ModuleDescriptorType.CUSTOM && this is YWebSubModuleDescriptor) {
        libs.add(
            JavaLibraryDescriptor(
                name = "${this.name} - Web Classes",
                exported = true,
                libraryPaths = buildList {
                    add(
                        JavaLibraryPath.Root(
                            OrderRootType.CLASSES,
                            this@webLibs.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES)
                        )
                    )

                    ProjectConstants.Directory.ALL_SRC_DIR_NAMES
                        .map { this@webLibs.moduleRootPath.resolve(it) }
                        .filter { it.directoryExists }
                        .map { JavaLibraryPath.Root(OrderRootType.SOURCES, it) }
                        .forEach { add(it) }

                    listOf(
                        this@webLibs.moduleRootPath.resolve(ProjectConstants.Directory.ADDON_SRC),
                        this@webLibs.moduleRootPath.resolve(ProjectConstants.Directory.COMMON_WEB_SRC),
                    )
                        .filter { it.directoryExists }
                        .flatMap { srcDir ->
                            srcDir.listDirectoryEntries()
                                .filter { it.directoryExists }
                        }
                        .map { JavaLibraryPath.Root(OrderRootType.SOURCES, it) }
                        .forEach { add(it) }
                }
            )
        )

        libs.add(
            JavaLibraryDescriptor(
                name = "${this.name} - Test Classes",
                exported = true,
                scope = DependencyScope.TEST,
                libraryPaths = buildList {
                    add(JavaLibraryPath.Root(OrderRootType.CLASSES, this@webLibs.moduleRootPath.resolve(ProjectConstants.Directory.TEST_CLASSES)))

                    ProjectConstants.Directory.TEST_SRC_DIR_NAMES
                        .map { this@webLibs.moduleRootPath.resolve(it) }
                        .filter { it.directoryExists }
                        .map { JavaLibraryPath.Root(OrderRootType.SOURCES, it) }
                        .forEach { add(it) }
                }
            )
        )
    }

    this.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_LIB)
        .takeIf { it.directoryExists }
        ?.let { libFolder ->
            libs.add(
                JavaLibraryDescriptor(
                    name = "${this.name} - Web Library",
                    descriptorType = LibraryDescriptorType.WEB_INF_LIB,
                    exported = true,
                    libraryPaths = buildList {
                        add(JavaLibraryPath.Root(OrderRootType.CLASSES, libFolder))

                        // we have to add each jar file explicitly, otherwise Spring will not recognise `classpath:/META-INF/my.xml` in the jar files
                        // JetBrains IntelliJ IDEA issue - https://youtrack.jetbrains.com/issue/IDEA-257819
                        libFolder.listDirectoryEntries()
                            .filter { it.name.endsWith(".jar") }
                            .map { JavaLibraryPath.Root(OrderRootType.CLASSES, it) }
                            .forEach { add(it) }

                        this@webLibs.getStandardSourceJarDirectory(importContext)
                            .map { JavaLibraryPath.JarDirectory(OrderRootType.SOURCES, it) }
                            .forEach { add(it) }
                    },
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
                .map { JavaLibraryPath.Root(OrderRootType.SOURCES, it) }
            JavaLibraryDescriptor(
                name = "${it.name} - Common Web Classes",
                exported = true,
                libraryPaths = buildList {
                    add(JavaLibraryPath.JarDirectory(OrderRootType.CLASSES, it.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES)))

                    addAll(webSourceFiles)
                }
            )
        }

    addAll(this@commonWebLibs.webLibs(importContext))
    addAll(classesLibs)
}

private fun ConfigModuleDescriptor.libs() = listOf(
    JavaLibraryDescriptor(
        name = "Config License",
        exported = true,
        libraryPaths = listOf(
            JavaLibraryPath.JarDirectory(OrderRootType.CLASSES, this.moduleRootPath.resolve(ProjectConstants.Directory.LICENCE))
        )
    )
)

private fun PlatformModuleDescriptor.libs(importContext: ProjectImportContext): List<JavaLibraryDescriptor> {
    val dbDriversPath = (importContext.externalDbDriversDirectory
        ?: this.moduleRootPath.resolve(ProjectConstants.Paths.LIB_DB_DRIVER))
    return listOf(
        JavaLibraryDescriptor(
            name = HybrisConstants.PLATFORM_DATABASE_DRIVER_LIBRARY,
            exported = true,
            libraryPaths = listOf(
                JavaLibraryPath.JarDirectory(OrderRootType.CLASSES, dbDriversPath),
            )
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