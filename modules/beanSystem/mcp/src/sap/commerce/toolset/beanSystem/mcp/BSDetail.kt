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

package sap.commerce.toolset.beanSystem.mcp

/**
 * How much information the `sap_commerce_list_*` bean-system tools return per classifier, in
 * ascending order of verbosity — the ladder is the same for every bean kind, so all four tools
 * share it.
 *
 * [MEMBERS] means whatever the classifier declares: properties for a bean, values for an enum.
 */
enum class BSDetail {
    BASIC,
    MEMBERS,
    FULL;

    val withMembers: Boolean
        get() = this != BASIC

    val full: Boolean
        get() = this == FULL

    companion object {
        fun resolve(detail: String) = entries.find { it.name.equals(detail.trim(), ignoreCase = true) }
            ?: error("Invalid detail '$detail'. Valid values: ${entries.joinToString { it.name }}")
    }
}
