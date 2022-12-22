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

package com.intellij.idea.plugin.hybris.impex.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.utils.ProjectPropertiesUtils
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext


/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class ImpexMacrosConfigCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val psiElementUnderCaret = parameters.position
        val prevLeaf = PsiTreeUtil.prevLeaf(psiElementUnderCaret)
        if (prevLeaf != null && prevLeaf.text.contains(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
            val position = parameters.position
            val query = getQuery(position)
            val module = ModuleUtil.findModuleForPsiElement(position)
            ProjectPropertiesUtils.findAutoCompleteProperties(module!!, query).forEach {
                result.addElement(LookupElementBuilder.create("${it.key}").withIcon(AllIcons.Nodes.Property))
            }
        }

        if (psiElementUnderCaret.text.contains(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
            val position = parameters.position
            val prefix = getPrefix(position)
            val query = position.text.substring(prefix.length).replace("IntellijIdeaRulezzz", "")
            val module = ModuleUtil.findModuleForPsiElement(position)
            ProjectPropertiesUtils.findAutoCompleteProperties(module!!, query).forEach {
                result.addElement(LookupElementBuilder.create(prefix+"${it.key}").withIcon(AllIcons.Nodes.Property))
            }
        }
    }

    private fun getQuery(position: PsiElement) = position.text.replace("-", "")
        .replace("IntellijIdeaRulezzz", "")

    private fun getPrefix(position: PsiElement): String {
        val text = position.text

        val index = text.indexOf(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)
        return text.substring(0, index + HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length)
    }
}