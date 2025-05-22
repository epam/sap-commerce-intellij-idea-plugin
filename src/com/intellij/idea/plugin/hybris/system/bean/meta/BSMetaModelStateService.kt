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

package com.intellij.idea.plugin.hybris.system.bean.meta

import com.intellij.idea.plugin.hybris.system.meta.AbstractMetaModelStateService
import com.intellij.idea.plugin.hybris.system.meta.CachedState
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class BSMetaModelStateService(project: Project, private val coroutineScope: CoroutineScope) : AbstractMetaModelStateService<BSMetaModel, BSGlobalMetaModel>(project) {

    private val metaModelCollector = project.service<BSMetaModelCollector>()
    private val metaModelProcessor = project.service<BSMetaModelProcessor>()

    override fun processState(metaModels: Collection<String>) {
        if (metaModelState.value.computing) return

        _metaModelState.value = CachedState(null, computed = false, computing = true)

        DumbService.Companion.getInstance(project).runWhenSmart {
            coroutineScope.launch {
                val newState = withBackgroundProgress(project, "Re-building Bean System...", true) {
                    val collectedDependencies = readAction { metaModelCollector.collectDependencies() }

                    val localMetaModels = reportProgress(collectedDependencies.size) { progressReporter ->
                        collectedDependencies
                            .map {
                                progressReporter.sizedStep(1, "Processing: ${it.name}...") {
                                    async {
                                        val cachedMetaModel = metaModelsState.value[it.name]
                                        if (cachedMetaModel == null || metaModels.contains(it.name)) {
                                            it.name to metaModelProcessor.process(it)
                                        } else {
                                            it.name to cachedMetaModel
                                        }
                                    }
                                }
                            }
                            .awaitAll()
                            .filter { (_, model) -> model != null }
                            .distinctBy { it.first }
                            .associate { it.first to it.second!! }
                    }

                    _metaModelsState.value = localMetaModels

                    BSGlobalMetaModel().also { globalMetaModel ->
                        val metaModelsToMerge = metaModelsState.value.values.sortedBy { !it.custom }
                        readAction { BSMetaModelMerger.merge(globalMetaModel, metaModelsToMerge) }
                    }
                }

                _metaModelState.value = CachedState(newState, computed = true, computing = false)
                _recomputeMetas.value = null

                project.messageBus.syncPublisher(TOPIC).beanSystemChanged(newState)
            }
        }
    }
}