/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrCoreData
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrQueryObject
import com.intellij.openapi.project.Project
import com.intellij.util.castSafelyTo
import com.intellij.util.containers.mapSmartNotNull
import org.apache.http.HttpStatus
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrRequest
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.impl.NoOpResponseParser
import org.apache.solr.client.solrj.request.CoreAdminRequest
import org.apache.solr.client.solrj.request.QueryRequest
import org.apache.solr.client.solrj.response.CoreAdminResponse
import org.apache.solr.common.params.CoreAdminParams
import org.apache.solr.common.util.NamedList

class DefaultSolrHttpClient : SolrHttpClient {

    override fun coresData(project: Project): Array<SolrCoreData> {
        return coresData(project, solrConnectionSettings(project))
    }

    override fun coresData(project: Project, solrConnectionSettings: HybrisRemoteConnectionSettings): Array<SolrCoreData> {
        return CoreAdminRequest()
                .apply {
                    setAction(CoreAdminParams.CoreAdminAction.STATUS)
                    setBasicAuthCredentials(solrConnectionSettings.adminLogin, solrConnectionSettings.adminPassword)
                }
                .runCatching { process(buildHttpSolrClient(solrConnectionSettings.generatedURL)) }
                .map { parseCoreResponse(it) }
                .getOrElse {
                    throw it
                }
    }

    private fun parseCoreResponse(response: CoreAdminResponse) =
            response.coreStatus.asShallowMap().values.castSafelyTo<Collection<Map<Any, Any>>>()!!.mapSmartNotNull { buildSolrCoreData(it) }.toTypedArray()

    private fun buildSolrCoreData(it: Map<Any, Any>) =
            SolrCoreData(it["name"] as String, (it["index"] as NamedList<*>)["numDocs"] as Int)

    override fun listOfCores(project: Project): Array<String> {
        return listOfCores(project, solrConnectionSettings(project))
    }

    override fun listOfCores(project: Project, solrConnectionSettings: HybrisRemoteConnectionSettings): Array<String> {
        return coresData(project, solrConnectionSettings).map { data -> data.core }.toTypedArray()
    }

    private fun buildHttpSolrClient(url: String): HttpSolrClient {
        return HttpSolrClient.Builder(url).build()
    }

    override fun executeSolrQuery(project: Project,
                                  queryObject: SolrQueryObject): HybrisHttpResult {
        return executeSolrQuery(project, solrConnectionSettings(project), queryObject)
    }

    override fun executeSolrQuery(project: Project,
                                  solrConnectionSettings: HybrisRemoteConnectionSettings,
                                  queryObject: SolrQueryObject): HybrisHttpResult {
        return executeSolrRequest(
                solrConnectionSettings,
                queryObject,
                buildQueryRequest(
                        buildSolrQuery(queryObject),
                        solrConnectionSettings)
        )
    }

    private fun executeSolrRequest(solrConnectionSettings: HybrisRemoteConnectionSettings, queryObject: SolrQueryObject, queryRequest: QueryRequest): HybrisHttpResult {
        return buildHttpSolrClient("${solrConnectionSettings.generatedURL}/${queryObject.core}")
                .runCatching { request(queryRequest) }
                .map { resultBuilder().output(it["response"] as String?).build() }
                .getOrElse { resultBuilder().errorMessage(it.message).httpCode(HttpStatus.SC_BAD_GATEWAY).build() }
    }

    private fun resultBuilder() = HybrisHttpResult.HybrisHttpResultBuilder.createResult()

    private fun buildQueryRequest(solrQuery: SolrQuery, solrConnectionSettings: HybrisRemoteConnectionSettings): QueryRequest {
        return QueryRequest(solrQuery).apply {
            setBasicAuthCredentials(solrConnectionSettings.adminLogin, solrConnectionSettings.adminPassword)
            method = SolrRequest.METHOD.POST
            // https://issues.apache.org/jira/browse/SOLR-5530
            // https://stackoverflow.com/questions/28374428/return-solr-response-in-json-format/37212234#37212234
            responseParser = NoOpResponseParser("json")
        }
    }

    private fun buildSolrQuery(queryObject: SolrQueryObject): SolrQuery {
        return SolrQuery().apply {
            rows = queryObject.rows
            query = queryObject.query
            setParam("wt", "json")
        }
    }

    // active or default
    private fun solrConnectionSettings(project: Project): HybrisRemoteConnectionSettings {
        return HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).getActiveSolrConnectionSettings(project)
    }
}