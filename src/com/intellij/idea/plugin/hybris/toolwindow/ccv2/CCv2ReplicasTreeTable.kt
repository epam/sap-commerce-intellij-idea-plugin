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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentStatus
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.treetable.TreeTable
import com.intellij.ui.treeStructure.treetable.TreeTableModel
import com.intellij.ui.treeStructure.treetable.TreeTableTree
import com.intellij.util.asSafely
import com.intellij.util.ui.UIUtil
import java.util.*
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

private const val TREE_COLUMN = 0
private const val SEVERITIES_COLUMN = 1
private const val IS_ENABLED_COLUMN = 2

class CCv2ReplicasTreeTable : TreeTable(
    CCv2ReplicasTreeTableModel(CCv2ReplicaTreeNode.Group("root"))
), Disposable {

    fun refresh(project: Project, subscription: CCv2Subscription) {
        isEnabled = false
        model
        CCv2Service.getInstance(project).fetchEnvironments(
            listOf(subscription),
            onStartCallback = {
//                startLoading("Fetching environments...")
            },
            onCompleteCallback = { response ->
                response[subscription]
                    ?.filter { environment -> environment.accessible }
                    ?.let { environments ->
//                        stopLoading()
                    }
            },
            sendEvents = false,
            statuses = EnumSet.of(CCv2EnvironmentStatus.AVAILABLE),
            requestV1Details = true,
            requestV1Health = false
        )
    }

    override fun dispose() = UIUtil.dispose(this)
}

private class CCv2ReplicasTreeTableModel(root: TreeNode) : DefaultTreeModel(root), TreeTableModel {
    private var myTreeTable: TreeTable? = null

    override fun getColumnCount(): Int = 3
    override fun getColumnName(column: Int) = null

    override fun getColumnClass(column: Int) = when (column) {
        // TODO: add constants
        TREE_COLUMN -> TreeTableModel::class.java
        SEVERITIES_COLUMN -> Icon::class.java
        IS_ENABLED_COLUMN -> Boolean::class.java
        else -> throw java.lang.IllegalArgumentException("Unexpected value: $column")
    }

    override fun getValueAt(node: Any?, column: Int): Any? = when (column) {
        TREE_COLUMN -> null
        SEVERITIES_COLUMN -> HybrisIcons.CCv2.DESCRIPTOR // TODO: implement
        IS_ENABLED_COLUMN -> isEnabled(node)
        else -> throw IllegalArgumentException()
    }

    private fun isEnabled(node: Any?): Any? {
        // TODO: implement me
        return true
    }

    override fun isCellEditable(node: Any?, column: Int): Boolean = column == IS_ENABLED_COLUMN

    override fun setValueAt(aValue: Any?, node: Any?, column: Int) {
        val doEnable = aValue?.asSafely<Boolean>() ?: return

        // TODO: implement me
    }

    override fun setTree(tree: JTree?) {
        myTreeTable = (tree as TreeTableTree).treeTable
    }

}