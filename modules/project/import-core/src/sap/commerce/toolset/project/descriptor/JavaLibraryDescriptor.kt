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

package sap.commerce.toolset.project.descriptor

import com.intellij.openapi.roots.DependencyScope
import java.io.File

data class JavaLibraryDescriptor(
    var name: String? = null,
    var libraryFile: File,
    var jarFiles: Set<File> = emptySet(),
    var sourceFiles: List<File> = emptyList(),
    var sourceJarDirectories: List<File> = emptyList(),
    var exported: Boolean = false,
    var directoryWithClasses: Boolean = false,
    var descriptorType: LibraryDescriptorType = LibraryDescriptorType.UNKNOWN,
    var scope: DependencyScope = DependencyScope.COMPILE
)