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

package sap.commerce.toolset.java.descriptor

import com.intellij.java.workspace.entities.JavaResourceRootPropertiesEntityBuilder
import com.intellij.java.workspace.entities.JavaSourceRootPropertiesEntityBuilder
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.SourceRootEntityBuilder
import com.intellij.platform.workspace.jps.entities.SourceRootTypeId
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import java.nio.file.Path
import kotlin.io.path.pathString

internal class SourceRootEntityDescriptor(
    val sourceRootTypeId: SourceRootTypeId,
    val moduleEntity: ModuleEntity,
    val path: Path,
    val javaSourceRoot: ((SourceRootEntityBuilder) -> JavaSourceRootPropertiesEntityBuilder)? = null,
    val javaResourceRoot: ((SourceRootEntityBuilder) -> JavaResourceRootPropertiesEntityBuilder)? = null,
) {
    fun url(virtualFileUrlManager: VirtualFileUrlManager): VirtualFileUrl = virtualFileUrlManager.fromPath(path.pathString)
}