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
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.CxRemoteLogAccess
import sap.commerce.toolset.logging.custom.settings.event.CxCustomLogTemplateStateListener
import sap.commerce.toolset.logging.exec.event.CxRemoteLogStateListener
import sap.commerce.toolset.logging.presentation.CxLogTemplatePresentation
import sap.commerce.toolset.logging.ui.tree.CxLoggersTree
import sap.commerce.toolset.logging.ui.tree.CxLoggersTreeNode
import sap.commerce.toolset.logging.ui.tree.nodes.*
import sap.commerce.toolset.ui.addMouseListener
import sap.commerce.toolset.ui.addTreeModelListener
import sap.commerce.toolset.ui.addTreeSelectionListener
import sap.commerce.toolset.ui.event.MouseListener
import sap.commerce.toolset.ui.event.TreeModelListener
import sap.commerce.toolset.ui.pathData
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.event.TreeModelEvent

class CxLoggersSplitView(private val project: Project) : OnePixelSplitter(false, 0.25f), Disposable {

    private val tree = CxLoggersTree(project).apply { registerListeners(this) }
    private val remoteLogStateView = CxRemoteLogStateView(project)
    private val bundledLogTemplatesView = CxBundledLogTemplatesView(project)
    private val customLogTemplatesView = CxCustomLogTemplatesView(project)

    init {
        firstComponent = JBScrollPane(tree)
        secondComponent = remoteLogStateView.view

        PopupHandler.installPopupMenu(tree, "sap.cx.loggers.toolwindow.menu", "Sap.Cx.LoggersToolWindow")
        Disposer.register(this, tree)
        Disposer.register(this, remoteLogStateView)
        Disposer.register(this, bundledLogTemplatesView)
        Disposer.register(this, customLogTemplatesView)

        with(project.messageBus.connect(this)) {
            subscribe(HacConnectionSettingsListener.TOPIC, object : HacConnectionSettingsListener {
                override fun onActive(connection: HacConnectionSettingsState) = updateTree()
                override fun onUpdate(settings: Collection<HacConnectionSettingsState>) = updateTree()
                override fun onSave(settings: Collection<HacConnectionSettingsState>) = updateTree()
                override fun onCreate(connection: HacConnectionSettingsState) = updateTree()
                override fun onDelete(connection: HacConnectionSettingsState) = updateTree()
            })

            subscribe(CxRemoteLogStateListener.TOPIC, object : CxRemoteLogStateListener {
                override fun onLoggersStateChanged(remoteConnection: HacConnectionSettingsState) {
                    val node = tree.lastSelectedPathComponent
                        ?.asSafely<CxLoggersTreeNode>()
                        ?.userObject
                        ?.asSafely<CxRemoteLogStateNode>()
                        ?.takeIf { it.connection.uuid == remoteConnection.uuid }
                        ?: return

                    node.update()
                    updateSecondComponent(node)
                }
            })

            subscribe(CxCustomLogTemplateStateListener.TOPIC, object : CxCustomLogTemplateStateListener {
                override fun onTemplateUpdated(templateUUID: String) = updateTree()

                override fun onTemplateDeleted() = updateTree()

                override fun onLoggerUpdated(modifiedTemplate: CxLogTemplatePresentation) {
                    val node = tree.lastSelectedPathComponent
                        ?.asSafely<CxLoggersTreeNode>()
                        ?.userObject
                        ?.asSafely<CxCustomLogTemplateItemNode>()
                        ?.takeIf { it.uuid == modifiedTemplate.uuid }
                        ?: return

                    node.update(modifiedTemplate)
                    updateSecondComponent(node)
                }
            })
        }
    }

    fun updateTree() {
        tree.update()
    }

    private fun registerListeners(tree: CxLoggersTree) = tree
        .addTreeSelectionListener(tree) {
            it.newLeadSelectionPath
                ?.pathData(CxLoggersNode::class)
                ?.let { node -> updateSecondComponent(node) }
        }
        .addTreeModelListener(tree, object : TreeModelListener {
            override fun treeNodesChanged(e: TreeModelEvent) {
                tree.selectionPath
                    ?.takeIf { e.treePath?.lastPathComponent == it.parentPath?.lastPathComponent }
                    ?.pathData(CxLoggersNode::class)
                    ?.let { node -> updateSecondComponent(node) }
            }
        })
        .addMouseListener(tree, object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                tree
                    .takeIf { e.getClickCount() == 2 && !e.isConsumed }
                    ?.getPathForLocation(e.getX(), e.getY())
                    ?.pathData(CxRemoteLogStateNode::class)
                    ?.let {
                        e.consume()
                        HacExecConnectionService.getInstance(project).connections
                            .find { connection -> connection.uuid == it.connection.uuid }
                            ?.let { connection -> CxRemoteLogAccess.getInstance(project).fetch(connection) }
                    }
            }
        })

    private fun updateSecondComponent(node: CxLoggersNode) {
        CoroutineScope(Dispatchers.Default).launch {
            if (project.isDisposed) return@launch

            when (node) {
                is CxRemoteLogStateNode -> {
                    secondComponent = remoteLogStateView.view

                    CxRemoteLogAccess.getInstance(project).state(node.connection.uuid).get()
                        ?.let { remoteLogStateView.renderLoggers(it) }
                        ?: remoteLogStateView.renderFetchLoggers()
                }

                is CxBundledLogTemplateGroupNode -> {
                    secondComponent = bundledLogTemplatesView.view

                    bundledLogTemplatesView.renderNothingSelected()
                }

                is CxBundledLogTemplateItemNode -> {
                    secondComponent = bundledLogTemplatesView.view

                    node.loggers.associateBy { it.name }.let {
                        bundledLogTemplatesView.renderLoggersTemplate(it)
                    }
                }

                is CxCustomLogTemplateGroupNode -> {
                    secondComponent = customLogTemplatesView.view

                    customLogTemplatesView.renderNothingSelected()
                }

                is CxCustomLogTemplateItemNode -> {
                    secondComponent = customLogTemplatesView.view

                    node.loggers.associateBy { it.name }.let {
                        customLogTemplatesView.renderLoggersTemplate(node.uuid, it)
                    }
                }

                else -> {
                    secondComponent = remoteLogStateView.view

                    remoteLogStateView.renderNothingSelected()
                }
            }
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 933155170958799595L
    }
}