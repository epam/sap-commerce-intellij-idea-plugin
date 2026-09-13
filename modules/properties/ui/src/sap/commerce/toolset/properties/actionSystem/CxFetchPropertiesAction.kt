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

package sap.commerce.toolset.properties.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AnimatedIcon
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.selectedNodes
import sap.commerce.toolset.properties.ui.tree.nodes.CxRemotePropertyStateNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceExtensionNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceProjectNode

/**
 * Brings the selected node up to date from wherever its properties come from: an instance is fetched over hAC, the
 * project is re-read from disk.
 *
 * Nothing else in the tree has a source to go back to, so the action stays disabled there rather than quietly doing
 * something to a node the user was not looking at.
 */
class CxFetchPropertiesAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup

        when (val target = e.target()) {
            is Target.Remote -> CxRemotePropertyStateService.getInstance(project).fetch(target.connection)
            is Target.Chain -> CxPropertyCollector.getInstance(project).reload(target.extensions)
            null -> Unit
        }
    }

    override fun update(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup

        when (val target = e.target()) {
            is Target.Remote -> {
                val service = CxRemotePropertyStateService.getInstance(project)
                val fetched = service.stateInitialized

                e.presentation.isEnabled = service.ready
                e.presentation.text = if (fetched) "Refresh Properties" else "Fetch Properties"
                e.presentation.icon = if (fetched) HybrisIcons.Log.Action.REFRESH else HybrisIcons.Log.Action.FETCH
                e.presentation.disabledIcon = if (service.ready) null else AnimatedIcon.Default.INSTANCE
            }

            is Target.Chain -> {
                val collector = CxPropertyCollector.getInstance(project)

                e.presentation.isEnabled = collector.ready
                e.presentation.text = when (val extensions = target.extensions) {
                    null -> "Reload Properties from Disk"
                    else -> "Reload ${extensions.singleOrNull() ?: "${extensions.size} Extensions"} from Disk"
                }
                e.presentation.icon = HybrisIcons.Log.Action.REFRESH
                e.presentation.disabledIcon = if (collector.ready) null else AnimatedIcon.Default.INSTANCE
            }

            null -> {
                e.presentation.isEnabled = false
                e.presentation.text = "Fetch Properties"
                e.presentation.icon = HybrisIcons.Log.Action.FETCH
                e.presentation.disabledIcon = null
            }
        }
    }

    /** What the selection can be brought up to date from, or `null` when it has no source of its own. */
    private fun AnActionEvent.target(): Target? {
        val nodes = selectedNodes()?.takeIf { it.isNotEmpty() } ?: return null

        nodes.singleOrNull()
            ?.let { it as? CxRemotePropertyStateNode }
            ?.let { return Target.Remote(it.connection) }

        // The whole chain is what the Project node shows, so anything selected beside it is already covered by it.
        if (nodes.any { it is CxSourceProjectNode }) return Target.Chain(null)

        return nodes
            .mapNotNull { (it as? CxSourceExtensionNode)?.extension }
            .takeIf { it.size == nodes.size }
            ?.let { Target.Chain(it) }
    }

    private sealed interface Target {
        @JvmInline
        value class Remote(val connection: HacConnectionSettingsState) : Target

        /** Extensions to re-read, or `null` for the whole chain. */
        @JvmInline
        value class Chain(val extensions: Collection<String>?) : Target
    }
}
