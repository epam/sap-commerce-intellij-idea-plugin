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

package sap.commerce.toolset.ccv2.project.descriptor.provider

import sap.commerce.toolset.ccv2.project.descriptor.CCv2CoreModuleDescriptor
import sap.commerce.toolset.project.context.ModuleDescriptorProviderContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType

class CCv2CoreModuleDescriptorProvider : CCv2ModuleDescriptorProvider(
    ModuleDescriptorType.CCV2_CORE
) {

    override fun create(context: ModuleDescriptorProviderContext) = CCv2CoreModuleDescriptor(context.moduleRootPath)
}
