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

package sap.commerce.toolset.project.diagram.node.graph

import com.intellij.openapi.module.Module
import sap.commerce.toolset.project.facet.YFacetConstants
import sap.commerce.toolset.project.yExtensionName

object ModuleDepGraphFactory {

    fun buildNode(module: Module) = with(YFacetConstants.getModuleSettings(module)) {
        val info = getInfo()
        val properties = buildList {
            info?.description?.let { add(ModuleDepGraphFieldDescription(it)) }
            info?.version?.let { add(ModuleDepGraphFieldParameter("Version", it)) }

            if (info?.deprecated ?: false) add(ModuleDepGraphFieldParameter("Deprecated"))
            if (info?.useMaven ?: false) add(ModuleDepGraphFieldParameter("Maven Enabled"))
            if (info?.jaloLogicFree ?: false) add(ModuleDepGraphFieldParameter("Jalo Logic Free"))
            if (info?.extGenTemplateExtension ?: false) add(ModuleDepGraphFieldParameter("Template Extension"))
            if (info?.requiredByAll ?: false) add(ModuleDepGraphFieldParameter("Required by All"))

            subModuleType?.let { add(ModuleDepGraphFieldParameter("Sub-module Type", it.name)) }
            if (info?.moduleGenName != null) add(ModuleDepGraphFieldParameter("Module Generation Name", info.moduleGenName))
            if (info?.classPathGen != null) add(ModuleDepGraphFieldParameter("Classpath Generation", info.classPathGen))

            if (info?.coreModule ?: false) add(ModuleDepGraphFieldParameter("Core module", info.packageRoot))
            if (info?.webModule ?: false) add(ModuleDepGraphFieldParameter("Web module", info.webRoot))

            if (info?.backofficeModule ?: false) add(ModuleDepGraphFieldParameter("Backoffice module"))
            if (info?.hacModule ?: false) add(ModuleDepGraphFieldParameter("HAC module"))
            if (info?.hmcModule ?: false) add(ModuleDepGraphFieldParameter("HMC module"))
            if (addon) add(ModuleDepGraphFieldParameter("Addon"))
        }

        ModuleDepGraphNodeModule(
            module,
            type,
            subModuleType,
            module.yExtensionName(),
            properties.toTypedArray()
        )
    }
}