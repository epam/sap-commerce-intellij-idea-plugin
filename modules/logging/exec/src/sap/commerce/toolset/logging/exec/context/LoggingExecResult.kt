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

package sap.commerce.toolset.logging.exec.context

import org.apache.http.HttpStatus
import sap.commerce.toolset.exec.context.ExecResult
import sap.commerce.toolset.logging.CxLoggerModel

data class LoggingExecResult(
    val statusCode: Int = HttpStatus.SC_OK,
    override val errorMessage: String? = null,
    override val errorDetailMessage: String? = null,
    private val result: List<CxLoggerModel>? = null,
) : ExecResult {

    val loggers
        get() = result
            ?.distinctBy { it.name }
            ?.associateBy { it.name }

    val hasError
        get() = errorMessage != null
}