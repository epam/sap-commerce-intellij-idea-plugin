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

package com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch

import com.google.gson.Gson
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import java.io.Serial
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class FlexibleSearchExecutionClient(project: Project, coroutineScope: CoroutineScope) : ExecutionClient<FlexibleSearchExecutionContext>(project, coroutineScope) {

    override suspend fun execute(context: FlexibleSearchExecutionContext): ExecutionResult {
        val settings = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val params = context.params(settings)
            .map { BasicNameValuePair(it.key, it.value) }
        val actionUrl = "${settings.generatedURL}/console/flexsearch/execute"

        val response = HybrisHacHttpClient.getInstance(project)
            .post(actionUrl, params, true, context.timeout, settings, null)
        val statusLine = response.statusLine

        val resultBuilder = ExecutionResult.builder()
            .remoteConnectionType(RemoteConnectionType.Hybris)
            .httpCode(statusLine.statusCode)

        if (statusLine.statusCode != HttpStatus.SC_OK || response.entity == null) {
            resultBuilder
                .badRequest()
                .errorMessage("[${statusLine.statusCode}] ${statusLine.reasonPhrase}")
        } else {
            try {
                val json = response.entity.content
                    .readAllBytes()
                    .toString(StandardCharsets.UTF_8)
                    .let { Gson().fromJson(it, HashMap::class.java) }

                json["exception"]
                    ?.asSafely<MutableMap<*, *>>()
                    ?.let { it["message"] }
                    ?.toString()
                    ?.let {
                        resultBuilder
                            .badRequest()
                            .errorMessage(it)
                    }
                    ?: resultBuilder.output(buildTableResult(json))
            } catch (e: Exception) {
                resultBuilder
                    .badRequest()
                    .errorMessage("Cannot parse response from the server: ${e.message} $actionUrl")
            }
        }

        return resultBuilder.build()
    }

    private fun buildTableResult(json: HashMap<*, *>): String {
        val tableBuilder = TableBuilder()

        json["headers"].asSafely<MutableList<String>>()
            ?.let { headers -> tableBuilder.addHeaders(headers) }
        json["resultList"].asSafely<List<List<String>>>()
            ?.forEach { row -> tableBuilder.addRow(row) }

        return tableBuilder.toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1238922198933240517L
    }

}