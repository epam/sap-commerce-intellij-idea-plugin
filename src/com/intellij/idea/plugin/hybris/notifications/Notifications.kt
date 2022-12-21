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
    private val notification: Notification
    private var delay: Long? = null

    init {
        notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_HYBRIS)
            .createNotification(title, content, type)
            .setIcon(HybrisIcons.HYBRIS_ICON)
    }

    fun important(important: Boolean): Notifications {
        notification.isImportant = important
        return this
    }

    fun delay(delay: Long): Notifications {
        if (delay > 0) {
            this.delay = delay
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
                SystemNotifications.getInstance().notify(notificationName, notificationTitle, notificationText)
            }
        }

        @JvmStatic
        fun create(type: NotificationType, title: String, content: String): Notifications {
            return Notifications(type, title, content)
        }
    }
}