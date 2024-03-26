/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.manifest.jsonSchema.providers

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType

@Service(Service.Level.PROJECT)
class ManifestCommerceJsonSchemaFileProvider(val project: Project) : JsonSchemaFileProvider {

    override fun isAvailable(file: VirtualFile) = HybrisConstants.CCV2_MANIFEST_NAME == file.name
            && HybrisConstants.CCV2_CORE_CUSTOMIZE_NAME == file.parent?.name
            && ProjectSettingsComponent.getInstance(project).isHybrisProject()

    override fun getName() = "SAP Commerce Manifest"
    override fun getSchemaFile() = JsonSchemaProviderFactory.getResourceFile(javaClass, "/schemas/manifest-commerce.schema.json")
    override fun getSchemaType() = SchemaType.embeddedSchema

    companion object {
        fun instance(project: Project): JsonSchemaFileProvider = project.getService(ManifestCommerceJsonSchemaFileProvider::class.java)
    }
}