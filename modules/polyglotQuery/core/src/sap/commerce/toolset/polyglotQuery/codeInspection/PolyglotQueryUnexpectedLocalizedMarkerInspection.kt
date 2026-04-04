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
package sap.commerce.toolset.polyglotQuery.codeInspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.polyglotQuery.codeInspection.fix.PolyglotQueryDeleteValueGroupFix
import sap.commerce.toolset.polyglotQuery.psi.PolyglotQueryAttributeKey
import sap.commerce.toolset.polyglotQuery.psi.PolyglotQueryLocalized
import sap.commerce.toolset.polyglotQuery.psi.PolyglotQueryVisitor
import sap.commerce.toolset.typeSystem.psi.reference.result.TSResolveResultUtil

class PolyglotQueryUnexpectedLocalizedMarkerInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = PolyglotQueryPsiVisitor(holder)

    private class PolyglotQueryPsiVisitor(private val problemsHolder: ProblemsHolder) : PolyglotQueryVisitor() {

        override fun visitLocalized(element: PolyglotQueryLocalized) {
            val attribute = element.parent
                .asSafely<PolyglotQueryAttributeKey>()
                ?.attributeKeyName
                ?: return

            val featureName = attribute.text.trim()

            attribute.reference
                ?.asSafely<PsiPolyVariantReference>()
                ?.multiResolve(false)
                ?.firstOrNull()
                ?.takeUnless { TSResolveResultUtil.isLocalized(it, featureName) }
                ?.let {
                    problemsHolder.registerProblem(
                        element,
                        i18n("hybris.inspections.language.unexpected", featureName),
                        ProblemHighlightType.ERROR,
                        PolyglotQueryDeleteValueGroupFix(element, featureName)
                    )
                }
        }
    }
}

