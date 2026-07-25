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

import com.intellij.mcpserver.project
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.shared.mcp.transform.dto.TransformerDto
import sap.commerce.toolset.shared.mcp.transform.dto.TransformersDto
import sap.commerce.toolset.shared.mcp.transform.dto.TransformersResultDto
import sap.commerce.toolset.transform.TransformationResult
import sap.commerce.toolset.transform.Transformer

@Service(Service.Level.PROJECT)
class TransformerMcpService {

    fun list(languageId: String?): TransformersResultDto {
        val all = Transformer.EP.extensionList

        val filtered = if (languageId != null) {
            all.filter { t ->
                t.fileType.language.id.equals(languageId, ignoreCase = true)
                    || t.fileType.language.displayName.equals(languageId, ignoreCase = true)
            }
        } else {
            all
        }

        val transformers = filtered
            .groupBy { it.fileType.language }
            .map { (language, transformers) ->
                TransformersDto(
                    languageId = language.id,
                    displayName = language.displayName,
                    transformers = transformers.map { it.mcpDto },
                )
            }

        return TransformersResultDto(transformers = transformers)
    }

    val Transformer<in PsiFile, out TransformationResult>.mcpDto: TransformerDto
        get() = TransformerDto(id, fileType.name, description)

    companion object {
        suspend fun getInstance(): TransformerMcpService = currentCoroutineContext().project.service()
    }
}