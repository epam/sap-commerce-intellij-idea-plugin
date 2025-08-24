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

@file:JvmName("ImpExPsiUtil")

package sap.commerce.toolset.impex.psi

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.typeSystem.psi.reference.result.*

fun getHeaderLine(element: ImpExFullHeaderParameter): ImpExHeaderLine? = PsiTreeUtil
    .getParentOfType(element, ImpExHeaderLine::class.java)

fun getValueGroup(element: ImpExString): ImpExValueGroup? = PsiTreeUtil
    .getParentOfType(element, ImpExValueGroup::class.java)

fun getValueGroup(element: ImpExValue): ImpExValueGroup? = PsiTreeUtil
    .getParentOfType(element, ImpExValueGroup::class.java)

fun getValueGroup(element: ImpExValueLine, columnNumber: Int): ImpExValueGroup? = element
    .childrenOfType<ImpExValueGroup>()
    .getOrNull(columnNumber)

fun getAnyAttributeName(element: ImpExAnyAttributeValue): ImpExAnyAttributeName? = PsiTreeUtil
    .getPrevSiblingOfType(element, ImpExAnyAttributeName::class.java)

fun getAnyAttributeValue(element: ImpExAnyAttributeName): ImpExAnyAttributeValue? = PsiTreeUtil
    .getNextSiblingOfType(element, ImpExAnyAttributeValue::class.java)

fun getUniqueFullHeaderParameters(element: ImpExHeaderLine) = element.fullHeaderParameterList
    .filter { it.getAttribute(AttributeModifier.UNIQUE)?.anyAttributeValue?.textMatches("true") ?: false }

fun getTableRange(element: ImpExHeaderLine): TextRange {
    val tableElements = ArrayDeque<PsiElement>()
    var next = element.nextSibling

    while (next != null) {
        if (next is ImpExHeaderLine || next is ImpExUserRights) {

            // once all lines processed, we have to go back till last value line
            var lastElement = tableElements.lastOrNull()
            while (lastElement != null && lastElement !is ImpExValueLine) {
                tableElements.removeLastOrNull()
                lastElement = tableElements.lastOrNull()
            }

            next = null
        } else {
            tableElements.add(next)
            next = next.nextSibling
        }
    }

    val endOffset = tableElements.lastOrNull()
        ?.endOffset
        ?: element.endOffset

    return TextRange.create(element.startOffset, endOffset)
}

fun addValueGroups(element: ImpExValueLine, groupsToAdd: Int) {
    if (groupsToAdd <= 0) return

    repeat(groupsToAdd) {
        ImpExElementFactory.createValueGroup(element.project)
            ?.let { element.addAfter(it, element.valueGroupList.lastOrNull()) }
    }
}

fun getAttribute(element: ImpExFullHeaderParameter, attributeModifier: AttributeModifier): ImpExAttribute? = element
    .modifiersList
    .flatMap { it.attributeList }
    .find { it.anyAttributeName.textMatches(attributeModifier.modifierName) }

fun getHeaderTypeName(element: ImpExSubTypeName): ImpExHeaderTypeName? = element
    .valueLine
    ?.headerLine
    ?.fullHeaderType
    ?.headerTypeName

