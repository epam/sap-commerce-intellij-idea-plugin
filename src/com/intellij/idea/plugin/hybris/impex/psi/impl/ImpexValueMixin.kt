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
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexDocumentIdUsage
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValue
import com.intellij.idea.plugin.hybris.impex.psi.references.*
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.*
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.sql.indexOf
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import java.io.Serial

abstract class ImpexValueMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost, ImpexValue {

    override fun isValidHost() = true
    override fun updateText(text: String) = this
    override fun createLiteralTextEscaper() = LiteralTextEscaper.createSimple(this)

    override fun getFieldValue(index: Int): PsiElement? = getFieldValues()
        .getOrNull(index)

    override fun getReference() = references.firstOrNull()

    override fun getReferences(): Array<PsiReference> = CachedValuesManager.getManager(project).getCachedValue(this) {
        CachedValueProvider.Result.create(
            calculateReferences(),
            project.service<TSModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT
        )
    }

    private fun calculateReferences(): Array<PsiReference> {
        val fullHeaderParameter = valueGroup?.fullHeaderParameter
            ?: return emptyArray()

        return calculateDocIdReference(fullHeaderParameter)
            ?.let { arrayOf(it) }
            ?: fullHeaderParameter
                .anyHeaderParameterName
                .reference
                ?.asSafely<ImpexTSAttributeReference>()
                ?.multiResolve(false)
                ?.firstOrNull()
                ?.asSafely<AttributeResolveResult>()
                ?.meta
                ?.type
                ?.let { TSMetaModelAccess.getInstance(project).findMetaClassifierByName(it) }
                ?.let { meta ->
                    meta.name?.let { calculateTSReference(fullHeaderParameter, meta, it) }
                }
                ?.let { arrayOf(it) }
            ?: emptyArray()
    }

    private fun calculateDocIdReference(fullHeaderParameter: ImpexFullHeaderParameter): PsiReferenceBase.Poly<PsiElement>? = fullHeaderParameter
        .parametersList
        .firstOrNull()
        ?.parameterList
        ?.takeIf { it.size == 1 }
        ?.firstOrNull()
        ?.childrenOfType<ImpexDocumentIdUsage>()
        ?.firstOrNull()
        ?.let { ImpExDocumentIdUsageReference(this) }

    private fun calculateTSReference(
        fullHeaderParameter: ImpexFullHeaderParameter,
        meta: TSGlobalMetaClassifier<out DomElement>,
        name: String
    ): PsiReferenceBase.Poly<PsiElement>? = when {
        meta is TSGlobalMetaEnum -> {
            val index = getParameterIndex(fullHeaderParameter) ?: return null

            if (meta.isDynamic) ImpExTSDynamicEnumValueReference(this, index, name)
            else ImpExTSStaticEnumValueReference(this, index, name)
        }

        meta is TSGlobalMetaItem && HybrisConstants.TS_COMPOSED_TYPE == name -> {
            val index = getParameterIndex(fullHeaderParameter) ?: return null

            ImpExTSComposedTypeValueReference(this, index, name)
        }

        else -> null
    }

    private fun getParameterIndex(fullHeaderParameter: ImpexFullHeaderParameter, attributeName: String = "code") = fullHeaderParameter.parametersList
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
                ?.let { it == attributeName }
                ?: false
        }
        ?.takeIf { it >= 0 && getFieldValues().size > it }

    private fun getFieldValues(): Array<PsiElement> = findChildrenByType(ImpexTypes.FIELD_VALUE, PsiElement::class.java)

    companion object {
        @Serial
        private val serialVersionUID: Long = 8258794639693010240L
    }

}