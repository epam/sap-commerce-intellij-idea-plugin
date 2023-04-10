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

package com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.lookup

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.*

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

}