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

package sap.commerce.toolset.beanSystem.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.settings.yDeveloperSettings
import javax.swing.JCheckBox

class BSProjectConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(project: Project) : BoundSearchableConfigurable(
        i18n("hybris.settings.project.bs.title"), "[y] SAP CX Bean System configuration."
    ) {

        private val developerSettings = project.yDeveloperSettings
        private val mutableSettings = developerSettings.beanSystemSettings.mutable()

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
                        checkBox("Properties")
                            .bindSelected(mutableSettings.folding::tablifyProperties)
                            .enabledIf(foldingEnableCheckBox.selected)
                    }
                }
            }
        }

        override fun apply() {
            super.apply()

            developerSettings.beanSystemSettings = mutableSettings.immutable()
        }
    }
}