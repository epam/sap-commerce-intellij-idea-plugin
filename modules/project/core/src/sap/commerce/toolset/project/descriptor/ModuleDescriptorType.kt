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

package sap.commerce.toolset.project.descriptor

import sap.commerce.toolset.HybrisIcons
import javax.swing.Icon

enum class ModuleDescriptorType(val title: String, val lazyIcon: () -> Icon = { HybrisIcons.Y.LOGO_BLUE }) {
    CONFIG("Config", { HybrisIcons.Extension.CONFIG }),
    CUSTOM("Custom", { HybrisIcons.Extension.CUSTOM }),
    EXT("Ext", { HybrisIcons.Extension.EXT }),
    NONE("None", { HybrisIcons.Module.NONE }),
    OOTB("Ootb", { HybrisIcons.Extension.OOTB }),
    PLATFORM("Platform", { HybrisIcons.Extension.PLATFORM }),
    ECLIPSE("Eclipse", { HybrisIcons.Module.ECLIPSE }),
    MAVEN("Maven", { HybrisIcons.Module.MAVEN }),
    GRADLE("Gradle", HybrisIcons.Module.GRADLE),
    CCV2_EXTERNAL("CCv2 External", { HybrisIcons.Extension.CLOUD }),
    CCV2_STOREFRONT("CCv2 Storefront", { HybrisIcons.Module.CCV2 }),
    CCV2_CORE("CCv2 Core", { HybrisIcons.Module.CCV2 }),
    CCV2_DATAHUB("CCv2 DataHub", { HybrisIcons.Module.CCV2 }),
    ANGULAR("Angular", HybrisIcons.Module.ANGULAR),
    ROOT("Root", { HybrisIcons.Module.ROOT }),
}