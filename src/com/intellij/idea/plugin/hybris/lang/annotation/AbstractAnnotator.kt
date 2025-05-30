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

package com.intellij.idea.plugin.hybris.lang.annotation

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.tree.IElementType
import com.intellij.util.asSafely

abstract class AbstractAnnotator(private val highlighter: SyntaxHighlighter) : Annotator {

    fun highlightError(
        holder: AnnotationHolder,
        element: PsiElement,
        message: String,
        severity: HighlightSeverity = HighlightSeverity.ERROR,
        type: ProblemHighlightType = ProblemHighlightType.ERROR,
        range: TextRange = element.textRange
    ) {
        annotation(
            message,
            holder,
            severity
        )
            .range(range)
            .highlightType(type)
            .create()
    }

    fun highlightReference(
        tokenType: IElementType,
        holder: AnnotationHolder,
        element: PsiElement,
        messageKey: String,
        referenceHolder: PsiElement = element.parent
    ) {
        val resolved = (referenceHolder.reference as? PsiReferenceBase.Poly<*>)
            ?.multiResolve(true)
            ?.isNotEmpty()
            ?: true

        if (resolved) {
            highlight(tokenType, holder, element)
        } else {
            highlightError(holder, element, message(messageKey, element.text))
        }
    }

    fun highlightReference(
        textAttributesKey: TextAttributesKey?,
        holder: AnnotationHolder,
        element: PsiElement,
        messageKey: String,
        reference: PsiReference
    ) {
        val range = reference.absoluteRange
        val isValid = reference.asSafely<PsiReferenceBase.Poly<PsiElement>>()
            ?.multiResolve(false)
            ?.isNotEmpty()
            ?: (reference.resolve() != null)

        if (isValid) {
            highlight(textAttributesKey, holder, element, range = range)
        } else {
            highlightError(holder, element, message(messageKey, reference.canonicalText), range = range)
        }
    }

    fun highlight(
        tokenType: IElementType,
        holder: AnnotationHolder,
        element: PsiElement,
        highlightSeverity: HighlightSeverity = HighlightSeverity.TEXT_ATTRIBUTES,
        range: TextRange = element.textRange,
        message: String? = null,
        fix: IntentionAction? = null,
    ) = highlighter
        .getTokenHighlights(tokenType)
        .firstOrNull()
        ?.let { highlight(it, holder, element, highlightSeverity, range, message, fix) }

    fun highlight(
        textAttributesKey: TextAttributesKey?,
        holder: AnnotationHolder,
        element: PsiElement,
        highlightSeverity: HighlightSeverity = HighlightSeverity.TEXT_ATTRIBUTES,
        range: TextRange = element.textRange,
        message: String? = null,
        fix: IntentionAction? = null,
    ) {
        val builder = annotation(message, holder, highlightSeverity)
            .range(range)
        textAttributesKey?.let { builder.textAttributes(it) }
        fix?.let { builder.withFix(it) }
        builder.create()
    }

    fun annotation(
        message: String?,
        holder: AnnotationHolder,
        highlightSeverity: HighlightSeverity
    ) = message
        ?.let { m -> holder.newAnnotation(highlightSeverity, m) }
        ?: holder.newSilentAnnotation(highlightSeverity)
}