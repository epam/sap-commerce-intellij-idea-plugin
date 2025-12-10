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

import com.intellij.ide.IdeBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.CxRemoteLogStateService
import sap.commerce.toolset.logging.custom.settings.event.CxCustomLogTemplateStateListener
import sap.commerce.toolset.logging.exec.event.CxRemoteLogStateListener
import sap.commerce.toolset.logging.presentation.CxLogTemplatePresentation
import sap.commerce.toolset.logging.ui.tree.CxLoggersTree
import sap.commerce.toolset.logging.ui.tree.CxLoggersTreeNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxBundledLogTemplateItemNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxCustomLogTemplateItemNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxLoggersNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxRemoteLogStateNode
import sap.commerce.toolset.ui.addMouseListener
import sap.commerce.toolset.ui.addTreeModelListener
import sap.commerce.toolset.ui.addTreeSelectionListener
import sap.commerce.toolset.ui.event.MouseListener
import sap.commerce.toolset.ui.event.TreeModelListener
import sap.commerce.toolset.ui.pathData
import sap.commerce.toolset.ui.toolwindow.CxToolWindowActivationAware
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.event.TreeModelEvent

class CxLoggersSplitView(private val project: Project) : OnePixelSplitter(false, 0.25f), CxToolWindowActivationAware, Disposable {

    private val tree = CxLoggersTree(project).apply { registerListeners(this) }
    private val remoteLogStateView by lazy { CxRemoteLogStateView(project).also { Disposer.register(this, it) } }
    private val bundledLogTemplatesView by lazy { CxBundledLogTemplatesView(project).also { Disposer.register(this, it) } }
    private val customLogTemplatesView by lazy { CxCustomLogTemplatesView(project).also { Disposer.register(this, it) } }

    init {
        firstComponent = JBScrollPane(tree)
        secondComponent = remoteLogStateView.view

        PopupHandler.installPopupMenu(tree, "sap.cx.loggers.toolwindow.menu", "Sap.Cx.LoggersToolWindow")
        Disposer.register(this, tree)

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
                override fun onTemplatesDeleted() {
                    updateTree()
                    updateSecondComponent(null)
                }

                override fun onLoggerDeleted(modifiedTemplate: CxLogTemplatePresentation) {
                    val node = customLogTemplateItemNode(modifiedTemplate)
                        ?: return

                    node.update(modifiedTemplate)
                    updateSecondComponent(node)
                }

                override fun onLoggerUpdated(modifiedTemplate: CxLogTemplatePresentation) {
                    val node = customLogTemplateItemNode(modifiedTemplate)
                        ?: return

                    node.update(modifiedTemplate)
                }

                private fun customLogTemplateItemNode(modifiedTemplate: CxLogTemplatePresentation): CxCustomLogTemplateItemNode? = tree.lastSelectedPathComponent
                    ?.asSafely<CxLoggersTreeNode>()
                    ?.userObject
                    ?.asSafely<CxCustomLogTemplateItemNode>()
                    ?.takeIf { it.uuid == modifiedTemplate.uuid }
            })
        }
    }

    override fun onActivated() = updateTree()
    private fun updateTree() = tree.onActivated()

    private fun registerListeners(tree: CxLoggersTree) = tree
        .addTreeSelectionListener(tree) { event ->
            event
                .takeIf { it.isAddedPath }
                ?.newLeadSelectionPath
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
                    .selectionPath
                    ?.takeIf { e.getClickCount() == 2 && !e.isConsumed }
                    ?.pathData(CxRemoteLogStateNode::class)
                    ?.let {
                        e.consume()
                        HacExecConnectionService.getInstance(project).connections
                            .find { connection -> connection.uuid == it.connection.uuid }
                            ?.let { connection -> CxRemoteLogStateService.getInstance(project).fetch(connection) }
                    }
            }
        })

    private fun updateSecondComponent(node: CxLoggersNode?) {
        CoroutineScope(Dispatchers.EDT).launch {
            if (project.isDisposed) return@launch

            when (node) {
                is CxRemoteLogStateNode -> {
                    secondComponent = remoteLogStateView.view

                    val loggers = CxRemoteLogStateService.getInstance(project).state(node.connection.uuid).get()
                    remoteLogStateView.render(loggers)
                }

                is CxBundledLogTemplateItemNode -> {
                    secondComponent = bundledLogTemplatesView.view

                    node.loggers.associateBy { it.name }.let {
                        bundledLogTemplatesView.render(it)
                    }
                }

                is CxCustomLogTemplateItemNode -> {
                    secondComponent = customLogTemplatesView.view

                    node.loggers.associateBy { it.name }.let {
                        customLogTemplatesView.render(node.uuid, it)
                    }
                }

                else -> {
                    secondComponent = panel {
                        row {
                            label(IdeBundle.message("empty.text.nothing.selected"))
                                .resizableColumn()
                                .align(Align.CENTER)
                        }
                            .resizableRow()
                    }
                }
            }
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 933155170958799595L
    }
}