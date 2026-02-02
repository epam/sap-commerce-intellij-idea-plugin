/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import sap.commerce.toolset.equalsIgnoreOrder
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.ui.CRUDListPanel
import javax.swing.JCheckBox

class ProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(project: Project) : BoundSearchableConfigurable(
        i18n("hybris.settings.project.title"), "hybris.settings"
    ) {

        private val projectSettings = ProjectSettings.getInstance(project)
        private lateinit var generateCodeOnRebuildCheckBox: JCheckBox

        private val excludedFromScanning = CRUDListPanel(
            "hybris.import.settings.excludedFromScanning.directory.popup.add.title",
            "hybris.import.settings.excludedFromScanning.directory.popup.add.text",
            "hybris.import.settings.excludedFromScanning.directory.popup.edit.title",
            "hybris.import.settings.excludedFromScanning.directory.popup.edit.text",
        )

        override fun createPanel() = panel {
            group(i18n("hybris.settings.project.details.title")) {
                row(i18n("hybris.settings.project.details.platform_version.title")) {
                    textField()
                        .enabled(false)
                        .text(projectSettings.hybrisVersion ?: "")
                        .align(AlignX.FILL)
                }.layout(RowLayout.PARENT_GRID)

                row {
                    textField()
                        .label(i18n("hybris.import.wizard.hybris.distribution.directory.label"))
                        .enabled(false)
                        .text(projectSettings.platformRelativePath ?: "")
                        .align(AlignX.FILL)
                        .resizableColumn()

                    contextHelp(i18n("hybris.import.wizard.hybris.distribution.directory.help.description"))
                }.layout(RowLayout.PARENT_GRID)

                row {
                    textField()
                        .label(i18n("hybris.import.wizard.javadoc.url.label"))
                        .enabled(false)
                        .text(projectSettings.javadocUrl ?: "")
                        .align(AlignX.FILL)
                        .resizableColumn()

                    contextHelp(i18n("hybris.import.wizard.javadoc.url.help.description"))
                }.layout(RowLayout.PARENT_GRID)
            }

            group(i18n("hybris.settings.project.build.title")) {
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
                        .bindSelected(projectSettings::generateCodeOnRebuild)
                        .component
                }
                row {
                    checkBox("Generate code before execution of the JUnit Run Configuration")
                        .bindSelected(projectSettings::generateCodeOnJUnitRunConfiguration)
                        .enabledIf(generateCodeOnRebuildCheckBox.selected)
                }
                row {
                    checkBox("Generate code before execution of the SAP CX Server Run Configuration")
                        .bindSelected(projectSettings::generateCodeOnServerRunConfiguration)
                        .enabledIf(generateCodeOnRebuildCheckBox.selected)
                }
                row("Code generation timeout:") {
                    spinner(1..10000, 1)
                        .bindIntValue(projectSettings::generateCodeTimeoutSeconds)
                        .enabledIf(generateCodeOnRebuildCheckBox.selected)
                        .commentRight("(seconds)")
                }
            }

            group(i18n("hybris.settings.project.common.title")) {
                row {
                    checkBox("Show full Module name in the Project View")
                        .comment("If checked, complete module name will be represented as <code>[Platform.core]</code> instead of <code>core</code>.")
                        .bindSelected(projectSettings::showFullModuleName)
                }
                row {
                    checkBox(i18n("hybris.project.import.withDecompiledOotbSources"))
                        .bindSelected(projectSettings::withDecompiledOotbSources)
                    contextHelp(i18n("hybris.project.import.withDecompiledOotbSources.help.description"))
                }
            }

            group(i18n("hybris.project.import.isExcludedFromScanning"), false) {
                row {
                    comment(i18n("hybris.project.import.isExcludedFromScanning.help.description"))
                }
                row {
                    cell(excludedFromScanning)
                        .align(AlignX.FILL)
                        .onApply { projectSettings.excludedFromScanning = excludedFromScanning.data.toMutableSet() }
                        .onReset { excludedFromScanning.data = projectSettings.excludedFromScanning.toList() }
                        .onIsModified { excludedFromScanning.data.equalsIgnoreOrder(projectSettings.excludedFromScanning.toList()).not() }
                }
            }
        }
    }
}