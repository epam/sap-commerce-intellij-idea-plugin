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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.PsiClassImplUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.constants.modifier.ImpexModifier
import sap.commerce.toolset.impex.constants.modifier.TypeModifier
import sap.commerce.toolset.impex.psi.ImpexAnyAttributeValue
import sap.commerce.toolset.impex.psi.ImpexVisitor
import sap.commerce.toolset.impex.psi.references.ImpExJavaClassReference

abstract class AbstractImpExInvalidClassReferenceModifierInspection(
    private val modifier: ImpexModifier,
    private vararg val targetTypes: String,
    private val targetShortTypes: String = targetTypes
        .map { it.substringAfterLast(".") }
        .joinToString(" or ") { "'$it'" }
) : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : ImpexVisitor() {

        override fun visitAnyAttributeValue(element: ImpexAnyAttributeValue) {
            if (modifier.modifierName != element.anyAttributeName?.text) return
            if (element.text.startsWith("$")) return
            val reference = element.reference

            if (reference == null) unresolvedReference(element)
            else if (reference is ImpExJavaClassReference) {
                val psiClass = reference.resolve() as? PsiClass

                if (psiClass == null) {
                    unresolvedReference(element)
                    return
                }

                val superClass = PsiClassImplUtil.getAllSuperClassesRecursively(psiClass)
                    .firstOrNull { targetTypes.contains(it.qualifiedName) }

                if (superClass != null) return

                holder.registerProblem(
                    element,
                    i18n(
                        "hybris.inspections.impex.ImpExUnresolvedClassReferenceInspection.wrongImplementation",
                        modifier.modifierName,
                        element.text,
                        targetShortTypes
                    ),
                    ProblemHighlightType.ERROR
                )
            }
        }

        private fun unresolvedReference(element: ImpexAnyAttributeValue) {
            holder.registerProblem(
                element,
                i18n("hybris.inspections.impex.ImpExUnresolvedClassReferenceInspection.unresolved", modifier.modifierName, element.text),
                ProblemHighlightType.ERROR
            )
        }
    }
}

class ImpExInvalidProcessorValueInspection : AbstractImpExInvalidClassReferenceModifierInspection(
    TypeModifier.PROCESSOR,
    HybrisConstants.CLASS_FQN_IMPEX_PROCESSOR
)

class ImpExInvalidCellDecoratorValueInspection : AbstractImpExInvalidClassReferenceModifierInspection(
    AttributeModifier.CELL_DECORATOR,
    HybrisConstants.CLASS_FQN_IMPEX_CELL_DECORATOR
)

class ImpExInvalidTranslatorValueInspection : AbstractImpExInvalidClassReferenceModifierInspection(
    AttributeModifier.TRANSLATOR,
    *HybrisConstants.CLASS_FQN_IMPEX_TRANSLATORS
)
