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

package com.intellij.idea.plugin.hybris.toolwindow.loggers

import com.intellij.idea.plugin.hybris.settings.RemoteConnectionListener
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerAccess
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggersStateListener
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.LoggersOptionsTree
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.LoggersOptionsTreeNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.LoggersHacConnectionNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.LoggersNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.templates.BundledLoggersTemplateLoggersOptionsNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.templates.CustomLoggersTemplateLoggersOptionsNode
import com.intellij.idea.plugin.hybris.ui.UiUtil.addMouseListener
import com.intellij.idea.plugin.hybris.ui.UiUtil.addTreeModelListener
import com.intellij.idea.plugin.hybris.ui.UiUtil.addTreeSelectionListener
import com.intellij.idea.plugin.hybris.ui.UiUtil.pathData
import com.intellij.idea.plugin.hybris.ui.event.MouseListener
import com.intellij.idea.plugin.hybris.ui.event.TreeModelListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.event.TreeModelEvent

class LoggersSplitView(
    private val project: Project,
    private val coroutineScope: CoroutineScope
) : OnePixelSplitter(false, 0.25f), Disposable {

    private val tree = LoggersOptionsTree(project).apply { registerListeners(this) }
    private val loggersStateView = LoggersStateView(project, coroutineScope)

    init {
        firstComponent = JBScrollPane(tree)
        secondComponent = loggersStateView.view

        //PopupHandler.installPopupMenu(tree, "action.group.id", "place")
        Disposer.register(this, tree)
        Disposer.register(this, loggersStateView)

        val activeConnection = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        updateTree(activeConnection)

        with(project.messageBus.connect(this)) {
            subscribe(RemoteConnectionListener.TOPIC, object : RemoteConnectionListener {
                override fun onActiveHybrisConnectionChanged(remoteConnection: RemoteConnectionSettings) {
                    updateTree(remoteConnection)
                }

                override fun onHybrisConnectionModified(remoteConnection: RemoteConnectionSettings) = tree.update()
            })

            subscribe(CxLoggersStateListener.TOPIC, object : CxLoggersStateListener {
                override fun onLoggersStateChanged(remoteConnection: RemoteConnectionSettings) {
                    tree.lastSelectedPathComponent
                        ?.asSafely<LoggersOptionsTreeNode>()
                        ?.userObject
                        ?.asSafely<LoggersHacConnectionNode>()
                        ?.takeIf { it.connectionSettings == remoteConnection }
                        ?.let { updateSecondComponent(it) }
                }
            })
        }
    }

    private fun updateTree(settings: RemoteConnectionSettings) {
        val connections = RemoteConnectionService.getInstance(project)
            .getRemoteConnections(RemoteConnectionType.Hybris)
            .associateWith { (it == settings) }
        tree.update(connections)
    }

    private fun registerListeners(tree: LoggersOptionsTree) = tree
        .addTreeSelectionListener(tree) {
            it.newLeadSelectionPath
                ?.pathData(LoggersNode::class)
                ?.let { node -> updateSecondComponent(node) }
        }
        .addTreeModelListener(tree, object : TreeModelListener {
            override fun treeNodesChanged(e: TreeModelEvent) {
                tree.selectionPath
                    ?.takeIf { e.treePath?.lastPathComponent == it.parentPath?.lastPathComponent }
                    ?.pathData(LoggersNode::class)
                    ?.let { node -> updateSecondComponent(node) }
            }
        })
        .addMouseListener(tree, object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                tree
                    .takeIf { e.getClickCount() == 2 && !e.isConsumed }
                    ?.getPathForLocation(e.getX(), e.getY())
                    ?.pathData(LoggersHacConnectionNode::class)
                    ?.let {
                        e.consume()
                        CxLoggerAccess.getInstance(project).fetch(it.connectionSettings)
                    }
            }
        })

    private fun updateSecondComponent(node: LoggersNode) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            when (node) {
                is LoggersHacConnectionNode -> CxLoggerAccess.getInstance(project).state(node.connectionSettings).get()
                    ?.let { loggersStateView.renderLoggers(it) }
                    ?: loggersStateView.renderFetchLoggers()

                is BundledLoggersTemplateLoggersOptionsNode -> loggersStateView.renderNoLoggerTemplates()
                is CustomLoggersTemplateLoggersOptionsNode -> loggersStateView.renderNoLoggerTemplates()
                else -> loggersStateView.renderNothingSelected()
            }
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 933155170958799595L
    }
}