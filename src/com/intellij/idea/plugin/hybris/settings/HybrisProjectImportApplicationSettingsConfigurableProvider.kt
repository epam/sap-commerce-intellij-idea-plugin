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
package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.common.equalsIgnoreOrder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.ui.CRUDListPanel
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class HybrisProjectImportApplicationSettingsConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable() = SettingsConfigurable()

    class SettingsConfigurable : BoundSearchableConfigurable(
        message("hybris.settings.application.project_import.title"), "[y] SAP Commerce plugin project import configuration."
    ) {

        private val state = HybrisApplicationSettingsComponent.getInstance().state

        private lateinit var groupModulesCheckBox: JCheckBox
        private lateinit var externalModulesCheckBox: JCheckBox


        private val junkList = CRUDListPanel(
            "hybris.import.settings.junk.directory.popup.add.title",
            "hybris.import.settings.junk.directory.popup.add.text",
            "hybris.import.settings.junk.directory.popup.edit.title",
            "hybris.import.settings.junk.directory.popup.edit.text",
        )
        private val excludeResources = CRUDListPanel(
            "hybris.import.settings.exclude.resources.popup.add.title",
            "hybris.import.settings.exclude.resources.popup.add.text",
            "hybris.import.settings.exclude.resources.popup.edit.title",
            "hybris.import.settings.exclude.resources.popup.edit.text",
        )
        private val excludeFromIndex = CRUDListPanel(
            "hybris.import.settings.excludedFromIndex.directory.popup.add.title",
            "hybris.import.settings.excludedFromIndex.directory.popup.add.text",
            "hybris.import.settings.excludedFromIndex.directory.popup.edit.title",
            "hybris.import.settings.excludedFromIndex.directory.popup.edit.text",
        )

        override fun createPanel() = panel {
            group("Modules Grouping") {
                row {
                    groupModulesCheckBox = checkBox(message("hybris.import.settings.group.modules"))
                        .bindSelected(state::groupModules)
                        .component
                }
                indent {
                    row {
                        icon(HybrisIcons.EXTENSION_OOTB)
                        textField()
                            .label(message("hybris.import.settings.group.hybris"))
                            .bindText(state::groupHybris)
                        textField()
                            .label(message("hybris.import.settings.group.unused"))
                            .bindText(state::groupOtherHybris)
                    }.layout(RowLayout.PARENT_GRID)
                    row {
                        icon(HybrisIcons.EXTENSION_CUSTOM)
                        textField()
                            .label(message("hybris.import.settings.group.custom"))
                            .bindText(state::groupCustom)
                        textField()
                            .label(message("hybris.import.settings.group.unused"))
                            .bindText(state::groupOtherCustom)
                    }.layout(RowLayout.PARENT_GRID)
                    row {
                        icon(HybrisIcons.EXTENSION_PLATFORM)
                        textField()
                            .label(message("hybris.import.settings.group.platform"))
                            .bindText(state::groupPlatform)
                        textField()
                            .label(message("hybris.import.settings.group.nonhybris"))
                            .bindText(state::groupNonHybris)
                    }.layout(RowLayout.PARENT_GRID)
                    row {
                        icon(HybrisIcons.MODULE_CCV2_GROUP)
                        textField()
                            .label(message("hybris.import.settings.group.ccv2"))
                            .bindText(state::groupCCv2)
                    }.layout(RowLayout.PARENT_GRID)
                }.visibleIf(groupModulesCheckBox.selected)
                row {
                    externalModulesCheckBox = checkBox("Group external modules")
                        .bindSelected(state::groupExternalModules)
                        .comment(message("hybris.project.view.external.module.tooltip"))
                        .component

                }
                indent {
                    row {
                        icon(HybrisIcons.MODULE_EXTERNAL_GROUP)
                        textField()
                            .label("External modules:")
                            .bindText(state::groupNameExternalModules)
                            .enabledIf(externalModulesCheckBox.selected)
                    }
                }
            }

            group(message("hybris.import.settings.junk.directory.name"), false) {
                row {
                    cell(junkList)
                        .align(AlignX.FILL)
                        .onApply { state.junkDirectoryList = junkList.data }
                        .onReset { junkList.data = state.junkDirectoryList }
                        .onIsModified { junkList.data.equalsIgnoreOrder(state.junkDirectoryList).not() }
                }
            }

            group(message("hybris.import.settings.exclude.resources.name"), false) {
                row {
                    comment("Use SAP Commerce extension name, not fully qualified IDEA module name.")
                }
                row {
                    cell(excludeResources)
                        .align(AlignX.FILL)
                        .onApply { state.extensionsResourcesToExclude = excludeResources.data }
                        .onReset { excludeResources.data = state.extensionsResourcesToExclude }
                        .onIsModified { excludeResources.data.equalsIgnoreOrder(state.extensionsResourcesToExclude).not() }
                }
            }

            group(message("hybris.import.settings.excludedFromIndex.directory.name"), false) {
                row {
                    cell(excludeFromIndex)
                        .align(AlignX.FILL)
                        .onApply { state.excludedFromIndexList = excludeFromIndex.data }
                        .onReset { excludeFromIndex.data = state.excludedFromIndexList }
                        .onIsModified { excludeFromIndex.data.equalsIgnoreOrder(state.excludedFromIndexList).not() }
                }
            }
        }
    }
}