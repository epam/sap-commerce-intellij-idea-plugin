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

import com.intellij.java.workspace.entities.javaResourceRoots
import com.intellij.java.workspace.entities.javaSourceRoots
import com.intellij.openapi.util.io.FileUtil
import com.intellij.platform.workspace.jps.entities.ContentRootEntityBuilder
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import com.intellij.platform.workspace.jps.entities.SourceRootEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import sap.commerce.toolset.java.descriptor.SourceRootEntityDescriptor
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCustomRegularModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path
import kotlin.io.path.pathString

internal val ModuleDescriptor.isCustomModuleDescriptor
    get() = this is YCustomRegularModuleDescriptor
        || (this is YSubModuleDescriptor && this.owner is YCustomRegularModuleDescriptor)

internal fun ContentRootEntityBuilder.excludeDirectories(
    importContext: ProjectImportContext,
    virtualFileUrlManager: VirtualFileUrlManager,
    excludePaths: Collection<Path>
) = excludePaths
    .filter { path -> !importContext.settings.ignoreNonExistingSourceDirectories || path.directoryExists }
    .map { virtualFileUrlManager.fromPath(it.pathString) }
    .map { ExcludeUrlEntity(it, this.entitySource) }
    .let { newExcludedUrls -> this.excludedUrls += newExcludedUrls }

internal fun ContentRootEntityBuilder.addSourceRoots(
    importContext: ProjectImportContext,
    virtualFileUrlManager: VirtualFileUrlManager,
    rootEntities: Collection<SourceRootEntityDescriptor>,
    pathsToIgnore: Collection<Path>,
) = rootEntities
    .filter { rootEntity -> !importContext.settings.ignoreNonExistingSourceDirectories || rootEntity.path.directoryExists }
    .filter { rootEntity -> pathsToIgnore.none { FileUtil.isAncestor(it.toFile(), rootEntity.path.toFile(), false) } }
    .map { rootEntity ->
        SourceRootEntity(
            url = rootEntity.url(virtualFileUrlManager),
            rootTypeId = rootEntity.sourceRootTypeId,
            entitySource = rootEntity.moduleEntity.entitySource
        ) {
            rootEntity.javaSourceRoot?.let { it(this) }
                ?.let { this.javaSourceRoots += it }
            rootEntity.javaResourceRoot?.let { it(this) }
                ?.let { this.javaResourceRoots += it }
        }
    }
    .let { newSourceRoots -> this.sourceRoots += newSourceRoots }

