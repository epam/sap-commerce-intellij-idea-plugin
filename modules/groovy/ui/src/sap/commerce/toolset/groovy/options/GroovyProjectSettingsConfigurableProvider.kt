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

package sap.commerce.toolset.groovy.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.groovy.actionSystem.GroovyFileToolbarInstaller
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.settings.yDeveloperSettings
import javax.swing.JCheckBox

class GroovyProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject && Plugin.GROOVY.isActive()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        i18n("hybris.settings.project.groovy.title"), "hybris.groovy.settings"
    ) {

        private val developerSettings = project.yDeveloperSettings
        private val mutableSettings = developerSettings.groovySettings.mutable()
        private lateinit var enableActionToolbar: JCheckBox

        override fun createPanel() = panel {
            group("Language") {
                row {
                    enableActionToolbar = checkBox("Enable actions toolbar for each Groovy file")
                        .bindSelected(mutableSettings::enableActionsToolbar)
                        .comment("Actions toolbar enables possibility to change current remote SAP Commerce session and perform operations on current file, such as `Execute on remote server`")
                        .onApply { GroovyFileToolbarInstaller.getInstance().toggleToolbarForAllEditors(project) }
                        .component
                }
                row {
                    checkBox("Enable actions toolbar for a Test Groovy file")
                        .bindSelected(mutableSettings::enableActionsToolbarForGroovyTest)
                        .comment("Enables Actions toolbar for the groovy files located in the <strong>${HybrisConstants.TEST_SRC_DIRECTORY}</strong> or <strong>${HybrisConstants.GROOVY_TEST_SRC_DIRECTORY}</strong> directory.")
                        .enabledIf(enableActionToolbar.selected)
                        .onApply { GroovyFileToolbarInstaller.getInstance().toggleToolbarForAllEditors(project) }
                }
                row {
                    checkBox("Enable actions toolbar for a IDE Groovy scripts")
                        .bindSelected(mutableSettings::enableActionsToolbarForGroovyIdeConsole)
                        .comment("Enables Actions toolbar for the groovy files located in the <strong>${HybrisConstants.IDE_CONSOLES_PATH}</strong> (In Project View, Scratches and Consoles -> IDE Consoles).")
                        .enabledIf(enableActionToolbar.selected)
                        .onApply { GroovyFileToolbarInstaller.getInstance().toggleToolbarForAllEditors(project) }
                }
            }
        }

        override fun apply() {
            super.apply()
            developerSettings.groovySettings = mutableSettings.immutable()
        }
    }
}