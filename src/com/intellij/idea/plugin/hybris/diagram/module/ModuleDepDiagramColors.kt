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

package com.intellij.idea.plugin.hybris.diagram.module

import com.intellij.diagram.DiagramColors
import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.editor.colors.ColorKey.createColorKeyWithFallback

object ModuleDepDiagramColors {

    val EDGE_CIRCULAR: ColorKey = createColorKeyWithFallback("MODULE_DEP_DIAGRAM_EDGE_CIRCULAR", DiagramColors.BAD_EDGE)
}