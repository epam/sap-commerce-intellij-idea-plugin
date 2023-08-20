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

package com.intellij.idea.plugin.hybris.codeInspection.rule.impex

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes.DOCUMENT_ID
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexFunctionTSAttributeReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSAttributeReference
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement

class ImpExIncompleteHeaderAbbreviationUsageInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexHeaderLineVisitor(holder)

    private class ImpexHeaderLineVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

        override fun visitParameter(parameter: ImpexParameter) {
            if (parameter.firstChild is ImpexMacroUsageDec || (parameter.firstChild as? LeafPsiElement)?.elementType == DOCUMENT_ID) return

            parameter.references
                .find { it is ImpexFunctionTSAttributeReference }
                ?.let { it as? ImpexFunctionTSAttributeReference }
                ?.takeIf { it.multiResolve(false).isEmpty() }
                ?.let { ref ->
                    parameter.itemTypeName
                        ?.let { registerProblem(ref, it) }
                }
                ?: return
        }

        override fun visitAnyHeaderParameterName(parameter: ImpexAnyHeaderParameterName) {
            if (parameter.firstChild is ImpexMacroUsageDec || (parameter.firstChild as? LeafPsiElement)?.elementType == DOCUMENT_ID) return

            parameter.references
                .find { it is ImpexTSAttributeReference }
                ?.let { it as? ImpexTSAttributeReference }
                ?.takeIf { it.multiResolve(false).isEmpty() }
                ?.let { ref ->
                    parameter.headerItemTypeName
                        ?.text
                        ?.let { registerProblem(ref, it) }
                }
                ?: return
        }

        private fun registerProblem(reference: TSReferenceBase<out PsiElement>, typeName: String) {
            problemsHolder.registerProblemForReference(
                reference,
                ProblemHighlightType.ERROR,
                message("hybris.inspections.UnknownTypeAttributeInspection.key", reference.value, typeName),
            )
        }
    }
}