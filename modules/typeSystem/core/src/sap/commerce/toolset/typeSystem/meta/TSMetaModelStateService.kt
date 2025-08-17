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

package sap.commerce.toolset.typeSystem.meta

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.meta.MetaModelStateService
import sap.commerce.toolset.typeSystem.meta.event.TSMetaModelChangeListener
import sap.commerce.toolset.typeSystem.model.Items

@Service(Service.Level.PROJECT)
class TSMetaModelStateService(project: Project, coroutineScope: CoroutineScope) : MetaModelStateService<TSGlobalMetaModel, TSMetaModel, Items>(
    project, coroutineScope, "Type",
    TSMetaCollector.getInstance(project),
    TSMetaModelProcessor.getInstance(project)
) {

    override fun onCompletion(newState: TSGlobalMetaModel) {
        project.messageBus.syncPublisher(TSMetaModelChangeListener.TOPIC).onChanged(newState)
    }

    override suspend fun create(metaModelsToMerge: Collection<TSMetaModel>): TSGlobalMetaModel = TSGlobalMetaModel().also {
        readAction { TSMetaModelMerger.merge(it, metaModelsToMerge.sortedBy { meta -> !meta.custom }) }
    }

    companion object {
        fun state(project: Project) = getInstance(project).get()
        fun getInstance(project: Project): TSMetaModelStateService = project.service()
    }

}