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

package com.intellij.idea.plugin.hybris.tools.ccv2.ui.tree

import com.intellij.ui.treeStructure.treetable.TreeTable
import com.intellij.ui.treeStructure.treetable.TreeTableModel
import com.intellij.ui.treeStructure.treetable.TreeTableTree
import com.intellij.util.asSafely
import com.intellij.util.ui.tree.TreeUtil
import java.io.Serial
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class CCv2TreeTableModel(
    root: TreeNode,
    private val selectedReplicaIds: MutableCollection<String>,
) : DefaultTreeModel(root), TreeTableModel {
    private var myTreeTable: TreeTable? = null

    override fun getColumnCount(): Int = 2
    override fun getColumnName(column: Int) = null

    override fun getColumnClass(column: Int) = when (column) {
        TREE_COLUMN -> TreeTableModel::class.java
        IS_ENABLED_COLUMN -> Boolean::class.java
        else -> throw java.lang.IllegalArgumentException("Unexpected value: $column")
    }

    override fun getValueAt(node: Any?, column: Int): Any? = when (column) {
        TREE_COLUMN -> node?.asSafely<CCv2TreeNode>()?.label()
        IS_ENABLED_COLUMN -> isSelected(node)
        else -> throw IllegalArgumentException()
    }

    private fun isSelected(node: Any?): Any? = node.asSafely<CCv2TreeNode>()
        ?.selectionState()

    override fun isCellEditable(node: Any?, column: Int): Boolean = column == IS_ENABLED_COLUMN

    override fun setValueAt(aValue: Any?, node: Any?, column: Int) {
        val doEnable = aValue?.asSafely<Boolean>() ?: return

        when (node) {
            is CCv2TreeNode.Group -> {
                TreeUtil.listChildren(node).forEach { setValueAt(aValue, it, column) }
            }

            is CCv2TreeNode.Replica -> {
                if (doEnable) selectedReplicaIds.add(node.replica.name)
                else selectedReplicaIds.remove(node.replica.name)

                node.dropCache()

                generateSequence(node.parent.asSafely<CCv2TreeNode.Group>()) { it.parent.asSafely<CCv2TreeNode.Group>() }
                    .forEach { it.dropCache() }
            }
        }
    }

    override fun setTree(tree: JTree?) {
        myTreeTable = (tree as TreeTableTree).treeTable
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 409957019411399730L
        const val TREE_COLUMN = 0
        const val IS_ENABLED_COLUMN = 1
    }

}