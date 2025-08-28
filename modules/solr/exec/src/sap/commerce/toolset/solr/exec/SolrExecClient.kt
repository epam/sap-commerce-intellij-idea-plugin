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

package sap.commerce.toolset.solr.exec

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import com.intellij.util.containers.mapSmartNotNull
import kotlinx.coroutines.CoroutineScope
import org.apache.http.HttpStatus
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrRequest
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.impl.NoOpResponseParser
import org.apache.solr.client.solrj.request.CoreAdminRequest
import org.apache.solr.client.solrj.request.QueryRequest
import org.apache.solr.client.solrj.response.CoreAdminResponse
import org.apache.solr.common.params.CoreAdminParams
import org.apache.solr.common.util.NamedList
import sap.commerce.toolset.exec.DefaultExecClient
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.exec.settings.state.generatedURL
import sap.commerce.toolset.solr.exec.context.SolrCoreData
import sap.commerce.toolset.solr.exec.context.SolrQueryExecContext
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import java.io.Serial

@Service(Service.Level.PROJECT)
class SolrExecClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecClient<SolrQueryExecContext>(project, coroutineScope) {

    override suspend fun execute(context: SolrQueryExecContext): DefaultExecResult {
        val settings = context.connection
        val solrQuery = buildSolrQuery(context)
        val queryRequest = buildQueryRequest(solrQuery, settings)
        val url = "${settings.generatedURL}/${context.core}"

        return buildHttpSolrClient(settings, url)
            .runCatching { request(queryRequest) }
            .map { namedList ->
                DefaultExecResult(
                    output = (namedList["response"] as String).takeIf { it.isNotBlank() }
                )
            }
            .getOrElse {
                DefaultExecResult(
                    errorMessage = it.message,
                    statusCode = HttpStatus.SC_BAD_GATEWAY
                )
            }
    }

    fun listOfCores(solrConnectionSettings: SolrConnectionSettingsState) = coresData(solrConnectionSettings)
        .map { it.core }
        .toTypedArray()

    fun coresData(settings: SolrConnectionSettingsState) = CoreAdminRequest()
        .apply {
            setAction(CoreAdminParams.CoreAdminAction.STATUS)
            setBasicAuthCredentials(settings.username, settings.password)
        }
        .runCatching { process(buildHttpSolrClient(settings, settings.generatedURL)) }
        .map { parseCoreResponse(it) }
        .getOrElse {
            throw it
        }

    private fun parseCoreResponse(response: CoreAdminResponse) = response
        .coreStatus
        .asShallowMap()
        .values
        .asSafely<Collection<Map<Any, Any>>>()
        ?.mapSmartNotNull { buildSolrCoreData(it) }
        ?.toTypedArray()
        ?: emptyArray()

    private fun buildSolrCoreData(it: Map<Any, Any>) = SolrCoreData(
        it["name"] as String,
        (it["index"] as NamedList<*>)["numDocs"] as Int
    )

    private fun buildHttpSolrClient(settings: SolrConnectionSettingsState, url: String) = HttpSolrClient.Builder(url)
        .withConnectionTimeout(settings.timeout)
        .withSocketTimeout(settings.socketTimeout)
        .build()

    private fun buildQueryRequest(solrQuery: SolrQuery, solrConnectionSettings: SolrConnectionSettingsState) = QueryRequest(solrQuery).apply {
        setBasicAuthCredentials(solrConnectionSettings.username, solrConnectionSettings.password)
        method = SolrRequest.METHOD.POST
        // https://issues.apache.org/jira/browse/SOLR-5530
        // https://stackoverflow.com/questions/28374428/return-solr-response-in-json-format/37212234#37212234
        responseParser = NoOpResponseParser("json")
    }

    private fun buildSolrQuery(queryObject: SolrQueryExecContext) = SolrQuery().apply {
        rows = queryObject.rows
        query = queryObject.content
        setParam("wt", "json")
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4606760283632482489L

        fun getInstance(project: Project): SolrExecClient = project.service()
    }
}