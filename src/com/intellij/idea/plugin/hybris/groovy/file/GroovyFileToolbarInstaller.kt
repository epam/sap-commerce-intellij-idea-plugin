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
package com.intellij.idea.plugin.hybris.groovy.file

import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.startup.event.AbstractHybrisFileToolbarInstaller
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.groovy.GroovyFileType

class GroovyFileToolbarInstaller : AbstractHybrisFileToolbarInstaller(
    "hybris.groovy.console",
    "hybris.groovy.toolbar.left",
    "hybris.groovy.toolbar.right"
) {

    companion object {
        val instance: GroovyFileToolbarInstaller? = ApplicationManager.getApplication().getService(GroovyFileToolbarInstaller::class.java)
    }

    override fun isToolbarEnabled(project: Project, editor: EditorEx): Boolean {
        val settings = HybrisProjectSettingsComponent.getInstance(project).state
        val file = editor.virtualFile
        val isTestFile = file.path.contains("testsrc", true)
        val enabledForGroovyTestOrAllGroovyFiles = settings.groovySettings.enableActionsToolbarForGroovyTest && isTestFile || !isTestFile
        return (PluginCommon.isPluginActive(PluginCommon.GROOVY_PLUGIN_ID)
            && file.fileType is GroovyFileType
            && settings.groovySettings.enableActionsToolbar
            && enabledForGroovyTestOrAllGroovyFiles)
    }
}