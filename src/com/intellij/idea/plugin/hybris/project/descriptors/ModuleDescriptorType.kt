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
package com.intellij.idea.plugin.hybris.project.descriptors

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import javax.swing.Icon

enum class ModuleDescriptorType(val icon: Icon = HybrisIcons.Y_LOGO_BLUE) {
    CONFIG(HybrisIcons.EXTENSION_CONFIG),
    CUSTOM(HybrisIcons.EXTENSION_CUSTOM),
    EXT(HybrisIcons.EXTENSION_EXT),
    NONE,
    OOTB(HybrisIcons.EXTENSION_OOTB),
    PLATFORM(HybrisIcons.EXTENSION_PLATFORM),
    ECLIPSE,
    MAVEN,
    GRADLE,
    CCV2(HybrisIcons.EXTENSION_CLOUD)
}
