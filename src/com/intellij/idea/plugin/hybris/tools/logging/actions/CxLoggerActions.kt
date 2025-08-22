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

package com.intellij.idea.plugin.hybris.tools.logging.actions

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerAccess
import com.intellij.idea.plugin.hybris.tools.logging.LogLevel
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.LoggersOptionsTree
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.templates.BundledLoggersTemplateItemNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.templates.CustomLoggersTemplateLoggersOptionsNode
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.AnimatedIcon
import com.intellij.util.asSafely
import javax.swing.tree.DefaultMutableTreeNode

abstract class CxLoggerAction(private val logLevel: LogLevel) : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val logIdentifier = e.getData(HybrisConstants.DATA_KEY_LOGGER_IDENTIFIER)

        if (logIdentifier == null) {
            Notifications.error("Unable to change the log level", "Cannot retrieve a logger name.")
                .hideAfter(5)
                .notify(project)
            return
        }

        CxLoggerAccess.getInstance(project).setLogger(logIdentifier, logLevel)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        super.update(e)
        val project = e.project ?: return

        e.presentation.text = logLevel.name
        e.presentation.icon = logLevel.icon
        e.presentation.isEnabled = CxLoggerAccess.getInstance(project).ready
    }
}

class AllLoggerAction : CxLoggerAction(LogLevel.ALL)
class OffLoggerAction : CxLoggerAction(LogLevel.OFF)
class TraceLoggerAction : CxLoggerAction(LogLevel.TRACE)
class DebugLoggerAction : CxLoggerAction(LogLevel.DEBUG)
class InfoLoggerAction : CxLoggerAction(LogLevel.INFO)
class WarnLoggerAction : CxLoggerAction(LogLevel.WARN)
class ErrorLoggerAction : CxLoggerAction(LogLevel.ERROR)
class FatalLoggerAction : CxLoggerAction(LogLevel.FATAL)

class FetchLoggersStateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return
        val loggerAccessService = CxLoggerAccess.getInstance(project)

        loggerAccessService.fetch()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return
        val loggerAccess = CxLoggerAccess.getInstance(project)

        e.presentation.isEnabled = loggerAccess.ready

        val state = CxLoggerAccess.getInstance(project).stateInitialized

        e.presentation.text = if (state) "Refresh Loggers State" else "Fetch Loggers State"
        e.presentation.icon = if (state) HybrisIcons.Log.Action.REFRESH else HybrisIcons.Log.Action.FETCH
        e.presentation.disabledIcon = if (loggerAccess.ready) null else AnimatedIcon.Default.INSTANCE
    }
}


class ApplyBundledTemplateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return

        e.selectedNode()
            ?.asSafely<BundledLoggersTemplateItemNode>()
            ?.loggers
            ?.let {
                CxLoggerAccess.getInstance(project).setLoggers(it) { _, result ->
                    println(result)
                }
            }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.text = "Apply Template"
        e.presentation.icon = HybrisIcons.Log.Template.APPLY
    }
}

class CxLoggersContextMenuActionGroup : ActionGroup(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val selectedNode = e?.selectedNode() ?: return emptyArray()
        return when (selectedNode) {
            is BundledLoggersTemplateItemNode -> arrayOf(ApplyBundledTemplateAction())
            else -> emptyArray()
        }
    }

    override fun update(e: AnActionEvent) {
        val selectedNode = e.selectedNode()
        when (selectedNode) {
            is BundledLoggersTemplateItemNode -> {
                e.presentation.isEnabledAndVisible = true
            }

            is CustomLoggersTemplateLoggersOptionsNode -> {
                e.presentation.isEnabledAndVisible = true
            }

            else -> {
                e.presentation.isEnabledAndVisible = false
            }
        }
    }
}

private fun AnActionEvent.selectedNode(): Any? = this.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT)
    ?.asSafely<LoggersOptionsTree>()
    ?.selectionPath
    ?.lastPathComponent
    ?.asSafely<DefaultMutableTreeNode>()
    ?.userObject