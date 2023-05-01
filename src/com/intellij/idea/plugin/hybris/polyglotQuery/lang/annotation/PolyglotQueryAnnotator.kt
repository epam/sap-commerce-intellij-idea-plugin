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
package com.intellij.idea.plugin.hybris.polyglotQuery.lang.annotation

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQuerySyntaxHighlighter
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType

class PolyglotQueryAnnotator : Annotator {

    private val highlighter: PolyglotQuerySyntaxHighlighter by lazy {
        PolyglotQuerySyntaxHighlighter.instance
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            IDENTIFIER -> when (element.parent.elementType) {
                TYPE_KEY -> highlight(TYPE_KEY, holder, element)
                BIND_PARAMETER -> highlight(BIND_PARAMETER, holder, element)
                LOCALIZED_NAME -> highlight(LOCALIZED_NAME, holder, element)
                ATTRIBUTE_KEY -> highlight(ATTRIBUTE_KEY, holder, element)
            }

            QUESTION_MARK -> when (element.parent.elementType) {
                BIND_PARAMETER -> highlight(BIND_PARAMETER, holder, element)
            }
        }
    }

    private fun highlightError(holder: AnnotationHolder, element: PsiElement, message: String) {
        annotation(
            message,
            holder,
            HighlightSeverity.ERROR
        )
            .range(element.textRange)
            .highlightType(ProblemHighlightType.ERROR)
            .create()
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
