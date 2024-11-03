/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.type.searcheverywhere

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.BSDomFileDescription
import com.intellij.idea.plugin.hybris.system.type.file.TSDomFileDescription
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import javax.swing.Icon

data class SystemRef(val id: String, val displayName: String, val icon: Icon?) {

    companion object {
        private val typeSystem = SystemRef("type", "Type System", HybrisIcons.TypeSystem.FILE)
        private val beanSystem = SystemRef("bean", "Bean System", HybrisIcons.BeanSystem.FILE)

        fun forNavigationItem(item: NavigationItem): SystemRef? = when (item) {
            is PsiElement -> {
                val file = item.containingFile as? XmlFile
                    ?: return null

                when (DomManager.getDomManager(item.project).getDomFileDescription(file)) {
                    is TSDomFileDescription -> typeSystem
                    is BSDomFileDescription -> beanSystem
                    else -> null
                }

            }

            else -> null
        }

        fun forAllSystems() = listOf(typeSystem, beanSystem)
    }
}
