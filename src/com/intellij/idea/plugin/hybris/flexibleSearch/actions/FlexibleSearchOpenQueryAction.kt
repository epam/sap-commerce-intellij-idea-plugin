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

import com.intellij.idea.plugin.hybris.actions.OpenInHybrisConsoleService
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisFlexibleSearchConsole
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.SingleRootFileViewProvider

class FlexibleSearchOpenQueryAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        e.presentation.text = HybrisI18NBundleUtils.message("hybris.fxs.actions.open_query")
        e.presentation.description = HybrisI18NBundleUtils.message("hybris.fxs.actions.open_query.description")
        e.presentation.icon = HybrisIcons.Console.Actions.OPEN
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val content = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
            ?.firstOrNull()
            ?.takeIf { it.fileType is FlexibleSearchFileType }
            ?.takeUnless { SingleRootFileViewProvider.isTooLargeForIntelligence(it) }
            ?.let { FileDocumentManager.getInstance().getDocument(it) }
            ?.text
            ?: return

        OpenInHybrisConsoleService.getInstance(project)
            .openInConsole(HybrisFlexibleSearchConsole::class, content)
    }

}
