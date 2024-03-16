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
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.module.Module

object ModuleDepGraphFactory {

    fun buildNode(module: Module): ModuleDepGraphNodeModule {
        val properties = mutableListOf<ModuleDepGraphField>()

        val moduleSettings = HybrisProjectSettingsComponent.getInstance(module.project).getModuleSettings(module)

        if (moduleSettings.description != null) properties.add(ModuleDepGraphFieldDescription(moduleSettings.description!!))
        if (moduleSettings.subModuleType != null) properties.add(ModuleDepGraphFieldSubModuleType("Sub-module Type", moduleSettings.subModuleType!!.name))
        if (moduleSettings.deprecated) properties.add(ModuleDepGraphFieldParameter("Deprecated"))
        if (moduleSettings.useMaven) properties.add(ModuleDepGraphFieldParameter("Maven Enabled"))
        if (moduleSettings.jaloLogicFree) properties.add(ModuleDepGraphFieldParameter("Jalo Logic Free"))
        if (moduleSettings.extGenTemplateExtension) properties.add(ModuleDepGraphFieldParameter("Template Extension"))
        if (moduleSettings.moduleGenName != null) properties.add(ModuleDepGraphFieldParameter("Module Generation Name"))
        if (moduleSettings.hacModule) properties.add(ModuleDepGraphFieldParameter("HAC module"))
        if (moduleSettings.webModule) properties.add(ModuleDepGraphFieldParameter("Web module"))
        if (moduleSettings.hmcModule) properties.add(ModuleDepGraphFieldParameter("HMC module"))
        if (moduleSettings.backofficeModule) properties.add(ModuleDepGraphFieldParameter("Backoffice module"))
        if (moduleSettings.addon) properties.add(ModuleDepGraphFieldParameter("Addon"))
        if (moduleSettings.classPathGen != null) properties.add(ModuleDepGraphFieldParameter("Classpath Generation"))

        return ModuleDepGraphNodeModule(
            module,
            moduleSettings.type,
            moduleSettings.subModuleType,
            module.yExtensionName(),
            properties.toTypedArray()
        )
    }
}