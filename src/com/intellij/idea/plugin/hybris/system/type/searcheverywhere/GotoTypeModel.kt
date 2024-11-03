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

import com.intellij.ide.IdeBundle
import com.intellij.ide.util.gotoByName.*
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.ui.IdeUICustomization

class GotoTypeModel(project: Project, contributors: List<ChooseByNameContributor>) : FilteringGotoByModel<LanguageRef>(project, contributors) {

    override fun getPromptText(): String = "Enter SAP CX type name:"
    override fun getNotInMessage() = IdeUICustomization.getInstance().projectMessage("label.no.matches.found.in.project")
    override fun willOpenEditor() = true

    override fun getNotFoundMessage(): String = IdeBundle.message("label.no.matches.found")
    override fun getCheckBoxName(): String? = null
    override fun loadInitialCheckBoxState(): Boolean = false

    override fun saveInitialCheckBoxState(state: Boolean) {
    }

    override fun getSeparators(): Array<String> = emptyArray()

    override fun getFullName(element: Any): String? {
        if (element is PsiElement && !element.isValid) {
            return null
        }

        return getElementName(element)
    }

    override fun filterValueFor(item: NavigationItem) = LanguageRef.forNavigationitem(item)

}