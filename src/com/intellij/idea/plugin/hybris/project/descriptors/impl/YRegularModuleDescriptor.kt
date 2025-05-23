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
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ExtensionInfo
import kotlinx.collections.immutable.toImmutableSet
import java.io.File

abstract class YRegularModuleDescriptor protected constructor(
    moduleRootDirectory: File,
    rootProjectDescriptor: HybrisProjectDescriptor,
    extensionInfo: ExtensionInfo,
) : AbstractYModuleDescriptor(
    moduleRootDirectory, rootProjectDescriptor,
    extensionInfo.extension.name, extensionInfo = extensionInfo
) {

    var isInLocalExtensions = false
    var isNeededDependency = false

    val hasHmcModule = extensionInfo.extension.hmcmodule != null
    val isHacAddon = isMetaKeySetToTrue(HybrisConstants.EXTENSION_META_KEY_HAC_MODULE)

    val hasBackofficeModule = isMetaKeySetToTrue(HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE)
        && File(moduleRootDirectory, HybrisConstants.BACKOFFICE_MODULE_DIRECTORY).isDirectory

    val hasWebModule = extensionInfo.extension.webmodule != null
        && File(moduleRootDirectory, HybrisConstants.WEB_MODULE_DIRECTORY).isDirectory

    override fun isPreselected() = isInLocalExtensions || isNeededDependency

    override fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>): Set<String> {
        val extension = extensionInfo.extension
            ?: return getDefaultRequiredExtensionNames()

        val requiresExtension = extension.requiresExtension
            .takeIf { it.isNotEmpty() }
            ?: return getDefaultRequiredExtensionNames()

        val requiredExtensionNames = requiresExtension
            .filter { it.name.isNotBlank() }
            .map { it.name }
            .toMutableSet()

        requiredExtensionNames.addAll(getAdditionalRequiredExtensionNames())

        if (hasWebModule) {
            requiredExtensionNames
                .map { "$it." + HybrisConstants.COMMON_WEB_MODULE_DIRECTORY }
                .filter { moduleDescriptors.contains(it) }
                .let { requiredExtensionNames.addAll(it) }
        }
        if (hasHmcModule) {
            requiredExtensionNames.add(HybrisConstants.EXTENSION_NAME_HMC)
        }
        if (hasBackofficeModule) {
            requiredExtensionNames.add(HybrisConstants.EXTENSION_NAME_BACK_OFFICE + "." + HybrisConstants.WEB_MODULE_DIRECTORY)
        }
        return requiredExtensionNames.toImmutableSet()
    }

    internal open fun getDefaultRequiredExtensionNames() = setOf(HybrisConstants.EXTENSION_NAME_PLATFORM)
    internal open fun getAdditionalRequiredExtensionNames() = setOf(HybrisConstants.EXTENSION_NAME_PLATFORM)
}
