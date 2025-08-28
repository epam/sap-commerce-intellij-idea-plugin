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

package sap.commerce.toolset.groovy.exec.context

import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.groovy.GroovyExecConstants

data class GroovyReplicaAwareContext(
    val replicaSelectionMode: ReplicaSelectionMode,
    val replicaContexts: Collection<ReplicaContext> = emptyList(),
) {
    override fun toString() = replicaSelectionMode.previewText.invoke(this)

    val previewText
        get() = replicaSelectionMode.previewText.invoke(this)

    val description
        get() = replicaSelectionMode.previewDescription.invoke(this)

    fun mutable() = Mutable(
            replicaSelectionMode = replicaSelectionMode,
            replicaContexts = replicaContexts.map { it.mutable() }.toMutableList()
        )

    data class Mutable(
        var replicaSelectionMode: ReplicaSelectionMode,
        var replicaContexts: MutableCollection<ReplicaContext.Mutable>,
    ) {
        fun immutable() = GroovyReplicaAwareContext(
                replicaSelectionMode = replicaSelectionMode,
                replicaContexts = replicaContexts.map { it.immutable() }
            )
    }

    companion object {
        fun auto() = GroovyReplicaAwareContext(GroovyExecConstants.auto)

        fun manual(executionContexts: Collection<ReplicaContext> = emptyList()) = GroovyReplicaAwareContext(
            GroovyExecConstants.manual,
            executionContexts
        )
    }
}