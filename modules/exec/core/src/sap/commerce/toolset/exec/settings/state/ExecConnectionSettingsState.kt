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

package sap.commerce.toolset.exec.settings.state

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.util.text.StringUtil
import sap.commerce.toolset.exec.generateUrl

interface ExecConnectionSettingsState {
    val scope: ExecConnectionScope
    val uuid: String
    val name: String?
    val ssl: Boolean
    val host: String
    val port: String?
    val webroot: String
    val timeout: Int

    val credentials: Credentials?
    val username: String
    val password: String

    fun mutable(): Mutable

    interface Mutable {
        var scope: ExecConnectionScope
        var uuid: String
        var name: String?
        var ssl: Boolean
        var timeout: Int
        var host: String
        var port: String?
        var webroot: String
        var username: String
        var password: String

        fun immutable(): ExecConnectionSettingsState
    }
}

val ExecConnectionSettingsState.generatedURL: String
    get() = generateUrl(ssl, host, port, webroot)

val ExecConnectionSettingsState.presentationName: String
    get() = (name
        ?.takeIf { it.isNotBlank() }
        ?: generatedURL
            .replace("-public.model-t.cc.commerce.ondemand.com", StringUtil.THREE_DOTS)
            .takeIf { it.isNotBlank() }
        )
        .let { scope.shortTitle + " : " + it }

val ExecConnectionSettingsState.connectionName: String
    get() = name ?: generatedURL

val ExecConnectionSettingsState.shortenConnectionName: String
    get() = connectionName
        .let { StringUtil.shortenPathWithEllipsis(it, 20) }