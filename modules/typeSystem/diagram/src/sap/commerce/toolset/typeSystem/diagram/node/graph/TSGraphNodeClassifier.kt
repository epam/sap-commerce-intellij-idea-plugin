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

import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaClassifier

/**
 * `transitiveNode` can be true in the following cases:
 *  - non-custom Extends Node (will be taken into account only in case of "Custom + Extends" or "All" current Scope
 *  - non-custom Dependency Node (will be taken into account only in combination with `model.isShowDependencies == true`
 */
data class TSGraphNodeClassifier(
    override val name: String,
    val meta: TSGlobalMetaClassifier<*>,
    override val fields: MutableList<TSGraphField> = mutableListOf(),
    val transitiveNode: Boolean = false,
    override var collapsed: Boolean = false,
    override val tooltip: String?
) : TSGraphNode {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TSGraphNodeClassifier) return false

        if (name != other.name) return false
        if (meta != other.meta) return false
        return fields.toTypedArray().contentEquals(other.fields.toTypedArray())
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + meta.hashCode()
        result = 31 * result + fields.toTypedArray().contentHashCode()
        return result
    }
}