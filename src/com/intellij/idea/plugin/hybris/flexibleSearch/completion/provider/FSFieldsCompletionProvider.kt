/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.isColumnReferenceIdentifier
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaRelation
import com.intellij.javaee.JavaeeIcons.PARAMETER_ICON
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTreeUtil.findSiblingBackward
import com.intellij.util.ProcessingContext
import java.util.*
import java.util.stream.Stream

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class FSFieldsCompletionProvider : CompletionProvider<CompletionParameters>() {
    companion object {
        val instance: FSFieldsCompletionProvider = FSFieldsCompletionProvider()
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = getProject(parameters) ?: return
        val psiElementUnderCaret = parameters.originalPosition ?: parameters.position

        if (isColumnReferenceIdentifier(parameters)) {
            val tableNameId = findSiblingBackward(parameters.position, FlexibleSearchTypes.TABLE_NAME_IDENTIFIER, null)

            if (tableNameId == null) {
                val querySpecification = PsiTreeUtil.getParentOfType(psiElementUnderCaret, FlexibleSearchQuerySpecification::class.java)

                val tableRefs = PsiTreeUtil.findChildrenOfType(querySpecification, FlexibleSearchTableReference::class.java)
                val correlationNames = if (tableRefs.isNotEmpty()) PsiTreeUtil.findChildrenOfType(tableRefs.first(), FlexibleSearchCorrelationName::class.java) else listOf()

                if (correlationNames.isNotEmpty()) {
                    correlationNames.map { it.text }
                            .map { LookupElementBuilder.create(it).withPresentableText(it).withIcon(PARAMETER_ICON) }
                            .forEach { result.addElement(it) }

                } else {
                    val tableName = PsiTreeUtil.findChildOfType(querySpecification, FlexibleSearchTableName::class.java)
                    if (tableName != null) {
                        fillDomAttributesCompletions(project, tableName.text, result)
                    }
                }
            } else {
                val querySpecification = PsiTreeUtil.getTopmostParentOfType(psiElementUnderCaret, FlexibleSearchQuerySpecification::class.java)
                val tableNames = PsiTreeUtil.findChildrenOfType(querySpecification, FlexibleSearchTableReference::class.java)

                if (tableNames.isNotEmpty()) {
                    val foundTableName = tableNames
                            .map { PsiTreeUtil.findChildOfType(it, FlexibleSearchTableName::class.java) }
                            .filter {
                                if (it != null) {
                                    val element = PsiTreeUtil.getNextSiblingOfType(it, FlexibleSearchCorrelationName::class.java)
                                    element != null && element.text == tableNameId.text
                                } else {
                                    false
                                }
                            }
                    if (foundTableName.isNotEmpty()) {
                        fillDomAttributesCompletions(project, foundTableName.first()!!.text, result)
                    }
                }
            }
        }
    }

    private fun getProject(parameters: CompletionParameters): Project? = parameters.editor.project

    private fun fillDomAttributesCompletions(
            project: Project,
            itemTypeCode: String,
            resultSet: CompletionResultSet
    ) {
        val metaItem = Optional.ofNullable(TSMetaModelService.getInstance(project).findMetaItemByName(itemTypeCode))

        val currentPrefix = resultSet.prefixMatcher.prefix
        val delimiters = arrayOf('.', ':')
        val emptyPrefixResultSet = resultSet.withPrefixMatcher(currentPrefix.substringAfter(delimiters))
        metaItem
                .map { meta -> meta.getAttributes(true).stream() }
                .orElse(Stream.empty())
                .map<LookupElementBuilder> { prop ->
                    val name = prop.name ?: return@map null

                    val builder = LookupElementBuilder
                            .create(name)
                            .withIcon(HybrisIcons.TYPE_SYSTEM)
                            .withStrikeoutness(prop.isDeprecated)
                    val typeText = getTypePresentableText(prop.type)
                    return@map if (StringUtil.isEmpty(typeText)) builder else builder.withTypeText(typeText, true)
                }
                .filter { Objects.nonNull(it) }
                .forEach { emptyPrefixResultSet.addElement(it) }
        metaItem
                .map { meta -> meta.getReferenceEndsStream(true) }
                .orElse(Stream.empty<TSMetaRelation.ReferenceEnd>())
                .map { ref ->
                    LookupElementBuilder
                            .create(ref.role)
                            .withTypeText(ref.typeName)
                            .withIcon(HybrisIcons.TYPE_SYSTEM)
                }
                .forEach { emptyPrefixResultSet.addElement(it) }
    }

    private fun getTypePresentableText(type: String?): String {
        if (type == null) {
            return ""
        }
        val index = type.lastIndexOf('.')
        return if (index >= 0) type.substring(index + 1) else type
    }


    private fun String.substringAfter(delimiters: Array<Char>, missingDelimiterValue: String = this): String {
        val result = delimiters
                .filter { delimiter -> indexOf(delimiter) != -1 }
                .map { delimiter -> substring(indexOf(delimiter) + 1, length) }
        return if (result.isEmpty()) missingDelimiterValue else result.first()
    }


}