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

package com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph

import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaClassifier

/**
 * `transitiveNode` can be true in following cases:
 *  - non-custom Extends Node (will be taken into account only in case of "Custom + Extends" or "All" current Scope
 *  - non-custom Dependency Node (will be taken into account only in combination with `model.isShowDependencies == true`
 */
data class TSGraphNodeClassifier(
    override val name: String,
    val meta: TSGlobalMetaClassifier<*>,
    override val fields: Array<TSGraphField> = emptyArray(),
    val transitiveNode: Boolean = false
) : TSGraphNode {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TSGraphNodeClassifier) return false

        if (name != other.name) return false
        if (meta != other.meta) return false
        return fields.contentEquals(other.fields)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + meta.hashCode()
        result = 31 * result + fields.contentHashCode()
        return result
    }
}