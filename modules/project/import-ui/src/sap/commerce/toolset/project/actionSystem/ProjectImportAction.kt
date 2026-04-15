/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.project.actionSystem

import com.intellij.icons.AllIcons
import com.intellij.ide.actions.ImportModuleAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.wm.impl.welcomeScreen.NewWelcomeScreen
import com.intellij.projectImport.ProjectImportProvider
import sap.commerce.toolset.project.HybrisProjectImportProvider

class ProjectImportAction : DumbAwareAction(
    "Import SAP Commerce Project",
    null,
    null
) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val importProvider = ProjectImportProvider.PROJECT_IMPORT_PROVIDER.extensionsIfPointIsRegistered
            .filterIsInstance<HybrisProjectImportProvider>()
            .firstOrNull()
            ?: return

        ImportModuleAction.doImport(null) {
            ImportModuleAction.selectFileAndCreateWizard(
                project = null,
                dialogParent = null,
                descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                providers = arrayOf(importProvider),
            )
        }
    }

    override fun update(e: AnActionEvent) {
        if (NewWelcomeScreen.isNewWelcomeScreen(e)) {
            e.presentation.setIcon(AllIcons.ToolbarDecorator.Import)
        }

        e.presentation.isVisible = false
    }

}
