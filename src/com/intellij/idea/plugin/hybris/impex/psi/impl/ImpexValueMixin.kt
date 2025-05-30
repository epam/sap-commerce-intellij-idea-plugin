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
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.Cardinality
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiReference
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import java.io.Serial

abstract class ImpexValueMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost, ImpexValue {

    override fun isValidHost() = true
    override fun updateText(text: String) = this
    override fun createLiteralTextEscaper() = LiteralTextEscaper.createSimple(this)

    override fun getFieldValue(index: Int): PsiElement? = getFieldValues()
        .getOrNull(index)

    override fun getReferences(): Array<PsiReference> = CachedValuesManager.getManager(project).getCachedValue(this) {
        CachedValueProvider.Result.create(
            collectReferences(),
            project.service<TSModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT
        )
    }

    private fun collectReferences(): Array<PsiReference> {
        val fullHeaderParameter = valueGroup?.fullHeaderParameter
            ?: return emptyArray()

        val meta: TSMetaClassifier<out DomElement> = getAttribute(fullHeaderParameter)
            .let {
                when (it) {
                    is AttributeResolveResult -> it.meta
                    is RelationEndResolveResult -> it.meta
                    else -> null
                }
            }
            ?: return emptyArray()

        val attributeType = when (meta) {
            is TSGlobalMetaItem.TSGlobalMetaItemAttribute -> meta.type
            is TSMetaRelation.TSMetaRelationElement -> meta.type
            else -> null
        } ?: return emptyArray()

        val metaModelAccess = TSMetaModelAccess.getInstance(project)
        return collectDocIdValuesReferences(fullHeaderParameter, meta, attributeType, metaModelAccess)
            ?: collectTSReferences(fullHeaderParameter, attributeType, metaModelAccess)
            ?: emptyArray()
    }

    private fun collectDocIdValuesReferences(
        fullHeaderParameter: ImpexFullHeaderParameter,
        meta: TSMetaClassifier<out DomElement>,
        attributeType: String,
        metaModelAccess: TSMetaModelAccess,
    ): Array<PsiReference>? {
        val cardinality = when (meta) {
            is TSMetaRelation.TSMetaRelationElement -> meta.cardinality
            is TSGlobalMetaItem.TSGlobalMetaItemAttribute -> when (metaModelAccess.findMetaClassifierByName(attributeType)) {
                is TSGlobalMetaCollection -> Cardinality.MANY
                else -> null
            }

            else -> Cardinality.ONE
        }

        // ensure that column has single parameter as a &DocumentID
        fullHeaderParameter
            .parametersList
            .firstOrNull()
            ?.parameterList
            ?.takeIf { it.size == 1 }
            ?.firstOrNull()
            ?.childrenOfType<ImpexDocumentIdUsage>()
            ?.firstOrNull()
            ?: return null

        if (cardinality == Cardinality.ONE) {
            return TextRange(0, textLength)
                .takeUnless { it.isMacro(text) }
                ?.let { ImpExDocumentIdUsageReference(this, it) }
                ?.let { arrayOf(it) }
        }

        /**
         * INSERT_UPDATE ListAddToCartAction; uid[unique = true]  ; &actionRef
         *                                  ; ListAddToCartAction ; ListAddToCartAction
         *                                  ; Action_2            ; Action_2
         *                                  ; Action_3            ; Action_3
         *
         * INSERT_UPDATE SearchResultsGridComponent; actions(&actionRef)[collection-delimiter = |]
         *                                         ; Action_2 | Action_3 | ListAddToCartAction | Wrong
         *
         * Injection -> SearchResultsGridComponent : actions
         */

        return collectRanges(fullHeaderParameter, AttributeModifier.COLLECTION_DELIMITER, ",")
            .filterNot { it.isMacro(text) }
            .map { ImpExDocumentIdUsageReference(this, it) }
            .toTypedArray()
    }

    private fun collectTSReferences(
        fullHeaderParameter: ImpexFullHeaderParameter,
        attributeType: String,
        metaModelAccess: TSMetaModelAccess
    ) = metaModelAccess.findMetaClassifierByName(attributeType)
        ?.let {
            when (it) {
                is TSGlobalMetaEnum -> collectTSReferencesForMetaEnum(fullHeaderParameter, it, attributeType)
                is TSGlobalMetaItem -> collectTSReferencesForMetaItem(fullHeaderParameter, attributeType)
                is TSGlobalMetaCollection -> collectTSReferencesForMetaCollection(fullHeaderParameter, it, metaModelAccess)
                else -> null
            }
                ?.toTypedArray()
        }

