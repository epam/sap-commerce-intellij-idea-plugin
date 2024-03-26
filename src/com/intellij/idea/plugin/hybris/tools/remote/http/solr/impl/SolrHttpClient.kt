/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote.http.solr.impl

import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrCoreData
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrQueryObject
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import com.intellij.util.containers.mapSmartNotNull
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

@Service(Service.Level.PROJECT)
class SolrHttpClient {

    fun coresData(project: Project): Array<SolrCoreData> = coresData(solrConnectionSettings(project))

    fun listOfCores(solrConnectionSettings: RemoteConnectionSettings) = coresData(solrConnectionSettings)
        .map { it.core }
        .toTypedArray()

    fun executeSolrQuery(project: Project, queryObject: SolrQueryObject) = executeSolrQuery(solrConnectionSettings(project), queryObject)

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

    private fun executeSolrQuery(
        solrConnectionSettings: RemoteConnectionSettings,
        queryObject: SolrQueryObject
    ): HybrisHttpResult = executeSolrRequest(
        solrConnectionSettings,
        queryObject,
        buildQueryRequest(
            buildSolrQuery(queryObject),
            solrConnectionSettings
        )
    )

    private fun executeSolrRequest(solrConnectionSettings: RemoteConnectionSettings, queryObject: SolrQueryObject, queryRequest: QueryRequest): HybrisHttpResult =
        buildHttpSolrClient("${solrConnectionSettings.generatedURL}/${queryObject.core}")
            .runCatching { request(queryRequest) }
            .map { resultBuilder().output(it["response"] as String?).build() }
            .getOrElse { resultBuilder().errorMessage(it.message).httpCode(HttpStatus.SC_BAD_GATEWAY).build() }

    private fun resultBuilder() = HybrisHttpResult.HybrisHttpResultBuilder.createResult()

    private fun buildQueryRequest(solrQuery: SolrQuery, solrConnectionSettings: RemoteConnectionSettings) = QueryRequest(solrQuery).apply {
        setBasicAuthCredentials(solrConnectionSettings.username, solrConnectionSettings.password)
        method = SolrRequest.METHOD.POST
        // https://issues.apache.org/jira/browse/SOLR-5530
        // https://stackoverflow.com/questions/28374428/return-solr-response-in-json-format/37212234#37212234
        responseParser = NoOpResponseParser("json")
    }

    private fun buildSolrQuery(queryObject: SolrQueryObject) = SolrQuery().apply {
        rows = queryObject.rows
        query = queryObject.query
        setParam("wt", "json")
    }

    // active or default
    private fun solrConnectionSettings(project: Project) = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.SOLR)

    companion object {
        @JvmStatic
        fun getInstance(project: Project): SolrHttpClient = project.getService(SolrHttpClient::class.java)
    }
}