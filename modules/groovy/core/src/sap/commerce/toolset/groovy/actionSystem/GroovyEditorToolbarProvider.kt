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

package sap.commerce.toolset.groovy.actionSystem

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.groovy.GroovyFileType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.actionSystem.HybrisEditorToolbarProvider
import sap.commerce.toolset.groovy.GroovyConstants
import sap.commerce.toolset.settings.yDeveloperSettings

class GroovyEditorToolbarProvider(
    override val toolbarId: String = "hybris.groovy.console",
    override val leftGroupId: String = "hybris.groovy.toolbar.left",
    override val rightGroupId: String = "hybris.groovy.toolbar.right",
    override val fileType: FileType = GroovyFileType.GROOVY_FILE_TYPE
) : HybrisEditorToolbarProvider {

    override fun isEnabled(project: Project, vf: VirtualFile): Boolean {
        if (Plugin.GROOVY.isDisabled()) return false
        if (fileType != vf.fileType) return false

        val groovySettings = project.yDeveloperSettings.groovySettings

        // Checking special cases where toolbar might not be desired
        val path = vf.path
        val isTestFile = path.contains(HybrisConstants.TEST_SRC_DIRECTORY, true)
            || path.contains(HybrisConstants.GROOVY_TEST_SRC_DIRECTORY, true)
        val isIdeConsole = path.contains(HybrisConstants.IDE_CONSOLES_PATH)
            || path.contains(GroovyConstants.PATH_CONSOLES_GROOVY)
        val testFileCheckPassed = groovySettings.enableActionsToolbarForGroovyTest && isTestFile || !isTestFile
        val ideConsoleCheckPassed = groovySettings.enableActionsToolbarForGroovyIdeConsole && isIdeConsole || !isIdeConsole

        return groovySettings.enableActionsToolbar
            && testFileCheckPassed
            && ideConsoleCheckPassed
    }
}