    private fun collectTSReferencesForMetaItem(fullHeaderParameter: ImpexFullHeaderParameter, attributeType: String): List<PsiReference>? {
        val parameters = fullHeaderParameter
            .parametersList
            .firstOrNull()
            ?.parameterList
            ?: return null

        if (HybrisConstants.TS_COMPOSED_TYPE == attributeType) {
            /**
             * UPDATE BundleTemplateStatus[batchmode = true]; itemtype(code)[unique = true]
             *                                              ; Address
             *
             * To be injected into -> Address
             */
            return parameters
                .takeIf { it.size == 1 }
                ?.firstOrNull()
                ?.takeIf { HybrisConstants.ATTRIBUTE_CODE == it.text }
                ?.let { ImpExValueTSClassifierReference(this, TextRange.create(0, textLength)) }
                ?.let { listOf(it) }
        }

        /**
         * INSERT AttributeConstraint; descriptor(enclosingType(code), qualifier)
         *                           ; ConsumedDestination:url
         *
         * To be injected into -> ConsumedDestination
         */

        val ranges = collectRanges(fullHeaderParameter, AttributeModifier.PATH_DELIMITER, ":")
        return parameters
            .mapIndexedNotNull { index, parameter ->
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
                    ?.let { ranges.getOrNull(index) }
                    ?.let { ImpExValueTSClassifierReference(this, it) }
            }
    }

    private fun collectTSReferencesForMetaEnum(
        fullHeaderParameter: ImpexFullHeaderParameter,
        attributeMeta: TSGlobalMetaEnum,
        attributeType: String
    ): List<PsiReference>? {
        val ranges = collectRanges(fullHeaderParameter, AttributeModifier.PATH_DELIMITER, ":")

        return fullHeaderParameter
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
                        val range = ranges.getOrNull(index)
                            ?.takeUnless { range -> range.isMacro(text) }
                            ?: return@let null

                        when {
                            it.name == HybrisConstants.ATTRIBUTE_CODE -> if (attributeMeta.isDynamic) ImpExValueTSDynamicEnumReference(this, attributeType, range)
                            else ImpExValueTSStaticEnumReference(this, attributeType, range)

                            it.type == HybrisConstants.TS_COMPOSED_TYPE -> ImpExValueTSClassifierReference(this, range)
                            else -> null
                        }
                    }
            }
            ?.take(getFieldValues().size)
    }

    private fun collectTSReferencesForMetaCollection(
        fullHeaderParameter: ImpexFullHeaderParameter,
        attributeMeta: TSGlobalMetaCollection,
        metaModelAccess: TSMetaModelAccess
    ): List<PsiReference>? {
        return metaModelAccess.findMetaClassifierByName(attributeMeta.elementType)
            ?.let { targetMeta ->
                when {
                    targetMeta is TSGlobalMetaItem && targetMeta.name == HybrisConstants.TS_COMPOSED_TYPE -> collectRanges(
                        fullHeaderParameter,
                        AttributeModifier.COLLECTION_DELIMITER,
                        ","
                    )
                        .filterNot { range -> range.isMacro(text) }
                        .map { ImpExValueTSClassifierReference(this, it) }

                    else -> null
                }
            }
    }

    private fun collectRanges(fullHeaderParameter: ImpexFullHeaderParameter, modifier: AttributeModifier, defaultDelimiter: String): List<TextRange> {
        val delimiter = fullHeaderParameter.getAttributeValue(modifier, defaultDelimiter)

        return buildList {
            var previousStart = 0

            text.split(delimiter).forEachIndexed { index, part ->
                val partTrimmed = part.trim()
                val trimStart = part.trimStart()
                val startWhitespaces = part.length - trimStart.length

                val start = startWhitespaces + previousStart
                val end = start + partTrimmed.length

                previousStart += part.length + delimiter.length

                add(TextRange.create(start, end))
            }
        }
    }

    private fun getAttribute(fullHeaderParameter: ImpexFullHeaderParameter) = fullHeaderParameter
        .anyHeaderParameterName
        .reference
        ?.asSafely<ImpexTSAttributeReference>()
        ?.multiResolve(false)
        ?.firstOrNull()

    private fun getFieldValues(): Array<PsiElement> = findChildrenByType(ImpexTypes.FIELD_VALUE, PsiElement::class.java)

    companion object {
        @Serial
        private val serialVersionUID: Long = 8258794639693010240L
    }

}

private fun TextRange.isMacro(text: String): Boolean = substring(text)
    .startsWith(HybrisConstants.IMPEX_PREFIX_MACRO)
