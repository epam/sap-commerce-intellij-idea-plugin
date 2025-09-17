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

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgressScope
import kotlinx.coroutines.*
import sap.commerce.toolset.exec.context.ExecContext
import sap.commerce.toolset.exec.context.ExecResult
import java.io.Serial
import kotlin.coroutines.CoroutineContext

abstract class ExecClient<E : ExecContext, R : ExecResult>(
    protected val project: Project,
    protected val coroutineScope: CoroutineScope
) : UserDataHolderBase() {

    fun execute(
        context: E,
        onError: (CoroutineContext, Throwable) -> Unit = { _, _ -> },
        beforeCallback: (CoroutineScope) -> Unit = { _ -> },
        resultCallback: (CoroutineScope, R) -> Unit,
    ) {
        execute(
            contexts = listOf(context),
            onError = onError,
            beforeCallback = beforeCallback,
            resultCallback = resultCallback,
        )
    }

    fun execute(
        contexts: Collection<E>,
        onError: (CoroutineContext, Throwable) -> Unit = { _, _ -> },
        beforeCallback: (CoroutineScope) -> Unit = { _ -> },
        resultCallback: (CoroutineScope, R) -> Unit = { _, _ -> },
        afterCallback: (CoroutineScope, Collection<R>) -> Unit = { _, _ -> },
    ) {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, exception ->
            onError.invoke(coroutineContext, exception)
        }
        coroutineScope.launch(exceptionHandler) {
            beforeCallback.invoke(this)

            val results = contexts
                .map { context ->
                    async {
                        process(context, resultCallback)
                    }
                }
                .awaitAll()

            afterCallback.invoke(this, results)
        }
    }

    abstract suspend fun execute(context: E): R

    abstract suspend fun onError(context: E, exception: Throwable): R

    private suspend fun process(
        context: E,
        resultCallback: (CoroutineScope, R) -> Unit
    ) = withBackgroundProgress(project, context.executionTitle, true) {
        val result = reportProgressScope { _ ->
            try {
                execute(context)
            } catch (t: Throwable) {
                thisLogger().error(t)

                onError(context, t)
            }
        }

        resultCallback.invoke(this, result)

        result
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3086939180615991870L
    }
}