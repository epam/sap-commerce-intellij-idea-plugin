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

import kotlinx.collections.immutable.toImmutableSet
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.descriptor.*
import sap.commerce.toolset.util.toSystemIndependentName
import java.nio.file.Path

class YAcceleratorAddonSubModuleDescriptor(
    owner: YRegularModuleDescriptor,
    moduleRootDirectory: Path,
    val webRoot: Path = moduleRootDirectory.resolve(ProjectConstants.Directory.WEB_ROOT),
    override val name: String = owner.name + "." + ProjectConstants.Directory.ACCELERATOR_ADDON + "." + EiConstants.Extension.WEB,
    override val subModuleDescriptorType: SubModuleDescriptorType = SubModuleDescriptorType.ADDON,
) : AbstractYSubModuleDescriptor(owner, moduleRootDirectory) {

    private val yTargetModules = mutableSetOf<YModuleDescriptor>()

    override val extensionDescriptor by lazy {
        ExtensionDescriptor(
            path = moduleRootDirectory.toSystemIndependentName,
            name = name,
            readonly = readonly,
            type = descriptorType,
            subModuleType = (this as? YSubModuleDescriptor)?.subModuleDescriptorType,
            addon = getRequiredExtensionNames().contains(EiConstants.Extension.ADDON_SUPPORT),
            installedIntoExtensions = yTargetModules
                .map { it.name }
                .toSet()
        )
    }

    override fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>): Set<String> {
        val webNames = owner.getRequiredExtensionNames()
            .map { it + "." + EiConstants.Extension.WEB }
        // Strange, but acceleratoraddon may rely on another acceleratoraddon
        val acceleratorWebNames = owner.getRequiredExtensionNames()
            .map { it + "." + ProjectConstants.Directory.ACCELERATOR_ADDON + "." + EiConstants.Extension.WEB }
        return setOf(owner.name) + webNames + acceleratorWebNames
    }

    fun getTargetModules(): Set<YModuleDescriptor> = yTargetModules.toImmutableSet()
    fun addTargetModule(module: YModuleDescriptor) = yTargetModules.add(module)

}
