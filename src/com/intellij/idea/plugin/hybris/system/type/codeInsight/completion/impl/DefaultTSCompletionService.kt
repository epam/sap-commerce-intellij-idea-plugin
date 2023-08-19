/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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
package com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.impl

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_KEY
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_SOURCE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_TARGET
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_VALUE
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter
import com.intellij.idea.plugin.hybris.impex.utils.ProjectPropertiesUtils
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.idea.plugin.hybris.system.type.codeInsight.lookup.TSLookupElementFactory
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem.TSGlobalMetaItemAttribute
import com.intellij.openapi.project.Project
import org.apache.commons.lang.StringUtils
import java.util.*

class DefaultTSCompletionService(private val project: Project) : TSCompletionService {

    override fun getCompletions(typeCode: String) = getCompletions(
        typeCode,
        TSMetaType.META_ITEM, TSMetaType.META_ENUM, TSMetaType.META_RELATION, TSMetaType.META_COLLECTION, TSMetaType.META_MAP
    )

    override fun getCompletions(typeCode: String, vararg types: TSMetaType) = getCompletions(
        typeCode,
        0, *types
    )

    override fun getCompletions(vararg types: TSMetaType) = with(TSMetaModelAccess.getInstance(project)) {
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

    override fun getImpexInlineTypeCompletions(project: Project, element: ImpexParameter): List<LookupElementBuilder> {
        val completion = HybrisProjectSettingsComponent.getInstance(project).state.impexSettings.completion
        if (!completion.showInlineTypes) return emptyList()

        val referenceItemTypeName = element.referenceItemTypeName ?: return emptyList()
        val suffix = if (element.inlineTypeName == null && completion.addCommaAfterInlineType) {
            "."
        } else {
            ""
        }

        val metaModelAccess = TSMetaModelAccess.getInstance(project)
        metaModelAccess.findMetaItemByName(referenceItemTypeName)
            ?: return emptyList()

        return metaModelAccess.getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)
            .filter { meta ->
                meta.allExtends.find { it.name.equals(referenceItemTypeName, true) } != null
                    // or itself, it will be highlighted as unnecessary via Inspection
                    || meta.name.equals(referenceItemTypeName, true)
            }
            .mapNotNull {
                TSLookupElementFactory.build(it, suffix)
            }
    }

    override fun getHeaderAbbreviationCompletions() = ProjectPropertiesUtils.findAutoCompleteProperties(project, HybrisConstants.PROPERTY_IMPEX_HEADER_REPLACEMENT)
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
        val completions = LinkedList(getCompletions(linkMetaItem, setOf(ATTRIBUTE_SOURCE, ATTRIBUTE_TARGET)))
        completions.add(TSLookupElementFactory.build(metaRelation.source, ATTRIBUTE_SOURCE))
        completions.add(TSLookupElementFactory.build(metaRelation.target, ATTRIBUTE_TARGET))
        return completions
    }

    private fun getCompletions(metaMap: TSGlobalMetaMap) = listOf(
        TSLookupElementFactory.build(metaMap.argumentType, ATTRIBUTE_KEY),
        TSLookupElementFactory.build(metaMap.returnType, ATTRIBUTE_VALUE),
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
        attribute: TSGlobalMetaItemAttribute
    ): LookupElementBuilder? {
        val name = attribute.name
        return if (StringUtils.isBlank(name) || excludeNames.contains(name.trim { it <= ' ' })) {
            null
        } else TSLookupElementFactory.build(attribute, name)
    }

}