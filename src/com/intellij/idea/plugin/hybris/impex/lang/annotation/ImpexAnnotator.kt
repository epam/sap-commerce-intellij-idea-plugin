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
package com.intellij.idea.plugin.hybris.impex.lang.annotation

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.highlighting.DefaultImpexSyntaxHighlighter
import com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.lang.annotation.AbstractAnnotator
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class ImpexAnnotator : AbstractAnnotator(DefaultImpexSyntaxHighlighter.instance) {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            ImpexTypes.VALUE_SUBTYPE -> {
                highlightReference(ImpexTypes.VALUE_SUBTYPE, holder, element, "hybris.inspections.impex.unresolved.subType.key")
            }

            ImpexTypes.MACRO_USAGE -> {
                if (element.text.startsWith(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
                    val macroUsageDec = element.parent as? ImpexMacroUsageDec ?: return

                    val propertyKey = macroUsageDec.configPropertyKey
                        ?: element.text.replace(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "")

                    highlight(
                        ImpexHighlighterColors.MACRO_CONFIG_KEY,
                        holder,
                        element,
                        range = TextRange.from(element.textRange.startOffset + HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length, propertyKey.length)
                    )

                    highlight(
                        ImpexHighlighterColors.MACRO_CONFIG_PREFIX,
                        holder,
                        element,
                        range = TextRange.from(element.textRange.startOffset, HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length)
                    )
                } else if (element.text.startsWith("$")) {
                    val textLength = element.parent.reference
                        ?.resolve()
                        ?.textLength
                        ?: element.textLength
                    highlight(
                        ImpexHighlighterColors.MACRO_USAGE_DEC,
                        holder,
                        element,
                        range = TextRange.from(element.textRange.startOffset, textLength)
                    )
                }
            }
        }
    }
}
