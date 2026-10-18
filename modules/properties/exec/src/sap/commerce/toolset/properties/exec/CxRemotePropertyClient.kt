/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.exec

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import sap.commerce.toolset.extensions.ExtensionsService
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.CxPropertyConstants
import sap.commerce.toolset.properties.CxPropertyKey
import sap.commerce.toolset.settings.state.TransactionMode

/**
 * Talks to the `configstore` of a remote SAP Commerce instance.
 *
 * Everything here is a single request which suspends until it has an answer, and nothing here remembers anything: the
 * accumulated view a tool window scrolls through, and the notifications that go with it, belong a layer up.
 */
@Service(Service.Level.PROJECT)
class CxRemotePropertyClient(private val project: Project) {

    /**
     * One page of the instance's properties.
     *
     * Both filters are applied by the instance rather than here, which is the difference between one response and
     * every property it has.
     */
    suspend fun fetch(
        connection: HacConnectionSettingsState,
        page: Int = 1,
        pageSize: Int = CxPropertyConstants.DEFAULT_PAGE_SIZE,
        keyFilter: String = "",
        valueFilter: String = "",
    ): CxRemotePropertyStatePage? {
        val script = ExtensionsService.getInstance()
            .findResource(CxPropertyConstants.EXTENSION_STATE_SCRIPT)
            .replace(CxPropertyConstants.PAGE_PLACEHOLDER, page.coerceAtLeast(1).toString())
            .replace(CxPropertyConstants.PAGE_SIZE_PLACEHOLDER, pageSize.coerceAtLeast(1).toString())
            .replace(CxPropertyConstants.KEY_FILTER_PLACEHOLDER, CxGroovyStringLiteral.escape(keyFilter.trim()))
            .replace(CxPropertyConstants.VALUE_FILTER_PLACEHOLDER, CxGroovyStringLiteral.escape(valueFilter.trim()))

        val context = GroovyExecContext(
            connection = connection,
            executionTitle = "Fetching Properties from SAP Commerce [${connection.shortenConnectionName}]...",
            content = script,
            transactionMode = TransactionMode.ROLLBACK,
            timeout = connection.timeout,
        )

        val result = GroovyExecClient.getInstance(project).execute(context)

        if (result.hasError) error(result.errorMessage ?: "Unable to retrieve properties from ${connection.connectionName}.")

        return result.result?.let { CxRemotePropertyPayload.parse(it) }
    }

    suspend fun upsert(connection: HacConnectionSettingsState, key: String, value: String): CxRemotePropertyWriteResult {
        val trimmedKey = key.trim()

        return post(connection, "configstore", BasicNameValuePair("key", trimmedKey), BasicNameValuePair("val", value))
    }

    suspend fun delete(connection: HacConnectionSettingsState, key: String): CxRemotePropertyWriteResult {
        val trimmedKey = key.trim()

        return post(connection, "configdelete", BasicNameValuePair("key", trimmedKey))
    }

    private suspend fun post(
        connection: HacConnectionSettingsState,
        action: String,
        vararg params: BasicNameValuePair,
    ): CxRemotePropertyWriteResult {
        val key = params.first().value
        if (!isValidPropertyKey(key)) return CxRemotePropertyWriteResult.failed("'$key' is not a valid property key")

        val response = HacHttpClient.getInstance(project).post(
            "${connection.generatedURL}/platform/$action",
            params.toList(),
            true,
            connection.timeout,
            connection,
            null,
        )

        return if (response.statusLine.statusCode == HttpStatus.SC_OK) CxRemotePropertyWriteResult.SUCCESS
        else CxRemotePropertyWriteResult.failed(response.statusLine.reasonPhrase)
    }

    companion object {
        fun getInstance(project: Project): CxRemotePropertyClient = project.service()

        /** A key the `configstore` would reject out of hand, so there is no point spending a request on it. */
        fun isValidPropertyKey(key: String) = CxPropertyKey.isValid(key)
    }
}
