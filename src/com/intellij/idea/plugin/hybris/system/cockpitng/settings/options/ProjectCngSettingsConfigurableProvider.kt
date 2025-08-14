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

package com.intellij.idea.plugin.hybris.system.cockpitng.settings.options

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.settings.DeveloperSettings
import com.intellij.idea.plugin.hybris.util.isHybrisProject
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class ProjectCngSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(project: Project) : BoundSearchableConfigurable(
        HybrisI18NBundleUtils.message("hybris.settings.project.cng.title"), "[y] SAP CX Cockpit NG configuration."
    ) {

        private val developerSettings = DeveloperSettings.getInstance(project)
        private val mutableSettings = developerSettings.cngSettings.mutable()

        private lateinit var foldingEnableCheckBox: JCheckBox

        override fun createPanel() = panel {
            group("Code Folding") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(mutableSettings.folding::enabled)
                        .component
                }
                group("Table-Like Folding", true) {
                    row {
                        checkBox("Wizard properties")
                            .bindSelected(mutableSettings.folding::tablifyWizardProperties)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Navigation nodes")
                            .bindSelected(mutableSettings.folding::tablifyNavigationNodes)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Search fields")
                            .bindSelected(mutableSettings.folding::tablifySearchFields)
                            .enabledIf(foldingEnableCheckBox.selected)
                    }
                    row {
                        checkBox("List columns")
                            .bindSelected(mutableSettings.folding::tablifyListColumns)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Parameters")
                            .bindSelected(mutableSettings.folding::tablifyParameters)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Molds")
                            .bindSelected(mutableSettings.folding::tablifyMolds)
                            .enabledIf(foldingEnableCheckBox.selected)
                    }
                }
            }
        }

        override fun apply() {
            super.apply()

            developerSettings.cngSettings = mutableSettings.immutable()
        }
    }
}