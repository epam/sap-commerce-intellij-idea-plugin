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

package com.intellij.idea.plugin.hybris.tools.remote.http.flexibleSearch

import com.google.gson.Gson
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.http.HttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHttpResult
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHttpResult.HybrisHttpResultBuilder
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class FlexibleSearchHttpClient(project: Project, coroutineScope: CoroutineScope) : HttpClient<FlexibleSearchExecutionContext, HybrisHttpResult>(project, coroutineScope) {

    override suspend fun execute(context: FlexibleSearchExecutionContext): HybrisHttpResult {
        val settings = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        val params = context.params(settings)
            .map { BasicNameValuePair(it.key, it.value) }
        val actionUrl = "${settings.generatedURL}/console/flexsearch/execute"

        val response = HybrisHacHttpClient.getInstance(project)
            .post(actionUrl, params, true, context.timeout.toLong(), settings, null)
        val statusLine = response.statusLine
        val resultBuilder = when {
            statusLine.statusCode != HttpStatus.SC_OK || response.entity == null -> HybrisHttpResultBuilder.createResult()
                .badRequest()
                .errorMessage("[${statusLine.statusCode}] ${statusLine.reasonPhrase}")

            else -> parseResponse(response, actionUrl)
        }

        return resultBuilder.build()
    }

    private fun parseResponse(response: HttpResponse, actionUrl: String) = try {
        val json = response.entity.content
            .readAllBytes()
            .toString(StandardCharsets.UTF_8)
            .let { Gson().fromJson(it, HashMap::class.java) }

        json["exception"]
            ?.asSafely<MutableMap<*, *>>()
            ?.let { it["message"] }
            ?.toString()
            ?.let {
                HybrisHttpResultBuilder.createResult()
                    .badRequest()
                    .errorMessage(it)
            }
            ?: HybrisHttpResultBuilder.createResult()
                .output(buildTableResult(json))
    } catch (e: Exception) {
        HybrisHttpResultBuilder.createResult()
            .badRequest()
            .errorMessage("Cannot parse response from the server: ${e.message} $actionUrl")
    }

    private fun buildTableResult(json: HashMap<*, *>): String {
        val tableBuilder = TableBuilder()

        json["headers"].asSafely<MutableList<String>>()
            ?.let { headers -> tableBuilder.addHeaders(headers) }
        json["resultList"].asSafely<List<List<String>>>()
            ?.forEach { row -> tableBuilder.addRow(row) }

        return tableBuilder.toString()

    }

}