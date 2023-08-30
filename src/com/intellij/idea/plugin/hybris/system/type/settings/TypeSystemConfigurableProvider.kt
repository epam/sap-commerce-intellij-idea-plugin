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

package com.intellij.idea.plugin.hybris.system.type.settings

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class TypeSystemConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.ts.title"), "[y] SAP Commerce plugin Type System configuration."
    ) {

        private val settings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state.typeSystemSettings

        private lateinit var foldingEnableCheckBox: JCheckBox

        override fun createPanel() = panel {
            group("Code Folding") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(settings.folding::enabled)
                        .component
                }
                row {
                    checkBox("Use table-like folding for Atomics")
                        .bindSelected(settings.folding::tablifyAtomics)
                        .enabledIf(foldingEnableCheckBox.selected)
                }
                row {
                    checkBox("Use table-like folding for Collections")
                        .bindSelected(settings.folding::tablifyCollections)
                        .enabledIf(foldingEnableCheckBox.selected)
                }
                row {
                    checkBox("Use table-like folding for Maps")
                        .bindSelected(settings.folding::tablifyMaps)
                        .enabledIf(foldingEnableCheckBox.selected)
                }
                row {
                    checkBox("Use table-like folding for Relations")
                        .bindSelected(settings.folding::tablifyRelations)
                        .enabledIf(foldingEnableCheckBox.selected)
                }
                row {
                    checkBox("Use table-like folding for Item attributes")
                        .bindSelected(settings.folding::tablifyItemAttributes)
                        .enabledIf(foldingEnableCheckBox.selected)
                }
            }
        }
    }
}