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
package sap.commerce.toolset.impex.lang.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.highlighting.ImpExHighlighterColors
import sap.commerce.toolset.impex.highlighting.ImpExSyntaxHighlighter
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.psi.references.*
import sap.commerce.toolset.lang.annotation.AbstractAnnotator
import sap.commerce.toolset.spring.psi.reference.SpringReference

class ImpExAnnotator : AbstractAnnotator() {

    override val highlighter: SyntaxHighlighter
        get() = ImpExSyntaxHighlighter.getInstance()

    private val tsElementTypes = setOf(ImpExTypes.TYPE, ImpExTypes.TARGET)
    private val userRightsParameters = mapOf(
        ImpExTypes.TYPE to 0,
        ImpExTypes.UID to 1,
        ImpExTypes.MEMBEROFGROUPS to 2,
        ImpExTypes.PASSWORD to 3,
        ImpExTypes.TARGET to 4
    )
    private val userRightsParameterNames = mapOf(
        0 to "Type",
        1 to "UID",
        2 to "MemberOfGroups",
        3 to "Password",
        4 to "Target"
    )

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            ImpExTypes.BEANSHELL_SCRIPT_BODY_LINE,
            ImpExTypes.JAVASCRIPT_SCRIPT_BODY_LINE,
            ImpExTypes.BEANSHELL_SCRIPT_BODY_MULTILINE,
            ImpExTypes.JAVASCRIPT_SCRIPT_BODY_MULTILINE -> {
                highlight(ImpExHighlighterColors.SCRIPT_BODY, holder, element)
            }

            ImpExTypes.USER_RIGHTS_HEADER_PARAMETER -> {
                val headerParameter = element as? ImpExUserRightsHeaderParameter ?: return
                val elementType = headerParameter.firstChild.elementType ?: return
                val noPasswordColumn = headerParameter.headerLine
                    ?.userRightsHeaderParameterList
                    ?.none { it.firstChild.elementType == ImpExTypes.PASSWORD }
                    ?.takeIf { it }
                    ?.takeIf { elementType == ImpExTypes.TARGET }
                    ?.let { 1 }
                    ?: 0
                val actualColumnNumber = headerParameter.columnNumber ?: return

                when (elementType) {
                    ImpExTypes.PERMISSION -> {
                        if (actualColumnNumber >= userRightsParameters.size - noPasswordColumn) return
                        val expectedColumnName = userRightsParameterNames[actualColumnNumber] ?: return

                        highlightError(
                            holder, element, i18n(
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
                            holder, element, i18n(
                                "hybris.inspections.impex.userRights.header.mandatory.order",
                                headerParameter.text,
                                expectedColumnNumber + 1 - noPasswordColumn,
                            )
                        )
                    }
                }
            }

            ImpExTypes.USER_RIGHTS_SINGLE_VALUE -> {
                val value = element as? ImpExUserRightsValue ?: return
                val headerParameter = value.headerParameter ?: return
                if (!tsElementTypes.contains(headerParameter.firstChild.elementType)) return

                highlight(ImpExTypes.HEADER_TYPE, holder, element)
            }

            ImpExTypes.VALUE -> {
                val value = element as? ImpExValue ?: return

                value.references.forEach { reference ->
                    when (reference) {
                        is ImpExValueTSStaticEnumReference,
                        is ImpExValueTSClassifierReference,
                        is ImpExDocumentIdUsageReference,
                        is SpringReference -> holder.highlightReference(reference, ImpExHighlighterColors.VALUE_REFERENCE)
                    }
                }
            }

            ImpExTypes.USER_RIGHTS_ATTRIBUTE_VALUE -> {
                val value = element as? ImpExUserRightsValue ?: return
                val headerParameter = value.headerParameter ?: return
                if (!tsElementTypes.contains(headerParameter.firstChild.elementType)) return

                highlight(ImpExTypes.HEADER_PARAMETER_NAME, holder, element)
            }

            ImpExTypes.DOT -> {
                if (element.parent.elementType == ImpExTypes.USER_RIGHTS_PERMISSION_VALUE) {
                    highlight(ImpExHighlighterColors.USER_RIGHTS_PERMISSION_INHERITED, holder, element)
                }
            }

            ImpExTypes.VALUE_SUBTYPE -> {
                val subType = element.parent as? ImpExSubTypeName ?: return
                val headerType = subType.headerTypeName ?: return

                if (subType.textMatches(headerType)) {
                    highlight(ImpExHighlighterColors.VALUE_SUBTYPE_SAME, holder, element)
                }
            }

            ImpExTypes.MACRO_USAGE -> {
                if (element.text.startsWith(ImpExConstants.MACRO_CONFIG_COMPLETE_MARKER)) {
                    val macroUsageDec = element.parent as? ImpExMacroUsageDec ?: return

                    val propertyKey = macroUsageDec.configPropertyKey
                        ?: element.text.replace(ImpExConstants.MACRO_CONFIG_COMPLETE_MARKER, "")

                    // TODO: replace this with the correct Lexer and Parse, introduce new TOKEN MACRO_CONFIG_PREFIX
                    highlight(
                        ImpExHighlighterColors.MACRO_CONFIG_KEY,
                        holder,
                        element,
                        range = TextRange.from(element.textRange.startOffset + ImpExConstants.MACRO_CONFIG_COMPLETE_MARKER.length, propertyKey.length)
                    )

                    highlight(
                        ImpExHighlighterColors.MACRO_CONFIG_PREFIX,
                        holder,
                        element,
                        range = TextRange.from(element.textRange.startOffset, ImpExConstants.MACRO_CONFIG_COMPLETE_MARKER.length)
                    )
                } else if (element.text.startsWith("$")) {
                    val textLength = element.parent.reference
                        ?.resolve()
                        ?.text
                        ?.let { ImpExMacroReference.escapeName(it) }
                        ?.length
                        ?: element.textLength
                    highlight(
                        ImpExHighlighterColors.MACRO_USAGE_DEC,
                        holder,
                        element,
                        range = TextRange.from(element.textRange.startOffset, textLength)
                    )
                }
            }

            ImpExTypes.HEADER_PARAMETER_NAME -> {
                if (element.parent.reference is ImpExHeaderAbbreviationReference) {
                    highlight(ImpExHighlighterColors.ATTRIBUTE_HEADER_ABBREVIATION, holder, element)
                } else {
                    element.parentOfType<ImpExFullHeaderParameter>()
                        ?.getAttribute(AttributeModifier.UNIQUE)
                        ?.anyAttributeValue
                        ?.takeIf { it.textMatches("true") }
                        ?.let {
                            highlight(
                                ImpExHighlighterColors.HEADER_UNIQUE_PARAMETER_NAME,
                                holder,
                                element
                            )
                        }
                }
            }

            ImpExTypes.PARAMETER -> {
                if (element.parent.reference is ImpExHeaderAbbreviationReference) {
                    highlight(ImpExHighlighterColors.ATTRIBUTE_HEADER_ABBREVIATION, holder, element)
                }
            }
        }
    }
}
