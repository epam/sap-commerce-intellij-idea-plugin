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
package sap.commerce.toolset.polyglotQuery.lang.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import sap.commerce.toolset.lang.annotation.AbstractAnnotator
import sap.commerce.toolset.polyglotQuery.highlighting.PolyglotQuerySyntaxHighlighter
import sap.commerce.toolset.polyglotQuery.psi.PolyglotQueryTypes.*

class PolyglotQueryAnnotator : AbstractAnnotator() {

    override val highlighter: SyntaxHighlighter
        get() = PolyglotQuerySyntaxHighlighter.getInstance()

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            IDENTIFIER -> when (element.parent.elementType) {
                BIND_PARAMETER -> highlight(BIND_PARAMETER, holder, element)
                LOCALIZED_NAME -> highlight(LOCALIZED_NAME, holder, element)
                TYPE_KEY_NAME -> element.parent.references.forEach { holder.highlightReference(it, TYPE_KEY_NAME) }
                ATTRIBUTE_KEY_NAME -> element.parent.references.forEach { holder.highlightReference(it, ATTRIBUTE_KEY_NAME) }
            }

            QUESTION_MARK -> when (element.parent.elementType) {
                BIND_PARAMETER -> highlight(BIND_PARAMETER, holder, element)
            }
        }
    }
}
