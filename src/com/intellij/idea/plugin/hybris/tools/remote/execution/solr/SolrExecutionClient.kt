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

package com.intellij.idea.plugin.hybris.tools.remote.execution.solr

import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
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
import java.io.Serial

@Service(Service.Level.PROJECT)
class SolrExecutionClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecutionClient<SolrQueryExecutionContext>(project, coroutineScope) {

    fun coresData(): Array<SolrCoreData> = coresData(solrConnectionSettings(project))

    fun listOfCores(solrConnectionSettings: RemoteConnectionSettings) = coresData(solrConnectionSettings)
        .map { it.core }
        .toTypedArray()

    override suspend fun execute(context: SolrQueryExecutionContext): DefaultExecutionResult {
        val settings = solrConnectionSettings(project)
        val solrQuery = buildSolrQuery(context)
        val queryRequest = buildQueryRequest(solrQuery, settings)

        return executeSolrRequest(settings, context, queryRequest)
    }

    fun executeSolrQuery(context: SolrQueryExecutionContext) = with(solrConnectionSettings(project)) {
        executeSolrRequest(
            this,
            context,
            buildQueryRequest(buildSolrQuery(context), this)
        )
    }

    private fun coresData(settings: RemoteConnectionSettings) = CoreAdminRequest()
        .apply {
            setAction(CoreAdminParams.CoreAdminAction.STATUS)
            setBasicAuthCredentials(settings.username, settings.password)
        }
        .runCatching { process(buildHttpSolrClient(settings.generatedURL)) }
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

    private fun buildHttpSolrClient(url: String) = HttpSolrClient.Builder(url).build()

    private fun executeSolrRequest(solrConnectionSettings: RemoteConnectionSettings, queryObject: SolrQueryExecutionContext, queryRequest: QueryRequest): DefaultExecutionResult {
        val result = DefaultExecutionResult(
            remoteConnectionType = RemoteConnectionType.SOLR
        )
        return buildHttpSolrClient("${solrConnectionSettings.generatedURL}/${queryObject.core}")
            .runCatching { request(queryRequest) }
            .map {
                result.output = it["response"] as String
                result
            }
            .getOrElse {
                result.errorMessage = it.message
                result.statusCode = HttpStatus.SC_BAD_GATEWAY
                result
            }
    }

    private fun buildQueryRequest(solrQuery: SolrQuery, solrConnectionSettings: RemoteConnectionSettings) = QueryRequest(solrQuery).apply {
        setBasicAuthCredentials(solrConnectionSettings.username, solrConnectionSettings.password)
        method = SolrRequest.METHOD.POST
        // https://issues.apache.org/jira/browse/SOLR-5530
        // https://stackoverflow.com/questions/28374428/return-solr-response-in-json-format/37212234#37212234
        responseParser = NoOpResponseParser("json")
    }

    private fun buildSolrQuery(queryObject: SolrQueryExecutionContext) = SolrQuery().apply {
        rows = queryObject.rows
        query = queryObject.content
        setParam("wt", "json")
    }

    // active or default
    private fun solrConnectionSettings(project: Project) = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.SOLR)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4606760283632482489L
    }
}