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

package sap.commerce.toolset.java.configurator.contentEntry

import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import org.jetbrains.jps.model.JpsElement
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCustomRegularModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path

internal val ModuleDescriptor.isCustomModuleDescriptor
    get() = this is YCustomRegularModuleDescriptor
        || (this is YSubModuleDescriptor && this.owner is YCustomRegularModuleDescriptor)

internal fun ContentEntry.excludeDirectories(vararg excludePaths: Path) = excludePaths
    .mapNotNull { VfsUtil.findFile(it, true) }
    .forEach { this.addExcludeFolder(it) }

internal fun <P : JpsElement> ContentEntry.addSourceRoots(
    importContext: ProjectImportContext,
    paths: Collection<Path>,
    pathsToIgnore: Collection<Path>,
    jpsRootType: JpsModuleSourceRootType<P>,
    jpsProperties: P = jpsRootType.createDefaultProperties(),
) = paths
    .filter { path -> !importContext.settings.ignoreNonExistingSourceDirectories || path.directoryExists  }
    .filter { path -> pathsToIgnore.none { FileUtil.isAncestor(it.toFile(), path.toFile(), false) } }
    .mapNotNull { path -> VfsUtil.findFile(path, true) }
    .forEach { vf -> this.addSourceFolder(vf, jpsRootType, jpsProperties) }
