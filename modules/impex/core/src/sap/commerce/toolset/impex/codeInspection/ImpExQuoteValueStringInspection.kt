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
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.codeInspection.fix.ImpExDisableQuotingForAttributeQuickFix
import sap.commerce.toolset.impex.codeInspection.fix.ImpExEnableQuotingForAttributeQuickFix
import sap.commerce.toolset.impex.codeInspection.fix.ImpExQuoteStringQuickFix
import sap.commerce.toolset.impex.psi.ImpExAnyHeaderParameterName
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValue
import sap.commerce.toolset.impex.psi.ImpExVisitor
import sap.commerce.toolset.impex.psi.references.ImpExTSAttributeReference
import sap.commerce.toolset.settings.yDeveloperSettings
import sap.commerce.toolset.typeSystem.psi.reference.result.AttributeResolveResult

class ImpExQuoteValueStringInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.WEAK_WARNING
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : ImpExVisitor() {

        override fun visitFullHeaderParameter(headerParameter: ImpExFullHeaderParameter) {
            val parameterName = headerParameter.anyHeaderParameterName
            val attributeMeta = parameterName.applicableItemAttribute ?: return
            val typeName = attributeMeta.owner.name ?: return
            val attributeName = attributeMeta.name
            val impExSettings = headerParameter.project.yDeveloperSettings.impexSettings
            val isAttributeExcluded = impExSettings.quoteStringExclusions[typeName]
                ?.contains(attributeName)
                ?: false
            val unquotedValues = headerParameter.valueGroups
                .mapNotNull { it.value }
                .filter { it.isImportable }
                .filter { it.isQuotable }

            val whitelistedValues = unquotedValues
                .filterNot { impExSettings.quoteStringWhitelist && impExSettings.quoteStringWhitelistPattern.matches(it.text.trim()) }

            if (isAttributeExcluded) {
                holder.registerProblem(
                    parameterName,
                    i18n("hybris.inspections.impex.ImpExQuoteValueStringInspection.enable.key", typeName, attributeName),
                    ProblemHighlightType.INFORMATION,
                    ImpExEnableQuotingForAttributeQuickFix(typeName, attributeName),
                )
                return
            } else {
                val highlightType = if (whitelistedValues.isNotEmpty()) ProblemHighlightType.WEAK_WARNING
                else ProblemHighlightType.INFORMATION
                holder.registerProblem(
                    parameterName,
                    i18n("hybris.inspections.impex.ImpExQuoteValueStringInspection.exclude.key", typeName, attributeName),
                    highlightType,
                    ImpExDisableQuotingForAttributeQuickFix(typeName, attributeName),
                )
            }

            if (unquotedValues.isNotEmpty()) {
                fun registerInfoQuickFix(checkValuePattern: Boolean, presentationText: String) {
                    holder.registerProblem(
                        parameterName,
                        i18n("hybris.inspections.impex.ImpExQuoteValueStringInspection.forceQuote.key", typeName, attributeName),
                        ProblemHighlightType.INFORMATION,
                        ImpExQuoteStringQuickFix(
                            element = parameterName,
                            checkValuePattern = checkValuePattern,
                            presentationText = presentationText,
                            overridePreviewInfo = IntentionPreviewInfo.EMPTY
                        ),
                    )
                }

                if (whitelistedValues.isNotEmpty()) registerInfoQuickFix(
                    checkValuePattern = true,
                    presentationText = "Quote ${whitelistedValues.size} eligible value strings for: '$typeName.$attributeName'"
                )

                registerInfoQuickFix(
                    checkValuePattern = false,
                    presentationText = "Quote all ${unquotedValues.size} value strings for: '$typeName.$attributeName'"
                )
            }
        }

        override fun visitValue(value: ImpExValue) {
            if (value.isNonImportable) return
            if (value.isNotQuotable) return

            val impExSettings = value.project.yDeveloperSettings.impexSettings
            val attributeMeta = value.valueGroup
                ?.fullHeaderParameter
                ?.anyHeaderParameterName
                ?.applicableItemAttribute
                ?: return
            val typeName = attributeMeta.owner.name ?: return
            val attributeName = attributeMeta.name
            val trimmedText = value.text.trim()

            fun registerFix(type: ProblemHighlightType) {
                holder.registerProblem(
                    value,
                    i18n("hybris.inspections.impex.ImpExQuoteValueStringInspection.key", typeName, attributeName, StringUtil.shortenPathWithEllipsis(trimmedText, 25)),
                    type,
                    ImpExQuoteStringQuickFix(
                        element = value,
                        presentationText = "Quote '$typeName.$attributeName' value string: '${StringUtil.shortenPathWithEllipsis(value.text, 25)}'"
                    )
                )
            }

            val isExcluded = impExSettings.quoteStringExclusions[typeName]
                ?.contains(attributeName)
                ?: false
            if (isExcluded) {
                registerFix(ProblemHighlightType.INFORMATION)
                return
            }

            if (impExSettings.quoteStringWhitelist && impExSettings.quoteStringWhitelistPattern.matches(trimmedText)) {
                // ignore whitelisted value
                registerFix(ProblemHighlightType.INFORMATION)
                return
            }

            registerFix(ProblemHighlightType.WEAK_WARNING)
        }

        private val ImpExAnyHeaderParameterName.applicableItemAttribute
            get() = reference
                ?.asSafely<ImpExTSAttributeReference>()
                ?.multiResolve(false)
                ?.firstOrNull()
                ?.asSafely<AttributeResolveResult>()
                ?.meta
                ?.takeIf {
                    "localized:java.lang.String" == it.type || "java.lang.String" == it.type && !it.modifiers.isUnique
                }
    }
}