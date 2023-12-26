/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.tree

import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.TSNode
import java.io.Serial
import javax.swing.tree.DefaultMutableTreeNode

class TreeNode(private val node: TSNode) : DefaultMutableTreeNode(node) {

    override fun toString() = node.toString()

    companion object {
        @Serial
        private val serialVersionUID: Long = -4729552031003080261L
    }
}