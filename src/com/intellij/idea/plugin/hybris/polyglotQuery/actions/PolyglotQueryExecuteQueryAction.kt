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

import com.intellij.idea.plugin.hybris.actions.AbstractExecuteAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.polyglotQuery.file.PolyglotQueryFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class PolyglotQueryExecuteQueryAction : AbstractExecuteAction(
    PolyglotQueryFileType.instance.defaultExtension,
    HybrisConstants.CONSOLE_TITLE_POLYGLOT_QUERY
) {

    init {
        with(templatePresentation) {
            text = HybrisI18NBundleUtils.message("hybris.pgq.actions.execute_query")
            description = HybrisI18NBundleUtils.message("hybris.pgq.actions.execute_query.description")
            icon = HybrisIcons.CONSOLE_EXECUTE
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val file = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
        val enabled = file != null && file.name.endsWith(".$extension")
        e.presentation.isEnabledAndVisible = enabled
    }
}