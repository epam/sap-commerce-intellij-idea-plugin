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

import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.settings.ApplicationSettings
import sap.commerce.toolset.settings.toIdeaGroup
import java.nio.file.Path

open class ExternalModuleDescriptor(
    moduleRootDirectory: Path,
    name: String,
    descriptorType: ModuleDescriptorType,
) : AbstractModuleDescriptor(moduleRootDirectory, name, descriptorType) {

    override fun groupName(importContext: ProjectImportContext): Array<String>? = ApplicationSettings.getInstance().groupNonHybris.toIdeaGroup()
}
