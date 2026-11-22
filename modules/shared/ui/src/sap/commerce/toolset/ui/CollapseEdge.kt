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

package sap.commerce.toolset.ui

import com.intellij.icons.AllIcons
import java.awt.BorderLayout
import javax.swing.Icon

/**
 * The edge of the editor a collapsible panel is docked to, the collapsed panel is a strip along this edge.
 */
enum class CollapseEdge(
    val collapseIcon: Icon,
    val expandIcon: Icon,
    val constraint: String,
) {
    RIGHT(AllIcons.General.ChevronRight, AllIcons.General.ChevronLeft, BorderLayout.EAST),
    BOTTOM(AllIcons.General.ChevronDown, AllIcons.General.ChevronUp, BorderLayout.SOUTH),
}
