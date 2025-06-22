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

@file:JvmName("ImpexPsiUtil")

package com.intellij.idea.plugin.hybris.impex.psi

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.*
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.util.*

fun getHeaderLine(element: ImpexFullHeaderParameter): ImpexHeaderLine? = PsiTreeUtil
    .getParentOfType(element, ImpexHeaderLine::class.java)

fun getValueGroup(element: ImpexString): ImpexValueGroup? = PsiTreeUtil
    .getParentOfType(element, ImpexValueGroup::class.java)

fun getValueGroup(element: ImpexValue): ImpexValueGroup? = PsiTreeUtil
    .getParentOfType(element, ImpexValueGroup::class.java)

fun getValueGroup(element: ImpexValueLine, columnNumber: Int): ImpexValueGroup? = element
    .childrenOfType<ImpexValueGroup>()
    .getOrNull(columnNumber)

fun getAnyAttributeName(element: ImpexAnyAttributeValue): ImpexAnyAttributeName? = PsiTreeUtil
    .getPrevSiblingOfType(element, ImpexAnyAttributeName::class.java)

fun getAnyAttributeValue(element: ImpexAnyAttributeName): ImpexAnyAttributeValue? = PsiTreeUtil
    .getNextSiblingOfType(element, ImpexAnyAttributeValue::class.java)

fun getUniqueFullHeaderParameters(element: ImpexHeaderLine) = element.fullHeaderParameterList
    .filter { it.getAttribute(AttributeModifier.UNIQUE)?.anyAttributeValue?.textMatches("true") ?: false }

fun getTableRange(element: ImpexHeaderLine): TextRange {
    val tableElements = ArrayDeque<PsiElement>()
    var next = element.nextSibling

    while (next != null) {
        if (next is ImpexHeaderLine || next is ImpexUserRights) {

            // once all lines processed, we have to go back till last value line
            var lastElement = tableElements.lastOrNull()
            while (lastElement != null && lastElement !is ImpexValueLine) {
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

fun addValueGroups(element: ImpexValueLine, groupsToAdd: Int) {
    if (groupsToAdd <= 0) return

    repeat(groupsToAdd) {
        ImpExElementFactory.createValueGroup(element.project)
            ?.let { element.addAfter(it, element.valueGroupList.lastOrNull()) }
    }
}

fun getAttribute(element: ImpexFullHeaderParameter, attributeModifier: AttributeModifier): ImpexAttribute? = element
    .modifiersList
    .flatMap { it.attributeList }
    .find { it.anyAttributeName.textMatches(attributeModifier.modifierName) }

fun getHeaderTypeName(element: ImpexSubTypeName): ImpexHeaderTypeName? = element
    .valueLine
    ?.headerLine
    ?.fullHeaderType
    ?.headerTypeName

fun getConfigPropertyKey(element: ImpexMacroUsageDec): String? {
    if (!element.text.startsWith(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) return null

    val project = element.project
    val propertyKey = element.text.replace(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "")

    if (propertyKey.isBlank()) return null

    return if (DumbService.isDumb(project)) {
        element.text.replace(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "")
    } else PropertyService.getInstance(project)
        ?.findMacroProperty(propertyKey)
        ?.key
        ?: element.text.replace(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "")
}

fun getInlineTypeName(element: ImpexParameter): String? = element.text
//    .replace(CompletionUtilCore.DUMMY_IDENTIFIER, "")
    .substringBefore("(")
    .substringBefore("[")
    .trim()
    .indexOf('.')
    .takeIf { it >= 0 }
    ?.let { element.text.substring(0, it).trim() }

fun getAttributeName(element: ImpexParameter): String = element.text
//    .replace(CompletionUtilCore.DUMMY_IDENTIFIER, "")
    .substringBefore("(")
    .substringBefore("[")
    .substringAfter(".")
    .trim()

/**
 * 1. Try to get inline `MyType` type: referenceAttr(MyType.attr)
 * 2. If not present fallback to a type of the `referenceAttr`: referenceAttr(attr)
 */
fun getItemTypeName(element: ImpexParameter): String? = element
    .inlineTypeName
    ?: element.referenceItemTypeName

fun getReferenceName(element: ImpexParameter): String? = (PsiTreeUtil
    .getParentOfType(element, ImpexParameter::class.java)
    ?: PsiTreeUtil.getParentOfType(element, ImpexFullHeaderParameter::class.java)
        ?.anyHeaderParameterName)
    ?.text

fun getReferenceItemTypeName(element: ImpexParameter): String? = (
    PsiTreeUtil
        .getParentOfType(element, ImpexParameter::class.java)
        ?: PsiTreeUtil.getParentOfType(element, ImpexFullHeaderParameter::class.java)
            ?.anyHeaderParameterName
    )
    ?.reference
    ?.let { it as PsiPolyVariantReference }
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

fun getHeaderItemTypeName(element: ImpexAnyHeaderParameterName): ImpexHeaderTypeName? = PsiTreeUtil
    .getParentOfType(element, ImpexHeaderLine::class.java)
    ?.fullHeaderType
    ?.headerTypeName

// ------------------------------------------
//              User Rights
// ------------------------------------------
fun getValueGroup(element: ImpexUserRightsValueLine, index: Int): ImpexUserRightsValueGroup? = element
    .userRightsValueGroupList
    .getOrNull(index)

fun getHeaderParameter(element: ImpexUserRightsHeaderLine, index: Int): ImpexUserRightsHeaderParameter? = element
    .userRightsHeaderParameterList
    .getOrNull(index)

fun getHeaderLine(element: ImpexUserRightsValueLine): ImpexUserRightsHeaderLine? = PsiTreeUtil
    .getPrevSiblingOfType(element, ImpexUserRightsHeaderLine::class.java)

fun getValueLine(element: ImpexUserRightsValueGroup): ImpexUserRightsValueLine? = element
    .parentOfType<ImpexUserRightsValueLine>()

fun getValueLine(element: ImpexUserRightsValue): ImpexUserRightsValueLine? = element
    .parentOfType<ImpexUserRightsValueLine>()

fun getColumnNumber(element: ImpexUserRightsValueGroup): Int? = element
    .valueLine
    ?.let { valueLine ->
        valueLine.userRightsValueGroupList.indexOf(element)
            .takeIf { it != -1 }
            ?.let {
                // we always have to plus one column, because a first value group is not part of the list
                it + 1
            }
    }

fun getHeaderParameter(element: ImpexUserRightsValueGroup): ImpexUserRightsHeaderParameter? = element
    .columnNumber
    ?.let {
        getValueLine(element)
            ?.headerLine
            ?.getHeaderParameter(it)
    }

fun getHeaderParameter(element: ImpexUserRightsValue): ImpexUserRightsHeaderParameter? = when (val parent = element.parent) {
    is ImpexUserRightsFirstValueGroup -> {
        getValueLine(element)
            ?.headerLine
            ?.getHeaderParameter(0)
    }

    is ImpexUserRightsValueGroup -> {
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
