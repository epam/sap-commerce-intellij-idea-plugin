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
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.psi.references.*
import sap.commerce.toolset.impex.utils.ImpExPsiUtils
import sap.commerce.toolset.typeSystem.TSConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.*
import sap.commerce.toolset.typeSystem.model.Cardinality
import sap.commerce.toolset.typeSystem.psi.reference.result.AttributeResolveResult
import sap.commerce.toolset.typeSystem.psi.reference.result.RelationEndResolveResult
import java.io.Serial

abstract class ImpExFullHeaderParameterMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExFullHeaderParameter {

    override fun getColumnNumber(): Int = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_COLUMN_NUMBER, {
        val columnNumber = ImpExPsiUtils.getColumnNumber(this)

        CachedValueProvider.Result.createSingleDependency(
            columnNumber,
            this
        )
    }, false)

    override fun getTypeSystemContext(): ImpExHeaderParameterTSContext? {
        val meta: TSMetaClassifier<out DomElement> = this
            .anyHeaderParameterName
            .reference
            ?.asSafely<ImpExTSAttributeReference>()
            ?.multiResolve(false)
            ?.firstOrNull()
            .let {
                when (it) {
                    is AttributeResolveResult -> it.meta
                    is RelationEndResolveResult -> it.meta
                    else -> return null
                }
            }

        val attributeType = when (meta) {
            is TSGlobalMetaItem.TSGlobalMetaItemAttribute -> meta.type
            is TSMetaRelation.TSMetaRelationElement -> meta.type
            else -> null
        } ?: return null

        return ImpExHeaderParameterTSContext(meta, attributeType)
    }

    override fun getValueGroups(): List<ImpExValueGroup> = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_VALUE_GROUPS, {
        val valueGroups = this
            .headerLine
            ?.valueLines
            ?.mapNotNull { it.getValueGroup(this.columnNumber) }
            ?: emptyList()

        CachedValueProvider.Result.createSingleDependency(
            valueGroups,
            this
        )
    }, false)

    override fun isUnique() = this.getParametersContext()
        .rootParameter.attributes[AttributeModifier.UNIQUE.modifierName]
        ?.resolvedValue == "true"

    override fun getAttribute(attributeModifier: AttributeModifier): ImpExAttribute? = this.modifiersList
        .flatMap { it.attributeList }
        .lastOrNull { it.anyAttributeName.textMatches(attributeModifier.modifierName) }

    override fun getAttributeValue(attributeModifier: AttributeModifier, defaultValue: String) = getParametersContext().rootParameter.getAttributeValue(
        attributeModifier,
        defaultValue
    )

    override fun collectDocIdReferences(
        targetElement: PsiElement,
        tsContext: ImpExHeaderParameterTSContext,
    ): Array<PsiReference>? {
        val cardinality = when (tsContext.meta) {
            is TSMetaRelation.TSMetaRelationElement -> tsContext.meta.cardinality
            is TSGlobalMetaItem.TSGlobalMetaItemAttribute -> when (TSMetaModelAccess.getInstance(project).findMetaClassifierByName(tsContext.attributeType)) {
                is TSGlobalMetaCollection -> Cardinality.MANY
                else -> Cardinality.ONE
            }

            else -> Cardinality.ONE
        }

        // ensure that column has single parameter as a &DocumentID
        this.parametersList
            .firstOrNull()
            ?.parameterList
            ?.takeIf { it.size == 1 }
            ?.firstOrNull()
            ?.childrenOfType<ImpExDocumentIdUsage>()
            ?.firstOrNull()
            ?: return null

        if (cardinality == Cardinality.ONE) {
            return TextRange(0, targetElement.textLength)
                .takeUnless { it.isMacro(targetElement.text) }
                ?.let { ImpExDocumentIdUsageReference.create(this, targetElement, it) }
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
        return collectRanges(targetElement, AttributeModifier.COLLECTION_DELIMITER, ",")
            .filterNot { it.isMacro(targetElement.text) }
            .map { ImpExDocumentIdUsageReference.create(this, targetElement, it) }
            .toTypedArray()
    }

    override fun collectTSReferences(
        targetElement: PsiElement,
        tsContext: ImpExHeaderParameterTSContext,
        valuesProvider: () -> Array<PsiElement>
    ): Array<PsiReference>? {
        val metaModelAccess = TSMetaModelAccess.getInstance(project)
        return metaModelAccess.findMetaClassifierByName(tsContext.attributeType)
            ?.let {
                when (it) {
                    is TSGlobalMetaAtomic -> collectTSReferencesForMetaAtomic(targetElement, tsContext.attributeType)
                    is TSGlobalMetaEnum -> collectTSReferencesForMetaEnum(targetElement, it, tsContext.attributeType, valuesProvider)
                    is TSGlobalMetaItem -> collectTSReferencesForMetaItem(targetElement, tsContext.meta, tsContext.attributeType)
                    is TSGlobalMetaCollection -> collectTSReferencesForMetaCollection(targetElement, it, metaModelAccess)
                    else -> null
                }
                    ?.toTypedArray()
            }
    }

    override fun resolveDefaultValue(): String? {
        val pathDelimiter = getAttributeValue(AttributeModifier.PATH_DELIMITER, ImpExConstants.PATH_DELIMITER)

        return getAttributeValue(AttributeModifier.DEFAULT, "")
            .takeIf { it.isNotEmpty() }
            ?: getParametersContext().flattenSubParameters
                .map { it.getAttributeValue(AttributeModifier.DEFAULT, "") }
                .filter { it.isNotEmpty() }
                .joinToString(pathDelimiter)
    }

    override fun getParametersContext(): ParametersContext = CachedValuesManager.getManager(project)
        .getCachedValue(this, CACHE_KEY_EXPANDED_ATTRIBUTES, {
            val realParameter = this@ImpExFullHeaderParameterMixin
            val headerTypeName = this.headerLine?.fullHeaderType?.headerTypeName?.text ?: "T"
            val expandedParameter = anyHeaderParameterName
                .takeIf { it.macroUsageDecList.isNotEmpty() }
                ?.children
                ?.joinToString("") {
                    when (it) {
                        is ImpExMacroUsageDec -> it.resolveValue(HashSet())
                        is ImpExPossibleMacroUsageDec -> it.resolveValue()
                        else -> it.text
                    }
                }
                ?.let { locallyExpandedParameter ->
                    val macros = buildString {
                        PsiTreeUtil.processElements(anyHeaderParameterName.containingFile) { element ->
                            if (element == this) return@processElements false
                            if (element is ImpExMacroDeclaration) this@buildString.appendLine(element.text)
                            return@processElements true
                        }
                    }
                    val locallyExpandedParameterWithModifiers = locallyExpandedParameter + realParameter.modifiersList
                        .joinToString("") { it.text }
                    ImpExElementFactory.createFullHeaderParameter(project, headerTypeName, macros, locallyExpandedParameterWithModifiers)
                }
                ?: realParameter

            val expandedAttributes = ParametersContext(
                rootParameter = parameter(
                    name = expandedParameter.anyHeaderParameterName.text,
                    modifiers = expandedParameter.modifiersList,
                    tsContext = expandedParameter.typeSystemContext,
                ),
                subParameters = expandedParameter.parametersList
                    .firstOrNull()
                    ?.parameterList
                    ?.map { parameter(it.attributeName, it.modifiersList, it.typeSystemContext, it.subParameters) }
            )

            CachedValueProvider.Result.createSingleDependency(
                expandedAttributes,
                realParameter
            )
        }, false)

    private fun parameter(
        name: String,
        modifiers: List<ImpExModifiers>,
        tsContext: ImpExHeaderParameterTSContext?,
        subParameters: ImpExSubParameters? = null
    ): ParametersContext.Parameter {
        val attributes = modifiers
            .flatMap { it.attributeList }
            .associate { attribute ->
                val name = attribute.anyAttributeName.text
                val rawValue = attribute.anyAttributeValue?.text
                val resolvedValue = attribute.anyAttributeValue
                    ?.childLeafs()
                    ?.joinToString("") {
                        if (it.elementType == ImpExTypes.MACRO_USAGE) it.parentOfType<ImpExMacroUsageDec>()
                            ?.resolveValue(HashSet())
                            ?: it.text
                        else if (it.elementType == ImpExTypes.POSSIBLE_MACRO_USAGE) it.parentOfType<ImpExPossibleMacroUsageDec>()
                            ?.resolveValue()
                            ?: it.text
                        else it.text
                    }
                name to ParametersContext.Attribute(
                    name = name,
                    rawValue = rawValue,
                    resolvedValue = resolvedValue
                )
            }
        return ParametersContext.Parameter(
            name = name,
            metaContext = tsContext,
            attributes = attributes,
            subParameters = subParameters?.parameterList
                ?.map { parameter(it.attributeName, it.modifiersList, it.typeSystemContext, it.subParameters) }
        )
    }

    private fun collectRanges(targetElement: PsiElement, modifier: AttributeModifier, defaultDelimiter: String): List<TextRange> {
        val delimiter = getAttributeValue(modifier, defaultDelimiter)

        return buildList {
            var previousStart = 0

            targetElement.text.split(delimiter).forEachIndexed { _, part ->
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

    private fun collectTSReferencesForMetaItem(
        targetElement: PsiElement,
        meta: TSMetaClassifier<out DomElement>,
        attributeType: String
    ): List<PsiReference>? {
        val parameters = this.parametersList
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
                        collectRanges(targetElement, AttributeModifier.COLLECTION_DELIMITER, ",")
                            .map { ImpExValueTSClassifierReference(targetElement, it) }
                    } else {
                        /**
                         * UPDATE BundleTemplateStatus[batchmode = true]; itemtype(code)[unique = true]
                         *                                              ; Address
                         *
                         * To be injected into -> Address
                         */
                        listOf(ImpExValueTSClassifierReference(targetElement, TextRange.create(0, targetElement.textLength)))
                    }
                }
        }

        /**
         * INSERT AttributeConstraint; descriptor(enclosingType(code), qualifier)
         *                           ; ConsumedDestination:url
         *
         * To be injected into -> ConsumedDestination
         */

        val ranges = collectRanges(targetElement, AttributeModifier.PATH_DELIMITER, ImpExConstants.PATH_DELIMITER)
        return parameters
            .mapIndexedNotNull { index, parameter ->
                val targetMetaType = parameter.reference.asSafely<ImpExFunctionTSAttributeReference>()
                    ?.multiResolve(false)
                    ?.firstOrNull()
                    ?.let { resolveResult ->
                        when (resolveResult) {
                            is AttributeResolveResult -> resolveResult.meta.type
                            is RelationEndResolveResult -> resolveResult.meta.type
                            else -> null
                        }
                    } ?: return@mapIndexedNotNull null
                val targetRange = ranges.getOrNull(index) ?: return@mapIndexedNotNull null

                when {
                    targetMetaType == TSConstants.Type.COMPOSED_TYPE -> ImpExValueTSClassifierReference(targetElement, targetRange)

                    TSConstants.Type.ATTRIBUTE_DESCRIPTOR == attributeType
                        && TSConstants.Attribute.QUALIFIER.equals(parameter.text, true)
                        && targetMetaType == TSConstants.Type.JAVA_STRING -> ImpExValueTSAttributeReference(targetElement, targetRange)

                    else -> null
                }
            }
    }

    private fun collectTSReferencesForMetaAtomic(targetElement: PsiElement, attributeType: String): List<PsiReference>? {
        if (TSConstants.Type.JAVA_CLASS != attributeType) return null

        return if (text.startsWith(ImpExConstants.MACRO_MARKER)) null
        else listOf(ImpExJavaClassReference(targetElement))
    }

    private fun collectTSReferencesForMetaEnum(
        targetElement: PsiElement,
        attributeMeta: TSGlobalMetaEnum,
        attributeType: String,
        valuesProvider: () -> Array<PsiElement>
    ): List<PsiReference>? {
        val ranges = collectRanges(targetElement, AttributeModifier.PATH_DELIMITER, ImpExConstants.PATH_DELIMITER)

        return this.parametersList
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
                            ?.takeUnless { range -> range.isMacro(targetElement.text) }
                            ?: return@let null

                        when {
                            it.name == TSConstants.Attribute.CODE -> if (attributeMeta.isDynamic) ImpExValueTSDynamicEnumReference(targetElement, attributeType, range)
                            else ImpExValueTSStaticEnumReference(targetElement, attributeType, range)

                            it.type == TSConstants.Type.COMPOSED_TYPE -> ImpExValueTSClassifierReference(targetElement, range)
                            else -> null
                        }
                    }
            }
            ?.take(valuesProvider().size)
    }

    private fun collectTSReferencesForMetaCollection(
        targetElement: PsiElement,
        attributeMeta: TSGlobalMetaCollection,
        metaModelAccess: TSMetaModelAccess
    ): List<PsiReference>? {
        return metaModelAccess.findMetaClassifierByName(attributeMeta.elementType)
            ?.let { targetMeta ->
                when {
                    targetMeta is TSGlobalMetaItem && targetMeta.name == TSConstants.Type.COMPOSED_TYPE -> collectRanges(
                        targetElement,
                        AttributeModifier.COLLECTION_DELIMITER,
                        ImpExConstants.COLLECTION_DELIMITER
                    )
                        .filterNot { range -> range.isMacro(targetElement.text) }
                        .map { ImpExValueTSClassifierReference(targetElement, it) }

                    else -> null
                }
            }
    }

    private fun TextRange.isMacro(text: String): Boolean = substring(text)
        .startsWith(ImpExConstants.MACRO_MARKER)


    data class ParametersContext(
        val rootParameter: Parameter,
        val subParameters: List<Parameter>?
    ) {
        val flattenSubParameters: List<Parameter>
            get() = subParameters.orEmpty().flatMap { it.flatten() }

        data class Parameter(
            val name: String,
            val metaContext: ImpExHeaderParameterTSContext?,
            val attributes: Map<String, Attribute>,
            val subParameters: List<Parameter>?
        ) {
            fun flatten(): List<Parameter> =
                listOf(this) + subParameters.orEmpty().flatMap { it.flatten() }

            fun getAttributeValue(attributeModifier: AttributeModifier, defaultValue: String): String = attributes[attributeModifier.modifierName]
                ?.resolvedValue
                ?: defaultValue
        }

        data class Attribute(
            val name: String,
            val rawValue: String?,
            val resolvedValue: String?,
        )
    }

    companion object {
        private val CACHE_KEY_EXPANDED_ATTRIBUTES = Key.create<CachedValue<ParametersContext>>("SAP_CX_IMPEX_EXPANDED_ATTRIBUTE")
        val CACHE_KEY_COLUMN_NUMBER = Key.create<CachedValue<Int>>("SAP_CX_IMPEX_COLUMN_NUMBER")
        val CACHE_KEY_VALUE_GROUPS = Key.create<CachedValue<List<ImpExValueGroup>>>("SAP_CX_IMPEX_VALUE_GROUPS")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}