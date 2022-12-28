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

package com.intellij.idea.plugin.hybris.codeInspection.rule

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes.DOCUMENT_ID
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.findHeaderItemTypeName
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class ImpexUnknownTypeAttributeInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexHeaderLineVisitor(holder)
}

private class ImpexHeaderLineVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {
    override fun visitAnyHeaderParameterName(parameter: ImpexAnyHeaderParameterName) {
        if (isNotMacros(parameter) && isNotDocumentId(parameter.firstChild)) {
            val references = parameter.references
            if (references.isNotEmpty()) {
                val firstReference = references.first()
                if (!firstReference.canonicalText.contains(".") && firstReference is TSReferenceBase<*>) {
                    val result = firstReference.multiResolve(false)

                    if (result.isEmpty()) {
                        problemsHolder.registerProblem(parameter,
                                "Unknown attribute for type '${findHeaderItemTypeName(parameter).map { it.text }.orElse("")}'",
                                ProblemHighlightType.ERROR)
                    }
                }
            }
        }
    }


    private fun isNotMacros(parameter: ImpexAnyHeaderParameterName) = parameter.firstChild !is ImpexMacroUsageDec

    private fun isNotDocumentId(element: PsiElement) = (element as LeafPsiElement).elementType != DOCUMENT_ID
}