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

package sap.commerce.toolset.logging

import com.google.gson.Gson
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.ResourceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.logging.bundled.CxBundledLogTemplates
import sap.commerce.toolset.logging.custom.settings.CxCustomLogTemplatesSettings
import sap.commerce.toolset.logging.custom.settings.event.CxCustomLogTemplateStateListener
import sap.commerce.toolset.logging.custom.settings.state.CxCustomLogTemplateState
import sap.commerce.toolset.logging.custom.settings.state.CxCustomLoggerState
import sap.commerce.toolset.logging.presentation.CxLogTemplatePresentation
import java.io.InputStreamReader

@Service(Service.Level.PROJECT)
class CxLogService(private val project: Project, private val coroutineScope: CoroutineScope) {

    fun bundledTemplates(): List<CxLogTemplatePresentation> = ResourceUtil.getResourceAsStream(
        this.javaClass.classLoader,
        "cx-loggers",
        "templates.json"
    )
        .use { input ->
            InputStreamReader(input, Charsets.UTF_8).use { reader ->
                Gson().fromJson(reader, CxBundledLogTemplates::class.java)
            }
        }
        .templates
        .takeIf { it.isNotEmpty() }
        ?.map { it.presentation(project) }
        ?: emptyList()

    fun customTemplates() = CxCustomLogTemplatesSettings.getInstance(project).templates
        .map { it.presentation(project) }

    fun addTemplate(template: CxCustomLogTemplateState) {
        with(CxCustomLogTemplatesSettings.getInstance(project)) {
            templates = templates + template
        }

        project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onTemplateUpdated(template.uuid)
    }

    fun updateTemplate(template: CxCustomLogTemplateState) {
        updateCustomLoggerTemplateInternal(template)
        project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onTemplateUpdated(template.uuid)
    }

    fun deleteTemplate(templateId: String) {
        with(CxCustomLogTemplatesSettings.getInstance(project)) {
            templates = templates.filter { it.uuid != templateId }
        }

        project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onTemplateDeleted()
    }

    fun addLogger(templateUUID: String, logger: String, effectiveLevel: CxLogLevel) = with(CxCustomLogTemplatesSettings.getInstance(project)) {
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
            val modifiedTemplate = loggerTemplateState.presentation(project)
            project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onLoggerUpdated(modifiedTemplate)
        }
    }

    fun deleteLogger(templateUUID: String, loggerName: String) = with(CxCustomLogTemplatesSettings.getInstance(project)) {
        val loggerTemplateState = templates
            .find { it.uuid == templateUUID }
            ?.mutable()
            ?.apply {
                //remove logger
                val newLoggerConfigs = loggers.get().filter { logger -> logger.name.get() != loggerName }

                loggers.set(newLoggerConfigs)
            }
            ?.immutable()
            ?: return@with

        updateCustomLoggerTemplateInternal(loggerTemplateState)

        coroutineScope.launch {
            val modifiedTemplate = loggerTemplateState.presentation(project)
            project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onLoggerUpdated(modifiedTemplate)
        }
    }

    fun updateLogger(templateUUID: String, loggerName: String, effectiveLevel: CxLogLevel) = with(CxCustomLogTemplatesSettings.getInstance(project)) {
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
            val modifiedTemplate = loggerTemplateState.presentation(project)
            project.messageBus.syncPublisher(CxCustomLogTemplateStateListener.TOPIC).onLoggerUpdated(modifiedTemplate)
        }
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
        fun getInstance(project: Project): CxLogService = project.getService(CxLogService::class.java)
    }
}