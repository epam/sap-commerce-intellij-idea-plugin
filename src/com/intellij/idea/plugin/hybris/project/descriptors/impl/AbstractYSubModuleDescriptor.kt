/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.idea.plugin.hybris.project.descriptors.*
import java.io.File

abstract class AbstractYSubModuleDescriptor(
    override val owner: YModuleDescriptor,
    override val rootDirectory: File,
    override val name: String = owner.name + "." + rootDirectory.name,
    override val rootProjectDescriptor: HybrisProjectDescriptor = owner.rootProjectDescriptor,
    override var importStatus: ModuleDescriptorImportStatus = ModuleDescriptorImportStatus.MANDATORY,
    override val springFileSet: Set<String> = mutableSetOf(),
    override val dependenciesTree: Set<YModuleDescriptor> = mutableSetOf(),
    override val descriptorType: ModuleDescriptorType = owner.descriptorType
) : YSubModuleDescriptor {

    override fun compareTo(other: ModuleDescriptor) = other
        .let { it as? YSubModuleDescriptor }
        ?.owner
        ?.compareTo(this.owner)
        ?: -1

    override fun toString() = "YSubModuleDescriptor(name='$name', owner=$owner, rootDirectory=$rootDirectory)"

}