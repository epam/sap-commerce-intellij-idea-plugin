/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.type.codeInsight.lookup

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.codeInsight.completion.AutoPopupInsertHandler
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.AtomicType
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.idea.plugin.hybris.system.type.model.deployment.TypeMapping

object TSLookupElementFactory {

    const val PRIORITY_2_0 = 2.0
    const val PRIORITY_1_0 = 1.0
    const val GROUP_1 = 1
    const val GROUP_2 = 2
    const val GROUP_3 = 3

    fun build(
        meta: TSGlobalMetaItem,
        suffix: String = ""
    ) = meta.name
        ?.let {
            LookupElementBuilder.create(it + suffix)
                .withPresentableText(it)
                .withTailText(if (meta.isAbstract) " (" + message("hybris.ts.type.abstract") + ")" else "", true)
                .withIcon(HybrisIcons.TypeSystem.ITEM)
                .withTypeText(":: ${meta.extendedMetaItemName ?: HybrisConstants.TS_TYPE_GENERIC_ITEM}", HybrisIcons.TypeSystem.ITEM, true)
                .withTypeIconRightAligned(true)
                .withCaseSensitivity(false)
        }

    fun build(meta: TSGlobalMetaRelation) = meta.name
        ?.let {
            LookupElementBuilder.create(it)
                .withTypeText(meta.flattenType)
                .withIcon(HybrisIcons.TypeSystem.Types.RELATION)
                .withCaseSensitivity(false)
        }

    fun build(meta: TSGlobalMetaCollection) = meta.name
        ?.let {
            LookupElementBuilder.create(it)
                .withTypeText(meta.flattenType)
                .withIcon(HybrisIcons.TypeSystem.Types.COLLECTION)
                .withCaseSensitivity(false)
        }

    fun build(meta: TSGlobalMetaMap) = meta.name
        ?.let {
            LookupElementBuilder.create(it)
                .withTypeText(meta.flattenType)
                .withIcon(HybrisIcons.TypeSystem.Types.MAP)
                .withCaseSensitivity(false)
        }

    fun build(meta: TSGlobalMetaItem.TSGlobalMetaItemAttribute) = LookupElementBuilder.create(meta.name)
        .withStrikeoutness(meta.isDeprecated)
        .withTypeText(
            meta.flattenType,
            if (meta.isLocalized) HybrisIcons.TypeSystem.LOCALIZED
            else null,
            true
        )
        .withTypeIconRightAligned(true)
        .withIcon(HybrisIcons.TypeSystem.ATTRIBUTE)
        .withCaseSensitivity(false)

    fun build(meta: TSMetaRelation.TSMetaOrderingAttribute) = LookupElementBuilder.create(meta.name)
        .withTypeText(meta.flattenType, true)
        .withTailText(" (1-to-m ordering attribute)")
        .withIcon(HybrisIcons.TypeSystem.ORDERING_ATTRIBUTE)
        .withCaseSensitivity(false)

    fun build(meta: TSMetaRelation.TSMetaRelationElement) = meta.qualifier
        ?.let {
            LookupElementBuilder.create(it)
                .withStrikeoutness(meta.isDeprecated)
                .withTypeText(meta.flattenType)
                .withIcon(
                    when (meta.end) {
                        TSMetaRelation.RelationEnd.SOURCE -> HybrisIcons.TypeSystem.RELATION_SOURCE
                        TSMetaRelation.RelationEnd.TARGET -> HybrisIcons.TypeSystem.RELATION_TARGET
                    }
                )
                .withCaseSensitivity(false)
        }

    fun build(relationElement: TSMetaRelation.TSMetaRelationElement, lookupString: String) = LookupElementBuilder.create(lookupString)
        .withIcon(
            when (relationElement.end) {
                TSMetaRelation.RelationEnd.SOURCE -> HybrisIcons.TypeSystem.RELATION_SOURCE
                TSMetaRelation.RelationEnd.TARGET -> HybrisIcons.TypeSystem.RELATION_TARGET
            }
        )
        .withStrikeoutness(relationElement.isDeprecated)
        .withTypeText(relationElement.flattenType, true)
        .withCaseSensitivity(false)

