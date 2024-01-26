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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.util.xmlb.Accessor
import com.intellij.util.xmlb.SerializationFilter
import java.util.*

class HybrisRemoteConnectionSettings : SerializationFilter {

    var shared: Boolean = false
    var isSsl: Boolean = true
    var uuid: String = UUID.randomUUID().toString()
    var displayName: String? = null
    var hostIP: String = HybrisConstants.DEFAULT_HOST_URL
    var port: String? = null
    var adminLogin: String? = null
    var adminPassword: String?
        set(value) {
            val credentialAttributes = CredentialAttributes("SAP CX - SOLR - $uuid", hacLogin)
            PasswordSafe.instance.setPassword(credentialAttributes, value)
        }
        get() {
            val credentialAttributes = CredentialAttributes("SAP CX - HAC - $uuid", hacLogin)
            return PasswordSafe.instance.getPassword(credentialAttributes)
                ?: "nimda"
        }
    var sslProtocol: String? = null
    var solrWebroot: String = "solr"
    var hacWebroot: String = ""
    var hacLogin: String = "admin"
    var hacPassword: String
        set(value) {
            val credentialAttributes = CredentialAttributes("SAP CX - HAC - $uuid", hacLogin)
            PasswordSafe.instance.setPassword(credentialAttributes, value)
        }
        get() {
            val credentialAttributes = CredentialAttributes("SAP CX - HAC - $uuid", hacLogin)
            return PasswordSafe.instance.getPassword(credentialAttributes)
                ?: "nimda"
        }
    var type: Type = Type.Hybris
    val generatedURL: String
        get() {
            return when (type) {
                Type.Hybris -> RemoteConnectionUtil.generateUrl(isSsl, hostIP, port, hacWebroot)
                Type.SOLR -> RemoteConnectionUtil.generateUrl(isSsl, hostIP, port, solrWebroot)
            }
        }

    override fun toString() = displayName
        ?.takeIf { it.isNotBlank() }
        ?: generatedURL
            .takeIf { it.isNotBlank() }
        ?: super.toString()

    override fun accepts(accessor: Accessor, bean: Any) = accessor.name != "adminPassword"
        && accessor.name != "hacPassword"
        && accessor.name != "shared"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HybrisRemoteConnectionSettings) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode() = uuid.hashCode()

    enum class Type {
        Hybris, SOLR
    }
}