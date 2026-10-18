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
 * What the properties view lists. The two report modes compare the remote instance against the project's own property
 * chain, so both of them need the complete remote set rather than the page the user has scrolled to.
 */
enum class CxPropertyViewMode(val title: String, val description: String) {

    ALL("All Properties", "Every property of the remote instance"),

    /** On the remote instance, declared nowhere the active project configuration reads. */
    MISSING_IN_PROJECT("Missing in Project", "Remote properties which the project does not declare"),

    /** Declared by the active project configuration, absent from the remote instance. */
    MISSING_ON_REMOTE("Missing on Remote", "Project properties which the remote instance does not have"),
    ;

    val report
        get() = this != ALL
}
