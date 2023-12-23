/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.flexibleSearch.lang.annotation

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchSyntaxHighlighter
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchYColumnName
import com.intellij.idea.plugin.hybris.lang.annotation.AbstractAnnotator
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.TSResolveResultUtil
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.elementType

class FlexibleSearchAnnotator : AbstractAnnotator(FlexibleSearchSyntaxHighlighter.getInstance()) {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            IDENTIFIER,
            BACKTICK_LITERAL -> when (element.parent.elementType) {
                FUNCTION_NAME -> highlight(FUNCTION_NAME, holder, element)
                COLUMN_NAME -> highlightReference(COLUMN_NAME, holder, element, "hybris.inspections.fxs.unresolved.columnAlias.key")
                Y_COLUMN_NAME -> highlightReference(Y_COLUMN_NAME, holder, element, "hybris.inspections.fxs.unresolved.attribute.key")
                SELECTED_TABLE_NAME -> highlightReference(SELECTED_TABLE_NAME, holder, element, "hybris.inspections.fxs.unresolved.tableAlias.key")
                DEFINED_TABLE_NAME -> highlightReference(DEFINED_TABLE_NAME, holder, element, "hybris.inspections.fxs.unresolved.type.key")
                EXT_PARAMETER_NAME -> highlight(EXT_PARAMETER_NAME, holder, element)
                TABLE_ALIAS_NAME -> highlight(TABLE_ALIAS_NAME, holder, element)
                COLUMN_ALIAS_NAME -> highlight(COLUMN_ALIAS_NAME, holder, element)
                COLUMN_LOCALIZED_NAME -> highlight(COLUMN_LOCALIZED_NAME, holder, element)
            }

            // Special case, [y] allows reserved words for attributes & types
            ORDER -> when (element.parent.elementType) {
                COLUMN_NAME -> highlight(COLUMN_NAME, holder, element)
                Y_COLUMN_NAME -> highlight(Y_COLUMN_NAME, holder, element)
                DEFINED_TABLE_NAME -> highlight(DEFINED_TABLE_NAME, holder, element)
                EXT_PARAMETER_NAME -> highlight(EXT_PARAMETER_NAME, holder, element)
            }

            BOOLEAN_LITERAL -> highlight(
                textAttributesKey = null,
                holder = holder,
                element = element,
                highlightSeverity = HighlightSeverity.WARNING,
                message = "Since not all databases recognize true as a query parameter, 0 and 1 should be used instead of false and true.",
                fix = object : BaseIntentionAction() {

                    val newValue = if (element.text.trim().equals("true", true)) 1 else 0

                    override fun getFamilyName() = "[y] FlexibleSearch"
                    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?) = (file?.isWritable ?: false) && canModify(file)
                    override fun getText() = "Replace with $newValue"

                    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
                        if (editor == null || file == null) return

                        (element as? LeafPsiElement)
                            ?.replaceWithText(newValue.toString())
                    }
                }
            )

            COLON -> if (element.parent.elementType == COLUMN_SEPARATOR
                && element.parent.parent.elementType == COLUMN_REF_EXPRESSION
            ) {
                highlight(
                    textAttributesKey = null,
                    holder = holder,
                    element = element,
                    highlightSeverity = HighlightSeverity.ERROR,
                    message = message("hybris.inspections.fxs.element.separator.colon.notAllowed"),
                    fix = object : BaseIntentionAction() {

                        override fun getFamilyName() = "[y] FlexibleSearch"
                        override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?) = (file?.isWritable ?: false) && canModify(file)
                        override fun getText() = "Replace with '.'"

                        override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
                            if (editor == null || file == null) return

                            (element as? LeafPsiElement)
                                ?.replaceWithText(HybrisConstants.FXS_TABLE_ALIAS_SEPARATOR_DOT)
                        }
                    }
                )
            }

            STAR,
            EXCLAMATION_MARK -> when (element.parent.elementType) {
                DEFINED_TABLE_NAME -> highlight(FlexibleSearchHighlighterColors.FXS_TABLE_TAIL, holder, element)
            }

            COLUMN_LOCALIZED_NAME -> {
                val language = element.text.trim()

                val propertiesService = PropertyService.getInstance(element.project) ?: return
                val supportedLanguages = propertiesService.getLanguages()

                if (!propertiesService.containsLanguage(language, supportedLanguages)) {
                    highlightError(
                        holder, element,
                        message(
                            "hybris.inspections.language.unsupported",
                            language,
                            supportedLanguages.joinToString()
                        )
                    )
                }

                element.parent.childrenOfType<FlexibleSearchYColumnName>()
                    .firstOrNull()
                    ?.let { yColumn ->
                        val featureName = yColumn.text.trim()
                        (yColumn.reference as? PsiReferenceBase.Poly<*>)
                            ?.multiResolve(false)
                            ?.firstOrNull()
                            ?.takeIf { !TSResolveResultUtil.isLocalized(it, featureName) }
                            ?.let {
                                highlightError(
                                    holder, element,
                                    message("hybris.inspections.language.unexpected", featureName)
                                )
                            }
                    }
            }

            TokenType.ERROR_ELEMENT -> when (element.parent.elementType) {
                COLUMN_LOCALIZED_NAME ->
                    highlightError(
                        holder, element,
                        message("hybris.inspections.fxs.element.language.missing")
                    )
            }
        }
    }

}
