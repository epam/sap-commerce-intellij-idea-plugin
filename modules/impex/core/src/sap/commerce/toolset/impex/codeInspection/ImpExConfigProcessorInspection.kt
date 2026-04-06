/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.codeInspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.codeInspection.fix.ImpExGenerateConfigImportProcessorQuickFix
import sap.commerce.toolset.impex.psi.ImpExMacroDeclaration
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.psi.ImpExVisitor

class ImpExConfigProcessorInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = Visitor(holder)

    private class Visitor(private val problemsHolder: ProblemsHolder) : ImpExVisitor() {

        override fun visitMacroDeclaration(declaration: ImpExMacroDeclaration) {
            val macroValue = PsiTreeUtil.findChildOfType(declaration, ImpExMacroUsageDec::class.java) ?: return

            if (!macroValue.text.contains(ImpExConstants.IMPEX_CONFIG_PREFIX)) return

            var isExist = false
            PsiSearchHelper.getInstance(macroValue.project)
                .processElementsWithWord(
                    { element, _ ->
                        if (element.node.elementType != ImpExConstants.FILE_NODE_TYPE
                            && element.node.elementType != ImpExTypes.LINE_COMMENT
                        ) {
                            isExist = true
                        }
                        true
                    }, GlobalSearchScope.fileScope(macroValue.containingFile),
                    HybrisConstants.CLASS_FQN_CONFIG_IMPORT_PROCESSOR,
                    UsageSearchContext.ANY,
                    true,
                    false
                )
            if (!isExist) {
                problemsHolder.registerProblem(
                    macroValue,
                    i18n("hybris.inspections.impex.ImpExConfigProcessorInspection.key", ImpExConstants.IMPEX_CONFIG_PREFIX),
                    ProblemHighlightType.ERROR,
                    ImpExGenerateConfigImportProcessorQuickFix(macroValue)
                )
            }
        }

    }
}

