/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.polyglotQuery.actions

import com.intellij.idea.plugin.hybris.actions.CopyFileToHybrisConsoleUtils
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.polyglotQuery.file.PolyglotQueryFileType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.SingleRootFileViewProvider

class PolyglotQueryOpenQueryAction : AnAction(
    message("hybris.pgq.actions.open_query"),
    message("hybris.pgq.actions.open_query.description"),
    HybrisIcons.CONSOLE_OPEN
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val query = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
            ?.firstOrNull()
            ?.takeIf { it.fileType is PolyglotQueryFileType }
            ?.takeUnless { SingleRootFileViewProvider.isTooLargeForIntelligence(it) }
            ?.let { FileDocumentManager.getInstance().getDocument(it) }?.text
            ?: return

        CopyFileToHybrisConsoleUtils.copyQueryToConsole(
            project,
            HybrisConstants.CONSOLE_TITLE_POLYGLOT_QUERY,
            query
        )
    }

}
