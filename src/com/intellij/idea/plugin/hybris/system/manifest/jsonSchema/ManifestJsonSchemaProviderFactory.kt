/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.system.manifest.jsonSchema

import com.intellij.idea.plugin.hybris.system.manifest.jsonSchema.providers.ManifestCommerceJsonSchemaFileProvider
import com.intellij.idea.plugin.hybris.system.manifest.jsonSchema.providers.ManifestDataHubJsonSchemaFileProvider
import com.intellij.idea.plugin.hybris.system.manifest.jsonSchema.providers.ManifestJavascriptStorefrontJsonSchemaFileProvider
import com.intellij.openapi.project.Project
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory

class ManifestJsonSchemaProviderFactory : JsonSchemaProviderFactory {

    override fun getProviders(project: Project) = listOf(
            ManifestCommerceJsonSchemaFileProvider.instance(project),
            ManifestDataHubJsonSchemaFileProvider.instance(project),
            ManifestJavascriptStorefrontJsonSchemaFileProvider.instance(project)
    )

}
