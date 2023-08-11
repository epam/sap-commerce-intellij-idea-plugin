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
package com.intellij.idea.plugin.hybris.impex.actions

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.idea.plugin.hybris.tools.remote.action.AbstractExecuteAction
import com.intellij.openapi.actionSystem.ActionUpdateThread

class ImpExExecuteAction : AbstractExecuteAction() {

    init {
        with (templatePresentation) {
            text = message("hybris.impex.actions.execute_query")
            description = message("hybris.impex.actions.execute_query.description")
            icon = HybrisIcons.CONSOLE_EXECUTE
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
    override val extension = ImpexFileType.INSTANCE.defaultExtension
    override val consoleName = HybrisConstants.IMPEX_CONSOLE_TITLE
}
