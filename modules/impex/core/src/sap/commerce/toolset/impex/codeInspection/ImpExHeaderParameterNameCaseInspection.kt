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

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.codeInspection.fix.ImpExCorrectHeaderParameterNameQuickFix
import sap.commerce.toolset.impex.psi.ImpExAnyHeaderParameterName
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpExVisitor
import sap.commerce.toolset.impex.psi.references.ImpExTSAttributeReference
import sap.commerce.toolset.typeSystem.psi.reference.result.TSResolveResult

class ImpExHeaderParameterNameCaseInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : ImpExVisitor() {

        override fun visitAnyHeaderParameterName(element: ImpExAnyHeaderParameterName) {
            if (element.firstChild is ImpExMacroUsageDec || element.firstChild is ImpExDocumentIdUsage) return

            val ref = element.references
                .find { it is ImpExTSAttributeReference }
                .asSafely<ImpExTSAttributeReference>()
                ?: return

            val resolveResults = ref.multiResolve(false)
            if (resolveResults.isEmpty()) return

            val canonicalName = resolveResults.first()
                .asSafely<TSResolveResult<*>>()
                ?.meta
                ?.name
                ?: return
            if (canonicalName == ref.value) return

            val typeName = element.headerItemTypeName?.text ?: return

            holder.registerProblem(
                element,
                i18n("hybris.inspections.impex.ImpExHeaderParameterNameCaseInspection.key", ref.value, canonicalName, typeName),
                ProblemHighlightType.WEAK_WARNING,
                ImpExCorrectHeaderParameterNameQuickFix(element, canonicalName)
            )
        }
    }
}
