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
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.bean.meta.BSModificationTracker
import com.intellij.idea.plugin.hybris.system.meta.MetaModelModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.util.PathUtil
import com.intellij.util.asSafely

// TODO: Add support of the CNG-related files changes
class MetaSystemsAsyncFileListener : AsyncFileListener {

    override fun prepareChange(events: List<VFileEvent>) = ProjectManager.getInstance().openProjects
        .filterNot { DumbService.isDumb(it) }
        .filter { ProjectSettingsComponent.getInstance(it).isHybrisProject() }
        .mapNotNull { project ->
            events
                .mapNotNull { event ->
                    val mapToMetaFile: (String) -> Pair<MetaModelModificationTracker, String>? = {
                        when {
                            it.endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING) -> project.service<TSModificationTracker>() to it
                            it.endsWith(HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING) -> project.service<BSModificationTracker>() to it
                            else -> null
                        }
                    }
                    event.asSafely<VFilePropertyChangeEvent>()
                        ?.takeIf { it.isRename }
                        ?.let {
                            it.oldValue?.asSafely<String>()?.let(mapToMetaFile)
                                ?: it.newValue?.asSafely<String>()?.let(mapToMetaFile)
                        }
                        ?: PathUtil.getFileName(event.path)
                            .let(mapToMetaFile)
                        ?: return@mapNotNull null
                }
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
                            } catch (e: ProcessCanceledException) {
                                // do nothing; once done, model access service will notify all listeners
                            }
                        }
                    }
                }
            }
        }
}
