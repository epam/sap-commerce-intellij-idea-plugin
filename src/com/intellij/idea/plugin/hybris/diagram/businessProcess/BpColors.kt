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

package com.intellij.idea.plugin.hybris.diagram.businessProcess

import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.editor.colors.ColorKey.createColorKeyWithFallback
import com.intellij.openapi.editor.colors.ColorKey.find

object BpColors {

    val START: ColorKey = createColorKeyWithFallback("BP_DIAGRAM_START_EDGE", find("DIAGRAM_DEFAULT_EDGE"))
    val PARTIAL: ColorKey = createColorKeyWithFallback("BP_DIAGRAM_PARTIAL_EDGE", find("DIAGRAM_DEFAULT_EDGE"))
    val OK: ColorKey = createColorKeyWithFallback("BP_DIAGRAM_OK_EDGE", find("DIAGRAM_DEFAULT_EDGE"))
    val NOK: ColorKey = createColorKeyWithFallback("BP_DIAGRAM_NOK_EDGE", find("DIAGRAM_DEFAULT_EDGE"))
    val TIMEOUT: ColorKey = createColorKeyWithFallback("BP_DIAGRAM_TIMEOUT_EDGE", find("DIAGRAM_DEFAULT_EDGE"))
    val CYCLE: ColorKey = createColorKeyWithFallback("BP_DIAGRAM_CYCLE_EDGE", find("DIAGRAM_DEFAULT_EDGE"))
    val CANCEL: ColorKey = createColorKeyWithFallback("BP_DIAGRAM_CANCEL_EDGE", find("DIAGRAM_DEFAULT_EDGE"))
    val DEFAULT: ColorKey = createColorKeyWithFallback("BP_DIAGRAM_DEFAULT_EDGE", find("DIAGRAM_DEFAULT_EDGE"))
}