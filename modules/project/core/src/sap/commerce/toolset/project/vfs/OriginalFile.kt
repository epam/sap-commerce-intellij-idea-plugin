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

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile

/**
 * A file may stand in for a part of another file, e.g. the editor of a single value of an ImpEx file edits a copy of the value line.
 * The access to such a file is the access to the original one, see [HybrisWritingAccessProvider].
 */
object OriginalFile {

    private val KEY = Key.create<VirtualFile>("sap.commerce.toolset.vfs.original_file")

    fun attach(file: VirtualFile, original: VirtualFile) {
        file.putUserData(KEY, original)
    }

    fun of(file: VirtualFile): VirtualFile? = file.getUserData(KEY)
}
