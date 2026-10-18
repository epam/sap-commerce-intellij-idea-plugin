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
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.*
import sap.commerce.toolset.properties.custom.settings.CxCustomPropertyTemplatesSettings
import sap.commerce.toolset.properties.custom.settings.event.CxCustomPropertyTemplateStateListener
import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyState
import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyTemplateState
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.properties.presentation.CxPropertyTemplatePresentation

/**
 * Changes the custom property templates.
 *
 * Every change is one atomic update of the settings, and listeners hear about it on the calling thread - the EDT
 * whenever a change comes from the tool window.
 */
@Service(Service.Level.PROJECT)
class CxCustomPropertyTemplateService(private val project: Project) {

    fun getTemplates() = CxCustomPropertyTemplatesSettings.getInstance(project).templates.map { it.presentation() }

    fun addTemplate(template: CxCustomPropertyTemplateState) {
        settings().updateTemplates { it + template }
        scheduleSave()
        publisher().onTemplateUpdated(template.uuid)
    }

    fun updateTemplate(template: CxCustomPropertyTemplateState) {
        settings().updateTemplates { it.replacing(template) }
        scheduleSave()
        publisher().onTemplateUpdated(template.uuid)
    }

    fun deleteTemplates(templateIds: List<String>) {
        if (templateIds.isEmpty()) return
        settings().updateTemplates { templates -> templates.filter { it.uuid !in templateIds } }
        scheduleSave()
        publisher().onTemplatesDeleted()
    }

    fun addProperty(templateUUID: String, key: String, value: String) {
        val normalizedKey = key.trim()
        if (!CxPropertyKey.isValid(normalizedKey)) return

        updateTemplateProperties(templateUUID) { it.withProperty(normalizedKey, value) }
            ?.let { publisher().onPropertyUpdated(it.presentation()) }
    }

    fun updateProperty(templateUUID: String, key: String, value: String) {
        updateTemplateProperties(templateUUID) { it.withValue(key, value) }
            ?.let { publisher().onPropertyUpdated(it.presentation()) }
    }

    fun deleteProperty(templateUUID: String, key: String) {
        updateTemplateProperties(templateUUID) { it.withoutProperty(key) }
            ?.let { publisher().onPropertyDeleted(it.presentation()) }
    }

    fun findTemplate(templateUUID: String) = CxCustomPropertyTemplatesSettings.getInstance(project).templates
        .find { it.uuid == templateUUID }

    /** [name], or the first `name (N)` no template is using yet. */
    fun uniqueTemplateName(name: String) = uniqueName(name, templateNames())

    /** Names the templates use, leaving out the one with [exceptUUID] - a template may keep its own name. */
    fun templateNames(exceptUUID: String? = null) = CxCustomPropertyTemplatesSettings.getInstance(project).templates
        .filter { it.uuid != exceptUUID }
        .mapTo(HashSet()) { it.name }

    fun createTemplateFromProperties(templateName: String, properties: Collection<CxPropertyPresentation>) =
        CxCustomPropertyTemplateState(
            name = templateName,
            properties = properties.map { CxCustomPropertyState(it.key, it.value) },
        )

    /** Applies [transform] to the template with [templateUUID] and returns the result, or `null` when there is none. */
    private fun updateTemplateProperties(
        templateUUID: String,
        transform: (CxCustomPropertyTemplateState) -> CxCustomPropertyTemplateState,
    ): CxCustomPropertyTemplateState? {
        var updated: CxCustomPropertyTemplateState? = null

        settings().updateTemplates { templates ->
            templates.map { template ->
                if (template.uuid == templateUUID) transform(template).also { updated = it }
                else template
            }
        }

        if (updated != null) scheduleSave()
        return updated
    }

    private fun settings() = CxCustomPropertyTemplatesSettings.getInstance(project)

    private fun publisher() = project.messageBus.syncPublisher(CxCustomPropertyTemplateStateListener.TOPIC)

    private fun scheduleSave() {
        SaveAndSyncHandler.getInstance().scheduleProjectSave(project)
    }

    companion object {
        fun getInstance(project: Project): CxCustomPropertyTemplateService = project.service()
    }
}

private fun CxCustomPropertyTemplateState.presentation() = CxPropertyTemplatePresentation(
    uuid = uuid,
    name = name,
    properties = properties.map { CxPropertyPresentation.of(it.key, it.value) },
    icon = HybrisIcons.Log.Template.CUSTOM,
)
