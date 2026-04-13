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
package sap.commerce.toolset.project.configurator

import com.intellij.openapi.diagnostic.thisLogger
import sap.commerce.toolset.path
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class BackupProjectSettingsConfigurator : ProjectBeforeCreateConfigurator, ProjectPostImportConfigurator {

    override val name: String
        get() = "Backup Project Settings"

    override fun configure(context: ProjectImportContext.Mutable) {
        if (context.refresh) return
        val restoreExistingProjectFiles = context.restoreExistingProjectFiles
        if (restoreExistingProjectFiles.isEmpty()) return

        val tempDirectory = Files.createTempDirectory("project-settings-")
        context.restoreExistingProjectFilesTempDirectory = tempDirectory

        thisLogger().debug("Preserving project settings files to temporary directory: $tempDirectory")

        moveFiles(tempDirectory, restoreExistingProjectFiles)
    }

    override suspend fun configure(context: ProjectPostImportContext) {
        if (context.refresh) return
        val tempDirectory = context.restoreExistingProjectFilesTempDirectory
            ?: return
        val ideaDirectoryPath = context.project.path
            ?.resolve(ProjectConstants.Paths.IDEA)
            ?: return

        thisLogger().debug("Restoring project settings files from the backup: $tempDirectory")

        moveFiles(ideaDirectoryPath, tempDirectory.listDirectoryEntries())

        thisLogger().debug("Removing project settings temporary backup files: $tempDirectory")
        tempDirectory.deleteRecursivelyNoFollow()
    }

    private fun moveFiles(
        targetDirectory: Path,
        fromFiles: Collection<Path>,
    ) = runCatching {
        fromFiles.forEach { source ->
            val target = targetDirectory.resolve(source.name)

            if (source.directoryExists) {
                if (source.isSymbolicLink()) {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS)
                } else {
                    Files.createDirectories(target)
                    source.listDirectoryEntries().forEach { child ->
                        if (child.fileExists) {
                            Files.copy(child, target.resolve(child.name), StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS)
                        } else if (child.directoryExists) {
                            Files.copy(child, target.resolve(child.name), StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS)
                        }
                    }
                }
            } else if (source.fileExists) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS)
            }
        }
    }.onFailure { error ->
        thisLogger().warn("Failed to move files to $targetDirectory", error)
    }

    private fun Path.deleteRecursivelyNoFollow() = Files.walkFileTree(this, object : SimpleFileVisitor<Path>() {

        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            Files.delete(file) // deletes file or symlink itself
            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            if (exc != null) throw exc
            Files.delete(dir)
            return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
            // Fail fast (or switch to CONTINUE if you want best-effort cleanup)
            throw exc
        }
    })
}
