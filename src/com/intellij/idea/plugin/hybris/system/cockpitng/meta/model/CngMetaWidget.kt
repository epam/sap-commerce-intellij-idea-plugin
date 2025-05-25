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
package com.intellij.idea.plugin.hybris.system.cockpitng.meta.model

import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.Widget

class CngMetaWidget(
    dom: Widget,
    fileName: String,
    val id: String,
    custom: Boolean,
    val widgets: Collection<CngMetaWidget> = emptyList(),
) : CngMeta<Widget>(dom, fileName, custom) {

    val name: String? = dom.title.stringValue
    val slotId: String? = dom.slotId.stringValue
    val widgetDefinitionId: String? = dom.widgetDefinitionId.stringValue
    val access: String? = dom.access.stringValue
    val lastFocusedChildIndex: String? = dom.lastFocusedChildIndex.stringValue
    val lastFocusedTemplateInstanceId: String? = dom.lastFocusedTemplateInstanceId.stringValue
    val template = dom.template.value ?: false

    override fun toString() = id
}