/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.flexibleSearch.file.actions

import com.intellij.idea.plugin.hybris.actions.ActionUtils
import com.intellij.idea.plugin.hybris.actions.CopyFileToHybrisConsoleUtils
import com.intellij.idea.plugin.hybris.actions.CopyFileToHybrisConsoleUtils.isRequiredSingleFileExtension
import com.intellij.idea.plugin.hybris.common.HybrisConstants.FLEXIBLE_SEARCH_CONSOLE_TITLE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.FLEXIBLE_SEARCH_FILE_EXTENSION
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class CopyFlexibleSearchFileAction : AnAction(), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(event :AnActionEvent) {
        val project = event.project ?: return
        event.presentation.isEnabledAndVisible = ActionUtils.isHybrisContext(project) && isRequiredSingleFileExtension(project, FLEXIBLE_SEARCH_FILE_EXTENSION)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        CopyFileToHybrisConsoleUtils.copySelectedFilesToHybrisConsole(project, FLEXIBLE_SEARCH_CONSOLE_TITLE, FLEXIBLE_SEARCH_FILE_EXTENSION)
    }
}
