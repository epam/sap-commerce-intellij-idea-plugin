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
package com.intellij.idea.plugin.hybris.diagram.businessProcess.impl

import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphNode
import com.intellij.idea.plugin.hybris.system.businessProcess.model.NavigableElement
import com.intellij.idea.plugin.hybris.system.businessProcess.model.Process
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

class DefaultBpGraphNode(
    override val navigableElement: NavigableElement,
    override val nodesMap: Map<String, BpGraphNode>,
    override val xmlVirtualFile: VirtualFile,
    override val process: Process
) : BpGraphNode {
    override val transitions: MutableMap<String, BpGraphNode> = HashMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val otherNode = other as DefaultBpGraphNode
        if (!navigableElement.getId().isValid || !otherNode.navigableElement.getId().isValid) return false

        return EqualsBuilder()
            .append(navigableElement.getId().stringValue, otherNode.navigableElement.getId().stringValue)
            .isEquals
    }

    override fun hashCode(): Int = HashCodeBuilder(17, 37)
        .append(navigableElement.getId().stringValue)
        .toHashCode()

    override fun toString() = "DefaultBpGraphNode{genericAction=${navigableElement.getId().stringValue}}"
}
