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

package sap.commerce.toolset.typeSystem.diagram.node.graph

import sap.commerce.toolset.i18n
data class TSGraphNodeRoot(
    override val name: String = i18n("hybris.diagram.ts.provider.name"),
    override val fields: MutableList<TSGraphField> = mutableListOf(),
    override var collapsed: Boolean = false,
    override val tooltip: String? = null
) : TSGraphNode {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TSGraphNodeRoot) return false

        if (name != other.name) return false
        return fields.toTypedArray().contentEquals(other.fields.toTypedArray())
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + fields.toTypedArray().contentHashCode()
        return result
    }
}