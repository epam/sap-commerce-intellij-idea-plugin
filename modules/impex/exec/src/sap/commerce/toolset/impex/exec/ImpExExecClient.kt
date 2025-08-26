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

package sap.commerce.toolset.impex.exec

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
import sap.commerce.toolset.exec.DefaultExecClient
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.exec.settings.state.generatedURL
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import sap.commerce.toolset.impex.exec.context.ImpExExecContext
import java.io.IOException
import java.io.Serial
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class ImpExExecClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecClient<ImpExExecContext>(project, coroutineScope) {

    override suspend fun execute(context: ImpExExecContext): DefaultExecResult {
        val settings = context.connection
        val actionUrl = when (context.executionMode) {
            ImpExExecContext.ExecutionMode.IMPORT -> settings.generatedURL + "/console/impex/import"
            ImpExExecContext.ExecutionMode.VALIDATE -> settings.generatedURL + "/console/impex/import/validate"
        }
        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }

        val response = HacHttpClient.getInstance(project)
            .post(actionUrl, params, false, context.settings.timeout, settings, null)
        val statusLine = response.statusLine
        val statusCode = statusLine.statusCode

        if (statusCode != HttpStatus.SC_OK || response.entity == null) return DefaultExecResult(
            statusCode = statusCode,
            errorMessage = statusLine.reasonPhrase
        )

        try {
            val document = Jsoup.parse(response.entity.content, StandardCharsets.UTF_8.name(), "")

            return when (context.executionMode) {
                ImpExExecContext.ExecutionMode.IMPORT -> processResponse(document, "impexResult") { element ->
                    if (element.attr("data-level") == "error") DefaultExecResult(
                        statusCode = HttpStatus.SC_BAD_REQUEST,
                        errorMessage = element.attr("data-result").takeIf { it.isNotBlank() },
                        errorDetailMessage = document.getElementsByClass("impexResult")
                            .first()?.children()?.first()?.text()
                            ?: "No data in response"
                    )
                    else DefaultExecResult(
                        output = element.attr("data-result").takeIf { it.isNotBlank() }
                    )
                }

                ImpExExecContext.ExecutionMode.VALIDATE -> processResponse(document, "validationResultMsg") { element ->
                    if ("error" == element.attr("data-level")) DefaultExecResult(
                        statusCode = HttpStatus.SC_BAD_REQUEST,
                        errorMessage = element.attr("data-result").takeIf { it.isNotBlank() }
                    )
                    else DefaultExecResult(
                        output = element.attr("data-result").takeIf { it.isNotBlank() }
                    )
                }
            }
        } catch (e: IOException) {
            thisLogger().warn(e.message, e)

            return DefaultExecResult(
                errorMessage = e.message,
            )
        }
    }

    private fun processResponse(document: Document, id: String, mapper: (Element) -> DefaultExecResult) = document.getElementById(id)
        ?.takeIf { it.hasAttr("data-level") && it.hasAttr("data-result") }
        ?.let { mapper.invoke(it) }
        ?: DefaultExecResult(
            statusCode = HttpStatus.SC_BAD_REQUEST,
            errorMessage = "No data in response"
        )

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1646069318244320642L

        fun getInstance(project: Project): ImpExExecClient = project.service()
    }

}