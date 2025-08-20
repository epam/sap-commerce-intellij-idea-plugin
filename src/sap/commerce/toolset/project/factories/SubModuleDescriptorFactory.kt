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

package sap.commerce.toolset.project.factories

import kotlinx.collections.immutable.toImmutableSet
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.YRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.*
import java.io.File

object SubModuleDescriptorFactory {

    fun buildAll(owner: YRegularModuleDescriptor): Set<YSubModuleDescriptor> {
        val subModules = mutableSetOf<YSubModuleDescriptor>()

        if (owner.hasWebModule) build(owner, HybrisConstants.WEB_MODULE_DIRECTORY, subModules) { YWebSubModuleDescriptor(owner, it) }
        if (owner.hasHmcModule) build(owner, HybrisConstants.HMC_MODULE_DIRECTORY, subModules) { YHmcSubModuleDescriptor(owner, it) }
        if (owner.isHacAddon) build(owner, HybrisConstants.HAC_MODULE_DIRECTORY, subModules) { YHacSubModuleDescriptor(owner, it) }

        if (owner.hasBackofficeModule) {
            build(owner, HybrisConstants.BACKOFFICE_MODULE_DIRECTORY, subModules) { backoffice ->
                val subModule = YBackofficeSubModuleDescriptor(owner, backoffice)

                if (subModule.hasWebModule) {
                    build(subModule, HybrisConstants.WEB_MODULE_DIRECTORY, subModules) { web ->
                        YWebSubModuleDescriptor(owner, web, subModule.name + "." + web.name)
                    }
                }
                subModule
            }
        }

        build(owner, HybrisConstants.COMMON_WEB_MODULE_DIRECTORY, subModules) { YCommonWebSubModuleDescriptor(owner, it) }
        build(owner, HybrisConstants.ACCELERATOR_ADDON_WEB_PATH, subModules) { YAcceleratorAddonSubModuleDescriptor(owner, it) }

        return subModules.toImmutableSet()
    }

    private fun build(
        owner: YModuleDescriptor,
        subModuleDirectory: String,
        subModules: MutableSet<YSubModuleDescriptor>,
        builder: (File) -> (YSubModuleDescriptor)
    ) = File(owner.moduleRootDirectory, subModuleDirectory)
        .takeIf { it.exists() }
        ?.let { builder.invoke(it) }
        ?.let { subModules.add(it) }
}