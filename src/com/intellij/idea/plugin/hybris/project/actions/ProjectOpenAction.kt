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

package com.intellij.idea.plugin.hybris.project.actions

import com.intellij.ide.actions.ImportModuleAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.CancellationException

class ProjectOpenAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        // can be invoked only programmatically
        e.presentation.isEnabledAndVisible = false
    }

    override fun actionPerformed(e: AnActionEvent) {
        val data = e.getData(DATA_KEY_PROJECT_FILE) ?: return

        val providers = ImportModuleAction.getProviders(null)
        var cancel = false

        val modules = ImportModuleAction.doImport(null) {
            val createImportWizard = ImportModuleAction.createImportWizard(null, null, data, *providers.toTypedArray())
            createImportWizard?.cancelButton?.addActionListener {
                cancel = true
            }
            createImportWizard
        }

        if (cancel) {
            throw CancellationException("Project opening cancelled")
        }

        println(modules)
    }

    companion object {
        val DATA_KEY_PROJECT_FILE = DataKey.create<VirtualFile>("myfile")
    }
}