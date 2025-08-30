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

import com.intellij.openapi.util.text.StringUtil
import sap.commerce.toolset.exec.generateUrl

interface ConnectionSettingsState {
    val uuid: String
    val scope: ExecConnectionScope
    val name: String?
    val ssl: Boolean
    val timeout: Int
    val host: String
    val port: String?
    val webroot: String

    val generatedURL: String
        get() = generateUrl(ssl, host, port, webroot)

    val presentationName: String
        get() = (name
            ?.takeIf { it.isNotBlank() }
            ?: generatedURL
                .replace("-public.model-t.cc.commerce.ondemand.com", StringUtil.THREE_DOTS)
                .takeIf { it.isNotBlank() }
            )
            .let { scope.shortTitle + " : " + it }

    val connectionName: String
        get() = name ?: generatedURL

    val shortenConnectionName: String
        get() = connectionName
            .let { StringUtil.shortenPathWithEllipsis(it, 20) }
}