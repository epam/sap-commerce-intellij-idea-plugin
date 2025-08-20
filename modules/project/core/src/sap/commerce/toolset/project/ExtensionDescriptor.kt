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

package sap.commerce.toolset.project

import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.SubModuleDescriptorType

class ExtensionDescriptor(
    var name: String = "",
    var description: String? = null,
    var readonly: Boolean = false,
    var useMaven: Boolean = false,
    var type: ModuleDescriptorType = ModuleDescriptorType.NONE,
    var subModuleType: SubModuleDescriptorType? = null,
    var backofficeModule: Boolean = false,
    var hacModule: Boolean = false,
    var webModule: Boolean = false,
    var hmcModule: Boolean = false,
    var coreModule: Boolean = false,
    var deprecated: Boolean = false,
    var extGenTemplateExtension: Boolean = false,
    var jaloLogicFree: Boolean = false,
    var addon: Boolean = false,
    var requiredByAll: Boolean = false,
    var classPathGen: String? = null,
    var moduleGenName: String? = null,
    var packageRoot: String? = null,
    var webRoot: String? = null,
    var version: String? = null,
    var installedIntoExtensions: Set<String> = emptySet(),
)