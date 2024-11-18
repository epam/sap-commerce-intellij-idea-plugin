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
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexMacroReference
import com.intellij.psi.PsiElementVisitor

class ImpexUnknownMacrosInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = UnknownMacrosVisitor(holder)
}

private class UnknownMacrosVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

    override fun visitMacroUsageDec(usage: ImpexMacroUsageDec) {
        val text = usage.text
        if (text.startsWith(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) return

        if (usage.reference?.resolve() != null) return

        val macroName = text
            .takeIf { it.isNotEmpty() }
            ?.let { ImpexMacroReference.escapeName(it) }
            ?: return
        problemsHolder.registerProblem(
            usage,
            message("hybris.inspections.impex.ImpexUnknownMacrosInspection.key", macroName),
            ProblemHighlightType.ERROR
        )
    }
}