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

package com.intellij.idea.plugin.hybris.system.cockpitng.meta

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.*
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.*
import com.intellij.idea.plugin.hybris.system.meta.MetaModelProcessor
import com.intellij.idea.plugin.hybris.system.type.meta.impl.CaseInsensitive.CaseInsensitiveConcurrentHashMap
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement

@Service(Service.Level.PROJECT)
class CngMetaModelAggregatedProcessor(project: Project) : MetaModelProcessor<DomElement, CngMeta<DomElement>>(project) {

    private val metaConfigProcessor = project.service<CngMetaModelConfigProcessor>()
    private val metaWidgetsProcessor = project.service<CngMetaModelWidgetsProcessor>()
    private val metaActionDefinitionProcessor = project.service<CngMetaModelActionDefinitionProcessor>()
    private val metaWidgetDefinitionProcessor = project.service<CngMetaModelWidgetDefinitionProcessor>()
    private val metaEditorDefinitionProcessor = project.service<CngMetaModelEditorDefinitionProcessor>()

    override fun process(container: String, yContainer: String, fileName: String, custom: Boolean, dom: DomElement): CngMeta<DomElement> = when (dom) {
        is Config -> metaConfigProcessor.process(container, yContainer, fileName, custom, dom)
        is Widgets -> metaWidgetsProcessor.process(container, yContainer, fileName, custom, dom)
        is ActionDefinition -> metaActionDefinitionProcessor.process(container, yContainer, fileName, custom, dom)
        is WidgetDefinition -> metaWidgetDefinitionProcessor.process(container, yContainer, fileName, custom, dom)
        is EditorDefinition -> metaEditorDefinitionProcessor.process(container, yContainer, fileName, custom, dom)
        else -> null
    }
        ?.asSafely<CngMeta<DomElement>>()
        ?: error("Unknown dom type - ${dom::class.java.simpleName}")
}

@Service(Service.Level.PROJECT)
class CngMetaModelConfigProcessor(project: Project) : MetaModelProcessor<Config, CngMetaConfig>(project) {

    override fun process(container: String, yContainer: String, fileName: String, custom: Boolean, dom: Config): CngMetaConfig {
        val contexts = dom.contexts
            .mapNotNull { dom ->
                dom.component.stringValue
                    ?.takeIf { it.isNotBlank() }
                    ?.let { CngMetaContext(dom, fileName, it, custom) }
            }

        return CngMetaConfig(dom, fileName, custom, contexts)
    }
}

@Service(Service.Level.PROJECT)
class CngMetaModelWidgetsProcessor(project: Project) : MetaModelProcessor<Widgets, CngMetaWidgets>(project) {

    override fun process(container: String, yContainer: String, fileName: String, custom: Boolean, dom: Widgets): CngMetaWidgets = CngMetaWidgets(
        dom, fileName, custom,
        processWidgets(fileName, custom, dom.widgets),
        dom.widgetExtensions.mapNotNull { dom ->
            dom.widgetId.stringValue
                ?.takeIf { it.isNotBlank() }
                ?.let { CngMetaWidgetExtension(dom, fileName, custom, processWidgets(fileName, custom, dom.widgets)) }
        }
    )

    private fun processWidgets(
        fileName: String,
        custom: Boolean,
        widgets: List<Widget>
    ): List<CngMetaWidget> = widgets
        .mapNotNull { dom ->
            val id = dom.id
                // if ID is null we may need to re-index the project, faced such issue due broken Stubs
                .takeIf { it.exists() }
                ?.stringValue
                ?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

            val subWidgets = if (dom.widgets.isNotEmpty()) processWidgets(fileName, custom, dom.widgets)
            else emptyList()
            CngMetaWidget(dom, fileName, id, custom, subWidgets)
        }
}

@Service(Service.Level.PROJECT)
class CngMetaModelActionDefinitionProcessor(project: Project) : MetaModelProcessor<ActionDefinition, CngMetaActionDefinition?>(project) {

    override fun process(container: String, yContainer: String, fileName: String, custom: Boolean, dom: ActionDefinition) = CngMetaModelNameProvider
        .extract(dom)
        ?.let { id -> CngMetaActionDefinition(dom, fileName, id, custom) }
}

@Service(Service.Level.PROJECT)
class CngMetaModelEditorDefinitionProcessor(project: Project) : MetaModelProcessor<EditorDefinition, CngMetaEditorDefinition?>(project) {

    override fun process(container: String, yContainer: String, fileName: String, custom: Boolean, dom: EditorDefinition) = CngMetaModelNameProvider
        .extract(dom)
        ?.let { id -> CngMetaEditorDefinition(dom, fileName, id, custom) }
}

@Service(Service.Level.PROJECT)
class CngMetaModelWidgetDefinitionProcessor(project: Project) : MetaModelProcessor<WidgetDefinition, CngMetaWidgetDefinition?>(project) {

    override fun process(container: String, yContainer: String, fileName: String, custom: Boolean, dom: WidgetDefinition) = CngMetaModelNameProvider
        .extract(dom)
        ?.let { id ->
            val settings = CaseInsensitiveConcurrentHashMap<String, CngMetaWidgetSetting>()
            dom.settings.settings
                .map { CngMetaWidgetSetting(it, fileName, custom) }
                .forEach { settings[it.id] = it }

            CngMetaWidgetDefinition(dom, fileName, id, custom, settings)
        }
}
