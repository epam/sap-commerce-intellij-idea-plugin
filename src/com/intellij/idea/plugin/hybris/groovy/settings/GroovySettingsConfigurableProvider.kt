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

package com.intellij.idea.plugin.hybris.groovy.settings

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.groovy.file.GroovyFileToolbarInstaller
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import org.jetbrains.plugins.groovy.GroovyFileType
import javax.swing.JCheckBox

class GroovySettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.groovy.title"), "hybris.groovy.settings"
    ) {

        private val state = HybrisProjectSettingsComponent.getInstance(project).state.groovySettings
        private lateinit var enableActionToolbar: JCheckBox

        override fun createPanel() = panel {
            group("Language") {
                row {
                    enableActionToolbar = checkBox("Enable actions toolbar for each Groovy file")
                        .bindSelected(state::enableActionsToolbar)
                        .comment("Actions toolbar enables possibility to change current remote SAP Commerce session and perform operations on current file, such as `Execute on remote server`")
                        .component
                }
                row {
                    checkBox("Enable actions toolbar for a Test Groovy file")
                        .bindSelected(state::enableActionsToolbarForGroovyTest)
                        .comment("Enables Actions toolbar for the groovy files located in the testsrc folder.")
                        .enabledIf(enableActionToolbar.selected)
                        .onApply {
                            FileEditorManager.getInstance(project).allEditors
                                .filter { it.file.fileType is GroovyFileType }
                                .forEach {
                                    val editorEx = EditorUtil.getEditorEx(it)
                                    if (isGroovyFileToolbarEnabled(it.file, state.enableActionsToolbar, state.enableActionsToolbarForGroovyTest)) {
                                        editorEx
                                            ?.takeIf { it.permanentHeaderComponent == null }
                                            ?.let { GroovyFileToolbarInstaller.instance?.install(project, it) }
                                    } else {
                                        editorEx
                                            ?.takeIf { it.permanentHeaderComponent != null }
                                            ?.let {
                                                it.permanentHeaderComponent = null
                                                it.headerComponent = null
                                            }
                                    }
                                }
                        }
                }

            }
        }

        private fun isGroovyFileToolbarEnabled(
            file: VirtualFile,
            enableActionsToolbar: Boolean,
            enableActionsToolbarForGroovyTest: Boolean
        ): Boolean {
            val isTestFile = file.path.contains("testsrc", true)
            val enabledForGroovyTestOrAllGroovyFiles = enableActionsToolbarForGroovyTest && isTestFile || !isTestFile
            return (PluginCommon.isPluginActive(PluginCommon.GROOVY_PLUGIN_ID)
                && file.fileType is GroovyFileType
                && enableActionsToolbar
                && enabledForGroovyTestOrAllGroovyFiles)
        }
    }
}