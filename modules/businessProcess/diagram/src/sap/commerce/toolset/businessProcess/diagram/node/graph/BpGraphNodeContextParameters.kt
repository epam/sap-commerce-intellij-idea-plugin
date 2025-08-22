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
package sap.commerce.toolset.businessProcess.diagram.node.graph

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import sap.commerce.toolset.businessProcess.model.Process

open class BpGraphNodeContextParameters(
    override val name: String,
    override val virtualFileUrl: String,
    override val virtualFileName: String,
    override val process: Process,
    override val properties: Array<BpGraphField> = emptyArray()
) : BpGraphNode {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val otherNode = other as BpGraphNodeContextParameters

        return EqualsBuilder()
            .append(name, otherNode.name)
            .isEquals
    }

    override fun hashCode(): Int = HashCodeBuilder(17, 37)
        .append(name)
        .toHashCode()

    override fun toString() = "BpGraphNodeContextParameters{genericAction=$name}"

}
