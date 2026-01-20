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
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.yExtensionDescriptor

object ModuleDepGraphFactory {

    fun buildNode(module: Module) = module.yExtensionDescriptor?.let { buildNode(module, it) }

    private fun buildNode(module: Module, extensionDescriptor: ExtensionDescriptor) = with(extensionDescriptor) {
        val eiContext = getContext()
        val properties = buildList {
            eiContext?.description?.let { add(ModuleDepGraphFieldDescription(it)) }
            eiContext?.version?.let { add(ModuleDepGraphFieldParameter("Version", it)) }

            if (eiContext?.deprecated ?: false) add(ModuleDepGraphFieldParameter("Deprecated"))
            if (eiContext?.useMaven ?: false) add(ModuleDepGraphFieldParameter("Maven Enabled"))
            if (eiContext?.jaloLogicFree ?: false) add(ModuleDepGraphFieldParameter("Jalo Logic Free"))
            if (eiContext?.extGenTemplateExtension ?: false) add(ModuleDepGraphFieldParameter("Template Extension"))
            if (eiContext?.requiredByAll ?: false) add(ModuleDepGraphFieldParameter("Required by All"))

            subModuleType?.let { add(ModuleDepGraphFieldParameter("Sub-module Type", it.name)) }
            if (eiContext?.moduleGenName != null) add(ModuleDepGraphFieldParameter("Module Generation Name", eiContext.moduleGenName))
            if (eiContext?.classPathGen != null) add(ModuleDepGraphFieldParameter("Classpath Generation", eiContext.classPathGen))

            if (eiContext?.coreModule ?: false) add(ModuleDepGraphFieldParameter("Core module", eiContext.packageRoot))
            if (eiContext?.webModule ?: false) add(ModuleDepGraphFieldParameter("Web module", eiContext.webRoot))

            if (eiContext?.backofficeModule ?: false) add(ModuleDepGraphFieldParameter("Backoffice module"))
            if (eiContext?.hacModule ?: false) add(ModuleDepGraphFieldParameter("HAC module"))
            if (eiContext?.hmcModule ?: false) add(ModuleDepGraphFieldParameter("HMC module"))
            if (addon) add(ModuleDepGraphFieldParameter("Addon"))
        }

        ModuleDepGraphNodeModule(
            module,
            type,
            subModuleType,
            extensionDescriptor.name,
            properties.toTypedArray()
        )
    }
}