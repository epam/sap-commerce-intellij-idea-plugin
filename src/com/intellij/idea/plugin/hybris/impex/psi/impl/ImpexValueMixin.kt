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

package com.intellij.idea.plugin.hybris.impex.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValue
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpExTSDynamicEnumValueReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpExTSStaticEnumValueReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSAttributeReference
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.lang.ASTNode
import com.intellij.psi.*
import com.intellij.sql.indexOf
import com.intellij.util.asSafely
import java.io.Serial

abstract class ImpexValueMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost, ImpexValue {

    override fun isValidHost() = true
    override fun updateText(text: String) = this
    override fun createLiteralTextEscaper() = LiteralTextEscaper.createSimple(this)

    override fun getFieldValue(index: Int): PsiElement? = getFieldValues()
        .getOrNull(index)

    override fun getReference() = references.firstOrNull()

    override fun getReferences(): Array<out PsiReference?> {
        val hasMacroUsage = children
            .filterIsInstance<ImpexValue>()
            .any { it.children.any { subChild -> subChild is ImpexMacroUsageDec } }
        if (hasMacroUsage) return emptyArray()

        val fullHeaderParameter = valueGroup?.fullHeaderParameter
            ?: return emptyArray()

        return fullHeaderParameter
            .anyHeaderParameterName
            .reference
            ?.asSafely<ImpexTSAttributeReference>()
            ?.multiResolve(false)
            ?.firstOrNull()
            ?.asSafely<AttributeResolveResult>()
            ?.meta
            ?.type
            ?.let { TSMetaModelAccess.getInstance(project).findMetaEnumByName(it) }
            ?.let { meta ->
                val index = getEnumCodeParameterIndex(fullHeaderParameter)
                    ?.takeIf { getFieldValues().size > it }
                    ?: return emptyArray()

                if (meta.isDynamic) ImpExTSDynamicEnumValueReference(this, index, meta.name!!)
                else ImpExTSStaticEnumValueReference(this, index, meta.name!!)
            }
            ?.let { arrayOf<PsiReference>(it) }
            ?: emptyArray()
    }

    private fun getEnumCodeParameterIndex(fullHeaderParameter: ImpexFullHeaderParameter) = fullHeaderParameter.parametersList
        .firstOrNull()
        ?.parameterList
        ?.indexOf { parameter ->
            parameter.reference
                ?.asSafely<PsiReferenceBase.Poly<PsiElement>>()
                ?.multiResolve(false)
                ?.firstOrNull()
                ?.asSafely<AttributeResolveResult>()
                ?.meta
                ?.name
                ?.let { it == "code" }
                ?: false
        }

    private fun getFieldValues(): Array<PsiElement> = findChildrenByType(ImpexTypes.FIELD_VALUE, PsiElement::class.java)

    companion object {
        @Serial
        private val serialVersionUID: Long = 8258794639693010240L
    }

}