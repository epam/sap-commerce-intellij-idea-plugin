/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.bean.codeInsight.lookup

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaProperty
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import javax.swing.Icon

object BSLookupElementFactory {

    fun build(meta: BSGlobalMetaEnum) = meta.name
        ?.let { LookupElementBuilder.create(it) }
        ?.withPresentableText(BSMetaHelper.getShortName(meta.name) ?: "?")
        ?.withTailText(if (meta.isDeprecated) " deprecated" else null)
        ?.withTypeText(meta.name, true)
        ?.withIcon(HybrisIcons.BS_ENUM)
        ?.withCaseSensitivity(true)

    fun build(meta: BSGlobalMetaBean, metaType: BSMetaType) = meta.fullName
        ?.let { LookupElementBuilder.create(BSMetaHelper.getEscapedName(it)) }
        ?.withPresentableText(meta.flattenType ?: "?")
        ?.withTailText(
            listOfNotNull(
                if (meta.isAbstract) "abstract" else null,
                if (meta.isDeprecated) "deprecated" else null
            ).joinToString(",", " ")
        )
        ?.withTypeText(meta.name, true)
        ?.withIcon(
            when (metaType) {
                BSMetaType.META_BEAN -> HybrisIcons.BS_BEAN
                BSMetaType.META_WS_BEAN -> HybrisIcons.BS_WS_BEAN
                BSMetaType.META_EVENT -> HybrisIcons.BS_EVENT_BEAN
                else -> HybrisIcons.BS_BEAN
            }
        )
        ?.withCaseSensitivity(true)

    fun build(meta: BSMetaProperty) = meta.name
        ?.let { LookupElementBuilder.create(it) }
        ?.withTypeText(meta.flattenType, true)
        ?.withIcon(HybrisIcons.BS_PROPERTY)
        ?.withCaseSensitivity(false)

    fun buildLevelMapping(levelMapping: String) = PrioritizedLookupElement.withPriority(
        LookupElementBuilder.create(levelMapping)
            .withTypeText("Level Mapping", true)
            .withIcon(HybrisIcons.BS_LEVEL_MAPPING)
            .withCaseSensitivity(false), 1.0
    )

    fun build(bean: Bean): LookupElement? {
        val clazz = bean.clazz.stringValue ?: return null
        val tail = listOfNotNull(
            if (bean.abstract.value) "abstract" else null,
            if (bean.deprecated.value) "deprecated" else null
        ).joinToString(",", " ")
        val lookupElement = LookupElementBuilder.create(BSMetaHelper.getEscapedName(clazz))
            .withPresentableText(clazz.substringAfterLast("."))
            .withTailText(tail, true)
            .withTypeText(clazz, true)
            .withIcon(HybrisIcons.BS_BEAN)
        return if (bean.abstract.value) {
            PrioritizedLookupElement.withGrouping(
                PrioritizedLookupElement.withPriority(lookupElement, 1.0),
                1
            )
        } else {
            PrioritizedLookupElement.withGrouping(
                PrioritizedLookupElement.withPriority(lookupElement, 2.0),
                2
            )
        }
    }

    fun build(enum: Enum): LookupElement? {
        val clazz = enum.clazz.stringValue ?: return null
        val lookupElement = LookupElementBuilder.create(clazz)
            .withPresentableText(clazz.substringAfterLast("."))
            .withTailText(if (enum.deprecated.value) " deprecated" else null, true)
            .withTypeText(clazz, true)
            .withIcon(HybrisIcons.BS_ENUM)
        return if (enum.deprecated.value) {
            PrioritizedLookupElement.withGrouping(
                PrioritizedLookupElement.withPriority(lookupElement, 1.0),
                1
            )
        } else {
            PrioritizedLookupElement.withGrouping(
                PrioritizedLookupElement.withPriority(lookupElement, 2.0),
                2
            )
        }
    }

    fun buildWsHint(hint: String) = LookupElementBuilder.create(hint)
        .withIcon(HybrisIcons.BS_WS_HINT)
        .withTypeText("WS Hint", true)

    fun buildPropertyType(lookupString: String, priority: Double, group: Int, icon: Icon, typeText: String) = PrioritizedLookupElement.withGrouping(
        PrioritizedLookupElement.withPriority(
            LookupElementBuilder.create(lookupString)
                .withIcon(icon)
                .withTypeText(typeText, true), priority
        ), group
    )
}