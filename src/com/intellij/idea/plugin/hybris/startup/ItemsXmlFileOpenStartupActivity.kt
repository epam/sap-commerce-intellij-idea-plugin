/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisItemsXmlFileType
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.type.system.validation.ItemsFileValidation
import com.intellij.idea.plugin.hybris.type.system.validation.impl.DefaultItemsFileValidation
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope


private const val NOTIFICATION_TITLE = "hybris.notification.ts.validation.title"
private const val NOTIFICATION_CONTENT = "hybris.notification.ts.validation.content"

class ItemsXmlFileOpenStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        if (!ApplicationManager.getApplication().getService(CommonIdeaService::class.java).isHybrisProject(project)) {
            return
        }

        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER, ItemsXmlFileEditorManagerListener(project)
        )

        StartupManager.getInstance(project).runAfterOpened {
            val isOutdated = FileTypeIndex.getFiles(
                HybrisItemsXmlFileType.INSTANCE,
                GlobalSearchScope.projectScope(project)
            )
                .any { file -> DefaultItemsFileValidation(project).isFileOutOfDate(file) }
            if (isOutdated) {
                Notifications.create(
                    NotificationType.WARNING,
                    HybrisI18NBundleUtils.message(NOTIFICATION_TITLE),
                    HybrisI18NBundleUtils.message(NOTIFICATION_CONTENT)
                )
                    .important(true)
                    .delay(10)
                    .notify(project)
            }
        }
    }

    private class ItemsXmlFileEditorManagerListener(private val project: Project) : FileEditorManagerListener {
        private val validator: ItemsFileValidation

        init {
            validator = DefaultItemsFileValidation(project)
        }

        override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
            StartupManager.getInstance(project).runAfterOpened {
                if (validator.isFileOutOfDate(file)) {
                    Notifications.create(
                        NotificationType.WARNING,
                        HybrisI18NBundleUtils.message(NOTIFICATION_TITLE),
                        HybrisI18NBundleUtils.message(NOTIFICATION_CONTENT)
                    )
                        .delay(10)
                        .notify(project)
                }
            }
        }
    }
}