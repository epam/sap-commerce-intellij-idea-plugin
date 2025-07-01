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

package com.intellij.idea.plugin.hybris.ui

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.popup.PopupFactoryImpl
import com.intellij.util.asSafely
import java.awt.Dimension
import java.io.Serial
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.SwingConstants
import javax.swing.event.ListSelectionEvent

class ActionButtonWithTextAndDescription(private val actionGroup: ActionGroup) : CustomComponentAction {

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent = object : ActionButtonWithText(
        actionGroup, presentation, place, Dimension()
    ) {
        @Serial
        private val serialVersionUID: Long = 5346829716506322630L

        override fun createAndShowActionGroupPopup(actionGroup: ActionGroup, event: AnActionEvent): JBPopup = JBPopupFactory.getInstance()
            .createActionGroupPopup(null, actionGroup, event.dataContext, null, true)
            .also { listPopup ->
                listPopup.listStep.values.firstOrNull()
                    ?.asSafely<PopupFactoryImpl.ActionItem>()
                    ?.description
                    ?.let { listPopup.setAdText(it, 2) }

                listPopup.addListSelectionListener { e: ListSelectionEvent? ->
                    e?.source
                        ?.asSafely<JList<Any>>()
                        ?.selectedValue
                        ?.asSafely<PopupFactoryImpl.ActionItem>()
                        ?.description
                        ?.let { description ->
                            listPopup.setAdText(description, SwingConstants.LEFT)
                        }
                }

                listPopup.showUnderneathOf(this)
            }
    }
}