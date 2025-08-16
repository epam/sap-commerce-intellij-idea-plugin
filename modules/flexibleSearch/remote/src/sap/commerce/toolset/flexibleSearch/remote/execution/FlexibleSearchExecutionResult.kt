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

package sap.commerce.toolset.flexibleSearch.remote.execution

import org.apache.http.HttpStatus
import sap.commerce.toolset.exec.remote.context.ConsoleAwareExecutionResult
import sap.commerce.toolset.exec.remote.context.DefaultExecutionResult
import sap.commerce.toolset.exec.remote.context.ReplicaContext
import sap.commerce.toolset.exec.remote.settings.state.RemoteConnectionType

data class FlexibleSearchExecutionResult(
    override val remoteConnectionType: RemoteConnectionType = RemoteConnectionType.Hybris,
    val statusCode: Int = HttpStatus.SC_OK,
    override val result: String? = null,
    override val output: String? = null,
    override val replicaContext: ReplicaContext? = null,
    override val errorMessage: String? = null,
    override val errorDetailMessage: String? = null,
) : ConsoleAwareExecutionResult {

    val hasDataRows: Boolean
        get() = output?.trim()?.contains("\n") ?: false

    companion object {
        fun from(result: DefaultExecutionResult) = FlexibleSearchExecutionResult(
            remoteConnectionType = result.remoteConnectionType,
            statusCode = result.statusCode,
            result = result.result,
            output = result.output,
            replicaContext = result.replicaContext,
            errorMessage = result.errorMessage,
            errorDetailMessage = result.errorDetailMessage,
        )
    }
}