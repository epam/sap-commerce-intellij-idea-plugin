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

package sap.commerce.toolset.util

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.platform.util.progress.reportSequentialProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.*

/*
This function will process files in the directory before going deeper
 */
suspend fun Path.findRecursivelyOptimized(
    ignoredDirNames: Collection<String>,
    filter: (Path) -> Boolean
): Path? = reportSequentialProgress { reporter ->
    withContext(Dispatchers.IO) {
        val queue = ArrayDeque<Path>()
        queue.add(this@findRecursivelyOptimized)

        while (queue.isNotEmpty()) {
            coroutineContext.ensureActive()

            val currentDir = queue.removeFirst()

            if (currentDir.directoryExists) {
                reporter.indeterminateStep("Scanning directory: ${currentDir.pathString}")

                val files = mutableListOf<Path>()
                val dirs = mutableListOf<Path>()

                currentDir.listDirectoryEntries().forEach { path ->
                    coroutineContext.ensureActive()
                    if (path.directoryExists) {
                        if (!ignoredDirNames.contains(path.name)) {
                            dirs.add(path)
                        }
                    } else {
                        files.add(path)
                    }
                }

                for (file in files) {
                    coroutineContext.ensureActive()
                    if (filter(file)) return@withContext file
                }

                queue.addAll(dirs)
            } else {
                if (filter(currentDir)) return@withContext currentDir
            }
        }
        null
    }
}

val Path.directoryExists
    get() = this.exists() && this.isDirectory()

val Path.fileExists
    get() = this.exists() && this.isRegularFile()

fun Path?.isDescendantOf(parent: Path): Boolean =
    try {
        val realParent = parent.toRealPath().normalize()
        val realChild = this?.toRealPath()?.normalize()
        realChild?.startsWith(realParent) ?: false
    } catch (e: IOException) {
        this?.thisLogger()?.warn("Can't find $parent in $this", e)
        false
    }

val Path.toSystemIndependentName
    get() = FileUtil.toSystemIndependentName(pathString)
