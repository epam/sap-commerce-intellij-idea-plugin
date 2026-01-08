/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.project.descriptor.provider

import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.extensioninfo.EiModelAccess
import sap.commerce.toolset.project.context.ModuleDescriptorProviderContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.impl.YBackofficeModuleDescriptor
import kotlin.io.path.name

class YBackofficeModuleDescriptorProvider : ModuleDescriptorProvider {

    override fun isApplicable(context: ModuleDescriptorProviderContext) = context.moduleRoot.type == ModuleDescriptorType.OOTB
        && context.moduleRoot.path.name == EiConstants.Extension.BACK_OFFICE

    override fun create(context: ModuleDescriptorProviderContext): YBackofficeModuleDescriptor {
        val extensionInfo = EiModelAccess.getInstance().getContext(context.moduleRootPath)
            ?: throw HybrisConfigurationException("Cannot unmarshall extensioninfo.xml for $context")

        return YBackofficeModuleDescriptor(context.moduleRootPath, extensionInfo).apply {
            SubModuleDescriptorFactory.getInstance().buildAll(this)
                .forEach { this.addSubModule(it) }
        }
    }
}
