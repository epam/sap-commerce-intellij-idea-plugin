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

package sap.commerce.toolset.groovy.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.actionSystem.HybrisEditorToolbarProvider
import sap.commerce.toolset.groovy.actionSystem.GroovyEditorToolbarProvider
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.settings.state.SpringContextMode
import sap.commerce.toolset.settings.state.TransactionMode
import sap.commerce.toolset.settings.yDeveloperSettings
import javax.swing.JCheckBox

class GroovyProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject && Plugin.GROOVY.isActive()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        i18n("hybris.settings.project.groovy.title"), "hybris.groovy.settings"
    ) {

        private val developerSettings = project.yDeveloperSettings
        private val mutable = developerSettings.groovySettings.mutable()
        private lateinit var enableActionToolbar: JCheckBox
        private val toolbarProvider by lazy {
            HybrisEditorToolbarProvider.EP.findExtensionOrFail(GroovyEditorToolbarProvider::class.java)
        }

        override fun createPanel() = panel {
            row {
                comboBox(
                    EnumComboBoxModel(SpringContextMode::class.java),
                    renderer = SimpleListCellRenderer.create("?") { it.presentationText }
                )
                    .label("Spring context mode:")
                    .comment("""
                        Defines default mode for each new Editor.<br>
                        <cod>${SpringContextMode.LOCAL.presentationText}</code> mode enables direct resolution of the Spring beans, so it will be possible to enable code completion for such statements <code>productService.getProduct(..)</code>. 
                    """.trimIndent())
                    .bindItem(mutable::springContextMode.toNullableProperty(SpringContextMode.DISABLED))
            }
                .layout(RowLayout.PARENT_GRID)

            row {
                comboBox(
                    EnumComboBoxModel(TransactionMode::class.java),
                    renderer = SimpleListCellRenderer.create("?") { it.presentationText }
                )
                    .label("Transaction mode:")
                    .comment("""
                        Defines default mode for each new Editor. 
                    """.trimIndent())
                    .bindItem(mutable::transactionMode.toNullableProperty(TransactionMode.ROLLBACK))
            }
                .layout(RowLayout.PARENT_GRID)

            separator()

            row {
                enableActionToolbar = checkBox("Enable actions toolbar for each Groovy file")
                    .bindSelected(mutable::enableActionsToolbar)
                    .comment("Actions toolbar enables possibility to change current remote SAP Commerce session and perform operations on current file, such as `Execute on remote server`")
                    .component
            }
            row {
                checkBox("Enable actions toolbar for a Test Groovy file")
                    .bindSelected(mutable::enableActionsToolbarForGroovyTest)
                    .comment("Enables Actions toolbar for the groovy files located in the <strong>${HybrisConstants.TEST_SRC_DIRECTORY}</strong> or <strong>${HybrisConstants.GROOVY_TEST_SRC_DIRECTORY}</strong> directory.")
                    .enabledIf(enableActionToolbar.selected)
            }
            row {
                checkBox("Enable actions toolbar for a IDE Groovy scripts")
                    .bindSelected(mutable::enableActionsToolbarForGroovyIdeConsole)
                    .comment("Enables Actions toolbar for the groovy files located in the <strong>${HybrisConstants.IDE_CONSOLES_PATH}</strong> (In Project View, Scratches and Consoles -> IDE Consoles).")
                    .enabledIf(enableActionToolbar.selected)
            }
        }

        override fun apply() {
            super.apply()
            developerSettings.groovySettings = mutable.immutable()
            toolbarProvider.toggle(project)
        }
    }
}