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

package sap.commerce.toolset.project.descriptor

import sap.commerce.toolset.extensioninfo.context.ExtensionInfoContext

interface YModuleDescriptor : ModuleDescriptor {

    val extensionInfo: ExtensionInfoContext
    fun getSubModules(): Set<YSubModuleDescriptor>
    fun addSubModule(subModule: YSubModuleDescriptor): Boolean
    fun removeSubModule(subModule: YSubModuleDescriptor): Boolean
    fun getSpringFiles(): Set<String>
    fun addSpringFile(file: String): Boolean
}