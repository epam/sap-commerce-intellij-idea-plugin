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

package com.intellij.idea.plugin.hybris.tools.remote.http

data class ExecutionContext(
    val mode: ReplicaSelectionMode,
    val contexts: Collection<ReplicaAwareExecutionContext> = emptyList(),
) {
    override fun toString() = when (mode) {
        ReplicaSelectionMode.AUTO -> "Auto-discover replica"
        ReplicaSelectionMode.MANUAL -> "Manual"
        ReplicaSelectionMode.CCV2 -> "CCv2"
    }

    val description
        get() = when (mode) {
            ReplicaSelectionMode.CCV2 -> "- CCv2 ${contexts.size} replica(s) -"

            ReplicaSelectionMode.MANUAL -> listOfNotNull(
                "- Manually configured replica(s) -",
                contexts.groupBy { it.cookieName }
                    .map { (cookieName, ids) ->
                        "Cookie: $cookieName (${ids.size} replica(s))"
                    }
            ).joinToString("\n")

            else -> null
        }

    companion object {
        fun auto() = ExecutionContext(ReplicaSelectionMode.AUTO)

        fun ccv2(replicaIds: Collection<String> = emptyList()) = ExecutionContext(
            ReplicaSelectionMode.CCV2,
            replicaIds.map { ReplicaAwareExecutionContext(it) }
        )

        fun manual(executionContexts: Collection<ReplicaAwareExecutionContext> = emptyList()) = ExecutionContext(
            ReplicaSelectionMode.MANUAL,
            executionContexts
        )
    }
}
