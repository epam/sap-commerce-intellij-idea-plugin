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

package com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.pojo.RegionEntity
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.RegionEntityService
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.services.RegionService
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui.listeners.HybrisConsoleEventListener
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui.listeners.HybrisConsoleQueryBodyDocumentListener
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui.listeners.HybrisConsoleQueryPanelEventManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JPanel

class HybrisConsoleQueryPanel(private val project: Project, private val console: HybrisConsole, region: String) : JPanel() {

    private val saveButton = JButton(HybrisIcons.SAVE_ALL)
    private val loadButton = JButton(HybrisIcons.UPLOAD)
    private val removeButton = JButton(HybrisIcons.CANCEL)

    private val regionEntityService = RegionEntityService.getInstance(project)
    private val regionService = RegionService.getInstance(project)
    private val region = regionService.findOrCreate(region)

    private var regionEntitiesComboBox = ComboBox<RegionEntity<*>>(emptyArray(), 200)

    private val queryNamePlaceholder = "Query Name (max length: 25)"

    private val queryNameTextField = HybrisConsoleQueryTextField(queryNamePlaceholder)

    private val maxPossibleItemsNumber = this.region.maxNumberEntities
    private val maxQueryBodyLength = 300
    private val maxQueryNameLength = 25

    init {
        addComponentsToPanel()
    }

    private fun addComponentsToPanel() {
        HybrisConsoleQueryPanelEventManager.getInstance(project).addListener(HybrisConsoleQueryPanelListener())
        addComboBox()
        saveLastQuery()
        loadQueryBodyToConsole()
        removeQuery()
    }

    private fun addComboBox() {
        regionEntitiesComboBox = ComboBox(regionEntityService.getAll(this.region.name).values.toTypedArray(), 150)
        regionEntitiesComboBox.renderer = RegionEntityCellRenderer()
        add(regionEntitiesComboBox,0)
        regionEntitiesComboBox.addActionListener {
            run {
                removeButton.isEnabled = regionEntitiesComboBox.selectedIndex != -1
                loadButton.isEnabled = regionEntitiesComboBox.selectedIndex != -1
            }
        }
        
        addRegionEntitiesToComboBox()
    }

    private fun addRegionEntitiesToComboBox() {
        if (regionEntitiesComboBox.itemCount == 0)
            regionEntitiesComboBox.model = DefaultComboBoxModel<RegionEntity<*>>(regionEntityService.getAll(this.region.name).values.toTypedArray())

    }

    private fun saveLastQuery() {
        addQueryTextFieldAndSaveButton()
        console.editorDocument.addDocumentListener(HybrisConsoleQueryBodyDocumentListener(saveButton, queryNameTextField, queryNamePlaceholder))
        addListenerToSaveButton()
    }

    private fun addQueryTextFieldAndSaveButton() {
        queryNameTextField.border = JBUI.Borders.empty()
        queryNameTextField.setPlaceholder(queryNamePlaceholder, console, saveButton)
        queryNameTextField.preferredSize = Dimension(170, 20)
        queryNameTextField.addKeyListener(RemoveTextKeyAdapter())
        add(queryNameTextField)
        saveButton.border = JBUI.Borders.empty()
        saveButton.toolTipText = "Save Last Query"
        saveButton.preferredSize = Dimension(30, 25)
        add(saveButton)
    }

    private fun addListenerToSaveButton() {
        saveButton.addActionListener {
            run {
                if (queryNameTextField.text.length <= maxQueryNameLength) {
                    if (console.editorDocument.text.length <= maxQueryBodyLength) {
                        addQueryToComboBox()
                    } else {
                        Notifications.create(NotificationType.WARNING,
                            HybrisI18NBundleUtils.message("hybris.notification.query.body.title"),
                            HybrisI18NBundleUtils.message("hybris.notification.query.body.content", maxQueryBodyLength)
                        )
                            .notify(project)
                    }
                } else {
                    Notifications.create(NotificationType.WARNING,
                        HybrisI18NBundleUtils.message("hybris.notification.query.name.title"),
                        HybrisI18NBundleUtils.message("hybris.notification.query.name.content", maxQueryNameLength)
                    )
                        .notify(project)
                }
            }
        }
    }

    private fun addQueryToComboBox() {
        val savedEntity = regionEntityService.save(region.name, queryNameTextField.text, console.editorDocument.text)
        regionEntitiesComboBox.addItem(savedEntity)
        while (regionEntitiesComboBox.itemCount > maxPossibleItemsNumber) {
            regionEntitiesComboBox.removeItemAt(0)
        }
        queryNameTextField.setPlaceholder(queryNamePlaceholder, console, saveButton)
        saveButton.isEnabled = false
    }

    private fun loadQueryBodyToConsole() {
        loadButton.border = JBUI.Borders.empty()
        loadButton.toolTipText = "Load Selected Query"
        loadButton.preferredSize = Dimension(30, 25)
        loadButton.isEnabled = false
        loadButton.addActionListener {
            run {
                val selectedEntity = this.regionEntitiesComboBox.selectedItem as RegionEntity<*>
                console.setInputText(selectedEntity.body as String)
            }
        }
        add(loadButton)
    }

    private fun removeQuery() {
        removeButton.border = JBUI.Borders.empty()
        removeButton.toolTipText = "Remove Selected Query"
        removeButton.preferredSize = Dimension(30, 25)
        removeButton.isEnabled = false
        add(removeButton)
        removeButton.addActionListener {
            run {
                val selectedEntity = this.regionEntitiesComboBox.selectedItem as RegionEntity<*>
                regionEntityService.remove(selectedEntity.id)
                regionEntitiesComboBox.removeItem(selectedEntity)
            }
        }
    }

    private inner class RemoveTextKeyAdapter : KeyAdapter() {
        override fun keyReleased(e: KeyEvent?) {
            if (e != null && (e.keyCode == KeyEvent.VK_BACK_SPACE ||
                            e.keyCode == KeyEvent.VK_DELETE) && queryNameTextField.text.isNotEmpty()) {
                queryNameTextField.text = ""
            }
        }
    }

    private inner class HybrisConsoleQueryPanelListener : HybrisConsoleEventListener {
        override fun update() {
            addRegionEntitiesToComboBox()
        }
    }
}