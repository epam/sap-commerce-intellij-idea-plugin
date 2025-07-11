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

package com.intellij.idea.plugin.hybris.ccv2.ui

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentStatus
import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.profile.codeInspection.ui.table.ThreeStateCheckBoxRenderer
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.treeStructure.treetable.TreeTable
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import java.io.Serial
import java.util.*
import javax.swing.tree.DefaultMutableTreeNode

class CCv2TreeTable(
    private val root: DefaultMutableTreeNode = CCv2TreeNode.RootNode(),
    private val myModel: CCv2TreeTableModel = CCv2TreeTableModel(root)
) : TreeTable(myModel), Disposable {

    val loadingState = AtomicProperty<CCv2Subscription?>(null)

    init {
        setRootVisible(false)
        emptyText.text = "No replicas found"

        setTreeCellRenderer(CCv2TreeCellRenderer())

        with(columnModel.getColumn(CCv2TreeTableModel.IS_ENABLED_COLUMN)) {
            setMaxWidth(JBUIScale.scale(22 + if (SystemInfo.isMac) 16 else 0))
            setCellRenderer(ThreeStateCheckBoxRenderer().apply { isOpaque = true })
            setCellEditor(ThreeStateCheckBoxRenderer())
        }
    }

    fun reset() {
        emptyText.text = "No replicas found"

        root.removeAllChildren()
        myModel.reload(root)
    }

    fun refresh(project: Project, subscription: CCv2Subscription) {
        isEnabled = false

        CCv2Service.Companion.getInstance(project).fetchEnvironments(
            listOf(subscription),
            onStartCallback = {
                loadingState.set(subscription)
                reset()
            },
            onCompleteCallback = { response ->
                response[subscription]
                    ?.filter { it.accessible }
                    ?.forEach { environment ->
                        environment.services
                            ?.filter { it.code.startsWith("hcs_platform_") }
                            ?.filter { it.replicas.isNotEmpty() }
                            ?.map { CCv2TreeNode.ServiceNode(it) }
                            ?.map { serviceNode ->
                                serviceNode.service.replicas
                                    .filter { replica -> replica.ready }
                                    .map { replica -> CCv2TreeNode.Replica(replica) }
                                    .forEach { replicaNode -> serviceNode.add(replicaNode) }

                                serviceNode
                            }
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { serviceNodes ->
                                CCv2TreeNode.EnvironmentNode(environment).also { environmentNode ->
                                    serviceNodes.forEach { environmentNode.add(it) }
                                    root.add(environmentNode)
                                }
                            }
                    }

                TreeUtil.expandAll(tree)
                isEnabled = true
                loadingState.set(null)
            },
            sendEvents = false,
            statuses = EnumSet.of(CCv2EnvironmentStatus.AVAILABLE),
            requestV1Details = true,
            requestV1Health = false,
            requestServices = true
        )
    }

    override fun dispose() = UIUtil.dispose(this)

    companion object {
        @Serial
        private const val serialVersionUID: Long = 5062030489991250736L
    }
}