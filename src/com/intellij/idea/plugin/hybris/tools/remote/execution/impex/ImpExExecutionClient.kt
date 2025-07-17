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
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionResult
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
class ImpExExecutionClient(project: Project, coroutineScope: CoroutineScope) : ExecutionClient<ImpExExecutionContext>(project, coroutineScope) {

    override suspend fun execute(context: ImpExExecutionContext): ExecutionResult {
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
        val resultBuilder = ExecutionResult.builder()
            .remoteConnectionType(RemoteConnectionType.Hybris)
            .httpCode(statusLine.statusCode)

        if (statusLine.statusCode != HttpStatus.SC_OK) {
            return resultBuilder
                .errorMessage(statusLine.reasonPhrase)
                .build()
        }

        try {
            val document = Jsoup.parse(response.entity.content, StandardCharsets.UTF_8.name(), "")

            when (context.executionMode) {
                ExecutionMode.IMPORT -> processImportResponse(document, resultBuilder)
                ExecutionMode.VALIDATE -> processValidateResponse(document, resultBuilder)
            }
        } catch (e: IOException) {
            thisLogger().warn(e.message, e)

            resultBuilder.errorMessage(e.message)
        }

        return resultBuilder.build()
    }

    private fun processImportResponse(document: Document, resultBuilder: ExecutionResult.Builder) {
        document.getElementById("impexResult")
            ?.takeIf { it.hasAttr("data-level") && it.hasAttr("data-result") }
            ?.let { resultElement ->
                val dataResult = resultElement.attr("data-result")
                if (resultElement.attr("data-level") == "error") {
                    document.getElementsByClass("impexResult")
                        .first()?.children()?.first()?.text()
                        ?.let { it ->
                            resultBuilder
                                .errorMessage(dataResult)
                                .detailMessage(it)
                        }
                        ?: resultBuilder.errorMessage("No data in response")
                } else {
                    resultBuilder.output(dataResult)
                }
            }
            ?: resultBuilder.errorMessage("No data in response")
    }

    private fun processValidateResponse(document: Document, resultBuilder: ExecutionResult.Builder) {
        document.getElementById("validationResultMsg")
            ?.takeIf { it.hasAttr("data-level") && it.hasAttr("data-result") }
            ?.let {
                if ("error" == it.attr("data-level")) {
                    val dataResult = it.attr("data-result")
                    resultBuilder.errorMessage(dataResult).build()
                } else {
                    val dataResult = it.attr("data-result")
                    resultBuilder.output(dataResult)
                }
            }
            ?: resultBuilder.errorMessage("No data in response")
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1646069318244320642L
    }

}