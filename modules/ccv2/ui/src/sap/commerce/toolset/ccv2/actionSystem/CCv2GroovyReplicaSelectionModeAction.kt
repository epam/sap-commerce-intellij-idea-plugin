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

package sap.commerce.toolset.ccv2.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.util.Disposer
import com.intellij.util.asSafely
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.ccv2.CCv2ExecConstants
import sap.commerce.toolset.ccv2.ui.CCv2ReplicaSelectionDialog
import sap.commerce.toolset.groovy.actionSystem.GroovyReplicaSelectionModeAction
import sap.commerce.toolset.groovy.editor.groovyExecContextSettings
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import java.awt.Component

class CCv2GroovyReplicaSelectionModeAction : GroovyReplicaSelectionModeAction(CCv2ExecConstants.ccv2) {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = Plugin.GROOVY.isActive()
        super.update(e)
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val component = e.inputEvent?.source?.asSafely<Component>() ?: return
        val execSettings = e.groovyExecContextSettings {
            val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
            GroovyExecContext.defaultSettings(activeConnection)
        }

        val dialog = CCv2ReplicaSelectionDialog(project, editor, execSettings, component)
        dialog.showAndGet()
        Disposer.dispose(dialog)
    }
}