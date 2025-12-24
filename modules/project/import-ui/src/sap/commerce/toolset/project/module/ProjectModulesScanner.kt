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

package sap.commerce.toolset.project.module

import com.intellij.execution.wsl.WslDistributionManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.project.HybrisProjectImportService
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
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
class ProjectModulesScanner {

    @Throws(InterruptedException::class, IOException::class)
    fun findModuleRoots(
        projectDescriptor: HybrisProjectDescriptor,
        modulesContext: ModulesContext,
        excludedFromScanning: Set<File>,
        acceptOnlyHybrisModules: Boolean,
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

        val projectModuleDetector = ProjectModuleDetector.getInstance()

        if (projectModuleDetector.hasVCS(moduleDirectory)) {
            thisLogger().info("Detected version control service ${moduleDirectory.absolutePath}")
            projectDescriptor.detectedVcs.add(moduleDirectory.getCanonicalFile())
        }

        if (projectModuleDetector.isHybrisModule(moduleDirectory)) {
            thisLogger().info("Detected hybris module ${moduleDirectory.absolutePath}")
            modulesContext.hybrisModules.add(moduleDirectory)
            return
        }
        if (projectModuleDetector.isConfigModule(moduleDirectory)) {
            thisLogger().info("Detected config module ${moduleDirectory.absolutePath}")
            modulesContext.hybrisModules.add(moduleDirectory)
            return
        }

        if (!acceptOnlyHybrisModules) {
            // TODO: review this logic
            if (!moduleDirectory.absolutePath.endsWith(HybrisConstants.PLATFORM_MODULE) && !FileUtil.filesEqual(
                    moduleDirectory,
                    projectDescriptor.rootDirectory
                ) && (projectModuleDetector.isGradleModule(moduleDirectory) || projectModuleDetector.isGradleKtsModule(moduleDirectory))
                && !projectModuleDetector.isCCv2Module(moduleDirectory)
            ) {
                thisLogger().info("Detected gradle module ${moduleDirectory.absolutePath}")

                modulesContext.nonHybrisModules.add(moduleDirectory)
            }

            if (projectModuleDetector.isMavenModule(moduleDirectory)
                && !FileUtil.filesEqual(moduleDirectory, projectDescriptor.rootDirectory) && !projectModuleDetector.isCCv2Module(moduleDirectory)
            ) {
                thisLogger().info("Detected maven module ${moduleDirectory.absolutePath}")
                modulesContext.nonHybrisModules.add(moduleDirectory)
            }

            if (projectModuleDetector.isPlatformModule(moduleDirectory)) {
                thisLogger().info("Detected platform module ${moduleDirectory.absolutePath}")
                modulesContext.hybrisModules.add(moduleDirectory)
            } else if (projectModuleDetector.isEclipseModule(moduleDirectory)
                && !FileUtil.filesEqual(moduleDirectory, projectDescriptor.rootDirectory)
            ) {
                thisLogger().info("Detected eclipse module ${moduleDirectory.absolutePath}")
                modulesContext.nonHybrisModules.add(moduleDirectory)
            }

            if (projectModuleDetector.isCCv2Module(moduleDirectory)) {
                thisLogger().info("Detected CCv2 module ${moduleDirectory.absolutePath}")
                modulesContext.ccv2Modules.add(moduleDirectory)
                val name = moduleDirectory.getName()
                if (name.endsWith(CCv2Constants.DATAHUB_NAME)) {
                    // faster import: no need to process sub-folders of the CCv2 js-storefront and datahub directories
                    return
                }
            }

            if (projectModuleDetector.isAngularModule(moduleDirectory)) {
                thisLogger().info("Detected Angular module ${moduleDirectory.absolutePath}")
                modulesContext.nonHybrisModules.add(moduleDirectory)
                // do not go deeper
                return
            }
        }

        scanForSubdirectories(projectDescriptor, modulesContext, excludedFromScanning, acceptOnlyHybrisModules, moduleDirectory.toPath(), progressListenerProcessor)
    }

