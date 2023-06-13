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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.services.VirtualFileSystemService
import com.intellij.idea.plugin.hybris.facet.ExtensionDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorImportStatus
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.openapi.application.ApplicationManager
import io.ktor.util.*
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.File
import java.util.*

abstract class AbstractModuleDescriptor(
    override val moduleRootDirectory: File,
    override val rootProjectDescriptor: HybrisProjectDescriptor,
    override val name: String,
    override val descriptorType: ModuleDescriptorType = ModuleDescriptorType.NONE,
    override var groupNames: Array<String> = emptyArray(),
    override var readonly: Boolean = false,
) : ModuleDescriptor {

    override var importStatus = ModuleDescriptorImportStatus.UNUSED
    override var springFileSet = mutableSetOf<String>()
    override val dependencies = mutableSetOf<ModuleDescriptor>()
    private lateinit var requiredExtensionNames: Set<String>

    override fun compareTo(other: ModuleDescriptor) = name
        .compareTo(other.name, true)

    override fun hashCode() = HashCodeBuilder(17, 37)
        .append(this.name)
        .append(moduleRootDirectory)
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
                    .append(moduleRootDirectory, it.moduleRootDirectory)
                    .isEquals
            }
            ?: false
    }

    override fun extensionDescriptor() = ExtensionDescriptor(
        name = name,
        type = descriptorType
    )

    override fun isPreselected() = false

    override fun ideaModuleFile(): File {
        val futureModuleName = ideaModuleName()
        return rootProjectDescriptor.modulesFilesDirectory
            ?.let { File(rootProjectDescriptor.modulesFilesDirectory, futureModuleName + HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION) }
            ?: File(moduleRootDirectory, futureModuleName + HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION)
    }

    override fun getRelativePath(): String {
        val projectRootDir: File = rootProjectDescriptor.rootDirectory
            ?: return moduleRootDirectory.path
        val virtualFileSystemService = ApplicationManager.getApplication().getService(VirtualFileSystemService::class.java)

        return if (virtualFileSystemService.fileContainsAnother(projectRootDir, moduleRootDirectory)) {
            virtualFileSystemService.getRelativePath(projectRootDir, moduleRootDirectory)
        } else moduleRootDirectory.path
    }

    override fun getAllDependencies() = recursivelyCollectDependenciesPlainSet(this, TreeSet())
        .unmodifiable()

    private fun recursivelyCollectDependenciesPlainSet(descriptor: ModuleDescriptor, dependenciesSet: MutableSet<ModuleDescriptor>): Set<ModuleDescriptor> {
        val dependencies = descriptor.dependencies

        if (CollectionUtils.isEmpty(dependencies)) return dependenciesSet

        dependencies
            .filterNot { dependenciesSet.contains(it) }
            .forEach {
                dependenciesSet.add(it)
                dependenciesSet.addAll(recursivelyCollectDependenciesPlainSet(it, dependenciesSet))
            }

        return dependenciesSet
    }

    override fun getRequiredExtensionNames() = requiredExtensionNames
    override fun setRequiredExtensionNames(moduleDescriptors: Map<String, ModuleDescriptor>) {
        requiredExtensionNames = initDependencies(moduleDescriptors)
    }

    internal open fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>): Set<String> = emptySet()

    override fun toString() = javaClass.simpleName +
        "{" +
        "name=$name, " +
        "moduleRootDirectory=$moduleRootDirectory, " +
        "}"
}
