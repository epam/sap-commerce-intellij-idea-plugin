/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.project

import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.settings.ProjectSettings
import java.io.File

interface HybrisProjectImportBuilder {

    var hybrisModulesToImport: MutableList<ModuleDescriptor>

    fun getHybrisProjectDescriptor(): HybrisProjectDescriptor
    fun setRootProjectDirectory(directory: File)
    fun setAllModuleList()
    fun getBestMatchingExtensionsToImport(settings: ProjectSettings): MutableList<ModuleDescriptor>
    fun setCoreStepModuleList()
    fun setExternalStepModuleList()
}
