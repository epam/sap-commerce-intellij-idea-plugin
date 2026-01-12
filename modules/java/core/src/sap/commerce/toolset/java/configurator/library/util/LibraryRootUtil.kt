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

package sap.commerce.toolset.java.configurator.library.util

import com.intellij.platform.workspace.jps.entities.LibraryRoot
import com.intellij.platform.workspace.jps.entities.LibraryRoot.InclusionOptions
import com.intellij.platform.workspace.jps.entities.LibraryRootTypeId
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import sap.commerce.toolset.project.fromJar
import sap.commerce.toolset.project.fromPath
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

fun ModuleDescriptor.lib(virtualFileUrlManager: VirtualFileUrlManager) = this.compiledArchives(
    virtualFileUrlManager, Path(ProjectConstants.Directory.LIB)
)

fun ModuleDescriptor.classes(virtualFileUrlManager: VirtualFileUrlManager) = this.compiled(
    virtualFileUrlManager, Path(ProjectConstants.Directory.CLASSES)
)

fun ModuleDescriptor.testClasses(virtualFileUrlManager: VirtualFileUrlManager) = this.compiled(
    virtualFileUrlManager, Path(ProjectConstants.Directory.TEST_CLASSES)
)

fun ModuleDescriptor.resources(virtualFileUrlManager: VirtualFileUrlManager) = this.compiled(
    virtualFileUrlManager, Path(ProjectConstants.Directory.RESOURCES)
)

fun ModuleDescriptor.sources(virtualFileUrlManager: VirtualFileUrlManager) = this.sources(
    virtualFileUrlManager, *ProjectConstants.Directory.ALL_SRC_DIR_NAMES.map { Path(it) }.toTypedArray()
)

fun ModuleDescriptor.testSources(virtualFileUrlManager: VirtualFileUrlManager) = this.sources(
    virtualFileUrlManager, *ProjectConstants.Directory.TEST_SRC_DIR_NAMES.map { Path(it) }.toTypedArray()
)

// we have to add each jar file explicitly, otherwise Spring will not recognise `classpath:/META-INF/my.xml` in the jar files
// JetBrains IntelliJ IDEA issue - https://youtrack.jetbrains.com/issue/IDEA-257819
fun ModuleDescriptor.webRootJars(virtualFileUrlManager: VirtualFileUrlManager) = this.compiledJars(
    virtualFileUrlManager, ProjectConstants.Paths.WEBROOT_WEB_INF_LIB
)

fun ModuleDescriptor.webRootClasses(virtualFileUrlManager: VirtualFileUrlManager) = this.compiled(
    virtualFileUrlManager, ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES
)

fun ModuleDescriptor.nestedSources(virtualFileUrlManager: VirtualFileUrlManager, path: String) = this
    .moduleRootPath.resolve(path)
    .takeIf { it.directoryExists }
    ?.listDirectoryEntries()
    ?.mapNotNull { virtualFileUrlManager.fromPath(it) }
    ?.map { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
    ?: emptyList()

fun ModuleDescriptor.serverJarFiles(virtualFileUrlManager: VirtualFileUrlManager) = this
    .moduleRootPath.resolve(ProjectConstants.Directory.BIN)
    .takeIf { it.directoryExists }
    ?.listDirectoryEntries()
    ?.filter { it.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }
    ?.mapNotNull { virtualFileUrlManager.fromJar(it) }
    ?.map { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
    ?: emptyList()

fun ModuleDescriptor.docSources(virtualFileUrlManager: VirtualFileUrlManager): List<LibraryRoot> {
    val rootDescriptor = this.asSafely<YSubModuleDescriptor>()?.owner
        ?: this

    return rootDescriptor.sourcesArchives(virtualFileUrlManager, ProjectConstants.Paths.DOC_SOURCES)
}

fun ModuleDescriptor.compiledJars(virtualFileUrlManager: VirtualFileUrlManager, vararg paths: Path) = paths
    .asSequence()
    .map { this.moduleRootPath.resolve(it) }
    .filter { it.directoryExists }
    .flatMap { it.listDirectoryEntries() }
    .filter { it.extension == "jar" }
    .mapNotNull { virtualFileUrlManager.fromJar(it) }
    .map { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
    .toList()

fun ModuleDescriptor.compiled(virtualFileUrlManager: VirtualFileUrlManager, vararg paths: Path) = this.libraryRoots(
    virtualFileUrlManager, LibraryRootTypeId.COMPILED, InclusionOptions.ROOT_ITSELF, *paths
)

fun ModuleDescriptor.compiledArchives(virtualFileUrlManager: VirtualFileUrlManager, vararg paths: Path) = this.libraryRoots(
    virtualFileUrlManager, LibraryRootTypeId.COMPILED, InclusionOptions.ARCHIVES_UNDER_ROOT, *paths
)

fun ModuleDescriptor.compiledArchivesRecursively(virtualFileUrlManager: VirtualFileUrlManager, vararg paths: Path) = this.libraryRoots(
    virtualFileUrlManager, LibraryRootTypeId.COMPILED, InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY, *paths
)

fun ModuleDescriptor.sources(virtualFileUrlManager: VirtualFileUrlManager, vararg paths: Path) = this.libraryRoots(
    virtualFileUrlManager, LibraryRootTypeId.SOURCES, InclusionOptions.ROOT_ITSELF, *paths
)

fun ModuleDescriptor.sourcesArchives(virtualFileUrlManager: VirtualFileUrlManager, vararg paths: Path) = this.libraryRoots(
    virtualFileUrlManager, LibraryRootTypeId.SOURCES, InclusionOptions.ARCHIVES_UNDER_ROOT, *paths
)

private fun ModuleDescriptor.libraryRoots(
    virtualFileUrlManager: VirtualFileUrlManager,
    type: LibraryRootTypeId,
    inclusionOptions: InclusionOptions,
    vararg paths: Path
) = paths
    .mapNotNull { virtualFileUrlManager.fromPath(moduleRootPath.resolve(it)) }
    .map { LibraryRoot(it, type, inclusionOptions) }

