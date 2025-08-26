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

package sap.commerce.toolset.logging.template

import com.google.gson.Gson
import sap.commerce.toolset.extensions.ExtensionsService
import sap.commerce.toolset.logging.CxLoggerModel
import sap.commerce.toolset.logging.CxLoggersConstants

object CxLoggersTemplatesAccess {

    fun bundledLoggerTemplates() = ExtensionsService.getInstance().findResource(CxLoggersConstants.CX_LOGGERS_BUNDLED)
        .let { Gson().fromJson(it, TemplatesConfigurationDto::class.java) }
        .templates
        .takeIf { it.isNotEmpty() }
        ?.associate { item ->
            item.name to item.loggers.map { logConfig -> CxLoggerModel.of(logConfig.identifier, logConfig.effectiveLevel) }
        }
        ?: emptyMap()
}