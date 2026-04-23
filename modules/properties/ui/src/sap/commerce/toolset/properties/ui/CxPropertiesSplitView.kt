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
import kotlinx.coroutines.*
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.custom.settings.event.CxCustomPropertyTemplateStateListener
import sap.commerce.toolset.properties.exec.event.CxRemotePropertyStateListener
import sap.commerce.toolset.properties.presentation.CxPropertyTemplatePresentation
import sap.commerce.toolset.properties.ui.tree.CxPropertiesTree
import sap.commerce.toolset.properties.ui.tree.CxPropertiesTreeNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateItemNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxPropertiesNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxRemotePropertyStateNode
import sap.commerce.toolset.ui.addTreeSelectionListener
import sap.commerce.toolset.ui.pathData
import sap.commerce.toolset.ui.toolwindow.CxToolWindowActivationAware
import java.io.Serial

class CxPropertiesSplitView(private val project: Project) : OnePixelSplitter(false, 0.25f), CxToolWindowActivationAware, Disposable {
    private val tree = CxPropertiesTree(project)
    private val remotePropertyStateView by lazy { CxRemotePropertyStateView(project).also { Disposer.register(this, it) } }
    private val customPropertyTemplatesView by lazy { CxCustomPropertyTemplatesView(project).also { Disposer.register(this, it) } }
    private val nothingSelectedPanel = panel {
        row {
            label(IdeBundle.message("empty.text.nothing.selected"))
                .resizableColumn()
                .align(Align.CENTER)
        }.resizableRow()
    }

    private var job = SupervisorJob()
    private var coroutineScope = CoroutineScope(Dispatchers.Default + job)

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

        with(project.messageBus.connect(this)) {
            subscribe(HacConnectionSettingsListener.TOPIC, object : HacConnectionSettingsListener {
                override fun onActive(connection: HacConnectionSettingsState) = updateTree()
                override fun onUpdate(settings: Collection<HacConnectionSettingsState>) = updateTree()
                override fun onSave(settings: Collection<HacConnectionSettingsState>) = updateTree()
                override fun onCreate(connection: HacConnectionSettingsState) = updateTree()
                override fun onDelete(connection: HacConnectionSettingsState) = updateTree()
            })

            subscribe(CxRemotePropertyStateListener.TOPIC, object : CxRemotePropertyStateListener {
                override fun onPropertiesStateChanged(remoteConnection: HacConnectionSettingsState) {
                    val node = tree.lastSelectedPathComponent
                        ?.asSafely<CxPropertiesTreeNode>()
                        ?.userObject
                        ?.asSafely<CxRemotePropertyStateNode>()
                        ?.takeIf { it.connection.uuid == remoteConnection.uuid }
                        ?: return

                    updateSecondComponent(node) { node.update() }
                }
            })

            subscribe(CxCustomPropertyTemplateStateListener.TOPIC, object : CxCustomPropertyTemplateStateListener {
                override fun onTemplateUpdated(templateUUID: String) = updateTree()

                override fun onTemplatesDeleted() {
                    updateSecondComponent(null) { updateTree() }
                }

                override fun onPropertyDeleted(modifiedTemplate: CxPropertyTemplatePresentation) {
                    customTemplateNode(modifiedTemplate)?.let { node ->
                        updateSecondComponent(node) { node.update(modifiedTemplate) }
                    }
                }

                override fun onPropertyUpdated(modifiedTemplate: CxPropertyTemplatePresentation) {
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
    override fun dispose() = Unit

    private fun updateTree() = tree.onActivated()

    private fun updateSecondComponent(node: CxPropertiesNode?, beforeUpdate: () -> Unit = {}) {
        job.cancel()
        job = SupervisorJob()
        coroutineScope = CoroutineScope(Dispatchers.Default + job)

        coroutineScope.launch {
            ensureActive()
            if (project.isDisposed) return@launch

            withContext(Dispatchers.EDT) { beforeUpdate() }

            val component = when (node) {
                is CxRemotePropertyStateNode -> remotePropertyStateView.render(
                    coroutineScope,
                    CxRemotePropertyStateService.getInstance(project).state(node.connection.uuid).get()?.values,
                )
                is CxCustomPropertyTemplateItemNode -> customPropertyTemplatesView.render(coroutineScope, node.uuid, node.properties)

                else -> nothingSelectedPanel
            }

            withContext(Dispatchers.EDT) {
                ensureActive()
                secondComponent = component
            }
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -130054558419937927L
    }
}
