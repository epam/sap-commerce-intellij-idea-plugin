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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.common.equalsIgnoreOrder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.ui.CRUDListPanel
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class HybrisProjectSettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.title"), "hybris.project.settings"
    ) {

        private val state = HybrisProjectSettingsComponent.getInstance(project).state
        private lateinit var generateCodeOnRebuildCheckBox: JCheckBox

        private val excludedFromScanning = CRUDListPanel(
            "hybris.import.settings.excludedFromScanning.directory.popup.add.title",
            "hybris.import.settings.excludedFromScanning.directory.popup.add.text",
            "hybris.import.settings.excludedFromScanning.directory.popup.edit.title",
            "hybris.import.settings.excludedFromScanning.directory.popup.edit.text",
        )

        override fun createPanel() = panel {
            group(message("hybris.settings.project.details.title")) {
                row(message("hybris.settings.project.details.platform_version.title")) {
                    textField()
                        .enabled(false)
                        .text(state.hybrisVersion ?: "")
                        .align(AlignX.FILL)
                }.layout(RowLayout.PARENT_GRID)
                row(message("hybris.import.wizard.hybris.distribution.directory.label")) {
                    textField()
                        .enabled(false)
                        .text(state.hybrisDirectory ?: "")
                        .align(AlignX.FILL)
                }.layout(RowLayout.PARENT_GRID)
                row(message("hybris.import.wizard.javadoc.url.label")) {
                    textField()
                        .enabled(false)
                        .text(state.javadocUrl ?: "")
                        .align(AlignX.FILL)
                }.layout(RowLayout.PARENT_GRID)
            }

            group(message("hybris.settings.project.build.title")) {
                row {
                    generateCodeOnRebuildCheckBox = checkBox("Generate code before the Rebuild Project action")
                        .comment(
                            """
                            If checked, beans and models will be re-generated to the <strong>boostrap/gensrc</strong> before the compilation process.<br>
                            Once generated, compilation will be triggered and create class files which will be placed under <strong>boostrap/modelclasses</strong>.<br>
                            After that, <strong>models.jar</strong> will be created from the <strong>boostrap/modelclasses</strong> folder.<br>
                            As a final step, project compilation will continue.
                        """.trimIndent()
                        )
                        .bindSelected(state::generateCodeOnRebuild)
                        .component
                }
                row("Code generation timeout (in seconds):") {
                    spinner(1..10000, 1)
                        .bindIntValue(state::generateCodeTimeoutSeconds)
                        .enabledIf(generateCodeOnRebuildCheckBox.selected)
                }
            }

            group(message("hybris.settings.project.common.title")) {
                row {
                    checkBox("Show complete Module name in the Project View")
                        .comment("If checked, complete module name will be represented as <code>[Platform.core]</code> instead of <code>core</code>.")
                        .bindSelected(state::showFullModuleName)
                }
            }

            group(message("hybris.settings.project.refresh.title")) {
                row {
                    checkBox("Remove external modules")
                        .comment("If checked, non SAP Commerce external modules will be removed during the project refresh.")
                        .bindSelected(state::removeExternalModulesOnRefresh)
                }
                row {
                    checkBox(message("hybris.import.wizard.import.ootb.modules.read.only.label"))
                        .comment(message("hybris.import.wizard.import.ootb.modules.read.only.tooltip"))
                        .bindSelected(state::importOotbModulesInReadOnlyMode)
                }
                row {
                    checkBox(message("hybris.import.wizard.exclude.test.sources.label"))
                        .bindSelected(state::excludeTestSources)
                }
                row {
                    checkBox(message("hybris.project.import.followSymlink"))
                        .bindSelected(state::followSymlink)
                }
                row {
                    checkBox(message("hybris.project.import.scanExternalModules"))
                        .bindSelected(state::scanThroughExternalModule)
                }
                row {
                    checkBox(message("hybris.project.import.importCustomAntBuildFiles"))
                        .bindSelected(state::importCustomAntBuildFiles)
                }
            }

            group("Directories excluded from the project scanning", false) {
                row {
                    comment("Specify directories related to the project root, use '/' separator for sub-directories.")
                }
                row {
                    cell(excludedFromScanning)
                        .align(AlignX.FILL)
                        .onApply { state.excludedFromScanning = excludedFromScanning.data.toMutableSet() }
                        .onReset { excludedFromScanning.data = state.excludedFromScanning.toList() }
                        .onIsModified { excludedFromScanning.data.equalsIgnoreOrder(state.excludedFromScanning.toList()).not() }
                }
            }
        }
    }
}
