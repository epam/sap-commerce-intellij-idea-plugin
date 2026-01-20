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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.util.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.ProjectImportConstants
import sap.commerce.toolset.project.context.ModuleGroup
import sap.commerce.toolset.project.context.ModuleRoot
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.isDescendantOf
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.isHidden
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.name
import kotlin.io.path.readSymbolicLink

@Service
class ModuleRootsScanner {

    private val logger = thisLogger()

    suspend fun execute(
        context: ProjectImportContext.Mutable,
        rootDirectory: Path,
        skipDirectories: Collection<Path>
    ): Collection<ModuleRoot> {
        // prevent infinity loops in case of cycle symlinks
        val visited = mutableSetOf<Path>()
        val options = setOf(FileVisitOption.FOLLOW_LINKS)

        val moduleRootResolvers = ModuleRootResolver.EP.extensionList
        val moduleRoots = mutableListOf<ModuleRoot>()

        reportSequentialProgress { reporter ->
            withContext(Dispatchers.IO) {
                Files.walkFileTree(
                    rootDirectory,
                    options,
                    Int.MAX_VALUE,
                    object : SimpleFileVisitor<Path>() {
                        override fun preVisitDirectory(path: Path, attrs: BasicFileAttributes): FileVisitResult {
                            ensureActive()

                            // prevent recursion
                            if (visited.contains(path)) return FileVisitResult.SKIP_SUBTREE
                            else {
                                visited.add(path)
                                if (path.isSymbolicLink()) {
                                    visited.add(path.readSymbolicLink())
                                }
                            }

                            return when {
                                path.isHidden() -> {
                                    logger.debug("Skipping hidden directory: $path")
                                    FileVisitResult.SKIP_SUBTREE
                                }

                                skipDirectories.contains(path) -> {
                                    logger.debug("Skipping manually excluded directory: $path")
                                    FileVisitResult.SKIP_SUBTREE
                                }

                                path.isDirectoryExcluded -> {
                                    logger.debug("Skipping excluded directory: $path")
                                    FileVisitResult.SKIP_SUBTREE
                                }

                                else -> {
                                    reporter.indeterminateStep("Processing: $path")

                                    processVcsRoot(path, context)

                                    moduleRootResolvers
                                        .firstOrNull { it.isApplicable(context, rootDirectory, path) }
                                        ?.resolve(path)
                                        ?.also {
                                            it.moduleRoot?.let { moduleRoot ->
                                                val pathMessage = if (path.isSymbolicLink()) "$path -> (${path.readSymbolicLink()})"
                                                else path
                                                logger.debug("Detected module [${moduleRoot.type} | $pathMessage]")
                                                moduleRoots.add(moduleRoot)
                                            }
                                        }
                                        ?.fileVisitResult
                                        ?: FileVisitResult.CONTINUE
                                }
                            }
                        }

                        override fun visitFileFailed(file: Path, exc: IOException) = FileVisitResult.SKIP_SUBTREE
                    }
                )
            }
        }

        return moduleRoots
    }

    fun processModuleRootsByTypePriority(
        context: ProjectImportContext.Mutable,
        rootDirectory: Path,
        moduleRoots: Collection<ModuleRoot>
    ): Collection<ModuleRoot> {
        val moduleRootDirectories = mutableMapOf<String, ModuleRoot>()

        moduleRoots.filter { it.moduleGroup == ModuleGroup.HYBRIS }
            .forEach { file -> addIfNotExists(context, rootDirectory, file, moduleRootDirectories) }

        moduleRoots.filter { it.moduleGroup == ModuleGroup.OTHER }
            .forEach { file -> addIfNotExists(context, rootDirectory, file, moduleRootDirectories) }

        return moduleRootDirectories.values
    }

    private fun addIfNotExists(
        context: ProjectImportContext.Mutable,
        rootDirectory: Path,
        moduleRoot: ModuleRoot,
        moduleRoots: MutableMap<String, ModuleRoot>
    ) {
        val hybrisDistributionDirectory = context.platformDistributionPath
        val externalExtensionsDirectory = context.externalExtensionsDirectory
        try {
            // this will resolve symlinks
            val moduleRootPath = moduleRoot.path
            val canonicalPath = moduleRootPath.toCanonicalPath()
            val currentPath = moduleRoots[canonicalPath]?.path
            if (currentPath == null) {
                moduleRoots[canonicalPath] = moduleRoot
                return
            }

            if (hybrisDistributionDirectory != null && !currentPath.isDescendantOf(hybrisDistributionDirectory)) {
                if (moduleRootPath.isDescendantOf(hybrisDistributionDirectory)) {
                    moduleRoots[canonicalPath] = moduleRoot
                    return
                }
            }
            if (externalExtensionsDirectory != null && !currentPath.isDescendantOf(externalExtensionsDirectory)) {
                if (moduleRootPath.isDescendantOf(externalExtensionsDirectory)) {
                    moduleRoots[canonicalPath] = moduleRoot
                    return
                }
            }
            if (!currentPath.isDescendantOf(rootDirectory)
                && moduleRootPath.isDescendantOf(rootDirectory)
            ) {
                moduleRoots[canonicalPath] = moduleRoot
            }
        } catch (e: IOException) {
            thisLogger().error("Unable to locate ${moduleRoot.path}", e)
        }
    }


    private fun processVcsRoot(directory: Path, context: ProjectImportContext.Mutable) {
        if (directory.isVcs) {
            thisLogger().debug("Detected version control system: $directory")
            context.addVcs(directory)
        }
    }

    private val Path.isDirectoryExcluded
        get() = ProjectImportConstants.excludedFromScanningDirectories.contains(name)
            || endsWith(ProjectConstants.Paths.PLATFORM_BOOTSTRAP)

    private val Path.isVcs
        get() = resolve(ProjectConstants.Directory.GIT).directoryExists
            || resolve(ProjectConstants.Directory.SVN).directoryExists
            || resolve(ProjectConstants.Directory.HG).directoryExists

    companion object {
        fun getInstance(): ModuleRootsScanner = application.service()
    }
}