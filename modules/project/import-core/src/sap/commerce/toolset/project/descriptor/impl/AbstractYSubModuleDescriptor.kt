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

import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import java.nio.file.Path
import kotlin.io.path.name

abstract class AbstractYSubModuleDescriptor(
    override val owner: YRegularModuleDescriptor,
    override val moduleRootPath: Path,
    override val name: String = owner.name + "." + moduleRootPath.name,
) : AbstractYModuleDescriptor(
    moduleRootPath = moduleRootPath,
    name = name,
    descriptorType = owner.type,
    extensionInfo = owner.extensionInfo
), YSubModuleDescriptor {

    override fun initDependencies(moduleDescriptors: Map<String, Collection<ModuleDescriptor>>) = setOf(owner.name)
    override fun isPreselected() = owner.isPreselected()
}