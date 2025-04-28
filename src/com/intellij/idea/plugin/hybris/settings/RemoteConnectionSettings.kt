/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionScope
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.Accessor
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.Transient

@Tag("HybrisRemoteConnectionSettings")
class RemoteConnectionSettings : BaseState(), Comparable<RemoteConnectionSettings> {

    var uuid by string(null)

    @Transient
    var credentials: Credentials? = null
    var displayName by string(null)
    var scope by property(RemoteConnectionScope.PROJECT_PERSONAL) { false }
    var type by property(RemoteConnectionType.Hybris) { false }
    var hostIP by string(HybrisConstants.DEFAULT_HOST_URL)
    var port by string(null)
    var isSsl by property(true)
    var isWsl by property(false)
    var sslProtocol by string(HybrisConstants.DEFAULT_SSL_PROTOCOL)
    var sessionCookieName by string(HybrisConstants.DEFAULT_SESSION_COOKIE_NAME)

    var solrWebroot by string("solr")
    var hacWebroot by string("")

    val username: String
        get() = credentials?.userName
            ?: PasswordSafe.instance.get(CredentialAttributes("SAP CX - $uuid"))
                ?.userName
            ?: if (type == RemoteConnectionType.Hybris) "admin"
            else "solrserver"
    val password: String
        get() = credentials?.getPasswordAsString()
            ?: PasswordSafe.instance.get(CredentialAttributes("SAP CX - $uuid"))
                ?.getPasswordAsString()
            ?: if (type == RemoteConnectionType.Hybris) "nimda"
            else "server123"

    val generatedURL: String
        get() {
            return when (type) {
                RemoteConnectionType.Hybris -> RemoteConnectionUtil.generateUrl(isSsl, hostIP, port, hacWebroot)
                RemoteConnectionType.SOLR -> RemoteConnectionUtil.generateUrl(isSsl, hostIP, port, solrWebroot)
            }
        }

    override fun toString() = (displayName
        ?.takeIf { it.isNotBlank() }
        ?: generatedURL
            .takeIf { it.isNotBlank() }
        ?: super.toString()
        )
        .let { scope.shortTitle + " : " + it }

    override fun accepts(accessor: Accessor, bean: Any) = accessor.name != "credentials"
        && accessor.name != "username"
        && accessor.name != "password"
        && accessor.name != "scope"

    fun connectionName() = displayName ?: generatedURL

    fun shortenConnectionName() = (displayName ?: generatedURL)
        .let { if (it.length > 20) it.take(20) + ".." else it }

    override fun compareTo(other: RemoteConnectionSettings) = uuid
        ?.compareTo(other.uuid ?: "")
        ?: -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RemoteConnectionSettings) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (uuid?.hashCode() ?: 0)
        return result
    }

}