    fun build(meta: TSGlobalMetaEnum, lookupString: String?) = lookupString
        ?.let {
            LookupElementBuilder.create(it)
                .withTailText(if (meta.isDynamic) " (" + message("hybris.ts.type.dynamic") + ")" else "", true)
                .withIcon(HybrisIcons.TypeSystem.Types.ENUM)
                .withTypeText(":: ${HybrisConstants.TS_TYPE_ENUMERATION_VALUE}", HybrisIcons.TypeSystem.ITEM, true)
                .withTypeIconRightAligned(true)
                .withCaseSensitivity(false)
        }

    fun build(attribute: TSGlobalMetaItem.TSGlobalMetaItemAttribute, name: String) = LookupElementBuilder.create(name.trim { it <= ' ' })
        .withIcon(HybrisIcons.TypeSystem.ATTRIBUTE)
        .withTailText(if (attribute.isDynamic) " (" + message("hybris.ts.type.dynamic") + ')' else "", true)
        .withStrikeoutness(attribute.isDeprecated)
        .withTypeText(
            attribute.flattenType,
            if (attribute.isLocalized) HybrisIcons.TypeSystem.LOCALIZED
            else null,
            true
        )
        .withTypeIconRightAligned(true)
        .withCaseSensitivity(false)

    fun build(type: String?, lookupString: String) = LookupElementBuilder.create(lookupString)
        .withTypeText(type, true)
        .withIcon(HybrisIcons.TypeSystem.Types.MAP)
        .withCaseSensitivity(false)

    fun build(dom: EnumType) = dom.code.stringValue
        ?.let {
            LookupElementBuilder.create(it)
                .withTailText(if (dom.dynamic.value) " (" + message("hybris.ts.type.dynamic") + ")" else "", true)
                .withIcon(HybrisIcons.TypeSystem.Types.ENUM)
        }

    fun build(dom: AtomicType) = dom.clazz.stringValue
        ?.let {
            LookupElementBuilder.create(it)
                .withIcon(HybrisIcons.TypeSystem.Types.ATOMIC)
        }

    fun build(dom: ItemType) = dom.code.stringValue
        ?.let {
            LookupElementBuilder.create(it)
                .withIcon(HybrisIcons.TypeSystem.ITEM)
        }

    fun buildPrimitive(dom: TypeMapping?) = dom
        ?.let {
            val type = it.type.stringValue ?: return@let null
            val persistenceType = it.persistenceType.stringValue ?: return@let null

            LookupElementBuilder.create(type)
                .withTypeText(persistenceType, true)
                .withIcon(HybrisIcons.TypeSystem.Types.PRIMITIVE)
        }
        ?.let { PrioritizedLookupElement.withGrouping(it, GROUP_3) }

    fun buildCustomProperty(lookupString: String) = LookupElementBuilder.create(lookupString)
        .withIcon(HybrisIcons.TypeSystem.CUSTOM_PROPERTY)
        .withCaseSensitivity(false)

    fun buildHeaderAbbreviation(lookupString: String) = LookupElementBuilder.create(lookupString)
        .withTypeText("Header Abbreviation", true)
        .withIcon(HybrisIcons.TypeSystem.HEADER_ABBREVIATION)
        .withInsertHandler(lookupString.contains('@')
            .takeIf { it }
            ?.let {
                object : AutoPopupInsertHandler() {
                    override fun handle(context: InsertionContext, item: LookupElement) {
                        lookupString.indexOf('@')
                            .takeIf { it >= 0 }
                            ?.let { index ->
                                val cursorOffset = context.editor.caretModel.offset
                                val moveBackTo = lookupString.length - index - 1
                                val offset = cursorOffset - moveBackTo
                                context.editor.caretModel.moveToOffset(offset)
                                context.editor.selectionModel.setSelection(offset, offset + moveBackTo)
                            }
                    }
                }
            }
        )
        .let { PrioritizedLookupElement.withPriority(it, 2.0) }
}