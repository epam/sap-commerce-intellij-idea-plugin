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
package sap.commerce.toolset.cockpitNG.meta

import sap.commerce.toolset.CaseInsensitiveMap.CaseInsensitiveConcurrentHashMap
import sap.commerce.toolset.cockpitNG.meta.model.CngMetaActionDefinition
import sap.commerce.toolset.cockpitNG.meta.model.CngMetaEditorDefinition
import sap.commerce.toolset.cockpitNG.meta.model.CngMetaWidget
import sap.commerce.toolset.cockpitNG.meta.model.CngMetaWidgetDefinition
import sap.commerce.toolset.meta.GlobalMetaModel

/**
 * Component can be any string
 * @see <a href="https://help.sap.com/docs/SAP_COMMERCE/5c9ea0c629214e42b727bf08800d8dfa/8c75ec11866910149df9dfb10df17f03.html?locale=en-US&q=editorareaactions%20component#configuration-context">docs</a>
 */
class CngGlobalMetaModel : GlobalMetaModel {

    val components = mutableSetOf<String>()
    val contextAttributes = mutableMapOf<String, MutableSet<String>>()
    val actionDefinitions = CaseInsensitiveConcurrentHashMap<String, CngMetaActionDefinition>()
    val widgetDefinitions = CaseInsensitiveConcurrentHashMap<String, CngMetaWidgetDefinition>()
    val editorDefinitions = CaseInsensitiveConcurrentHashMap<String, CngMetaEditorDefinition>()
    val widgets = CaseInsensitiveConcurrentHashMap<String, CngMetaWidget>()

}
