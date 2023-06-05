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
import com.intellij.idea.plugin.hybris.facet.ExtensionDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptorUtil
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ExtensionInfo
import java.io.File

abstract class AbstractYModuleDescriptor(
    moduleRootDirectory: File,
    rootProjectDescriptor: HybrisProjectDescriptor,
    name: String,
    override var subModules: MutableSet<YSubModuleDescriptor> = mutableSetOf(),
    internal val extensionInfo: ExtensionInfo,
    private val metas: Map<String, String> = extensionInfo.extension.meta
        .associate { it.key to it.value }
) : AbstractModuleDescriptor(moduleRootDirectory, rootProjectDescriptor, name), YModuleDescriptor {

    override var springFileSet = mutableSetOf<String>()
    override val dependenciesTree = mutableSetOf<YModuleDescriptor>()

    // Must be called at the end of the module import
    override fun extensionDescriptor() = ExtensionDescriptor(
        name = name,
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
        classPathGen = metas[HybrisConstants.EXTENSION_META_KEY_CLASSPATHGEN],
        moduleGenName = metas[HybrisConstants.EXTENSION_META_KEY_MODULE_GEN],
        addon = YModuleDescriptorUtil.getRequiredExtensionNames(this).contains(HybrisConstants.EXTENSION_NAME_ADDONSUPPORT)
    )

    internal fun isMetaKeySetToTrue(metaKeyName: String) = metas[metaKeyName]
        ?.let { "true".equals(it, true) }
        ?: false
}
