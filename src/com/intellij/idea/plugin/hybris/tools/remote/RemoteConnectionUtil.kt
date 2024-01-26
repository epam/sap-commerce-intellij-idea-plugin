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

package com.intellij.idea.plugin.hybris.tools.remote

import com.intellij.idea.plugin.hybris.common.HybrisConstants

object RemoteConnectionUtil {

    fun generateUrl(ssl: Boolean, host: String?, port: String?, webroot: String?) = buildString {
        if (ssl) append(HybrisConstants.HTTPS_PROTOCOL)
        else append(HybrisConstants.HTTP_PROTOCOL)
        append(host?.trim() ?: "")
        port
            ?.takeIf { it.isNotBlank() }
            ?.takeUnless { "443" == it && ssl }
            ?.takeUnless { "80" == it && !ssl }
            ?.let {
                append(HybrisConstants.URL_PORT_DELIMITER)
                append(it)
            }
            ?: ""
        webroot
            ?.takeUnless { it.isBlank() }
            ?.let {
                append('/')
                append(
                    it
                        .trimStart(' ', '/')
                        .trimEnd(' ', '/')
                )
            }
            ?: ""
    }
}