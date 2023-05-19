/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.common.services;

import com.intellij.idea.plugin.hybris.project.tasks.TaskProgressProcessor
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.IOException

interface VirtualFileSystemService {

    @Throws(IOException::class)
    fun removeAllFiles(files: Collection<File>)

    fun getByUrl(url : String): VirtualFile?

    @Throws(InterruptedException::class)
    fun findFileByNameInDirectory(
        directory : File,
        fileName : String,
        progressListenerProcessor : TaskProgressProcessor<File>,
    ): File

    fun fileContainsAnother(parent : File, child : File): Boolean

    fun fileDoesNotContainAnother(parent : File, child : File): Boolean

    fun pathContainsAnother(parent : String, child : String): Boolean

    fun pathDoesNotContainAnother(parent : String, child : String): Boolean

    fun getRelativePath(parent : File, child : File): String

    fun getRelativePath(parent : String, child : String): String
}
