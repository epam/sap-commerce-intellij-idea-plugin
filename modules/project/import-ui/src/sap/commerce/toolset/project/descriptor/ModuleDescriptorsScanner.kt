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
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.project.HybrisProjectImportService
import sap.commerce.toolset.project.context.ModuleFilesContext
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

// TODO: extract WSL scanner
@Service
class ModuleDescriptorsScanner {

    @Throws(InterruptedException::class, IOException::class)
    fun findModuleRoots(
        importContext: ProjectImportContext.Mutable,
        moduleFilesContext: ModuleFilesContext,
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

        val moduleDescriptorResolver = ModuleDescriptorResolver.getInstance()

        if (moduleDescriptorResolver.isHybrisExtension(moduleDirectory)) {
            thisLogger().info("Detected hybris module ${moduleDirectory.absolutePath}")
            moduleFilesContext.add(ModuleGroup.HYBRIS, moduleDirectory)
            return
        }

        if (moduleDescriptorResolver.isConfigModule(moduleDirectory)) {
            thisLogger().info("Detected config module ${moduleDirectory.absolutePath}")
            moduleFilesContext.add(ModuleGroup.HYBRIS, moduleDirectory)
            return
        }

        if (!moduleDirectory.absolutePath.endsWith(HybrisConstants.PLATFORM_MODULE)
            && !FileUtil.filesEqual(moduleDirectory, importContext.rootDirectory)
            && (moduleDescriptorResolver.isGradleModule(moduleDirectory) || moduleDescriptorResolver.isGradleKtsModule(moduleDirectory))
            && !moduleDescriptorResolver.isCCv2Module(moduleDirectory)
        ) {
            thisLogger().info("Detected gradle module ${moduleDirectory.absolutePath}")

            moduleFilesContext.add(ModuleGroup.OTHER, moduleDirectory)
        }

        if (moduleDescriptorResolver.isMavenModule(moduleDirectory)
            && !FileUtil.filesEqual(moduleDirectory, importContext.rootDirectory)
            && !moduleDescriptorResolver.isCCv2Module(moduleDirectory)
        ) {
            thisLogger().info("Detected maven module ${moduleDirectory.absolutePath}")
            moduleFilesContext.add(ModuleGroup.OTHER, moduleDirectory)
        }

        if (moduleDescriptorResolver.isPlatformModule(moduleDirectory)) {
            thisLogger().info("Detected platform module ${moduleDirectory.absolutePath}")
            moduleFilesContext.add(ModuleGroup.HYBRIS, moduleDirectory)
        } else if (moduleDescriptorResolver.isEclipseModule(moduleDirectory)
            && !FileUtil.filesEqual(moduleDirectory, importContext.rootDirectory)
        ) {
            thisLogger().info("Detected eclipse module ${moduleDirectory.absolutePath}")
            moduleFilesContext.add(ModuleGroup.OTHER, moduleDirectory)
        }

        if (moduleDescriptorResolver.isCCv2Module(moduleDirectory)) {
            thisLogger().info("Detected CCv2 module ${moduleDirectory.absolutePath}")
            moduleFilesContext.add(ModuleGroup.OTHER, moduleDirectory)
            val name = moduleDirectory.getName()
            if (name.endsWith(CCv2Constants.DATAHUB_NAME)) {
                // faster import: no need to process sub-folders of the CCv2 datahub directory
                return
            }
        }

        if (moduleDescriptorResolver.isAngularModule(moduleDirectory)) {
            thisLogger().info("Detected Angular module ${moduleDirectory.absolutePath}")
            moduleFilesContext.add(ModuleGroup.OTHER, moduleDirectory)
            // do not go deeper
            return
        }

        scanForSubdirectories(importContext, moduleFilesContext, excludedFromScanning, moduleDirectory.toPath(), progressListenerProcessor)
    }

    @Throws(InterruptedException::class, IOException::class)
    fun processDirectoriesByTypePriority(
        importContext: ProjectImportContext.Mutable,
        rootDirectory: File,
        moduleFilesContext: ModuleFilesContext
    ): Collection<File> {
        val moduleRootDirectories = mutableMapOf<String, File>()

        moduleFilesContext.hybrisModules
            .forEach { file -> addIfNotExists(importContext, rootDirectory, moduleRootDirectories, file) }

        moduleFilesContext.nonHybrisModules
            .forEach { file -> addIfNotExists(importContext, rootDirectory, moduleRootDirectories, file) }

        return moduleRootDirectories.values
    }

    private fun hasVCS(moduleDirectory: File?) = File(moduleDirectory, ".git").isDirectory()
        || File(moduleDirectory, ".svn").isDirectory()
        || File(moduleDirectory, ".hg").isDirectory()

    @Throws(IOException::class, InterruptedException::class)
    private fun scanForSubdirectories(
        importContext: ProjectImportContext.Mutable,
        moduleFilesContext: ModuleFilesContext,
        excludedFromScanning: Set<File>,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        if (!modulePath.isDirectory()) return

        if (isPathInWSLDistribution(modulePath)) {
            scanSubdirectoriesWSL(importContext, moduleFilesContext, excludedFromScanning, modulePath, progressListenerProcessor)
        } else {
            scanSubdirectories(importContext, moduleFilesContext, excludedFromScanning, modulePath, progressListenerProcessor)
        }
    }

    private fun isPathInWSLDistribution(modulePath: Path) = WslDistributionManager.getInstance().getInstalledDistributions()
        .map { it.getUNCRootPath() }
        .any { modulePath.startsWith(it) }

    @Throws(InterruptedException::class, IOException::class)
    private fun scanSubdirectories(
        importContext: ProjectImportContext.Mutable,
        moduleFilesContext: ModuleFilesContext,
        excludedFromScanning: Set<File>,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        val importService = HybrisProjectImportService.getInstance()

        val files = Files.newDirectoryStream(modulePath, DirectoryStream.Filter { file: Path? ->
            if (file == null
                || !Files.isDirectory(file)
                || importService.isDirectoryExcluded(file)
            ) return@Filter false
            !Files.isSymbolicLink(file) || importContext.settings.followSymlink
        })
        for (file in files) {
            findModuleRoots(
                importContext,
                moduleFilesContext,
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
        moduleFilesContext: ModuleFilesContext,
        excludedFromScanning: Set<File>,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        val importService = HybrisProjectImportService.getInstance()
        val followSymlink = importContext.settings.followSymlink

        Files.list(modulePath).use { stream ->
            stream
                .filter { Files.isDirectory(it) }
                .filter { !importService.isDirectoryExcluded(it) }
                .filter { !Files.isSymbolicLink(it) || followSymlink }
                .map { it.toFile() }
                .forEach { moduleRoot ->
                    findModuleRoots(importContext, moduleFilesContext, excludedFromScanning, moduleRoot, progressListenerProcessor)
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

    companion object {
        fun getInstance(): ModuleDescriptorsScanner = application.service()
    }
}