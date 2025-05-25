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

import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.ActionDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.EditorDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.WidgetDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.Widgets
import com.intellij.idea.plugin.hybris.system.meta.Meta
import com.intellij.idea.plugin.hybris.system.meta.MetaCollector
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Service(Service.Level.PROJECT)
class CngMetaCollector(project: Project) : MetaCollector<DomElement>(project, DomElement::class.java, nameProvider = CngModificationTracker.KEY_PROVIDER) {

    private val metaConfigCollector = project.service<CngMetaConfigCollector>()
    private val metaWidgetsCollector = project.service<CngMetaWidgetsCollector>()
    private val metaActionDefinitionCollector = project.service<CngMetaActionDefinitionCollector>()
    private val metaWidgetDefinitionCollector = project.service<CngMetaWidgetDefinitionCollector>()
    private val metaEditorDefinitionCollector = project.service<CngMetaEditorDefinitionCollector>()

    override suspend fun collectDependencies(): Set<Meta<DomElement>> = coroutineScope {
        listOf(
            async { metaConfigCollector.collectDependencies() },
            async { metaWidgetsCollector.collectDependencies() },
            async { metaActionDefinitionCollector.collectDependencies() },
            async { metaWidgetDefinitionCollector.collectDependencies() },
            async { metaEditorDefinitionCollector.collectDependencies() },
        )
            .awaitAll()
            .flatten()
            .asSafely<Collection<Meta<DomElement>>>()
            ?.toImmutableSet()
            ?: emptySet()
    }
}

@Service(Service.Level.PROJECT)
class CngMetaConfigCollector(project: Project) : MetaCollector<Config>(
    project,
    Config::class.java,
    nameProvider = CngModificationTracker.KEY_PROVIDER
)

@Service(Service.Level.PROJECT)
class CngMetaWidgetsCollector(project: Project) : MetaCollector<Widgets>(
    project,
    Widgets::class.java,
    nameProvider = CngModificationTracker.KEY_PROVIDER
)

@Service(Service.Level.PROJECT)
class CngMetaActionDefinitionCollector(project: Project) : MetaCollector<ActionDefinition>(
    project,
    ActionDefinition::class.java,
    { it.id.exists() },
    CngModificationTracker.KEY_PROVIDER
)

@Service(Service.Level.PROJECT)
class CngMetaWidgetDefinitionCollector(project: Project) : MetaCollector<WidgetDefinition>(
    project,
    WidgetDefinition::class.java,
    { it.id.exists() },
    CngModificationTracker.KEY_PROVIDER
)

@Service(Service.Level.PROJECT)
class CngMetaEditorDefinitionCollector(project: Project) : MetaCollector<EditorDefinition>(
    project,
    EditorDefinition::class.java,
    { it.id.exists() },
    CngModificationTracker.KEY_PROVIDER
)
