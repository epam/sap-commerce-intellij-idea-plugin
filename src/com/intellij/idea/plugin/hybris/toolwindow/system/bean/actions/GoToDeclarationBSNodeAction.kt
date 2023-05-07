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
package com.intellij.idea.plugin.hybris.toolwindow.system.bean.actions

import com.intellij.idea.plugin.hybris.actions.AbstractGoToDeclarationAction
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.idea.plugin.hybris.system.bean.model.EnumValue
import com.intellij.idea.plugin.hybris.system.bean.model.Property
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.BSTreeModel
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes.BSMetaNode
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes.BSMetaTypeNode
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes.BSNode
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.refactoring.suggested.startOffset
import javax.swing.JTree

class GoToDeclarationBSNodeAction : AbstractGoToDeclarationAction() {

    init {
        ActionUtil.copyFrom(this, "GotoDeclarationOnly")
    }

    override fun update(e: AnActionEvent) {
        val node = getSelectedNode(e)

        if (node == null || node is BSMetaTypeNode) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
        e.presentation.icon = HybrisIcons.DECLARATION
    }

    override fun actionPerformed(e: AnActionEvent) {
        val tsNode = getSelectedNode(e) ?: return
        val project = e.project ?: return
        if (tsNode !is BSMetaNode<*>) return

        when (val dom = tsNode.meta.retrieveDom()) {
            is Bean -> navigate(project, dom, dom.clazz.xmlAttributeValue?.startOffset)
            is Enum -> navigate(project, dom, dom.clazz.xmlAttributeValue?.startOffset)
            is EnumValue -> navigate(project, dom, dom.xmlElement?.startOffset)
            is Property -> navigate(project, dom, dom.name.xmlAttributeValue?.startOffset)
        }
    }

    private fun getSelectedNode(event: AnActionEvent) = event
        .getData(PlatformCoreDataKeys.CONTEXT_COMPONENT)
        ?.let { it as? JTree }
        ?.selectionPath
        ?.lastPathComponent
        ?.let { it as? BSTreeModel.Node }
        ?.userObject
        ?.let { it as? BSNode }
}