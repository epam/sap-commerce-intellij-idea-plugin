/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

import com.intellij.idea.plugin.hybris.actions.AbstractExecuteAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely

class FlexibleSearchExecuteQueryAction : AbstractExecuteAction(
    FlexibleSearchFileType.defaultExtension,
    HybrisConstants.CONSOLE_TITLE_FLEXIBLE_SEARCH
) {

    init {
        with(templatePresentation) {
            text = message("hybris.fxs.actions.execute_query")
            description = message("hybris.fxs.actions.execute_query.description")
            icon = HybrisIcons.Console.Actions.EXECUTE
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val file = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
        val enabled = file != null && file.name.endsWith(".$extension")
        e.presentation.isEnabledAndVisible = enabled
    }

    override fun processContent(e: AnActionEvent, content: String, editor: Editor, project: Project): String = FileEditorManager.getInstance(project)
        .getSelectedEditor(editor.virtualFile)
        .asSafely<FlexibleSearchSplitEditor>()
        ?.getQuery()
        ?: content
}
