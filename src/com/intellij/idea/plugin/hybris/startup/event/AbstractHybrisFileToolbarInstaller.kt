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

package com.intellij.idea.plugin.hybris.startup.event

import com.intellij.ide.actions.ToggleToolbarAction
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.impl.EditorHeaderComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.util.containers.JBIterable

abstract class AbstractHybrisFileToolbarInstaller(
    private val toolbarId: String,
    private val leftGroupId: String,
    private val rightGroupId: String,
    internal val fileType: FileType
) {

    abstract fun isToolbarEnabled(project: Project, editor: EditorEx): Boolean

    private fun install(project: Project, editor: EditorEx) {
        val actionManager = ActionManager.getInstance()
        val headerComponent = EditorHeaderComponent()
        val leftGroup = actionManager.getAction(leftGroupId) as ActionGroup
        val rightGroup = actionManager.getAction(rightGroupId) as ActionGroup
        val leftToolbar = actionManager.createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, leftGroup, true)
        val rightToolbar = actionManager.createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, rightGroup, true)
        rightToolbar.setReservePlaceAutoPopupIcon(false)
        leftToolbar.targetComponent = editor.contentComponent
        rightToolbar.targetComponent = editor.contentComponent
        headerComponent.add(leftToolbar.component, "Center")
        headerComponent.add(rightToolbar.component, "East")
        leftToolbar.updateActionsAsync()
        editor.permanentHeaderComponent = headerComponent
        editor.headerComponent = headerComponent

        ToggleToolbarAction.setToolbarVisible(
            toolbarId,
            PropertiesComponent.getInstance(project),
            JBIterable.of(headerComponent),
            null as Boolean?
        )
    }

    fun toggleToolbarForAllEditors(project: Project) {
        FileEditorManager.getInstance(project).allEditors
            .filter { fileType == it.file.fileType }
            .mapNotNull { EditorUtil.getEditorEx(it) }
            .forEach {
                toggleToolbar(project, it)
            }
    }

    fun toggleToolbar(project: Project, editor: EditorEx) {
        if (isToolbarEnabled(project, editor)) enableToolbar(project, editor)
        else toggle(editor, false)
    }

    private fun enableToolbar(project: Project, editor: EditorEx) {
        if (editor.permanentHeaderComponent == null) install(project, editor)
        else toggle(editor, true)
    }

    private fun toggle(editor: EditorEx, visible: Boolean) = with(editor) {
        permanentHeaderComponent?.isVisible = visible
        headerComponent?.isVisible = visible
    }
}