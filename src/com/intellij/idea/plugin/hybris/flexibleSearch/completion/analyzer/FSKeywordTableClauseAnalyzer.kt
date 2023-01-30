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

package com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFile
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchJoinCondition
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.Icon

fun isFile(parameters: CompletionParameters) =
        parameters.position.parent != null && parameters.position.parent.parent != null && parameters.position.parent.parent is FlexibleSearchFile

fun isJoinCondition(parameters: CompletionParameters) =
        parameters.position.parent != null && parameters.position.parent.parent != null && parameters.position.parent.parent is FlexibleSearchJoinCondition

fun isTableNameIdentifier(parameters: CompletionParameters) =
        (parameters.position as LeafPsiElement).elementType == FlexibleSearchTypes.TABLE_NAME_IDENTIFIER

fun isColumnReferenceIdentifier(parameters: CompletionParameters) =
        (parameters.position as LeafPsiElement).elementType == FlexibleSearchTypes.COLUMN_REFERENCE_IDENTIFIER

fun isIdentifier(parameters: CompletionParameters) =
        (parameters.position as LeafPsiElement).elementType == FlexibleSearchTypes.IDENTIFIER


fun addToResult(results: Set<String>, completionResultSet: CompletionResultSet, icon: Icon, bold: Boolean = false) {
    results.forEach { completionResultSet.addElement(LookupElementBuilder.create(it).withCaseSensitivity(false).withBoldness(bold).withIcon(icon)) }
}

fun addSymbolToResult(results: Set<String>, completionResultSet: CompletionResultSet, icon: Icon, bold: Boolean = false) {
    results.forEach { completionResultSet.addElement(LookupElementBuilder.create(it).withPresentableText(it).withCaseSensitivity(false).withBoldness(bold).withIcon(icon)) }
}

fun PsiElement.skipWhitespaceSiblingsBackward() = PsiTreeUtil.skipSiblingsBackward(this, PsiWhiteSpace::class.java)
