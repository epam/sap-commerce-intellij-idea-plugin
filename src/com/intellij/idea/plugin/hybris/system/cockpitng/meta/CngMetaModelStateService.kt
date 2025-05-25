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

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngMeta
import com.intellij.idea.plugin.hybris.system.meta.MetaModelStateService
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class CngMetaModelStateService(project: Project, coroutineScope: CoroutineScope) : MetaModelStateService<CngGlobalMetaModel, CngMeta<DomElement>, DomElement>(
    project, coroutineScope, "Cockpit NG",
    project.service<CngMetaCollector>(),
    project.service<CngMetaModelAggregatedProcessor>()
) {

    override fun onCompletion(newState: CngGlobalMetaModel) {
        project.messageBus.syncPublisher(TOPIC).cngSystemChanged(newState)
    }

    override suspend fun create(metaModelsToMerge: Collection<CngMeta<DomElement>>): CngGlobalMetaModel = CngGlobalMetaModel().also {
        readAction { CngMetaModelMerger.merge(it, metaModelsToMerge.sortedBy { meta -> !meta.custom }) }
    }

}