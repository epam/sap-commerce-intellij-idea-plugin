/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.psi.impl

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.references.*
import com.intellij.idea.plugin.hybris.properties.PropertiesService
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import java.io.Serial

abstract class ImpexAnyHeaderParameterNameMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpexAnyHeaderParameterName {

    private var myReference: PsiReference? = null

    override fun subtreeChanged() {
        putUserData(ImpexTSAttributeReference.CACHE_KEY, null)
        PsiTreeUtil.getParentOfType(this, ImpexFullHeaderParameter::class.java)
            ?.getParametersList()
            ?.flatMap { it.getParameterList() }
            ?.forEach {
                it.putUserData(ImpexFunctionTSItemReference.CACHE_KEY, null)
                it.putUserData(ImpexFunctionTSAttributeReference.CACHE_KEY, null)
            }
    }

    override fun getReference() = getReferences().firstOrNull()

    override fun getReferences(): Array<PsiReference> {
        val leafType = firstChild
            ?.node
            ?.elementType

        when {
            ImpexTypes.MACRO_USAGE == leafType -> return arrayOf(ImpexMacroReference(this))
            ImpexTypes.DOCUMENT_ID == leafType -> return arrayOf(ImpexDocumentIdReference(this))

            //optimisation: don't even try for macro's and documents
            ImpexTypes.HEADER_PARAMETER_NAME != leafType
                && ImpexTypes.FUNCTION != leafType -> return PsiReference.EMPTY_ARRAY

            PsiUtils.shouldCreateNewReference(myReference, text) -> {
                myReference = if (isHeaderAbbreviation()) ImpExHeaderAbbreviationReference(this)
                else ImpexTSAttributeReference(this)
            }
        }

        return myReference
            ?.let { arrayOf(it) }
            ?: emptyArray()
    }

    override fun clone(): Any {
        val result = super.clone() as ImpexAnyHeaderParameterNameMixin
        result.myReference = null
        return result
    }

    private fun isHeaderAbbreviation() = PropertiesService.getInstance(project)
        ?.findAutoCompleteProperties(HybrisConstants.PROPERTY_IMPEX_HEADER_REPLACEMENT)
        ?.asSequence()
        ?.mapNotNull { it.value }
        ?.mapNotNull { abbreviation ->
            abbreviation
                .split("...")
                .takeIf { it.size == 2 }
                ?.map { it.trim() }
        }
        ?.mapNotNull { it.firstOrNull() }
        ?.map { it.replace("\\\\", "\\") }
        ?.any { text.removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED).matches(it.toRegex()) }
        ?: false

    companion object {
        @Serial
        private const val serialVersionUID: Long = -914083395962819287L
    }
}
