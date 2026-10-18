/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.Function
import com.intellij.util.application
import com.intellij.util.asSafely
import kotlinx.coroutines.*
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.i18n
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.custom.settings.event.CxCustomPropertyTemplateStateListener
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.event.CxPropertyChainListener
import sap.commerce.toolset.properties.settings.event.CxPropertyViewSettingsListener
import sap.commerce.toolset.properties.settings.state.CxPropertySourceMode
import sap.commerce.toolset.properties.exec.event.CxRemotePropertyStateListener
import sap.commerce.toolset.properties.presentation.CxPropertyTemplatePresentation
import sap.commerce.toolset.properties.ui.tree.CxPropertiesTree
import sap.commerce.toolset.properties.ui.tree.CxPropertiesTreeNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateItemNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxPropertiesNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxRemotePropertyStateNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceCodeNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceExtensionGroupNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceExtensionNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceProjectNode
import sap.commerce.toolset.ui.addMouseListener
import sap.commerce.toolset.ui.addTreeSelectionListener
import sap.commerce.toolset.ui.event.MouseListener
import sap.commerce.toolset.ui.pathData
import sap.commerce.toolset.ui.toolwindow.CxToolWindowActivationAware
import java.awt.event.MouseEvent
import java.io.Serial

class CxPropertiesSplitView(private val project: Project) : OnePixelSplitter(false, 0.25f), CxToolWindowActivationAware, Disposable {
    internal val tree = CxPropertiesTree(project)
    private val remotePropertyStateView by lazy { CxRemotePropertyStateView(project).also { Disposer.register(this, it) } }
    private val customPropertyTemplatesView by lazy { CxCustomPropertyTemplatesView(project).also { Disposer.register(this, it) } }
    private val sourcePropertiesView by lazy { CxSourcePropertiesView(project).also { Disposer.register(this, it) } }
    private val nothingSelectedPanel = panel {
        row {
            label(i18n("empty.text.nothing.selected"))
                .resizableColumn()
                .align(Align.CENTER)
        }.resizableRow()
    }

    private val selectAnExtensionPanel = panel {
        row {
            label("Select an extension to see the properties it declares, or the Project node to see the whole chain.")
                .resizableColumn()
                .align(Align.CENTER)
        }.resizableRow()
    }

    private val coroutineScope = CxPropertiesViewScopeService.getInstance(project).coroutineScope
    private var renderJob: Job? = null

