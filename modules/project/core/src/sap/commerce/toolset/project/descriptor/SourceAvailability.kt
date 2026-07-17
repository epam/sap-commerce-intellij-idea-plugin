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

package sap.commerce.toolset.project.descriptor

import sap.commerce.toolset.HybrisIcons
import javax.swing.Icon

enum class SourceAvailability(val icon: Icon = HybrisIcons.Y.LOGO_BLUE, val title: String) {
    SOURCES_ONLY(HybrisIcons.SourceAvailability.SOURCES_ONLY, "Sources"),
    SOURCES_WITH_JAR(HybrisIcons.SourceAvailability.SOURCES_WITH_JAR, "Sources & server.jar"),
    EXTERNAL(HybrisIcons.SourceAvailability.EXTERNAL, "Binary"),
    NONE(HybrisIcons.SourceAvailability.NONE, "None"),
    UNKNOWN(HybrisIcons.SourceAvailability.UNKNOWN, "Unknown");

    companion object {
        fun of(hasSources: Boolean, hasJar: Boolean) = when {
            hasSources && hasJar -> SOURCES_WITH_JAR
            hasSources -> SOURCES_ONLY
            hasJar -> EXTERNAL
            else -> NONE
        }
    }
}