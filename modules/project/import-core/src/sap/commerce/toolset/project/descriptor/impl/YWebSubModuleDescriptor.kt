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

package sap.commerce.toolset.project.descriptor.impl

import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.SubModuleDescriptorType
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.YRegularModuleDescriptor
import java.nio.file.Path
import kotlin.io.path.name

class YWebSubModuleDescriptor(
    owner: YRegularModuleDescriptor,
    moduleRootPath: Path,
    name: String = owner.name + "." + moduleRootPath.name,
    val webRoot: Path = moduleRootPath.resolve(ProjectConstants.Directory.WEB_ROOT),
    override val subModuleDescriptorType: SubModuleDescriptorType = SubModuleDescriptorType.WEB
) : AbstractYSubModuleDescriptor(owner, moduleRootPath, name) {

    override fun addDirectDependencies(dependencies: Collection<ModuleDescriptor>): Boolean {
        dependencies
            .asSequence()
            .filterIsInstance<YModuleDescriptor>()
            .flatMap { it.getAllDependencies() }
            .filterIsInstance<YCustomRegularModuleDescriptor>()
            .flatMap { it.getSubModules() }
            .filterIsInstance<YCommonWebSubModuleDescriptor>()
            .toList()
            .forEach { it.addDependantWebExtension(this) }
        return super.addDirectDependencies(dependencies)
    }
}