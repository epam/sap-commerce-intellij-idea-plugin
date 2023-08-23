/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.project

import com.intellij.ide.IconProvider
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.psi.PsiElement
import javax.swing.Icon

class HybrisProjectIconProvider : IconProvider() {

    override fun getIcon(element: PsiElement, p1: Int): Icon? {
        val file = element.containingFile ?: return null

        return when {
            file.name == HybrisConstants.UNMANAGED_DEPENDENCIES_TXT -> HybrisIcons.UNMANAGED_DEPENDENCIES
            file.name == HybrisConstants.EXTERNAL_DEPENDENCIES_XML -> HybrisIcons.EXTERNAL_DEPENDENCIES
            file.name.endsWith(HybrisConstants.IMPORT_OVERRIDE_FILENAME) -> HybrisIcons.PLUGIN_SETTINGS
            else -> null
        }
    }
}
