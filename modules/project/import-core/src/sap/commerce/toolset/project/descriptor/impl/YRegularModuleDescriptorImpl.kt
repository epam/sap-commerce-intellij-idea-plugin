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

import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.extensioninfo.context.Info
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.YRegularModuleDescriptor
import java.io.File

abstract class YRegularModuleDescriptorImpl protected constructor(
    moduleRootDirectory: File,
    descriptorType: ModuleDescriptorType,
    extensionInfo: Info,
) : AbstractYModuleDescriptor(moduleRootDirectory, extensionInfo.name, descriptorType, extensionInfo), YRegularModuleDescriptor {

    override var isInLocalExtensions = false
    override var isNeededDependency = false

    override fun isPreselected() = isInLocalExtensions || isNeededDependency

    override fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>): Set<String> = extensionInfo.requiredExtensions
        .takeIf { it.isNotEmpty() }
        ?.map { it.name }
        ?.let { directRequiredExtensions ->
            buildSet {
                addAll(directRequiredExtensions)
                addAll(getAdditionalRequiredExtensionNames())

                if (extensionInfo.webModule) this
                    .map { "$it." + EiConstants.Extension.COMMON_WEB }
                    .filter { moduleDescriptors.contains(it) }
                    .let { addAll(it) }

                if (extensionInfo.hmcModule) add(EiConstants.Extension.HMC)
                if (extensionInfo.backofficeModule) add(EiConstants.Extension.BACK_OFFICE + "." + EiConstants.Extension.WEB)
            }
        }
        ?: getDefaultRequiredExtensionNames()

    override fun getDefaultRequiredExtensionNames() = setOf(EiConstants.Extension.PLATFORM)
    override fun getAdditionalRequiredExtensionNames() = setOf(EiConstants.Extension.PLATFORM)
}