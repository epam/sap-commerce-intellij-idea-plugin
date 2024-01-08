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
package com.intellij.idea.plugin.hybris.flexibleSearch.ui

import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.idea.plugin.hybris.flexibleSearch.settings.FlexibleSearchSettings
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

abstract class AbstractFxSEditorNotificationProvider : EditorNotificationProvider, DumbAware {

    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        val projectSettings = HybrisProjectSettingsComponent.getInstance(project)
        if (!projectSettings.isHybrisProject()) return null
        if (!FileTypeRegistry.getInstance().isFileOfType(file, FlexibleSearchFileType)) return null

        val developerSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state
        val fxsSettings = developerSettings.flexibleSearchSettings
        if (!shouldCollect(fxsSettings)) return null

        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null

        if (collect(fxsSettings, psiFile).isEmpty()) return null

        return panelFunction(fxsSettings, project, psiFile, file)
    }

    protected abstract fun shouldCollect(fxsSettings: FlexibleSearchSettings): Boolean

    protected abstract fun panelFunction(
        fxsSettings: FlexibleSearchSettings,
        project: Project,
        psiFile: PsiFile,
        file: VirtualFile
    ): Function<FileEditor, EditorNotificationPanel>

    protected abstract fun collect(fxsSettings: FlexibleSearchSettings, psiFile: PsiFile): Collection<LeafPsiElement>

}
