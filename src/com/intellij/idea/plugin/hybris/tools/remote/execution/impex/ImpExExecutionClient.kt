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
import java.io.IOException
import java.io.Serial
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class ImpExExecutionClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecutionClient<ImpExExecutionContext>(project, coroutineScope) {

    override suspend fun execute(context: ImpExExecutionContext): DefaultExecutionResult {
        val settings = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)

        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }
        val actionUrl = when (context.executionMode) {
            ExecutionMode.IMPORT -> settings.generatedURL + "/console/impex/import"
            ExecutionMode.VALIDATE -> settings.generatedURL + "/console/impex/import/validate"
        }

        val response = HybrisHacHttpClient.getInstance(project)
            .post(actionUrl, params, false, HybrisHacHttpClient.DEFAULT_HAC_TIMEOUT, settings, null)

        val statusLine = response.statusLine

        if (statusLine.statusCode != HttpStatus.SC_OK) {
            return DefaultExecutionResult(
                statusCode = statusLine.statusCode,
                errorMessage = statusLine.reasonPhrase
            )
        }

        val result = DefaultExecutionResult(
            statusCode = statusLine.statusCode
        )

        try {
            val document = Jsoup.parse(response.entity.content, StandardCharsets.UTF_8.name(), "")

            when (context.executionMode) {
                ExecutionMode.IMPORT -> processImportResponse(document, result)
                ExecutionMode.VALIDATE -> processValidateResponse(document, result)
            }
        } catch (e: IOException) {
            thisLogger().warn(e.message, e)

            result.errorMessage = e.message
        }

        return result
    }

    private fun processImportResponse(document: Document, result: DefaultExecutionResult) {
        document.getElementById("impexResult")
            ?.takeIf { it.hasAttr("data-level") && it.hasAttr("data-result") }
            ?.let { resultElement ->
                val dataResult = resultElement.attr("data-result")
                if (resultElement.attr("data-level") == "error") {
                    document.getElementsByClass("impexResult")
                        .first()?.children()?.first()?.text()
                        ?.let { it ->
                            result.errorMessage = dataResult
                            result.detailMessage = it
                        }
                        ?: "No data in response".let { result.errorMessage = it }
                } else {
                    result.output = dataResult
                }
            }
            ?: "No data in response".let { result.errorMessage = it }

    }

    private fun processValidateResponse(document: Document, result: DefaultExecutionResult) {
        document.getElementById("validationResultMsg")
            ?.takeIf { it.hasAttr("data-level") && it.hasAttr("data-result") }
            ?.let {
                if ("error" == it.attr("data-level")) {
                    val dataResult = it.attr("data-result")
                    result.errorMessage = dataResult
                } else {
                    val dataResult = it.attr("data-result")
                    result.output = dataResult
                }
            }
            ?: "No data in response".let { result.errorMessage = it }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1646069318244320642L
    }

}