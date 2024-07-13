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

import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.utils.Plugin.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.extensions.PluginId
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.JButton
import javax.swing.JList
import javax.swing.ListModel

class CheckRequiredPluginsWizardStep(context: WizardContext) : ProjectImportWizardStep(context) {

    private val ultimateEditionOnly by lazy {
        listOf(
            DATABASE,
            SPRING,
            JAVAEE,
            JAVAEE_WEB,
            JAVAEE_EL,
            DIAGRAM
        )
            .map { it.id }
    }
    private val plugins = mapOf(
        SPRING.id to SPRING,
        KOTLIN.id to KOTLIN,
        GROOVY.id to GROOVY,
        GRADLE.id to GRADLE,
        DATABASE.id to DATABASE,
        DIAGRAM.id to DIAGRAM,
        PROPERTIES.id to PROPERTIES,
        COPYRIGHT.id to COPYRIGHT,
        JAVAEE_EL.id to JAVAEE_EL,
        JAVAEE_WEB.id to JAVAEE_WEB,
        JAVAEE.id to JAVAEE,
        JAVAEE.id to JAVAEE,
        MAVEN.id to MAVEN,
        ANT_SUPPORT.id to ANT_SUPPORT,
        JREBEL.id to JREBEL,
        JAVASCRIPT.id to JAVASCRIPT,
        INTELLILANG.id to INTELLILANG,
    )
    private val excludedIdPrefix = "com.intellij.modules"

    private val cellRenderer = object : ColoredListCellRenderer<PluginId>() {
        @Serial
        private val serialVersionUID: Long = -7396769063069852812L

        override fun customizeCellRenderer(list: JList<out PluginId>, value: PluginId, index: Int, selected: Boolean, hasFocus: Boolean) {
            plugins[value.idString]
                ?.takeIf { it.url != null }
                ?.let {
                    append(value.idString, SimpleTextAttributes.LINK_ATTRIBUTES)
                }
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
        plugins[element.idString]
            ?.url
            ?.let { BrowserUtil.browse(it) }
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
                enablePlugins = button("Enable and Restart") { _ ->
                    if (!enablePlugins.isEnabled) return@button
                    enablePlugins.isEnabled = false
                    enablePlugins.text = "Enabling ${notEnabledModel.items.size} plugins, IDE will restart automatically.."

                    enablePlugins(notEnabledModel.items)
                }
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

        enablePlugins.isEnabled = !notEnabledModel.isEmpty

        return isAnyMissing()
    }

    private fun enablePlugins(pluginIds: Collection<PluginId>) {
        val pluginManager = PluginManager.getInstance()
        pluginIds.forEach { pluginManager.enablePlugin(it) }

        ApplicationManager.getApplication()
            .asSafely<ApplicationEx>()
            ?.restart(true)
    }

    private fun validateDependencies() {
        notInstalledModel.removeAll()
        notEnabledModel.removeAll()

        val hybrisPlugin = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID)) ?: return

        hybrisPlugin.dependencies
            .filter { it.isOptional }
            .map { it.pluginId }
            .filterNot { it.idString.startsWith(excludedIdPrefix) }
            .map { pluginId ->
                if (!PluginManager.isPluginInstalled(pluginId)) {
                    notInstalledModel.add(pluginId)
                    return@map
                }
                PluginManagerCore.getPlugin(pluginId)
                    ?.takeUnless { it.isEnabled }
                    ?.let { notEnabledModel.add(pluginId) }
            }
    }

    private fun isAnyMissing(): Boolean {
        if (!notEnabledModel.isEmpty) return true
        if (HybrisConstants.IDEA_EDITION_ULTIMATE.equals(ApplicationNamesInfo.getInstance().editionName, true)) return !notInstalledModel.isEmpty

        return notInstalledModel.items
            .any { !ultimateEditionOnly.contains(it.idString) }
    }

}