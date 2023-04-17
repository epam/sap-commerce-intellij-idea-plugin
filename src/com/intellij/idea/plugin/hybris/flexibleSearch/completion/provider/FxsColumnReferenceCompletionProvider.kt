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
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.isColumnReferenceIdentifier
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTreeUtil.findSiblingBackward
import com.intellij.util.ProcessingContext

class FxsColumnReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
    companion object {
        val instance: CompletionProvider<CompletionParameters> =
            ApplicationManager.getApplication().getService(FxsColumnReferenceCompletionProvider::class.java)
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val psiElementUnderCaret = parameters.originalPosition ?: parameters.position

        if (!isColumnReferenceIdentifier(parameters)) return

        val tableNameId = findSiblingBackward(parameters.position, FlexibleSearchTypes.TABLE_NAME_IDENTIFIER, null)

        if (tableNameId != null) {
            addCompletionsWithTableName(psiElementUnderCaret, tableNameId, project, result)
        } else {
            addCompletionsWithoutTableName(psiElementUnderCaret, result, project)
        }
    }

    private fun addCompletionsWithoutTableName(
        psiElementUnderCaret: PsiElement,
        result: CompletionResultSet,
        project: Project
    ) {
        val querySpecification = PsiTreeUtil.getParentOfType(psiElementUnderCaret, FlexibleSearchQuerySpecification::class.java)

        val tableRefs = PsiTreeUtil.findChildrenOfType(querySpecification, FlexibleSearchTableReference::class.java)
        val correlationNames =
            if (tableRefs.isNotEmpty()) PsiTreeUtil.findChildrenOfType(tableRefs.first(), FlexibleSearchCorrelationName::class.java)
            else listOf()

        if (correlationNames.isNotEmpty()) {
            correlationNames
                .map { it.text }
                .map {
                    LookupElementBuilder.create(it)
                        .withPresentableText(it)
                        .withIcon(AllIcons.Nodes.Parameter)
                }
                .forEach { result.addElement(it) }

        } else {
            val tableName = PsiTreeUtil.findChildOfType(querySpecification, FlexibleSearchTableName::class.java) ?: return
            fillDomAttributesCompletions(project, tableName.text, result)
        }
    }

    private fun addCompletionsWithTableName(
        psiElementUnderCaret: PsiElement,
        tableNameId: PsiElement,
        project: Project,
        result: CompletionResultSet
    ) {
        val resultCaseInsensitive = result.caseInsensitive()
        val querySpecification = PsiTreeUtil.getTopmostParentOfType(psiElementUnderCaret, FlexibleSearchQuerySpecification::class.java)

        PsiTreeUtil.findChildrenOfType(querySpecification, FlexibleSearchTableReference::class.java)
            .mapNotNull { PsiTreeUtil.findChildOfType(it, FlexibleSearchTableName::class.java) }
            .firstOrNull {
                val element = PsiTreeUtil.getNextSiblingOfType(it, FlexibleSearchCorrelationName::class.java)
                element != null && element.text == tableNameId.text
            }
            ?.let { fillDomAttributesCompletions(project, it.text, resultCaseInsensitive) }
    }

    private fun fillDomAttributesCompletions(
        project: Project,
        itemTypeCode: String,
        resultSet: CompletionResultSet
    ) {
        val currentPrefix = resultSet.prefixMatcher.prefix
        val delimiters = arrayOf('.', ':')
        val emptyPrefixResultSet = resultSet.withPrefixMatcher(currentPrefix.substringAfter(delimiters))

        emptyPrefixResultSet.addAllElements(TSCompletionService.getInstance(project).getCompletions(itemTypeCode))
    }

    private fun String.substringAfter(delimiters: Array<Char>, missingDelimiterValue: String = this): String {
        val result = delimiters
            .filter { delimiter -> indexOf(delimiter) != -1 }
            .map { delimiter -> substring(indexOf(delimiter) + 1, length) }
        return if (result.isEmpty()) missingDelimiterValue else result.first()
    }


}