    init {
        firstComponent = JBScrollPane(tree)
        secondComponent = nothingSelectedPanel

        Disposer.register(this, tree)
        PopupHandler.installPopupMenu(tree, "sap.cx.properties.toolwindow.menu", "Sap.Cx.PropertiesToolWindow")

        tree.addTreeSelectionListener(tree) { event ->
            event
                .takeIf { it.isAddedPath }
                ?.newLeadSelectionPath
                ?.pathData(CxPropertiesNode::class)
                ?.let { updateSecondComponent(it) }
        }
        tree.addMouseListener(tree, object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                tree.selectionPath
                    ?.takeIf { e.clickCount == 2 && !e.isConsumed }
                    ?.pathData(CxRemotePropertyStateNode::class)
                    ?.let {
                        e.consume()
                        HacExecConnectionService.getInstance(project).connections
                            .find { connection -> connection.uuid == it.connection.uuid }
                            ?.let { connection -> CxRemotePropertyStateService.getInstance(project).fetch(connection) }
                    }
            }
        })

        with(project.messageBus.connect(this)) {
            subscribe(HacConnectionSettingsListener.TOPIC, object : HacConnectionSettingsListener {
                override fun onActivate(connection: HacConnectionSettingsState) = onEdt { updateTree() }
                override fun onSave(settings: Collection<HacConnectionSettingsState>) = onEdt { updateTree() }
                override fun onCreate(connection: HacConnectionSettingsState) = onEdt { updateTree() }
            })

            subscribe(CxRemotePropertyStateListener.TOPIC, object : CxRemotePropertyStateListener {
                override fun onPropertiesStateChanged(remoteConnection: HacConnectionSettingsState) = onEdt {
                    val node = tree.lastSelectedPathComponent
                        ?.asSafely<CxPropertiesTreeNode>()
                        ?.userObject
                        ?.asSafely<CxRemotePropertyStateNode>()
                        ?.takeIf { it.connection.uuid == remoteConnection.uuid }
                        ?: return@onEdt

                    updateSecondComponent(node) { node.update() }
                }
            })

            // A reload re-reads the files the Source Code branch is built from, so both it and the view beside it
            // are showing yesterday's values until they are rebuilt.
            subscribe(CxPropertyChainListener.TOPIC, object : CxPropertyChainListener {
                override fun onChainReloaded() = rebuildTree()
            })

            subscribe(CxPropertyViewSettingsListener.TOPIC, object : CxPropertyViewSettingsListener {
                override fun onSourceModeChanged(sourceMode: CxPropertySourceMode) = onEdt { updateSecondComponent(null) { updateTree() } }
            })

            // Refreshing the project renames modules when a group changes, and adds or drops them when the configured
            // extensions change - either way the Source Code branch is stale until the tree is rebuilt.
            subscribe(ModuleListener.TOPIC, object : ModuleListener {
                override fun modulesAdded(project: Project, modules: List<Module>) = scheduleTreeUpdate()

                override fun moduleRemoved(project: Project, module: Module) = scheduleTreeUpdate()

                override fun modulesRenamed(
                    project: Project,
                    modules: List<Module>,
                    oldNameProvider: Function<in Module, String>,
                ) = scheduleTreeUpdate()
            })

            subscribe(CxCustomPropertyTemplateStateListener.TOPIC, object : CxCustomPropertyTemplateStateListener {
                override fun onTemplateUpdated(templateUUID: String) = onEdt { updateTree() }

                override fun onTemplatesDeleted() = onEdt {
                    updateSecondComponent(null) { updateTree() }
                }

                override fun onPropertyDeleted(modifiedTemplate: CxPropertyTemplatePresentation) = onEdt {
                    customTemplateNode(modifiedTemplate)?.let { node ->
                        updateSecondComponent(node) { node.update(modifiedTemplate) }
                    }
                }

                override fun onPropertyUpdated(modifiedTemplate: CxPropertyTemplatePresentation) = onEdt {
                    customTemplateNode(modifiedTemplate)?.let { node ->
                        updateSecondComponent(node) { node.update(modifiedTemplate) }
                    }
                }

                private fun customTemplateNode(modifiedTemplate: CxPropertyTemplatePresentation) = tree.lastSelectedPathComponent
                    ?.asSafely<CxPropertiesTreeNode>()
                    ?.userObject
                    ?.asSafely<CxCustomPropertyTemplateItemNode>()
                    ?.takeIf { it.uuid == modifiedTemplate.uuid }
            })
        }
    }

    override fun onActivated() = updateTree()
    override fun dispose() {
        renderJob?.cancel()
    }

    private fun updateTree() = tree.onActivated()

    /** Listeners are notified on whatever thread published, while the tree and the views belong to the EDT. */
    private fun onEdt(block: () -> Unit) = application.invokeLater({ if (!project.isDisposed) block() }, project.disposed)

    /** The module model is still being written when these arrive, so the rebuild waits for the write action to end. */
    private fun scheduleTreeUpdate() = onEdt {
        // The chain is cached against the project settings, which a refresh normally bumps - but not every module
        // change goes through them, and a stale chain is worse than a scan nobody asked for.
        CxPropertyCollector.getInstance(project).resetCache()

        rebuildTree()
    }

    private fun rebuildTree() = onEdt {
        updateTree()
        // Nodes survive a reload by key, so whatever is selected is re-rendered rather than left showing old values.
        updateSecondComponent(tree.selectionPath?.pathData(CxPropertiesNode::class))
    }


    private fun updateSecondComponent(node: CxPropertiesNode?, beforeUpdate: () -> Unit = {}) {
        renderJob?.cancel()
        renderJob = coroutineScope.launch {
            if (project.isDisposed) return@launch

            withContext(Dispatchers.EDT) { beforeUpdate() }

            val component = when (node) {
                is CxRemotePropertyStateNode -> remotePropertyStateView.render(
                    this,
                    node.connection,
                    CxRemotePropertyStateService.getInstance(project).state(node.connection.uuid).get(),
                )
                is CxCustomPropertyTemplateItemNode -> customPropertyTemplatesView.render(this, node.uuid, node.properties)
                is CxSourceProjectNode -> sourcePropertiesView.render(CxSourceSelection.Project)
                is CxSourceExtensionNode -> sourcePropertiesView.render(CxSourceSelection.Extension(node.extension))
                is CxSourceCodeNode, is CxSourceExtensionGroupNode -> selectAnExtensionPanel

                else -> nothingSelectedPanel
            }

            withContext(Dispatchers.EDT) { secondComponent = component }
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -130054558419937927L
    }
}
