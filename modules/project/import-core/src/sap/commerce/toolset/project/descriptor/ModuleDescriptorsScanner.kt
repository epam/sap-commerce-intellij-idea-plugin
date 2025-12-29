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

package sap.commerce.toolset.project.descriptor

import com.intellij.execution.wsl.WslDistributionManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.ProjectImportConstants
import sap.commerce.toolset.project.context.ModuleGroup
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.tasks.TaskProgressProcessor
import sap.commerce.toolset.project.utils.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

// TODO: extract WSL scanner, convert to coroutine
@Service
class ModuleDescriptorsScanner {

    @Throws(InterruptedException::class, IOException::class)
    fun findModuleRoots(
        importContext: ProjectImportContext.Mutable,
        moduleRootsContext: ModuleRootsContext,
        excludedFromScanning: Set<File>,
        moduleDirectory: File,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        if (!progressListenerProcessor.shouldContinue(moduleDirectory)) {
            thisLogger().error("Modules scanning has been interrupted.")
            throw InterruptedException("Modules scanning has been interrupted.")
        }

        if (moduleDirectory.isHidden()) {
            thisLogger().debug("Skipping hidden directory: $moduleDirectory")
            return
        }
        if (excludedFromScanning.contains(moduleDirectory)) {
            thisLogger().debug("Skipping excluded directory: $moduleDirectory")
            return
        }

        if (hasVCS(moduleDirectory)) {
            thisLogger().info("Detected version control service ${moduleDirectory.absolutePath}")
            importContext.addVcs(moduleDirectory.getCanonicalFile())
        }

        val moduleRootResolver = ModuleRootResolver.getInstance()

        if (moduleRootResolver.isHybrisExtensionRoot(moduleDirectory)) {
            thisLogger().info("Detected hybris module ${moduleDirectory.absolutePath}")
            moduleRootsContext.add(ModuleGroup.HYBRIS, moduleDirectory)
            return
        }

        if (moduleRootResolver.isConfigModuleRoot(moduleDirectory)) {
            thisLogger().info("Detected config module ${moduleDirectory.absolutePath}")
            moduleRootsContext.add(ModuleGroup.HYBRIS, moduleDirectory)
            return
        }

        if (!moduleDirectory.absolutePath.endsWith(HybrisConstants.PLATFORM_MODULE)
            && !FileUtil.filesEqual(moduleDirectory, importContext.rootDirectory)
            && (moduleRootResolver.isGradleModuleRoot(moduleDirectory) || moduleRootResolver.isGradleKtsModuleRoot(moduleDirectory))
            && !moduleRootResolver.isCCv2ModuleRoot(moduleDirectory)
        ) {
            thisLogger().info("Detected gradle module ${moduleDirectory.absolutePath}")

            moduleRootsContext.add(ModuleGroup.OTHER, moduleDirectory)
        }

        if (moduleRootResolver.isMavenModuleRoot(moduleDirectory)
            && !FileUtil.filesEqual(moduleDirectory, importContext.rootDirectory)
            && !moduleRootResolver.isCCv2ModuleRoot(moduleDirectory)
        ) {
            thisLogger().info("Detected maven module ${moduleDirectory.absolutePath}")
            moduleRootsContext.add(ModuleGroup.OTHER, moduleDirectory)
        }

        if (moduleRootResolver.isPlatformModuleRoot(moduleDirectory)) {
            thisLogger().info("Detected platform module ${moduleDirectory.absolutePath}")
            moduleRootsContext.add(ModuleGroup.HYBRIS, moduleDirectory)
        } else if (moduleRootResolver.isEclipseModuleRoot(moduleDirectory)
            && !FileUtil.filesEqual(moduleDirectory, importContext.rootDirectory)
        ) {
            thisLogger().info("Detected eclipse module ${moduleDirectory.absolutePath}")
            moduleRootsContext.add(ModuleGroup.OTHER, moduleDirectory)
        }

        if (moduleRootResolver.isCCv2ModuleRoot(moduleDirectory)) {
            thisLogger().info("Detected CCv2 module ${moduleDirectory.absolutePath}")
            moduleRootsContext.add(ModuleGroup.OTHER, moduleDirectory)
            val name = moduleDirectory.getName()
            if (name.endsWith(ProjectImportConstants.CCV2_DATAHUB_NAME)) {
                // faster import: no need to process sub-folders of the CCv2 datahub directory
                return
            }
        }

        if (moduleRootResolver.isAngularModuleRoot(moduleDirectory)) {
            thisLogger().info("Detected Angular module ${moduleDirectory.absolutePath}")
            moduleRootsContext.add(ModuleGroup.OTHER, moduleDirectory)
            // do not go deeper
            return
        }

        scanForSubdirectories(importContext, moduleRootsContext, excludedFromScanning, moduleDirectory.toPath(), progressListenerProcessor)
    }

