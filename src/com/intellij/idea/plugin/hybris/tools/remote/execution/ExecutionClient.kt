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

package com.intellij.idea.plugin.hybris.tools.remote.execution

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.io.Serial

abstract class ExecutionClient<E : ExecutionContext>(
    protected val project: Project,
    protected val coroutineScope: CoroutineScope
) : UserDataHolderBase() {

    fun execute(
        context: E,
        beforeCallback: (CoroutineScope) -> Unit = { _ -> },
        resultCallback: (CoroutineScope, ExecutionResult) -> Unit
    ) {
        execute(
            contexts = listOf(context),
            beforeCallback = beforeCallback,
            resultCallback = resultCallback,
        )
    }

    fun execute(
        contexts: Collection<E>,
        beforeCallback: (CoroutineScope) -> Unit = { _ -> },
        resultCallback: (CoroutineScope, ExecutionResult) -> Unit,
        resultsCallback: (CoroutineScope, Collection<ExecutionResult>) -> Unit = { _, _ -> }
    ) {
        coroutineScope.launch {
            beforeCallback.invoke(this)

            val results = contexts
                .map { context ->
                    async {
                        process(context, resultCallback)
                    }
                }
                .awaitAll()

            resultsCallback.invoke(this, results)
        }
    }

    internal abstract suspend fun execute(context: E): ExecutionResult

    private suspend fun process(
        context: E,
        resultCallback: (CoroutineScope, ExecutionResult) -> Unit
    ) = withBackgroundProgress(project, context.title, true) {
        val result = reportProgress { progressReporter ->
            execute(context)
        }

        resultCallback.invoke(this, result)

        result
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3086939180615991870L
    }
}
