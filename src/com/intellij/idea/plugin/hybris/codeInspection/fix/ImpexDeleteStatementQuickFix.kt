/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.codeInspection.fix

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

class ImpexDeleteStatementQuickFix(
    parameter: ImpexHeaderTypeName,
    private val elementName: String,
    private val message: String = message("hybris.inspections.fix.impex.DeleteStatement.text", elementName)
) : LocalQuickFixOnPsiElement(parameter) {

    private val elementsListToStopSearching = mutableSetOf<IElementType>(
        ImpexTypes.USER_RIGHTS,
        ImpexTypes.HEADER_LINE
    )

    override fun getFamilyName() = message("hybris.inspections.fix.impex.DeleteStatement")

    override fun getText() = message

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val headerLine = PsiTreeUtil.getParentOfType(startElement, ImpexHeaderLine::class.java) ?: return

        val valueLines = headerLine.valueLines

        if (valueLines.isEmpty()) {
            deleteHeaderLine(headerLine)
            return
        }

        for (valueLine in valueLines) {
            var nextSibling = valueLine.nextSibling
            while (isNextSiblingElementBlank(nextSibling)) {
                val currentSibling = nextSibling
                nextSibling = currentSibling.nextSibling
                currentSibling.delete()
                valueLine.delete()
            }
        }

        val elementsToDelete = mutableListOf<PsiElement>()
        var nextHeaderSibling = headerLine.nextSibling
        while (isNextSiblingElementBlank(nextHeaderSibling)) {
            elementsToDelete.add(nextHeaderSibling)
            nextHeaderSibling = nextHeaderSibling.nextSibling
        }

        var nextSibling = valueLines
            .last()
            .nextSibling

        while (!elementsListToStopSearching.contains(nextSibling.elementType) && nextSibling != null) {
            if (nextSibling.elementType == ImpexTypes.CRLF || nextSibling.elementType == TokenType.WHITE_SPACE) {
                elementsToDelete.add(nextSibling)
            }
            nextSibling = nextSibling.nextSibling
        }

        elementsToDelete.forEach { it.delete() }
        headerLine.delete()
    }

    private fun deleteHeaderLine(headerLine: ImpexHeaderLine) {
        val emptyCrlfElements = mutableListOf<PsiElement>()
        var nextSibling = headerLine.nextSibling

        while (isNextSiblingElementBlank(nextSibling)) {
            emptyCrlfElements.add(nextSibling)
            nextSibling = nextSibling.nextSibling
        }
        emptyCrlfElements.forEach { it.delete() }
        headerLine.delete()
        return
    }

    private fun isNextSiblingElementBlank(nextSibling: PsiElement?) = nextSibling
        .elementType == ImpexTypes.CRLF && nextSibling != null
}