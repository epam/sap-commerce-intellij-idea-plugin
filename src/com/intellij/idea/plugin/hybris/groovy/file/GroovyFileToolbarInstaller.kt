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
package com.intellij.idea.plugin.hybris.groovy.file

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.startup.event.AbstractHybrisFileToolbarInstaller
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.groovy.GroovyFileType

class GroovyFileToolbarInstaller : AbstractHybrisFileToolbarInstaller(
    "hybris.groovy.console",
    "hybris.groovy.toolbar.left",
    "hybris.groovy.toolbar.right",
    GroovyFileType.GROOVY_FILE_TYPE
) {

    companion object {
        fun getInstance(): GroovyFileToolbarInstaller? = ApplicationManager.getApplication().getService(GroovyFileToolbarInstaller::class.java)
    }

    override fun isToolbarEnabled(project: Project, editor: EditorEx): Boolean {
        val settings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state
        val file = editor.virtualFile
        val path = file.path
        val isTestFile = path.contains(HybrisConstants.TEST_SRC_DIRECTORY, true)
            || path.contains(HybrisConstants.GROOVY_TEST_SRC_DIRECTORY, true)
        val enabledForGroovyTestOrAllGroovyFiles = settings.groovySettings.enableActionsToolbarForGroovyTest && isTestFile || !isTestFile

        return PluginCommon.isPluginActive(PluginCommon.PLUGIN_GROOVY)
            && fileType == file.fileType
            && settings.groovySettings.enableActionsToolbar
            && enabledForGroovyTestOrAllGroovyFiles
    }
}