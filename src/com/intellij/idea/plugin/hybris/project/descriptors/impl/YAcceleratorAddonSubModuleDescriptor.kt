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
import kotlinx.collections.immutable.toImmutableSet
import java.io.File

class YAcceleratorAddonSubModuleDescriptor(
    owner: YRegularModuleDescriptor,
    moduleRootDirectory: File,
    val webRoot: File = File(moduleRootDirectory, HybrisConstants.WEB_ROOT_DIRECTORY),
    override val name: String = owner.name + "." + HybrisConstants.ACCELERATOR_ADDON_DIRECTORY + "." + HybrisConstants.WEB_MODULE_DIRECTORY,
    override val subModuleDescriptorType: SubModuleDescriptorType = SubModuleDescriptorType.ADDON,
) : AbstractYSubModuleDescriptor(owner, moduleRootDirectory) {

    private val yTargetModules = mutableSetOf<YModuleDescriptor>()
    private val myExtensionDescriptor by lazy {
        with(super.extensionDescriptor()) {
            installedIntoExtensions = yTargetModules
                .map { it.name }
                .toSet()
            this
        }
    }

    override fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>): Set<String> {
        val webNames = owner.getRequiredExtensionNames()
            .map { it + "." + HybrisConstants.WEB_MODULE_DIRECTORY }
        // Strange, but acceleratoraddon may rely on another acceleratoraddon
        val acceleratorWebNames = owner.getRequiredExtensionNames()
            .map { it + "." + HybrisConstants.ACCELERATOR_ADDON_DIRECTORY + "." + HybrisConstants.WEB_MODULE_DIRECTORY }
        return setOf(owner.name) + webNames + acceleratorWebNames
    }

    fun getTargetModules(): Set<YModuleDescriptor> = yTargetModules.toImmutableSet()
    fun addTargetModule(module: YModuleDescriptor) = yTargetModules.add(module)

    override fun extensionDescriptor() = myExtensionDescriptor
}