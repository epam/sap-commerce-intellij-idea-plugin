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

package com.intellij.idea.plugin.hybris.system.type.settings.options

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.settings.DeveloperSettings
import com.intellij.idea.plugin.hybris.system.type.settings.ui.TSDiagramSettingsExcludedTypeNameTable
import com.intellij.idea.plugin.hybris.util.isHybrisProject
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import javax.swing.JCheckBox

class ProjectTypeSystemConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(project: Project) : BoundSearchableConfigurable(
        HybrisI18NBundleUtils.message("hybris.settings.project.ts.title"), "[y] SAP CX Type System configuration."
    ) {

        private val developerSettings = DeveloperSettings.getInstance(project)
        private val tsMutableSettings = developerSettings.typeSystemSettings.mutable()
        private val tsDiagramMutableSettings = developerSettings.typeSystemDiagramSettings.mutable()

        private val excludedTypeNamesTable = TSDiagramSettingsExcludedTypeNameTable.getInstance(project)
        private val excludedTypeNamesPane = ToolbarDecorator.createDecorator(excludedTypeNamesTable)
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel()

        init {
            excludedTypeNamesPane.minimumSize = Dimension(excludedTypeNamesPane.width, 400)
        }

        private lateinit var foldingEnableCheckBox: JCheckBox

        override fun createPanel() = panel {
            group("Code Folding - items.xml") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(tsMutableSettings.folding::enabled)
                        .component
                }
                group("Table-Like Folding", true) {
                    row {
                        checkBox("Atomics")
                            .bindSelected(tsMutableSettings.folding::tablifyAtomics)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Collections")
                            .bindSelected(tsMutableSettings.folding::tablifyCollections)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Maps")
                            .bindSelected(tsMutableSettings.folding::tablifyMaps)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Relations")
                            .bindSelected(tsMutableSettings.folding::tablifyRelations)
                            .enabledIf(foldingEnableCheckBox.selected)
                    }
                    row {
                        checkBox("Item attributes")
                            .bindSelected(tsMutableSettings.folding::tablifyItemAttributes)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Item indexes")
                            .bindSelected(tsMutableSettings.folding::tablifyItemIndexes)
                            .enabledIf(foldingEnableCheckBox.selected)
                        checkBox("Item custom properties")
                            .bindSelected(tsMutableSettings.folding::tablifyItemCustomProperties)
                            .enabledIf(foldingEnableCheckBox.selected)
                    }
                }
            }

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

            developerSettings.typeSystemSettings = tsMutableSettings.immutable()
            developerSettings.typeSystemDiagramSettings = tsDiagramMutableSettings.immutable()
        }

        private fun getNewTypeNames() = excludedTypeNamesTable.getItems()
            .map { it.typeName }
            .toMutableSet()
    }
}