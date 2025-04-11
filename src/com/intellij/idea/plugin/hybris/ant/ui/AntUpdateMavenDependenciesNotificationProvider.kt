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
package com.intellij.idea.plugin.hybris.ant.ui

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.lang.ant.config.AntBuildFileBase
import com.intellij.lang.ant.config.AntConfiguration
import com.intellij.lang.ant.config.execution.ExecutionHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.removeUserData
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import com.intellij.util.asSafely
import java.util.function.Function
import javax.swing.JComponent

class AntUpdateMavenDependenciesNotificationProvider : EditorNotificationProvider {

    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        val settings = ProjectSettingsComponent.getInstance(project)
        if (!settings.isHybrisProject()) return null
        if (file.name != HybrisConstants.EXTERNAL_DEPENDENCIES_XML) return null

        val showPanel = file
            .getUserData(HybrisConstants.KEY_ANT_UPDATE_MAVEN_DEPENDENCIES)
            ?: false

        if (!showPanel) return null

        return Function { fileEditor ->
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning)
            panel.icon(HybrisIcons.Y.LOGO_BLUE)
            panel.text = "[y] Download missing external dependencies via Ant"
            panel.createActionLabel("Download") {
                AntConfiguration.getInstance(project).buildFiles
                    .firstOrNull { it.virtualFile?.path?.endsWith("hybris/bin/platform/build.xml") ?: false }
                    ?.asSafely<AntBuildFileBase>()
                    ?.let { buildFile ->
                        val targets = listOf(HybrisConstants.ANT_TARGET_UPDATE_MAVEN_DEPENDENCIES)
                        ExecutionHandler.runBuild(buildFile, targets, null, DataContext.EMPTY_CONTEXT, emptyList())
                        { _, _ ->
                            EditorNotifications.getInstance(project).removeNotificationsForProvider(this)
                            file.removeUserData(HybrisConstants.KEY_ANT_UPDATE_MAVEN_DEPENDENCIES)
                        }
                        ToolWindowManager.getInstance(project).activateEditorComponent()
                    }
            }
            panel
        }
    }
}
