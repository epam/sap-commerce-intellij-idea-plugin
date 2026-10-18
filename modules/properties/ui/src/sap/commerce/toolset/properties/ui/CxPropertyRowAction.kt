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

package sap.commerce.toolset.properties.ui

/**
 * Action icons pinned to the right edge of a property row, ordered as they are laid out.
 *
 * The hit zones are measured from the right edge of the cell, so the enum also fixes how far each one reaches:
 * [DELETE] sits outermost, every following entry one [hitWidth] further to the left.
 */
internal enum class CxPropertyRowAction(val tooltip: String, val hitWidth: Int = 28) {

    DELETE("Delete property"),
    EDIT("Edit property"),
    REPORT("Show property report"),
    ;

    companion object {

        /** Distance from the right edge of a cell at which the zone of this action starts. */
        fun offsetOf(action: CxPropertyRowAction) = entries
            .take(action.ordinal + 1)
            .sumOf { it.hitWidth }
    }
}
