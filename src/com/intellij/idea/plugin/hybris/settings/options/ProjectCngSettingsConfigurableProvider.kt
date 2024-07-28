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

package com.intellij.idea.plugin.hybris.settings.options

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class ProjectCngSettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = ProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.cng.title"), "[y] SAP CX Cockpit NG configuration."
    ) {

        private val settings = DeveloperSettingsComponent.getInstance(project).state.cngSettings

        private lateinit var foldingEnableCheckBox: JCheckBox

        override fun createPanel() = panel {
            group("Code Folding") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(settings.folding::enabled)
                        .component
                }
                group("Table-Like Folding", true) {
                    row {
                        checkBox("Wizard properties")
                            .bindSelected(settings.folding::tablifyWizardProperties)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Navigation nodes")
                            .bindSelected(settings.folding::tablifyNavigationNodes)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Search fields")
                            .bindSelected(settings.folding::tablifySearchFields)
                            .enabledIf(foldingEnableCheckBox.selected)
                    }
                    row {
                        checkBox("List columns")
                            .bindSelected(settings.folding::tablifyListColumns)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Parameters")
                            .bindSelected(settings.folding::tablifyParameters)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Molds")
                            .bindSelected(settings.folding::tablifyMolds)
                            .enabledIf(foldingEnableCheckBox.selected)
                    }
                }
            }
        }
    }
}