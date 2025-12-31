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

package sap.commerce.toolset.project.vfs

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.pathString

// TODO: review / remove / refactor
@Service
class VirtualFileSystemService {

    @Throws(IOException::class)
    fun removeAllFiles(files: Collection<Path>) {
        if (files.isEmpty()) return

        val localFileSystem = LocalFileSystem.getInstance()

        val virtualFiles = mutableListOf<VirtualFile>()
        val nonVirtualFiles = mutableListOf<Path>()

        for (file in files) {
            val virtualFile = localFileSystem.findFileByNioFile(file)
            if (null != virtualFile) {
                virtualFiles.add(virtualFile)
            } else {
                nonVirtualFiles.add(file)
            }
        }

        nonVirtualFiles.forEach { Files.delete(it) }

        virtualFiles
            .takeIf { it.isNotEmpty() }
            ?.let {
                application.runWriteAction {
                    object : ThrowableComputable<Unit, IOException> {
                        override fun compute() = it.forEach { vf -> vf.delete(this) }
                    }
                }
            }
    }

    fun getRelativePath(parent: Path, child: Path) = getRelativePath(parent.pathString, child.pathString)

    private fun getRelativePath(parent: String, child: String) = FileUtil.normalize(child)
        .substring(FileUtil.normalize(parent).length)

    companion object {
        @JvmStatic
        fun getInstance(): VirtualFileSystemService = application.service()
    }
}