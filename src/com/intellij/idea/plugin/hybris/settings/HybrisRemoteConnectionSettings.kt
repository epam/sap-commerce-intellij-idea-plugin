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

package com.intellij.idea.plugin.hybris.settings

import java.io.Serializable

class HybrisRemoteConnectionSettings : Serializable {

    var isSsl: Boolean = false
    var uuid: String? = null
    var displayName: String? = null
    var hostIP: String? = null
    var port: String? = null
    var adminLogin: String? = null
    var adminPassword: String? = null
    var sslProtocol: String? = null
    var generatedURL: String? = null
    var solrWebroot: String? = null
    var hacWebroot: String? = null
    var hacLogin: String? = null
    var hacPassword: String? = null
    var type: Type = Type.Hybris


    override fun toString() = displayName
        ?.takeIf { it.isNotBlank() }
        ?: generatedURL
            ?.takeIf { it.isNotBlank() }
        ?: super.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HybrisRemoteConnectionSettings) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid?.hashCode() ?: 0
    }

    companion object {
        private const val serialVersionUID: Long = -5898943547737648391L
    }

    enum class Type {
        Hybris, SOLR
    }
}