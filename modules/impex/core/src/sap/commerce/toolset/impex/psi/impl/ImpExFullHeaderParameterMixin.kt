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

    override fun getTypeSystemContext(): ImpExFullHeaderParameterTSContext? {
        val meta: TSMetaClassifier<out DomElement> = resolveTSAttribute()
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

        return ImpExFullHeaderParameterTSContext(meta, attributeType)
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

    override fun isUnique() = this.getExpandedAttributes()
        .lastOrNull { it.anyAttributeName.textMatches(AttributeModifier.UNIQUE.modifierName) }
        ?.anyAttributeValue?.textMatches("true") ?: false

    override fun getAttribute(attributeModifier: AttributeModifier): ImpExAttribute? = this.modifiersList
        .flatMap { it.attributeList }
        .lastOrNull { it.anyAttributeName.textMatches(attributeModifier.modifierName) }

    override fun getAttributeValue(attributeModifier: AttributeModifier, defaultValue: String): String = getExpandedAttributes()
        .lastOrNull { it.anyAttributeName.textMatches(attributeModifier.modifierName) }
        ?.anyAttributeValue
        ?.childLeafs()
        ?.joinToString("") {
            if (it.elementType == ImpExTypes.MACRO_USAGE) it.parentOfType<ImpExMacroUsageDec>()
                ?.resolveValue(HashSet())
                ?: it.text
            else it.text
        }
        ?: defaultValue

    override fun collectDocIdReferences(
        targetElement: PsiElement,
        tsContext: ImpExFullHeaderParameterTSContext,
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
        tsContext: ImpExFullHeaderParameterTSContext,
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

    // In contrast to the "getAttribute" function, this function will expand any macros, create new temporary file and recreate FullHeaderParameter
    // due that, it will NOT BE PART of the original PsiTree and MUST NOT be accesses outside of this internal logic.
    private fun getExpandedAttributes(): List<ImpExAttribute> = CachedValuesManager.getManager(project)
        .getCachedValue(this, CACHE_KEY_EXPANDED_ATTRIBUTES, {
            val macroExpandedParameter = anyHeaderParameterName
                .takeIf { it.macroUsageDecList.isNotEmpty() }
                ?.children
                ?.joinToString("") {
                    when (it) {
                        is ImpExMacroUsageDec -> it.resolveValue(HashSet())
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
                    val locallyExpandedParameterWithModifiers = locallyExpandedParameter + this.modifiersList
                        .joinToString { it.text }
                    ImpExElementFactory.createFullHeaderParameter(project, macros, locallyExpandedParameterWithModifiers)
                }
                ?: this

            val expandedAttribute = macroExpandedParameter.modifiersList
                .flatMap { it.attributeList }

            CachedValueProvider.Result.createSingleDependency(
                expandedAttribute,
                this@ImpExFullHeaderParameterMixin
            )
        }, false)

    private fun resolveTSAttribute() = this
        .anyHeaderParameterName
        .reference
        ?.asSafely<ImpExTSAttributeReference>()
        ?.multiResolve(false)
        ?.firstOrNull()

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

    companion object {
        val CACHE_KEY_COLUMN_NUMBER = Key.create<CachedValue<Int>>("SAP_CX_IMPEX_COLUMN_NUMBER")
        val CACHE_KEY_VALUE_GROUPS = Key.create<CachedValue<List<ImpExValueGroup>>>("SAP_CX_IMPEX_VALUE_GROUPS")
        val CACHE_KEY_EXPANDED_ATTRIBUTES = Key.create<CachedValue<List<ImpExAttribute>>>("SAP_CX_IMPEX_EXPANDED_ATTRIBUTE")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}