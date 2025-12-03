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

package sap.commerce.toolset.logging.template

import com.google.gson.Gson
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.ResourceUtil
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.logging.CxLoggerModel
import sap.commerce.toolset.logging.event.CxCustomLoggerTemplateStateListener
import sap.commerce.toolset.logging.resolveIconBlocking
import sap.commerce.toolset.logging.resolvePsiElementPointerBlocking
import sap.commerce.toolset.logging.settings.CxLoggerTemplatesSettings
import sap.commerce.toolset.logging.state.CxCustomLoggerConfig
import sap.commerce.toolset.logging.state.CxCustomLoggerTemplateState
import java.io.InputStreamReader

@Service(Service.Level.PROJECT)
class CxLoggersTemplatesService(private val project: Project) {

    private val iconsMap = mapOf(
        "DISABLE" to HybrisIcons.Log.Template.DISABLE,
        "ENABLE" to HybrisIcons.Log.Template.ENABLE
    )

    fun bundledLoggerTemplates(): List<CxLoggersTemplateModel> = ResourceUtil.getResourceAsStream(
        this.javaClass.classLoader,
        "cx-loggers",
        "templates.json"
    )
        .use { input ->
            InputStreamReader(input, Charsets.UTF_8).use { reader ->
                Gson().fromJson(reader, CxLoggersTemplatesDto::class.java)
            }
        }
        .templates
        .takeIf { it.isNotEmpty() }
        ?.map { item ->
            CxLoggersTemplateModel(
                name = item.name,
                loggers = item.loggers
                    .map { logConfig ->
                        val icon = logConfig.resolveIconBlocking(project)
                        val pointer = logConfig.resolvePsiElementPointerBlocking(project)

                        CxLoggerModel.of(
                            name = logConfig.identifier,
                            effectiveLevel = logConfig.effectiveLevel,
                            icon = icon,
                            psiElementPointer = pointer
                        )
                    },
                icon = item.iconName
                    ?.let { iconsMap.getOrElse(it) { HybrisIcons.Log.Template.DEFAULT } }
                    ?: HybrisIcons.Log.Template.DEFAULT

            )
        }
        ?: emptyList()

    fun customLoggerTemplates() = CxLoggerTemplatesSettings.getInstance(project)
        .customLoggerTemplates
        .map { templateState ->
            createLoggerTemplateModel(templateState)
        }

    fun addCustomLoggerTemplate(template: CxCustomLoggerTemplateState) {
        with(CxLoggerTemplatesSettings.getInstance(project)) {
            customLoggerTemplates = customLoggerTemplates + template
        }

        project.messageBus.syncPublisher(CxCustomLoggerTemplateStateListener.TOPIC).onLoggerTemplatesUpdated()
    }

    fun updateCustomLoggerTemplate(template: CxCustomLoggerTemplateState) {

        updateCustomLoggerTemplateInternal(template)

        project.messageBus.syncPublisher(CxCustomLoggerTemplateStateListener.TOPIC).onLoggerTemplatesUpdated()
    }

    private fun updateCustomLoggerTemplateInternal(template: CxCustomLoggerTemplateState) {
        with(CxLoggerTemplatesSettings.getInstance(project)) {
            customLoggerTemplates = customLoggerTemplates.toMutableList().apply {
                val position = indexOfFirst { it.uuid == template.uuid }
                removeIf { it.uuid == template.uuid }
                add(position, template)
            }
        }
    }

    fun addLogger(templateUUID: String, logger: String, effectiveLevel: String) {
        with(CxLoggerTemplatesSettings.getInstance(project)) {
            val loggerTemplate = customLoggerTemplates
                .find { it.uuid == templateUUID }
                ?.mutable()
                ?.apply {
                    val newItem = CxCustomLoggerConfig(effectiveLevel, logger).mutable()
                    val newLoggerConfigs = loggers.get().toMutableList()
                        .apply { add(newItem) }

                    loggers.set(newLoggerConfigs)
                }
                ?.immutable()
                ?: return@with

            updateCustomLoggerTemplateInternal(loggerTemplate)

            val modifiedLoggerTemplate = createLoggerTemplateModel(loggerTemplate)

            project.messageBus.syncPublisher(CxCustomLoggerTemplateStateListener.TOPIC).onLoggerTemplateUpdated(modifiedLoggerTemplate)
        }
    }

    private fun createLoggerTemplateModel(loggerTemplate: CxCustomLoggerTemplateState): CxLoggersTemplateModel = CxLoggersTemplateModel(
        uuid = loggerTemplate.uuid,
        name = loggerTemplate.name,
        loggers = loggerTemplate.loggers
            .map { loggerState -> CxLoggerModel.of(loggerState.name, loggerState.effectiveLevel) }
            .toList(),
        icon = HybrisIcons.Log.Template.CUSTOM_TEMPLATE
    )

    fun deleteCustomTemplate(templateId: String) {
        with(CxLoggerTemplatesSettings.getInstance(project)) {
            customLoggerTemplates = customLoggerTemplates.filter { it.uuid != templateId }
        }

        project.messageBus.syncPublisher(CxCustomLoggerTemplateStateListener.TOPIC).onLoggerTemplatesUpdated()
    }

    companion object {
        fun getInstance(project: Project): CxLoggersTemplatesService = project.getService(CxLoggersTemplatesService::class.java)
    }
}