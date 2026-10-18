/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties

import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyState
import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyTemplateState

/** The template with [key] set to [value]: an existing property keeps its position, a new one goes last. */
fun CxCustomPropertyTemplateState.withProperty(key: String, value: String): CxCustomPropertyTemplateState {
    val property = CxCustomPropertyState(key, value)

    return if (properties.any { it.key == key }) copy(properties = properties.map { if (it.key == key) property else it })
    else copy(properties = properties + property)
}

/** The template with the value of an existing [key] replaced; a key the template does not have is left out. */
fun CxCustomPropertyTemplateState.withValue(key: String, value: String) = copy(
    properties = properties.map { if (it.key == key) it.copy(value = value) else it },
)

fun CxCustomPropertyTemplateState.withoutProperty(key: String) = copy(properties = properties.filterNot { it.key == key })

/** The templates with the one sharing [template]'s uuid replaced in place, or [template] appended when none does. */
fun List<CxCustomPropertyTemplateState>.replacing(template: CxCustomPropertyTemplateState) =
    if (any { it.uuid == template.uuid }) map { if (it.uuid == template.uuid) template else it }
    else this + template
