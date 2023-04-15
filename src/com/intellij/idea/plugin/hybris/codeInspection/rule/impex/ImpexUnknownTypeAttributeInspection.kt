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

package com.intellij.idea.plugin.hybris.codeInspection.rule.impex

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes.DOCUMENT_ID
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class ImpexUnknownTypeAttributeInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexHeaderLineVisitor(holder)

    private class ImpexHeaderLineVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

        override fun visitParameter(parameter: ImpexParameter) {
            inspect(parameter)
        }

        override fun visitAnyHeaderParameterName(parameter: ImpexAnyHeaderParameterName) {
            inspect(parameter)
        }

        private fun inspect(parameter: PsiElement) {
            if (parameter.firstChild is ImpexMacroUsageDec || (parameter.firstChild as? LeafPsiElement)?.elementType == DOCUMENT_ID) return

            val firstReference = parameter.references.firstOrNull() ?: return

            if (firstReference.canonicalText.contains(".")) return
            if (firstReference !is TSReferenceBase<*>) return
            if (firstReference.multiResolve(false).isNotEmpty()) return

            val typeName = (
                PsiTreeUtil.getParentOfType(parameter, ImpexParameter::class.java)
                    ?: PsiTreeUtil.getParentOfType(parameter, ImpexFullHeaderParameter::class.java)
                        ?.anyHeaderParameterName
                )
                ?.text
                ?: "?"
            problemsHolder.registerProblem(
                parameter,
                message("hybris.inspections.UnknownTypeAttributeInspection.key", parameter.text, typeName),
                ProblemHighlightType.ERROR)
        }

    }
}