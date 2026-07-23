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

package sap.commerce.toolset.flexibleSearch.transform

import com.intellij.lang.Language
import sap.commerce.toolset.flexibleSearch.FlexibleSearchConstants
import sap.commerce.toolset.flexibleSearch.FlexibleSearchLanguage
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.flexibleSearch.transform.context.FxSTransformationRequest
import sap.commerce.toolset.flexibleSearch.transform.context.FxSTransformationResult
import sap.commerce.toolset.flexibleSearch.transform.impex.ImpExHeaderBuilder
import sap.commerce.toolset.flexibleSearch.transform.impex.ImpExTransformationService
import sap.commerce.toolset.transform.Transformer

class FxSImpExTransformer : Transformer<FlexibleSearchPsiFile, FxSTransformationResult> {

    override val id: String
        get() = "fxs-to-impex"
    override val name: String
        get() = "ImpEx"
    override val description: String
        get() = "Converts FlexibleSearch query results to ImpEx format, resolving FK natural keys, enum codes, and localized attributes via the SAP Commerce type system"
    override val language: Language
        get() = FlexibleSearchLanguage

    override fun isApplicable(language: Language) = language is FlexibleSearchLanguage

    override fun transform(psiFile: FlexibleSearchPsiFile, onComplete: (FxSTransformationResult) -> Unit) {
        val context = psiFile.context()

        ImpExTransformationService.getInstance(context.project).transform(context) { impexContent ->
            onComplete(
                FxSTransformationResult(
                    transformerName = name,
                    content = impexContent,
                    exportType = context.typeName,
                    exportRows = context.rows,
                )
            )
        }
    }

    override suspend fun transform(psiFile: FlexibleSearchPsiFile): FxSTransformationResult {
        val context = psiFile.context()
        val impexContent = ImpExTransformationService.getInstance(context.project).transform(context)

        return FxSTransformationResult(
            transformerName = name,
            content = impexContent,
            exportType = context.typeName,
            exportRows = context.rows,
        )
    }

    private fun FlexibleSearchPsiFile.context(): FxSTransformationRequest {
        val project = this.project
        val includeTypeSystemUnique = getUserData(FlexibleSearchConstants.Transform.INCLUDE_TYPE_SYSTEM_UNIQUE) ?: false
        val includeData = getUserData(FlexibleSearchConstants.Transform.INCLUDE_DATA) ?: false
        val connection = getUserData(FlexibleSearchExecConstants.Transform.CONNECTION)
        val execSettings = getUserData(FlexibleSearchExecConstants.Transform.EXEC_SETTINGS)
            ?: FlexibleSearchExecContext.defaultSettings(connection)
        val execResult = getUserData(FlexibleSearchExecConstants.Transform.EXEC_RESULTS)

        val headers = execResult?.headers ?: emptyList()
        val rows = execResult?.rows ?: emptyList()
        val baseQueryInfo = FxSQueryAnalyzer.analyze(this, headers)

        val queryInfo = if (includeTypeSystemUnique) {
            val tsUniqueAttrs = ImpExHeaderBuilder.typeSystemUniqueAttributeNames(baseQueryInfo.primaryType, project)
            baseQueryInfo.copy(uniqueAttributeNames = baseQueryInfo.uniqueAttributeNames + tsUniqueAttrs)
        } else {
            baseQueryInfo
        }

        val params = ImpExHeaderBuilder.buildParams(queryInfo, project)
        val joinUniqueParams = ImpExHeaderBuilder.buildJoinUniqueParams(queryInfo, project)
        val exportRows = if (includeData) rows else emptyList()

        return FxSTransformationRequest(
            project = project,
            queryInfo = queryInfo,
            params = params,
            joinUniqueParams = joinUniqueParams,
            rows = exportRows,
            connection = connection,
            execSettings = execSettings,
        )
    }
}