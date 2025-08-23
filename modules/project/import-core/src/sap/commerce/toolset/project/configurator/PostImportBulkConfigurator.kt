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

package sap.commerce.toolset.project.configurator

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor

@Service(Service.Level.PROJECT)
class PostImportBulkConfigurator(private val project: Project) {

    fun configure(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        ReadAction
            .nonBlocking<List<() -> Unit>> {
                if (project.isDisposed) return@nonBlocking emptyList()

                ProjectPostImportConfigurator.EP.extensionList
                    .map { it.postImport(hybrisProjectDescriptor) }
                    .flatten()
            }
            .finishOnUiThread(ModalityState.defaultModalityState()) { actions ->
                if (project.isDisposed) return@finishOnUiThread

                actions.forEach { it() }

                notifyImportFinished(project, hybrisProjectDescriptor.refresh)
            }
            .inSmartMode(project)
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    private fun notifyImportFinished(project: Project, refresh: Boolean) {
        val notificationContent = if (refresh) i18n("hybris.notification.project.refresh.finished.content")
        else i18n("hybris.notification.project.import.finished.content")
        val notificationTitle = if (refresh) i18n("hybris.notification.project.refresh.title")
        else i18n("hybris.notification.project.import.title")

        with(Notifications.Companion) {
            create(NotificationType.INFORMATION, notificationTitle, notificationContent).notify(project)
            showSystemNotificationIfNotActive(project, notificationContent, notificationTitle, notificationContent)
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PostImportBulkConfigurator = project.service()
    }
}