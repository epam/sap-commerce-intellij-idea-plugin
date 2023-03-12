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
import com.intellij.idea.plugin.hybris.common.HybrisConstants.CODE_ATTRIBUTE_NAME
import com.intellij.idea.plugin.hybris.common.HybrisConstants.NAME_ATTRIBUTE_NAME
import com.intellij.idea.plugin.hybris.common.HybrisConstants.SOURCE_ATTRIBUTE_NAME
import com.intellij.idea.plugin.hybris.common.HybrisConstants.TARGET_ATTRIBUTE_NAME
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem.TSGlobalMetaItemAttribute
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation.RelationEnd
import com.intellij.openapi.project.Project
import org.apache.commons.lang.StringUtils
import java.util.*

class DefaultTSCompletionService(private val project: Project) : TSCompletionService {

    override fun getCompletions(typeCode: String): List<LookupElementBuilder> {
        val metaService = TSMetaModelAccess.getInstance(project)

        return (metaService.findMetaItemByName(typeCode)
            ?.let { getCompletions(it) }
            ?: metaService.findMetaEnumByName(typeCode)
                ?.let { getCompletions() }
            ?: metaService.findMetaRelationByName(typeCode)
                ?.let { getCompletions(it, metaService) }
            ?: emptyList())
    }

    private fun getCompletions() = listOf(
        LookupElementBuilder.create(CODE_ATTRIBUTE_NAME)
            .withIcon(HybrisIcons.TS_ATTRIBUTE)
            .withTypeText("String", true),
        LookupElementBuilder.create(NAME_ATTRIBUTE_NAME)
            .withIcon(HybrisIcons.TS_ATTRIBUTE)
            .withTypeText("String", true)
    )

    private fun getCompletions(metaRelation: TSGlobalMetaRelation, metaService: TSMetaModelAccess): List<LookupElementBuilder> {
        val linkMetaItem = metaService.findMetaItemByName(HybrisConstants.TS_TYPE_LINK) ?: return emptyList()
        val completions = LinkedList(getCompletions(linkMetaItem, setOf(SOURCE_ATTRIBUTE_NAME, TARGET_ATTRIBUTE_NAME)))
        completions.add(
            LookupElementBuilder.create(SOURCE_ATTRIBUTE_NAME)
                .withIcon(HybrisIcons.TS_RELATION_SOURCE)
                .withStrikeoutness(metaRelation.source.isDeprecated)
                .withTypeText(metaRelation.source.flattenType, true)
        )
        completions.add(
            LookupElementBuilder.create(TARGET_ATTRIBUTE_NAME)
                .withIcon(HybrisIcons.TS_RELATION_TARGET)
                .withStrikeoutness(metaRelation.target.isDeprecated)
                .withTypeText(metaRelation.target.flattenType, true)
        )
        return completions
    }

    private fun getCompletions(metaItem: TSGlobalMetaItem) = getCompletions(metaItem, emptySet())

    private fun getCompletions(metaItem: TSGlobalMetaItem, excludeNames: Set<String>): List<LookupElementBuilder> {
        val attributes = metaItem.allAttributes
            .mapNotNull { mapAttributeToLookup(excludeNames, it) }
        val relationEnds = metaItem.allRelationEnds
            .filter { it.qualifier != null }
            .map {
                LookupElementBuilder.create(it.qualifier!!)
                    .withIcon(
                        when (it.end) {
                            RelationEnd.SOURCE -> HybrisIcons.TS_RELATION_SOURCE
                            RelationEnd.TARGET -> HybrisIcons.TS_RELATION_TARGET
                        }
                    )
                    .withTypeText(it.flattenType, true)
            }
        return attributes + relationEnds
    }

    private fun mapAttributeToLookup(
        excludeNames: Set<String>,
        attribute: TSGlobalMetaItemAttribute
    ): LookupElementBuilder? {
        val name = attribute.name
        return if (StringUtils.isBlank(name) || excludeNames.contains(name.trim { it <= ' ' })) {
            null
        } else LookupElementBuilder.create(name.trim { it <= ' ' })
            .withIcon(HybrisIcons.TS_ATTRIBUTE)
            .withTailText(if (attribute.isDynamic) " (" + HybrisI18NBundleUtils.message("hybris.ts.type.dynamic") + ')' else "", true)
            .withStrikeoutness(attribute.isDeprecated)
            .withTypeText(attribute.flattenType, true)
    }

}