/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.psi.PsiErrorElement
import com.intellij.util.ProcessingContext

class FxSRootCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val psiErrorElement = parameters.position.parent as? PsiErrorElement
            ?: return

        val fxsSettings = HybrisProjectSettingsComponent.getInstance(parameters.position.project).state.flexibleSearchSettings
        // FlexibleSearchTokenType.toString()
        when (psiErrorElement.errorDescription.substringBefore(">") + ">") {
            "<statement>" -> result.addAllElements(
                FxSLookupElementFactory.buildKeywords(setOf("SELECT"), fxsSettings)
            )
        }
    }
}