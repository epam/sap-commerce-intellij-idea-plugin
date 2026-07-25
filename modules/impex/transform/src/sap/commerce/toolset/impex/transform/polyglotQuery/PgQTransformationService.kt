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

package sap.commerce.toolset.impex.transform.polyglotQuery

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.transform.context.ImpExTransformationResult
import sap.commerce.toolset.polyglotQuery.psi.PolyglotElementFactory

@Service(Service.Level.PROJECT)
class PgQTransformationService(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) {

    fun transform(
        languageFileType: LanguageFileType,
        element: ImpExValueLine,
        onComplete: (ImpExTransformationResult) -> Unit,
    ) {
        coroutineScope.launch {
            val result = transform(languageFileType, element)
            onComplete(result)
        }
    }

    suspend fun transform(languageFileType: LanguageFileType, element: ImpExValueLine): ImpExTransformationResult {
        val data = readAction { buildTransformData(element) }
            ?: error("cannot extract PSI/meta data")

        val content = buildString {
            append("GET {${data.rootType}}")
            if (data.conditions.isNotEmpty()) {
                append(" WHERE ")
                append(data.conditions.joinToString(" AND "))
            }
        }

        val formattedText = edtWriteAction {
            PolyglotElementFactory.createFile(data.project, content)
                .let { CodeStyleManager.getInstance(data.project).reformat(it) }
                .text
        }

        return ImpExTransformationResult(
            languageName = languageFileType.name,
            content = formattedText,
            exportType = data.rootType,
        )
    }

    private fun buildTransformData(element: ImpExValueLine): TransformData? {
        val header = element.headerLine ?: return null
        val rootType = header.fullHeaderType?.headerTypeName?.text ?: return null
        val project = header.project

        val paramNameCounts = mutableMapOf<String, Int>()
        val conditions = mutableListOf<String>()

        header.uniqueFullHeaderParameters.forEach { param ->
            val attrName = param.parametersContext.rootParameter.name
            val count = paramNameCounts.merge(attrName, 1, Int::plus)!!
            val paramName = if (count == 1) attrName else "$attrName$count"
            conditions += "{$attrName}=?$paramName"
        }

        if (conditions.isEmpty()) return null

        return TransformData(
            project = project,
            rootType = rootType,
            conditions = conditions,
        )
    }

    private data class TransformData(
        val project: Project,
        val rootType: String,
        val conditions: List<String>,
    )

    companion object {
        fun getInstance(project: Project): PgQTransformationService = project.service()
    }
}
