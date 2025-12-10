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

package sap.commerce.toolset.logging.custom

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.logging.CxLogLevel
import sap.commerce.toolset.logging.custom.settings.CxCustomLogTemplatesSettings
import sap.commerce.toolset.logging.custom.settings.event.CxCustomLogTemplateStateListener
import sap.commerce.toolset.logging.custom.settings.state.CxCustomLogTemplateState
import sap.commerce.toolset.logging.custom.settings.state.CxCustomLoggerState
import sap.commerce.toolset.logging.presentation
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation

@Service(Service.Level.PROJECT)
class CxCustomLogTemplateService(private val project: Project, private val coroutineScope: CoroutineScope) {

    fun getTemplates() = CxCustomLogTemplatesSettings.getInstance(project).templates
        .map { it.presentation() }

    fun addTemplate(template: CxCustomLogTemplateState) {
        with(CxCustomLogTemplatesSettings.getInstance(project)) {
            templates = templates + template
        }

        project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onTemplateUpdated(template.uuid)
    }

    fun updateCustomTemplate(template: CxCustomLogTemplateState) {
        updateCustomLoggerTemplateInternal(template)
        project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onTemplateUpdated(template.uuid)
    }

    fun deleteCustomTemplates(templateIds: List<String>) {
        if (templateIds.isEmpty()) return

        with(CxCustomLogTemplatesSettings.getInstance(project)) {
            templates = templates.filter { !templateIds.contains(it.uuid) }
        }

        project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onTemplateDeleted()
    }

    fun deleteCustomTemplate(templateId: String) {
        with(CxCustomLogTemplatesSettings.getInstance(project)) {
            templates = templates.filter { it.uuid != templateId }
        }

        project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onTemplateDeleted()
    }

    fun addCustomLogger(templateUUID: String, logger: String, effectiveLevel: CxLogLevel) = with(CxCustomLogTemplatesSettings.getInstance(project)) {
        val loggerTemplateState = templates
            .find { it.uuid == templateUUID }
            ?.mutable()
            ?.apply {
                val newItem = CxCustomLoggerState(effectiveLevel, logger).mutable()
                val newLoggerConfigs = loggers.get().toMutableList()
                    .apply { add(newItem) }

                loggers.set(newLoggerConfigs)
            }
            ?.immutable()
            ?: return@with

        updateCustomLoggerTemplateInternal(loggerTemplateState)

        coroutineScope.launch {
            val modifiedTemplate = loggerTemplateState.presentation()
            project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onLoggerUpdated(modifiedTemplate)
        }
    }

    fun deleteCustomLogger(templateUUID: String, loggerName: String) = with(CxCustomLogTemplatesSettings.getInstance(project)) {
        val loggerTemplateState = templates
            .find { it.uuid == templateUUID }
            ?.mutable()
            ?.apply {
                val newLoggerConfigs = loggers.get()
                    .filter { logger -> logger.name.get() != loggerName }

                loggers.set(newLoggerConfigs)
            }
            ?.immutable()
            ?: return@with

        updateCustomLoggerTemplateInternal(loggerTemplateState)

        coroutineScope.launch {
            val modifiedTemplate = loggerTemplateState.presentation()
            project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onLoggerDeleted(modifiedTemplate)
        }
    }

    fun updateCustomLogger(templateUUID: String, loggerName: String, effectiveLevel: CxLogLevel) = with(CxCustomLogTemplatesSettings.getInstance(project)) {
        val loggerTemplateState = templates
            .find { it.uuid == templateUUID }
            ?.mutable()
            ?.apply {
                loggers.get().find { logger -> logger.name.get() == loggerName }
                    ?.effectiveLevel
                    ?.set(effectiveLevel)
            }
            ?.immutable()
            ?: return@with

        updateCustomLoggerTemplateInternal(loggerTemplateState)

        coroutineScope.launch {
            val modifiedTemplate = loggerTemplateState.presentation()
            project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onLoggerUpdated(modifiedTemplate)
        }
    }

    fun findCustomTemplate(templateUUID: String) = CxCustomLogTemplatesSettings.getInstance(project)
        .templates
        .find { it.uuid == templateUUID }

    fun createTemplateFromLoggers(connectionName: String, loggers: Map<String, CxLoggerPresentation>) = loggers.values
        .map { CxCustomLoggerState(it.level, it.name) }
        .let {
            CxCustomLogTemplateState(
                name = generateCustomTemplateName(connectionName),
                defaultEffectiveLevel = CxLogLevel.INFO,
                loggers = it
            )
        }

    private fun generateCustomTemplateName(connectionName: String): String {
        val customTemplateName = "Remote '$connectionName' | template"
        val count = CxCustomLogTemplatesSettings.getInstance(project).templates.count { it.name.startsWith(customTemplateName) }

        return if (count >= 1) "$customTemplateName ($count)"
        else customTemplateName
    }

    private fun updateCustomLoggerTemplateInternal(template: CxCustomLogTemplateState) {
        with(CxCustomLogTemplatesSettings.getInstance(project)) {
            templates = templates.toMutableList().apply {
                val position = indexOfFirst { it.uuid == template.uuid }
                removeIf { it.uuid == template.uuid }
                add(position, template)
            }
        }
    }

    companion object {
        fun getInstance(project: Project): CxCustomLogTemplateService = project.getService(CxCustomLogTemplateService::class.java)
    }
}