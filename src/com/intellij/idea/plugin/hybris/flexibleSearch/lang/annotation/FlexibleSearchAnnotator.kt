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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchSyntaxHighlighter
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType

class FlexibleSearchAnnotator : Annotator {

    private val highlighter: FlexibleSearchSyntaxHighlighter by lazy {
        ApplicationManager.getApplication().getService(FlexibleSearchSyntaxHighlighter::class.java)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            IDENTIFIER,
            BACKTICK_LITERAL -> when (element.parent.elementType) {
                COLUMN_NAME -> highlight(COLUMN_NAME, holder, element)
                FUNCTION_NAME -> highlight(FUNCTION_NAME, holder, element)
                DEFINED_TABLE_NAME -> highlight(DEFINED_TABLE_NAME, holder, element)
                EXT_PARAMETER_NAME -> highlight(EXT_PARAMETER_NAME, holder, element)
            }

            BRACKET_LITERAL -> if (element.parent.elementType == COLUMN_LOCALIZED_NAME) {
                val text = element.text
                    .let { it.substring(it.indexOf('[') + 1, it.indexOf(']')) }
                    .trim()

                if (text.isEmpty()) {
                    highlight(TokenType.BAD_CHARACTER, holder, element,
                        highlightSeverity = HighlightSeverity.WARNING,
                        message = HybrisI18NBundleUtils.message("hybris.editor.annotator.fxs.missingLangLiteral")
                    )
                } else {
                    val startOffset = element.textRange.startOffset + element.text.indexOf(text)
                    highlight(COLUMN_LOCALIZED_NAME, holder, element, range = TextRange.from(startOffset, text.length))
                }
            }
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
        ?.let {
            val annotation = message
                ?.let { m -> holder.newAnnotation(highlightSeverity, m) }
                ?: holder.newSilentAnnotation(highlightSeverity)
            annotation
                .range(range)
                .textAttributes(it)
                .create()
        }
}
