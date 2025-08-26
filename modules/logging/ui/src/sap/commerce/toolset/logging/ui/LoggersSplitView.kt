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

package sap.commerce.toolset.logging.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.CxLoggerAccess
import sap.commerce.toolset.logging.exec.event.CxLoggersStateListener
import sap.commerce.toolset.logging.ui.tree.LoggersOptionsTree
import sap.commerce.toolset.logging.ui.tree.LoggersOptionsTreeNode
import sap.commerce.toolset.logging.ui.tree.nodes.BundledLoggersTemplateLoggersOptionsNode
import sap.commerce.toolset.logging.ui.tree.nodes.CustomLoggersTemplateLoggersOptionsNode
import sap.commerce.toolset.logging.ui.tree.nodes.LoggersHacConnectionNode
import sap.commerce.toolset.logging.ui.tree.nodes.LoggersNode
import sap.commerce.toolset.ui.addMouseListener
import sap.commerce.toolset.ui.addTreeModelListener
import sap.commerce.toolset.ui.addTreeSelectionListener
import sap.commerce.toolset.ui.event.MouseListener
import sap.commerce.toolset.ui.event.TreeModelListener
import sap.commerce.toolset.ui.pathData
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

        // TODO: review this logic, let's NOT build the tree on init, instead render it on first show
        val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
        updateTree(activeConnection)

        with(project.messageBus.connect(this)) {
            subscribe(HacConnectionSettingsListener.TOPIC, object : HacConnectionSettingsListener {
                override fun onActiveConnectionChanged(connection: HacConnectionSettingsState) = updateTree(connection)
                override fun onModified(connection: HacConnectionSettingsState) = tree.update()
            })

            subscribe(CxLoggersStateListener.TOPIC, object : CxLoggersStateListener {
                override fun onLoggersStateChanged(remoteConnection: HacConnectionSettingsState) {
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

    private fun updateTree(settings: HacConnectionSettingsState) {
        val connections = HacExecConnectionService.getInstance(project).connections
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