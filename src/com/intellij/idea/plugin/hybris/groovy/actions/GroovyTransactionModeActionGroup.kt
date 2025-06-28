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
package com.intellij.idea.plugin.hybris.groovy.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
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

class GroovyTransactionModeActionGroup : DefaultActionGroup() {

    init {
        templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
        templatePresentation.putClientProperty(ActionUtil.COMPONENT_PROVIDER, object : CustomComponentAction {
            override fun createCustomComponent(presentation: Presentation, place: String): JComponent = object :
                ActionButtonWithText(this@GroovyTransactionModeActionGroup, presentation, place, Dimension()) {
                @Serial
                private val serialVersionUID: Long = 5346829716506322630L

                override fun createAndShowActionGroupPopup(actionGroup: ActionGroup, event: AnActionEvent): JBPopup = JBPopupFactory.getInstance()
                    .createActionGroupPopup(null, this@GroovyTransactionModeActionGroup, event.dataContext, null, true)
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
        )
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val mode = DeveloperSettingsComponent.getInstance(project).state.groovySettings.txMode
            .let { message("hybris.groovy.actions.transaction.${it.name.lowercase()}") }

        e.presentation.text = message("hybris.groovy.actions.transaction.mode", mode)
    }

}
