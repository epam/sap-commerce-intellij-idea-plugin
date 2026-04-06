/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiReference
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.psi.ImpExValue
import sap.commerce.toolset.impex.psi.references.*
import sap.commerce.toolset.spring.SpringFallbackScope
import sap.commerce.toolset.spring.psi.reference.SpringReference
import sap.commerce.toolset.typeSystem.TSConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import sap.commerce.toolset.typeSystem.meta.model.*
import sap.commerce.toolset.typeSystem.model.Cardinality
import sap.commerce.toolset.typeSystem.psi.reference.result.AttributeResolveResult
import sap.commerce.toolset.typeSystem.psi.reference.result.RelationEndResolveResult
import java.io.Serial

abstract class ImpExValueMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost, ImpExValue {

    override fun isValidHost() = true
    override fun updateText(text: String) = this
    override fun createLiteralTextEscaper() = LiteralTextEscaper.createSimple(this)

    override fun getFieldValue(index: Int): PsiElement? = getFieldValues()
        .getOrNull(index)

    override fun getReferences(): Array<PsiReference> = CachedValuesManager.getManager(project).getCachedValue(this) {
        CachedValueProvider.Result.create(
            collectReferences(),
            TSModificationTracker.getInstance(project), this
        )
    }

    override fun isNonImportable(): Boolean = !isImportable()

    override fun isQuotable(): Boolean {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return false
        if (trimmedText.startsWith('\"')) return false
        val leafs = childLeafs()
            .groupBy { it.elementType }

        return !(leafs.size == 1 && leafs[ImpExTypes.MACRO_USAGE]?.size == 1)
    }

    override fun isNotQuotable(): Boolean = !isQuotable

