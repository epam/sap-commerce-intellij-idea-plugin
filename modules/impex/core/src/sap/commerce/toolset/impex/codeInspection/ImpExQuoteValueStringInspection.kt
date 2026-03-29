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
import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.*
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

            val hasUnquotedValues = headerParameter.valueGroups
                .mapNotNull { it.value }
                .any { it.text.isNotBlank() && !it.text.trim().startsWith("\"") }

            if (hasUnquotedValues) {
                val isExcluded = headerParameter.project.yDeveloperSettings.impexSettings.quoteStringsExclusions[typeName]
                    ?.contains(attributeName)
                    ?: false
                // already excluded
                // TODO: add one more "fix" to re-enable quotes for value strings
                if (isExcluded) return

                holder.registerProblem(
                    parameterName,
                    i18n("hybris.inspections.impex.ImpExQuoteValueStringInspection.exclude.key", typeName, attributeName),
                    ProblemHighlightType.WEAK_WARNING,
                    LocalFixExclude(typeName, attributeName),
                )
            }
        }

        override fun visitValue(value: ImpExValue) {
            val trimmedText = value.text.trim()
            if (trimmedText.startsWith('\"')) return
            if (trimmedText.isBlank()) return

            val attributeMeta = value.valueGroup
                ?.fullHeaderParameter
                ?.anyHeaderParameterName
                ?.applicableItemAttribute
                ?: return

            val typeName = attributeMeta.owner.name ?: return
            val attributeName = attributeMeta.name

            val isExcluded = value.project.yDeveloperSettings.impexSettings.quoteStringsExclusions[typeName]
                ?.contains(attributeName)
                ?: false
            if (isExcluded) return

            holder.registerProblem(
                value,
                i18n("hybris.inspections.impex.ImpExQuoteValueStringInspection.key", typeName, attributeName, StringUtil.shortenPathWithEllipsis(trimmedText, 25)),
                ProblemHighlightType.WEAK_WARNING,
                LocalFix(value, typeName, attributeName)
            )
        }

        private val ImpExAnyHeaderParameterName.applicableItemAttribute
            get() = reference
                ?.asSafely<ImpExTSAttributeReference>()
                ?.multiResolve(false)
                ?.firstOrNull()
                ?.asSafely<AttributeResolveResult>()
                ?.meta
                ?.takeIf {
                    "localized:java.lang.String" == it.type
                        || "java.lang.String" == it.type && !it.modifiers.isUnique
                }

        private class LocalFix(
            element: PsiElement,
            private val typeName: String,
            private val attributeName: String,
            private val shortText: String = StringUtil.shortenPathWithEllipsis(element.text, 25)
        ) : LocalQuickFixOnPsiElement(element), LowPriorityAction {

            override fun getFamilyName() = "[y] Quote value string"
            override fun getText() = "Wrap in quotes '$typeName.$attributeName' value string: '$shortText'"

            override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
                val newValue = startElement.text.replace("\"", "\"\"")

                val newElement = ImpExElementFactory.createFile(
                    project, """
                        UPDATE Title;name;
                        ; "$newValue"

                """.trimIndent()
                )
                    .childrenOfType<ImpExValueLine>()
                    .flatMap { it.valueGroupList }
                    .mapNotNull { it.value }
                    .lastOrNull()
                    ?: return

                startElement.replace(newElement)
            }
        }

        private class LocalFixExclude(
            private val typeName: String,
            private val attributeName: String,
        ) : LocalQuickFix, HighPriorityAction {

            override fun getFamilyName() = "[y] Exclusion: quote value strings"
            override fun getName() = "Do not quote value strings for: '$typeName.$attributeName'"
            override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo = IntentionPreviewInfo.EMPTY

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                project.yDeveloperSettings.impexSettings = project.yDeveloperSettings.impexSettings.mutable()
                    .apply {
                        quoteStringsExclusions.getOrPut(typeName) { mutableSetOf() }
                            .add(attributeName)
                    }
                    .immutable()
            }

        }
    }
}