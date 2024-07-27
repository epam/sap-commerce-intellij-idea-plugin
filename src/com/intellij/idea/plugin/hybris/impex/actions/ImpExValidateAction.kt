/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.actions.AbstractExecuteAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.openapi.actionSystem.ActionUpdateThread

class ImpExValidateAction : AbstractExecuteAction(
    ImpexFileType.defaultExtension,
    HybrisConstants.CONSOLE_TITLE_IMPEX
) {

    init {
        with(templatePresentation) {
            text = "Validate ImpEx"
            description = "Validate ImpEx file via remote SAP Commerce instance"
            icon = HybrisIcons.ImpEx.Actions.VALIDATE
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun doExecute(consoleService: HybrisConsoleService) {
        consoleService.validateImpex()
    }
}
