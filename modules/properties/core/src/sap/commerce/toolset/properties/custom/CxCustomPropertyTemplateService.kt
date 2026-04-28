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

package sap.commerce.toolset.properties.custom

import com.intellij.ide.SaveAndSyncHandler
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.custom.settings.CxCustomPropertyTemplatesSettings
import sap.commerce.toolset.properties.custom.settings.event.CxCustomPropertyTemplateStateListener
import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyState
import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyTemplateState
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.properties.presentation.CxPropertyTemplatePresentation

@Service(Service.Level.PROJECT)
class CxCustomPropertyTemplateService(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) {
    fun getTemplates() = CxCustomPropertyTemplatesSettings.getInstance(project).templates.map { it.presentation() }

    fun addTemplate(template: CxCustomPropertyTemplateState) {
        with(CxCustomPropertyTemplatesSettings.getInstance(project)) {
            templates = templates + template
        }
        scheduleSave()
        project.messageBus.syncPublisher(CxCustomPropertyTemplateStateListener.TOPIC).onTemplateUpdated(template.uuid)
    }

    fun updateTemplate(template: CxCustomPropertyTemplateState) {
        updateTemplateState(template)
        scheduleSave()
        project.messageBus.syncPublisher(CxCustomPropertyTemplateStateListener.TOPIC).onTemplateUpdated(template.uuid)
    }

    fun deleteTemplates(templateIds: List<String>) {
        if (templateIds.isEmpty()) return
        with(CxCustomPropertyTemplatesSettings.getInstance(project)) {
            templates = templates.filter { it.uuid !in templateIds }
        }
        scheduleSave()
        project.messageBus.syncPublisher(CxCustomPropertyTemplateStateListener.TOPIC).onTemplatesDeleted()
    }

    fun addProperty(templateUUID: String, key: String, value: String) {
        val normalizedKey = key.trim()
        if (!isValidPropertyKey(normalizedKey)) return

        val template = CxCustomPropertyTemplatesSettings.getInstance(project).templates
            .find { it.uuid == templateUUID }
            ?.mutable()
            ?.apply {
                val newProperties = properties.get().toMutableList()
                val existingIndex = newProperties.indexOfFirst { it.key.get() == normalizedKey }
                val newItem = CxCustomPropertyState(normalizedKey, value).mutable()

                if (existingIndex >= 0) {
                    newProperties[existingIndex] = newItem
                } else {
                    newProperties.add(newItem)
                }

                properties.set(newProperties)
            }
            ?.immutable()
            ?: return

        updateTemplateState(template)
        scheduleSave()
        publishPropertyUpdated(template)
    }

    fun updateProperty(templateUUID: String, key: String, value: String) {
        val template = CxCustomPropertyTemplatesSettings.getInstance(project).templates
            .find { it.uuid == templateUUID }
            ?.mutable()
            ?.apply {
                properties.get()
                    .find { it.key.get() == key }
                    ?.value
                    ?.set(value)
            }
            ?.immutable()
            ?: return

        updateTemplateState(template)
        scheduleSave()
        publishPropertyUpdated(template)
    }

    fun deleteProperty(templateUUID: String, key: String) {
        val template = CxCustomPropertyTemplatesSettings.getInstance(project).templates
            .find { it.uuid == templateUUID }
            ?.mutable()
            ?.apply {
                properties.set(properties.get().filterNot { it.key.get() == key })
            }
            ?.immutable()
            ?: return

        updateTemplateState(template)
        scheduleSave()
        coroutineScope.launch {
            project.messageBus.syncPublisher(CxCustomPropertyTemplateStateListener.TOPIC)
                .onPropertyDeleted(template.presentation())
        }
    }

    fun findTemplate(templateUUID: String) = CxCustomPropertyTemplatesSettings.getInstance(project).templates
        .find { it.uuid == templateUUID }

    fun createTemplateFromProperties(templateName: String, properties: Collection<CxPropertyPresentation>) =
        CxCustomPropertyTemplateState(
            name = templateName,
            properties = properties.map { CxCustomPropertyState(it.key, it.value) },
        )

    private fun updateTemplateState(template: CxCustomPropertyTemplateState) {
        with(CxCustomPropertyTemplatesSettings.getInstance(project)) {
            templates = templates.toMutableList().apply {
                val position = indexOfFirst { it.uuid == template.uuid }
                removeIf { it.uuid == template.uuid }
                add(if (position >= 0) position else size, template)
            }
        }
    }

    private fun publishPropertyUpdated(template: CxCustomPropertyTemplateState) {
        coroutineScope.launch {
            project.messageBus.syncPublisher(CxCustomPropertyTemplateStateListener.TOPIC)
                .onPropertyUpdated(template.presentation())
        }
    }

    private fun scheduleSave() {
        SaveAndSyncHandler.getInstance().scheduleProjectSave(project)
    }

    private fun isValidPropertyKey(key: String): Boolean = key.isNotBlank() && !key.any(Char::isWhitespace)

    companion object {
        fun getInstance(project: Project): CxCustomPropertyTemplateService =
            project.getService(CxCustomPropertyTemplateService::class.java)
    }
}

private fun CxCustomPropertyTemplateState.presentation() = CxPropertyTemplatePresentation(
    uuid = uuid,
    name = name,
    properties = properties.map { CxPropertyPresentation.of(it.key, it.value) },
    icon = HybrisIcons.Log.Template.CUSTOM,
)
