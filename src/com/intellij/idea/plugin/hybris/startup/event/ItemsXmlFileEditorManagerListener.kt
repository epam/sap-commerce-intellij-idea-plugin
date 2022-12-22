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

package com.intellij.idea.plugin.hybris.startup.event

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.type.system.validation.ItemsFileValidation
import com.intellij.idea.plugin.hybris.type.system.validation.impl.DefaultItemsFileValidation
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.vfs.VirtualFile

class ItemsXmlFileEditorManagerListener(private val project: Project) : FileEditorManagerListener {
    private val validator: ItemsFileValidation

    init {
        validator = DefaultItemsFileValidation(project)
    }

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        if (validator.isFileOutOfDate(file)) {
            Notifications.create(
                NotificationType.WARNING,
                HybrisI18NBundleUtils.message("hybris.notification.ts.validation.title"),
                HybrisI18NBundleUtils.message("hybris.notification.ts.validation.content")
            )
                .delay(10)
                .notify(project)
        }
    }
}