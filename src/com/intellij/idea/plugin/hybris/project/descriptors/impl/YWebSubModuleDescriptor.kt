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

package com.intellij.idea.plugin.hybris.project.descriptors.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.SubModuleDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import java.io.File


class YWebSubModuleDescriptor(
    owner: YRegularModuleDescriptor,
    moduleRootDirectory: File,
    name: String = owner.name + "." + moduleRootDirectory.name,
    val webRoot: File = File(moduleRootDirectory, HybrisConstants.WEB_ROOT_DIRECTORY),
    override val subModuleDescriptorType: SubModuleDescriptorType = SubModuleDescriptorType.WEB
) : AbstractYSubModuleDescriptor(owner, moduleRootDirectory, name) {

    override fun addDirectDependencies(dependencies: Collection<ModuleDescriptor>): Boolean {
        dependencies
            .filterIsInstance<YModuleDescriptor>()
            .flatMap { it.getAllDependencies() }
            .filterIsInstance<YCustomRegularModuleDescriptor>()
            .flatMap { it.getSubModules() }
            .filterIsInstance<YCommonWebSubModuleDescriptor>()
            .forEach { it.addDependantWebExtension(this) }
        return super.addDirectDependencies(dependencies)
    }
}
