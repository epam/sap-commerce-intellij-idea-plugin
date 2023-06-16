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

import com.intellij.idea.plugin.hybris.project.descriptors.SubModuleDescriptorType
import java.io.File

/**
 * The acceleratorstorefrontcommons AddOn is a special type of AddOn that encapsulates the common web resources for a storefront.
 * This allows you, for example, to re-use web code that is common to the different Accelerators, such as B2B and Telco.
 * It is also acceptable to have other AddOns depend on the acceleratorstorefrontcommons AddOn.
 *
 * @see <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/7e47d40a176d48ba914b50957d003804/8abe0978866910149e909bc3753ba6ef.html?locale=en-US">acceleratorstorefrontcommons AddOn</a>
 */
class YCommonWebSubModuleDescriptor(
    owner: YRegularModuleDescriptor,
    moduleRootDirectory: File,
    override val subModuleDescriptorType: SubModuleDescriptorType = SubModuleDescriptorType.COMMON_WEB,
) : AbstractYSubModuleDescriptor(owner, moduleRootDirectory)