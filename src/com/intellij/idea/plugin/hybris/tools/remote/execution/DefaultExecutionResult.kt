/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.tools.remote.execution

import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.ReplicaContext
import org.apache.http.HttpStatus

data class DefaultExecutionResult(
    var errorMessage: String? = null,
    var detailMessage: String? = null,
    var output: String = "",
    var result: String? = null,
    var statusCode: Int = HttpStatus.SC_OK,
    val remoteConnectionType: RemoteConnectionType = RemoteConnectionType.Hybris,
    val replicaContext: ReplicaContext? = null,
) : ExecutionResult {

    val hasError
        get() = errorMessage != null

}
