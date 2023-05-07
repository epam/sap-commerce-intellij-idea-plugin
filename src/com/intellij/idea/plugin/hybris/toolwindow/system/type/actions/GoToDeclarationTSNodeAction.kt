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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.actions

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.TSTreeModel
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.TSMetaNode
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.TSMetaTypeNode
import com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes.TSNode
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.Project
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.xml.DomElement
import javax.swing.JTree

class GoToDeclarationTSNodeAction : AnAction() {

    init {
        ActionUtil.copyFrom(this, "GotoDeclarationOnly")
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        val tsNode = getSelectedNode(e)

        if (tsNode == null || tsNode is TSMetaTypeNode) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
        e.presentation.icon = HybrisIcons.DECLARATION
    }

    override fun actionPerformed(e: AnActionEvent) {
        val tsNode = getSelectedNode(e) ?: return
        val project = e.project ?: return
        if (tsNode !is TSMetaNode<*>) return

        when (val dom = tsNode.meta.retrieveDom()) {
            is AtomicType -> navigate(project, dom, dom.clazz.xmlAttributeValue?.startOffset)
            is CollectionType -> navigate(project, dom, dom.code.xmlAttributeValue?.startOffset)
            is EnumType -> navigate(project, dom, dom.code.xmlAttributeValue?.startOffset)
            is EnumValue -> navigate(project, dom, dom.code.xmlAttributeValue?.startOffset)
            is Attribute -> navigate(project, dom, dom.qualifier.xmlAttributeValue?.startOffset)
            is CustomProperty -> navigate(project, dom, dom.name.xmlAttributeValue?.startOffset)
            is Index -> navigate(project, dom, dom.name.xmlAttributeValue?.startOffset)
            is ItemType -> navigate(project, dom, dom.code.xmlAttributeValue?.startOffset)
            is MapType -> navigate(project, dom, dom.code.xmlAttributeValue?.startOffset)
            is Relation -> navigate(project, dom, dom.code.xmlAttributeValue?.startOffset)
            is RelationElement -> navigate(project, dom, dom.xmlTag?.startOffset)
        }
    }

    private fun navigate(project: Project, dom: DomElement, offset: Int?) {
        if (offset == null) return

        dom.xmlTag?.containingFile?.virtualFile
            ?.let {
                PsiNavigationSupport.getInstance()
                    .createNavigatable(project, it, offset)
                    .navigate(false)
            }
    }

    private fun getSelectedNode(event: AnActionEvent) = event
        .getData(PlatformCoreDataKeys.CONTEXT_COMPONENT)
        ?.let { it as? JTree }
        ?.selectionPath
        ?.lastPathComponent
        ?.let { it as? TSTreeModel.Node }
        ?.userObject
        ?.let { it as? TSNode }
}