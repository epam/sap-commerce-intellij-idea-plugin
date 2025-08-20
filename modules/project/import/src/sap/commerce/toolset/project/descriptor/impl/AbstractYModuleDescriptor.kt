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

import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.jaxb.ExtensionInfo
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import java.io.File

abstract class AbstractYModuleDescriptor(
    moduleRootDirectory: File,
    rootProjectDescriptor: HybrisProjectDescriptor,
    name: String,
    override val extensionInfo: ExtensionInfo,
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
            packageRoot = extensionInfo.extension.coremodule?.packageroot,
            webRoot = extensionInfo.extension.webmodule?.webroot,
            version = extensionInfo.extension.version,
            requiredByAll = extensionInfo.extension.isRequiredbyall,
            addon = getRequiredExtensionNames().contains(HybrisConstants.EXTENSION_NAME_ADDONSUPPORT)
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