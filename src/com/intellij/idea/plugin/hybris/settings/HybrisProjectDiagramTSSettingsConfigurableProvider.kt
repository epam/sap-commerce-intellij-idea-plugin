/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.components.TSDiagramSettingsExcludedTypeNameTable
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumHeight
import com.intellij.util.ui.JBUI

class HybrisProjectDiagramTSSettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.diagram.ts.title"), "[y] SAP Commerce plugin Type System Diagram configuration."
    ) {

        private val tsSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state.typeSystemDiagramSettings;

        private val excludedTypeNamesTable = TSDiagramSettingsExcludedTypeNameTable.getInstance(project)
        private val excludedTypeNamesPane = ToolbarDecorator.createDecorator(excludedTypeNamesTable)
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel();

        init {
            excludedTypeNamesPane.minimumHeight = 400
        }

        override fun createPanel() = panel {
            group("Common Settings") {
                row {
                    checkBox("Nodes collapsed by default")
                        .bindSelected(tsSettings::nodesCollapsedByDefault)
                }

                row {
                    checkBox("Show OOTB Map Nodes")
                        .comment("One of the OOTB Map example is `localized:java.lang.String`")
                        .bindSelected(tsSettings::showOOTBMapNodes)
                }

                row {
                    checkBox("Show Custom Atomic Nodes")
                        .bindSelected(tsSettings::showCustomAtomicNodes)
                }

                row {
                    checkBox("Show Custom Collection Nodes")
                        .bindSelected(tsSettings::showCustomCollectionNodes)
                }

                row {
                    checkBox("Show Custom Enum Nodes")
                        .bindSelected(tsSettings::showCustomEnumNodes)
                }

                row {
                    checkBox("Show Custom Map Nodes")
                        .bindSelected(tsSettings::showCustomMapNodes)
                }

                row {
                    checkBox("Show Custom Relation Nodes")
                        .comment("Relations with Deployment details are not affected by this flag")
                        .bindSelected(tsSettings::showCustomRelationNodes)
                }
            }

            group("Excluded Type Names", false) {
                row {
                    cell(excludedTypeNamesPane)
                        .onApply { tsSettings.excludedTypeNames = getNewTypeNames() }
                        .onReset { excludedTypeNamesTable.updateModel(tsSettings) }
                        .onIsModified { tsSettings.excludedTypeNames != getNewTypeNames()  }
                        .align(Align.FILL)
                }
            }
        }

        private fun getNewTypeNames() = excludedTypeNamesTable.getItems()
            .map { it.typeName }
            .toMutableSet()
    }
}
