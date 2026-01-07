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

import com.intellij.java.workspace.entities.JavaResourceRootPropertiesEntity
import com.intellij.java.workspace.entities.JavaResourceRootPropertiesEntityBuilder
import com.intellij.java.workspace.entities.JavaSourceRootPropertiesEntity
import com.intellij.java.workspace.entities.JavaSourceRootPropertiesEntityBuilder
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.SourceRootEntityBuilder
import com.intellij.platform.workspace.jps.entities.SourceRootTypeId
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.workspaceModel.ide.legacyBridge.impl.java.JAVA_RESOURCE_ROOT_ENTITY_TYPE_ID
import com.intellij.workspaceModel.ide.legacyBridge.impl.java.JAVA_SOURCE_ROOT_ENTITY_TYPE_ID
import com.intellij.workspaceModel.ide.legacyBridge.impl.java.JAVA_TEST_RESOURCE_ROOT_ENTITY_TYPE_ID
import com.intellij.workspaceModel.ide.legacyBridge.impl.java.JAVA_TEST_ROOT_ENTITY_TYPE_ID
import java.nio.file.Path
import kotlin.io.path.pathString

internal class SourceRootEntityDescriptor private constructor(
    val sourceRootTypeId: SourceRootTypeId,
    val moduleEntity: ModuleEntity,
    val path: Path,
    val javaSourceRoot: ((SourceRootEntityBuilder) -> JavaSourceRootPropertiesEntityBuilder)? = null,
    val javaResourceRoot: ((SourceRootEntityBuilder) -> JavaResourceRootPropertiesEntityBuilder)? = null,
) {
    fun url(virtualFileUrlManager: VirtualFileUrlManager) = virtualFileUrlManager.fromPath(path.pathString)

    companion object {

        fun sources(
            sourceRootTypeId: SourceRootTypeId = JAVA_SOURCE_ROOT_ENTITY_TYPE_ID,
            moduleEntity: ModuleEntity,
            path: Path,
            generated: Boolean = false,
            packagePrefix: String = ""
        ) = SourceRootEntityDescriptor(
            sourceRootTypeId = sourceRootTypeId,
            moduleEntity = moduleEntity,
            path = path,
            javaSourceRoot = { JavaSourceRootPropertiesEntity(generated, packagePrefix, it.entitySource) }
        )

        fun resources(
            sourceRootTypeId: SourceRootTypeId = JAVA_RESOURCE_ROOT_ENTITY_TYPE_ID,
            moduleEntity: ModuleEntity,
            path: Path,
            generated: Boolean = false,
            relativeOutputPath: String = ""
        ) = SourceRootEntityDescriptor(
            sourceRootTypeId = sourceRootTypeId,
            moduleEntity = moduleEntity,
            path = path,
            javaResourceRoot = { JavaResourceRootPropertiesEntity(generated, relativeOutputPath, it.entitySource) }
        )

        fun generatedSources(moduleEntity: ModuleEntity, path: Path) = sources(
            moduleEntity = moduleEntity,
            path = path,
            generated = true
        )

        fun testSources(moduleEntity: ModuleEntity, path: Path) = sources(
            sourceRootTypeId = JAVA_TEST_ROOT_ENTITY_TYPE_ID,
            moduleEntity = moduleEntity,
            path = path
        )

        fun testResources(moduleEntity: ModuleEntity, path: Path) = resources(
            sourceRootTypeId = JAVA_TEST_RESOURCE_ROOT_ENTITY_TYPE_ID,
            moduleEntity = moduleEntity,
            path = path
        )
    }
}