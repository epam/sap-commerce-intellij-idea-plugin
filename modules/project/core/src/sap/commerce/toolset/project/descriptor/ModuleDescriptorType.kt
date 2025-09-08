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

package sap.commerce.toolset.project.descriptor

import sap.commerce.toolset.HybrisIcons
import javax.swing.Icon

enum class ModuleDescriptorType(val icon: Icon = HybrisIcons.Y.LOGO_BLUE, val title: String) {
    CONFIG(HybrisIcons.Extension.CONFIG, "Config"),
    CUSTOM(HybrisIcons.Extension.CUSTOM, "Custom"),
    EXT(HybrisIcons.Extension.EXT, "Ext"),
    NONE(HybrisIcons.Module.NONE, "None"),
    OOTB(HybrisIcons.Extension.OOTB, "Ootb"),
    PLATFORM(HybrisIcons.Extension.PLATFORM, "Platform"),
    ECLIPSE(HybrisIcons.Module.ECLIPSE, "Eclipse"),
    MAVEN(HybrisIcons.Module.MAVEN, "Maven"),
    GRADLE(HybrisIcons.Module.GRADLE, "Gradle"),
    CCV2_EXTERNAL(HybrisIcons.Extension.CLOUD, "CCv2 External"),
    CCV2_STOREFRONT(HybrisIcons.Module.CCV2, "CCv2 Storefront"),
    CCV2_CORE(HybrisIcons.Module.CCV2, "CCv2 Core"),
    CCV2_DATAHUB(HybrisIcons.Module.CCV2, "CCv2 DataHub"),
    ANGULAR(HybrisIcons.Module.ANGULAR, "Angular"),
}