    @Throws(InterruptedException::class, IOException::class)
    fun processDirectoriesByTypePriority(
        importContext: ProjectImportContext.Mutable,
        rootDirectory: File,
        moduleRootsContext: ModuleRootsContext
    ): Collection<File> {
        val moduleRootDirectories = mutableMapOf<String, File>()

        moduleRootsContext.hybrisModules
            .forEach { file -> addIfNotExists(importContext, rootDirectory, moduleRootDirectories, file) }

        moduleRootsContext.nonHybrisModules
            .forEach { file -> addIfNotExists(importContext, rootDirectory, moduleRootDirectories, file) }

        return moduleRootDirectories.values
    }

    private fun hasVCS(moduleDirectory: File?) = File(moduleDirectory, ".git").isDirectory()
        || File(moduleDirectory, ".svn").isDirectory()
        || File(moduleDirectory, ".hg").isDirectory()

    @Throws(IOException::class, InterruptedException::class)
    private fun scanForSubdirectories(
        importContext: ProjectImportContext.Mutable,
        moduleRootsContext: ModuleRootsContext,
        excludedFromScanning: Set<File>,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        if (!modulePath.isDirectory()) return

        if (isPathInWSLDistribution(modulePath)) {
            scanSubdirectoriesWSL(importContext, moduleRootsContext, excludedFromScanning, modulePath, progressListenerProcessor)
        } else {
            scanSubdirectories(importContext, moduleRootsContext, excludedFromScanning, modulePath, progressListenerProcessor)
        }
    }

    private fun isPathInWSLDistribution(modulePath: Path) = WslDistributionManager.getInstance().getInstalledDistributions()
        .map { it.getUNCRootPath() }
        .any { modulePath.startsWith(it) }

    @Throws(InterruptedException::class, IOException::class)
    private fun scanSubdirectories(
        importContext: ProjectImportContext.Mutable,
        moduleRootsContext: ModuleRootsContext,
        excludedFromScanning: Set<File>,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        val files = Files.newDirectoryStream(modulePath, DirectoryStream.Filter { file: Path? ->
            if (file == null
                || !Files.isDirectory(file)
                || isDirectoryExcluded(file)
            ) return@Filter false
            !Files.isSymbolicLink(file) || importContext.settings.followSymlink
        })
        for (file in files) {
            findModuleRoots(
                importContext,
                moduleRootsContext,
                excludedFromScanning,
                file.toFile(),
                progressListenerProcessor
            )
        }
        files.close()
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun scanSubdirectoriesWSL(
        importContext: ProjectImportContext.Mutable,
        moduleRootsContext: ModuleRootsContext,
        excludedFromScanning: Set<File>,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        val followSymlink = importContext.settings.followSymlink

        Files.list(modulePath).use { stream ->
            stream
                .filter { Files.isDirectory(it) }
                .filter { !isDirectoryExcluded(it) }
                .filter { !Files.isSymbolicLink(it) || followSymlink }
                .map { it.toFile() }
                .forEach { moduleRoot ->
                    findModuleRoots(importContext, moduleRootsContext, excludedFromScanning, moduleRoot, progressListenerProcessor)
                }
        }
    }

    private fun addIfNotExists(
        importContext: ProjectImportContext.Mutable,
        rootDirectory: File,
        moduleRootDirectories: MutableMap<String, File>,
        file: File
    ) {
        val hybrisDistributionDirectory = importContext.platformDirectory
        val externalExtensionsDirectory = importContext.externalExtensionsDirectory
        try {
            // this will resolve symlinks
            val path = file.getCanonicalPath()
            val current = moduleRootDirectories[path]
            if (current == null) {
                moduleRootDirectories[path] = file
                return
            }
            if (hybrisDistributionDirectory != null && !FileUtils.isFileUnder(current, hybrisDistributionDirectory)) {
                if (FileUtils.isFileUnder(file, hybrisDistributionDirectory)) {
                    moduleRootDirectories[path] = file
                    return
                }
            }
            if (externalExtensionsDirectory != null && !FileUtils.isFileUnder(current, externalExtensionsDirectory)) {
                if (FileUtils.isFileUnder(file, externalExtensionsDirectory)) {
                    moduleRootDirectories[path] = file
                    return
                }
            }
            if (!FileUtils.isFileUnder(current, rootDirectory)
                && FileUtils.isFileUnder(file, rootDirectory)
            ) {
                moduleRootDirectories[path] = file
            }
        } catch (e: IOException) {
            thisLogger().error("Unable to locate ${file.absolutePath}", e)
        }
    }

    private fun isDirectoryExcluded(path: Path): Boolean = ProjectImportConstants.excludedFromScanningDirectories.contains(path.name)
        || path.endsWith(ProjectConstants.Directory.PATH_PLATFORM_BOOTSTRAP)

    companion object {
        fun getInstance(): ModuleDescriptorsScanner = application.service()
    }
}