    @Throws(InterruptedException::class, IOException::class)
    fun processDirectoriesByTypePriority(
        projectDescriptor: HybrisProjectDescriptor,
        rootDirectory: File,
        modulesContext: ModulesContext,
        excludedFromScanning: Set<File>,
        scanThroughExternalModule: Boolean,
        progressListenerProcessor: TaskProgressProcessor<File>
    ): Collection<File> {
        val moduleRootDirectories = mutableMapOf<String, File>()

        modulesContext.hybrisModules
            .forEach { file -> addIfNotExists(projectDescriptor, rootDirectory, moduleRootDirectories, file) }

        if (scanThroughExternalModule) {
            thisLogger().info("Scanning for higher priority modules")

            modulesContext.nonHybrisModules
                .forEach { nonHybrisModulePath ->
                    val nonHybrisModulesContext = ModulesContext()
                    scanForSubdirectories(projectDescriptor, nonHybrisModulesContext, excludedFromScanning, true, nonHybrisModulePath.toPath(), progressListenerProcessor)

                    val hybrisModuleDescriptors = nonHybrisModulesContext.hybrisModules
                    if (hybrisModuleDescriptors.isEmpty()) {
                        thisLogger().info("Confirmed module: $nonHybrisModulePath")
                        addIfNotExists(projectDescriptor, rootDirectory, moduleRootDirectories, nonHybrisModulePath)
                    } else {
                        thisLogger().info("Replaced module: $nonHybrisModulePath")
                        hybrisModuleDescriptors
                            .forEach { file -> addIfNotExists(projectDescriptor, rootDirectory, moduleRootDirectories, file) }
                    }
                }
        } else {
            modulesContext.nonHybrisModules
                .forEach { file -> addIfNotExists(projectDescriptor, rootDirectory, moduleRootDirectories, file) }
        }

        modulesContext.ccv2Modules
            .forEach { file -> addIfNotExists(projectDescriptor, rootDirectory, moduleRootDirectories, file) }

        return moduleRootDirectories.values
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun scanForSubdirectories(
        projectDescriptor: HybrisProjectDescriptor,
        modulesContext: ModulesContext,
        excludedFromScanning: Set<File>,
        acceptOnlyHybrisModules: Boolean,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        if (!modulePath.isDirectory()) return

        if (isPathInWSLDistribution(modulePath)) {
            scanSubdirectoriesWSL(projectDescriptor, modulesContext, excludedFromScanning, acceptOnlyHybrisModules, modulePath, progressListenerProcessor)
        } else {
            scanSubdirectories(projectDescriptor, modulesContext, excludedFromScanning, acceptOnlyHybrisModules, modulePath, progressListenerProcessor)
        }
    }

    private fun isPathInWSLDistribution(modulePath: Path) = WslDistributionManager.getInstance().getInstalledDistributions()
        .map { it.getUNCRootPath() }
        .any { modulePath.startsWith(it) }

    @Throws(InterruptedException::class, IOException::class)
    private fun scanSubdirectories(
        projectDescriptor: HybrisProjectDescriptor,
        modulesContext: ModulesContext,
        excludedFromScanning: Set<File>,
        acceptOnlyHybrisModules: Boolean,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        val importService = HybrisProjectImportService.getInstance()

        val files = Files.newDirectoryStream(modulePath, DirectoryStream.Filter { file: Path? ->
            if (file == null
                || !Files.isDirectory(file)
                || importService.isDirectoryExcluded(file)
            ) return@Filter false
            !Files.isSymbolicLink(file) || projectDescriptor.importContext.followSymlink
        })
        for (file in files) {
            findModuleRoots(
                projectDescriptor,
                modulesContext,
                excludedFromScanning,
                acceptOnlyHybrisModules,
                file.toFile(),
                progressListenerProcessor
            )
        }
        files.close()
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun scanSubdirectoriesWSL(
        projectDescriptor: HybrisProjectDescriptor,
        modulesContext: ModulesContext,
        excludedFromScanning: Set<File>,
        acceptOnlyHybrisModules: Boolean,
        modulePath: Path,
        progressListenerProcessor: TaskProgressProcessor<File>
    ) {
        val importService = HybrisProjectImportService.getInstance()
        val followSymlink = projectDescriptor.importContext.followSymlink

        Files.list(modulePath).use { stream ->
            stream
                .filter { Files.isDirectory(it) }
                .filter { !importService.isDirectoryExcluded(it) }
                .filter { !Files.isSymbolicLink(it) || followSymlink }
                .map { it.toFile() }
                .forEach { moduleRoot ->
                    findModuleRoots(projectDescriptor, modulesContext, excludedFromScanning, acceptOnlyHybrisModules, moduleRoot, progressListenerProcessor)
                }
        }
    }

    private fun addIfNotExists(
        projectDescriptor: HybrisProjectDescriptor,
        rootDirectory: File,
        moduleRootDirectories: MutableMap<String, File>,
        file: File
    ) {
        val hybrisDistributionDirectory = projectDescriptor.hybrisDistributionDirectory
        val externalExtensionsDirectory = projectDescriptor.externalExtensionsDirectory
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
        fun getInstance(): ProjectModulesScanner = application.service()
    }
}