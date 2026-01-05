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

package sap.commerce.toolset.java.configurator

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import org.jetbrains.jps.model.JpsElement
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.*
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory

class JavaModuleContentRootsConfigurator : ModuleImportConfigurator {

    private val rootsToIgnore = mapOf(
        "acceleratorstorefrontcommons" to listOf(Path("commonweb", "testsrc"))
    )

    override val name: String
        get() = "Content Roots"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        module: Module,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val moduleRootPath = moduleDescriptor.moduleRootPath
        val contentEntry =  VfsUtil.findFile(moduleRootPath, true)
            ?.let { modifiableModelsProvider.getModifiableRootModel(module).addContentEntry(it) }
            ?: return

        val dirsToIgnore = rootsToIgnore[module.name]
            ?.map { moduleRootPath.resolve(it) }
            ?: emptyList()

        when (moduleDescriptor) {
            is YWebSubModuleDescriptor -> moduleDescriptor.configureWebRoots(importContext, contentEntry, dirsToIgnore)
            is YCommonWebSubModuleDescriptor -> moduleDescriptor.configureWebModuleRoots(importContext, contentEntry, dirsToIgnore)
            is YAcceleratorAddonSubModuleDescriptor -> moduleDescriptor.configureWebModuleRoots(importContext, contentEntry, dirsToIgnore)
            is PlatformModuleDescriptor -> moduleDescriptor.configurePlatformRoots(importContext, contentEntry, dirsToIgnore)
            else -> moduleDescriptor.configureCommonRoots(importContext, contentEntry, dirsToIgnore)
        }
    }

    private fun PlatformModuleDescriptor.configurePlatformRoots(
        importContext: ProjectImportContext,
        contentEntry: ContentEntry,
        dirsToIgnore: Collection<Path>,
    ) {
        this.configureCommonRoots(importContext, contentEntry, dirsToIgnore)

        val bootstrapPath = moduleRootPath.resolve(ProjectConstants.Directory.BOOTSTRAP)

        contentEntry.addResourcesDirectory(bootstrapPath)
        // Only when bootstrap gensrc registered as source folder we can properly build the Class Hierarchy
        val genSrcPath = bootstrapPath.resolve(ProjectConstants.Directory.GEN_SRC)
        contentEntry.addSourceFolderIfNotIgnored(
            importContext,
            genSrcPath,
            JavaSourceRootType.SOURCE,
            JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
            dirsToIgnore
        )

        contentEntry.excludeDirectory(genSrcPath)
        contentEntry.excludeDirectory(bootstrapPath.resolve(ProjectConstants.Directory.MODEL_CLASSES))
        contentEntry.excludeDirectory(moduleRootPath.resolve(ProjectConstants.Directory.TOMCAT_6))
        contentEntry.excludeDirectory(moduleRootPath.resolve(ProjectConstants.Directory.TOMCAT))

        contentEntry.addExcludePattern("apache-ant-*")
    }

    private fun YWebSubModuleDescriptor.configureWebRoots(
        importContext: ProjectImportContext,
        contentEntry: ContentEntry,
        dirsToIgnore: List<Path>,
    ) {
        configureCommonRoots(importContext, contentEntry, dirsToIgnore)
        configureWebModuleRoots(importContext, contentEntry, dirsToIgnore)

        if (this.isCustomModuleDescriptor || !importContext.settings.importOOTBModulesInReadOnlyMode) {
            this.configureExternalModuleRoot(importContext, contentEntry, ProjectConstants.Directory.COMMON_WEB_SRC, JavaSourceRootType.SOURCE)
            this.configureExternalModuleRoot(importContext, contentEntry, ProjectConstants.Directory.ADDON_SRC, JavaSourceRootType.SOURCE)
        }
    }

    private fun YWebSubModuleDescriptor.configureExternalModuleRoot(
        importContext: ProjectImportContext,
        contentEntry: ContentEntry,
        sourceRoot: String,
        type: JavaSourceRootType
    ) {
        val commonWebSrcDir = moduleRootPath.resolve(sourceRoot)
            .takeIf { it.directoryExists }
            ?: return

        Files.newDirectoryStream(commonWebSrcDir) { it.isDirectory() }.use { directoryStream ->
            directoryStream.forEach { srcDir ->
                contentEntry.addSourceFolderIfNotIgnored(
                    importContext,
                    srcDir,
                    type,
                    JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
                    emptyList()
                )
            }
        }
    }

    private fun YSubModuleDescriptor.configureWebInf(
        importContext: ProjectImportContext,
        contentEntry: ContentEntry,
    ) {
        if (this.isCustomModuleDescriptor
            || (!importContext.settings.importOOTBModulesInReadOnlyMode && moduleRootPath.testSrcDirectoriesExists)
        ) {
            val excludePath = moduleRootPath.resolve(ProjectConstants.Paths.ACCELERATOR_ADDON_WEB)
            contentEntry.excludeDirectory(excludePath)
        }
    }

    private fun YSubModuleDescriptor.configureWebModuleRoots(
        importContext: ProjectImportContext,
        contentEntry: ContentEntry,
        dirsToIgnore: List<Path>,
    ) {
        this.configureCommonRoots(importContext, contentEntry, dirsToIgnore)

        contentEntry.excludeSubDirectories(
            moduleRootPath,
            listOf(ProjectConstants.Directory.TEST_CLASSES)
        )
        this.configureWebInf(importContext, contentEntry)
    }

    private fun ModuleDescriptor.configureCommonRoots(
        importContext: ProjectImportContext,
        contentEntry: ContentEntry,
        dirsToIgnore: Collection<Path>,
    ) {
        if (this.isCustomModuleDescriptor
            || !importContext.settings.importOOTBModulesInReadOnlyMode
            || EiConstants.Extension.PLATFORM_SERVICES == this.name
        ) {
            contentEntry.addSourceRoots(
                importContext,
                this.moduleRootPath,
                dirsToIgnore,
                ProjectConstants.Directory.SRC_DIR_NAMES,
                JavaSourceRootType.SOURCE
            )

            if (this.isCustomModuleDescriptor || !importContext.settings.excludeTestSources) {
                contentEntry.addSourceRoots(
                    importContext,
                    this.moduleRootPath,
                    dirsToIgnore,
                    ProjectConstants.Directory.TEST_SRC_DIR_NAMES,
                    JavaSourceRootType.TEST_SOURCE
                )
            }

            contentEntry.addSourceFolderIfNotIgnored(
                importContext,
                this.moduleRootPath.resolve(ProjectConstants.Directory.GEN_SRC),
                JavaSourceRootType.SOURCE,
                JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
                dirsToIgnore
            )

            this.configureResourceDirectory(importContext, contentEntry, dirsToIgnore)
        }

        this.excludeCommonNeedlessDirs(importContext, contentEntry)
    }

    private fun ModuleDescriptor.configureResourceDirectory(
        importContext: ProjectImportContext,
        contentEntry: ContentEntry,
        dirsToIgnore: Collection<Path>,
    ) {
        val resourcesPath = moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES)
        val properties = if (this is YBackofficeSubModuleDescriptor) JpsJavaExtensionService.getInstance().createResourceRootProperties("cockpitng", false)
        else JavaResourceRootType.RESOURCE.createDefaultProperties()

        contentEntry.addSourceFolderIfNotIgnored(
            importContext,
            resourcesPath,
            JavaResourceRootType.RESOURCE,
            properties,
            dirsToIgnore
        )

        if (importContext.settings.extensionsResourcesToExclude.contains(this.name)) {
            contentEntry.excludeDirectory(resourcesPath)
        }
    }

    private fun ModuleDescriptor.excludeCommonNeedlessDirs(
        importContext: ProjectImportContext,
        contentEntry: ContentEntry,
    ) {
        contentEntry.excludeSubDirectories(
            moduleRootPath,
            listOf(
                ProjectConstants.Directory.NODE_MODULES,
                HybrisConstants.EXTERNAL_TOOL_BUILDERS_DIRECTORY,
                HybrisConstants.SETTINGS_DIRECTORY,
                ProjectConstants.Directory.TEST_CLASSES,
                ProjectConstants.Directory.ECLIPSE_BIN,
                ProjectConstants.Directory.BOWER_COMPONENTS,
                ProjectConstants.Directory.JS_TARGET,
                HybrisConstants.SPOCK_META_INF_SERVICES_DIRECTORY
            )
        )

        if (this.isCustomModuleDescriptor || !importContext.settings.importOOTBModulesInReadOnlyMode) {
            contentEntry.excludeDirectory(moduleRootPath.resolve(ProjectConstants.Directory.CLASSES))
        }
    }

    private fun ContentEntry.excludeSubDirectories(
        dir: Path,
        names: Collection<String>,
    ) = names
        .map { dir.resolve(it) }
        .forEach { this.excludeDirectory(it) }

    private fun <P : JpsElement> ContentEntry.addSourceRoots(
        importContext: ProjectImportContext,
        dir: Path,
        dirsToIgnore: Collection<Path>,
        directories: Collection<String>,
        scope: JpsModuleSourceRootType<P>
    ) = directories
        .map { dir.resolve(it) }
        .forEach {
            addSourceFolderIfNotIgnored(
                importContext,
                it,
                scope,
                scope.createDefaultProperties(),
                dirsToIgnore
            )
        }

    private fun <P : JpsElement> ContentEntry.addSourceFolderIfNotIgnored(
        importContext: ProjectImportContext,
        srcDir: Path,
        rootType: JpsModuleSourceRootType<P>,
        properties: P,
        dirsToIgnore: Collection<Path>,
    ) {
        if (dirsToIgnore.none { FileUtil.isAncestor(it.toFile(), srcDir.toFile(), false) }) {
            if (importContext.settings.ignoreNonExistingSourceDirectories && !srcDir.directoryExists) return

            VfsUtil.findFile(srcDir, true)
                ?.let { this.addSourceFolder(it, rootType, properties) }
        }
    }

    private fun ContentEntry.addResourcesDirectory(moduleRootPath: Path) = moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES)
        .let { VfsUtil.findFile(it, true) }
        ?.let { this.addSourceFolder(it, JavaResourceRootType.RESOURCE) }


    private fun ContentEntry.excludeDirectory(excludePath: Path) = VfsUtil.findFile(excludePath, true)
        ?.let { this.addExcludeFolder(it) }

    private val Path.testSrcDirectoriesExists
        get() = ProjectConstants.Directory.TEST_SRC_DIR_NAMES
            .any { this.resolve(it).directoryExists }

    private val ModuleDescriptor.isCustomModuleDescriptor
        get() = this is YCustomRegularModuleDescriptor
            || (this is YSubModuleDescriptor && this.owner is YCustomRegularModuleDescriptor)
}