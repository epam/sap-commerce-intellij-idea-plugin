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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.apache.commons.lang3.Validate

class ImpexHeaderItemTypeParameterNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    public override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        Validate.notNull(parameters)
        Validate.notNull(result)

        val project = parameters.position.project
        val psiElementUnderCaret = if (parameters.position is LeafPsiElement)
            parameters.position.parent
        else parameters.position
        val typeName = findItemTypeName(psiElementUnderCaret) ?: return

        TSCompletionService.getInstance(project)
            .getCompletions(typeName)
            .let { result.addAllElements(it) }
    }

    private fun findItemTypeName(element: PsiElement) = (
        PsiTreeUtil.getParentOfType(element, ImpexParameter::class.java)
            ?: PsiTreeUtil.getParentOfType(element, ImpexFullHeaderParameter::class.java)
                ?.anyHeaderParameterName
        )
        ?.reference
        ?.let { it as PsiPolyVariantReference }
        ?.multiResolve(false)
        ?.firstOrNull()
        ?.let {
            when (it) {
                is AttributeResolveResult -> it.meta.type
                is EnumResolveResult -> it.meta.name
                is ItemResolveResult -> it.meta.name
                is RelationResolveResult -> it.meta.name
                is RelationEndResolveResult -> it.meta.name
                else -> null
            }
        }

    companion object {

        val instance: CompletionProvider<CompletionParameters> = ApplicationManager.getApplication().getService(ImpexHeaderItemTypeParameterNameCompletionProvider::class.java)

    }
}
