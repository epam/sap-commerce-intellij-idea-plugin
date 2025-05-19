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
import com.intellij.idea.plugin.hybris.system.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.asSafely

class TSBulkFileListener : BulkFileListener {

    override fun after(events: MutableList<out VFileEvent>) {
        val project = getActiveProject() ?: return

        if (DumbService.isDumb(project)) return

        val fileIndex = ProjectRootManager.getInstance(project).fileIndex

        val suitableEvents = events
            .mapNotNull { event ->
                val file = event.file
                    ?.takeIf { fileIndex.isInContent(it) }
                    ?: return@mapNotNull null

                val fileName = (event.asSafely<VFilePropertyChangeEvent>()
                    ?.takeIf { it.isRename }
                    ?.oldValue
                    ?.asSafely<String>()
                    ?.takeIf { it.endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING) }
                    ?: event.path
                        .takeIf { it.endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING) }
                        ?.let { file.name }
                    ?: return@mapNotNull null)
                event to fileName
            }

        if (suitableEvents.isNotEmpty()) {
            // re-triggering GlobalMetaModel state on file changes
            try {
                TSModificationTracker.resetCache(suitableEvents.map { it.second })
                TSMetaModelAccess.getInstance(project).getMetaModel()
            } catch (e: ProcessCanceledException) {
                // do nothing; once done, model access service will notify all listeners
            }
        }
    }

    private fun getActiveProject(): Project? {
        val windowManager = WindowManager.getInstance()

        return ProjectManager.getInstance().openProjects
            .lastOrNull { windowManager.suggestParentWindow(it)?.isActive ?: false }
    }

}