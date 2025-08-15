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

import com.intellij.ide.ui.search.SearchUtil
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.treetable.TreeTableTree
import com.intellij.util.asSafely
import com.intellij.util.ui.UIUtil
import java.io.Serial
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

class CCv2TreeCellRenderer : DefaultTreeCellRenderer() {

    override fun getTreeCellRendererComponent(
        tree: JTree, node: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean
    ) = SimpleColoredComponent().apply {
        if (node !is CCv2TreeNode) return@apply

        val reallyHasFocus = tree.asSafely<TreeTableTree>()?.treeTable?.hasFocus() ?: false
        val background = UIUtil.getTreeBackground(selected, reallyHasFocus)
        UIUtil.changeBackGround(this, background)

        val foreground = when {
            selected -> UIUtil.getTreeSelectionForeground(reallyHasFocus)
            else -> UIUtil.getTreeForeground()
        }

        val style = if (node is CCv2TreeNode.Group) SimpleTextAttributes.STYLE_BOLD
        else SimpleTextAttributes.STYLE_PLAIN

        SearchUtil.appendFragments(null, node.label(), style, foreground, background, this)

        node.hint()
            ?.let {
                val attributes = if (selected) SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, foreground)
                else SimpleTextAttributes.GRAYED_ATTRIBUTES

                append(" $it", attributes)
            }

        setForeground(foreground)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 1451475520404592449L
    }
}