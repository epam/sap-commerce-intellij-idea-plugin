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
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.*
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.checker.FSFromClauseKeywordsAnalyzer
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.checker.FSSelectClauseKeywordsAnalyzer
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.checker.FSWhereClauseKeywordsAnalyzer
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchSelectList
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

class FxsKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        processingContext: ProcessingContext,
        completionResultSet: CompletionResultSet
    ) {
        if ((parameters.originalPosition == null && !isTableNameIdentifier(parameters) && !isColumnReferenceIdentifier(parameters)) || isFile(parameters)) {
            addToResult(hashSetOf("SELECT", "FROM", "WHERE"), completionResultSet, AllIcons.Nodes.Static, true)
        }
        if ((isColumnReferenceIdentifier(parameters) && parameters.position.skipWhitespaceSiblingsBackward() != null && parameters.position.skipWhitespaceSiblingsBackward()!!.text != "}") ||
            (isColumnReferenceIdentifier(parameters) && PsiTreeUtil.getParentOfType(parameters.position, FlexibleSearchSelectList::class.java) != null)) {
            FxsColumnReferenceCompletionProvider.instance.addCompletionVariants(parameters, ProcessingContext(), completionResultSet)

        }
        if (isFile(parameters)) {
            addToResult(hashSetOf("SELECT", "FROM", "WHERE"), completionResultSet, AllIcons.Nodes.Static, true)
        }

        FSSelectClauseKeywordsAnalyzer.analyzeCompletions(parameters, completionResultSet)
        FSWhereClauseKeywordsAnalyzer.analyzeCompletions(parameters, completionResultSet)
        FSFromClauseKeywordsAnalyzer.analyzeCompletions(parameters, completionResultSet)
    }

    companion object {
        val instance: CompletionProvider<CompletionParameters> =
            ApplicationManager.getApplication().getService(FxsKeywordCompletionProvider::class.java)
    }
}