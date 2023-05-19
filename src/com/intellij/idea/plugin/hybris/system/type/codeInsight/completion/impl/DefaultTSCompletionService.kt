/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_SOURCE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ATTRIBUTE_TARGET
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
        TSMetaType.META_ITEM, TSMetaType.META_ENUM, TSMetaType.META_RELATION
    )

    override fun getCompletions(typeCode: String, vararg types: TSMetaType) = with(TSMetaModelAccess.getInstance(project)) {
        types
            .firstNotNullOfOrNull { metaType ->
                when (metaType) {
                    TSMetaType.META_ITEM -> this.findMetaItemByName(typeCode)
                        ?.let { getCompletions(it) }

                    TSMetaType.META_ENUM -> this.findMetaEnumByName(typeCode)
                        ?.let { getCompletionsForEnum(this) }

                    TSMetaType.META_RELATION -> this.findMetaRelationByName(typeCode)
                        ?.let { getCompletions(it, this) }

                    else -> null
                }
            }
            ?: emptyList()
    }

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

    private fun getCompletionsForEnum(metaModelAccess: TSMetaModelAccess) = metaModelAccess.findMetaItemByName(HybrisConstants.TS_TYPE_ENUMERATION_VALUE)
        ?.allAttributes
        ?.map { TSLookupElementFactory.build(it) }
        ?: emptyList()

    private fun getCompletions(metaRelation: TSGlobalMetaRelation, metaService: TSMetaModelAccess): List<LookupElementBuilder> {
        val linkMetaItem = metaService.findMetaItemByName(HybrisConstants.TS_TYPE_LINK) ?: return emptyList()
        val completions = LinkedList(getCompletions(linkMetaItem, setOf(ATTRIBUTE_SOURCE, ATTRIBUTE_TARGET)))
        completions.add(TSLookupElementFactory.build(metaRelation.source, ATTRIBUTE_SOURCE))
        completions.add(TSLookupElementFactory.build(metaRelation.target, ATTRIBUTE_TARGET))
        return completions
    }

    private fun getCompletions(metaItem: TSGlobalMetaItem) = getCompletions(metaItem, emptySet())

    private fun getCompletions(metaItem: TSGlobalMetaItem, excludeNames: Set<String>): List<LookupElementBuilder> {
        val attributes = metaItem.allAttributes
            .mapNotNull { mapAttributeToLookup(excludeNames, it) }
        val relationEnds = metaItem.allRelationEnds
            .mapNotNull { TSLookupElementFactory.build(it) }
        return attributes + relationEnds
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