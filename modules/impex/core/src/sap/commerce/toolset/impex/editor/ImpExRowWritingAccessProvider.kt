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

package sap.commerce.toolset.impex.editor

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.WritingAccessProvider

/**
 * An attempt to type into a field of the row mode is the attempt to modify the source file,
 * so the same restrictions (files outside of the project, version control, etc.) have to be applied as for the editor of the source file.
 */
class ImpExRowWritingAccessProvider(private val project: Project) : WritingAccessProvider() {

    override fun requestWriting(files: Collection<VirtualFile>): Collection<VirtualFile> {
        val rowFiles = files.mapNotNull { file -> ImpExRowContext.sourceFileOf(file)?.let { file to it } }
        if (rowFiles.isEmpty()) return emptyList()

        val readonlySources = ReadonlyStatusHandler.getInstance(project)
            .ensureFilesWritable(rowFiles.map { it.second }.distinct())
            .readonlyFiles
            .toSet()

        return rowFiles
            .filter { (_, source) -> source in readonlySources }
            .map { (file, _) -> file }
    }

    override fun isPotentiallyWritable(file: VirtualFile): Boolean = ImpExRowContext.sourceFileOf(file)
        ?.let { it.isWritable && isPotentiallyWritable(it, project) }
        ?: true
}
