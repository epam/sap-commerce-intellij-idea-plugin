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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.ImpExSubTypeName
import sap.commerce.toolset.impex.psi.ImpExUserRightsAttributeValue
import sap.commerce.toolset.impex.psi.ImpExUserRightsSingleValue
import sap.commerce.toolset.impex.psi.ImpExValue
import sap.commerce.toolset.impex.psi.references.ImpExDocumentIdUsageReference
import sap.commerce.toolset.impex.psi.references.ImpExValueTSClassifierReference
import sap.commerce.toolset.impex.psi.references.ImpExValueTSStaticEnumReference
import sap.commerce.toolset.spring.psi.reference.SpringReference

class ImpExUnresolvedReferenceInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpExVisitor(holder)

    private class ImpExVisitor(private val problemsHolder: ProblemsHolder) : sap.commerce.toolset.impex.psi.ImpExVisitor() {

        override fun visitUserRightsSingleValue(element: ImpExUserRightsSingleValue) = element.verifyReferences {
            i18n("hybris.inspections.impex.unresolved.type.key", canonicalText)
        }

        override fun visitUserRightsAttributeValue(element: ImpExUserRightsAttributeValue) = element.verifyReferences {
            i18n("hybris.inspections.impex.unresolved.type.key", canonicalText)
        }

        override fun visitSubTypeName(element: ImpExSubTypeName) = element.verifyReferences {
            i18n("hybris.inspections.impex.unresolved.subType.key", canonicalText)
        }

        override fun visitValue(element: ImpExValue) = element.verifyReferences {
            when (this) {
                is ImpExValueTSStaticEnumReference -> "hybris.inspections.impex.unresolved.enumValue.key"
                is ImpExValueTSClassifierReference -> "hybris.inspections.impex.unresolved.composedType.key"
                is ImpExDocumentIdUsageReference -> "hybris.inspections.impex.unresolved.docUsage.key"
                is SpringReference -> "hybris.inspections.impex.unresolved.springBean.key"
                else -> null
            }
                ?.let { i18n(it, canonicalText) }
        }

        private fun PsiElement.verifyReferences(descriptionProvider: PsiReference.() -> String?) = references
            .filter {
                it.asSafely<PsiPolyVariantReference>()
                    ?.multiResolve(false)
                    ?.isEmpty()
                    ?: (it.resolve() == null)
            }
            .forEach { reference ->
                val description = descriptionProvider(reference) ?: return@forEach
                problemsHolder.registerProblem(reference, description, ProblemHighlightType.ERROR)
            }
    }
}
