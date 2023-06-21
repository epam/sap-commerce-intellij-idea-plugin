/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
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

package com.intellij.idea.plugin.hybris.impex.assistance;

import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider

class ImpexColumnBreadcrumbsProvider : BreadcrumbsProvider {

    override fun getLanguages(): Array<Language> = arrayOf(ImpexLanguage.getInstance())

    override fun acceptElement(element: PsiElement) = element is ImpexFullHeaderParameter
        || element is ImpexFullHeaderType
        || element is ImpexAnyHeaderMode

    override fun getElementInfo(psi: PsiElement): String {
        val headerParameter = getLinkedHeaderParameter(psi)
        if (headerParameter != null) return headerParameter.text

        val adjustedPsi = adjustWhiteSpaceAndSeparator(psi)

        val parentParameter = PsiTreeUtil.getParentOfType(adjustedPsi, ImpexFullHeaderParameter::class.java, false)
        if (parentParameter != null) return parentParameter.text

        val mode = PsiTreeUtil.getParentOfType(adjustedPsi, ImpexAnyHeaderMode::class.java, false)
        if (mode != null) return mode.text

        val type = PsiTreeUtil.getParentOfType(adjustedPsi, ImpexFullHeaderType::class.java, false)
        if (type != null) return type.headerTypeName.text

        return "<error> : ${psi.node.elementType} : ${psi.text}"
    }

    override fun getParent(element: PsiElement): PsiElement? {
        val linkedParameter = getLinkedHeaderParameter(element)
        if (linkedParameter != null) return linkedParameter

        val parentParameter = PsiTreeUtil.getParentOfType(element, ImpexFullHeaderParameter::class.java, true)
        if (parentParameter != null) return parentParameter

        if (element is ImpexAnyHeaderMode) return null

        val adjustedPsi = adjustWhiteSpaceAndSeparator(element)
        return when (adjustedPsi) {
            is ImpexAnyHeaderMode -> adjustedPsi
            is ImpexFullHeaderParameter -> {
                val line = getImpexHeaderLine(adjustedPsi)
                line?.fullHeaderType
                    ?: line?.anyHeaderMode
            }

            is ImpexFullHeaderType -> getImpexHeaderLine(adjustedPsi)
                ?.anyHeaderMode

            else -> {
                PsiTreeUtil.getParentOfType(
                    adjustedPsi,
                    ImpexValueGroup::class.java,
                    ImpexFullHeaderParameter::class.java,
                    ImpexFullHeaderType::class.java,
                    ImpexAnyHeaderMode::class.java
                )
            }
        }
    }

    private fun getImpexHeaderLine(adjustedPsi: PsiElement) = PsiTreeUtil
        .getParentOfType(adjustedPsi, ImpexHeaderLine::class.java, false)

    private fun getLinkedHeaderParameter(psi: PsiElement): ImpexFullHeaderParameter? = ImpexPsiUtils
        .getClosestSelectedValueGroupFromTheSameLine(psi)
        ?.fullHeaderParameter

    private fun adjustWhiteSpaceAndSeparator(psiElement: PsiElement): PsiElement {
        if (psiElement is PsiWhiteSpace) {
            val previousElement = PsiTreeUtil.skipSiblingsBackward(psiElement, PsiWhiteSpace::class.java)
            if (previousElement != null) return previousElement
        } else if (isParameterSeparator(psiElement)) {
            return psiElement.nextSibling
        }
        return psiElement
    }

    private fun isParameterSeparator(psi: PsiElement) = psi.node.elementType == ImpexTypes.PARAMETERS_SEPARATOR
        && psi.nextSibling != null
}