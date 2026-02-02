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

package sap.commerce.toolset.java.decompilation

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.workspace.jps.entities.LibraryId
import com.intellij.platform.workspace.jps.entities.LibraryRoot
import sap.commerce.toolset.project.descriptor.ModuleDescriptor

/**
 * Context for decompiling a single JAR file.
 *
 * @param moduleDescriptor Module owning this JAR (used to resolve the output directory).
 * @param libraryId The library id used to attach sources back.
 * @param libraryRoot The COMPILED library root representing this JAR.
 * @param jar Jar root in the VFS.
 */
data class JarDecompileContext(
    val moduleDescriptor: ModuleDescriptor,
    val libraryId: LibraryId,
    val libraryRoot: LibraryRoot,
    val jar: VirtualFile,
)
