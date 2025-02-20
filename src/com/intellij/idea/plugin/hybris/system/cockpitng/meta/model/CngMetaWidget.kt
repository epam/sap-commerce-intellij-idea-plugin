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
import com.intellij.openapi.vfs.VirtualFile

class CngMetaWidget(
    virtualFile: VirtualFile,
    myDom: Widget,
    val widgets: Collection<CngMetaWidget> = emptyList(),
) : CngMeta<Widget>(virtualFile, myDom) {

    val id: String = myDom.id.stringValue!!
    val name: String? = myDom.title.stringValue
    val slotId: String? = myDom.slotId.stringValue
    val widgetDefinitionId: String? = myDom.widgetDefinitionId.stringValue
    val access: String? = myDom.access.stringValue
    val lastFocusedChildIndex: String? = myDom.lastFocusedChildIndex.stringValue
    val lastFocusedTemplateInstanceId: String? = myDom.lastFocusedTemplateInstanceId.stringValue
    val template = myDom.template.value ?: false

    override fun toString() = id
}