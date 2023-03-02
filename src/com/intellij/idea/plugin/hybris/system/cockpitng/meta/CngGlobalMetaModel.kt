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
package com.intellij.idea.plugin.hybris.system.cockpitng.meta

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngMetaActionDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngMetaEditorDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngMetaWidget
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngMetaWidgetDefinition
import com.intellij.idea.plugin.hybris.system.type.meta.impl.CaseInsensitive.CaseInsensitiveConcurrentHashMap
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.ModificationTracker

/**
 * Component can be any string
 * @see <a href="https://help.sap.com/docs/SAP_COMMERCE/5c9ea0c629214e42b727bf08800d8dfa/8c75ec11866910149df9dfb10df17f03.html?locale=en-US&q=editorareaactions%20component#configuration-context">docs</a>
 */
class CngGlobalMetaModel : ModificationTracker, Disposable {

    private var modificationTracker = Long.MIN_VALUE
    val components = mutableSetOf<String>()
    val contextAttributes = mutableMapOf<String, MutableSet<String>>()
    val actionDefinitions = CaseInsensitiveConcurrentHashMap<String, CngMetaActionDefinition>()
    val widgetDefinitions = CaseInsensitiveConcurrentHashMap<String, CngMetaWidgetDefinition>()
    val editorDefinitions = CaseInsensitiveConcurrentHashMap<String, CngMetaEditorDefinition>()
    val widgets = CaseInsensitiveConcurrentHashMap<String, CngMetaWidget>()

    fun clear() {
        cleanup()

        if (modificationTracker == Long.MAX_VALUE) modificationTracker = 0L
        modificationTracker++
    }

    override fun getModificationCount() = modificationTracker
    override fun dispose() = cleanup()

    private fun cleanup() {
        components.clear()
        contextAttributes.clear()
        actionDefinitions.clear()
        widgetDefinitions.clear()
        editorDefinitions.clear()
        widgets.clear()
    }

}
