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
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil.getActiveRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult.HybrisHttpResultBuilder
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class FlexibleSearchHttpClient(private val project: Project, private val coroutineScope: CoroutineScope) : HttpClient<FlexibleSearchExecutionContext, HybrisHttpResult>(project, coroutineScope) {

    override suspend fun execute(context: FlexibleSearchExecutionContext): HybrisHttpResult {
        val hacClient = HybrisHacHttpClient.getInstance(project)
        val settings = getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        val params = context.params(settings)
            .map { BasicNameValuePair(it.key, it.value) }
        val actionUrl = settings.generatedURL + "/console/flexsearch/execute"

        val response: HttpResponse = hacClient.post(actionUrl, params, true, AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT.toLong(), settings, null)
        val statusLine = response.statusLine
        val resultBuilder = HybrisHttpResultBuilder.createResult().httpCode(statusLine.statusCode)
        if (statusLine.statusCode != HttpStatus.SC_OK || response.entity == null) {
            return resultBuilder
                .errorMessage("[${statusLine.statusCode}] ${statusLine.reasonPhrase}")
                .build()
        }

        val json: MutableMap<*, *>?
        try {
            val responseContent = response.entity.content
                .readAllBytes()
                .toString(StandardCharsets.UTF_8)
            json = Gson().fromJson(responseContent, HashMap::class.java)
        } catch (e: Exception) {
            return resultBuilder
                .errorMessage("${e.message} $actionUrl")
                .httpCode(HttpStatus.SC_BAD_REQUEST)
                .build()
        }

        if (json == null) {
            return HybrisHttpResultBuilder.createResult()
                .errorMessage("Cannot parse response from the server...")
                .build()
        }

        if (json["exception"] != null) {
            return HybrisHttpResultBuilder.createResult()
                .errorMessage((json["exception"] as MutableMap<*, *>)["message"].toString())
                .build()
        }

        val tableBuilder = TableBuilder()

        val headers = json["headers"] as MutableList<String>
        val resultList = json["resultList"] as List<List<String>>

        tableBuilder.addHeaders(headers)
        resultList.forEach { tableBuilder.addRow(it) }

        return resultBuilder
            .output(tableBuilder.toString())
            .build()
    }

}