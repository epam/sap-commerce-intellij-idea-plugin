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

package sap.commerce.toolset.properties.settings.state

/**
 * How the Source Code branch of the properties tree is arranged.
 */
enum class CxPropertySourceMode(val title: String, val description: String) {

    /** One node standing for the whole project, listing the chain as the running system would resolve it. */
    PROJECT("Project", "List the properties of the project as a whole"),

    /** One node per extension, grouped the way the Project view groups the modules. */
    EXTENSIONS("Extensions", "List the properties declared by each extension on its own"),
}
