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

package com.intellij.idea.plugin.hybris.project.wizard

import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import javax.swing.JButton

class CheckRequiredPluginsWizardStep(context: WizardContext) : ProjectImportWizardStep(context) {

    private val ultimateEditionOnly by lazy {
        listOf(
            PluginCommon.PLUGIN_DATABASE,
            PluginCommon.PLUGIN_SPRING,
            PluginCommon.PLUGIN_JAVAEE,
            PluginCommon.PLUGIN_JAVAEE_WEB,
            PluginCommon.PLUGIN_JAVAEE_EL,
            PluginCommon.PLUGIN_DIAGRAM
        )
            .map { it.id }
    }
    private val excludedIdPrefix = "com.intellij.modules"

    private val notInstalledModel = CollectionListModel<PluginId>()
    private val notEnabledModel = CollectionListModel<PluginId>()
    private val notInstalledList = JBList(notInstalledModel).also {
        it.isEnabled = true
    }
    private val notEnabledList = JBList(notEnabledModel).also {
        it.isEnabled = true
    }
    private lateinit var enablePlugins: JButton

    override fun updateDataModel() = Unit

    override fun getComponent() = panel {
        row {
            label("WARNING")
                .bold()
                .align(Align.CENTER)
                .comment(
                    """
                        Some of the required or optional plugins are not installed or disabled.<br>
                        SAP Commerce import may not work properly.
                    """.trimIndent()
                )
                .component.also {
                    it.font = JBUI.Fonts.label(36f)
                    it.foreground = ColorUtil.withAlpha(JBColor(0x660000, 0xC93B48), 0.7)
                }
        }
        group("Following plugins are not enabled") {
            row {
                cell(notEnabledList)
            }
            row {
                enablePlugins = button("Enable and Restart") { _ -> PluginCommon.enablePlugins(notEnabledModel.items) }
                    .component
            }
        }

        group("Following plugins are not installed") {
            row {
                cell(notInstalledList)
            }
        }
    }

    override fun isStepVisible(): Boolean {
        validateDependencies()

        if (!isAnyMissing()) return false

        enablePlugins.isEnabled = !notEnabledModel.isEmpty

        return true
    }

    private fun validateDependencies() {
        notInstalledModel.removeAll()
        notEnabledModel.removeAll()

        val hybrisPlugin = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID)) ?: return

        hybrisPlugin.dependencies
            .filter { it.isOptional }
            .map { it.pluginId }
            .filterNot { it.idString.startsWith(excludedIdPrefix) }
            .map {
                if (!PluginManager.isPluginInstalled(it)) {
                    notInstalledModel.add(it)
                    return@map
                }
                val plugin = PluginManagerCore.getPlugin(it)
                if (plugin != null && !plugin.isEnabled) {
                    notEnabledModel.add(it)
                }
            }
    }

    private fun isAnyMissing(): Boolean {
        if (!notEnabledModel.isEmpty) return true
        if (HybrisConstants.IDEA_EDITION_ULTIMATE.equals(ApplicationNamesInfo.getInstance().editionName, true)) return !notInstalledModel.isEmpty()

        return notInstalledModel.items
            .any { !ultimateEditionOnly.contains(it.idString) }
    }

}