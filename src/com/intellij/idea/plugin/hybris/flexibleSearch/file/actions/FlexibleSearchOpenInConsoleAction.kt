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

package com.intellij.idea.plugin.hybris.flexibleSearch.file.actions

import com.intellij.idea.plugin.hybris.actions.OpenInHybrisConsoleService
import com.intellij.idea.plugin.hybris.common.HybrisConstants.FLEXIBLE_SEARCH_FILE_EXTENSION
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisFlexibleSearchConsole
import com.intellij.idea.plugin.hybris.util.isHybrisProject
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class FlexibleSearchOpenInConsoleAction : AnAction(
    "Copy to FlexibleSearch Console",
    "Copy FlexibleSearch file to SAP Commerce console",
    HybrisIcons.Console.Actions.OPEN
), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        event.presentation.isEnabledAndVisible = project.isHybrisProject && OpenInHybrisConsoleService.getInstance(project)
            .isRequiredSingleFileExtension(FLEXIBLE_SEARCH_FILE_EXTENSION)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        OpenInHybrisConsoleService.getInstance(project)
            .openSelectedFilesInConsole(HybrisFlexibleSearchConsole::class, FLEXIBLE_SEARCH_FILE_EXTENSION)
    }
}
