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

package sap.commerce.toolset.ccv2.manifest.jsonSchema.providers

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.isHybrisProject

@Service(Service.Level.PROJECT)
class ManifestJavascriptStorefrontJsonSchemaFileProvider(val project: Project) : JsonSchemaFileProvider {
    override fun isAvailable(file: VirtualFile) = CCv2Constants.MANIFEST_NAME == file.name
            && CCv2Constants.JS_STOREFRONT_NAME == file.parent?.name
            && project.isHybrisProject

    override fun getName() = "SAP Javascript Storefront Manifest"
    override fun getSchemaFile() = JsonSchemaProviderFactory.getResourceFile(javaClass, "/schemas/manifest-js-storefront.schema.json")
    override fun getSchemaType() = SchemaType.embeddedSchema

    companion object {
        fun instance(project: Project): ManifestJavascriptStorefrontJsonSchemaFileProvider = project.service()
    }
}