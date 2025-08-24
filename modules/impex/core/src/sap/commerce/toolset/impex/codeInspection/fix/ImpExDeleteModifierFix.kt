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

package sap.commerce.toolset.impex.codeInspection.fix

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.ImpExAttribute
import sap.commerce.toolset.impex.psi.ImpExTypes

class ImpExDeleteModifierFix(
    modifier: ImpExAttribute,
    private val name: String = i18n("hybris.inspections.fix.impex.DeleteModifier.text", modifier.anyAttributeName.text)
) : LocalQuickFixOnPsiElement(modifier) {

    override fun getFamilyName() = i18n("hybris.inspections.fix.impex.DeleteModifier")
    override fun getText() = name

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val prevSiblingsToDelete = mutableSetOf<PsiElement>()
        var prevSibling = startElement.prevSibling

        while (prevSibling != null) {
            prevSibling = if (prevSibling.elementType == ImpExTypes.ATTRIBUTE_SEPARATOR || prevSibling.elementType == TokenType.WHITE_SPACE) {
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