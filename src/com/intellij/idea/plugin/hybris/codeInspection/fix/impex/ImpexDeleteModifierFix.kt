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

package com.intellij.idea.plugin.hybris.codeInspection.fix.impex

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType

class ImpexDeleteModifierFix(
    modifier: ImpexAttribute,
    private val name: String = message("hybris.inspections.fix.impex.DeleteModifier.text", modifier.anyAttributeName.text)
) : LocalQuickFixOnPsiElement(modifier) {

    override fun getFamilyName() = message("hybris.inspections.fix.impex.DeleteModifier")
    override fun getText() = name

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val prevSiblingsToDelete = mutableSetOf<PsiElement>()
        var prevSibling = startElement.prevSibling

        while (prevSibling != null) {
            prevSibling = if (prevSibling.elementType == ImpexTypes.ATTRIBUTE_SEPARATOR || prevSibling.elementType == TokenType.WHITE_SPACE) {
                prevSiblingsToDelete.add(prevSibling)
                prevSibling.prevSibling
            } else {
                null
            }
        }

        prevSiblingsToDelete.forEach { it.delete() }

        startElement.delete()
    }
}