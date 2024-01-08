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
import com.intellij.idea.plugin.hybris.flexibleSearch.FxSUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchColumnRefExpression
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchResultColumns
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

class FxSHybrisColumnCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val fxsSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(parameters.position.project).state.flexibleSearchSettings
        val addComma = FxSUtils.shouldAddCommaAfterExpression(parameters.position, fxsSettings)

        val parent = parameters.position.parentOfType<FlexibleSearchColumnRefExpression>()
        if (parent == null || parent.selectedTableName == null) {
            result.addElement(FxSLookupElementFactory.buildYColumn(addComma))
        }

        PsiTreeUtil.getParentOfType(parameters.position, FlexibleSearchResultColumns::class.java)
            ?.let {
                result.addElement(FxSLookupElementFactory.buildYColumnAll(addComma))
            }
    }
}