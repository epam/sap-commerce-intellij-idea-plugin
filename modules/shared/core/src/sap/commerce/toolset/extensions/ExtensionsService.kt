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

package sap.commerce.toolset.extensions

import com.intellij.ide.extensionResources.ExtensionsRootType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import java.nio.file.Files

/**
 * All resources located within the `resources/extensions` will be copied to the target IDE to the `config/extensions/<plugin_id>` folder.
 * The goal is to grant a chance to an end-user to adjust default scripts used by the Plugin to align with possible project specifics.
 */
@Service
class ExtensionsService {

    fun findResource(fqn: String): String {
        val extensionsRootType = ExtensionsRootType.getInstance()
        val pluginId = Plugin.HYBRIS.pluginId
        var path = extensionsRootType.findResource(pluginId, fqn)

        if (path == null || !Files.exists(path)) {
            extensionsRootType.extractBundledResources(pluginId, "")
            path = extensionsRootType.findResource(pluginId, fqn)
        }

        return path?.takeIf { Files.exists(it) }
            ?.let { Files.readString(it) }
            ?: throw HybrisConfigurationException("Unable to read Extension file: $fqn.")
    }

    companion object {
        fun getInstance(): ExtensionsService = application.service()
    }
}