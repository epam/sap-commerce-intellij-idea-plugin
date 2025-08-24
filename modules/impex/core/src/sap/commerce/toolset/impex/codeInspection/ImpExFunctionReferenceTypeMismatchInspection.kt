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
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpExParameter
import sap.commerce.toolset.impex.psi.ImpExVisitor
import sap.commerce.toolset.impex.psi.references.ImpExFunctionTSItemReference
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.*

class ImpExFunctionReferenceTypeMismatchInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpExHeaderLineVisitor(holder)

    private class ImpExHeaderLineVisitor(private val problemsHolder: ProblemsHolder) : ImpExVisitor() {

        override fun visitParameter(parameter: ImpExParameter) {
            if (parameter.firstChild is ImpExMacroUsageDec || parameter.firstChild is ImpExDocumentIdUsage) return

            val typeReference = parameter.references
                .find { it is ImpExFunctionTSItemReference }
                ?.asSafely<ImpExFunctionTSItemReference>()
                ?.takeIf { it.multiResolve(false).isNotEmpty() }
                ?: return

            val expectedItemType = parameter.referenceItemTypeName ?: return
            val inlineType = typeReference.value
            val metaModelAccess = TSMetaModelAccess.getInstance(parameter.project)
            val referenceMeta = metaModelAccess.findMetaClassifierByName(expectedItemType)
                ?: return

            when (referenceMeta) {
                is TSGlobalMetaItem -> {
                    val notExtends = metaModelAccess.findMetaItemByName(inlineType)
                        ?.allExtends
                        ?.contains(referenceMeta)
                        ?: false
                    if (!notExtends && !inlineType.equals(expectedItemType, true)) {
                        problemsHolder.registerProblemForReference(
                            typeReference,
                            ProblemHighlightType.ERROR,
                            i18n(
                                "hybris.inspections.impex.ImpexMismatchFunctionTypeInspection.key",
                                inlineType,
                                expectedItemType,
                                parameter.referenceName ?: "?"
                            )
                        )
                    }
                }

                is TSGlobalMetaEnum -> registerProblemTypeMismatch(typeReference, inlineType, "Enum")
                is TSGlobalMetaCollection -> registerProblemTypeMismatch(typeReference, inlineType, "Collection")
                is TSGlobalMetaMap -> registerProblemTypeMismatch(typeReference, inlineType, "Map")
                is TSGlobalMetaRelation -> registerProblemTypeMismatch(typeReference, inlineType, "Relation")
                is TSGlobalMetaAtomic -> registerProblemTypeMismatch(typeReference, inlineType, "Atomic")
            }
        }

        private fun registerProblemTypeMismatch(
            typeReference: ImpExFunctionTSItemReference,
            inlineType: String,
            type: String
        ) {
            problemsHolder.registerProblemForReference(
                typeReference,
                ProblemHighlightType.ERROR,
                i18n("hybris.inspections.impex.ImpexMismatchFunctionTypeInspection.onlyItemType.key", inlineType, type),
            )
        }

    }
}