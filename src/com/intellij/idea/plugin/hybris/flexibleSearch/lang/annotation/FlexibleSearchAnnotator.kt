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

import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchSyntaxHighlighter
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType

class FlexibleSearchAnnotator : Annotator {

    private val highlighter: FlexibleSearchSyntaxHighlighter by lazy {
        ApplicationManager.getApplication().getService(FlexibleSearchSyntaxHighlighter::class.java)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.elementType != FlexibleSearchTypes.IDENTIFIER) return

        when (element.parent.elementType) {
            FlexibleSearchTypes.COLUMN_NAME -> highlight(FlexibleSearchTypes.COLUMN_NAME, holder, element)
        }

    }

    private fun highlight(tokenType: IElementType, holder: AnnotationHolder, element: PsiElement) = highlighter
        .getTokenHighlights(tokenType)
        .firstOrNull()
        ?.let {
            holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(element.textRange)
                .textAttributes(it)
                .create()
        }
}
