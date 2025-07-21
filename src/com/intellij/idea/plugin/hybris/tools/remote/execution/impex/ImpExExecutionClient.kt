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

package com.intellij.idea.plugin.hybris.tools.remote.execution.impex

import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.io.Serial
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class ImpExExecutionClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecutionClient<ImpExExecutionContext>(project, coroutineScope) {

    override suspend fun execute(context: ImpExExecutionContext): DefaultExecutionResult {
        val settings = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val actionUrl = when (context.executionMode) {
            ExecutionMode.IMPORT -> settings.generatedURL + "/console/impex/import"
            ExecutionMode.VALIDATE -> settings.generatedURL + "/console/impex/import/validate"
        }
        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }

        val response = HybrisHacHttpClient.getInstance(project)
            .post(actionUrl, params, false, context.timeout, settings, null)
        val statusLine = response.statusLine
        val statusCode = statusLine.statusCode

        if (statusCode != HttpStatus.SC_OK || response.entity == null) return DefaultExecutionResult(
            statusCode = statusCode,
            errorMessage = statusLine.reasonPhrase
        )

        try {
            val document = Jsoup.parse(response.entity.content, StandardCharsets.UTF_8.name(), "")

            return when (context.executionMode) {
                ExecutionMode.IMPORT -> processResponse(document, "impexResult") { element ->
                    if (element.attr("data-level") == "error") DefaultExecutionResult(
                        statusCode = HttpStatus.SC_BAD_REQUEST,
                        errorMessage = element.attr("data-result").takeIf { it.isNotBlank() },
                        detailMessage = document.getElementsByClass("impexResult")
                            .first()?.children()?.first()?.text()
                            ?: "No data in response"
                    )
                    else DefaultExecutionResult(
                        output = element.attr("data-result").takeIf { it.isNotBlank() }
                    )
                }

                ExecutionMode.VALIDATE -> processResponse(document, "validationResultMsg") { element ->
                    if ("error" == element.attr("data-level")) DefaultExecutionResult(
                        statusCode = HttpStatus.SC_BAD_REQUEST,
                        errorMessage = element.attr("data-result").takeIf { it.isNotBlank() }
                    )
                    else DefaultExecutionResult(
                        output = element.attr("data-result").takeIf { it.isNotBlank() }
                    )
                }
            }
        } catch (e: IOException) {
            thisLogger().warn(e.message, e)

            return DefaultExecutionResult(
                errorMessage = e.message,
            )
        }
    }

    private fun processResponse(document: Document, id: String, mapper: (Element) -> DefaultExecutionResult) = document.getElementById(id)
        ?.takeIf { it.hasAttr("data-level") && it.hasAttr("data-result") }
        ?.let { mapper.invoke(it) }
        ?: DefaultExecutionResult(
            statusCode = HttpStatus.SC_BAD_REQUEST,
            errorMessage = "No data in response"
        )

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1646069318244320642L

        fun getInstance(project: Project): ImpExExecutionClient = project.service()
    }

}