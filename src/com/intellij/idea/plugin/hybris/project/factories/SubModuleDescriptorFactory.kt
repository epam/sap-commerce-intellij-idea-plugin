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

package com.intellij.idea.plugin.hybris.project.factories

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptorUtil
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*
import io.ktor.util.*
import java.io.File

object SubModuleDescriptorFactory {

    fun buildAll(owner: YRegularModuleDescriptor): Set<YSubModuleDescriptor> {
        val subModules = mutableSetOf<YSubModuleDescriptor>()

        if (YModuleDescriptorUtil.hasWebModule(owner)) {
            build(owner, HybrisConstants.WEB_MODULE_DIRECTORY, subModules) { YWebSubModuleDescriptor(owner, it) }
        }
        if (YModuleDescriptorUtil.hasHmcModule(owner)) {
            build(owner, HybrisConstants.HAC_MODULE_DIRECTORY, subModules) { YHmcSubModuleDescriptor(owner, it) }
        }
        if (YModuleDescriptorUtil.isHacAddon(owner)) {
            build(owner, HybrisConstants.HAC_MODULE_DIRECTORY, subModules) { YHacSubModuleDescriptor(owner, it) }
        }
        if (YModuleDescriptorUtil.hasBackofficeModule(owner)) {
            build(owner, HybrisConstants.BACKOFFICE_MODULE_DIRECTORY, subModules) { YBackofficeSubModuleDescriptor(owner, it) }
        }
        build(owner, HybrisConstants.COMMON_WEB_MODULE_DIRECTORY, subModules) { YCommonWebSubModuleDescriptor(owner, it) }
        build(owner, HybrisConstants.ACCELERATOR_ADDON_DIRECTORY, subModules) { YAcceleratorAddonSubModuleDescriptor(owner, it) }

        return subModules.unmodifiable()
    }

    private fun build(
        owner: YRegularModuleDescriptor,
        subModuleDirectory: String,
        subModules: MutableSet<YSubModuleDescriptor>,
        builder: (File) -> (YSubModuleDescriptor)
    ) = File(owner.rootDirectory, subModuleDirectory)
        .takeIf { it.exists() }
        ?.let { builder.invoke(it) }
        ?.let { subModules.add(it) }
}