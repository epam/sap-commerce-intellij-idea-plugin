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

package sap.commerce.toolset.beanSystem.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.psi.util.startOffset
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.AbstractGoToDeclarationAction
import sap.commerce.toolset.beanSystem.meta.model.BSMetaClassifier
import sap.commerce.toolset.beanSystem.model.*
import sap.commerce.toolset.ui.AbstractTable

class GoToDeclarationBSTableAction : AbstractGoToDeclarationAction() {

    init {
        ActionUtil.copyFrom(this, "GotoDeclarationOnly")
    }

    override fun update(e: AnActionEvent) {
        val item = getSelectedItem(e)

        if (item == null || item !is BSMetaClassifier<*>) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
        e.presentation.icon = HybrisIcons.Actions.GO_TO_DECLARATION
    }

    override fun actionPerformed(e: AnActionEvent) {
        val item = getSelectedItem(e) ?: return
        val project = e.project ?: return
        if (item !is BSMetaClassifier<*>) return

        when (val dom = item.retrieveDom()) {
            is EnumValue -> navigate(project, dom, dom.xmlElement?.startOffset)
            is Property -> navigate(project, dom, dom.name.xmlAttributeValue?.startOffset)
            is Hint -> navigate(project, dom, dom.name.xmlAttributeValue?.startOffset)
            is Import -> navigate(project, dom, dom.xmlTag?.startOffset)
            is Annotations -> navigate(project, dom, dom.xmlTag?.startOffset)
        }
    }

    private fun getSelectedItem(event: AnActionEvent) = event
        .getData(PlatformCoreDataKeys.CONTEXT_COMPONENT)
        ?.asSafely<AbstractTable<*, *>>()
        ?.getCurrentItem()
}