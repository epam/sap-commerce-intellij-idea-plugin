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

package com.intellij.idea.plugin.hybris.diagram.module.node.graph

import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.module.Module

object ModuleDepGraphFactory {

    fun buildNode(module: Module) = with(ProjectSettingsComponent.getInstance(module.project).getModuleSettings(module)) {
        val properties = mutableListOf<ModuleDepGraphField>()

        if (description != null) properties.add(ModuleDepGraphFieldDescription(description!!))
        if (version != null) properties.add(ModuleDepGraphFieldParameter("Version", version))

        if (deprecated) properties.add(ModuleDepGraphFieldParameter("Deprecated"))
        if (useMaven) properties.add(ModuleDepGraphFieldParameter("Maven Enabled"))
        if (jaloLogicFree) properties.add(ModuleDepGraphFieldParameter("Jalo Logic Free"))
        if (extGenTemplateExtension) properties.add(ModuleDepGraphFieldParameter("Template Extension"))
        if (requiredByAll) properties.add(ModuleDepGraphFieldParameter("Required by All"))

        if (subModuleType != null) properties.add(ModuleDepGraphFieldParameter("Sub-module Type", subModuleType!!.name))
        if (moduleGenName != null) properties.add(ModuleDepGraphFieldParameter("Module Generation Name", moduleGenName))
        if (classPathGen != null) properties.add(ModuleDepGraphFieldParameter("Classpath Generation", classPathGen))

        if (coreModule) properties.add(ModuleDepGraphFieldParameter("Core module", packageRoot))
        if (webModule) properties.add(ModuleDepGraphFieldParameter("Web module", webRoot))

        if (backofficeModule) properties.add(ModuleDepGraphFieldParameter("Backoffice module"))
        if (hacModule) properties.add(ModuleDepGraphFieldParameter("HAC module"))
        if (hmcModule) properties.add(ModuleDepGraphFieldParameter("HMC module"))
        if (addon) properties.add(ModuleDepGraphFieldParameter("Addon"))

        ModuleDepGraphNodeModule(
            module,
            type,
            subModuleType,
            module.yExtensionName(),
            properties.toTypedArray()
        )
    }
}