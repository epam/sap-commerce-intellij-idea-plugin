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
package sap.commerce.toolset.flexibleSearch.lang.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import sap.commerce.toolset.flexibleSearch.highlighting.FlexibleSearchHighlighterColors
import sap.commerce.toolset.flexibleSearch.highlighting.FlexibleSearchSyntaxHighlighter
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchTypes.*
import sap.commerce.toolset.i18n
import sap.commerce.toolset.lang.annotation.AbstractAnnotator

class FlexibleSearchAnnotator : AbstractAnnotator() {

    override val highlighter: SyntaxHighlighter
        get() = FlexibleSearchSyntaxHighlighter.getInstance()

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            IDENTIFIER,
            BACKTICK_LITERAL -> when (element.parent.elementType) {
                FUNCTION_NAME -> highlight(FUNCTION_NAME, holder, element)
                EXT_PARAMETER_NAME -> highlight(EXT_PARAMETER_NAME, holder, element)
                TABLE_ALIAS_NAME -> highlight(TABLE_ALIAS_NAME, holder, element)
                COLUMN_ALIAS_NAME -> highlight(COLUMN_ALIAS_NAME, holder, element)
                COLUMN_LOCALIZED_NAME -> highlight(COLUMN_LOCALIZED_NAME, holder, element)

                COLUMN_NAME -> element.parent.references.forEach { holder.highlightReference(it, COLUMN_NAME) }
                Y_COLUMN_NAME -> element.parent.references.forEach { holder.highlightReference(it, Y_COLUMN_NAME) }
                SELECTED_TABLE_NAME -> element.parent.references.forEach { holder.highlightReference(it, SELECTED_TABLE_NAME) }
                DEFINED_TABLE_NAME -> element.parent.references.forEach { holder.highlightReference(it, DEFINED_TABLE_NAME) }
            }

            // Special case, [y] allows reserved words for attributes & types
            ORDER -> when (element.parent.elementType) {
                COLUMN_NAME -> highlight(COLUMN_NAME, holder, element)
                Y_COLUMN_NAME -> highlight(Y_COLUMN_NAME, holder, element)
                DEFINED_TABLE_NAME -> highlight(DEFINED_TABLE_NAME, holder, element)
                EXT_PARAMETER_NAME -> highlight(EXT_PARAMETER_NAME, holder, element)
            }

            STAR,
            EXCLAMATION_MARK,
            DASH_MARK -> when (element.parent.elementType) {
                DEFINED_TABLE_NAME -> highlight(FlexibleSearchHighlighterColors.FXS_TABLE_TAIL, holder, element)
            }

            // TODO: migrate to Inspection Rule
            TokenType.ERROR_ELEMENT -> when (element.parent.elementType) {
                COLUMN_LOCALIZED_NAME ->
                    highlightError(
                        holder, element,
                        i18n("hybris.inspections.fxs.element.language.missing")
                    )
            }
        }
    }

}
