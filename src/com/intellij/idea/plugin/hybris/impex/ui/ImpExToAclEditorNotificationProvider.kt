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
package com.intellij.idea.plugin.hybris.impex.ui

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRights
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.util.application
import java.util.function.Function

class ImpExToAclEditorNotificationProvider : EditorNotificationProvider {

    override fun collectNotificationData(
        project: Project,
        file: VirtualFile
    ): Function<FileEditor, EditorNotificationPanel>? {
        val projectSettings = ProjectSettingsComponent.getInstance(project)
        if (!projectSettings.isHybrisProject()) return null
        if (!FileTypeRegistry.getInstance().isFileOfType(file, ImpexFileType)) return null
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null

        PsiTreeUtil.collectElementsOfType(psiFile, ImpexUserRights::class.java)
            .takeIf { it.isNotEmpty() } ?: return null

        return Function<FileEditor, EditorNotificationPanel> { fileEditor ->
            with(EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Promo)) {
                icon(HybrisIcons.Y.LOGO_GREEN)
                text = "User rights may be extracted to an own Access Control Lists File"

                application.runWriteAction {
                    if (ReadonlyStatusHandler.ensureFilesWritable(project, file)) {
                        createActionLabel("Extract user rights", "hybris.impex.extractAcl")
                    }
                }

                createActionLabel("Learn more..", "hybris.impex.learnMoreAcl", false)

                this
            }
        }
    }
}
