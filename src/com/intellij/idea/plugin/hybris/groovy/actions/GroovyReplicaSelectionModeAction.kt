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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.ReplicaContext
import com.intellij.idea.plugin.hybris.tools.remote.http.ReplicaSelectionMode
import com.intellij.idea.plugin.hybris.toolwindow.CCv2ReplicaSelectionDialog
import com.intellij.idea.plugin.hybris.toolwindow.ManualReplicaSelectionDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.KeepPopupOnPerform
import com.intellij.openapi.actionSystem.ex.CheckboxAction
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.getOrCreateUserDataUnsafe
import com.intellij.util.asSafely
import java.awt.Component

abstract class GroovyReplicaSelectionModeAction(private val replicaSelectionMode: ReplicaSelectionMode) : CheckboxAction(
    message("hybris.groovy.actions.executionMode.${replicaSelectionMode.name.lowercase()}"),
    message("hybris.groovy.actions.executionMode.${replicaSelectionMode.name.lowercase()}.description"),
    null
) {

    override fun update(e: AnActionEvent) {
        super.update(e)

        e.presentation.keepPopupOnPerform = KeepPopupOnPerform.Never
        e.presentation.icon = if (isSelected(e)) replicaSelectionMode.icon
        else IconLoader.getDisabledIcon(replicaSelectionMode.icon)
//        e.presentation.hoveredIcon = replicaSelectionMode.icon
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean = e.getData(CommonDataKeys.EDITOR)
        ?.getOrCreateUserDataUnsafe(HybrisConstants.KEY_GROOVY_REPLICA_SELECTION_MODE) { replicaSelectionMode }
        ?.let { it == replicaSelectionMode }
        ?: false

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) {
            e.getData(CommonDataKeys.EDITOR)
                ?.putUserData(HybrisConstants.KEY_GROOVY_REPLICA_SELECTION_MODE, replicaSelectionMode)
        }
    }
}

class GroovyAutoReplicaSelectionModeAction : GroovyReplicaSelectionModeAction(ReplicaSelectionMode.AUTO) {

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        HybrisHacHttpClient.getInstance(project).setReplicaExecutionContext(null)
        super.setSelected(e, state)
    }
}

class GroovyManualReplicaSelectionModeAction : GroovyReplicaSelectionModeAction(ReplicaSelectionMode.MANUAL) {

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val component = e.inputEvent?.source?.asSafely<Component>()
            ?: return
        val executionContext = editor.getUserData(HybrisConstants.KEY_REMOTE_EXECUTION_CONTEXT)
            ?.takeIf { it.mode == ReplicaSelectionMode.MANUAL }
            ?: ReplicaContext.manual()

        val result = ManualReplicaSelectionDialog(project, executionContext, component).showAndGet()
        super.setSelected(e, result)
    }
}

class GroovyCCv2ReplicaSelectionModeAction : GroovyReplicaSelectionModeAction(ReplicaSelectionMode.CCV2) {

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val component = e.inputEvent?.source?.asSafely<Component>()
            ?: return
        val executionContext = editor.getUserData(HybrisConstants.KEY_REMOTE_EXECUTION_CONTEXT)
            ?.takeIf { it.mode == ReplicaSelectionMode.CCV2 }
            ?: ReplicaContext.ccv2()

        val result = CCv2ReplicaSelectionDialog(project, executionContext, component).showAndGet()
        super.setSelected(e, result)
    }
}
