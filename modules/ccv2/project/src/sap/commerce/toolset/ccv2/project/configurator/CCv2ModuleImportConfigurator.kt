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
package sap.commerce.toolset.ccv2.project.configurator

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VfsUtil
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.io.path.pathString

class CCv2ModuleImportConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "CCv2 Modules"

    override fun isApplicable(moduleTypeId: String) = CCv2Constants.MODULE_TYPE_ID == moduleTypeId

    override fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        module: Module,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        modifiableModelsProvider.getModifiableRootModel(module)
            .addContentEntry(VfsUtil.pathToUrl(moduleDescriptor.moduleRootPath.pathString))
            .addExcludePattern(ProjectConstants.Directory.HYBRIS)
    }
}
