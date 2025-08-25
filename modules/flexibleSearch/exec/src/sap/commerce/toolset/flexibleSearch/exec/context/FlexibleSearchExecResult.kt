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

package sap.commerce.toolset.flexibleSearch.exec.context

import org.apache.http.HttpStatus
import sap.commerce.toolset.exec.context.ConsoleAwareExecResult
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.exec.context.ReplicaContext

data class FlexibleSearchExecResult(
    val statusCode: Int = HttpStatus.SC_OK,
    override val result: String? = null,
    override val output: String? = null,
    override val replicaContext: ReplicaContext? = null,
    override val errorMessage: String? = null,
    override val errorDetailMessage: String? = null,
) : ConsoleAwareExecResult {

    val hasDataRows: Boolean
        get() = output?.trim()?.contains("\n") ?: false

    companion object {
        fun from(result: DefaultExecResult) = FlexibleSearchExecResult(
            statusCode = result.statusCode,
            result = result.result,
            output = result.output,
            replicaContext = result.replicaContext,
            errorMessage = result.errorMessage,
            errorDetailMessage = result.errorDetailMessage,
        )
    }
}