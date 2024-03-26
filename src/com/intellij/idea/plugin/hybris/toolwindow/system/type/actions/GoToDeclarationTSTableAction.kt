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

import com.intellij.idea.plugin.hybris.actions.AbstractGoToDeclarationAction
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaClassifier
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.idea.plugin.hybris.toolwindow.ui.AbstractTable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.refactoring.suggested.startOffset

class GoToDeclarationTSTableAction : AbstractGoToDeclarationAction() {

    init {
        ActionUtil.copyFrom(this, "GotoDeclarationOnly")
    }

    override fun update(e: AnActionEvent) {
        val item = getSelectedItem(e)

        if (item == null || item !is TSMetaClassifier<*>) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
        e.presentation.icon = HybrisIcons.DECLARATION
    }

    override fun actionPerformed(e: AnActionEvent) {
        val item = getSelectedItem(e) ?: return
        val project = e.project ?: return
        if (item !is TSMetaClassifier<*>) return

        when (val dom = item.retrieveDom()) {
            is EnumValue -> navigate(project, dom, dom.code.xmlAttributeValue?.startOffset)
            is Attribute -> navigate(project, dom, dom.qualifier.xmlAttributeValue?.startOffset)
            is CustomProperty -> navigate(project, dom, dom.name.xmlAttributeValue?.startOffset)
            is Index -> navigate(project, dom, dom.name.xmlAttributeValue?.startOffset)
            is RelationElement -> navigate(project, dom, dom.xmlTag?.startOffset)
        }
    }

    private fun getSelectedItem(event: AnActionEvent) = event
        .getData(PlatformCoreDataKeys.CONTEXT_COMPONENT)
        ?.let { it as? AbstractTable<*, *> }
        ?.getCurrentItem()
}