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

package sap.commerce.toolset.grid

import com.intellij.database.csv.CsvFormat
import com.intellij.database.csv.CsvRecordFormat
import com.intellij.lang.Language
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.flexibleSearch.FlexibleSearchLanguage
import sap.commerce.toolset.polyglotQuery.PolyglotQueryLanguage
import java.util.*

class GridXSVFormatService(private val project: Project) : Disposable {

    private val quotationPolicy = CsvRecordFormat.QuotationPolicy.NEVER
    private val impExFormats = mutableMapOf<BitSet, CsvFormat>()
    private val fxsFormat by lazy { xsvFlexibleSearchFormat() }

    override fun dispose() = impExFormats.clear()

    fun getFormat(language: Language): CsvFormat = when (language) {
        is FlexibleSearchLanguage -> getFlexibleSearchFormat()
        is PolyglotQueryLanguage -> getFlexibleSearchFormat()
        else -> throw IllegalArgumentException("Unsupported language $language")
    }

    private fun getFlexibleSearchFormat() = fxsFormat

    private fun xsvFlexibleSearchFormat(): CsvFormat {
        val format = CsvRecordFormat("", "", null, emptyList(), quotationPolicy, HybrisConstants.FXS_TABLE_RESULT_SEPARATOR, "\n", true)

        return CsvFormat("FlexibleSearch - Results", format, format, "FlexibleSearch_results", false)
    }

    companion object {
        fun getInstance(project: Project): GridXSVFormatService = project.service()
    }
}