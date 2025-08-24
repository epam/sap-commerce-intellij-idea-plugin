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

package sap.commerce.toolset.impex.breadcrumbs

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.utils.ImpExPsiUtils

class ImpExColumnBreadcrumbsProvider : BreadcrumbsProvider {

    override fun getLanguages(): Array<Language> = arrayOf(ImpExLanguage)

    override fun acceptElement(element: PsiElement) = element is ImpExFullHeaderParameter
        || element is ImpExFullHeaderType
        || element is ImpExAnyHeaderMode

    override fun getElementInfo(psi: PsiElement): String {
        val headerParameter = getLinkedHeaderParameter(psi)
        if (headerParameter != null) return headerParameter.text

        val adjustedPsi = adjustWhiteSpaceAndSeparator(psi)

        val parentParameter = PsiTreeUtil.getParentOfType(adjustedPsi, ImpExFullHeaderParameter::class.java, false)
        if (parentParameter != null) return parentParameter.text

        val mode = PsiTreeUtil.getParentOfType(adjustedPsi, ImpExAnyHeaderMode::class.java, false)
        if (mode != null) return mode.text

        val type = PsiTreeUtil.getParentOfType(adjustedPsi, ImpExFullHeaderType::class.java, false)
        if (type != null) return type.headerTypeName.text

        return "<error> : ${psi.node.elementType} : ${psi.text}"
    }

    override fun getParent(element: PsiElement): PsiElement? {
        val linkedParameter = getLinkedHeaderParameter(element)
        if (linkedParameter != null) return linkedParameter

        val parentParameter = PsiTreeUtil.getParentOfType(element, ImpExFullHeaderParameter::class.java, true)
        if (parentParameter != null) return parentParameter

        if (element is ImpExAnyHeaderMode) return null

        return when (val adjustedPsi = adjustWhiteSpaceAndSeparator(element)) {
            is ImpExAnyHeaderMode -> adjustedPsi
            is ImpExFullHeaderParameter -> {
                val line = getImpexHeaderLine(adjustedPsi)
                line?.fullHeaderType
                    ?: line?.anyHeaderMode
            }

            is ImpExFullHeaderType -> getImpexHeaderLine(adjustedPsi)
                ?.anyHeaderMode

            else -> {
                PsiTreeUtil.getParentOfType(
                    adjustedPsi,
                    ImpExValueGroup::class.java,
                    ImpExFullHeaderParameter::class.java,
                    ImpExFullHeaderType::class.java,
                    ImpExAnyHeaderMode::class.java
                )
            }
        }
    }

    private fun getImpexHeaderLine(adjustedPsi: PsiElement) = PsiTreeUtil
        .getParentOfType(adjustedPsi, ImpExHeaderLine::class.java, false)

    private fun getLinkedHeaderParameter(psi: PsiElement): ImpExFullHeaderParameter? = ImpExPsiUtils
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

    private fun isParameterSeparator(psi: PsiElement) = psi.node.elementType == ImpExTypes.PARAMETERS_SEPARATOR
        && psi.nextSibling != null
}