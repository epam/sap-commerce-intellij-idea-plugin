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

package sap.commerce.toolset.impex.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.typeSystem.codeInsight.lookup.TSLookupElementFactory

class ImpExMacrosConfigCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val psiElementUnderCaret = parameters.position
        val project = psiElementUnderCaret.project
        val prevLeaf = PsiTreeUtil.prevLeaf(psiElementUnderCaret)
        val propertyService = PropertyService.getInstance(project)

        if (prevLeaf != null && prevLeaf.text.contains(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
            val position = parameters.position
            val query = getQuery(position)
            propertyService.findAutoCompleteProperties(query)
                .mapNotNull { it.key }
                .map { TSLookupElementFactory.buildCustomProperty(it) }
                .forEach { result.addElement(it) }
        }

        if (psiElementUnderCaret.text.contains(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
            val position = parameters.position
            val prefix = getPrefix(position)
            val query = position.text
                .substring(prefix.length)
                .replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")
            propertyService.findAutoCompleteProperties(query)
                .mapNotNull { it.key }
                .map { it }
                .map { TSLookupElementFactory.buildCustomProperty(it) }
                .forEach { result.addElement(it) }
        }
    }

    private fun getQuery(position: PsiElement) = position.text.replace("-", "")
        .replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")

    private fun getPrefix(position: PsiElement): String {
        val text = position.text

        val index = text.indexOf(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX)
        return text.substring(0, index + ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length)
    }

}