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

package sap.commerce.toolset.java.descriptor

import com.intellij.openapi.roots.DependencyScope
import sap.commerce.toolset.project.descriptor.LibraryDescriptorType
import java.nio.file.Path

data class JavaLibraryDescriptor(
    var name: String? = null,
    var libraryFile: Path,
    var jarFiles: Set<Path> = emptySet(),
    var sourceFiles: List<Path> = emptyList(),
    var sourceJarDirectories: List<Path> = emptyList(),
    var exported: Boolean = false,
    var directoryWithClasses: Boolean = false,
    var descriptorType: LibraryDescriptorType = LibraryDescriptorType.UNKNOWN,
    var scope: DependencyScope = DependencyScope.COMPILE
)