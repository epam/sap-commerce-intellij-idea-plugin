/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.impex.psi.impl

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.impex.psi.ImpExAnyHeaderParameterName
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.psi.references.*
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.psi.shouldCreateNewReference
import java.io.Serial

abstract class ImpExAnyHeaderParameterNameMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpExAnyHeaderParameterName {

    private var myReference: PsiReference? = null

    override fun subtreeChanged() {
        removeUserData(ImpExTSAttributeReference.CACHE_KEY)
        PsiTreeUtil.getParentOfType(this, ImpExFullHeaderParameter::class.java)
            ?.getParametersList()
            ?.flatMap { it.getParameterList() }
            ?.forEach {
                it.removeUserData(ImpExFunctionTSItemReference.CACHE_KEY)
                it.removeUserData(ImpExFunctionTSAttributeReference.CACHE_KEY)
            }
    }

    override fun getReference() = getReferences().firstOrNull()

    override fun getReferences(): Array<PsiReference> {
        val leafType = firstChild
            ?.node
            ?.elementType

        when {
            ImpExTypes.MACRO_USAGE == leafType -> return arrayOf(ImpExMacroReference(this))

            //optimisation: don't even try for macro's and documents
            ImpExTypes.HEADER_PARAMETER_NAME != leafType
                && ImpExTypes.FUNCTION != leafType -> return PsiReference.EMPTY_ARRAY

            shouldCreateNewReference(myReference, text) -> {
                myReference = if (isHeaderAbbreviation()) ImpExHeaderAbbreviationReference(this)
                else ImpExTSAttributeReference(this)
            }
        }

        return myReference
            ?.let { arrayOf(it) }
            ?: emptyArray()
    }

    override fun clone(): Any {
        val result = super.clone() as ImpExAnyHeaderParameterNameMixin
        result.myReference = null
        return result
    }

    private fun isHeaderAbbreviation() = PropertyService.getInstance(project)
        .findAutoCompleteProperties(HybrisConstants.PROPERTY_IMPEX_HEADER_REPLACEMENT)
        .asSequence()
        .mapNotNull { it.value }
        .mapNotNull { abbreviation ->
            abbreviation
                .split("...")
                .takeIf { it.size == 2 }
                ?.map { it.trim() }
        }
        .mapNotNull { it.firstOrNull() }
        .map { it.replace("\\\\", "\\") }
        .any { text.removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED).matches(it.toRegex()) }

    companion object {
        @Serial
        private val serialVersionUID: Long = -914083395962819287L
    }
}
