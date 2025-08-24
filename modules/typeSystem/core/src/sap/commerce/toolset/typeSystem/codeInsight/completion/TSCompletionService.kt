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

package sap.commerce.toolset.typeSystem.codeInsight.completion

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.StringUtils
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.settings.yDeveloperSettings
import sap.commerce.toolset.typeSystem.codeInsight.lookup.TSLookupElementFactory
import sap.commerce.toolset.typeSystem.meta.TSMetaHelper
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.*
import java.util.*

@Service(Service.Level.PROJECT)
class TSCompletionService(private val project: Project) {

    /**
     * This method should return lookup elements for possible type code, it can be Item/Enum or Relation
     */
    fun getCompletions(typeCode: String) = getCompletions(
        typeCode,
        TSMetaType.META_ITEM, TSMetaType.META_ENUM, TSMetaType.META_RELATION, TSMetaType.META_COLLECTION, TSMetaType.META_MAP
    )

    fun getCompletions(typeCode: String, vararg types: TSMetaType) = getCompletions(
        typeCode,
        0, *types
    )

    fun getCompletions(vararg types: TSMetaType) = with(TSMetaModelAccess.getInstance(project)) {
        types
            .map { metaType ->
                when (metaType) {
                    TSMetaType.META_ITEM -> this
                        .getAll<TSGlobalMetaItem>(metaType)
                        .mapNotNull { TSLookupElementFactory.build(it) }

                    TSMetaType.META_ENUM -> this
                        .getAll<TSGlobalMetaEnum>(metaType)
                        .mapNotNull { TSLookupElementFactory.build(it, it.name) }

                    TSMetaType.META_RELATION -> this
                        .getAll<TSGlobalMetaRelation>(metaType)
                        .mapNotNull { TSLookupElementFactory.build(it) }

                    TSMetaType.META_COLLECTION -> this
                        .getAll<TSGlobalMetaCollection>(metaType)
                        .mapNotNull { TSLookupElementFactory.build(it) }

                    TSMetaType.META_MAP -> this
                        .getAll<TSGlobalMetaMap>(metaType)
                        .mapNotNull { TSLookupElementFactory.build(it) }

                    else -> emptyList()
                }
            }
            .flatten()
    }

    fun getCompletions(meta: TSGlobalMetaEnum) = meta.values.values
        .map { TSLookupElementFactory.build(it) }

    fun getItemMetaTypeCompletions() = getMetaTypeCompletions { TSMetaHelper.isItemMetaType(it) }
    fun getItemAttributeMetaTypeCompletions() = getMetaTypeCompletions { TSMetaHelper.isItemAttributeMetaType(it) }
    fun getRelationElementMetaTypeCompletions() = getMetaTypeCompletions { TSMetaHelper.isRelationElementMetaType(it) }

