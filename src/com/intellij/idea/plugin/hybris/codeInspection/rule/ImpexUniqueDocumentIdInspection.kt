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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes.DOCUMENT_ID
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils.getColumnForHeader
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.util.HashSet

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class ImpexUniqueDocumentIdInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexDocumentIdVisitor(holder)
}

private class ImpexDocumentIdVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {
    override fun visitAnyHeaderParameterName(parameter: ImpexAnyHeaderParameterName) {
        if (isDocumentId(parameter.firstChild)) {
            val set = HashSet<String>()
            val column = getColumnForHeader(parameter.parent as ImpexFullHeaderParameter)
            column.forEach { value ->
                if (!set.add(value.text)) {
                    problemsHolder.registerProblem(value, "Qualifier '${parameter.text}' already used",
                            ProblemHighlightType.ERROR)
                }
            }
        }
    }

    private fun isDocumentId(element: PsiElement) = element is LeafPsiElement && element.elementType == DOCUMENT_ID
}