    override fun isImportable(): Boolean = firstLeaf().elementType
        .let { it != ImpExTypes.FIELD_VALUE_IGNORE && it != ImpExTypes.FIELD_VALUE_NULL }

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
            ?: collectTSReferences(fullHeaderParameter, meta, attributeType, metaModelAccess)
            ?: collectSpringReferences(meta)
            ?: emptyArray()
    }

    private fun collectSpringReferences(meta: TSMetaClassifier<out DomElement>): Array<PsiReference>? = meta.asSafely<TSGlobalMetaItem.TSGlobalMetaItemAttribute>()
        ?.takeIf { this.macroUsageDecList.isEmpty() }
        ?.takeIf { this.isImportable }
        ?.takeIf { attr ->
            val metaItem = attr.owner.name ?: return@takeIf false

            predefinedSpringReferenceAttributes.contains("$metaItem.${attr.name}".lowercase()) || predefinedSpringIdAttributes.contains(attr.name.lowercase())
        }
        ?.let { arrayOf(SpringReference(this, StringUtilRt.unquoteString(this.text), SpringFallbackScope.CUSTOM_MODULES)) }

    private fun collectDocIdValuesReferences(
        fullHeaderParameter: ImpExFullHeaderParameter,
        meta: TSMetaClassifier<out DomElement>,
        attributeType: String,
        metaModelAccess: TSMetaModelAccess,
    ): Array<PsiReference>? {
        val cardinality = when (meta) {
            is TSMetaRelation.TSMetaRelationElement -> meta.cardinality
            is TSGlobalMetaItem.TSGlobalMetaItemAttribute -> when (metaModelAccess.findMetaClassifierByName(attributeType)) {
                is TSGlobalMetaCollection -> Cardinality.MANY
                else -> Cardinality.ONE
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
            ?.childrenOfType<ImpExDocumentIdUsage>()
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
        fullHeaderParameter: ImpExFullHeaderParameter,
        meta: TSMetaClassifier<out DomElement>,
        attributeType: String,
        metaModelAccess: TSMetaModelAccess
    ) = metaModelAccess.findMetaClassifierByName(attributeType)
        ?.let {
            when (it) {
                is TSGlobalMetaAtomic -> collectTSReferencesForMetaAtomic(attributeType)
                is TSGlobalMetaEnum -> collectTSReferencesForMetaEnum(fullHeaderParameter, it, attributeType)
                is TSGlobalMetaItem -> collectTSReferencesForMetaItem(fullHeaderParameter, meta, attributeType)
                is TSGlobalMetaCollection -> collectTSReferencesForMetaCollection(fullHeaderParameter, it, metaModelAccess)
                else -> null
            }
                ?.toTypedArray()
        }

    private fun collectTSReferencesForMetaItem(fullHeaderParameter: ImpExFullHeaderParameter, meta: TSMetaClassifier<out DomElement>, attributeType: String): List<PsiReference>? {
        val parameters = fullHeaderParameter
            .parametersList
            .firstOrNull()
            ?.parameterList
            ?: return null

        if (TSConstants.Type.COMPOSED_TYPE == attributeType) {
            return parameters
                .takeIf { it.size == 1 }
                ?.firstOrNull()
                ?.takeIf { TSConstants.Attribute.CODE == it.text }
                ?.let { _ ->
                    if (meta is TSMetaRelation.TSMetaRelationElement && meta.cardinality == Cardinality.MANY) {
                        /**
                         * UPDATE CatalogVersionSyncJob; rootTypes(code)
                         *                             ; CMSItem, CMSRelation, Media, MediaContainer
                         */
                        collectRanges(fullHeaderParameter, AttributeModifier.COLLECTION_DELIMITER, ",")
                            .map { ImpExValueTSClassifierReference(this, it) }
                    } else {
                        /**
                         * UPDATE BundleTemplateStatus[batchmode = true]; itemtype(code)[unique = true]
                         *                                              ; Address
                         *
                         * To be injected into -> Address
                         */
                        listOf(ImpExValueTSClassifierReference(this, TextRange.create(0, textLength)))
                    }
                }
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
                parameter.reference.asSafely<ImpExFunctionTSAttributeReference>()
                    ?.multiResolve(false)
                    ?.firstOrNull()
                    ?.let { resolveResult ->
                        when (resolveResult) {
                            is AttributeResolveResult -> resolveResult.meta.type
                            is RelationEndResolveResult -> resolveResult.meta.type
                            else -> null
                        }
                    }
                    ?.takeIf { TSConstants.Type.COMPOSED_TYPE == it }
                    ?.let { ranges.getOrNull(index) }
                    ?.let { ImpExValueTSClassifierReference(this, it) }
            }
    }

    private fun collectTSReferencesForMetaAtomic(attributeType: String): List<PsiReference>? {
        if (TSConstants.Type.JAVA_CLASS != attributeType) return null

        return if (text.startsWith(ImpExConstants.MACRO_MARKER)) null
        else listOf(ImpExJavaClassReference(this))
    }

    private fun collectTSReferencesForMetaEnum(
        fullHeaderParameter: ImpExFullHeaderParameter,
        attributeMeta: TSGlobalMetaEnum,
        attributeType: String
    ): List<PsiReference>? {
        val ranges = collectRanges(fullHeaderParameter, AttributeModifier.PATH_DELIMITER, ":")

        return fullHeaderParameter
            .parametersList
            .firstOrNull()
            ?.parameterList
            ?.mapIndexedNotNull { index, parameter ->
                parameter.reference.asSafely<ImpExFunctionTSAttributeReference>()
                    ?.multiResolve(false)
                    ?.firstOrNull()
                    ?.asSafely<AttributeResolveResult>()
                    ?.meta
                    ?.let {
                        val range = ranges.getOrNull(index)
                            ?.takeUnless { range -> range.isMacro(text) }
                            ?: return@let null

                        when {
                            it.name == TSConstants.Attribute.CODE -> if (attributeMeta.isDynamic) ImpExValueTSDynamicEnumReference(this, attributeType, range)
                            else ImpExValueTSStaticEnumReference(this, attributeType, range)

                            it.type == TSConstants.Type.COMPOSED_TYPE -> ImpExValueTSClassifierReference(this, range)
                            else -> null
                        }
                    }
            }
            ?.take(getFieldValues().size)
    }

    private fun collectTSReferencesForMetaCollection(
        fullHeaderParameter: ImpExFullHeaderParameter,
        attributeMeta: TSGlobalMetaCollection,
        metaModelAccess: TSMetaModelAccess
    ): List<PsiReference>? {
        return metaModelAccess.findMetaClassifierByName(attributeMeta.elementType)
            ?.let { targetMeta ->
                when {
                    targetMeta is TSGlobalMetaItem && targetMeta.name == TSConstants.Type.COMPOSED_TYPE -> collectRanges(
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

    private fun collectRanges(fullHeaderParameter: ImpExFullHeaderParameter, modifier: AttributeModifier, defaultDelimiter: String): List<TextRange> {
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

    private fun getAttribute(fullHeaderParameter: ImpExFullHeaderParameter) = fullHeaderParameter
        .anyHeaderParameterName
        .reference
        ?.asSafely<ImpExTSAttributeReference>()
        ?.multiResolve(false)
        ?.firstOrNull()

    private fun getFieldValues(): Array<PsiElement> = findChildrenByType(ImpExTypes.FIELD_VALUE, PsiElement::class.java)

    @Suppress("SpellCheckingInspection")
    companion object {
        @Serial
        private val serialVersionUID: Long = 8258794639693010240L

        // Lowercased on purpose
        private val predefinedSpringReferenceAttributes = setOf(
            "solrindexedproperty.fieldvalueprovider",
            "solrindexedproperty.facetdisplaynameprovider",
            "solrindexedproperty.facetsortprovider",
            "solrindexedproperty.facettopvaluesprovider",
            "solrindexedproperty.topvaluesprovider",
            "solrindexedproperty.customFacetSortProvider",
            "solrsearchqueryproperty.facetdisplaynameprovider",
            "solrsearchqueryproperty.facetsortprovider",
            "solrsearchqueryproperty.facettopvaluesprovider",
            "solrsearchquerytemplate.ftsquerybuilder",
            "abstractasfacetconfiguration.valuesSortProvider",
            "abstractasfacetconfiguration.valuesdisplaynameprovider",
            "abstractasfacetconfiguration.topvaluesprovider",
            "solrextindexercronjob.queryparameterprovider",
            "solrindexedtype.identityprovider",
            "eventconfiguration.converterbean",
            "basestore.productsearchstrategy",
            "conversionmediaformat.conversionstrategy",
            "abstractrulebasedpromotionaction.strategyid",
            "distributedprocess.handlerbeanid",
            "automatedworkflowactiontemplate.jobhandler",
            "servicelayerjob.springidcronjobfactory",
        )
        private val predefinedSpringIdAttributes = setOf(
            "springid",
            "runnerbean"
        )
    }
}

private fun TextRange.isMacro(text: String): Boolean = substring(text)
    .startsWith(ImpExConstants.MACRO_MARKER)