    private fun getMetaTypeCompletions(filterByMetaType: (TSGlobalMetaItem) -> Boolean) = TSMetaModelAccess.getInstance(project)
        .getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)
        .filter(filterByMetaType)
        .mapNotNull { TSLookupElementFactory.build(it) }

    fun getSubTypeCompletions(
        project: Project,
        referenceItemTypeName: String?,
        inlineTypeName: String?
    ): List<LookupElement> {
        val completion = project.yDeveloperSettings.impexSettings.completion
        if (!completion.showInlineTypes) return emptyList()

        val referenceItemTypeName = referenceItemTypeName ?: return emptyList()
        val suffix = if (inlineTypeName == null && completion.addCommaAfterInlineType) "."
        else ""

        return TSMetaModelAccess.getInstance(project).findMetaItemByName(referenceItemTypeName)
            ?.hierarchy
            ?.mapNotNull {
                TSLookupElementFactory.build(it, suffix)
                    ?.withTypeText(" child of $referenceItemTypeName", true)
            }
            ?.map { PrioritizedLookupElement.withPriority(it, TSLookupElementFactory.PRIORITY_2_0) }
            ?.map { PrioritizedLookupElement.withGrouping(it, TSLookupElementFactory.GROUP_2) }
            ?: emptyList()
    }

    /**
     * See: https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/2fb5a2a780c94325b4a48ff62b36ab23.html#using-header-abbreviations
     */
    fun getHeaderAbbreviationCompletions(project: Project) = PropertyService.getInstance(project)
        .findAutoCompleteProperties(HybrisConstants.PROPERTY_IMPEX_HEADER_REPLACEMENT)
        .mapNotNull { it.value }
        .mapNotNull { abbreviation ->
            abbreviation
                .split("...")
                .takeIf { it.size == 2 }
                ?.map { it.trim() }
        }
        .mapNotNull { it.firstOrNull() }
        .map { TSLookupElementFactory.buildHeaderAbbreviation(it) }

    private fun getCompletions(typeCode: String, recursionLevel: Int, vararg types: TSMetaType): List<LookupElementBuilder> {
        if (recursionLevel > HybrisConstants.TS_MAX_RECURSION_LEVEL) return emptyList()

        val metaService = TSMetaModelAccess.getInstance(project)
        return types
            .firstNotNullOfOrNull { metaType ->
                when (metaType) {
                    TSMetaType.META_ITEM -> metaService.findMetaItemByName(typeCode)
                        ?.let { getCompletions(it) }

                    TSMetaType.META_ENUM -> metaService.findMetaEnumByName(typeCode)
                        ?.let { getCompletionsForEnum(metaService) }

                    TSMetaType.META_RELATION -> metaService.findMetaRelationByName(typeCode)
                        ?.let { getCompletions(it, metaService) }

                    TSMetaType.META_COLLECTION -> metaService.findMetaCollectionByName(typeCode)
                        ?.let { getCompletions(it.elementType, recursionLevel + 1, *types) }

                    TSMetaType.META_MAP -> metaService.findMetaMapByName(typeCode)
                        ?.let { getCompletions(it) }

                    else -> null
                }
            }
            ?: emptyList()
    }

    private fun getCompletionsForEnum(metaModelAccess: TSMetaModelAccess) = metaModelAccess.findMetaItemByName(HybrisConstants.TS_TYPE_ENUMERATION_VALUE)
        ?.allAttributes
        ?.values
        ?.map { TSLookupElementFactory.build(it) }
        ?: emptyList()

    private fun getCompletions(metaRelation: TSGlobalMetaRelation, metaService: TSMetaModelAccess): List<LookupElementBuilder> {
        val linkMetaItem = metaService.findMetaItemByName(HybrisConstants.TS_TYPE_LINK) ?: return emptyList()
        val completions = LinkedList(getCompletions(linkMetaItem, setOf(HybrisConstants.ATTRIBUTE_SOURCE, HybrisConstants.ATTRIBUTE_TARGET)))
        completions.add(TSLookupElementFactory.build(metaRelation.source, HybrisConstants.ATTRIBUTE_SOURCE))
        completions.add(TSLookupElementFactory.build(metaRelation.target, HybrisConstants.ATTRIBUTE_TARGET))
        return completions
    }

    private fun getCompletions(metaMap: TSGlobalMetaMap) = listOf(
        TSLookupElementFactory.build(metaMap.argumentType, HybrisConstants.ATTRIBUTE_KEY),
        TSLookupElementFactory.build(metaMap.returnType, HybrisConstants.ATTRIBUTE_VALUE),
    )

    private fun getCompletions(metaItem: TSGlobalMetaItem) = getCompletions(metaItem, emptySet())

    private fun getCompletions(metaItem: TSGlobalMetaItem, excludeNames: Set<String>): List<LookupElementBuilder> {
        val attributes = metaItem.allAttributes.values
            .mapNotNull { mapAttributeToLookup(excludeNames, it) }
        val orderingAttributes = metaItem.allOrderingAttributes.values
            .map { TSLookupElementFactory.build(it) }
        val relationEnds = metaItem.allRelationEnds
            .mapNotNull { TSLookupElementFactory.build(it) }
        return attributes + orderingAttributes + relationEnds
    }

    private fun mapAttributeToLookup(
        excludeNames: Set<String>,
        attribute: TSGlobalMetaItem.TSGlobalMetaItemAttribute
    ): LookupElementBuilder? {
        val name = attribute.name
        return if (StringUtils.isBlank(name) || excludeNames.contains(name.trim { it <= ' ' })) {
            null
        } else TSLookupElementFactory.build(attribute, name)
    }


    companion object {
        fun getInstance(project: Project): TSCompletionService = project.service()
    }

}