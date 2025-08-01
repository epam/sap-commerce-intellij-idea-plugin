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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.tree

import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionListener
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerAccess
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.toolwindow.loggers.LoggersStateListener
import com.intellij.idea.plugin.hybris.toolwindow.loggers.forms.LoggersStateView
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.HacConnectionLoggersNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.LoggerNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.templates.BundledLoggersTemplateLoggersOptionsNode
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.nodes.options.templates.CustomLoggersTemplateLoggersOptionsNode
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.TreeNode
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Serial
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener

class LoggersTreePanel(
    val project: Project,
    private val coroutineScope: CoroutineScope
) : OnePixelSplitter(false, 0.25f), Disposable {

    val tree: LoggersOptionsTree = LoggersOptionsTree(project)
    private val myDefaultPanel = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))
    private val fetchLoggersStatePanel = JBPanelWithEmptyText().withEmptyText("Fetch Loggers State")
    private val noLogTemplatePanel = JBPanelWithEmptyText().withEmptyText("No Logger Templates")
    val loggersStateView: LoggersStateView = LoggersStateView.getInstance(project)

    init {
        firstComponent = JBScrollPane(tree)
        secondComponent = myDefaultPanel

        //PopupHandler.installPopupMenu(tree, "action.group.id", "place")
        Disposer.register(this, tree)

        val activeConnection = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val connections = RemoteConnectionService.getInstance(project).getRemoteConnections(RemoteConnectionType.Hybris)
            .associateWith { (it == activeConnection) }
        tree.update(connections)

        with(project.messageBus.connect(this)) {
            subscribe(RemoteConnectionListener.TOPIC, object : RemoteConnectionListener {
                override fun onActiveHybrisConnectionChanged(remoteConnection: RemoteConnectionSettings) {
                    val connections = RemoteConnectionService.getInstance(project).getRemoteConnections(RemoteConnectionType.Hybris)
                        .associateWith { (it == remoteConnection) }
                    tree.update(connections)
                }

                override fun onActiveSolrConnectionChanged(remoteConnection: RemoteConnectionSettings) = Unit
            })
        }

        with(project.messageBus.connect(this)) {
            subscribe(LoggersStateListener.TOPIC, object : LoggersStateListener {
                override fun onLoggersStateChanged(remoteConnection: RemoteConnectionSettings) {
                    tree.lastSelectedPathComponent
                        ?.asSafely<LoggersOptionsTreeNode>()
                        ?.userObject
                        ?.asSafely<HacConnectionLoggersNode>()
                        ?.takeIf { it.connectionSettings == remoteConnection }
                        ?.let { updateSecondComponent(it) }
                }
            })
        }

        tree.addTreeSelectionListener(treeSelectionListener())
        tree.addTreeModelListener(treeModelListener())
    }

    private fun treeSelectionListener() = TreeSelectionListener { tls ->
        val path = tls.newLeadSelectionPath
        val component = path?.lastPathComponent
        val node = (component as? LoggersOptionsTreeNode)?.userObject as? LoggerNode

        updateSecondComponent(node)
    }

    private fun treeModelListener() = object : TreeModelListener {
        override fun treeNodesChanged(e: TreeModelEvent) {
            if (e.treePath?.lastPathComponent == tree.selectionPath?.parentPath?.lastPathComponent) {
                val node = tree
                    .selectionPath
                    ?.lastPathComponent
                    ?.asSafely<TreeNode>()
                    ?.userObject
                    ?.asSafely<LoggerNode>()
                updateSecondComponent(node)
            }
        }

        override fun treeNodesInserted(e: TreeModelEvent) = Unit
        override fun treeNodesRemoved(e: TreeModelEvent) = Unit
        override fun treeStructureChanged(e: TreeModelEvent) = Unit
    }

    private fun updateSecondComponent(node: LoggerNode?) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            when (node) {
                is HacConnectionLoggersNode -> {
                    val loggers = if (node.activeConnection) {
                        val loggersAccess = CxLoggerAccess.getInstance(project)
                        val loggers = loggersAccess.loggers(node.connectionSettings)
                        loggers.all()
                    } else {
                        emptyMap()
                    }
                    if (loggers.isNotEmpty()) {
                        loggersStateView.renderView(loggers) { coroutineScope, view ->
                            secondComponent = view
                        }
                    } else {
                        secondComponent = fetchLoggersStatePanel
                    }
                }

                is BundledLoggersTemplateLoggersOptionsNode -> {
                    secondComponent = noLogTemplatePanel
                }

                is CustomLoggersTemplateLoggersOptionsNode -> {
                    secondComponent = noLogTemplatePanel
                }

                else -> {
                    secondComponent = myDefaultPanel
                }
            }
        }
    }


    companion object {
        @Serial
        private const val serialVersionUID: Long = 933155170958799595L
    }
}