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

package com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.lookup

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.codeInsight.completion.AutoPopupInsertHandler
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.*
import com.intellij.psi.xml.XmlTag

object CngLookupElementFactory {

    fun build(meta: CngMetaWidgetDefinition, lookupString: String) = LookupElementBuilder.create(lookupString)
        .withTailText(meta.name?.let { name -> " $name" }, true)
        .withIcon(HybrisIcons.COCKPIT_NG_WIDGET_DEFINITION)

    fun build(meta: CngMetaEditorDefinition, lookupString: String) = LookupElementBuilder.create(lookupString)
        .withTailText(meta.name?.let { name -> " $name" }, true)
        .withIcon(HybrisIcons.COCKPIT_NG_EDITOR_DEFINITION)

    fun build(meta: CngMetaActionDefinition) = LookupElementBuilder.create(meta.id)
        .withTailText(meta.name?.let { name -> " $name" }, true)
        .withIcon(HybrisIcons.COCKPIT_NG_ACTION_DEFINITION)

    fun build(meta: CngMetaWidgetSetting) = LookupElementBuilder.create(meta.id)
        .withTypeText(meta.defaultValue?.let { defaultValue -> " $defaultValue" }, true)
        .withTypeText(meta.type)

    fun build(meta: CngMetaWidget) = LookupElementBuilder.create(meta.id)
        .withTailText(meta.name?.let { name -> " $name" }, true)
        .withIcon(HybrisIcons.COCKPIT_NG_WIDGET)

    fun buildInitializeProperty(tag: XmlTag) = tag.getAttributeValue("property")
        ?.let { LookupElementBuilder.create(it) }
        ?.withTypeText(
            tag.getAttributeValue("type")
                ?.substringAfterLast(".")
                ?: tag.getAttributeValue("template-bean"),
            true
        )
        ?.withIcon(HybrisIcons.COCKPIT_NG_INITIALIZE_PROPERTY)

    fun buildInitializeProperty(property: String) = LookupElementBuilder.create(property)
        .withIcon(HybrisIcons.COCKPIT_NG_INITIALIZE_PROPERTY)

    fun buildWrappingType(lookupString: String, presentableText: String, tailText: String? = null) = PrioritizedLookupElement.withGrouping(
        PrioritizedLookupElement.withPriority(
            LookupElementBuilder.create(lookupString)
                .withPresentableText(presentableText)
                .withTailText(tailText?.let { " ($it)" }, true)
                .withTypeText(lookupString, true)
                .withIcon(HybrisIcons.TYPE_OBJECT)
                .withInsertHandler(
                    object : AutoPopupInsertHandler() {
                        override fun handle(context: InsertionContext, item: LookupElement) {
                            val cursorOffset = context.editor.caretModel.offset
                            context.editor.caretModel.moveToOffset(cursorOffset - 1)
                        }
                    }
                ), 2.0
        ), 1)
}