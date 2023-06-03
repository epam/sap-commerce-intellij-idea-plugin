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

import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorImportStatus
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptorUtil.getIdeaModuleFile
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.File

abstract class AbstractModuleDescriptor(
    override val rootDirectory: File,
    override val rootProjectDescriptor: HybrisProjectDescriptor,
    override val name: String,
    override val descriptorType: ModuleDescriptorType = ModuleDescriptorType.NONE
) : ModuleDescriptor {
    override var importStatus = ModuleDescriptorImportStatus.UNUSED

    override fun compareTo(other: ModuleDescriptor) = other
        .name
        .compareTo(this.name, true)

    override fun hashCode() = HashCodeBuilder(17, 37)
        .append(this.name)
        .append(rootDirectory)
        .toHashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (null == other || javaClass != other.javaClass) {
            return false
        }

        return other
            .let { it as? AbstractModuleDescriptor }
            ?.let {
                EqualsBuilder()
                    .append(this.name, it.name)
                    .append(rootDirectory, it.rootDirectory)
                    .isEquals
            }
            ?: false
    }

    override fun toString() = javaClass.simpleName +
        "{" +
        "name=$name, " +
        "moduleRootDirectory=$rootDirectory, " +
        "moduleFile=${getIdeaModuleFile(this)}" +
        "}"
}
