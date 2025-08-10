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
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Serial
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener

class LoggersSplitView(
    private val project: Project,
    private val coroutineScope: CoroutineScope
) : OnePixelSplitter(false, 0.25f), Disposable {

    private val tree = LoggersOptionsTree(project)
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

                override fun onActiveSolrConnectionChanged(remoteConnection: RemoteConnectionSettings) = Unit
                override fun onHybrisConnectionModified(remoteConnection: RemoteConnectionSettings) = tree.update()
            })
        }

        with(project.messageBus.connect(this)) {
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

        tree.addTreeSelectionListener(treeSelectionListener())
        tree.addTreeModelListener(treeModelListener())
    }

    private fun updateTree(settings: RemoteConnectionSettings) {
        val connections = RemoteConnectionService.getInstance(project)
            .getRemoteConnections(RemoteConnectionType.Hybris)
            .associateWith { (it == settings) }
        tree.update(connections)
    }

    private fun treeSelectionListener() = TreeSelectionListener {
        it.newLeadSelectionPath
            ?.lastPathComponent
            ?.asSafely<LoggersOptionsTreeNode>()
            ?.userObject
            ?.asSafely<LoggersNode>()
            ?.let { node -> updateSecondComponent(node) }
    }

    private fun treeModelListener() = object : TreeModelListener {
        override fun treeNodesChanged(e: TreeModelEvent) {
            tree.selectionPath
                ?.takeIf { e.treePath?.lastPathComponent == it.parentPath?.lastPathComponent }
                ?.lastPathComponent
                ?.asSafely<LoggersOptionsTreeNode>()
                ?.userObject
                ?.asSafely<LoggersNode>()
                ?.let { node -> updateSecondComponent(node) }
        }

        override fun treeNodesInserted(e: TreeModelEvent) = Unit
        override fun treeNodesRemoved(e: TreeModelEvent) = Unit
        override fun treeStructureChanged(e: TreeModelEvent) = Unit
    }

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