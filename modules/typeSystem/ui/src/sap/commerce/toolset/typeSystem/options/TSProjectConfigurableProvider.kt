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

package sap.commerce.toolset.typeSystem.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.settings.yDeveloperSettings
import sap.commerce.toolset.typeSystem.ui.TSDiagramSettingsExcludedTypeNameTable
import java.awt.Dimension

class TSProjectConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(project: Project) : BoundSearchableConfigurable(
        i18n("hybris.settings.project.ts.title"), "[y] SAP CX Type System configuration."
    ) {

        private val developerSettings = project.yDeveloperSettings
        private val tsDiagramMutableSettings = developerSettings.typeSystemDiagramSettings.mutable()

        private val excludedTypeNamesTable = TSDiagramSettingsExcludedTypeNameTable.getInstance(project)
        private val excludedTypeNamesPane = ToolbarDecorator.createDecorator(excludedTypeNamesTable)
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel()

        init {
            excludedTypeNamesPane.minimumSize = Dimension(excludedTypeNamesPane.width, 400)
        }

        override fun createPanel() = panel {
            group("Diagram Settings") {
                row {
                    checkBox("Collapse nodes by default")
                        .bindSelected(tsDiagramMutableSettings::nodesCollapsedByDefault)
                }

                row {
                    checkBox("Show OOTB Map nodes")
                        .comment("One of the OOTB Map example is `localized:java.lang.String`.")
                        .bindSelected(tsDiagramMutableSettings::showOOTBMapNodes)
                }

                row {
                    checkBox("Show custom Atomic nodes")
                        .bindSelected(tsDiagramMutableSettings::showCustomAtomicNodes)
                }

                row {
                    checkBox("Show custom Collection nodes")
                        .bindSelected(tsDiagramMutableSettings::showCustomCollectionNodes)
                }

                row {
                    checkBox("Show custom Enum nodes")
                        .bindSelected(tsDiagramMutableSettings::showCustomEnumNodes)
                }

                row {
                    checkBox("Show custom Map nodes")
                        .bindSelected(tsDiagramMutableSettings::showCustomMapNodes)
                }

                row {
                    checkBox("Show custom Relation nodes")
                        .comment("Relations with set Deployment will be always displayed.")
                        .bindSelected(tsDiagramMutableSettings::showCustomRelationNodes)
                }
            }

            group("Diagram - Excluded Type Names", true) {
                row {
                    cell(excludedTypeNamesPane)
                        .onApply { tsDiagramMutableSettings.excludedTypeNames = getNewTypeNames() }
                        .onReset { excludedTypeNamesTable.updateModel(tsDiagramMutableSettings) }
                        .onIsModified { tsDiagramMutableSettings.excludedTypeNames != getNewTypeNames() }
                        .align(Align.FILL)
                }
            }
        }

        override fun apply() {
            super.apply()

            developerSettings.typeSystemDiagramSettings = tsDiagramMutableSettings.immutable()
        }

        private fun getNewTypeNames() = excludedTypeNamesTable.getItems()
            .map { it.typeName }
            .toMutableSet()
    }
}