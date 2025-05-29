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
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.psi.ImpexDocumentIdUsage
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValue
import com.intellij.idea.plugin.hybris.impex.psi.references.*
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaCollection
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
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
            ?: collectTSReferences(fullHeaderParameter)
            ?: emptyArray()
    }

    private fun collectTSReferences(fullHeaderParameter: ImpexFullHeaderParameter): Array<PsiReference>? {
        val resolveResult = fullHeaderParameter
            .anyHeaderParameterName
            .reference
            ?.asSafely<ImpexTSAttributeReference>()
            ?.multiResolve(false)
            ?.firstOrNull()
            ?: return null

        val attributeType = when (resolveResult) {
            is AttributeResolveResult -> resolveResult.meta.type
            is RelationEndResolveResult -> resolveResult.meta.type
            else -> return null
        } ?: return null

        val metaModelAccess = TSMetaModelAccess.getInstance(project)
        return metaModelAccess.findMetaClassifierByName(attributeType)
            ?.let {
                when (it) {
                    is TSGlobalMetaEnum -> collectTSReferencesForMetaEnum(fullHeaderParameter, it, attributeType)
                    is TSGlobalMetaItem -> collectTSReferencesForMetaItem(fullHeaderParameter, attributeType)
                    is TSGlobalMetaCollection -> collectTSReferencesForMetaCollection(fullHeaderParameter, it)
                    else -> null
                }
            }
            ?.toTypedArray()
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

    private fun collectTSReferencesForMetaItem(fullHeaderParameter: ImpexFullHeaderParameter, attributeType: String): List<PsiReference>? {
        if (HybrisConstants.TS_COMPOSED_TYPE == attributeType) {
            val valueElement = getValueElement(fullHeaderParameter) ?: return null

            listOf(ImpExValueTSClassifierReference(this, valueElement))
        }

        return fullHeaderParameter
            .parametersList
            .firstOrNull()
            ?.parameterList
            ?.mapIndexedNotNull { index, parameter ->
                parameter.reference.asSafely<ImpexFunctionTSAttributeReference>()
                    ?.multiResolve(false)
                    ?.firstOrNull()
                    ?.let { resolveResult ->
                        when (resolveResult) {
                            is AttributeResolveResult -> resolveResult.meta.type
                            is RelationEndResolveResult -> resolveResult.meta.type
                            else -> null
                        }
                    }
                    ?.takeIf { HybrisConstants.TS_COMPOSED_TYPE == it }
                    ?.let { ImpExValueTSClassifierReference(this, index) }
            }
            ?.take(getFieldValues().size)
    }

    private fun collectTSReferencesForMetaEnum(
        fullHeaderParameter: ImpexFullHeaderParameter,
        attributeMeta: TSGlobalMetaEnum,
        attributeType: String
    ): List<PsiReference>? = fullHeaderParameter
        .parametersList
        .firstOrNull()
        ?.parameterList
        ?.mapIndexedNotNull { index, parameter ->
            parameter.reference.asSafely<ImpexFunctionTSAttributeReference>()
                ?.multiResolve(false)
                ?.firstOrNull()
                ?.asSafely<AttributeResolveResult>()
                ?.meta
                ?.let {
                    when {
                        it.name == HybrisConstants.ATTRIBUTE_CODE -> if (attributeMeta.isDynamic) ImpExTSDynamicEnumValueReference(this, index, attributeType)
                        else ImpExTSStaticEnumValueReference(this, index, attributeType)

                        it.type == HybrisConstants.TS_COMPOSED_TYPE -> ImpExValueTSClassifierReference(this, index)
                        else -> null
                    }
                }
        }
        ?.take(getFieldValues().size)

    private fun collectTSReferencesForMetaCollection(
        fullHeaderParameter: ImpexFullHeaderParameter,
        attributeMeta: TSGlobalMetaCollection
    ): List<ImpExValueTSClassifierReference> {
        // TODO: support every other type
        if (HybrisConstants.TS_COMPOSED_TYPE != attributeMeta.elementType) return emptyList()

        val collectionDelimiter = fullHeaderParameter.modifiersList
            .flatMap { it.attributeList }
            .find { it.anyAttributeName.text == AttributeModifier.COLLECTION_DELIMITER.modifierName }
            ?.anyAttributeValue
            ?.text
            ?.trim()
            ?: ","

        return buildList {
            var previousStart = 0

            text.split(collectionDelimiter).forEachIndexed { index, part ->
                val partTrimmed = part.trim()
                val trimStart = part.trimStart()
                val startWhitespaces = part.length - trimStart.length

                val start = startWhitespaces + previousStart
                val end = start + partTrimmed.length

                previousStart += part.length + collectionDelimiter.length

                add(TextRange.create(start, end))
            }
        }
            .map {
                ImpExValueTSClassifierReference(
                    this, null, it,
                    Key.create("HYBRIS_TS_CACHED_REFERENCE_$textRange")
                )
            }
    }

    private fun getValueElement(
        fullHeaderParameter: ImpexFullHeaderParameter,
        attributeName: String = HybrisConstants.ATTRIBUTE_CODE
    ) = fullHeaderParameter.parametersList
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