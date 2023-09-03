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
import com.intellij.idea.plugin.hybris.impex.codeInsight.lookup.ImpExLookupElementFactory
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexMacroReference
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

class ImpexMacrosCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val originalFile = parameters.originalFile

        PsiTreeUtil.findChildrenOfType(originalFile, ImpexMacroDeclaration::class.java)
            .map { it.firstChild }
            .map { ImpexMacroReference.escapeName(it.text) }
            .map { ImpExLookupElementFactory.buildMacro(it) }
            .let { result.addAllElements(it) }
    }

    companion object {
        val instance: CompletionProvider<CompletionParameters> =
            ApplicationManager.getApplication().getService(ImpexMacrosCompletionProvider::class.java)
    }
}