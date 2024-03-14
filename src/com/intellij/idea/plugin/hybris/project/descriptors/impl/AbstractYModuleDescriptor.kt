/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.facet.ExtensionDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ExtensionInfo
import java.io.File

abstract class AbstractYModuleDescriptor(
    moduleRootDirectory: File,
    rootProjectDescriptor: HybrisProjectDescriptor,
    name: String,
    internal val extensionInfo: ExtensionInfo,
    private val metas: Map<String, String> = extensionInfo.extension.meta
        .associate { it.key to it.value }
) : AbstractModuleDescriptor(moduleRootDirectory, rootProjectDescriptor, name), YModuleDescriptor {

    private val myExtensionDescriptor by lazy {
        ExtensionDescriptor(
            name = name,
            description = extensionInfo.extension.description,
            readonly = readonly,
            useMaven = "true".equals(extensionInfo.extension.usemaven, true),
            type = descriptorType,
            subModuleType = (this as? YSubModuleDescriptor)?.subModuleDescriptorType,
            webModule = extensionInfo.extension.webmodule != null,
            coreModule = extensionInfo.extension.coremodule != null,
            hmcModule = extensionInfo.extension.hmcmodule != null,
            backofficeModule = isMetaKeySetToTrue(HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE),
            hacModule = isMetaKeySetToTrue(HybrisConstants.EXTENSION_META_KEY_HAC_MODULE),
            deprecated = isMetaKeySetToTrue(HybrisConstants.EXTENSION_META_KEY_DEPRECATED),
            extGenTemplateExtension = isMetaKeySetToTrue(HybrisConstants.EXTENSION_META_KEY_EXT_GEN),
            jaloLogicFree = extensionInfo.extension.isJaloLogicFree,
            classPathGen = metas[HybrisConstants.EXTENSION_META_KEY_CLASSPATHGEN],
            moduleGenName = metas[HybrisConstants.EXTENSION_META_KEY_MODULE_GEN],
            addon = getRequiredExtensionNames().contains(HybrisConstants.EXTENSION_NAME_ADDONSUPPORT)
        )
    }
    private var ySubModules = mutableSetOf<YSubModuleDescriptor>()

    override fun getSubModules(): Set<YSubModuleDescriptor> = ySubModules
    override fun addSubModule(subModule: YSubModuleDescriptor) = ySubModules.add(subModule)
    override fun removeSubModule(subModule: YSubModuleDescriptor) = ySubModules.remove(subModule)

    // Must be called at the end of the module import
    override fun extensionDescriptor() = myExtensionDescriptor

    internal fun isMetaKeySetToTrue(metaKeyName: String) = metas[metaKeyName]
        ?.let { "true".equals(it, true) }
        ?: false
}
