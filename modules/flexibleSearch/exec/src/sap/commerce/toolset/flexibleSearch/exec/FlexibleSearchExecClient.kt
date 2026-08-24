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

package sap.commerce.toolset.flexibleSearch.exec

import com.google.gson.Gson
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import sap.commerce.toolset.exec.ExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.flexibleSearch.exec.context.TableBuilder
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import sap.commerce.toolset.readResource
import sap.commerce.toolset.settings.state.TransactionMode
import java.io.Serial

@Service(Service.Level.PROJECT)
class FlexibleSearchExecClient(
    project: Project,
    coroutineScope: CoroutineScope
) : ExecClient<FlexibleSearchExecContext, FlexibleSearchExecResult>(project, coroutineScope) {

    override suspend fun onError(context: FlexibleSearchExecContext, exception: Throwable) = FlexibleSearchExecResult(
        errorMessage = exception.message,
        errorDetailMessage = exception.stackTraceToString()
    )

    override suspend fun execute(context: FlexibleSearchExecContext): FlexibleSearchExecResult = when {
        context.executableOnServiceLayer -> executeOnServiceLayer(context)
        else -> executeOnHac(context)
    }

    /**
     * Executes the query on the Service Layer via Groovy, as the HAC FlexibleSearch console does not return more
     * than [FlexibleSearchExecConstants.Limits.HAC_MAX_COUNT] rows.
     *
     * The column names are not reported by the Service Layer result, therefore they are taken from a probing
     * execution of the very same query on the HAC, limited to a single row.
     */
    private suspend fun executeOnServiceLayer(context: FlexibleSearchExecContext): FlexibleSearchExecResult {
        val probe = executeOnHac(context.copy(maxCount = 1))
        val headers = probe.headers
            ?.takeIf { probe.statusCode == HttpStatus.SC_OK }
            ?: return probe

        val script = readResource(FlexibleSearchExecConstants.Scripts.EXECUTE)
            .replace(FlexibleSearchExecConstants.Scripts.PLACEHOLDER_QUERY, context.content)
            .replace(FlexibleSearchExecConstants.Scripts.PLACEHOLDER_COLUMN_COUNT, headers.size.toString())
            .replace(FlexibleSearchExecConstants.Scripts.PLACEHOLDER_MAX_COUNT, context.maxCount.toString())

        val groovyResult = GroovyExecClient.getInstance(project).execute(
            GroovyExecContext(
                connection = context.connection,
                executionTitle = context.executionTitle,
                content = script,
                transactionMode = TransactionMode.ROLLBACK,
                timeout = context.timeout,
            )
        )

        if (groovyResult.hasError) return FlexibleSearchExecResult(
            statusCode = HttpStatus.SC_BAD_REQUEST,
            errorMessage = groovyResult.errorMessage,
            errorDetailMessage = groovyResult.errorDetailMessage,
        )

        val rows = groovyResult.result
            ?.let { parseRows(it) }
            ?: return FlexibleSearchExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                errorMessage = "Cannot parse rows returned by the Service Layer execution of the query",
            )

        return FlexibleSearchExecResult(
            output = buildTableResult(headers, rows),
            headers = headers,
            rows = rows,
        )
    }

    private suspend fun executeOnHac(context: FlexibleSearchExecContext): FlexibleSearchExecResult {
        val connection = context.connection
        val actionUrl = "${connection.generatedURL}/console/flexsearch/execute"
        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }

        val response = HacHttpClient.getInstance(project)
            .post(actionUrl, params, true, context.timeout, connection, null)
        val statusLine = response.statusLine
        val statusCode = statusLine.statusCode

        if (statusCode != HttpStatus.SC_OK || response.entity == null) return FlexibleSearchExecResult(
            statusCode = HttpStatus.SC_BAD_REQUEST,
            errorMessage = "[$statusCode] ${statusLine.reasonPhrase}",
        )

        try {
            val json = withContext(Dispatchers.IO) {
                response.entity.content.readAllBytes()
            }
                .toString(Charsets.UTF_8)
                .let { Gson().fromJson(it, HashMap::class.java) }

            val rawHeaders = json["headers"].asSafely<MutableList<String>>()
            val rawRows = json["resultList"].asSafely<List<List<String>>>()

            return json["exception"]
                ?.asSafely<MutableMap<*, *>>()
                ?.let { it["message"] }
                ?.toString()
                ?.let {
                    FlexibleSearchExecResult(
                        statusCode = HttpStatus.SC_BAD_REQUEST,
                        errorMessage = it
                    )
                }
                ?: FlexibleSearchExecResult(
                    output = buildTableResult(rawHeaders, rawRows),
                    headers = rawHeaders,
                    rows = rawRows,
                )
        } catch (e: Exception) {
            return FlexibleSearchExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                errorMessage = "Cannot parse response from the server: ${e.message} $actionUrl"
            )
        }
    }

    /**
     * Rows of the Service Layer execution as a json array of the arrays of the nullable column values.
     */
    private fun parseRows(json: String): List<List<String>>? = runCatching {
        Gson().fromJson(json, Array<Array<String?>>::class.java)
            .map { row -> row.map { value -> value ?: "" } }
    }.getOrNull()

    private fun buildTableResult(headers: List<String>?, rows: List<List<String>>?): String {
        val tableBuilder = TableBuilder()
        headers?.let { tableBuilder.addHeaders(it) }
        rows?.forEach { row -> tableBuilder.addRow(row) }
        return tableBuilder.toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1238922198933240517L
        fun getInstance(project: Project): FlexibleSearchExecClient = project.service()
    }

}