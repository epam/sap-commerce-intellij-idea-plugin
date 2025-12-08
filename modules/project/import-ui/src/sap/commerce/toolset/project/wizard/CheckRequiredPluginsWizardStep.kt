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

package sap.commerce.toolset.project.wizard

import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.extensions.PluginId
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.Plugin
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.JList
import javax.swing.ListModel

class CheckRequiredPluginsWizardStep(context: WizardContext) : ProjectImportWizardStep(context) {

    private val declaredPluginDependencies = Plugin.entries
        .filterNot { it == Plugin.HYBRIS }
        .filterNot { it == Plugin.ULTIMATE }
        .associateBy { it.id }

    private val cellRenderer = object : ColoredListCellRenderer<PluginId>() {
        @Serial
        private val serialVersionUID: Long = -7396769063069852812L

        override fun customizeCellRenderer(list: JList<out PluginId>, value: PluginId, index: Int, selected: Boolean, hasFocus: Boolean) {
            declaredPluginDependencies[value.idString]
                ?.takeIf { it.url != null }
                ?.let { append(value.idString, SimpleTextAttributes.LINK_ATTRIBUTES) }
                ?: append(value.idString)
        }
    }
    private val notInstalledModel = CollectionListModel<PluginId>()
    private val notEnabledModel = CollectionListModel<PluginId>()
    private val notInstalledList = JBList(notInstalledModel).also {
        it.isEnabled = true
        it.cellRenderer = cellRenderer
        it.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) = openPluginUrl(e, it, notEnabledModel)
        })
    }
    private val notEnabledList = JBList(notEnabledModel).also {
        it.isEnabled = true
        it.cellRenderer = cellRenderer
        it.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) = openPluginUrl(e, it, notEnabledModel)
        })
    }

    private fun openPluginUrl(e: MouseEvent, list: JBList<PluginId>, model: ListModel<PluginId>) {
        if (e.clickCount != 1) return
        val index = list.locationToIndex(e.point)
        if (index == -1) return
        val element = model.getElementAt(index)

        declaredPluginDependencies[element.idString]
            ?.url
            ?.let { BrowserUtil.browse(it) }
    }

    override fun updateDataModel() = Unit

    override fun getComponent() = panel {
        row {
            label("WARNING")
                .bold()
                .align(Align.CENTER)
                .comment(
                    """
                        Since IntelliJ IDEA 2025.3 and the introduction of the new unified distribution model,<br>
                        it is advisable to activate your subscription (if you have one) before importing the project.
                        <br><br>
                        One or more required or optional plugins are missing or disabled.<br>
                        As a result, the SAP Commerce import may not function correctly.<br>
                        For the best experience, it is recommended to activate the additional plugins prior to importing the project.
                    """.trimIndent()
                )
                .component.also {
                    it.font = JBUI.Fonts.label(36f)
                    it.foreground = ColorUtil.withAlpha(JBColor(0x660000, 0xC93B48), 0.7)
                }
        }

        group("Not Enabled Plugins") {
            row {
                cell(notEnabledList)
            }
        }

        group("Not Installed Plugins") {
            row {
                cell(notInstalledList)
            }
        }
    }

    override fun isStepVisible(): Boolean {
        notInstalledModel.removeAll()
        notEnabledModel.removeAll()

        val hybrisPlugin = Plugin.HYBRIS.pluginDescriptor
            ?: return false

        hybrisPlugin.dependencies
            .filter { it.isOptional }
            .map { it.pluginId }.distinct()
            .onEach { pluginId ->
                if (!PluginManager.isPluginInstalled(pluginId)) {
                    notInstalledModel.add(pluginId)
                    return@onEach
                }
                PluginManagerCore.getPlugin(pluginId)
                    ?.takeIf {
                        Plugin.of(pluginId.idString)?.isDisabled()
                            ?: (!PluginManagerCore.isLoaded(pluginId) || PluginManagerCore.isDisabled(pluginId))
                    }
                    ?.let { notEnabledModel.add(pluginId) }
            }

        return !notInstalledModel.isEmpty || !notEnabledModel.isEmpty
    }

}