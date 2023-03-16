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
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.impex.ImpexParserDefinition
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class ImpexConfigProcessorInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ConfigProcessorVisitor(holder)
}

private class ConfigProcessorVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

    override fun visitMacroDeclaration(declaration: ImpexMacroDeclaration) {
        val macroValue = PsiTreeUtil.findChildOfType(declaration, ImpexMacroUsageDec::class.java) ?: return

        if (!macroValue.text.contains(HybrisConstants.IMPEX_CONFIG_PREFIX)) return

        var isExist = false
        PsiSearchHelper.getInstance(macroValue.project)
            .processElementsWithWord({ element, _ ->
                if (element.node.elementType != ImpexParserDefinition.FILE
                    && element.node.elementType != ImpexTypes.LINE_COMMENT) {
                    isExist = true
                }
                true
            }, GlobalSearchScope.fileScope(macroValue.containingFile),
                HybrisConstants.CLASS_CONFIG_IMPORT_PROCESSOR,
                UsageSearchContext.ANY,
                true,
                false)
        if (!isExist) {
            problemsHolder.registerProblem(
                macroValue,
                HybrisI18NBundleUtils.message("hybris.inspections.impex.ImpexConfigProcessorInspection.key", HybrisConstants.IMPEX_CONFIG_PREFIX),
                ProblemHighlightType.ERROR)
        }
    }
}

