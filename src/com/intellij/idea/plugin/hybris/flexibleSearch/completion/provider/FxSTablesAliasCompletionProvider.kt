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

package com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableAliasName
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.util.ProcessingContext

class FxSTablesAliasCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val fxsSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(parameters.position.project).state.flexibleSearchSettings

        if (!fxsSettings.completion.suggestTableAliasNames) return

        (parameters.position.parent as? FlexibleSearchTableAliasName)
            ?.table
            ?.tableName
            ?.let { tableName ->
                val suggestions = setOf(
                    regexNoUpper.replace(tableName, ""),
                    regexNoUpperAndDigits.replace(tableName, ""),
                    regexNoUpper.replace(tableName, "").replace(regexDigitsToUnderscore, "_"),
                )
                    .map { it.lowercase() }
                    .takeIf { it.isNotEmpty() }
                // if nothing match - fallback to table name
                    ?: setOf(tableName.lowercase())

                result.addAllElements(FxSLookupElementFactory.buildTableAliases(suggestions))
            }
            ?: return
    }

    companion object {
        private val regexDigitsToUnderscore = Regex("[0-9]")
        private val regexNoUpper = Regex("[a-z]")
        private val regexNoUpperAndDigits = Regex("[a-z0-9]")
    }
}