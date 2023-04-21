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
package com.intellij.idea.plugin.hybris.flexibleSearch.refactoring.rename

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchPsiNamedElement
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.PsiElementRenameHandler
import com.intellij.refactoring.rename.RenameHandler

class FlexibleSearchRenameHandler : RenameHandler {

    private val psiRenameHandler = PsiElementRenameHandler()

    override fun isRenaming(dataContext: DataContext) = isAvailableOnDataContext(dataContext)

    override fun isAvailableOnDataContext(dataContext: DataContext) = PsiElementRenameHandler.getElement(dataContext) is FlexibleSearchPsiNamedElement
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext) = psiRenameHandler.invoke(project, editor, file, dataContext)
    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext) = psiRenameHandler.invoke(project, elements, dataContext)
}