fun getConfigPropertyKey(element: ImpExMacroUsageDec): String? {
    if (!element.text.startsWith(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) return null

    val project = element.project
    val propertyKey = element.text.replace(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "")

    if (propertyKey.isBlank()) return null

    return if (DumbService.isDumb(project)) {
        element.text.replace(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "")
    } else PropertyService.getInstance(project)
        .findMacroProperty(propertyKey)
        ?.key
        ?: element.text.replace(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "")
}

fun getInlineTypeName(element: ImpExParameter): String? = element.text
//    .replace(CompletionUtilCore.DUMMY_IDENTIFIER, "")
    .substringBefore("(")
    .substringBefore("[")
    .trim()
    .indexOf('.')
    .takeIf { it >= 0 }
    ?.let { element.text.substring(0, it).trim() }

fun getAttributeName(element: ImpExParameter): String = element.text
//    .replace(CompletionUtilCore.DUMMY_IDENTIFIER, "")
    .substringBefore("(")
    .substringBefore("[")
    .substringAfter(".")
    .trim()

/**
 * 1. Try to get inline `MyType` type: referenceAttr(MyType.attr)
 * 2. If not present fallback to a type of the `referenceAttr`: referenceAttr(attr)
 */
fun getItemTypeName(element: ImpExParameter): String? = element
    .inlineTypeName
    ?: element.referenceItemTypeName

fun getReferenceName(element: ImpExParameter): String? = (PsiTreeUtil
    .getParentOfType(element, ImpExParameter::class.java)
    ?: PsiTreeUtil.getParentOfType(element, ImpExFullHeaderParameter::class.java)
        ?.anyHeaderParameterName)
    ?.text

fun getReferenceItemTypeName(element: ImpExParameter): String? = (
    PsiTreeUtil
        .getParentOfType(element, ImpExParameter::class.java)
        ?: PsiTreeUtil.getParentOfType(element, ImpExFullHeaderParameter::class.java)
            ?.anyHeaderParameterName
    )
    ?.reference
    ?.asSafely<PsiPolyVariantReference>()
    ?.multiResolve(false)
    ?.firstOrNull()
    ?.let {
        when (it) {
            is AttributeResolveResult -> it.meta.type
            is EnumResolveResult -> it.meta.name
            is ItemResolveResult -> it.meta.name
            is RelationResolveResult -> it.meta.name
            is RelationEndResolveResult -> it.meta.type
            else -> null
        }
    }

fun getHeaderItemTypeName(element: ImpExAnyHeaderParameterName): ImpExHeaderTypeName? = PsiTreeUtil
    .getParentOfType(element, ImpExHeaderLine::class.java)
    ?.fullHeaderType
    ?.headerTypeName

// ------------------------------------------
//              User Rights
// ------------------------------------------
fun getValueGroup(element: ImpExUserRightsValueLine, index: Int): ImpExUserRightsValueGroup? = element
    .userRightsValueGroupList
    .getOrNull(index)

fun getHeaderParameter(element: ImpExUserRightsHeaderLine, index: Int): ImpExUserRightsHeaderParameter? = element
    .userRightsHeaderParameterList
    .getOrNull(index)

fun getHeaderLine(element: ImpExUserRightsValueLine): ImpExUserRightsHeaderLine? = PsiTreeUtil
    .getPrevSiblingOfType(element, ImpExUserRightsHeaderLine::class.java)

fun getValueLine(element: ImpExUserRightsValueGroup): ImpExUserRightsValueLine? = element
    .parentOfType<ImpExUserRightsValueLine>()

fun getValueLine(element: ImpExUserRightsValue): ImpExUserRightsValueLine? = element
    .parentOfType<ImpExUserRightsValueLine>()

fun getColumnNumber(element: ImpExUserRightsValueGroup): Int? = element
    .valueLine
    ?.let { valueLine ->
        valueLine.userRightsValueGroupList.indexOf(element)
            .takeIf { it != -1 }
            ?.let {
                // we always have to plus one column, because a first value group is not part of the list
                it + 1
            }
    }

fun getHeaderParameter(element: ImpExUserRightsValueGroup): ImpExUserRightsHeaderParameter? = element
    .columnNumber
    ?.let {
        getValueLine(element)
            ?.headerLine
            ?.getHeaderParameter(it)
    }

fun getHeaderParameter(element: ImpExUserRightsValue): ImpExUserRightsHeaderParameter? = when (val parent = element.parent) {
    is ImpExUserRightsFirstValueGroup -> {
        getValueLine(element)
            ?.headerLine
            ?.getHeaderParameter(0)
    }

    is ImpExUserRightsValueGroup -> {
        parent
            .columnNumber
            ?.let {
                getValueLine(element)
                    ?.headerLine
                    ?.getHeaderParameter(it)
            }
    }

    else -> null
}
