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
package com.intellij.idea.plugin.hybris.vfs.listeners

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.bean.meta.BSModificationTracker
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelStateService
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.util.isHybrisProject
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.util.PathUtil
import com.intellij.util.asSafely

/**
 * Limitation due performance restrictions:
 *
 * For CockpitNG changes only modification of already tracked files is supported (rename, remove, move, etc.)
 * Anyway, any modification to file itself will trigger corresponding PsiTree change which will be tracked by {@see com.intellij.idea.plugin.hybris.psi.listeners.PsiTreeChangeListener}
 * It means, that crete/move without modification of the file content may not always re-trigger cache re-evaluation.
 */
class MetaSystemsAsyncFileListener : AsyncFileListener {

    private fun mapToTracker(fileName: String, fqn: String, project: Project, trackedCngModels: Set<String>) = when {
        fileName.endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING) -> TSModificationTracker.getInstance(project) to fileName
        fileName.endsWith(HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING) -> BSModificationTracker.getInstance(project) to fileName
        // in case of the CockpitNG FQN is being tracked
        trackedCngModels.contains(fqn) -> CngModificationTracker.getInstance(project) to fqn
        else -> null
    }

    override fun prepareChange(events: List<VFileEvent>) = ProjectManager.getInstance().openProjects
        .filterNot { DumbService.isDumb(it) }
        .filter { it.isHybrisProject }
        .mapNotNull { project ->
            val trackedCngModels by lazy { CngMetaModelStateService.getInstance(project).getTrackedModels() }

            events
                .map { event ->
                    event.asSafely<VFilePropertyChangeEvent>()
                        ?.takeIf { it.isRename }
                        ?.let { propertyEvent ->
                            propertyEvent.oldValue?.asSafely<String>()
                                ?.let { it to propertyEvent.oldPath }
                                ?: propertyEvent.newValue?.asSafely<String>()
                                    ?.let { it to propertyEvent.newPath }
                        }
                        ?: (PathUtil.getFileName(event.path) to event.path)
                }
                .mapNotNull { mapToTracker(it.first, it.second, project, trackedCngModels) }
                .groupBy({ it.first }, { it.second })
                .takeIf { it.isNotEmpty() }
        }
        .takeIf { it.isNotEmpty() }
        ?.let { processEvents ->
            object : AsyncFileListener.ChangeApplier {
                override fun beforeVfsChange() {
                    processEvents.forEach { metaToFileNames ->
                        metaToFileNames.forEach { (tracker, fileNames) ->
                            // re-triggering GlobalMetaModel state on file changes
                            // extra cases on a file, not covered by CacheValue upToDate evaluation: create, remove, rename
                            try {
                                tracker.resetCache(fileNames)
                            } catch (_: Throwable) {
                                // do nothing; once done, model access service will notify all listeners
                            }
                        }
                    }
                }
            }
        }
}
