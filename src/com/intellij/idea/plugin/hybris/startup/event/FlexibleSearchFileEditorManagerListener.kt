/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.impl.EditorHeaderComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.util.containers.JBIterable
import javax.swing.JPanel

class FlexibleSearchFileEditorManagerListener(private val project: Project) : FileEditorManagerListener {

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        if (SingleRootFileViewProvider.isTooLargeForIntelligence(file)) return
        if (file.fileType !is FlexibleSearchFileType) return

        FileEditorManager.getInstance(project).getAllEditors(file)
            .firstNotNullOfOrNull { EditorUtil.getEditorEx(it) }
            ?.takeIf { it.permanentHeaderComponent == null }
            ?.let { installHeader(project, it, file) }
    }

    private fun installHeader(project: Project, editor: EditorEx, vf: VirtualFile) {
        val actionManager = ActionManager.getInstance()
        val leftGroup = actionManager.getAction("hybris.fxs.toolbar.left") as ActionGroup
        val rightGroup = actionManager.getAction("hybris.fxs.toolbar.right") as ActionGroup
        val headerComponent: JPanel = EditorHeaderComponent()
        val leftToolbar = ActionManager.getInstance().createActionToolbar("EditorToolbar", leftGroup, true)
        val rightToolbar = ActionManager.getInstance().createActionToolbar("EditorToolbar", rightGroup, true)
        rightToolbar.setReservePlaceAutoPopupIcon(false)
        leftToolbar.targetComponent = editor.contentComponent
        rightToolbar.targetComponent = editor.contentComponent
        headerComponent.add(leftToolbar.component, "Center")
        headerComponent.add(rightToolbar.component, "East")

        editor.permanentHeaderComponent = headerComponent
        editor.headerComponent = headerComponent

        ToggleToolbarAction.setToolbarVisible(
            "hybris.fxs.console",
            PropertiesComponent.getInstance(project),
            JBIterable.of(headerComponent),
            null as Boolean?
        )
    }
}
