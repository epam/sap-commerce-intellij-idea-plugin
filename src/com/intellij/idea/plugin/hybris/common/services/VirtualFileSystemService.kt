/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.common.services

import com.intellij.idea.plugin.hybris.project.tasks.TaskProgressProcessor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.IOException

@Service
class VirtualFileSystemService {

    @Throws(IOException::class)
    fun removeAllFiles(files: Collection<File>) {
        if (files.isEmpty()) return

        val localFileSystem = LocalFileSystem.getInstance()

        val virtualFiles = mutableListOf<VirtualFile>()
        val nonVirtualFiles = mutableListOf<File>()

        for (file in files) {
            val virtualFile = localFileSystem.findFileByIoFile(file)
            if (null != virtualFile) {
                virtualFiles.add(virtualFile)
            } else {
                nonVirtualFiles.add(file)
            }
        }

        nonVirtualFiles.forEach { FileUtil.delete(it) }

        virtualFiles
            .takeIf { it.isNotEmpty() }
            ?.let {
                ApplicationManager.getApplication().runWriteAction {
                    object : ThrowableComputable<Unit, IOException> {
                        override fun compute() = it.forEach { vf -> vf.delete(this) }
                    }
                }
            }
    }

    @Throws(InterruptedException::class)
    fun findFileByNameInDirectory(
        directory: File,
        fileName: String,
        progressListenerProcessor: TaskProgressProcessor<File>,
    ): File? {
        val result = Ref.create<File>()
        val interrupted = Ref.create(false)

        FileUtil.processFilesRecursively(directory) { file ->
            if (!progressListenerProcessor.shouldContinue(directory)) {
                interrupted.set(true)
                return@processFilesRecursively false
            }
            if (file.absolutePath.endsWith(fileName)) {
                result.set(file)
                return@processFilesRecursively false
            }
            true
        }

        if (interrupted.get()) {
            throw InterruptedException("Modules scanning has been interrupted.")
        }
        return result.get()
    }

    fun fileContainsAnother(parent: File, child: File) = pathContainsAnother(parent.absolutePath, child.absolutePath)

    fun getRelativePath(parent: File, child: File) = getRelativePath(parent.path, child.path)

    private fun pathContainsAnother(parent: String, child: String): Boolean = FileUtil.normalize(child)
        .startsWith(FileUtil.normalize(parent))

    private fun getRelativePath(parent: String, child: String) = FileUtil.normalize(child)
        .substring(FileUtil.normalize(parent).length)

    companion object {
        @JvmStatic
        fun getInstance(): VirtualFileSystemService = ApplicationManager.getApplication().getService(VirtualFileSystemService::class.java)
    }
}
