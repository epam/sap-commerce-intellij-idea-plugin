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
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.highlighting.DefaultImpexSyntaxHighlighter
import com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.idea.plugin.hybris.lang.annotation.AbstractAnnotator
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class ImpexAnnotator : AbstractAnnotator(DefaultImpexSyntaxHighlighter.instance) {

    private val tsElementTypes = setOf(ImpexTypes.TYPE, ImpexTypes.TARGET)
    private val userRightsParameters = mapOf(
        ImpexTypes.TYPE to 0,
        ImpexTypes.UID to 1,
        ImpexTypes.MEMBEROFGROUPS to 2,
        ImpexTypes.PASSWORD to 3,
        ImpexTypes.TARGET to 4
    )
    private val userRightsParametersNames = mapOf(
        0 to "Type",
        1 to "UID",
        2 to "MemberOfGroups",
        3 to "Password",
        4 to "Target"
    )

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            ImpexTypes.USER_RIGHTS_HEADER_PARAMETER -> {
                val headerParameter = element as? ImpexUserRightsHeaderParameter ?: return
                val elementType = headerParameter.firstChild.elementType ?: return
                val noPasswordColumn = headerParameter.headerLine
                    ?.userRightsHeaderParameterList
                    ?.none { it.firstChild.elementType == ImpexTypes.PASSWORD }
                    ?.takeIf { it }
                    ?.takeIf { elementType == ImpexTypes.TARGET }
                    ?.let { 1 }
                    ?: 0
                val actualColumnNumber = headerParameter.columnNumber ?: return

                when (elementType) {
                    ImpexTypes.PERMISSION -> {
                        if (actualColumnNumber >= userRightsParameters.size - noPasswordColumn) return
                        val expectedColumnName = userRightsParametersNames[actualColumnNumber] ?: return

                        highlightError(
                            holder, element, message(
                                "hybris.inspections.impex.userRights.header.mandatory.expected",
                                expectedColumnName,
                                actualColumnNumber + 1 - noPasswordColumn,
                                headerParameter.text,
                            )
                        )
                    }

                    else -> {
                        val expectedColumnNumber = userRightsParameters[elementType] ?: return

                        if (actualColumnNumber == expectedColumnNumber - noPasswordColumn) return
                        highlightError(
                            holder, element, message(
                                "hybris.inspections.impex.userRights.header.mandatory.order",
                                headerParameter.text,
                                expectedColumnNumber + 1 - noPasswordColumn,
                            )
                        )
                    }
                }
            }

            ImpexTypes.USER_RIGHTS_SINGLE_VALUE -> {
                val value = element as? ImpexUserRightsValue ?: return
                val headerParameter = value.headerParameter ?: return
                if (!tsElementTypes.contains(headerParameter.firstChild.elementType)) return

                highlightReference(
                    ImpexTypes.HEADER_TYPE, holder, element,
                    "hybris.inspections.impex.unresolved.type.key",
                    referenceHolder = element
                )
            }

            ImpexTypes.USER_RIGHTS_ATTRIBUTE_VALUE -> {
                val value = element as? ImpexUserRightsValue ?: return
                val headerParameter = value.headerParameter ?: return
                if (!tsElementTypes.contains(headerParameter.firstChild.elementType)) return

                highlightReference(
                    ImpexTypes.HEADER_PARAMETER_NAME, holder, element,
                    "hybris.inspections.impex.unresolved.type.key",
                    referenceHolder = element
                )
            }

            ImpexTypes.DOT -> {
                if (element.parent.elementType == ImpexTypes.USER_RIGHTS_PERMISSION_VALUE) {
                    highlight(ImpexHighlighterColors.USER_RIGHTS_PERMISSION_INHERITED, holder, element)
                }
            }

            ImpexTypes.VALUE_SUBTYPE -> {
                val subType = element.parent as? ImpexSubTypeName ?: return
                val headerType = subType.headerTypeName ?: return

                if (subType.textMatches(headerType)) {
                    highlight(ImpexHighlighterColors.VALUE_SUBTYPE_SAME, holder, element)
                } else {
                    highlightReference(ImpexTypes.VALUE_SUBTYPE, holder, element, "hybris.inspections.impex.unresolved.subType.key")
                }
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
