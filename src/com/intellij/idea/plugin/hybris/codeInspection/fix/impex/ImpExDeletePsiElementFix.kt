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

package com.intellij.idea.plugin.hybris.codeInspection.fix.impex

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

open class ImpExDeletePsiElementFix internal constructor(
    element: PsiElement,
    private val myFamilyName: String,
    private val myText: String,
) : LocalQuickFixOnPsiElement(element) {

    override fun getFamilyName() = myFamilyName
    override fun getText() = myText

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        startElement.delete()
    }
}

class ImpExDeleteParametersSeparatorFix(element: PsiElement) : ImpExDeletePsiElementFix(
    element,
    message("hybris.inspections.fix.impex.DeleteParametersSeparator"),
    message("hybris.inspections.fix.impex.DeleteParametersSeparator")
)

class ImpExDeleteValueGroupFix(element: PsiElement, valueGroupPreview: String) : ImpExDeletePsiElementFix(
    element,
    message("hybris.inspections.fix.impex.DeleteValueGroup"),
    message("hybris.inspections.fix.impex.DeleteValueGroup.key", valueGroupPreview
    )
)
