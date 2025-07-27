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
package com.intellij.idea.plugin.hybris.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionContext
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.ActiveIcon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent

abstract class ExecutionContextSettingsAction<M : ExecutionContext.ModifiableSettings> : DumbAwareAction() {

    protected abstract fun previewSettings(e: AnActionEvent, project: Project): String
    protected abstract fun settings(e: AnActionEvent, project: Project): M
    protected abstract fun settingsPanel(e: AnActionEvent, project: Project, settings: M): DialogPanel
    protected abstract fun applySettings(editor: Editor, settings: M)

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return
        val project = e.project ?: return

        e.presentation.icon = HybrisIcons.Connection.CONTEXT
        e.presentation.text = "Execution Context Settings<br>" + previewSettings(e, project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val inputEvent = e.inputEvent ?: return
        val settings = settings(e, project)
        val settingsPanel = settingsPanel(e, project, settings)

        var isFormValid = true

        JBPopupFactory.getInstance().createComponentPopupBuilder(settingsPanel, null)
            .setMovable(false)
            .setResizable(false)
            .setRequestFocus(true)
            .setTitle("Execution Settings")
            .setTitleIcon(ActiveIcon(HybrisIcons.Connection.CONTEXT))
            .setAdText("*applicable only to current editor")
            .setCancelCallback { isFormValid }
            .createPopup()
            .also { popup ->
                settingsPanel.registerValidators(popup) { validations ->
                    isFormValid = validations.values.all { it.okEnabled }
                }

                popup.addListener(object : JBPopupListener {
                    override fun onClosed(event: LightweightWindowEvent) {
                        if (isFormValid) {
                            settingsPanel.apply()
                            applySettings(editor, settings)
                        }
                    }
                })
                popup.showUnderneathOf(inputEvent.component)
            }
    }

}
