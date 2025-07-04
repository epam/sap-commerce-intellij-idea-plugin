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

package com.intellij.idea.plugin.hybris.flexibleSearch.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.util.asSafely
import java.awt.datatransfer.StringSelection

class FlexibleSearchCopyQueryToClipboardAction : AnAction(
    message("hybris.fxs.actions.copy_query_to_clipboard"),
    message("hybris.fxs.actions.copy_query_to_clipboard.description"),
    HybrisIcons.FlexibleSearch.COPY_TO_CLIPBOARD
), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext) ?: return
        val project = e.project ?: return

        e.presentation.isEnabled = FileEditorManager.getInstance(project)
            .getSelectedEditor(editor.virtualFile)
            .asSafely<FlexibleSearchSplitEditor>()
            ?.isParametersPanelVisible() ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext) ?: return
        val project = e.project ?: return

        val textToCopy = FileEditorManager.getInstance(project)
            .getSelectedEditor(editor.virtualFile)
            .asSafely<FlexibleSearchSplitEditor>()
            ?.getQuery()
            ?: ""

        CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))

        Notifications.create(NotificationType.INFORMATION, message("hybris.editor.gutter.fsq.notification.title"), textToCopy)
            .hideAfter(10)
            .notify(project)
    }

}