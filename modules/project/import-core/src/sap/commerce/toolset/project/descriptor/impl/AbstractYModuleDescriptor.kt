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

import com.intellij.openapi.util.io.FileUtil
import sap.commerce.toolset.extensioninfo.jaxb.ExtensionInfo
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import java.io.File

abstract class AbstractYModuleDescriptor(
    moduleRootDirectory: File,
    name: String,
    descriptorType: ModuleDescriptorType,
    override val extensionInfo: ExtensionInfo,
    private val metas: Map<String, String> = extensionInfo.extension.meta
        .associate { it.key to it.value }
) : AbstractModuleDescriptor(moduleRootDirectory, name, descriptorType), YModuleDescriptor {

    private val myExtensionDescriptor by lazy {
        ExtensionDescriptor(
            path = FileUtil.toSystemIndependentName(moduleRootDirectory.path),
            name = name,
            readonly = readonly,
            type = descriptorType,
            subModuleType = (this as? YSubModuleDescriptor)?.subModuleDescriptorType,
            addon = getRequiredExtensionNames().contains(ProjectConstants.Extension.ADDON_SUPPORT)
        )
    }
    private var ySubModules = mutableSetOf<YSubModuleDescriptor>()

    override fun getSubModules(): Set<YSubModuleDescriptor> = ySubModules
    override fun addSubModule(subModule: YSubModuleDescriptor) = ySubModules.add(subModule)
    override fun removeSubModule(subModule: YSubModuleDescriptor) = ySubModules.remove(subModule)

    // Must be called at the end of the module import
    override fun extensionDescriptor() = myExtensionDescriptor

    fun isMetaKeySetToTrue(metaKeyName: String) = metas[metaKeyName]
        ?.let { "true".equals(it, true) }
        ?: false
}