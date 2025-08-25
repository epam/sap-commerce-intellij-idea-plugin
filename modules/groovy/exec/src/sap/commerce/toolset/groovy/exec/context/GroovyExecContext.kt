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

import org.apache.commons.lang3.BooleanUtils
import sap.commerce.toolset.exec.context.ExecContext
import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.settings.state.TransactionMode

data class GroovyExecContext(
    override val executionTitle: String = DEFAULT_TITLE,
    private val content: String,
    private val transactionMode: TransactionMode = TransactionMode.ROLLBACK,
    val timeout: Int,
    val replicaContext: ReplicaContext? = null
) : ExecContext {

    fun params(): Map<String, String> = buildMap {
        put("scriptType", "groovy")
        put("commit", BooleanUtils.toStringTrueFalse(transactionMode == TransactionMode.COMMIT))
        put("script", content)
    }

    companion object {
        const val DEFAULT_TITLE = "Executing Groovy script on the remote SAP Commerce instance..."
    }
}