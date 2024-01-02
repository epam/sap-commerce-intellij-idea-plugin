/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.codeInspection.fix.impex.ImpExConvertToSingleQuoteStringQuickFix
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.elementType
import com.intellij.psi.util.firstLeaf

class ImpExDoubleQuotedStringAsModifierInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.WEAK_WARNING
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexHeaderLineVisitor(holder)

    private class ImpexHeaderLineVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

        override fun visitAnyAttributeValue(element: ImpexAnyAttributeValue) {
            element.stringList
                .firstOrNull()
                ?.takeIf { it.firstLeaf.elementType == ImpexTypes.DOUBLE_STRING }
                ?.let {
                    problemsHolder.registerProblem(
                        it,
                        "Inverted Comma should be used to escape attribute modifier value. Double quote will be taken as a value.",
                        ProblemHighlightType.WEAK_WARNING,
                        ImpExConvertToSingleQuoteStringQuickFix(it)
                    )
                }

        }
    }
}