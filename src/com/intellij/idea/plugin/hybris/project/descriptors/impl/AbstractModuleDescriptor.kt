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
import com.intellij.idea.plugin.hybris.common.services.VirtualFileSystemService
import com.intellij.idea.plugin.hybris.facet.ExtensionDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.*
import com.intellij.util.asSafely
import kotlinx.collections.immutable.toImmutableSet
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
    private lateinit var requiredExtensionNames: MutableSet<String>
    private val springFileSet = mutableSetOf<String>()
    private val directDependencies = mutableSetOf<ModuleDescriptor>()
    private val dependencies: Set<ModuleDescriptor> by lazy {
        recursivelyCollectDependenciesPlainSet(this, TreeSet())
            .toImmutableSet()
    }
    private val myExtensionDescriptor by lazy {
        ExtensionDescriptor(
            name = name,
            type = descriptorType
        )
    }

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
            .asSafely<AbstractModuleDescriptor>()
            ?.let {
                EqualsBuilder()
                    .append(this.name, it.name)
                    .append(moduleRootDirectory, it.moduleRootDirectory)
                    .isEquals
            }
            ?: false
    }

    override fun extensionDescriptor() = myExtensionDescriptor
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
        val virtualFileSystemService = VirtualFileSystemService.getInstance()

        return if (virtualFileSystemService.fileContainsAnother(projectRootDir, moduleRootDirectory)) {
            virtualFileSystemService.getRelativePath(projectRootDir, moduleRootDirectory)
        } else moduleRootDirectory.path
    }

    override fun getAllDependencies() = dependencies

    override fun getRequiredExtensionNames() = requiredExtensionNames
    override fun addRequiredExtensionNames(extensions: Set<YModuleDescriptor>) = extensions
        .map { it.name }
        .let { requiredExtensionNames.addAll(it) }
    override fun computeRequiredExtensionNames(moduleDescriptors: Map<String, ModuleDescriptor>) {
        requiredExtensionNames = initDependencies(moduleDescriptors).toMutableSet()
    }

    override fun getSpringFiles() = springFileSet

    override fun addSpringFile(file: String) = springFileSet.add(file)
    override fun getDirectDependencies() = directDependencies

    override fun addDirectDependencies(dependencies: Collection<ModuleDescriptor>) = this.directDependencies.addAll(dependencies)
    internal open fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>): Set<String> = emptySet()

    override fun toString() = "${javaClass.simpleName} {name=$name, moduleRootDirectory=$moduleRootDirectory}"

    private fun recursivelyCollectDependenciesPlainSet(descriptor: ModuleDescriptor, dependenciesSet: MutableSet<ModuleDescriptor>): Set<ModuleDescriptor> {
        val dependencies = descriptor.getDirectDependencies()

        if (CollectionUtils.isEmpty(dependencies)) return dependenciesSet

        dependencies
            .filterNot { dependenciesSet.contains(it) }
            .forEach {
                dependenciesSet.add(it)
                dependenciesSet.addAll(recursivelyCollectDependenciesPlainSet(it, dependenciesSet))
            }

        return dependenciesSet
    }
}
