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

package com.intellij.idea.plugin.hybris.system.type.codeInsight.lookup

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.AtomicType
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.ItemType

object TSLookupElementFactory {

    fun build(meta: TSGlobalMetaItem) = meta.name
        ?.let {
            LookupElementBuilder.create(it)
                .withTailText(if (meta.isAbstract) " (" + message("hybris.ts.type.abstract") + ")" else "", true)
                .withIcon(HybrisIcons.TS_ITEM)
        }

    fun build(meta: TSGlobalMetaRelation) = meta.name?.let {
        LookupElementBuilder.create(it)
            .withTypeText(meta.flattenType)
            .withIcon(HybrisIcons.TS_RELATION)
    }

    fun build(meta: TSGlobalMetaCollection) = meta.name?.let {
        LookupElementBuilder.create(it)
            .withTypeText(meta.flattenType)
            .withIcon(HybrisIcons.TS_COLLECTION)
    }

    fun build(meta: TSGlobalMetaMap) = meta.name?.let {
        LookupElementBuilder.create(it)
            .withTypeText(meta.flattenType)
            .withIcon(HybrisIcons.TS_MAP)
    }

    fun build(meta: TSGlobalMetaItem.TSGlobalMetaItemAttribute) = LookupElementBuilder.create(meta.name)
        .withStrikeoutness(meta.isDeprecated)
        .withTypeText(meta.flattenType, true)
        .withIcon(HybrisIcons.TS_ATTRIBUTE)
        .withCaseSensitivity(true)

    fun build(meta: TSMetaRelation.TSMetaRelationElement) = meta.qualifier
        ?.let {
            LookupElementBuilder.create(it)
                .withStrikeoutness(meta.isDeprecated)
                .withTypeText(meta.flattenType)
                .withIcon(
                    when (meta.end) {
                        TSMetaRelation.RelationEnd.SOURCE -> HybrisIcons.TS_RELATION_SOURCE
                        TSMetaRelation.RelationEnd.TARGET -> HybrisIcons.TS_RELATION_TARGET
                    }
                )
        }

    fun build(relationElement: TSMetaRelation.TSMetaRelationElement, lookupString: String) = LookupElementBuilder.create(lookupString)
        .withIcon(when (relationElement.end) {
            TSMetaRelation.RelationEnd.SOURCE -> HybrisIcons.TS_RELATION_SOURCE
            TSMetaRelation.RelationEnd.TARGET -> HybrisIcons.TS_RELATION_TARGET
        })
        .withStrikeoutness(relationElement.isDeprecated)
        .withTypeText(relationElement.flattenType, true)
        .withCaseSensitivity(true)

    fun build(meta: TSGlobalMetaEnum, lookupString: String?) = lookupString
        ?.let {
            LookupElementBuilder.create(it)
                .withTailText(if (meta.isDynamic) " (" + message("hybris.ts.type.dynamic") + ")" else "", true)
                .withIcon(HybrisIcons.TS_ENUM)
                .withCaseSensitivity(true)
        }

    fun build(attribute: TSGlobalMetaItem.TSGlobalMetaItemAttribute, name: String) = LookupElementBuilder.create(name.trim { it <= ' ' })
        .withIcon(HybrisIcons.TS_ATTRIBUTE)
        .withTailText(if (attribute.isDynamic) " (" + message("hybris.ts.type.dynamic") + ')' else "", true)
        .withStrikeoutness(attribute.isDeprecated)
        .withTypeText(attribute.flattenType, true)
        .withCaseSensitivity(true)

    fun build(map: TSGlobalMetaMap, type: String?, lookupString: String) = LookupElementBuilder.create(lookupString)
        .withTypeText(type, true)
        .withIcon(HybrisIcons.TS_MAP)
        .withCaseSensitivity(true)

    fun build(dom: EnumType) = dom.code.stringValue
        ?.let {
            LookupElementBuilder.create(it)
                .withTailText(if (dom.dynamic.value) " (" + message("hybris.ts.type.dynamic") + ")" else "", true)
                .withIcon(HybrisIcons.TS_ENUM)
        }

    fun build(dom: AtomicType) = dom.clazz.stringValue
        ?.let {
            LookupElementBuilder.create(it)
                .withIcon(HybrisIcons.TS_ATOMIC)
        }

    fun build(dom: ItemType) = dom.code.stringValue
        ?.let {
            LookupElementBuilder.create(it)
                .withIcon(HybrisIcons.TS_ITEM)
        }

    fun buildCustomProperty(lookupString: String) = LookupElementBuilder.create(lookupString)
        .withIcon(HybrisIcons.TS_CUSTOM_PROPERTY)
        .withCaseSensitivity(true)

}