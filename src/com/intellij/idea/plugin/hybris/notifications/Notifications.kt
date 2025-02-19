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

package com.intellij.idea.plugin.hybris.notifications

import com.intellij.idea.plugin.hybris.common.HybrisConstants.NOTIFICATION_GROUP_HYBRIS
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.SystemNotifications
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

class Notifications private constructor(type: NotificationType, title: String, content: String) {

    private val notification: Notification = NotificationGroupManager.getInstance()
        .getNotificationGroup(NOTIFICATION_GROUP_HYBRIS)
        .createNotification(title, content, type)
        .setIcon(
            when (type) {
                NotificationType.WARNING,
                NotificationType.ERROR -> HybrisIcons.Y.LOGO_ORANGE

                else -> HybrisIcons.Y.LOGO_BLUE
            }
        )

    private var delay: Long? = null
    private var system: Boolean = false

    fun important(important: Boolean): Notifications {
        notification.isImportant = important
        return this
    }

    fun system(system: Boolean): Notifications {
        this.system = system
        return this
    }

    fun hideAfter(seconds: Long): Notifications {
        if (seconds > 0) {
            this.delay = seconds
        }
        return this
    }

    fun addAction(text: String, biConsumer: BiConsumer<AnActionEvent, Notification>): Notifications {
        notification.addAction(object : NotificationAction(text) {
            override fun actionPerformed(
                e: AnActionEvent,
                notification: Notification
            ) {
                biConsumer.accept(e, notification)
            }
        })
        return this
    }

    fun notify(project: Project?) {
        notification.notify(project)
        if (delay != null) {
            AppExecutorUtil.getAppScheduledExecutorService().schedule({ notification.expire() }, delay!!, TimeUnit.SECONDS)
        }

        if (system) {
            val frame = WindowManager.getInstance().getFrame(project) ?: return
            if (!frame.hasFocus()) {
                SystemNotifications.getInstance().notify("SAP CX", notification.title!!, notification.content)
            }
        }
    }

    companion object {
        @JvmStatic
        fun showSystemNotificationIfNotActive(
            project: Project,
            notificationName: String,
            notificationTitle: String,
            notificationText: String
        ) {
            val frame = WindowManager.getInstance().getFrame(project) ?: return
            if (!frame.hasFocus()) {
                SystemNotifications.getInstance().notify("SAP CX", notificationTitle, notificationText)
            }
        }

        @JvmStatic
        fun create(type: NotificationType, title: String, content: String = "") = Notifications(type, title, content)
    }
}