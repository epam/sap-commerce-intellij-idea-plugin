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

package sap.commerce.toolset.logging.bundled

import com.google.gson.Gson
import com.intellij.openapi.components.Service
import com.intellij.util.ResourceUtil
import com.intellij.util.application
import sap.commerce.toolset.logging.presentation
import sap.commerce.toolset.logging.presentation.CxLogTemplatePresentation
import java.io.InputStreamReader

@Service
class CxBundledLogTemplateService {

    fun getTemplates(): List<CxLogTemplatePresentation> = ResourceUtil.getResourceAsStream(
        this.javaClass.classLoader,
        "cx-loggers",
        "templates.json"
    )
        .use { input ->
            InputStreamReader(input, Charsets.UTF_8).use { reader ->
                Gson().fromJson(reader, CxBundledLogTemplates::class.java)
            }
        }
        .templates
        .takeIf { it.isNotEmpty() }
        ?.map { it.presentation() }
        ?: emptyList()

    companion object {
        fun getInstance(): CxBundledLogTemplateService = application.getService(CxBundledLogTemplateService::class.java)
    }
}