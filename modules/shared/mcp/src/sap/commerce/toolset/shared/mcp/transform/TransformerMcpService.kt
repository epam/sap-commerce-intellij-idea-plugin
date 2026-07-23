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

package sap.commerce.toolset.shared.mcp.transform

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.shared.mcp.transform.dto.LanguageTransformers
import sap.commerce.toolset.shared.mcp.transform.dto.TransformerInfo
import sap.commerce.toolset.shared.mcp.transform.dto.TransformerMcpResult
import sap.commerce.toolset.transform.Transformer

@Service(Service.Level.PROJECT)
class TransformerMcpService {

    fun list(languageId: String?): TransformerMcpResult {
        val all = Transformer.EP.extensionList

        val filtered = if (languageId != null) {
            all.filter { t ->
                t.language.id.equals(languageId, ignoreCase = true) ||
                t.language.displayName.equals(languageId, ignoreCase = true)
            }
        } else {
            all
        }

        val languages = filtered
            .groupBy { it.language }
            .map { (language, transformers) ->
                LanguageTransformers(
                    languageId = language.id,
                    displayName = language.displayName,
                    transformers = transformers.map { t ->
                        TransformerInfo(id = t.id, name = t.name, description = t.description)
                    },
                )
            }

        return TransformerMcpResult(languages = languages)
    }

    companion object {
        fun getInstance(project: Project): TransformerMcpService = project.service()
    }
}