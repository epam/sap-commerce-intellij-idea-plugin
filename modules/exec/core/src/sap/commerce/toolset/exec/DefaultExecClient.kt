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

package sap.commerce.toolset.exec

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.exec.context.ExecContext
import java.io.Serial

abstract class DefaultExecClient<E : ExecContext>(
    project: Project,
    coroutineScope: CoroutineScope
) : ExecClient<E, DefaultExecResult>(project, coroutineScope) {

    override suspend fun onError(context: E, exception: Throwable) = DefaultExecResult(
        errorMessage = exception.message,
        errorDetailMessage = exception.stackTraceToString(),
    )

    companion object {
        @Serial
        private const val serialVersionUID: Long = -7785886660763821295L
    }
}