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

package com.intellij.idea.plugin.hybris.grid

import com.intellij.database.csv.CsvFormat
import com.intellij.database.csv.CsvRecordFormat
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import java.util.*

class GridXSVFormatService(project: Project) {

    private val developerSettings = DeveloperSettingsComponent.Companion.getInstance(project)
    private val valueSeparator = ";"
    private val quotationPolicy = CsvRecordFormat.QuotationPolicy.NEVER
    private val impExFormats = mutableMapOf<BitSet, CsvFormat>()
    private val fxsFormat by lazy { xsvFlexibleSearchFormat() }

    fun getFormat(language: Language): CsvFormat = when (language) {
        is ImpexLanguage -> getImpExFormat()
        is FlexibleSearchLanguage -> getFlexibleSearchFormat()
        else -> throw IllegalArgumentException("Unsupported language $language")
    }

    private fun getFlexibleSearchFormat() = fxsFormat

    private fun getImpExFormat(): CsvFormat {
        val editModeSettings = developerSettings.state.impexSettings.editMode

        val key = BitSet(2).also {
            it.set(0, editModeSettings.firstRowIsHeader)
            it.set(1, editModeSettings.trimWhitespace)
        }

        return impExFormats.computeIfAbsent(key) {
            xsvImpExFormat(
                firstRowIsHeader = key.get(0),
                trimWhitespace = key.get(1)
            )
        }
    }

    private fun xsvImpExFormat(firstRowIsHeader: Boolean, trimWhitespace: Boolean): CsvFormat {
        val headerFormat = if (firstRowIsHeader) CsvRecordFormat("", "", null, emptyList(), quotationPolicy, valueSeparator, "\n", trimWhitespace)
        else null
        val dataFormat = CsvRecordFormat("", "", null, emptyList(), quotationPolicy, valueSeparator, "\n", trimWhitespace)

        return CsvFormat("ImpEx", dataFormat, headerFormat, "ImpEx", false)
    }

    private fun xsvFlexibleSearchFormat(): CsvFormat {
        val format = CsvRecordFormat("", "", null, emptyList(), quotationPolicy, HybrisConstants.FXS_TABLE_RESULT_SEPARATOR, "\n", true)

        return CsvFormat("FlexibleSearch - Results", format, format, "FlexibleSearch_results", false)
    }
}