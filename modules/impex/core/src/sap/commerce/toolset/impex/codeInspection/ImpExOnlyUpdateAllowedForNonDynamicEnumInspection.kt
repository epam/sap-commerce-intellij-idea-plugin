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

package sap.commerce.toolset.impex.codeInspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.codeInspection.fix.ImpExChangeHeaderModeQuickFix
import sap.commerce.toolset.impex.constants.HeaderMode
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.impex.psi.ImpExHeaderTypeName
import sap.commerce.toolset.impex.psi.ImpExVisitor
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess

class ImpExOnlyUpdateAllowedForNonDynamicEnumInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpExOnlyUpdateAllowedForNonDynamicEnumVisitor(holder)

    private class ImpExOnlyUpdateAllowedForNonDynamicEnumVisitor(private val problemsHolder: ProblemsHolder) : ImpExVisitor() {

        override fun visitHeaderTypeName(parameter: ImpExHeaderTypeName) {
            val typeName = PsiTreeUtil.getParentOfType(parameter, ImpExHeaderLine::class.java)
                ?.fullHeaderType
                ?.headerTypeName
                ?.text
                ?: return

            val meta = TSMetaModelAccess.getInstance(parameter.firstChild.project).findMetaEnumByName(typeName)
                ?.takeUnless { it.isDynamic }
                ?: return

            val mode = PsiTreeUtil.getParentOfType(parameter, ImpExHeaderLine::class.java)
                ?.anyHeaderMode
                ?: return

            val impexModeUpdate = HeaderMode.UPDATE.name
            val impexModeRemove = HeaderMode.REMOVE.name

            val modeName = mode.text
                ?.uppercase()
                ?.takeUnless { it == impexModeUpdate || it == impexModeRemove }
                ?: return

            val enumName = meta.name ?: typeName

            problemsHolder.registerProblem(
                mode,
                i18n("hybris.inspections.impex.ImpexOnlyUpdateOrRemoveAllowedForNonDynamicEnumInspection.key", modeName, impexModeUpdate),
                ProblemHighlightType.ERROR,
                ImpExChangeHeaderModeQuickFix(
                    headerMode = mode,
                    elementName = enumName,
                    headerModeReplacement = HeaderMode.UPDATE
                ),
                ImpExChangeHeaderModeQuickFix(
                    headerMode = mode,
                    elementName = enumName,
                    headerModeReplacement = HeaderMode.REMOVE
                )
            )
        }
    }
}