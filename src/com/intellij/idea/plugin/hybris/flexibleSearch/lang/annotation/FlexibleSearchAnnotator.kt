/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchSyntaxHighlighter
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType

class FlexibleSearchAnnotator : Annotator {

    private val highlighter: FlexibleSearchSyntaxHighlighter by lazy {
        FlexibleSearchSyntaxHighlighter.instance
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            IDENTIFIER,
            BACKTICK_LITERAL -> when (element.parent.elementType) {
                FUNCTION_NAME -> highlight(FUNCTION_NAME, holder, element)
                COLUMN_NAME -> highlight(COLUMN_NAME, holder, element)
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

            STAR,
            EXCLAMATION_MARK -> when (element.parent.elementType) {
                DEFINED_TABLE_NAME -> highlight(FlexibleSearchHighlighterColors.FS_TABLE_TRAIL, holder, element)
            }

            TokenType.ERROR_ELEMENT -> when (element.parent.elementType) {
                COLUMN_LOCALIZED_NAME -> annotation(
                    message("hybris.editor.annotator.fxs.missingLangLiteral"),
                    holder,
                    HighlightSeverity.ERROR
                )
                    .range(element.textRange)
                    .highlightType(ProblemHighlightType.ERROR)
                    .create()
            }
        }
    }

    private fun highlightReference(
        tokenType: IElementType,
        holder: AnnotationHolder,
        element: PsiElement,
        messageKey: String
    ) {
        val resolved = (element.parent.reference as? PsiReferenceBase.Poly<*>)
            ?.multiResolve(true)
            ?.isNotEmpty()
            ?: true

        if (resolved) {
            highlight(tokenType, holder, element)
        } else {
            annotation(
                message(messageKey, element.text),
                holder,
                HighlightSeverity.ERROR
            )
                .range(element.textRange)
                .highlightType(ProblemHighlightType.ERROR)
                .create()
        }
    }

    private fun highlight(
        tokenType: IElementType,
        holder: AnnotationHolder,
        element: PsiElement,
        highlightSeverity: HighlightSeverity = HighlightSeverity.TEXT_ATTRIBUTES,
        range: TextRange = element.textRange,
        message: String? = null
    ) = highlighter
        .getTokenHighlights(tokenType)
        .firstOrNull()
        ?.let { highlight(it, holder, element, highlightSeverity, range, message) }

    private fun highlight(
        textAttributesKey: TextAttributesKey,
        holder: AnnotationHolder,
        element: PsiElement,
        highlightSeverity: HighlightSeverity = HighlightSeverity.TEXT_ATTRIBUTES,
        range: TextRange = element.textRange,
        message: String? = null
    ) {
        annotation(message, holder, highlightSeverity)
            .range(range)
            .textAttributes(textAttributesKey)
            .create()
    }

    private fun annotation(
        message: String?,
        holder: AnnotationHolder,
        highlightSeverity: HighlightSeverity
    ) = message
        ?.let { m -> holder.newAnnotation(highlightSeverity, m) }
        ?: holder.newSilentAnnotation(highlightSeverity)
}
